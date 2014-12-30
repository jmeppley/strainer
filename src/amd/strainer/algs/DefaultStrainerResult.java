/****** BEGIN LICENSE BLOCK *****
 * This file is part of the Strainer application. To obtain or learn more 
 * about strainer visit: 
 *  http://bioinformatics.org/strainer
 * 
 * Copyright (c) 2007 The Regents of the University of California.  All rights
 * reserved.
 *
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published 
 * by the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version. You may not use this file except in 
 * compliance with the License. 
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  
 * USA or visit http://www.gnu.org/licenses/lgpl.html
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT,
 * INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING
 * LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
 * DOCUMENTATION, EVEN IF REGENTS HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 * 
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE. THE SOFTWARE AND ACCOMPANYING
 * DOCUMENTATION, IF ANY, PROVIDED HEREUNDER IS PROVIDED "AS
 * IS". REGENTS HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS. 
 *
 * Contributor(s):
 *  John Eppley <jmeppley@berkeley.edu>
 * 
 ***** END LICENSE BLOCK ***** */

package amd.strainer.algs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.biojava.bio.BioException;

import amd.strainer.algs.GeneCrawler.GCRead;
import amd.strainer.objects.Alignment;
import amd.strainer.objects.Gene;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * Contains result from an Auto-straining Algorithm. The important bits of information are:
 * <ul>
 * <li>The scope of the straining: defined by setSequecneSegment()
 * <li>The resulting groups of reads: getStrains() and getStrainIterator() 
 * @author jmeppley
 *
 */
public class DefaultStrainerResult implements StrainerResult {
	public DefaultStrainerResult(SequenceSegment pSegment, Set<Strain> pStrains) {
		mSequenceSegment = pSegment;
		mStrains = pStrains;
	}
	
	Set<Strain> mStrains = null;
	public Set<Strain> getStrains() { return mStrains; }
	public int size() { return mStrains.size(); }
	public Iterator<Strain> getStrainIterator() { return mStrains.iterator(); }
	
	SequenceSegment mSequenceSegment = null;
	public SequenceSegment getSequenceSegment() {
		return mSequenceSegment;
	}
	
	public void close() {
		for (Strain strain : mStrains) {
			Iterator rit = strain.getReadableIterator();
			while (rit.hasNext()) {
				((GCRead)rit.next()).close();
			}
			strain.getAlignment().setDiffs(null);
			strain.close();
		}
	}
	
	public void setStrainsSequences() {
		// should we compress strains to AA sequences (it means re-checking for uniqueness)
		//  the setting is global and it would be faster to require the algorithms to deal with this, but
		//  this set up is more flexible and doesn't slow things down TOO much
		boolean convertToAA = ((Boolean)Config.getConfig().getSettings().get(Config.CONVERT_TO_AA)).booleanValue();
		Map<String,Strain> usedSeqs = new HashMap<String,Strain>();
		Set<Strain> duplicates = new HashSet<Strain>();

		// do we want to fill in gaps using the ReferenceSequence
		boolean fillFromComposite = ((Boolean)Config.getConfig().getSettings().get(Config.FILL_FROM_COMPOSITE)).booleanValue();

		// figure out if we need to reverse complement
		boolean direction = true;
		if (mSequenceSegment instanceof Gene) {
			direction = ((Gene) mSequenceSegment).getDirection();
		}

		// set actual sequence of bases for each strain
		for (Strain strain : mStrains) {
			Alignment alig = strain.getAlignment();
			String bases = alig.getBases(fillFromComposite,
					alig.getPosFromReference(mSequenceSegment.getStart()),
					alig.getPosFromReference(mSequenceSegment.getEnd()));
			if (convertToAA) {
				try {
					// convert bases to sequence of Amino Acids
					bases = Util.getProteinSequence(bases,direction);

					Strain dup = findMatchingSequence(usedSeqs,bases);
					
					if (dup!=null) {
						duplicates.add(strain);
						// adjust size of first strain we found with this seq
						dup.putAllReads(strain);
					} else {
						usedSeqs.put(bases,strain);
					}
				} catch (BioException ex) {
					System.err.println("error converting to AA: " + ex.toString());
					bases = null;
				}
			}
			
			strain.setBases(bases);
		}

		// remove strains that are redundant in AA space
		if (convertToAA) {
			for (Strain strain : duplicates) {
				mStrains.remove(strain);
			}
		}
	}
	
	private Strain findMatchingSequence(Map<String, Strain> usedSeqs, String bases) {
		for (Map.Entry<String,Strain> seqPair : usedSeqs.entrySet()) {
			String seq = seqPair.getKey();
			
			if (seq.length() != bases.length()) {
				// different lengths ==> different
				continue;
			}
			
			for (int i=0; i<seq.length(); i++) {
				char schar = Character.toUpperCase(seq.charAt(i));
				char bchar = Character.toUpperCase(bases.charAt(i));
				if (schar!=bchar && schar!='X' && bchar!='X') {
					// bases don't match
					continue;
				}
			}
			
			// everything is the same
			return seqPair.getValue();
		}
				
		// no other strains matched
		return null;
	}
		
	/**
	 * Returns the fsata header identifying the given strain.  Looks like
	 * <pre>&gt;REFERENCESEQUENCE_GENE_INDEX LENGTH STRAIN_SIZE NUM_DIFFS</pre>
	 * 
	 * @param pStrain <code>Strain</code> in question 
	 * @param pGene <code>Gene</code> that defines the boundaries for creating the strain
	 * @param pIndex The number variant (for the given gene) that the given strain is
	 * @return
	 */
	public static String getFastaHeader(Strain pStrain, Gene pGene, int pIndex, String pGenePrefix) {
		StringBuffer geneName = new StringBuffer(pGenePrefix);
//		if (GlobalSettings.getGenePrefix()!=null) {
//			geneName.append(pGene.getReferenceSequence().getName()).append("_Gene_");
//		}
		geneName.append(pGene.getName());

		// attempt to parse details string
		StringTokenizer st = new StringTokenizer(pGene.getDescription()," ## ");
		String gene = "hyp";
		String function = "unknown";
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			int colonIndex = line.indexOf(":");
			if (colonIndex>0) {
				String key = line.substring(0,colonIndex).trim();
				if (key.equals("gene") || key.equals("name")) {
					gene = line.substring(colonIndex+1);
				} else if (key.equals(function)) {
					function = line.substring(colonIndex+1);
				}
			}
		}
		
		return ">" + geneName.toString() + "_v" + pIndex + 
		" # Gene_name:" + gene + " # Function:" + function + 
		" # length:" + pStrain.getBases().length() + " # reads:" + pStrain.getSize() + " diffs:" + pStrain.getAlignment().getDiffs().size();
	}
	
	public String toString() {
		StringBuffer ret = new StringBuffer("StrainerResuls of ")
		.append(mSequenceSegment.toString()).append(":");

		for (Strain strain : getStrains()) {
			ret.append("\n");
			ret.append(strain.toString());
		}
		
		return ret.toString();
	}
}
