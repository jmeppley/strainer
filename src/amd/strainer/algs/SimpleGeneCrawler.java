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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import amd.strainer.display.actions.Task;
import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Alignment;
import amd.strainer.objects.Difference;
import amd.strainer.objects.Read;
import amd.strainer.objects.ReadEndComparator;
import amd.strainer.objects.ReadHolder;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * Finds all variants of the indicated gene which is passed in as a
 * SequenceSeqgment object.  In fact, this algorithm will work on any segment,
 * there is nothing special (for these purposes) about a gene.  The one
 * caveat is that the processing time can be prohibitively long for large 
 * sequence segents.
 * <p>
 * Variants are found by enumerating all the possible combinations of reads 
 * that span the given segment and determining which of these combinations
 * represent unique sequences.
 * 
 * @author J M Eppley
 */
public class SimpleGeneCrawler implements SegmentStrainer {
	// TODO:2 test on quality data
	
	/**
	 * Sub-sequence to be processed to find variants
	 */
	private SequenceSegment segment = null;
	/**
     * Define the reference sequence and range to process using a SequenceSegment object
     * @param pSegment A SequenceSegment
     */
	public void setSegment(SequenceSegment pSegment) { segment = pSegment; }
    /**
     * @return The SequenceSegment (to be) processed
     */
	public SequenceSegment getSegment() {return segment;}

	private Iterator<Read> mReadIterator = null;
	public void setReads(Iterator<Read> pReads) {
		mReadIterator = pReads;
	}

	// tell GUI about the algorithm
	public String getName() {
		return "Original Gene Crawler";
	}
	public String getDescription() {
		return "Builds every possible chain of reads across segment and finds all unique sequences.";
	}
	
	// tell GUI what the options are for this algorithm
	private HashMap<String,Object> mOptionsHash = null;
	public HashMap<String,Object> getOptionsHash() {
		if (mOptionsHash==null) {
			mOptionsHash = new HashMap<String,Object>();
			mOptionsHash.put(MINIMUM_OVERLAP,new Integer(mMinimumOverlap));
			mOptionsHash.put(MAXIMUM_DIFF,new Double(mMaximumDiff));
			mOptionsHash.put(Config.FILL_FROM_COMPOSITE,Boolean.FALSE);
			mOptionsHash.put(Config.CONVERT_TO_AA,Boolean.TRUE);
		}
		return mOptionsHash;
	}
	
	/**
	 * Reads in settings from the singleton Settings object in amd.strainer.algs.Config
	 */
	private void readSettings() {
		// Get the settings hash map
		HashMap<String,Object> settings =  Config.getConfig().getSettings();  
		if (settings==null) {
			return;
		}
	
		// keep track of all reads contributing to new strains (CombineStrains=true) 
		//  or discard reads once it's determined that they don't define a new 
		//  variant (CombineeStrains=false)
		Object value = settings.get(Config.KEEP_ALL_READS);
		if (value!=null) {
			setKeepAllReads(value);
		} else {
			settings.put(Config.KEEP_ALL_READS,new Boolean(mKeepAllReads));
		}

		// ignore regions outside of segment when comparing reads (or not)
		value = settings.get(Config.RESTRICT_TO_SEGMENT);
		if (value!=null) {
			setRestrictMatchesToSegment(value);
		} else {
			settings.put(Config.RESTRICT_TO_SEGMENT,new Boolean(mRestrictMatchesToSegment));
		}
		
		
		// how much do reads have to overlap by?
		value = settings.get(MINIMUM_OVERLAP);
		if (value!=null) {
			setMinimumOverlap(value);
		} else {
			settings.put(MINIMUM_OVERLAP,new Integer(mMinimumOverlap));
		}

		// how much difference between overlapping reads will be tolerated?
		value = settings.get(MAXIMUM_DIFF);
		if (value!=null) {
			setMaximumDiff(value);
		} else {
			settings.put(MAXIMUM_DIFF,new Double(mMaximumDiff));
		}
	}

	/**
	 * Constant used to indicate the minimumOverlap option in the settings hash map
	 */
	public static final String MINIMUM_OVERLAP = "Minimum number of overlapping bases";
	/**
	 * Constant used to indicate the maximumDiff option in the settings hash map
	 */
	public static final String MAXIMUM_DIFF = "Maximum fraction of SNPs between reads";

	private boolean mRestrictMatchesToSegment = true;
	/**
	 * override the restrictMatchesToSegment setting
	 * @param mRestrictMatchesToSegment if true, differences outside of the gene or segment are ignored when comparing reads.
	 */
	private void setRestrictMatchesToSegment(Object pValue) {
		this.mRestrictMatchesToSegment = Boolean.parseBoolean(pValue.toString());
	}
	private boolean mKeepAllReads = false;
	/**
	 * Override the keepAllReads setting.  
	 * @param pValue  If true, all reads will be remembered when building variants.
	 */
	private void setKeepAllReads(Object pValue) {
		mKeepAllReads = Boolean.parseBoolean(pValue.toString());
	}
	
	private int mMinimumOverlap = 80;
	private void setMinimumOverlap(Object pValue) {
		mMinimumOverlap = Integer.parseInt(pValue.toString());
	}
	
	double mMaximumDiff = 0.01;
	private void setMaximumDiff(Object pValue) {
		mMaximumDiff = Double.parseDouble(pValue.toString());
	}
	
	/**
	 * Variable to hold final result
	 */
	private DefaultStrainerResult result = null;
	
	private static final int ASC = ReadEndComparator.ASC;
	private static final int DESC = ReadEndComparator.DESC;
	
	// constructor
	/**
	 * Creates instance of SimpleGeneCrawler for processing the given segment
	 */
	public SimpleGeneCrawler () {
		// read settings from amd.strainer.algs.Config
		readSettings();
	}

	/**
	 * Return the results object
	 */
	public StrainerResult getStrains() throws SegmentStrainerException {
		if (result==null) {
			findStrains();
		}
		return result;
	}
	
	/**
	 * Generate the results.
	 */
	private void findStrains() throws SegmentStrainerException {
		// Get list of reads that overlap this segment
		// also get list of reads that intersect the start pos
		List<GCRead> reads = new ArrayList<GCRead>();

		if (mReadIterator==null) {
			// if no list of reads given...
			// assume base sequence is an ReferenceSequence object (or at least implements ReadHolder)
			mReadIterator = ((ReadHolder) segment.getSequence()).getReadIterator();
		}
		
		while (mReadIterator.hasNext()) {
			Read read = mReadIterator.next();
			
			// check read starts and ends against gene
			if (read.intersects(segment)) {
				GCRead r = new GCRead(read);
				
				if (intersectsEndOfSegment(read)) {
					r.intersectsEndOfSegment = true;
				}
				// find all reads intersecting this read on the right
				findIntersectionsOnRight(r,reads);
				
				// add this read to list
				reads.add(r);
			}
		}
		
		// sort intersection lists by endPos
		sortIntersectionListsByEndPos(reads);
		reads = sortByEndPos(reads,ASC);
		
		int count = -1;
		if (mTask!=null) {
			mTask.setLengthOfTask(reads.size());
		}
		
		// for each read build all possible links between ends of gene
		Set<Strain> strains = new HashSet<Strain>();
		
		for (GCRead read : reads) {
			if (mTask!=null) {
				count++;
				mTask.setCurrent(count);
			}

			if (read.isUsed()) {
				continue;
			}
			
//			((ReadDisplayGeometry)read.parent.getDisplayGeometry()).setFill(Color.cyan);
			
			Strain strain = new Strain();
			strain.stealReads = false;
			
			addFirstReadToStrain(strain,read);
			
			if (read.intersectsEndOfSegment) {
				read.setUsed(true);
				addStrainIfUnique(strain,strains);
			} else {
				extendStrainRight(strain,read,null,strains);
			}
		}
		
		if (mTask!=null) {
			mTask.setCurrent(reads.size());
		}

		// save results to publicly accessible variable
		result = new DefaultStrainerResult(getSegment(),strains);
	}
	
	private void findIntersectionsOnRight(GCRead pRead, Collection<GCRead> pReads) {
		for (GCRead read : pReads) {
			if (intersectsEndOfAlignedSequence(pRead, read)) {
				if (compareAlignedSequences(read, pRead)) {
					read.intersections.add(pRead);
				}
			} else if (intersectsEndOfAlignedSequence(read, pRead)) {
				if (compareAlignedSequences(pRead, read)) {
					pRead.intersections.add(read);
				}
			}
		}
	}
	
	private void sortIntersectionListsByEndPos(Collection<GCRead> pReads) {
		for (GCRead read : pReads) {
			read.intersections = sortByEndPos(read.intersections,DESC);
		}
	}
	
	private List<GCRead> sortByEndPos(List<GCRead> pReads, int pDir) {
		GCRead [] reads = pReads.toArray(new GCRead[pReads.size()]);
		Arrays.sort(reads,ReadEndComparator.getReadEndComparator(pDir));
		return Arrays.asList(reads);
	}
	
	private void addFirstReadToStrain(Strain pStrain, Read pRead) {
		pStrain.putRead(pRead.getIdInteger(),pRead);
		ArrayList<Difference> diffs = new ArrayList<Difference>();
		if (mRestrictMatchesToSegment) {
			//clip diffs to inside segment if mRestrictMAtches is true
			List<Difference> readDiffs = pRead.getAlignment().getDiffs();
			int i = 0;
			
			// skip to segment
			while (i < readDiffs.size()) {
				if (readDiffs.get(i).getPosition1()>=segment.getStart()) {
					break;
				}
				i++;
			}

			// gett diffs in segment
			while (i < readDiffs.size()) {
				if (readDiffs.get(i).getPosition1()>=segment.getEnd()) {
					break;
				}
				diffs.add(readDiffs.get(i));
				i++;
			}
		} else {
			diffs.addAll(pRead.getAlignment().getDiffs());
		}
		
		// create strain alignment from copied read diffs
		SequenceSegment ss1 = (SequenceSegment) pRead.getAlignment().getSequenceSegment1().clone();
		SequenceSegment ss2 = new SequenceSegment(pStrain,1,ss1.getLength());
		pStrain.setAlignment(new Alignment(ss1,ss2,true,diffs));
	}
	
	private boolean intersectsEndOfSegment(Read pRead) {
		return (pRead.getStart()<=segment.getEnd() && 
				pRead.getEnd()>=segment.getEnd());
	}
	
	/**
	 * Check if strain is sufficieantly different to be a new strain.  If so, add it to 
	 * collection, otherwise, add its reads to the train it matches
	 * 
	 * TODO:3 Do we need to think about how this is done when some errors are allowed?
	 * Does every read in the group need to be less than the cutoff from every other?
	 * Or is it good enough to just merge groups of reads if the consensuses (consensi?)
	 * are within the cutoff?
	 * 
	 * @param pStrain new strain to test
	 * @param pStrains list of established strains
	 */
	private void addStrainIfUnique(Strain pStrain, Collection<Strain> pStrains) {
		//System.out.println("Adding " + pStrain);
		for (Strain strain : pStrains) {
			if (compareAlignedSequences(strain,pStrain)) {
				// return without adding to set
				//System.out.println("  Same as " + strain);
				
				// if keepAllReads is true, add to reads matching strain
				if (mKeepAllReads) {
					// this is going to mess up incomplete strains...
					addReadsToStrain(strain,pStrain);
					// don't need to close strain (its done in addReadsToStrain)
				}
				
				return;
			}
		}
		// if we're still here, add to set
		pStrains.add(pStrain);
		setStrainForReads(pStrain,pStrain.getReadIterator());
	}
	
	private void addReadsToStrain(Strain pStrain, Strain pStrain2) {
		Iterator<Read> rit = pStrain2.getReadIterator();
		while (rit.hasNext()) {
			GCRead read = (GCRead) rit.next();

			// add read to new strain
			pStrain.putRead(read.getIdInteger(), read);
			// add new strain to read's list
			read.strains.add(pStrain);
			// remove old strain from read's list
			read.strains.remove(pStrain2);
		}
		pStrain2.close();
	}
	
	private void addReadToStrain(Strain pStrain, GCRead pRead) {
		pStrain.putRead(pRead.getIdInteger(),pRead);
//		pStrain.reads.put(new Integer(pRead.getId()),pRead);
//		pStrain.size++;
		pRead.strains.add(pStrain);
	}
	
	private void setStrainForReads(Strain pStrain, Iterator<Read> pReads) {
		while (pReads.hasNext()) {
			GCRead read = (GCRead) pReads.next();
			read.strains.add(pStrain);
		}
	}
	
	private void extendStrainRight(Strain pStrain, GCRead pRead, GCRead pPrevRead, 
			Set<Strain> pStrains) {
		// pReads should be a prescreened list of reads
		//  that intersect this strain on the right
		//System.out.println("Extending right " + pStrain.reads.size());
		
		pRead.setUsed(true);
		
		// get list of reads used by pPrevRead that are intersections of pRead
		HashSet<GCRead> troublemakers = null;
		if (pPrevRead != null) {
			troublemakers = new HashSet<GCRead>();
			for (GCRead read : pPrevRead.alreadyTriedReads) {
				if (pRead.intersections.contains(read)) {
					troublemakers.add(read);
				}
			}
		}
		
		boolean noExtensions = true;
		for (GCRead read : pRead.intersections) {
			//System.out.println("Trying " + read + ":" + read.dontUse());
			
			if (read.dontUse()) {
				// read has already been used to build strains off of pPrevRead 
				//  Therefore, pRead belongs in any strain containing read and pPrevRead
				for (Strain strain : read.strains) {
					if (strain.containsRead(pPrevRead)) {
						addReadToStrain(strain,pRead);
					}
				}
			} else {
				if (pPrevRead!=null && 
						read.getStart()<=pPrevRead.getEnd()) {
					// if this happens, then this read disagrees with a
					// read already in the strain
					// (if it overlaps prevRead, it would have been used if it matches)
					
					// don't use this read any more for this strain
					read.setDontUse(true);
					// but when we are done here, reset used to false
					pRead.differingReads.add(read);
					
					// move on to next read
					continue;
				}
				
				noExtensions=false;
				
				// check troublemakers to see if any overlap read
				if (pPrevRead != null) {
					boolean skip = false;
					for (GCRead tread : troublemakers) {
						if (tread.intersections.contains(read)) {
							// in this case pRead and read span an already used
							//  read (tread) and any strains built from these
							//  will match some strain built from tread
							
							// move on to next read in intersections
							skip = true;
							continue;
						}
					}
					if (skip) {
						// don't use this read any more for this strain
						read.setDontUse(true);
						// but when we are done here, reset dontuse to false
						pRead.alreadyTriedReads.add(read);
						
						// move on to next read in intersections
						continue;
					}
				}
				
				// make a new strain for each read and extend each
				Strain newStrain = (Strain) pStrain.clone();
				addReadToRightOfStrain(newStrain,read);
				
				if (read.intersectsEndOfSegment) {
					// if we span the gene, stop
					//System.out.println("end of strain");
					addStrainIfUnique(newStrain,pStrains);
				} else {
					extendStrainRight(newStrain,read,pRead,pStrains);
				}
				
				// don't use this read any more for this strain
				read.setDontUse(true);
				// but when we are done here, reset used to false
				pRead.alreadyTriedReads.add(read);
			}
		}
		
		if (noExtensions) {
			// if we didn't use any reads, end this strain
			addStrainIfUnique(pStrain,pStrains);
		}
		
		if (pRead.alreadyTriedReads.size() > 0) {
			// if we used any reads, reset them
			Iterator<GCRead>rit = pRead.alreadyTriedReads.iterator();
			while (rit.hasNext()) {
				rit.next().setDontUse(false);
				rit.remove();
			}
		}
		
		if (pRead.differingReads.size() > 0) {
			// if we used any reads, reset them
			Iterator<GCRead> rit = pRead.differingReads.iterator();
			while (rit.hasNext()) {
				rit.next().setDontUse(false);
				rit.remove();
			}
		}
	}
	
	private boolean intersectsEndOfAlignedSequence(AlignedSequence p1, 
			AlignedSequence p2) {
		// use strict > (not >=) to prevent read from matching self
		return (p1.getStart()<=(p2.getEnd()-(mMinimumOverlap-1)) && 
				p1.getEnd()>p2.getEnd());
	}
	
	private boolean compareAlignedSequences(AlignedSequence p1, 
			AlignedSequence p2) {
		int ovStart = Math.max(p2.getStart(), p1.getStart());
		if (mRestrictMatchesToSegment) {
			ovStart = Math.max(segment.getStart(),ovStart);
		}
		int ovEnd = Math.min(p2.getEnd(), p1.getEnd());
		if (mRestrictMatchesToSegment) {
			ovEnd = Math.min(segment.getEnd(),ovEnd);
		}
		
		if (ovEnd<ovStart) {
			return false;
		}
		
		return Util.compareAlignedSequences(p1, p2, ovStart, ovEnd, mMaximumDiff);
	}
	
	private void addReadToRightOfStrain(Strain pStrain, Read pRead) {
		pStrain.putRead(pRead.getIdInteger(),pRead);

		// skip to new diffs
		int i = -1;
		Difference diff = null;
		List<Difference> diffs = pRead.getAlignment().getDiffs();
		while (true) {
			i++;
			if (i>=diffs.size()) {
				// we reached the last diff before getting into overlap
				break;
			}
			diff = diffs.get(i);
			if (diff.getPosition1() > pStrain.getEnd()) {
				// stop this llop and move to next phase when we find the end of
				//  the strain as it was before this read was added
				break;
			}
		}
		
		int strainEndPosInRead = pRead.getAlignment().getPosFromReference(pStrain.getEnd());
		
		// add clones of new diffs
		while (i < diffs.size()) {
			// this is redundant on the first iteration.  I should fix that
			diff = (Difference) diffs.get(i).clone();

			if (mRestrictMatchesToSegment && diff.getPosition1()>segment.getEnd()) {
				break;
			}
			
			diff.setPosition2(pStrain.getLength() + diff.getPosition2()-strainEndPosInRead);
			pStrain.getAlignment().getDiffs().add(diff);

			i++;
		}
		
		// update end
		pStrain.getAlignment().getSequenceSegment1().setEnd(pRead.getEnd());
		pStrain.getAlignment().getSequenceSegment2().setEnd(pStrain.getLength());
	}
	
	/**
	 * Wrapper for Read objects that has methods useful to SimpleGeneCrawler. 
	 * 
	 * @author jmeppley
	 */
	public class GCRead extends Read {
		
		public Read parent = null;
		public HashSet<Strain> strains = new HashSet<Strain>();
		
		public GCRead(Read r) {
			parent = r;
			setId(r.getId());
			setName(r.getName());
			setLength(r.getAlignment().getLength());
			setAlignment(r.getAlignment());
		}
		
		List<GCRead> intersections = new ArrayList<GCRead>();    
		HashSet<GCRead> alreadyTriedReads = new HashSet<GCRead>();
		HashSet<GCRead> differingReads = new HashSet<GCRead>();
		
		boolean intersectsEndOfSegment = false;
		
		private boolean used = false;
		public boolean isUsed() { return used; }
		public void setUsed(boolean pUsed) { used = pUsed; }
		
		private boolean dontUse = false;
		public boolean dontUse() { return dontUse; }
		public void setDontUse(boolean pDontUse) { dontUse = pDontUse; }
		
		public void close() {
			getAlignment().setDiffs(null);
			setStrain(null);
			setMatepair(null);
			setClone(null);
		}
	}
	
	private Task mTask = null;
	public void setTask(Task pTask) {
		mTask = pTask;
	}
}
