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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Extension of the alignment object specifically for strains.  Will automatcially build Difference array and List of Unknown Regions
 * (uncovered regions) by inspecting the Strain object.
 * 
 * @author jmeppley
 *
 */
public class StrainAlignment extends Alignment {
	
	private Strain parent = null;
	int [] strainGaps = null;

	/**
	 * Creates a new StrainAlignment object with the following parameters. The direction is set
	 * to true and thee Difference array is initially empty.  It will be populated, the first time getDiffs()
	 * is called.  It is assumed that the entire strain is aligned (SequenceSegment2==null).
	 * 
	 * @param pSS1 region on reference sequence that this Strain aligns to
	 * @param pStrain the Strain object
	 */
	public StrainAlignment(SequenceSegment pSS1, Strain pStrain) {
		super(pSS1,null,true,null);
		parent = pStrain;
	}
	
	/**
	 * Creates a new StrainAlignment object with the following parameters. The direction is set
	 * to true and thee Difference array is initially empty.  It will be populated, the first time getDiffs()
	 * is called.
	 * 
	 * @param pSS1 region on reference sequence that this Strain aligns to
	 * @param pSS2 region in stain aligned to references (null => entire sequence)
	 * @param pStrain the Strain object
	 */
	public StrainAlignment(SequenceSegment pSS1,SequenceSegment pSS2, Strain pStrain) {
		super(pSS1, pSS2,true,null);
		parent = pStrain;
	}

	/** 
	 * Cloning not supported: Use setAlignmentFromReads() for parent strain instead.
	 */
	public Object clone() {
		throw new RuntimeException("Cloning not supported: Use setAlignmentFromReads() for parent strain instead.");
	}
	
	/**
	 * @return Returns the list of differences.
	 */
	public List<Difference> getDiffs() {
		if (super.getDiffs()==null) {
			try {
				setDiffs(calculateDiffs());
			} catch (Exception e) {
				e.printStackTrace(System.err);
				return new ArrayList<Difference>();
			}
		}		
		return super.getDiffs();
	}

	private List<Difference> calculateDiffs() {
	 	// merge diffs from all reads
		List<Difference> diffs = new ArrayList<Difference>(); 

		// get a sorted map of all diffs grouped by position 			
		SortedMap<Integer,List<Difference>> diffListsByPos = new TreeMap<Integer,List<Difference>>();

		// loop over readables...build map
		Iterator<Readable> rit = parent.getReadableIterator();
		while (rit.hasNext()) {
			Readable read = rit.next();
			for (Difference diff : read.getAlignment().getDiffs()) {
				Integer pos = new Integer(diff.getPosition1());
				List<Difference> posDiffs = diffListsByPos.get(pos);
				if (posDiffs == null) {
					posDiffs = new ArrayList<Difference>();
					diffListsByPos.put(pos,posDiffs);
				}
				posDiffs.add(diff);
			}
		}
		
//		int [] diffCoverage = new int [diffListsByPos.size()];
		SortedMap<Integer,Short> diffCoverageMap = new TreeMap<Integer,Short>();
		
		// loop over readables build table of coverage at each diff
		rit = parent.getReadableIterator();
		while (rit.hasNext()) {
			Readable read = rit.next();
			if (read instanceof Read) {
				SortedMap<Integer,List<Difference>> coveredDiffMap = diffListsByPos.subMap(read.getStart(),read.getEnd()+1);
				for (Integer diffPos : coveredDiffMap.keySet()) {
					Short coverage = diffCoverageMap.get(diffPos);
					if (coverage==null) {
						diffCoverageMap.put(diffPos,(short)1);
					} else {
						diffCoverageMap.put(diffPos, (short)(coverage.shortValue()+1));
					}
				}
			} else {
				Clone clone = (Clone) read;
				SortedMap<Integer,List<Difference>> coveredDiffMap = diffListsByPos.subMap(clone.reads[0].getStart(),clone.reads[0].getEnd()+1);
				for (Integer diffPos : coveredDiffMap.keySet()) {
					Short coverage = diffCoverageMap.get(diffPos);
					if (coverage==null) {
						diffCoverageMap.put(diffPos,(short)1);
					} else {
						diffCoverageMap.put(diffPos, (short)(coverage.shortValue()+1));
					}
				}
				coveredDiffMap = diffListsByPos.subMap(clone.reads[1].getStart(),clone.reads[1].getEnd()+1);
				for (Integer diffPos : coveredDiffMap.keySet()) {
					Short coverage = diffCoverageMap.get(diffPos);
					if (coverage==null) {
						diffCoverageMap.put(diffPos,(short)1);
					} else {
						diffCoverageMap.put(diffPos, (short)(coverage.shortValue()+1));
					}
				}
			}
		}
		
		/*
		 * get sorted list of positions, loop
		 */
		ArrayList<Integer> refSeqGaps = new ArrayList<Integer>();
		ArrayList<Integer> strainGaps = new ArrayList<Integer>();
		for (Map.Entry<Integer,List<Difference>> e : diffListsByPos.entrySet()) {
			List<Difference> posDiffs = e.getValue();
			
//			System.out.println("getting consensus at position " + e.getKey());

			// count the different things that can happen at this diff
			//  some will have a different base or a gap
			int readBaseCountA = 0;
			int readBaseCountG = 0;
			int readBaseCountC = 0;
			int readBaseCountT = 0;
			int readBaseCountN = 0;
			int readBaseCountX = 0;
			int readBaseCountGap = 0;
			//  some will have a base where the reference sequence has a gap
			int refSeqGapCountA = 0;
			int refSeqGapCountG = 0;
			int refSeqGapCountC = 0;
			int refSeqGapCountT = 0;
			int refSeqGapCountN = 0;
			int refSeqGapCountX = 0;
			int refSeqGapCountGap = 0;
				
			// loop over all diffs at this position
			Iterator<Difference> rdit = posDiffs.iterator();
			Difference diff = null;
			while (rdit.hasNext()) {
				diff = rdit.next();
//				System.out.println(diff.toString());
				
				if (diff.getBase1()=='-') {
					// refSeq is gapped, increment the appropriate counter
					switch (Character.toLowerCase(diff.getBase2())){
					case 'a':  refSeqGapCountA++; break;
					case 'c':  refSeqGapCountC++; break;
					case 'g':  refSeqGapCountG++; break;
					case 't':  refSeqGapCountT++; break;
					case 'n':  refSeqGapCountN++; break;
					case 'x':  refSeqGapCountX++; break;
					case '-':  refSeqGapCountGap++; break;
					}
				} else {
					// refSeq is NOT gapped, increment the appropriate counter
					switch (Character.toLowerCase(diff.getBase2())) {
					case 'a':  readBaseCountA++; break;
					case 'c':  readBaseCountC++; break;
					case 'g':  readBaseCountG++; break;
					case 't':  readBaseCountT++; break;
					case 'n':  readBaseCountN++; break;
					case 'x':  readBaseCountX++; break;
					case '-':  readBaseCountGap++; break;
					}
				}    
			}
			
			// get this position on the refSeq
			int ePos = diff.getPosition1();
			int sPos = ePos - getStart() + 1 + refSeqGaps.size() - strainGaps.size();
			
//			// count the number of reads that overlap this position
//			// (this is slow, but I don't have an alternative)
//			int readCount = 0;
////			System.out.println("parent has " + parent.size + " reads and spans " + parent.getStart() + " to " + parent.getEnd());
//			Iterator<Read> it = parent.getReadIterator();
//			while (it.hasNext()) {
//				Read r = it.next();
//				boolean intersects = r.intersectsRefereceSequenceAt(ePos);
////				System.out.println("Read " + r.getName() + " (" + r.getStart() + "-" + r.getEnd() + ":" + intersects);
//				if (intersects) {
//					readCount++;
//				}
//			}

			// get teh coverage at ths pos
			short readCount = diffCoverageMap.get(ePos);
			
//			System.out.println("There are " + readCount + " reads at position " + ePos);
			
			// this is ugly, but I'd have to do things completely 
			// differently to avoid it
			char base = '-';
			int gapcount = refSeqGapCountA+refSeqGapCountC+refSeqGapCountG+refSeqGapCountN+refSeqGapCountT+refSeqGapCountX;
			// this is the number of bases with no gaps
			int basecount = readCount - gapcount;

			// look for the most popular option
			if (refSeqGapCountA>basecount) {
				base = 'a';
				basecount = refSeqGapCountA;
			}
			if (refSeqGapCountC>basecount) {
				base = 'c';
				basecount = refSeqGapCountC;
			}
			if (refSeqGapCountT>basecount) {
				base = 't';
				basecount = refSeqGapCountT;
			}
			if (refSeqGapCountG>basecount) {
				base = 'g';
				basecount = refSeqGapCountG;
			}
			if (refSeqGapCountN>basecount) {
				base = 'n';
				basecount = refSeqGapCountN;
			}
			if (refSeqGapCountX>basecount) {
				base = 'x';
//				basecount = refSeqGapCountX;
			}
			
			// if one of the gap counts was the largest count, add a gap diff
			if (base!='-') {
				refSeqGaps.add(new Integer(sPos));
				diffs.add(new Difference(ePos,'-',sPos,base));
//				System.out.println("Added gap: " + base);
			}
			
			base = '0';
			int diffcount = readBaseCountA+readBaseCountC+readBaseCountG+readBaseCountT+readBaseCountN+readBaseCountX+readBaseCountGap;
			// this is the number of reads with the reference base
			basecount = readCount - diffcount;
			
			if (readBaseCountA>basecount) {
				base = 'a';
				basecount = readBaseCountA;
			} else if (readBaseCountC>basecount) {
				base = 'c';
				basecount = readBaseCountC;
			} else if (readBaseCountT>basecount) {
				base = 't';
				basecount = readBaseCountT;
			} else if (readBaseCountG>basecount) {
				base = 'g';
				basecount = readBaseCountG;
			} else if (readBaseCountN>basecount) {
				base = 'n';
				basecount = readBaseCountN;
			} else if (readBaseCountX>basecount) {
				base = 'x';
				basecount = readBaseCountN;
			} else if (readBaseCountGap>basecount) {
				base = '-';
//				basecount = readBaseCountGap;
			}
			
			// did a dif have a plurality?
			if (base!='0') {
				if (base=='-') {
					// in the gap case, make a special note of the position
					strainGaps.add(new Integer(sPos));
				}
				// in all diff cases, create a new diff object
				char refSeqBase = getSequenceSegment1().getSequence().getBase(ePos);
				diffs.add(new Difference(ePos,refSeqBase,sPos,base));
//				System.out.println("added diff: " + base);
			} else {
				// don't add a diff, since the plurality have ref seq base
			}
		}

		return diffs;
	}

	public List<SequenceSegment> getUnknownRegions() {
		if (unknownRegions==null)  {
			unknownRegions = findUnknownRegions();
		}
		
		return unknownRegions;

	}
	
	/*
	 * TODO:5 combine this with Strain.setAlignmentFromReads()?
	 */
	private List<SequenceSegment> findUnknownRegions() {
		List<SequenceSegment> holes = new ArrayList<SequenceSegment>();

		Iterator<Read> it = parent.getReadIterator();
		// only proceed if there are reads
		if (it.hasNext()) {
			// setup on first read
			Read read = it.next();
			// get ref seq object
			ReferenceSequence refSeq = (ReferenceSequence) read.getAlignment().getSequenceSegment1().getSequence();
			// keeps track of maximum extent
			SequenceSegment range = null;

			// initialize range
			range = new SequenceSegment(refSeq,
					read.getStart(),
					read.getEnd());
			
			// loop over remaining reads
			while (it.hasNext()) {
				read = it.next();
				updateConnected(read,refSeq,holes,range);
			}
		}
		
		return holes;
	}

	private static void updateConnected(Read read, ReferenceSequence pRefSeq, List<SequenceSegment> holes, 
			SequenceSegment range) {
		int s = read.getAlignment().getStart();
		int e = read.getAlignment().getEnd();
		
		if (s < range.getStart()) {
			if (e > range.getStart()) {
				if (e > range.getEnd()) {
					range.setStart(s);
					range.setEnd(e);
					holes.clear();
				} else {
					// overlaps beginning: extend start and look for holes in overlap
					range.setStart(s);
					for (int i=0; i<holes.size(); i++) {
						SequenceSegment hole = holes.get(i);
						
						if (hole.getEnd()<=e) {
							holes.remove(hole);
						} else if (hole.containsPosition(e)) {
							hole.setStart(e+1);
							// rest of the holes won't overlap
							break;
						} else {
							// rest of the holes wont overlap
							break;
						}
					}
				}
			} else {
				// entire read before range, extend and add new hole if there is real separation
				if (range.getStart()-e > 1) {
					SequenceSegment hole = new SequenceSegment(pRefSeq,e+1,range.getStart()-1);
					// add to beginning of list
					holes.add(0,hole);
				}
				// extend range start
				range.setStart(s);
			}
		} else if (s<range.getEnd()) {
			if (e >= range.getEnd()) {
				// overlaps end
				range.setEnd(e);
				for (int i=holes.size()-1; i>=0; i--) {
					SequenceSegment hole = holes.get(i);
					if (hole.getStart()>=s) {
						holes.remove(hole);
					} else if (hole.containsPosition(s)) {
						hole.setEnd(s-1);
						// we're going backwards, so remoaning holes won't overlap
						break;
					} else {
						// we're going backwards, so remoaning holes won't overlap
						break;
					}
				}
			} else {
				// new read is completely within range
				//  look for overlapping holes
//				HashSet newHoles = new HashSet();
				for (int i=0; i<holes.size(); i++) {
					SequenceSegment hole = holes.get(i);
					if (hole.getStart()>=s) {
						if (hole.getEnd()<=e) {
							//hole completely in read
							holes.remove(i);
							// roll back i so looping stays true
							i--;
						} else {
							if (hole.getStart()<=e) {
								// read overlaps beginning of hole, clip
								hole.setStart(e+1);
							} else {
								//hole is to the right of read, rest will be, too, so quit
								break;
							}
						}
					} else {
						if (hole.getEnd()<s) {
							// hole is to the left of read, try next one
							continue;
						} else  {
							if (hole.getEnd()<=e)  {
								// hole overlaps begining of reads, clip
								hole.setEnd(s-1);
							} else {
								// read is completely within hole, split in two
								SequenceSegment newHole = 
									new SequenceSegment(pRefSeq,
											hole.getStart(),
											s-1);
								holes.add(i,newHole);
								hole.setStart(e+1);
								// no more holes are relevant
								break;
							}
						}
					}
				}
			}
		} else {
			// entire read after range, extend and create hole if needed
			if (s-range.getEnd() > 1) {
				SequenceSegment hole = new SequenceSegment(pRefSeq,range.getEnd()+1,s-1);
				// add hole to end
				holes.add(hole);
			}
			// extend range
			range.setEnd(e);
		}
	}
	
	public boolean isUncovered(int pPos) {
		List urs = getUnknownRegions();
		for (int i=0; i<urs.size(); i++) {
			SequenceSegment ur = (SequenceSegment) urs.get(i);
			if (ur.containsPosition(pPos)) {
				return true;
			}
		}
		return false;
	}

	
}


