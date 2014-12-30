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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import amd.strainer.display.actions.Task;
import amd.strainer.objects.Read;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * Autostrains a given segment, but subdivides existing groupings.
 * 
 * @author jmeppley
 * @see amd.strainer.algs.NestedStrainerResult
 */
public class Substrainer implements SegmentStrainer {

	/**
	 * Sub-sequence to be processed to find variants
	 */
	private SequenceSegment segment = null;
	public void setSegment(SequenceSegment pSegment) { segment = pSegment; }
	public SequenceSegment getSegment() {return segment;}

	private Iterator<Read> mReadIterator = null;
	public void setReads(Iterator<Read> pReads) {
		mReadIterator = pReads;
	}

	private Task mTask = null;
	public void setTask(Task pTask) {
		mTask = pTask;	
	}

	private StrainerResult result = null;
	
	// constructor
	/**
	 * Creates instance of GeneCrawler for processing the given segment
	 */
	public Substrainer () {
	}

	// tell GUI about the algorithm
	public String getName() {
		return "Substrainer";
	}
	public String getDescription() {
		return "Runs the specified SegmentStrainer on each strain overlapping the segment.";
	}
	
	// tell GUI what the options are for this algorithm (also set default values here)
	private HashMap<String,Object> mOptionsHash = null;
	public HashMap<String,Object> getOptionsHash() {
		if (mOptionsHash==null) {
			mOptionsHash = new HashMap<String,Object>();
			mOptionsHash.put(Config.INTERNAL_SEGMENT_STRAINER,GeneCrawler.class);
			mOptionsHash.put(Config.FILL_FROM_COMPOSITE,Boolean.FALSE);
			mOptionsHash.put(Config.CONVERT_TO_AA,Boolean.TRUE);
		}
		return mOptionsHash;
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
		NestedStrainerResult allResults = new NestedStrainerResult();
		
		System.out.println("starting substrainer");
		// get internal strainer class
		Class ssClass = (Class) Config.getConfig().getSettings().get(Config.INTERNAL_SEGMENT_STRAINER);

		// get list of selected reads (null => all)
		List<Read> reads = null;
		if (mReadIterator!=null) {
			reads = new ArrayList<Read>();
			while (mReadIterator.hasNext()) {
				Read read = mReadIterator.next();
				reads.add(read);
			}
		}
		
		// get the ref seq
		ReferenceSequence refSeq = (ReferenceSequence) segment.getSequence();
		int count = 0;
		if (mTask != null) {
			mTask.setLengthOfTask(refSeq.strains.size());
		}
		
		// find strains intersecting this segment
		// assume base sequence is an ReferenceSequence object
		for (Strain strain : refSeq.strains.values()) {
			//System.out.println("Checking strain: " + strain.toString());
			
			// check read starts and ends against gene
			if (strain.intersects(segment)) {
				//System.out.println("region intesects: " + strain.toString());
				
				//create dummy segment with this strain as it's base sequence
				SequenceSegment sseg = new SequenceSegment(strain,segment.getStart(),segment.getEnd());

				// run segment strainer
				SegmentStrainer ss = null;
				try {
					ss = Config.getSegmentStrainer(ssClass);
//						(SegmentStrainer) ssClass.getConstructor(new Class [0]).newInstance(new Object [0]);
				} catch (InvocationTargetException ite) {
					throw new SegmentStrainerConfigurationException(ite.toString());
				} catch (InstantiationException ie) {
					throw new SegmentStrainerConfigurationException(ie.toString());
				} catch (IllegalAccessException iae) {
					throw new SegmentStrainerConfigurationException(iae.toString());
				} catch (NoSuchMethodException nsme) {
					throw new SegmentStrainerConfigurationException(nsme.toString());
				}
				ss.setSegment(sseg);
				if (reads==null) {
					// if no list of reads specified, use all in strain
					ss.setReads(strain.getReadIterator());
				} else {
					// otherwise make sure strain reads are in indicated list
					List<Read> strainerReads = new ArrayList<Read>();
					Iterator<Read> srit = strain.getReadIterator();
					while (srit.hasNext()) {
						Read read = srit.next();
						if (reads.contains(read)) {
							strainerReads.add(read);
						}
					}
					if (strainerReads.size()==0) {
						// no reads, skip

						if (mTask!=null) {
							count++;
							mTask.setCurrent(count);
						}

						continue;
					}
					ss.setReads(strainerReads.iterator());
				}

				// do straining
				StrainerResult sr = ss.getStrains();

//				System.out.println("Got " + sr.size() + " strains");
//				System.out.println(sr.toString());
				
				// combine results
				allResults.addResults(strain,sr);
				
			}

			if (mTask!=null) {
				count++;
				mTask.setCurrent(count);
			}
		}
		result = allResults;
//		System.out.println(result.toString());
	}
}
