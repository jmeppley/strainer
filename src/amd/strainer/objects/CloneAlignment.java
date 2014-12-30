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
import java.util.List;

/**
 * A description of how a pair of end Reads (a Clone) aligns to the ReferenceSequence
 * 
 * @author jmeppley
 *
 */
public class CloneAlignment extends Alignment {
	
	private Alignment a1 = null;
	private Alignment a2 = null;
	
	/**
	 * Creates a new CloneAlignment object from the alignments of two Reads
	 * @param p1 the Alignment object for a read
	 * @param p2 the Alignment of the matePair of the same read
	 */
	public CloneAlignment(Clone parent, Alignment p1, Alignment p2) {
		super((SequenceSegment) p1.getSequenceSegment1().clone(), 
				new SequenceSegment(parent,
						1,
						Math.max(p1.getEnd(),p2.getEnd()) - Math.min(p1.getStart(),p2.getStart())
						),
				true,null);

		if (p2.getStart()<p1.getStart()) {
			getSequenceSegment1().setStart(p2.getStart());
		}
		getSequenceSegment1().setEnd(Math.max(p1.getEnd(),p2.getEnd()));
		
		a1 = p1;
		a2 = p2;

	}

//	/**
//	 * Override Alignment.getBase because MatePair diffs are really read diffs
//	 * @param pPos something between 1 and getLength()
//	 * @return base at the specified position
//	 */
//	public char getBase(int pPos) {
//		// is this in the first read?
//		int x = pPos - a1.getLength();
//		if (x<=0) {
//			return a1.getBase(pPos);
//		}
//		
//		// does this fall in the gap?
//		int gap = a2.getStart()-a1.getEnd();
//		x = x-gap;
//		if (x<=0) {
//			return getSequenceSegment1().getSequence().getBase(getReferencePos(pPos));
//		}
//		
//		// this is in read 2
//		return a2.getBase(x);
//	}
	
	public Object clone() {
		// the alignment objects are not cloned.  It is assumed for now
		//  that a read's alignment and its association in a matepair are immmutable
		CloneAlignment c = new CloneAlignment((Clone)getSequenceSegment2().getSequence(),a1,a2);
		
		if (super.getDiffs() != null) {
			ArrayList<Difference> cDiffs = new ArrayList<Difference>();
			cDiffs.addAll(super.getDiffs());
			c.setDiffs(cDiffs);
		}
		return c;
	}
	
	/**
	 * @return Returns the list of Differences.
	 */
	public List<Difference> getDiffs() {
		if (super.getDiffs()==null) {
			setDiffs(lookUpDiffs());
		}
		
		return super.getDiffs();
	}

	// get position in mate pair of this position in read 2
	//  offset by length of read1's alig
	//  offset by distance between read1 and read2
	//  ofset by unaligned sequences at start of read2
	private int getMatePairPositionFromRead2(int pPos) {
		return a1.getLength() + (a2.getStart() - a1.getEnd()) - a2.getSequenceSegment2().getStart() + pPos;
	}
	
	// get position in mate pair of this position in read 1
	//  ofset by unaligned sequences at start of read2
	private int getMatePairPositionFromRead1(int pPos) {
		return pPos - a1.getSequenceSegment2().getStart();
	}

	private ArrayList<Difference> lookUpDiffs() {
	 	//copy diffs from each
		ArrayList<Difference> diffs = new ArrayList<Difference>(); 
		
		// get diffs from included reads
		List<Difference> d1 = a1.getDiffs();
		List	<Difference> d2 = a2.getDiffs();

		int i1=0;
		int i2=0;
		Difference d;
		while (true) {
			if (i1<d1.size()) {
				Difference diff1 = d1.get(i1);
				if (i2<d2.size()) {
					Difference diff2 = d2.get(i2);
					if (diff2.getPosition1()<diff1.getPosition1()) {
						// clone diff and change position2 so it's the position in the MP not the read
						d = (Difference) diff2.clone();
						d.setPosition2(getMatePairPositionFromRead2(diff2.getPosition2()));
						diffs.add(d);
						i2++;
					} else if (diff1.getPosition1()<diff2.getPosition1()) {
						// if read 1 algnment does not start at the read's 1st base, we need to adjust diffs
						d = (Difference) diff1.clone();
						d.setPosition2(getMatePairPositionFromRead1(diff1.getPosition2()));
						diffs.add(d);
						i1++;
					} else {
						if (diff1.getBase1()=='-') {
							if (diff2.getBase1()=='-') {
								//pick one
								diffs.add(diff1);
								i1++;
								i2++;
							} else {
								diffs.add(diff1);
								i1++;
							}
						} else {
							if (diff2.getBase1()=='-') {
								// clone diff and change position2 so it's the position in the MP not the read
								d = (Difference) diff2.clone();
								d.setPosition2(getMatePairPositionFromRead2(diff2.getPosition2()));
								diffs.add(d);
								i2++;
							} else {
								//pick one
								diffs.add(diff1);
								i1++;
								i2++;
							}
						}
					}
				} else {
					diffs.add(diff1);
					i1++;
				}
			} else {
				if (i2<d2.size()) {
					Difference diff2 = d2.get(i2);
					// clone diff and change position2 so it's the position in the MP not the read
					d = (Difference) diff2.clone();
					d.setPosition2(getMatePairPositionFromRead2(diff2.getPosition2()));
					diffs.add(d);
					i2++;
				} else {
					break;
				}
			}
		}
		
		return diffs;
	}

	public void close() {
		a1 = null;
		a2 = null;
	}
	
	public List<SequenceSegment> getUnknownRegions() {
		// this should return the empty space between the two Reads (if they do not overlap)
		if (unknownRegions==null)  {
			unknownRegions = new ArrayList<SequenceSegment>();
			if (a1.getEnd()<a2.getStart()) {
				unknownRegions.add(new SequenceSegment(a1.getSequenceSegment1().getSequence(),a1.getEnd(),a2.getStart()));
			} else if (a2.getEnd()<a1.getStart()) {
				unknownRegions.add(new SequenceSegment(a1.getSequenceSegment1().getSequence(),a2.getEnd(),a1.getStart()));
			}
		}
		
		return unknownRegions;

	}
}
