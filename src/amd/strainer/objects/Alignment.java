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
 ***** END LICENSE BLOCK ***** */package amd.strainer.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the alignment between a sequence (read, clone, or strain) and a reference sequence.
 * <P>
 * Two sequence segments indicate where on reference sequence (sequence segment 1) the specific
 * sequence lies.  Sequence segment 2 is only relevant for Reads and indicates what part of the
 * read aligns here.  In all other cases, the alignment refers to the entire sequence.
 * 
 * @author jmeppley
 */
public class Alignment {
	
	protected SequenceSegment ss1 = null;
	/**
	 * @return The SequenceSegment object indicating what part of the Reference sequence is aligned
	 */
	public SequenceSegment getSequenceSegment1() { return ss1; }
	protected SequenceSegment ss2 = null;
	/**
	 * @return A SequenceSegment object representing which part of the query sequence is aligned.  If null,
	 * the entire sequence is aligned.
	 */
	public SequenceSegment getSequenceSegment2() { return ss2; }
	private boolean direction = true;
	
	private int [] gaps = null;
	private int [] refSeqGaps = null;
	private int [] gapsRef = null;
	private int [] refSeqGapsRef = null;
	/**
	 * The number of exact nucleotide matches between sequences.  -1 if not yet calculated.
	 */
	public int identities = -1;
	/**
	 * The BLAST alignment score (-1 if not known)
	 */
	public int score = -1;
	/**
	 * The BLAST eValue (-1 if not known)
	 */
	public double eValue = -1.0;

	// This is an array list so the order won't change
	private List<Difference> diffs = null;
	protected List<SequenceSegment> unknownRegions = null;
	/**
	 * @return A java.util.List of SequenceSegments indicating any large gaps in the alignment. This is empty for reads
	 * and usually contains one entry for Clone objects.  StrainAlignments may contain any number from 0 up to one less than the
	 * number of reads.
	 */
	public List<SequenceSegment> getUnknownRegions() {
		if (unknownRegions==null)  {
			unknownRegions = new ArrayList<SequenceSegment>();
		}
		
		return unknownRegions;
	}
	/**
	 * @param pPos the position (relative to the Reference)
	 * @return True if the alignment does not cover the given position.
	 */
	public boolean isUncovered(int pPos) {
		// in most cases, if it's in the alignment, it's covered
		//  other cases should be handled by overwriting this method
		return (pPos<=getStart() || pPos>=getEnd());
	}
	
	/**
	 * @param pS1 The portion of the reference sequence aligned
	 * @param pS2 The portion of the query sequence aligned (may be null)
	 * @param pDir false if sequences are from opposite strands
	 * @param pDiffs java.util.List of amd.strainer.objects.Difference objects indicating where
	 *  aligned sequences disagree
	 */
	public Alignment(SequenceSegment pS1, SequenceSegment pS2, boolean pDir, List<Difference> pDiffs) {
		setSegments(pS1,pS2);
		direction = pDir;
		setDiffs(pDiffs);
	}
	
	/**
	 * Direction is assumed to be true, and no Differences
	 * @param pS1 The portion of the reference sequence aligned
	 * @param pS2 The portion of the query sequence aligned (may be null)
	 */
	private void setSegments(SequenceSegment pSS1, SequenceSegment pSS2) {
		ss1 = pSS1;
		ss2 = pSS2;
	}
	
	/**
	 * @param pQuery Any other alignments using the same reference sequence
	 * @return true if aligned regions on reference overlap
	 */
	public boolean intersects(Alignment pQuery) {
		return ss1.intersects(pQuery.getSequenceSegment1());
	}
	
	/**
	 * @return true if aligned sequence is on the forward strand
	 */
	public boolean isForward() {return direction;}
	
	/**
	 * @return The first position aligned on the reference sequence
	 */
	public int getStart() {
		return ss1.getStart();
	}
	
	/**
	 * @return The last position matched on the reference sequence
	 */
	public int getEnd() {
		return ss1.getEnd();
	}
	
	public Object clone() {
		SequenceSegment css1 = (SequenceSegment) ss1.clone();
		SequenceSegment css2 = null;
		if (ss2 != null) {
			css2 = (SequenceSegment) ss2.clone();
		}

		List<Difference> cDiffs = null;
		if (diffs != null) {
			cDiffs = new ArrayList<Difference>();
			cDiffs.addAll(diffs);
		}
		
		Alignment c = new Alignment(css1,css2,direction,cDiffs);
		return c;
	}
	
	private int id = -1;
	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * calculates the length of this alignment
	 * @return The length of the aligned sequence
	 */
	public int getLength() {
		// the length is the same as the last position relative to this alignment
		return getPosFromReference(getEnd());
	}
	
	/**
	 * @param pPos position in this sequence (1-length)
	 * @return position in reference sequence (start-end)
	 */
	public int getReferencePos(int pPos) {		
		if (gaps==null) { processDiffs(getDiffs()); }
		int shift = gapsBefore(pPos,gaps) - gapsBefore(pPos,refSeqGaps);
		return pPos + getStart() - ss2.getStart() + shift;
	}
	
	/**
	 * get the list of places that this sequence inserts a base relative to the reference
	 * @return an array of integer positions
	 */
	public int [] getReferenceGaps() {
		if (refSeqGaps==null) { processDiffs(getDiffs()); }
		return refSeqGapsRef;
	}

	/**
	 * @param pPos A position relative to the reference sequence (in the range start to end)
	 * @return The corresponding position relative to the aligned sequence.
	 */
	public int getPosFromReference(int pPos) {
		if (gaps==null) { processDiffs(getDiffs()); }
		int shift = gapsBefore(pPos,gapsRef) - gapsBefore(pPos,refSeqGapsRef);
		return pPos - getStart() + ss2.getStart() - shift;	
	}
	
	/**
	 * @return A string of nucleotides representing this aligned sequence
	 */
	public String getBases(boolean pFillFromConsensus) {
		return getBases(pFillFromConsensus,1,this.getLength());
	}

	/**
	 * Returns the specified subsequence.  
	 * @param pFillFromConsensus Fill unknown from reference or with 'n's
	 * @param pStart first base (1-length)
	 * @param pEnd last base (1-length)
	 * @return A string of nucleotides representing a part of this aligned sequence
	 */	
	public String getBases(boolean pFillFromConsensus,int pStart, int pEnd) {
		// this calls getBase() at each position.  This is not optimal for performance, but
		//  getting sequences shouldn't ever impinge on the GUI, and this is much
		//  easier to debug than a completely separate method.
		
		StringBuffer sb = new StringBuffer((pEnd-pStart)+1);
		Sequence reference = ss1.getSequence();
		for (int i = pStart; i <= pEnd; i++) {
			int rPos = this.getReferencePos(i);
			if (this.isUncovered(rPos)) {
				if (pFillFromConsensus) {
					sb.append(reference.getBase(rPos));
				} else {
					sb.append("n");
				}
			} else {
				sb.append(this.getBase(i));
			}
		}
		return sb.toString();
	}
		
	/**
	 * Returns the specified subsequence inserting gaps if necessary where indicated. This
	 * is useful for generating MSAs 
	 * @param pFillFromConsensus Fill unknown from reference or with 'n's
	 * @param pGaps locations to insert gaps
	 * @param pStart first base (in range 1-length)
	 * @param pEnd last base (in range 1-length)
	 * @return A string of nucleotides representing a part of this aligned sequence
	 */	
	public String getMSABases(boolean pFillFromConsensus,int [] pGaps,int pStart, int pEnd) {
		// this calls getBase() at each position.  This is not optimal for performance, but
		//  getting sequences shouldn't ever impinge on the GUI, and this is much
		//  easier to debug than a completely separate method.
		
		StringBuffer sb = new StringBuffer((pEnd-pStart)+1);
		Sequence reference = ss1.getSequence();
		for (int i = pStart; i <= pEnd; i++) {
			int rPos = this.getReferencePos(i);
			char base;
			if (this.isUncovered(rPos)) {
				if (pFillFromConsensus) {
					base=reference.getBase(rPos);
				} else {
					base='X';
				}
			} else {
				base = this.getBase(i);
			}
			sb.append(base);
			
			//check to see if we should insert gap because someone else gaps the reference
			if (Arrays.binarySearch(pGaps,(rPos+1))>=0) {
				if (Arrays.binarySearch(refSeqGapsRef,rPos+1)<0) {
					sb.append('-');
				}	
			}
			
			// see if this read has a gap
			if (getReferencePos(i+1)>rPos+1) {
				sb.append('-');
			}
		}
		return sb.toString();
	}
		
	/**
	 * returns base at specified relative position (element of [1,getLength()])
	 */
	public char getBase(int pPos) {
		// is there a diff at this position
		Difference d = getDiffAtPosition(pPos);

		int rPos = this.getReferencePos(pPos);

		// skip if reference sequence has an extra base here
		if (d!=null) {
			if (d.getBase2()=='-') { 
				return getSequenceSegment1().getSequence().getBase(rPos);
			} else {
//				System.out.println(d.getBase2());
				return d.getBase2();
			}
		}
		
//		System.out.println(getSequenceSegment1().getSequence().getBase(rPos));
		
		// if no diff:
		return getSequenceSegment1().getSequence().getBase(rPos);
	}

	/**
	 * returns base at specified relative position (element of [getStart(),getEnd()])
	 */
	public char getBaseFromReference(int pPos) {
		// is there a diff at this position
		Difference d = getDiffAtReferencePosition(pPos);

		// skip if refernce seqence has an extra base here
		if (d!=null) {
			return d.getBase2();
		}
		
		// if no diff:
		return getSequenceSegment1().getSequence().getBase(pPos);
	}

	/**
	 * Count how many diffs are between the given positions
	 * @param pStartPos  the first position to check (relative to the reference sequence)
	 * @param pEndPos the last position to check (relative to the reference sequence)
	 * @return the number of diffs
	 */
	public int countDiffsInRange(int pStartPos, int pEndPos) {
		// use halving algorithm to find first diff in range, then just count up

		// make sure diff array is current
		List<Difference> diffs = getDiffs();
		if (diffs.size()==0) {
			// if there are no diffs, there are no diffs in the range
			return 0;
		}
		
		int diffCount = 0;
		
		// start in middle
		int lastHigh = diffs.size();
		int lastLow = -1;
		int diffIndex;
		Difference d;
		
		// as long as high and low are at least 2 apart, keep halving
		while (lastLow + 1 < lastHigh) {
			diffIndex = (lastHigh+lastLow)/2;
			d = diffs.get(diffIndex);
			// check where we are
			if (d.getPosition1()<pStartPos) {
				// go up
				lastLow = diffIndex;
			} else if ( d.getPosition1()>pEndPos) {
				// go down
				lastHigh = diffIndex;
			} else {
				// diff is in range
				// increment counter if quality is good
				if (d.getBase2()!='n') {
					diffCount++;
				}

				// count diffs down till we leave range
				int i = 1;
				while (diffIndex-i>0 && 
						diffs.get(diffIndex-i).getPosition1()>=pStartPos) {
					if (d.getBase2()!='n') {
						diffCount++;
					}
					i++;
				}

				// count next diffs up til we leave the range
				i = 1;
				while (diffIndex+i<diffs.size() && 
						diffs.get(diffIndex+i).getPosition1()<=pEndPos) {
					if (d.getBase2()!='n') {
						diffCount++;
					}
					i++;
				}
				
				// done
				return diffCount;
			}
		}
		
		// done (if we get here, the answer should be 0)
		return diffCount;		
	}
	
	private Difference getDiffAtPosition(int pPos) {
		// use halving algorithm to find diff at pos

		// make sure diff array is current
		List<Difference> diffs = getDiffs();
		if (diffs.size()==0) {
			// if there are no diffs, there are no diffs in the range
			return null;
		}
		
		// start in middle
		int lastHigh = diffs.size();
		int lastLow = -1;
		int diffIndex;
		Difference d;
		
		// as long as high and low are at least 2 apart, keep halving
		while (lastLow + 1 < lastHigh) {
			diffIndex = (lastHigh+lastLow)/2;
			d = diffs.get(diffIndex);
			// check where we are
			if (d.getPosition2()<pPos) {
				// go up
				lastLow = diffIndex;
			} else if ( d.getPosition2()>pPos) {
				// go down
				lastHigh = diffIndex;
			} else {
				// diff is in pPos
				return d;
			}
		}
		
		// done (if we get here, there is no diff at pPos)
		return null;	

		/*
		for (int i = 0; i<getDiffs().size(); i++) {
			Difference d = (Difference) getDiffs().get(i);
			if (d.getPosition2()==pPos) {
				if (d.getBase1()!='-') {
					return d;
				}
			} else {
				if (d.getPosition2()>pPos) {
					break;
				}
			}
		}
		return null;
		*/
	}
	
	/**
	 * return the difference object at the given position relative to the reference sequence
	 * 
	 * @param pPos the position on the reference sequence
	 * @return a Difference object, null if same as reference at the given position
	 */
	public Difference getDiffAtReferencePosition(int pPos) {
		// use halving algorithm to find diff at pos

		// make sure diff array is current
		List<Difference> diffs = getDiffs();
		if (diffs.size()==0) {
			// if there are no diffs, there are no diffs in the range
			return null;
		}
		
		// start in middle
		int lastHigh = diffs.size();
		int lastLow = -1;
		int diffIndex;
		Difference d;
		
		// as long as high and low are at least 2 apart, keep halving
		while (lastLow + 1 < lastHigh) {
			diffIndex = (lastHigh+lastLow)/2;
			d = diffs.get(diffIndex);
			// check where we are
			if (d.getPosition1()<pPos) {
				// go up
				lastLow = diffIndex;
			} else if ( d.getPosition1()>pPos) {
				// go down
				lastHigh = diffIndex;
			} else {
				// diff is in pPos
				return d;
			}
		}
		
		// done (if we get here, there is no diff at pPos)
		return null;	
		
		/*
		for (int i = 0; i<getDiffs().size(); i++) {
			Difference d = (Difference) getDiffs().get(i);
			if (d.getPosition1()==pPos) {
				if (d.getBase1()!='-') {
					return d;
				}
			} else {
				if (d.getPosition1()>pPos) {
					break;
				}
			}
		}
		return null;
		*/
	}
	
	private int gapsBefore(int pPos, int [] pGaps) {
		int count = 0;
		for (int i = 0; (i<pGaps.length) && (pGaps[i]<=pPos); i++) {
			count++;
		}
		return count;
	}
	
	/**
	 * @return Returns the list of differences.
	 */
	public List<Difference> getDiffs() {
		return diffs;
	}

	/**
	 * @param diffs The diffs to set.
	 */
	public void setDiffs(List<Difference> diffs) {
		if (diffs != null) {
			processDiffs(diffs);
		} else {
			unknownRegions=null;
		}
		this.diffs = diffs;
	}
	
	/*
	 * generates map of diffs where index is the position on this sequence (1-length).
	 * Diffs where this sequence gaps the reference are skipped.
	 * <P>
	 * also genereates gap lists.  refSeqGaps lists the position where the reference sequence is
	 * gapped, and gaps lists the positions where this sequence is gapped.  
	 */
	private void processDiffs(List<Difference> pDiffs) {
		ArrayList<Integer> refSeqGapList = new ArrayList<Integer>();
		ArrayList<Integer> gapList = new ArrayList<Integer>();
		ArrayList<Integer> refSeqGapListRef = new ArrayList<Integer>();
		ArrayList<Integer> gapListRef = new ArrayList<Integer>();

 		for (Difference d : pDiffs) {
			// don't add to map if it's a gap
			if (d.getBase2()=='-') {
				gapList.add(new Integer(d.getPosition2()));
				gapListRef.add(new Integer(d.getPosition1()));
			} else {
				if (d.getBase1()=='-') {
					refSeqGapList.add(new Integer(d.getPosition2()));
					refSeqGapListRef.add(new Integer(d.getPosition1()));
				}
			}
		}

		// set arrays from ArrayLists to save memory
		this.gaps = new int [gapList.size()];
		for (int i = 0; i<gapList.size(); i++) {
			this.gaps[i] = gapList.get(i);
		}
		this.refSeqGaps = new int [refSeqGapList.size()];
		for (int i = 0; i<refSeqGapList.size(); i++) {
			this.refSeqGaps[i] = refSeqGapList.get(i);
		}
		this.gapsRef = new int [gapListRef.size()];
		for (int i = 0; i<gapListRef.size(); i++) {
			this.gapsRef[i] = gapListRef.get(i);
		}
		this.refSeqGapsRef = new int [refSeqGapListRef.size()];
		for (int i = 0; i<refSeqGapListRef.size(); i++) {
			this.refSeqGapsRef[i] = refSeqGapListRef.get(i);
		}
		
		// make sure ss2 numbers are correct (TODO:6 make sure this is really necessary)
		if (ss2!=null) {
			if (ss2.getStart()<1 || !(ss2.getSequence() instanceof Read)) {
				ss2.setStart(1);
			}
			int length = ss1.getEnd()-ss1.getStart()+1-gapList.size()+refSeqGapList.size();
			ss2.setEnd(ss2.getStart()+length-1);
		}
	}

//	protected int uncoveredLength = 0;
	
	private double identity = -1.0;
	public double getIdentity()  {
		if (identity<0) {
			int length = getEnd() - getStart();
			
			// get identity from the number of diffs
			boolean diffsWasNull = (this.diffs==null); 
			identity = ( (float) ((length-getUncoveredLength())-getDiffs().size())) / ((float) (length-getUncoveredLength()));
			// clear diff array if it wasn't already loaded
			if (diffsWasNull) {
				this.diffs=null;
			}
		}
		
		return identity;
	}

	private int getUncoveredLength() {
		List<SequenceSegment> holes = getUnknownRegions();
		int length = 0;
		for (SequenceSegment s : holes) {
			length += s.getEnd() - s.getStart();
		}
		return length;
	}
	
	public String toString() {
		return "Alig:" + getStart() + "-" + getEnd();
	}
}
