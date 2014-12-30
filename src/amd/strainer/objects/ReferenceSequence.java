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
package amd.strainer.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import amd.strainer.DuplicateReadNameException;

/**
 * The Sequence against which all other displayed objects are aligned TODO:5
 * implement or extend BioJava framework for this ?
 * 
 * @author jmeppley
 * 
 */
public class ReferenceSequence extends AbstractSequence implements ReadHolder {
	/**
	 * The reads aligned to this Sequence
	 */
	public HashMap<Integer, Read> reads = new HashMap<Integer, Read>();
	/**
	 * The Strains into which the Reads are grouped
	 */
	public HashMap<Integer, Strain> strains = new HashMap<Integer, Strain>();
	/**
	 * The annotations on this ReferenceSequence
	 */
	public HashMap<String, Gene> genes = new HashMap<String, Gene>();
	/**
	 * The largest ID of strains associated with this sequence. Add 1 to this to
	 * generate ID for new strain
	 */
	public int maxStrainId = 0;

	public boolean hasQualityData = false;

	public ReferenceSequence() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amd.strainer.objects.ReadHolder#getReadIterator()
	 */
	public Iterator<Read> getReadIterator() {
		return reads.values().iterator();
	}

	/**
	 * The path of the file (if one was used) from which the strains associated
	 * with this ReferenceSequence were read
	 */
	public String strainsFile = null;
	private Map<String, Integer> mReadIdMap = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuffer()
				.append(getName())
				.append("(")
				.append(getId())
				.append(") has ")
				.append(reads.size())
				.append(" reads and ")
				.append(strains.size())
				.append(" strains.")
				.toString();
	}

	/**
	 * Prints a summary of this object.
	 * <p>
	 * spcifically, the name followed by all the strain ID's
	 * 
	 * @return a string describing this object
	 */
	public String detailsString() {
		StringBuffer ret = new StringBuffer().append(getName()).append("(");
		for (Strain strain : strains.values()) {
			ret.append(strain.toString()).append(":");
		}
		return ret.toString();
	}

	/**
	 * Add a Strain to this objects collection. Also, adjust the maxStrainId
	 * value if necessary
	 * 
	 * @param pId
	 *            The strain's ID asn an Integer
	 * @param pStrain
	 *            Strain to add to ReferenceSequence
	 */
	public void putStrain(Integer pId, Strain pStrain) {
		strains.put(pId, pStrain);
		maxStrainId = Math.max(maxStrainId, pId.intValue());
	}

	/**
	 * Add a strain that dones not have an id to the ReferenceSequence. An ID
	 * will be chosen and set. maxStrainId will be incremented as well
	 * 
	 * @param pStrain
	 *            strain to be added to ReferenceSequence
	 */
	public void addStrainWithNoId(Strain pStrain) {
		maxStrainId++;
		strains.put(new Integer(maxStrainId), pStrain);
		pStrain.setId(maxStrainId);
	}

	/**
	 * @return a strainId that won't interfere with other strains already
	 *         created
	 */
	public int getNextStrainId() {
		return maxStrainId + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amd.strainer.objects.AbstractSequence#close()
	 */
	@Override
	public void close() {
		super.close();
		reads = null;
		strains = null;
		genes = null;
	}

	/**
	 * Changes the reference sequence and removes diffs from member sequences at
	 * this position
	 * 
	 * @param position1
	 * @param base2
	 */
	public void setBase(int position, char newBase, char oldBase) {
		setBase(position, newBase, oldBase, null);
	}

	/**
	 * Changes the reference sequence and removes diffs from member sequences at
	 * this position
	 * 
	 * @param position1
	 * @param base2
	 */
	public void setBase(int position, char newBase, char oldBase,
			Collection<Read> pReads) {
		if (oldBase == newBase) {
			return;
		}

		// do we need to adjust the refSeq size
		short change = 0;
		if (oldBase == '-') {
			change = 1;
		} else if (newBase == '-') {
			change = -1;
		}

		// update sequence
		StringBuffer newBases = new StringBuffer(getBases());
		if (oldBase != '-')
			newBases.delete(position - 1, position);
		if (newBase != '-')
			newBases.insert(position - 1, newBase);
		setBases(newBases.toString());
		setLength(getLength() + change);

		// if no reads passed in, try all reads
		if (pReads == null) {
			pReads = reads.values();
		}

		// update diffs
		for (Read read : pReads) {
			// change diff
			updateDiff(read, position, newBase, oldBase);

			// reset alignment diffs for any dependent objects (strains, clones)
			if (read.getClone() != null) {
				// set diffs to null--they will be recalculated next time they
				// are needed
				read.getClone().getAlignment().setDiffs(null);
			}
			if (read.getStrain() != null) {
				// set diffs to null--they will be recalculated next time they
				// are needed
				read.getStrain().getAlignment().setDiffs(null);
			}

		}

		// if we change the reference sequence length, we have to adjust every
		// start,end and diff
		// above this point
		if (change != 0) {
			Set<Clone> pendingClones = new HashSet<Clone>();

			// loop over strains
			for (Strain strain : strains.values()) {
				// loop over reads
				for (Readable rable : strain.getReadables()) {
					if (rable instanceof Clone) {
						Clone clone = (Clone) rable;
						// adjust start/end/diff positions if above positions
						moveRead(clone.reads[0], position, change);
						moveRead(clone.reads[1], position, change);
						moveClone(clone, position, change);
					} else {
						Read read = (Read) rable;
						// adjust start/end/diff positions if above positions
						moveRead(read, position, change);
						// look for clone in pending...if yes, remove and
						// reset... if no, add
						if (read.getClone() != null) {
							if (pendingClones.remove(read.getClone())) {
								moveClone(read.getClone(), position, change);
							} else {
								pendingClones.add(read.getClone());
							}
						}
					}
				}
				// rebuild strain alig from reads or maybe just fix start/end
				// and clear diffs
				moveStrain(strain, position, change);
			}
		}
	}

	private boolean moveAlignedSequence(AlignedSequence pAS, int position,
			int change) {
		// if position is in or before AS, adjust start/end
		if (position <= pAS.getEnd()) {
			pAS.getAlignment().getSequenceSegment1().setEnd(
					pAS.getEnd() + change);
			if (position < pAS.getStart()) {
				pAS.getAlignment().getSequenceSegment1().setStart(
						pAS.getStart() + change);
			} else {
				pAS.getAlignment().getSequenceSegment2().setEnd(
						pAS.getAlignment().getSequenceSegment2().getEnd()
								+ change);
			}
			return true;
		}

		// let caller know if we didn't have to change anything
		return false;
	}

	private void moveStrain(Strain pStrain, int position, int change) {
		// adjust start end
		if (moveAlignedSequence(pStrain, position, change)) {
			// if it moved, clear diffs so they are rebuilt from reads next time
			pStrain.getAlignment().setDiffs(null);
		}
	}

	private void moveClone(Clone pClone, int position, int change) {
		// adjust start end
		if (moveAlignedSequence(pClone, position, change)) {
			// if it moved, clear diffs so they are rebuilt from reads next time
			pClone.getAlignment().setDiffs(null);
		}
	}

	private void moveRead(Read pRead, int position, int change) {
		// adjust start end
		if (moveAlignedSequence(pRead, position, change)) {
			// if it moved, adjust postion of diffs so they are in the correct
			// spot
			// List<Difference> diffs = pRead.getAlignment().getDiffs();
			for (Difference diff : pRead.getAlignment().getDiffs()) {
				if (diff.getPosition1() > position) {
					diff.setPosition1(diff.getPosition1() + change);
				}
			}
		}
	}

	private void updateDiff(AlignedSequence aSeq, int position, char newBase,
			char oldBase) {
		char newBaseLower = Character.toLowerCase(newBase);

		// only consider overlapping sequences
		if (aSeq.getStart() <= position && aSeq.getEnd() >= position) {
			List<Difference> newDiffs = new ArrayList<Difference>();
			boolean passedPosition = false;

			// loop over diffs
			for (Difference diff : aSeq.getAlignment().getDiffs()) {
				if (diff.getPosition1() < position) {
					newDiffs.add(diff);
				} else if (diff.getPosition1() == position) {
					if (Character.toLowerCase(diff.getBase2()) == newBaseLower
							|| diff instanceof QualifiedDifference
							&& Character
									.toLowerCase(((QualifiedDifference) diff)
											.getBase2Actual()) == newBaseLower) {
						// skip this diff
						passedPosition = true;
					} else {
						// change diff
						diff.setBase1(newBase);
						// otherwise, diffs are unchanged...
						return;
					}
				} else {
					if (!passedPosition) {
						// there was no diff...add one to reflect change
						// NOTE: there is no quality data available in this case
						Difference newDiff = new Difference(
								position,
								newBase,
								aSeq.getAlignment().getPosFromReference(
										position),
								oldBase);
						newDiffs.add(newDiff);
						passedPosition = true;
					}
					newDiffs.add(diff);
				}
			}
			if (!passedPosition) {
				// there was no diff at position...add one to reflect change
				Difference newDiff = new Difference(position, newBase, aSeq
						.getAlignment()
						.getPosFromReference(position), oldBase);
				newDiffs.add(newDiff);
			}

			// replace diffs
			aSeq.getAlignment().setDiffs(newDiffs);
		}
	}

	/**
	 * Update nucleotide sequence of reference sequence to match the indicated
	 * AlignedSequence for the length of the alignment. Unknown bases (n's) are
	 * ignored.
	 * 
	 * @param seq
	 *            A Strain or Read
	 */
	public void setToSequence(AlignedSequence seq) {
		List<Difference> diffs = seq.getAlignment().getDiffs();

		// build map of overlapping reads for each diff
		SortedMap<Integer, List<Read>> diffSS = new TreeMap<Integer, List<Read>>();
		// initialize map
		for (Difference diff : diffs) {
			diffSS.put(diff.getPosition1(), new ArrayList<Read>());
		}

		// add each read to overlapped diffs
		for (Read read : reads.values()) {
			SortedMap<Integer, List<Read>> coveredDiffMap = diffSS.subMap(read
					.getStart(), read.getEnd() + 1);
			for (List<Read> readList : coveredDiffMap.values()) {
				readList.add(read);
			}
		}

		// update each diff
		for (Difference diff : diffs) {
			if (Character.toLowerCase(diff.getBase2()) != 'n') {
				setBase(
						diff.getPosition1(),
						diff.getBase2(),
						diff.getBase1(),
						diffSS.get(diff.getPosition1()));
			}
		}
	}

	public Map<String, Integer> getReadIdMap()
			throws DuplicateReadNameException {
		// TODO: find a way to nullify (or update) when reads changed
		// if (mReadIdMap == null) {
		mReadIdMap = new HashMap<String, Integer>(reads.size());
		for (Integer id : reads.keySet()) {
			String readName = reads.get(id).getName();
			if (mReadIdMap.containsKey(readName)) {
				throw new DuplicateReadNameException(readName);
			}
			mReadIdMap.put(readName, id);
		}
		// }
		return mReadIdMap;
	}
}
