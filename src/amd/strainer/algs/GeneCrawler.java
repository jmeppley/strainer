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

import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Alignment;
import amd.strainer.objects.Difference;
import amd.strainer.objects.Read;
import amd.strainer.objects.ReadEndComparator;
import amd.strainer.objects.ReadHolder;
import amd.strainer.objects.SequenceFragmentEndComparator;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * Attempts to find all variants of the indicated gene which is passed in as a
 * SequenceSeqgment object.  In fact, this algorithm will work on any segment,
 * there is nothing special (for these purposes) about a gene.  The one
 * caveat is that the processing time can be prohibitively long for large 
 * sequence segents.
 * <p>
 * Variants are found by enumerating all the possible combinations of overlapping, mtching reads 
 * that span the given segment and determining which of these combinations
 * represent unique sequences. If a unique pattern cannot be extened accross the Gene span,
 * it is extended using the closest match from the variants which do span the gene.
 * 
 * @author J M Eppley
 */
public class GeneCrawler extends AbstractSegmentStrainer implements SegmentStrainer{
	static final String ALGORITHM_NAME = "Expanded Gene Crawler";
	static final String ALGORITHM_DESCRIPTION = "Builds every possible chain of reads across segment and finds all unique sequences. Tries to extend incomplete strains.";

	// tell GUI about the algorithm
	public String getName() {
		return ALGORITHM_NAME;
	}
	public String getDescription() {
		return ALGORITHM_DESCRIPTION;
	}

	// tell GUI what the options are for this algorithm
	private HashMap<String,Object> mOptionsHash = null;
	public HashMap<String,Object> getOptionsHash() {
		if (mOptionsHash==null) {
			mOptionsHash = new HashMap<String,Object>();
			mOptionsHash.put(MINIMUM_OVERLAP,new Integer(mMinimumOverlap));
			mOptionsHash.put(COMPLETION_DIFF,new Double(mCompletionDiff));
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
		value = settings.get(COMPLETION_DIFF);
		if (value!=null) {
			setCompletionDiff(value);
		} else {
			settings.put(COMPLETION_DIFF,new Double(mCompletionDiff));
		}
	}

	/**
	 * Constant used to indicate the minimumOverlap option in the settings hash map
	 */
	public static final String MINIMUM_OVERLAP = "Minimum number of overlapping bases";
	/**
	 * Constant used to indicate the completionDiff option in the settings hash map
	 */
	public static final String COMPLETION_DIFF = "Maximum percent divergence of variants used to fill in gaps.";

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
	 * Change the keepAllReads setting.  
	 * @param pValue  If true, all reads will be remembered when building variants.
	 */
	private void setKeepAllReads(Object pValue) {
		mKeepAllReads = Boolean.parseBoolean(pValue.toString());
	}
	
	private int mMinimumOverlap = 80;
	private void setMinimumOverlap(Object pValue) {
		mMinimumOverlap = Integer.parseInt(pValue.toString());
	}
	
	double mCompletionDiff = 0.02;
	private void setCompletionDiff(Object pValue) {
		mCompletionDiff = Double.parseDouble(pValue.toString());
	}
	
	/**
	 * Variable to hold final result
	 */
	private DefaultStrainerResult result = null;
	
	private static final int ASC = ReadEndComparator.ASC;
	private static final int DESC = ReadEndComparator.DESC;
	
	// constructor
	/**
	 * Creates instance of GeneCrawler for processing the given segment
	 */
	public GeneCrawler () {
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
		//  also find overlaps within these reads
		List<GCRead> reads = getListOfIntersectingReads(mSegment,mReadIterator);
				
		// sort intersection lists by endPos
		sortIntersectionListsByEndPos(reads);
		// sort lis of reads by endPos
		reads = sortByEndPos(reads,ASC);
		
		// set up results
		Set<GCStrain> completeStrains = new HashSet<GCStrain>();
		Set<GCStrain> incompleteStrains = new HashSet<GCStrain>();

		int count = -1;
		if (mTask!=null) {
			mTask.setLengthOfTask(reads.size());
		}
		
		// for each read build all possible links between ends of gene
		//  use recursive calls to strain extending method
		for (GCRead read : reads) {
			if (mTask!=null) {
				count++;
				mTask.setCurrent(count);
			}
			
			if (read.isUsed()) {
				continue;
			}
			
			// create strain starting on this read
			GCStrain strain = new GCStrain();
			addFirstReadToStrain(strain,read);
			
			// check to see if it spans segment
			if (read.intersectsEndOfSegment) {
				read.setUsed(true);
				
				if (intersectsStartOfSegment(strain)) {
					addStrainIfUnique(strain,completeStrains);
				} else {
					incompleteStrains.add(strain);
				}
			} else {
				// this method will call itself recursively til it hits the end
				extendStrainRight(strain,read,null,completeStrains,incompleteStrains);
			}
		}

		if (mTask!=null) {
			mTask.setLengthOfTask(0);
		}
		
		// extend incomplete strains 
		extendIncompleteStrains(incompleteStrains,completeStrains);
		
		// save results to publicly accessible variable
		result = new DefaultStrainerResult(getSegment(),(Set)completeStrains);
	}
	
	private List<GCRead> getListOfIntersectingReads(SequenceSegment pSegment, Iterator<Read> pReadIterator) {
		List<GCRead> reads = new ArrayList<GCRead>();

		if (pReadIterator==null) {
			// if no list of reads given...
			// assume base sequence is an ReferenceSequence object (or at least implements ReadHolder)
			pReadIterator = ((ReadHolder) pSegment.getSequence()).getReadIterator();
		}

		while (pReadIterator.hasNext()) {
			Read read = pReadIterator.next();
			
			// check read starts and ends against gene
			if (read.intersects(pSegment)) {
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
		return reads;
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
				if (readDiffs.get(i).getPosition1()>=mSegment.getStart()) {
					break;
				}
				i++;
			}

			// gett diffs in segment
			while (i < readDiffs.size()) {
				if (readDiffs.get(i).getPosition1()>=mSegment.getEnd()) {
					break;
				}
				diffs.add(readDiffs.get(i));
				i++;
			}
		} else {
			diffs.addAll(pRead.getAlignment().getDiffs());
		}
		
		// create strain alignment from copied read diffs
		SequenceSegment ss = (SequenceSegment) pRead.getAlignment().getSequenceSegment1().clone();
		pStrain.setAlignment(new Alignment(ss,null,true,diffs));
	}

	private boolean intersectsEndOfSegment(Read pRead) {
		return (pRead.getStart()<=mSegment.getEnd() && 
				pRead.getEnd()>=mSegment.getEnd());
	}
	
	private boolean intersectsStartOfSegment(AlignedSequence pAS) {
		// we can just check the start, because the end has to be in the segment,
		//  if the sequence is being considered at this point
		return (pAS.getStart()<=mSegment.getStart());
	}

	/**
	 * Check if strain is sufficieantly different to be a new strain.  If so, add it to 
	 * collection, otherwise, add its reads to the train it matches
	 * 
	 * @param pStrain new strain to test
	 * @param pStrains list of established strains
	 */
	private void addStrainIfUnique(GCStrain pStrain, Collection<GCStrain> pStrains) {
		//System.out.println("Adding " + pStrain);
		for (GCStrain strain : pStrains) {
			if (compareAlignedSequences(strain,pStrain)) {
				// same, don't add to set
				
				// if keepAllReads is true, add to reads matching strain
				if (mKeepAllReads) {
					addReadsToStrain(strain,pStrain);
					// don't need to close strain (pStrain closed in addReadsToStrain)
				} else {
					pStrain.close();
				}
				
				return;
			}
		}
		// if we're still here, add to set
		pStrains.add(pStrain);
		setStrainForReads(pStrain,pStrain.getReadIterator());
	}
	
	private void addCompletedStrainIfUnique(GCStrain pCompletedStrain, Set<GCStrain> pCompletedStrains) {
		for (GCStrain strain : pCompletedStrains) {
			if (compareAlignedSequences(strain,pCompletedStrain)) {
				// same, don't add to set
				
				// if keepAllReads is true, add to reads matching strain
				if (mKeepAllReads) {
					addReadsToStrain(strain,pCompletedStrain);
					// don't need to close strain (done in addReadsToStrain)
				} else {
					pCompletedStrain.close();
				}
				
				return;
			}
		}
		
		// if we're still here, add to set
		pCompletedStrains.add(pCompletedStrain);
		setStrainForReads(pCompletedStrain,pCompletedStrain.getReadIterator());
	}

	private void addReadsToStrain(GCStrain pStrain, GCStrain pStrain2) {
		Iterator<Read> rit = pStrain2.getReadIterator();
		while (rit.hasNext()) {
			GCRead read = (GCRead) rit.next();

			// add read to new strain
//			if (
			pStrain.putRead(read.getIdInteger(), read);
//			==null) {
//				pStrain.size++;
//			}
			// add new strain to read's list
			read.strains.add(pStrain);
			// remove old strain from read's list
			read.strains.remove(pStrain2);
		}
		pStrain2.close();
	}
	
	private void addReadToStrain(GCStrain pStrain, GCRead pRead) {
		pStrain.putRead(pRead.getIdInteger(),pRead);
		pRead.strains.add(pStrain);
	}
	
	private void setStrainForReads(GCStrain pStrain, Iterator<Read> pReads) {
		while (pReads.hasNext()) {
			GCRead read = (GCRead) pReads.next();
			read.strains.add(pStrain);
		}
	}
	
	private void extendStrainRight(GCStrain pStrain, GCRead pRead, GCRead pPrevRead, 
			Set<GCStrain> pStrains, Set<GCStrain> pIncompleteStrains) {
		// since we are about to exted to the right from this read,
		//  it would be redundant to use it as a starting point for 
		//  future strains, so tag it as used
		pRead.setUsed(true);
		
		// get list of reads used by pPrevRead that are intersections of pRead
		//  we don't want to use these to extend this strain, because
		//  that would create an identical variant to one made in the previous iteration
		/*
		HashSet troublemakers = null;
		if (pPrevRead != null) {
			troublemakers = new HashSet();
			Iterator tmit = pPrevRead.alreadyTriedReads.iterator();
			while (tmit.hasNext()) {
				GCRead read = (GCRead) tmit.next();
				if (pRead.intersections.contains(read)) {
					troublemakers.add(read);
				}
			}
		}*/
		
		// keep track of if we are able to extend the strain at all
		boolean noExtensions = true;

		// loop over reads intersecting the last one added
		for (GCRead read : pRead.intersections) {
			//System.out.println("Trying " + read + ":" + read.dontUse());
			
			if (read.dontUse()) {
				// read has already been used to build strains off of pPrevRead 
				//  Therefore, pRead belongs in any strain containing read and pPrevRead
				for (GCStrain strain : read.strains) {
					if (strain.containsRead(pPrevRead)) {
						addReadToStrain(strain,pRead);
						// this counts as an extension
						noExtensions=false;
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
				/*
				if (pPrevRead != null) {
					Iterator tmit = troublemakers.iterator();
					boolean skip = false;
					while (tmit.hasNext()) {
						GCRead tread = (GCRead) tmit.next();
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
				}*/

				// TODO:4 this would probably use less memory if we didn't clone the strain and instead
				//  remove the read once the call to extend strain right returns
				//  (ie. There is only ever one strain and it's only cloned when one version of 
				//        it is added to the result set)
				
				// make a new strain for each read and extend each
				GCStrain newStrain = (GCStrain) pStrain.clone();
				addReadToRightOfStrain(newStrain,read);
				
				if (read.intersectsEndOfSegment) {
					// if we span the gene, stop
					if (intersectsStartOfSegment(newStrain)) {
						addStrainIfUnique(newStrain,pStrains);
					} else {
						pIncompleteStrains.add(newStrain);
					}
				} else {
					extendStrainRight(newStrain,read,pRead,pStrains,pIncompleteStrains);
				}
				
				// don't use this read any more for this strain
				read.setDontUse(true);
				// but when we are done here, reset used to false
				pRead.alreadyTriedReads.add(read);
			}
		}
		
		if (noExtensions) {
			// if we didn't use any reads, end this strain
			//			addStrainIfUnique(pStrain,pIncompleteStrains);
			pIncompleteStrains.add(pStrain);
			setStrainForReads(pStrain,pStrain.getReadIterator());		
		}
		
		if (pRead.alreadyTriedReads.size() > 0) {
			// if we used any reads, reset them
			Iterator<GCRead> rit = pRead.alreadyTriedReads.iterator();
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
			ovStart = Math.max(mSegment.getStart(),ovStart);
		}
		int ovEnd = Math.min(p2.getEnd(), p1.getEnd());
		if (mRestrictMatchesToSegment) {
			ovEnd = Math.min(mSegment.getEnd(),ovEnd);
		}
		
		if (ovEnd<ovStart) {
			return false;
		}
		
		return Util.compareAlignedSequences(p1, p2, ovStart, ovEnd, 0.0);
	}
	
	private void addReadToRightOfStrain(GCStrain pStrain, GCRead pRead) {
		// add read to straina nd update diffs
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
				break;
			}
		}
		
		int strainEndPosInRead = pRead.getAlignment().getPosFromReference(pStrain.getEnd());
		
		// add clones of new diffs
		while (i < diffs.size()){
			// this is redundant on the first iteration.  
			diff = (Difference) diffs.get(i).clone();

			if (mRestrictMatchesToSegment && diff.getPosition1()>mSegment.getEnd()) {
				break;
			}
			
			// update position2
			diff.setPosition2(pStrain.getLength() + diff.getPosition2()-strainEndPosInRead);
			pStrain.getAlignment().getDiffs().add(diff);
			i++;
		}
		
		// update end
		pStrain.getAlignment().getSequenceSegment1().setEnd(pRead.getEnd());

		// make sure strain alignment knows about new diffs
		pStrain.getAlignment().setDiffs(pStrain.getAlignment().getDiffs());

	}
	
	private void extendIncompleteStrains(Set<GCStrain> pIncompleteStrains, Set<GCStrain> pCompleteStrains) {
		Set<GCStrain> completedStrains = new HashSet<GCStrain>();
		
		// Extend right side of strains
		// Sort strains by end pos and loop
		GCStrain [] incStrains = pIncompleteStrains.toArray(new GCStrain [pIncompleteStrains.size()]);
		System.out.println(Arrays.deepToString(incStrains));
		Arrays.sort(incStrains,SequenceFragmentEndComparator.getSequenceFragmentEndComparator());
		for (int i = 0; i<incStrains.length; i++) {
			GCStrain incompleteStrain = incStrains[i];
			if (incompleteStrain.getEnd()>=mSegment.getEnd()) {
				// don't need to extend to the right
				addCompletedStrainIfUnique(incompleteStrain,completedStrains);
				continue;
			}
			
			// find likely completions
			Set<GCStrain> completions = findBestCompletions(incompleteStrain,pCompleteStrains,completedStrains);

			// store unique possibilities
			Iterator<GCStrain> cit = completions.iterator();
			while(cit.hasNext()) {
				GCStrain completion = cit.next();
				if (cit.hasNext()) {
					GCStrain newIncompleteStrain = (GCStrain)incompleteStrain.clone();
					newIncompleteStrain.setRightCompletion(completion,mSegment);
					addCompletedStrainIfUnique(newIncompleteStrain,completedStrains);
				} else {
					incompleteStrain.setRightCompletion(completion,mSegment);
					addCompletedStrainIfUnique(incompleteStrain,completedStrains);
				}
			}
		}

		// Extend left side of strains
		// Sort strains by end pos and loop
		incStrains = completedStrains.toArray(new GCStrain [0]);
		// the following try is for debugging
		try {
			Arrays.sort(incStrains,SequenceFragmentEndComparator.getSequenceFragmentEndComparator());
		} catch (NullPointerException npe) {
			System.out.println(Arrays.deepToString(incStrains));
			throw npe;
		}
		completedStrains.clear();
		for (int i = 0; i<incStrains.length; i++) {
			GCStrain incompleteStrain = incStrains[i];
			if (incompleteStrain.getStart()<=mSegment.getStart()) {
				// don't need to extend to the right
				addCompletedStrainIfUnique(incompleteStrain,completedStrains);
				continue;
			}
			
			// find likely completions
			Set<GCStrain> completions = findBestCompletions(incompleteStrain,pCompleteStrains,completedStrains);

			// store unique possibilities
			Iterator<GCStrain> cit = completions.iterator();
			while(cit.hasNext()) {
				GCStrain completion = cit.next();
				if (cit.hasNext()) {
					GCStrain newIncompleteStrain = (GCStrain)incompleteStrain.clone();
					newIncompleteStrain.setLeftCompletion(completion,mSegment);
					addCompletedStrainIfUnique(newIncompleteStrain,completedStrains);
				} else {
					incompleteStrain.setLeftCompletion(completion,mSegment);
					addCompletedStrainIfUnique(incompleteStrain,completedStrains);
				}
			}
		}

		// add completed strains to result set
		for (GCStrain strain : completedStrains) {
			pCompleteStrains.add(strain);
		}
	}

	private Set<GCStrain> findBestCompletions(GCStrain pIncompleteStrain, Set<GCStrain> pCompleteStrains, Set<GCStrain> pCompletedStrains) {
		GCStrain bestCompletion = null;
		double bestCompletionDiff = 1.0;
		Set<GCStrain> goodCompletions = new HashSet<GCStrain>();

		for (GCStrain completion : pCompleteStrains) {
			double completionDiff = getDiffBetweenAlignedSequences(pIncompleteStrain,completion);
			if (completionDiff<=mCompletionDiff) {
				goodCompletions.add(completion);
			}
			if (completionDiff<bestCompletionDiff) {
				bestCompletionDiff = completionDiff;
				bestCompletion = completion;
			}
		}
		
		for (GCStrain completion : pCompletedStrains) {
			double completionDiff = getDiffBetweenAlignedSequences(pIncompleteStrain,completion);
			if (completionDiff<=mCompletionDiff) {
				goodCompletions.add(completion);
			}
			if (completionDiff<bestCompletionDiff) {
				bestCompletionDiff = completionDiff;
				bestCompletion = completion;
			}
		}

		// use the best completion if there are none within tolerances
		if (goodCompletions.size()==0) {
			goodCompletions.add(bestCompletion);
		}
		
		return goodCompletions;
	}
	
	private double getDiffBetweenAlignedSequences(AlignedSequence p1, 
			AlignedSequence p2) {
		
		// calculate region of overlap
		int ovStart = Math.max(p2.getStart(), p1.getStart());
		if (mRestrictMatchesToSegment) {
			ovStart = Math.max(mSegment.getStart(),ovStart);
		}
		int ovEnd = Math.min(p2.getEnd(), p1.getEnd());
		if (mRestrictMatchesToSegment) {
			ovEnd = Math.min(mSegment.getEnd(),ovEnd);
		}
		
		if (ovEnd<ovStart) {
			// return imposslibly high number to indicate no overlap
			return 2;
		}

		// calculate diff in overlapping region
		return Util.getDiffBetweenAlignedSequences(p1, p2, ovStart, ovEnd);
	}

	/**
	 * wrapper for Strain objects that has methods and vars useful to GeneCrawler
	 * @author jmeppley
	 *
	 */
	public class GCStrain extends Strain {
		private GCStrain leftCompletion = null;
		private GCStrain rightCompletion = null;
		/**
		 * The strain that should be used to fill in gaps on the left of this strain
		 */
		public GCStrain getLeftCompletion() {
			return leftCompletion;
		}
		/**
		 * pCompletions the strain that should be used to fill in gaps
		 */
		public GCStrain getRightCompletion() {
			return rightCompletion;
		}
		/**
		 * @param pCompletions the strain that should be used to fill in gaps
		 */
		public void setLeftCompletion(GCStrain pCompletion, SequenceSegment pSegment) {
			leftCompletion = pCompletion;
			
			if (leftCompletion!=null && getStart() >= pSegment.getStart()) {
				// add diffs to left of strain
				extendAlignmentLeft(pSegment);
			}
		}
		
		/**
		 * @param pCompletions the strain that should be used to fill in gaps
		 */
		public void setRightCompletion(GCStrain pCompletion, SequenceSegment pSegment) {
			rightCompletion = pCompletion;
			
			if (rightCompletion!=null && getStart() >= pSegment.getStart()) {
				// add diffs to right of strain
				extendAlignmentRight(pSegment);
			}
		}

		
		private void extendAlignmentLeft(SequenceSegment pSegment) {
			// add diffs from completion to this strain's alignment
			
			// skip to diff in Segment
			int i = -1;
			Difference diff = null;
			List<Difference> diffs = leftCompletion.getAlignment().getDiffs();
			while (true) {
				i++;
				if (i>=diffs.size()) {
					// we reached the last diff from completion strain before getting into segment
					break;
				}
				// get next diff
				diff = diffs.get(i);
				if (diff.getPosition1() > pSegment.getStart()) {
					// this diff is in the segment
					break;
				}
			}
			
			List<Difference> newDiffs = new ArrayList<Difference>();
			int strainStartInComp = leftCompletion.getAlignment().getPosFromReference(getStart());
			
			// add clones of new diffs
			while (i < diffs.size()){
				// this is redundant on the first iteration.  I should fix that TODO:5
				diff = (Difference) diffs.get(i).clone();
				// make sure diff is in incomplete part of strain
				if (diff.getPosition1()<=getStart()) {
					newDiffs.add((Difference)diff.clone());
					i++;
				} else {
					// were done with the extension
					break;
				}
			}
			
			// update start
			getAlignment().getSequenceSegment1().setStart(pSegment.getEnd());
			
			// add old diffs to new.
			for (Difference d : getAlignment().getDiffs()) {
				d.setPosition2(d.getPosition2()+strainStartInComp-1);
				newDiffs.add(d);
			}
			getAlignment().setDiffs(newDiffs);
		}
		
		private void extendAlignmentRight(SequenceSegment pSegment) {
			// add diffs from completion to this strain's alignment
			
			// skip to new diffs
			int i = -1;
			Difference diff = null;
			List<Difference> diffs = rightCompletion.getAlignment().getDiffs();
			while (true) {
				i++;
				if (i>=diffs.size()) {
					// we reached the last diff from completion strain before getting into overlap
					break;
				}
				// get next diff
				diff = diffs.get(i);
				if (diff.getPosition1() > getEnd()) {
					// this diff is past the end of the current alignment
					break;
				}
			}
			
			int strainEndPosInComp = rightCompletion.getAlignment().getPosFromReference(getEnd());
			
			// add clones of new diffs
			while (i < diffs.size()){
				// this is redundant on the first iteration.  I should fix that
				diff = (Difference) diffs.get(i).clone();
				// update position2
				diff.setPosition2(getLength() + diff.getPosition2()-strainEndPosInComp);
				
				if (diff.getPosition1()<=pSegment.getEnd()) {
					getAlignment().getDiffs().add(diff);
					i++;
				} else {
					// we're done with the segment
					break;
				}
			}
			
			// update end
			getAlignment().getSequenceSegment1().setEnd(pSegment.getEnd());
			// make sure alignment knows about new diffs
			getAlignment().setDiffs(getAlignment().getDiffs());
		}
		
		public GCStrain() {
			// don't take reads from other strains
			stealReads = false;
		}

		/**
		 * Unlike for a normal AlignedSequence, this never calculates the sequence on the fly. It
		 * only returns the value set by setBases().
		 * 
		 * @see amd.strainer.objects.Sequence#getBases()
		 * @see amd.strainer.objects.AbstractAlignedSequence#getBases()
		 */
		public String getBases() {
			return bases;
		}
		
		public void close() {
			getAlignment().setDiffs(null);
			setAlignment(null);
			super.close();
		}

	}
	
	/**
	 * Wrapper for Read objects that has methods useful to GeneCrawler. 
	 * 
	 * @author jmeppley
	 */
	public class GCRead extends Read {
		
		public Read parent = null;
		public HashSet<GCStrain> strains = new HashSet<GCStrain>();
		
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
			setAlignment(null);
			setStrain(null);
			setMatepair(null);
			setClone(null);
		}
	}
}
