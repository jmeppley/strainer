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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import amd.strainer.display.DisplayData;
import amd.strainer.display.StrainDisplayGeometry;

/**
 * Represents a collection of reads that have been grouped together by the user. Reads and Clones can
 *  be added (as Readables). If a solo read is added and its matepair is already in the strain,
 *  the two will be linked. Additionally, the read will be removed from any other strains unless stealReads
 *  is set to false.
 * 
 * @author jmeppley
 *
 */
public class Strain extends AbstractAlignedSequence implements ReadHolder {
	/**
	 * If true, any Readable added to this Strain will be removed from it's previous strain
	 *  (using Readable.strain). In other words, a Readable can only be in one Strain at a time 
	 *  except for strains where stealReads is false.
	 */
	public boolean stealReads = true;
	
	/*
	 * The Readables in this strain. It should only be used read-only. Keys are Readable.getIdInteger()
	 */
	private HashMap<Integer,Readable> reads = new HashMap<Integer,Readable>();

	/**
	 *@return The number of Reads (not Readables) in this strain.
	 */
	public int getSize() { return size; }
	private int size = 0;
	
	/* (non-Javadoc)
	 * Returns an iterator over the Reads (not Readables) in this strain
	 * @see amd.strainer.objects.ReadHolder#getReadIterator()
	 */
	public Iterator<Read> getReadIterator() { return this.new ReadIterator(reads.values().iterator()); }
	
	/**
	 * @return an iterator over the Readables in this Strain
	 */
	public Iterator<Readable> getReadableIterator() {return reads.values().iterator(); }
	
	/**
	 * Return a collection of this Strain's Readables
	 */
	public Collection<Readable> getReadables() {
		return reads.values();
	}
	
	/**
	 * Return a collection of this Strain's Readables, sorted by Identity
	 */
	public List<Readable> getReadablesSortedByIdentity() {
		Readable [] readArray = reads.values().toArray(new Readable [reads.size()]);
		Arrays.sort(readArray,AlignedSequenceIdentityComparator.getAlignedSequenceIdentityComparator());
		return Arrays.asList(readArray);
	}
	
	/**
	 * Return a collection of this Strain's Readables, sorted by length
	 */
	public List<Readable> getReadablesSortedByLength() {
		Readable [] readArray = reads.values().toArray(new Readable [reads.size()]);
		Arrays.sort(readArray,AlignedSequenceLengthComparator.getAlignedSequenceLengthComparator());
		return Arrays.asList(readArray);
	}

	/* (non-Javadoc)
	 * @see amd.strainer.objects.AlignedSequence#initializeGraphics()
	 */
	public void initializeGraphics() {
		setDisplayGeometry(new StrainDisplayGeometry(this));
	}
	
//	// map of strain:sequencesegments
//	public HashMap links = new HashMap();
//
	
	/**
	 * If no specific name has been set for the strain, return its ID number.
	 * @see amd.strainer.objects.Sequence#getName()
	 */
	public String getName() {
		String name = super.getName();
		if (name==null) {
			return "Strain" + getIdInteger().toString();
		} 
		return name;
	}

	/**
	 * add a Read to the Strain. Check to see if its MatePair is already in the Strain.
	 * @param pReadId the Integer ID of the Read
	 * @param pRead the Read object
	 */
	public void putRead(Integer pReadId, Read pRead) {
		// remove from old strain if necessary
		if (stealReads) {
			if (pRead.getStrain()!=null) {
				pRead.getStrain().removeRead(pRead);
			}	
			pRead.setStrain(this);
		}

		// add to this strain
		Object ret = null;

		if (pRead.getMatePair()==null || pRead.isBadClone()) {
			// if it has no mate pair (or it's too far away), just add it solo
			ret = reads.put(pReadId,pRead);
		} else {
			// look for matepair (first solo then as Clone object)
			Integer matePairId = pRead.getMatePair().getIdInteger();
			Integer cloneId = pRead.getClone().getIdInteger();
			Read matePair = (Read) reads.remove(matePairId);
			if (matePair!=null) {
				// put clone object into strain (in place of removed MAte Pair)
				pRead.setInClone(true);
				matePair.setInClone(true);
				ret = reads.put(cloneId,pRead.getClone());
				if (stealReads) {
//					pRead.getClone().strain = this;
					pRead.getClone().setStrain(this);
				}
			} else {
				// look for Clone Object
				ret = reads.get(cloneId);
				if (ret==null) {
					// only add read if it's not already here
					ret = reads.put(pReadId,pRead);
				}
			}
		}
		if (ret==null) {
			// only increment if there wasn't already a read with this id
			size++;
		}
	}
	
	/**
	 * Add a Clone (a Read and its matePair) to the Strain.
	 * @param pCloneId the Integer ID of the Clone
	 * @param pClone the Clone object
	 */
	public void putClone(Integer pCloneId, Clone pClone) {
		if (stealReads) {
			// remove from old strain if necessary
			if (pClone.getStrain()!=null) {
				pClone.getStrain().removeClone(pClone);
			}
			pClone.setStrain(this);
		}
			
		// make sure reads are not already in strain
		removeRead(pClone.reads[0]);
		removeRead(pClone.reads[1]);
		
		// add mate pair
		Object ret = reads.put(pCloneId,pClone);

		// don't increment if we replaced one  (that should never happen, but just in case...)
		if (ret==null) {
			size = size+2;
		}
	}
	
	/**
	 * Add a Readable to the Strain. This checks the argument type and calls either addRead or add Clone
	 * @param pId the Integer ID f the Readable
	 * @param pR the Readable object
	 */
	public void putReadable(Integer pId, Readable pR) {
		if (pR instanceof Read) {
			putRead(pId, (Read) pR);
		} else {
			putClone(pId, (Clone) pR);
		}
	}
	
	/**
	 * Checks to see if the Read is in this strain
	 * @param pRead
	 * @return true if the Read or it's Clone is in this Strain
	 */
	public boolean containsRead(Read pRead) {
		if (reads.containsKey(pRead.getIdInteger())) {
			// if it's in the Strain as a Read, we're done
			return true;
		}

		// otherwise, check if it has a mate pair
		if (pRead.getMatePair()!=null) {
			// if it does...
			// look for Clone containing this read
			return reads.containsKey(pRead.getClone().getIdInteger());
		}
		
		// otehrwise, it's not here
		return false;
	}
	
	/**
	 * Remove a Read from the Strain. If the Read is part of a Clone in the Strain, the Clone is removed and the
	 * Read's matePair is added back by itself.<p>
	 * If stealReads is true, then the Read's strain variable is set to null.
	 * 
	 * @param pRead the Read to be removed
	 * @return true if the Read was in this Strain
	 */
	public boolean removeRead(Read pRead) {
		Object r = reads.remove(pRead.getIdInteger());
		if (r==null) {
			// check if it has a mate pair
			if (pRead.getMatePair()!=null) {
				// if it does...
				// first look for Clone containing this read
				r = reads.remove(pRead.getClone().getIdInteger());
				if (r!=null) {
					//System.out.println("   found MP, breaking up");
					if (stealReads) {
						((Clone)r).reads[0].setInClone(false);
						((Clone)r).reads[1].setInClone(false);
						((Clone)r).setStrain(null); // this removes both reads from strain...
						//...must add matepair back
						pRead.getMatePair().setStrain(this);
					}
					
					// put matePair(the other read) back in hash map by itself
					try {
						reads.put(pRead.getMatePair().getIdInteger(),pRead.getMatePair());
					} catch (NullPointerException e) {
						System.out.println(r);
						System.out.println(pRead);
						System.out.println(pRead.getMatePair());
					}
					size--;
					
					return true;
				} else {
					//System.out.println("Read " + pRead.getId() + "->" + pRead.getName() + " with matePair (" + pRead.matePairId + ") not in " + this.toString());
					return false;
				}
			} else {
				//System.out.println("Read " + pRead.getId() + "->" + pRead.getName() + " not in " + this.toString());
				return false;
			}
		} else {
			//System.out.println("   solo read");
			if (stealReads) {
				pRead.setStrain(null);
			}
			size--;
			return true;
		}
	}
	
	/**
	 * Remove a Clone from this Strain. If stealReads is true, then the Clone's strain variable is set to null
	 * 
	 * @param pClone the Clone to be removed
	 * @return true if the Clone was in the Strain.
	 */
	public boolean removeClone(Clone pClone) {
		Object clone = reads.remove(pClone.getIdInteger());
		if (clone==null) {
			return false;
		} else {
			if (stealReads) {
				pClone.setStrain(null);
//				pClone.strain=null;
//				pClone.reads[0].setStrain(null);
//				pClone.reads[1].setStrain(null);
			}
			size=size-2;
			return true;
		}
	}
	
	/**
	 * Removes the given Readable from this Strain.
	 * @param pR the Readable to be removed
	 * @return true if the Readable exists in this Strain
	 */
	public boolean removeReadable(Readable pR) {
		if (pR instanceof Read) {
			return removeRead((Read) pR);
		} else {
			return removeClone((Clone) pR);
		}
	}	
	
	/**
	 * Inspects the contents of the Strain and resets its Alignment based on the Reads it contains.
	 */
	public void setAlignmentFromReads() {
		Iterator<Readable> rit = reads.values().iterator();

		int start = 0;
		int end = 0;
		Sequence s = null;
		
		if (rit.hasNext()) {
			Readable read = rit.next();
			s = read.getAlignment().getSequenceSegment1().getSequence();
			start = read.getStart();
			end = read.getEnd();
		} else {
			setAlignment(null);
			return;
		}

		// loop over reads
		while (rit.hasNext()) {
			// get next read
			Readable read = rit.next();
			
			// adjust start if necessary
			if (read.getStart()<start) {
				start = read.getStart();
			}
			//adjust end if necessary
			if (read.getEnd()>end) {
				end = read.getEnd();
			}
		}
//		if (getAlignment()==null || !(getAlignment() instanceof StrainAlignment)) {
			setAlignment(new StrainAlignment(new SequenceSegment(s,start,end),new SequenceSegment(this,1,end-start+1),this));
//		} else {
//			getAlignment().getSequenceSegment1().setStart(start);
//			getAlignment().getSequenceSegment1().setEnd(end);
//			getAlignment().getSequenceSegment1().setSequence(s);
//			getAlignment().getSequenceSegment2().setEnd(end-start+1);
//		}
//		getDisplayGeometry().setParent(this);
	
		if (getStart()>getEnd()) System.out.println("Strain span is illegal: " + this.detailsString());
//		setLength(getEnd()-getStart());
	}
	
	public String detailsString () {
		return toString();
	}
	
	public String toString() {
		StringBuffer ret =new StringBuffer("Strain ");
		ret.append(getId()).append("::[").append(getSize()).append(" reads] (");
		ret.append(getStart()).append("-").append(getEnd()).append(")");
		return ret.toString();
	}
	
	public Object clone() {
		Strain c = (Strain) super.clone();
		c.reads = new HashMap<Integer,Readable>();
		c.reads.putAll(reads);
		c.size=getSize();
		c.stealReads = stealReads;
		return c;
	}
	
	/**
	 * Sort by alignment length
	 * @param pStrains A Collection of aligned sequences.
	 * @return List
	 */
	public static List<Strain> sortStrainsByLength(Collection<Strain> pStrains) {
		Strain [] strains = pStrains.toArray(new Strain [pStrains.size()]);
		Arrays.sort(strains, AlignedSequenceLengthComparator.getAlignedSequenceLengthComparator());
		return Arrays.asList(strains);
	}
	
	/**
	 * Sort by similarity to reference sequence
	 * @param pStrains A Collection<Strain>.
	 * @return List<Strain> sorted by identity
	 */
	public static List<Strain> sortStrainsByIdentity(Collection<Strain> pStrains) {
		Strain [] strains = pStrains.toArray(new Strain [pStrains.size()]);
		Arrays.sort(strains,AlignedSequenceIdentityComparator.getAlignedSequenceIdentityComparator());
		return Arrays.asList(strains);
	}
	
	/**
	 * Sorts Strains by number of reads (Strain.getSize())
	 * @param pStrains a Collection of Strains to be sorted
	 * @return a sorted List of strains
	 */
	public static List<Strain> sortStrainsBySize(Collection<Strain> pStrains) {
		Strain [] strains = pStrains.toArray(new Strain[pStrains.size()]);
		Arrays.sort(strains,StrainSizeComparator.getStrainSizeComparator());
		return Arrays.asList(strains);
	}
	
	/**
	 * Selects strain.  In particular is sets the selected flag to true.  Sets this
	 * object to data.selectedObject.  Adds all contained reads to prev selection list.
	 * Updates colors.
	 */
	public void select(DisplayData pData) {
		selected = true;
		pData.selectedObject = this;
		recalcColors();
		
		// loop over all reads and add to selected list
		for (Readable read : reads.values()) {
			read.addToSelectedList(pData);
		}	
	}
	
	/**
	 * Deselects a strain.  This merely changes the state and colors.
	 */
	public void deselect(DisplayData pData) {
		selected = false;
		recalcColors();
	}
	
	/**
	 * Add all contained reads to DisplayData.prevSelectedReads.
	 */
	public void addToSelectedList(DisplayData pData) {
		for(Readable read : reads.values()) {
			read.addToSelectedList(pData);
		}
	}
	
	/** 
	 * For each contained read, calls the read's version of this method
	 *  which removes the read from the prevSelected read list in DsiplayData
	 *  and calls the color setting method of the read
	 */
	public void removeFromSelectedList(DisplayData pData) {
		for(Readable read : reads.values()) {
			read.removeFromSelectedList(pData);
		}
	}
	
	public void close() {
		reads.clear();
		if (getAlignment()!=null) {
			getAlignment().setDiffs(null);
			setAlignment(null);
		}
	}
	
	/**
	 * Returns the sequence of this strain.  Same as getBases(), but may be cropped.  
	 *  If cropping region is beyond the edges of the strain, the strain boundary is used.
	 * @param pFillFromConsensus True: use reference to fill gaps, 'n' otherwise
	 * @param pCrop true: clip output to pStart and pEnd
	 * @param pStart position of first base (relative to reference sequence)
	 * @param pEnd position of last base (relative to reference sequence)
	 * @return DNA sequence of this strain
	 */
	public String getSequence(boolean pFillFromConsensus, boolean pCrop, int pStart, int pEnd) {
		String sequence = getBases(pFillFromConsensus);
		if (pCrop) {
			int start = Math.max(pStart - getStart(),0);
			int end = Math.min(pEnd - getStart(), getLength() - 1);
			return sequence.substring(start,end);
		}
		return sequence;
	}
		
	/**
	 * Returns list (separated by "\n") of read names in strain as a String
	 * @return String containing read names separated by newlines
	 */
	public String printList(boolean pFindMatePairs) {
		// buffer for output (guess the probable size)
		StringBuffer ret = new StringBuffer(20+4*size);
		
		// store matepair names if pFindMatePairs is true
		HashSet<String> names = new HashSet<String>();

		// loop over reads
		Iterator<Read> it = getReadIterator();
		while (it.hasNext()) {
			Read read = it.next();
			// print one read name per line
			ret.append(read.getName());
			ret.append("\n");
		
			// keep track of mate pair names of listed reads
			//  (remove from set if they get added to list)
			if (pFindMatePairs) {
				if (read.getMatePair()!=null) {
					if (!names.remove(read.getName())) {
						names.add(read.getMatePair().getName());
					}
				}	
			}
		}

		// names has list of matepairs not included in list
		if (pFindMatePairs) {
			for (String name : names) {
				ret.append(name);
				ret.append("\n");
			}
		}
		
		return ret.toString();
	}
	
	public HashSet<Readable> findDisconnectedReadsAfterPosition(int pPos, 
			boolean debug) {
		if (debug) System.out.println("Strain: " + getId());
		
		// sort reads by start position
		Readable [] readArray = reads.values().toArray(new Readable[reads.size()]);
		Arrays.sort(readArray,ReadStartComparator.getReadStartComparator());
		List<Readable> readList = Arrays.asList(readArray);
		
		if (debug) System.out.println("Has " + readList.size() + " reads");
		
		HashSet<Readable> firstGroup = new HashSet<Readable>();
		int maxEnd = 0;
		for (Readable read : readList) {
			if (debug) System.out.println("Read: " + read + ":" + read.getStart() + ":" + read.getEnd());
			
			if (maxEnd==0) {
				if (read.getStart() <= pPos) {
					if (read.getEnd() < pPos) {
						// skip reads that don't intersect this position
						if (debug) System.out.println("skip");
						continue;
					} 
				}
				maxEnd = read.getEnd();
				firstGroup.add(read);
				if (debug) System.out.println("start");
				
				continue;
			}
			
			if (read.getStart() <= maxEnd) {
				if (debug) System.out.println("keep");
				firstGroup.add(read);
				if (read.getEnd() > maxEnd) {
					if (debug) System.out.println("adjust");
					maxEnd = read.getEnd();
				}
			} else {
				// there is a gap here.  Return what we have
				if (debug) System.out.println("gap");
				return firstGroup;
			}
		}
		
		// we will only get here if there are no reads
		//  or if we ran out of reads before finding a gap
		if (debug) System.out.println("no gaps");
		return null;
	}	
	
	/**
	 * Given a Collection of Readables, iterates over Reads, separating any Clones on the way
	 * @author jmeppley
	 */
	private class ReadIterator implements Iterator<Read> {
		private Iterator<Readable> rit = null;
		private Read extraRead = null;
		
		ReadIterator(Iterator<Readable> pRit) {
//			System.out.println("Starin.ReadIterator: new read iterator using " + pRit);
			rit = pRit;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		public boolean hasNext() {
//			System.out.println("Starin.ReadIterator: extra: " + (extraRead!=null) + " next: " + rit.hasNext());
			return ((extraRead!=null) || rit.hasNext());
		}
		
		public Read next() {
			if (extraRead!=null) {
				Read temp = extraRead;
				extraRead = null;
//				System.out.println("Starin.ReadIterator: returning extra read");
				return temp;
			}

			if (rit.hasNext()) {
				Readable r = rit.next();
				if (r instanceof Clone) {
					Clone clone = (Clone) r;
					extraRead = clone.reads[1];
//					System.out.println("Starin.ReadIterator: returning half a clone");
					return clone.reads[0];
				} else {
//					System.out.println("Starin.ReadIterator: returning just one read");
					return (Read) r;
				}
			} else {
				throw new NoSuchElementException();
			}
		}
	}

	/**
	 * Add all reads from the given Strain to this strain
	 * @param strain
	 */
	public void putAllReads(Strain strain) {
		for (Readable readable : strain.reads.values()) {
			putReadable(readable.getIdInteger(),readable);
		}
	}
	
	private boolean open = true;
	public boolean isOpen() {return open;}
	public void toggleOpen() {
		open = !open;
	}

}
