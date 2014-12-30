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

import java.util.*;

public class DiffIterator {
	
	/* Set of reads we are investigating */
	HashMap<Integer,Read> reads = new HashMap<Integer,Read>();
	
	/* set to tru once we hit the end */
	boolean finished = false;
	/* map to keep track of where we are in the diff arrays for each read */
	HashMap<Integer,Integer> indices = new HashMap<Integer,Integer>();
	/* list of positions that have diffs */ 
	TreeMap<Integer,Set<Integer>> upcomingDiffs = new TreeMap<Integer,Set<Integer>>();
	/* list of reads that overlap pos */
	HashSet<Integer> activeReads = new HashSet<Integer>();
	
	private static final Character GAP_CHAR = new Character('-');
	
	int pos = -1;
	int start = -1;
	int end = -1;
	
	public DiffIterator(HashMap<Integer,Read> pReads, int pStart, int pEnd) {
		start = pStart;
		pos = start;
		end = pEnd;
		
		// fill reads hash only with reads that intersect our range
		for (Integer readId : pReads.keySet()) {
			Read read = pReads.get(readId);
			if (read.getStart()<=end && read.getEnd()>=start) {
				reads.put(readId, read);
				
				// While we're at it, find first diffs
				// loop over diffs until we find one past pos
				//  diffs will be in order
				for (int i = 0; i < read.getAlignment().getDiffs().size(); i++) {
					Difference diff = read.getAlignment().getDiffs().get(i);
					if (diff.getPosition1() >= pos &&
							diff.getPosition1() <= end) {
						// save this diff in all the structures
						indices.put(readId,i);
						Set<Integer> ids = upcomingDiffs.get(diff.getPosition1());
						if (ids==null) {
							ids = new HashSet<Integer>();
							upcomingDiffs.put(diff.getPosition1(),ids);
						} 
						ids.add(readId);
						
						break;
					}
				}
				
				// check if this read is active
				if (read.getStart() <= start) {
					activeReads.add(readId);
				}
			}
		}
	}
	
	public boolean hasNext() {
		return (upcomingDiffs.size() > 0);
	}
	
	public TreeMap<Integer,Map<Character,Set<Read>>> next() {
		if (upcomingDiffs.size() == 0) { return null; }
		
		// get pos and list of reads for next difference
		Integer nextDiffPos = upcomingDiffs.firstKey();
		Set<Integer> nextDiffs = upcomingDiffs.remove(nextDiffPos);
		
		// initialize return map
		HashMap<Character,Set<Read>> bases = new HashMap<Character,Set<Read>>();
		
		// we want to deal with non-gapped alignments first,
		//  so if any are gapped, hold them til next call
		int nGapped = 0;
		HashMap<Integer,Character> gapped = new HashMap<Integer,Character>();
		int nNotGapped = 0;
		
		// this will get set to the proper value if anything doesn't gap
		Character consensusBase = GAP_CHAR;
		for (Integer id : nextDiffs) {
			Integer diffIndex = indices.get(id);
			Read read = reads.get(id);
			Difference diff = read.getAlignment().getDiffs().get(diffIndex.intValue());
			if (diff.getBase1()=='-') {
				nGapped++;
				gapped.put(id,new Character(diff.getBase2()));
			} else {
				nNotGapped++;
				
				// enter this read into return map
				Character readBase = new Character(diff.getBase2());
				Set<Read> reads4Base = bases.get(readBase);
				if (reads4Base==null) {
					reads4Base = new HashSet<Read>();
					bases.put(readBase,reads4Base);
				} 
				reads4Base.add(read);
				
				// While we have a diff, get the consensus base for this pos
				if (consensusBase==GAP_CHAR) {
					consensusBase=new Character(diff.getBase1());
				}
			}
		}
		
		// update upcoming diffs
		for (Integer id : nextDiffs) {
			Integer pos = null;
			if (nGapped>0 && nNotGapped>0 && gapped.containsKey(id)) {
				// postpone this diff til next next() call
				pos = nextDiffPos;
			} else {
				int diffIndex = indices.get(id);
				int newIndex = diffIndex + 1;
				Read read = reads.get(id);
				if (newIndex>=read.getAlignment().getDiffs().size()) {
					pos = new Integer(
							read.getAlignment()
							.getDiffs()
							.get(diffIndex)
							.getPosition1());
					indices.put(id,newIndex);
				} else {
					indices.remove(id);
					continue;
				}
			}
			
			// update upcoming diff hash
			Set<Integer> ids = upcomingDiffs.get(pos);
			if (id==null) {
				ids = new HashSet<Integer>();
				upcomingDiffs.put(pos,ids);
			}
			ids.add(id);
		}
		
		// Warning: duplicated code.  Make changes twice if nec.!
		
		if (nGapped>0 && nNotGapped==0) {
			for (Integer readId : reads.keySet()) {
				Read read = reads.get(readId);
				
				Character readBase = GAP_CHAR;
				if (gapped.containsKey(readId)) {
					readBase = gapped.get(readId);
					activeReads.add(readId);
				} else {
					// check if read overlaps this position
					if (read.intersectsRefereceSequenceAt(nextDiffPos.intValue())) {
						activeReads.add(readId);
					} else {
						if (!activeReads.remove(readId)) {
							continue;
						}
						readBase = null;
					}
				}
				
				Set<Read> reads4Base = bases.get(readBase);
				if (reads4Base==null) {
					reads4Base = new HashSet<Read>();
					bases.put(readBase,reads4Base);
				} 
				reads4Base.add(read);
			}
		} else  {
			for (Integer readId : reads.keySet()) {
				if (nextDiffs.contains(readId)) { 
					// already added to hash, skip
					continue;
				}
				
				Read read = reads.get(readId);
				
				Character readBase = consensusBase;
				// check if read overlaps this position
				if (read.intersectsRefereceSequenceAt(nextDiffPos.intValue())) {
					activeReads.add(readId);
				} else {
					if (!activeReads.remove(readId)) {
						continue;
					}
					readBase = null;
				}
				
				Set<Read> reads4Base = bases.get(readBase);
				if (reads4Base==null) {
					reads4Base = new HashSet<Read>();
					bases.put(readBase,reads4Base);
				} 
				reads4Base.add(read);
			}
		}
		
		TreeMap<Integer,Map<Character,Set<Read>>> ret = new TreeMap<Integer,Map<Character,Set<Read>>>();
		ret.put(nextDiffPos,bases);
		return ret;
	}
	
	public HashSet<Integer> getActiveReadsAtEnd() {
		HashSet<Integer> ret = new HashSet<Integer>();
		
		for (Integer readId : reads.keySet()) {
			Read read = reads.get(readId);
			
			if (read.getStart()<=end && read.getEnd()>=end) {
				ret.add(readId);
			}
		}
		return ret;
	}
}	    


