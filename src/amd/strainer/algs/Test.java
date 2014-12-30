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

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.Gene;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

public class Test {
	static boolean fillFromConsensus = false;
	static PrintStream out = System.out;
	
	static StrainerResult getStrainsForRefSeq (ReferenceSequence pRefSeq) throws SegmentStrainerException {
		//System.out.println("straining reference");

		HashMap<String,Object> settings = amd.strainer.algs.Config.getConfig().getSettings();
		/**/
		SequenceSegment wdt = new SequenceSegment(pRefSeq, 1,
				pRefSeq.getLength());
		settings.put(SegmentLinker.SEGMENT_METHOD ,SegmentLinker.SEGMENT_BY_READ_COUNT );
		settings.put(SegmentLinker.SEGMENT_SIZE ,new Integer((50)));
		settings.put(GeneCrawler.COMBINE_STRAINS,new Boolean(true));
		settings.put(GeneCrawler.MINIMUM_OVERLAP,new Integer(5));
		settings.put(GeneCrawler.COMPLETION_DIFF,new Double(0.005));
		SegmentStrainer ss = new SegmentLinker();
		ss.setSegment(wdt);
		/*
		SequenceSegment wdt = new SequenceSegment(pRefSeq,
				pRefSeq.getLength()/2,
		pRefSeq.getLength());
		settings.put(GeneCrawler.COMBINE_STRAINS,new Boolean(true));
		SegmentStrainer ss = new GeneCrawler(wdt);
		/**/
		
		System.out.println("running seg strainer");
		// get gene strains
		long start = 0;
		long end = 0;
		start = System.currentTimeMillis();
		StrainerResult sr = ss.getStrains();
		end = System.currentTimeMillis();
		double seconds = (end - start) / 1000.0;
		
		System.out.println("Found " +
				sr.size() + " strains for " +     
				pRefSeq.getName() + " in " + seconds + " seconds.");
		
		return sr;
	}
	
	static StrainerResult getStrainsForRefSeq (ReferenceSequence pRefSeq, int pstart, int pend) throws SegmentStrainerException {
		//System.out.println("straining RefSeq");

		HashMap<String,Object> settings = amd.strainer.algs.Config.getConfig().getSettings();
		/**/
		SequenceSegment wdt = new SequenceSegment(pRefSeq, pstart, pend);
		settings.put(SegmentLinker.SEGMENT_METHOD ,SegmentLinker.SEGMENT_BY_READ_COUNT );
		settings.put(SegmentLinker.SEGMENT_SIZE ,new Integer(30));
		settings.put(GeneCrawler.COMBINE_STRAINS,new Boolean(true));
		settings.put(GeneCrawler.MINIMUM_OVERLAP,new Integer(1));
		settings.put(GeneCrawler.COMPLETION_DIFF,new Double(0.0));
		SegmentStrainer ss = new SegmentLinker();
		ss.setSegment(wdt);

		/*
		SequenceSegment wdt = new SequenceSegment(pRefSeq,
				pRefSeq.getLength()/2,
		pRefSeq.getLength());
		settings.put(GeneCrawler.COMBINE_STRAINS,new Boolean(true));
		SegmentStrainer ss = new GeneCrawler(wdt);
		/**/
		
		System.out.println("running seg strainer");
		// get gene strains
		long start = 0;
		long end = 0;
		start = System.currentTimeMillis();
		StrainerResult sr = ss.getStrains();
		end = System.currentTimeMillis();
		double seconds = (end - start) / 1000.0;
		
		System.out.println("Found " +
				sr.size() + " strains for " +     
				pRefSeq.getName() + " in " + seconds + " seconds.");
		
		return sr;
	}

	/*
	private static HashSet buildStrainsFromStrainerResult(StrainerResult sr, HashMap reads) {
		HashSet strains = new HashSet();
		java.util.List sortedStrains =
			Strain.sortStrainsBySize(sr.getStrains());
		
		// reads can be in multiple strains, so we are useing the simple
		// rule here that the bigger strains win.  So rather than figure
		// out what the biggest strain each read is in, I'm just
		// doing them in reverse order and only the last (biggest) will be kept
		
		for (int i = sortedStrains.size()-1; i >= 0; i--) {
			Strain strain = (Strain) sortedStrains.get(i);
			Strain newStrain = new Strain();
			Iterator rit = strain.reads.values().iterator();
			while (rit.hasNext()) {
				Read algRead = (Read) rit.next();
				// this is convoluted because read objects in strainer results
				//  are not same objects used in gene strainer
				Read read =
					(Read) reads.get(new Integer(algRead.getId()));
				Strain oldStrain = read.getStrain();
				oldStrain.removeReadable(read);
				newStrain.putRead(new Integer(read.getId()),read);
				
				if (oldStrain.size==0) {
					strains.remove(oldStrain);
					oldStrain.close();
				}
				oldStrain.setAlignmentFromReads();
			}
			newStrain.setAlignmentFromReads();
			strains.add(newStrain);
		}
		
		return strains;
	}
	*/
	
	static void outputStrains(ReferenceSequence pRefSeq, Gene pGene, HashSet<Strain> pStrains) {
		int index = 1;
		for ( Strain strain : pStrains) {
			out.println(">" + pRefSeq.getName() + "_gene_" + pGene.getName() 
					+ "_" + index);
			out.println(strain.getBases());
			index++;
		}
	}
	
	static void outputStrains(StrainerResult pSR) {
		pSR.setStrainsSequences();
		Iterator sit = pSR.getStrainIterator();
		int index = 1;
		while (sit.hasNext()) {
			Strain strain = (Strain) sit.next();
			
			out.println(">StrainerOutput_" + index);
			out.println(strain.getBases());
			index++;
		}
	}
	
	public static void main(String [] argv) {
		try {
			FileOutputStream fos = null;
			
			// check arguments for output file name
			//  if we do nothing, output goes to sys.out
			if (argv.length > 0 ) {
				/*
                 String file = argv[0];
				fos = new FileOutputStream(file,false);
				out = new PrintStream(fos);
		         */
				
				ReferenceSequence e = null;
				// TODO:4 get reference from arguments
				StrainerResult sr = getStrainsForRefSeq(e);
				outputStrains(sr);
			} else {
				//StrainerResult sr = processOneRefSeqSegment(60,20886,27853);
			}			
			
			
			if (fos!=null) {
				fos.close();
				out = System.out;
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}

