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

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import amd.strainer.display.ReferenceSequenceDisplayComponent;
import amd.strainer.display.actions.Task;
import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Alignment;
import amd.strainer.objects.Difference;
import amd.strainer.objects.Gene;
import amd.strainer.objects.Read;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

public class Util {

	public static final int ALL_GENES = 0;
	public static final int SELECTED_GENE = 1;

	/**
	 * Compares two aligned sequences within a defined range of bases. 
	 * 
	 * @param p1 First AlignedSequence
	 * @param p2 Second AlignedSequence
	 * @param pStart the first base to consider
	 * @param pEnd the last base to consider
	 * @param pMaximumDiff the highest difference/base ratio allowed
	 * @return true if measuered difference per base ratio is less than or equal to pMaximumDiff
	 */
	public static boolean compareAlignedSequences(AlignedSequence p1, 
			AlignedSequence p2,
			int pStart, int pEnd, double pMaximumDiff) {
		double diff = getDiffBetweenAlignedSequences(p1,p2,pStart,pEnd);
		return diff <= pMaximumDiff;
	}

	/**
	 * Compares two aligned sequences within a defined range of bases. 
	 * 
	 * @param p1 First AlignedSequence
	 * @param p2 Second AlignedSequence
	 * @param pStart the first base to consider
	 * @param pEnd the last base to consider
	 * @return calculated difference per base ratio
	 */
	public static double getDiffBetweenAlignedSequences(AlignedSequence p1, 
			AlignedSequence p2,
			int pStart, int pEnd) {
		
		int mismatchCount = countDiffsBetweenSequences(p1,p2,pStart,pEnd);
		
		int length = pEnd - pStart + 1;
		double diff = (double) mismatchCount / (double) length;
		return diff;
	}

	/**
	 * Calculate amino acid identity of two sequences
	 * 
	 * @param p1 first sequence
	 * @param p2 second sequence
	 * @param pGene Gene object which defines region to be considered
	 * @return the fraction of amino acids that are not conserved between seqences
	 */
	public static double calculateAminoAcidIdentity(AlignedSequence p1, AlignedSequence p2, Gene pGene) {
		String nSeq1 = p1.getAlignment().getBases(true,
				p1.getAlignment().getPosFromReference(pGene.getStart()),
				p1.getAlignment().getPosFromReference(pGene.getEnd()));
		String nSeq2 = p2.getAlignment().getBases(true,
				p2.getAlignment().getPosFromReference(pGene.getStart()),
				p2.getAlignment().getPosFromReference(pGene.getEnd()));
		String aaSeq1 = null,aaSeq2 = null;
		try {
			aaSeq1 = Util.getProteinSequence(nSeq1,true);
			aaSeq2 = Util.getProteinSequence(nSeq2,true);
		} catch (BioException ex) {
			System.err.print("Could not translate sequence for ");
			if (aaSeq1==null) {
				System.err.print(p1.getName());
			} else {
				System.err.print(p2.getName());
			}
			System.err.print(": ");
			System.err.println(ex.toString());
			return -1;
		}
		int length = aaSeq1.length();
		int diffs = 0;
		if (length>aaSeq2.length()) {
			length = aaSeq2.length();
			diffs = aaSeq1.length() - length;
			System.err.println("Amino acid seq lengths different, adding difference to diff count");
			System.out.println("n1: " +nSeq1.length() + " :: n2: " + nSeq2.length());
			System.out.println("a1: " +aaSeq1.length() + " :: a2: " + aaSeq2.length());
		} else if (length<aaSeq2.length()){
			diffs = aaSeq2.length() - length;
			System.err.println("Amino acid seq lengths different, adding difference to diff count");
			System.out.println("n1: " +nSeq1.length() + " :: n2: " + nSeq2.length());
			System.out.println("a1: " +aaSeq1.length() + " :: a2: " + aaSeq2.length());
		}
//		System.out.println(aaSeq1);
//		System.out.println(aaSeq2);
		for (int i=0;i<length;i++) {
			if (aaSeq1.charAt(i)!=aaSeq2.charAt(i)) {
				diffs++;
			}
		}
//		System.out.println(diffs);
		double pctAAId = 100*(length - diffs)/length;
		return pctAAId;
	}
	
	/**
	 * Finds maximal window that is covered by sequence spans.
	 * @param p1 first sequence
	 * @param p2 second sequence
	 * @param pStart first base considered
	 * @param pEnd last base considered
	 * @return the number fo bases in range that are covered by both sequences
	 */
	public static int calculateOverlapLength (AlignedSequence p1, AlignedSequence p2, int pStart, int pEnd) {
		// find maximal window that is covered by sequence spans
		int start = Math.max(p1.getStart(),Math.max(p2.getStart(),pStart));
		int end = Math.min(p1.getEnd(),Math.min(p2.getEnd(),pEnd));

		// check for holes in spans
		if (p1.getAlignment().getUnknownRegions().size()==0) {
			if (p2.getAlignment().getUnknownRegions().size()==0) {
				// no holes anywhere, overlap is simple
				return end - start + 1; 
			} else {
				// only holes in p2
				return calculateCoveredLength(start,end,p2.getAlignment().getUnknownRegions());
			}
		} else {
			if (p2.getAlignment().getUnknownRegions().size()==0) {
				// only holes in p1
				return calculateCoveredLength(start,end,p1.getAlignment().getUnknownRegions());
			} else {
				List<SequenceSegment> combinedHoles = combineUncoveredRegions(p1.getAlignment().getUnknownRegions(),p2.getAlignment().getUnknownRegions());
				return calculateCoveredLength(start,end,combinedHoles);
			}
		}
	}
	
	/**
	 * Merges two sets of uncovered regions
	 * 
	 * @param p1 first list of uncovered regions
	 * @param p2 second list of uncovered regions
	 * @return merged list such that entries are in order of start, don't overlap, and any point 
	 *  indicated in p1 or p1 is indicated in here
	 */
	public static List<SequenceSegment> combineUncoveredRegions(List<SequenceSegment> p1, List<SequenceSegment> p2) {
		ArrayList<SequenceSegment> newHoles = new ArrayList<SequenceSegment>();
		
		int i = 0;
		int j = 0;
		SequenceSegment next1,next2;
		while (true) {
			if (i<p1.size()) {
				next1 = p1.get(i);
			} else {
				next1 = null;
			}
			if (j<p2.size()) {
				next2 = p2.get(j);
			} else {
				next2 = null;
			}
			
			if (next1==null && next2==null) {
				break;
			} else if (next2==null) {
				newHoles.add(next1);
				i++;
			} else if (next1==null || next1.getStart()>next2.getEnd()+1) {
				newHoles.add(next2);
				j++;
			} else if (next1.getEnd()<next2.getStart()-1) {
				newHoles.add(next1);
				i++;
			} else  {
				SequenceSegment newHole = new SequenceSegment(next1.getSequence(),
						Math.min(next1.getStart(),next2.getStart()),
						Math.max(next1.getEnd(),next2.getEnd()));
				newHoles.add(newHole);
				i++;
				j++;
			}
		}
		
		return newHoles;
	}
	
	/**
	 * Calculates the total length of bases with coverage within the specified bases. 
	 * 
	 * @param pStart first base considered
	 * @param pEnd last base considered
	 * @param pUncovered List of SequenceSegments listing regions that dont't have sequence coverage. Expected to be in order and non-overlapping.
	 * @return the total length of uncovered regions in specified base range
	 */
	public static int calculateCoveredLength(int pStart, int pEnd, List pUncovered) {
		// this will get the total number of uncovered bases in range
		int subtraction = 0;

		// loop over holes
		for (int i = 0; i < pUncovered.size(); i++) {
			SequenceSegment ss = (SequenceSegment) pUncovered.get(i);

			// entire hole before start
			if (ss.getEnd()<pStart) {
				// hole not in range, skip
				continue;
			}

			// entire hole after end
			if (ss.getStart()>pEnd) {
				// hole not in range, skip rest (they are in order)
				break;
			}

			
			// at least partially in region
			if (ss.getStart()>=pStart) {
				if (ss.getEnd()<=pEnd) {
					// hole compleely in range, subtract length
					subtraction += ss.getEnd() - ss.getStart() + 1;
				} else {
					// hole overlaps end, subtract overlap
					subtraction += pEnd - ss.getStart() + 1;
					// rest will be past end, quit
					break;
				}
			} else {
				if (ss.getEnd()<pEnd) {
					// hole overlaps beginning
					subtraction += ss.getEnd()-pStart+1;
				} else {
					// hole covers whole thing
					return 0;
				}
			}
		}
		
		// return the length minus the total subtracted bases
		return pEnd - pStart + 1 - subtraction;
	}		

	/**
	 * Counts the number of differences between two sequences in the defined range. If either
	 * sequence has an 'n' or 'N' in a position, no difference will be counted at that position.
	 * 
	 * @param p1 Sequence 1
	 * @param p2 Sequence 2
	 * @param pStart first base considered
	 * @param pEnd last base considered
	 * @return the number of bases that did not agree
	 */
	public static int countDiffsBetweenSequences(AlignedSequence p1, AlignedSequence p2, 
			int pStart, int pEnd) 
	{
		// call countDiffs such that n's are ignored
		return countDiffsBetweenSequences(p1,p2,pStart,pEnd,true);
	}
	
	/**
	 * Counts the number of differences between two sequences in the defined range. If pIgnoreNs is set to true, 
	 * then if either
	 * sequence has an 'n' or 'N' in a position, no difference will be counted at that position.
	 * 
	 * @param p1 Sequence 1
	 * @param p2 Sequence 2
	 * @param pStart first base considered
	 * @param pEnd last base considered
	 * @param pIgnoreNs don't count's diffs where either sequence has an n (if true)
	 * @return the number of bases that did not agree
	 */
	public static int countDiffsBetweenSequences(AlignedSequence p1, AlignedSequence p2, 
			int pStart, int pEnd, boolean pIgnoreNs) 
	{
//		System.out.println("Counting diffs from " + pStart + " to " + pEnd + " (" + (pEnd-pStart+1) + " bases)");
		
		List u1 = p1.getAlignment().getUnknownRegions();
		List u2 = p2.getAlignment().getUnknownRegions();

		int start = Math.max(pStart,Math.max(p1.getStart(),p2.getStart()));
		int end = Math.min(pEnd,Math.min(p1.getEnd(),p2.getEnd()));
		
		if (start>end) {
			return 0;
		}
		
		// diff walkers should return (in order) the diffs in the specified range that
		//  do not fall in the list of excluded segments (other sequence's uncovered bits)
		DiffWalker dWalker1 = new DiffWalker (p1.getAlignment(),start,end,u2);
		DiffWalker dWalker2 = new DiffWalker (p2.getAlignment(),start,end,u1);

		int mismatchCount = 0;
		while (dWalker1.hasNext() || dWalker2.hasNext()) {
			if (dWalker1.nextPos() < dWalker2.nextPos()) {
				if(!pIgnoreNs || (dWalker1.next().getBase2()!='n' && dWalker1.next().getBase2()!='N')) {
					// only count the diff if it is not an 'n' or 'N'
					mismatchCount++;
				}
				dWalker1.increment();
			} else if (dWalker2.nextPos() < dWalker1.nextPos()) {
				if(!pIgnoreNs||(dWalker2.next().getBase2()!='n' && dWalker2.next().getBase2()!='N')) {
					// only count the diff if it is not an 'n' or 'N'
					mismatchCount++;
				}
				dWalker2.increment();
			} else {
				// diffs at same pos.
				// check for gaps
				if (dWalker1.next().getBase1()=='-') {
					if (dWalker2.next().getBase1()=='-') {
						// both gapped by referece seq, treat normally
						if (dWalker1.next().getBase2()!=dWalker2.next().getBase2()) {
							// only count the diff if neither is an 'n' or 'N'
							if(pIgnoreNs||(dWalker1.next().getBase2()!='n' && dWalker1.next().getBase2()!='N')) {
								if(pIgnoreNs||(dWalker2.next().getBase2()!='n' && dWalker2.next().getBase2()!='N')) {
									mismatchCount++;
								}
							}
						}
						dWalker1.increment();
						dWalker2.increment();
					} else {
						// only count the diff if it is not an 'n' or 'N'
						if(pIgnoreNs||(dWalker1.next().getBase2()!='n' && dWalker1.next().getBase2()!='N')) {
							mismatchCount++;
						}
						dWalker1.increment();
					}
				} else {
					if (dWalker2.next().getBase1()=='-') {
						// only count the diff if it is not an 'n' or 'N'
						if(pIgnoreNs||(dWalker2.next().getBase2()!='n' && dWalker2.next().getBase2()!='N')) {
							mismatchCount++;
						}
						dWalker2.increment();
					} else {
						// neither gapped by reference seq, treat normally
						if (dWalker1.next().getBase2()!=dWalker2.next().getBase2()) {
							// only count the diff if neither is not an 'n' or 'N'
							if(pIgnoreNs||(dWalker1.next().getBase2()!='n' && dWalker1.next().getBase2()!='N')) {
								if(pIgnoreNs||(dWalker2.next().getBase2()!='n' && dWalker2.next().getBase2()!='N')) {
									mismatchCount++;
								}
							}
						}
						dWalker1.increment();
						dWalker2.increment();
					}
				}
			}
		}
		
		return mismatchCount;
	}

	/**
	 * Uses BioJava tools to convert string to amino acid sequence
	 * @param pBases a string of nucleotides
	 * @param pDirection false if string is reverse complement
	 * @return AminoAcid sequence
	 * @throws IllegalAlphabetException 
	 * this will occur if you try and transcribe a non DNA sequence or translate
	 * a sequence that isn't a triplet view on a RNA sequence.
	 * @throws IllegalSymbolException
	 * this will happen if non IUB characters are used to create the DNA SymbolList
	 */
	public static String getProteinSequence(String pBases, boolean pDirection) throws IllegalAlphabetException,IllegalSymbolException {
		pBases = Util.trimTo3(pBases);
		
			//create a DNA SymbolList
			SymbolList symL = DNATools.createDNA(pBases);
	
			//System.out.println("raw: " + symL.seqString());
			
			if (!pDirection) {
				symL = DNATools.reverseComplement(symL);
				//System.out.println("compl:" + symL.seqString());
			}
	
			//transcribe to RNA
			symL = DNATools.toRNA(symL);
			
			//translate to protein
			symL = RNATools.translate(symL);
			
			//System.out.println("AA: " + symL.seqString());
	
			// save to set (should guarantee uniqueness)
			return symL.seqString();

			/* the following are now thrown
     	} catch (IllegalAlphabetException ex) {
		 
//			 * this will occur if you try and transcribe
//			 * a non DNA sequence or translate
//			 * a sequence that isn't a triplet view on a RNA sequence.
			 
			ex.printStackTrace();
		} catch (IllegalSymbolException ex) {
			// this will happen if non IUB characters
			//  are used to create the DNA SymbolList
			ex.printStackTrace();
		}
		*/
	}
	
	/**
	 * Runs a series of tests on the methods in this class
	 * @param args (ignored)
	 */
	public static void main(String [] args) {
		// Create 2 reads one from 2 to 22 and the other from 12 to 32
		Read r1 = new Read();
		List<Difference> diffs = new ArrayList<Difference>();
		Difference d = new Difference(4,'c',3,'t');
		diffs.add(d);
		d = new Difference(13,'c',12,'t');
		diffs.add(d);
		d = new Difference(18,'c',17,'t');
		diffs.add(d);
		ReferenceSequence r = new ReferenceSequence();
		Alignment a1 = new Alignment(new SequenceSegment(r,2,22),
				new SequenceSegment(r1,1,21),true,diffs);
		r1.setAlignment(a1);
		Read r2 = new Read();
		diffs = new ArrayList<Difference>();
		d = new Difference(13,'c',2,'t');
		diffs.add(d);
		d = new Difference(18,'c',7,'a');
		diffs.add(d);
		d = new Difference(20,'c',9,'t');
		diffs.add(d);
		d = new Difference(25,'c',14,'t');
		diffs.add(d);
		Alignment a2 = new Alignment(new SequenceSegment(r,12,32),
				new SequenceSegment(r2,1,21),true,diffs);
		r2.setAlignment(a2);
	
		// count diffs
		System.out.println("Seqeunces:");
		System.out.println("00000000011111111112222222222333");
		System.out.println("12345678901234567890123456789012");
		System.out.println(" --t--------t----t----");
		System.out.println("           -t----a-t----t-------");
		int ndiffs = countDiffsBetweenSequences(r1,r2,12,22);
		System.out.println("There are " + ndiffs + " diffs between 12 and 22");
		ndiffs = countDiffsBetweenSequences(r1,r2,1,42);
		System.out.println("There are " + ndiffs + " diffs between 1 and 42");
		ndiffs = countDiffsBetweenSequences(r1,r2,19,42);
		System.out.println("There are " + ndiffs + " diffs between 19 and 42");
		System.out.println("Is the difference better than 0.001? " + compareAlignedSequences(r1,r2,1,40,0.001));
		System.out.println("Is the difference better than 0.5? " + compareAlignedSequences(r1,r2,1,40,0.5));

		//TODO:2 test remaining methods
	}
	
	/**
	 * Put default values into the global settings hash based on the configuration info in optionsHash
	 * @param pOptions HashMap of option names and default values or lists
	 * @see SegmentStrainer#getOptionsHash()
	 */
	public static void setDefaultsFromOptionsHash(Map<String,Object> pOptions) {
		HashMap<String,Object> settings = Config.getConfig().getSettings();
		// loop over entries in pOptions
		for (Map.Entry<String,Object> pair : pOptions.entrySet()) {
			Object value = pair.getValue();
			if (value instanceof List) {
				value = ((List)value).get(0);
			}
			settings.put(pair.getKey(),value);
		}
	}

	static String trimTo3 (String pBases) {
		int length = pBases.length();
		int adj = length % 3;
		if (adj==0) {
			return pBases;
		}
		return pBases.substring(0,length-adj);
	}

	public static void writeStrainerResultsToFastaFile(PrintWriter pFastaFileWriter, Gene pGene, StrainerResult pSR, String pGenePrefix) {
		pSR.setStrainsSequences();
		Iterator<Strain> sit = pSR.getStrainIterator();
		
		int index = 1;
		while (sit.hasNext()) {
			Strain strain = sit.next();
			pFastaFileWriter.println(DefaultStrainerResult.getFastaHeader(strain,pGene,index,pGenePrefix));
			pFastaFileWriter.println(strain.getBases());
			index++;
		}
	}

	/**
	 * @param pStrains1 a Set of Strain objects
	 * @param pStrains2 another Set of Strain objects
	 * @return true if the same sequences are returned
	 */
	public static boolean compareStrainGroups(StrainerResult pStrains1, StrainerResult pStrains2) {
		if (pStrains1.size() != pStrains2.size()) {
			return false;
		}

		// collect sequences from one set
		Set<String> sequences1 = new HashSet<String>();
		pStrains1.setStrainsSequences();
		for (Strain strain : pStrains1.getStrains()) {
			sequences1.add(strain.getBases());
		}
		
		// collect sequences from other set
		Set<String> sequences2 = new HashSet<String>();
		pStrains2.setStrainsSequences();
		for (Strain strain : pStrains2.getStrains()) {
			sequences2.add(strain.getBases());
		}

		// remove every sequence in 1 from 2
		for (String seq : sequences1) {
			if(!sequences2.remove(seq)) {
				// if it wasn't there, results are different
				return false;
			}
		}

		// they were the same if sequences2 is empty
		return sequences2.size()>0;
	}

	public static void autostrainGene(Class pAlgClass, Gene pGene, Task pTask, Iterator<Read> pReads, PrintWriter pFastaFileWriter, ReferenceSequenceDisplayComponent pCanvas, String pGenePrefix) throws SegmentStrainerException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (pGenePrefix==null) { pGenePrefix = ""; }
		
		// create instance of selected segment strainer
		SegmentStrainer ss = Config.getSegmentStrainer(pAlgClass);
		
		ss.setSegment(pGene);
		ss.setTask(pTask);
		ss.setReads(pReads);
		
//		try {
			StrainerResult sr = ss.getStrains();
			if (pFastaFileWriter!=null) {
				Util.writeStrainerResultsToFastaFile(pFastaFileWriter,pGene,sr, pGenePrefix);
			}
			if (pCanvas!=null) {
				// (false=>don't save backup info, let caller do it in case they are doing multiple genes)
				pCanvas.updateStrainsFromStrainerResults(sr, false);
			}
			
			System.out.println(sr.toString());
			
//		} catch (Exception e) {
//			e.printStackTrace();
//			amd.strainer.display.util.Util.displayErrorMessage(PaneledReferenceSequenceDisplay.frame,"Can't strain gene " + pGene.getName() + ": " + e.toString());
//		}	
	}
}
