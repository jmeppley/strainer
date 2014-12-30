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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

import org.biojava.bio.BioException;

import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Alignment;
import amd.strainer.objects.Gene;
import amd.strainer.objects.Sequence;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * Holds results from substrainer. SubStrainer lonly strains within already defined groups and reaturns a DefaultStrainerResult for
 * each group. This is a wrapper for the set of returned results that implements the general StrainerResult interface.
 * @author jmeppley
 *
 */
public class NestedStrainerResult implements StrainerResult {
	private Map<Strain,StrainerResult> results = new HashMap<Strain,StrainerResult>();
	private int size = 0;
	public int size() { return size; }
	public Iterator<Strain> getStrainIterator() {
		return this.new StrainIterator();
	}
	public void addResults(Strain strain, StrainerResult pSR) throws SegmentStrainerException {
		if (mSegment==null) {
			Sequence seq = pSR.getSequenceSegment().getSequence();
			if (seq instanceof AlignedSequence) {
				seq = ((AlignedSequence)seq).getAlignment().getSequenceSegment1().getSequence();
			}
			mSegment = new SequenceSegment(seq,pSR.getSequenceSegment().getStart(),pSR.getSequenceSegment().getEnd());
		}
		results.put(strain,pSR);
		size = size + pSR.size();
	}
	public Set<Strain> getStrains() {
		// StrainIterator does all the leg work here
		Set<Strain> strains = new HashSet<Strain>();
		Iterator<Strain> it = getStrainIterator();
		while(it.hasNext()) {
			strains.add(it.next());
		}
		return strains;
	}
	
	private SequenceSegment mSegment = null;
	public SequenceSegment getSequenceSegment() {
		return mSegment;
	}

	public void close() {
		for (StrainerResult sr : results.values()) {
			sr.close();
		}
	}

	public void setStrainsSequences() {
		// should we compress strains to AA sequences (it means re-checking for uniqueness)
		//  the setting is global and it would be faster to require the algorithms to deal with this, but
		//  this set up is more flexible and doesn't slow things down TOO much
		boolean convertToAA = ((Boolean)Config.getConfig().getSettings().get(Config.CONVERT_TO_AA)).booleanValue();
		Map<String,Strain> usedSeqs = new HashMap<String,Strain>();
		Map<Strain,Strain> duplicates = new HashMap<Strain,Strain>();

		// do we want to fill in gaps using the ReferenceSequence
		boolean fillFromComposite = ((Boolean)Config.getConfig().getSettings().get(Config.FILL_FROM_COMPOSITE)).booleanValue();

		// figure out if we need to reverse complement
		boolean direction = true;
		if (mSegment instanceof Gene) {
			direction = ((Gene) mSegment).getDirection();
		}

		// set actual sequence of bases for each strain
		for (Iterator<Entry<Strain,StrainerResult>> eit = results.entrySet().iterator(); eit.hasNext();) {
			Entry<Strain,StrainerResult> entry = eit.next();
			Strain originalStrain = entry.getKey();
			StrainerResult sr = entry.getValue();
			for (Iterator<Strain> sit = sr.getStrainIterator(); sit.hasNext();) {
				Strain strain = sit.next();
				Alignment alig = strain.getAlignment();
				String bases = getSubstrainBases(fillFromComposite, alig, originalStrain,
						alig.getPosFromReference(mSegment.getStart()),
						alig.getPosFromReference(mSegment.getEnd()));
				if (convertToAA) {
					try {
						// convert bases to sequence of Amino Acids
						bases = Util.getProteinSequence(bases,direction);
						
						Strain dup = usedSeqs.get(bases);
						if (dup!=null) {
							duplicates.put(strain,originalStrain);
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
		}

		// remove strains that are redundant in AA space
		if (convertToAA) {
			for (Strain strain : duplicates.keySet()) {
				Strain origStrain = duplicates.get(strain);
				StrainerResult sr = results.get(origStrain);
				sr.getStrains().remove(strain);
			}
		}
	}
	
	private String getSubstrainBases(boolean pFillFromComposite, Alignment alig, Strain originalStrain, int pStart, int pEnd) {
		StringBuffer sb = new StringBuffer((pEnd-pStart)+1);
		Sequence reference = alig.getSequenceSegment1().getSequence();
		
		Alignment strainAlig = originalStrain.getAlignment();
		
		for (int i = pStart; i <= pEnd; i++) {
			int rPos = alig.getReferencePos(i);
			
			// check to see if substrain includes this base
			if (alig.isUncovered(rPos)) {
				// if not
				// check strain
				if (strainAlig.isUncovered(rPos)) {
					//if not, fill
					if (pFillFromComposite) {
						sb.append(reference.getBase(rPos));
					} else {
						sb.append("n");
					}
				} else {
					// get base from strain
					
					// can't just getBase with the ref. pos, there might be a gap
					int strainBase = strainAlig.getPosFromReference(rPos);
					int nextStrainBase =  strainAlig.getPosFromReference(rPos+1);
					// getBases will sort everything out for us
					sb.append(strainAlig.getBases(pFillFromComposite,strainBase,nextStrainBase-1));
				}
			} else {
				// get base from substrain
				sb.append(alig.getBase(i));
			}
		}
		return sb.toString();
	}

	private class StrainIterator implements Iterator<Strain> {
		Iterator<StrainerResult> resultIterator = null;
		Iterator<Strain> strainIterator = null;
		
		StrainIterator() {
			resultIterator = results.values().iterator();
		}

		public boolean hasNext() {
			if (strainIterator!=null && strainIterator.hasNext()) {
				return true;
			} else {
				// finished last result, try next
				while (resultIterator.hasNext()) {
					strainIterator = resultIterator.next().getStrainIterator();
					if (strainIterator.hasNext()) {
						return true;
					}
				}
				//no more results with any strains in them
				return false;
			}
		}
		
		public Strain next() {
			if (hasNext()) {
				return strainIterator.next();
			} else {
				throw new NoSuchElementException();
			}
		}
		
		public void remove() {
			strainIterator.remove();
		}
	}
	
	public String toString() {
		StringBuffer ret = new StringBuffer("NestedStrainerResuls:");

		Iterator it = getStrainIterator();
		while(it.hasNext()) {
			ret.append("\n");
			Strain strain = (Strain) it.next();
			ret.append(strain.toString());
		}
		
		return ret.toString();
	}
}
