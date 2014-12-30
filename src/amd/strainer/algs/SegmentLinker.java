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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import amd.strainer.display.actions.Task;
import amd.strainer.objects.Read;
import amd.strainer.objects.ReadStartComparator;
import amd.strainer.objects.Readable;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * Used for very long segments. Splits the segment up into pieces and calls GeneCrawler for each piece. Splits can be done on number of reads
 * or number of bases. 
 * 
 * @author jmeppley
 *
 */
public class SegmentLinker implements SegmentStrainer {
	// segment to autostrin
	private SequenceSegment segment = null;
	public void setSegment(SequenceSegment pSegment) { 
		segment = pSegment; 
		referenceSequence = (ReferenceSequence) pSegment.getSequence();
	}
	public SequenceSegment getSegment() {return segment;}

	// reference sequence underlying the segment
	private ReferenceSequence referenceSequence = null;
	public ReferenceSequence getReferenceSequence() {return referenceSequence;}
	
	private Iterator<Read> mReadIterator = null;
	public void setReads(Iterator<Read> pReads) {
		mReadIterator = pReads;
	}
	
	// tell GUI about the algorithm
	public String getName() {
		return "Segment Linker";
	}
	public String getDescription() {
		return "Subdivides segment and runs GeneCrawler on the bits.";
	}
	
	// tell GUI what the options are for this algorithm
	private HashMap<String,Object> mOptionsHash = null;
	public HashMap<String,Object> getOptionsHash() {
		if (mOptionsHash==null) {
			mOptionsHash = new HashMap<String,Object>();
			mOptionsHash.put(Config.FILL_FROM_COMPOSITE,Boolean.FALSE);
			mOptionsHash.put(Config.CONVERT_TO_AA,Boolean.TRUE);
			mOptionsHash.put(Config.INTERNAL_SEGMENT_STRAINER,SimpleGeneCrawler.class);
			List<String> options = new ArrayList<String>();
			options.add(SEGMENT_BY_READ_COUNT);
			options.add(SEGMENT_BY_LENGTH);
			mOptionsHash.put(SEGMENT_METHOD,options);
			mOptionsHash.put(SEGMENT_SIZE,new Integer(mSegmentSize));
		}
		return mOptionsHash;
	}

	private void readSettings() {
		HashMap<String,Object> settings = Config.getConfig().getSettings();
		if (settings==null) {
			return;
		}
		
		try {
			setSegmentMethod(settings.get(SEGMENT_METHOD).toString());
		} catch (NullPointerException e) {
			settings.put(SEGMENT_METHOD,mSegmentMethod);
		}
		
		try {
			setSegmentSize(Integer.parseInt(settings.get(SEGMENT_SIZE).toString()));
		} catch (NullPointerException e) {
			settings.put(SEGMENT_SIZE,new Integer(mSegmentSize));
		}
		
		try {
			setSegmentStrainer((Class)settings.get(Config.INTERNAL_SEGMENT_STRAINER));
		} catch (ClassCastException e) {
			try {
				setSegmentStrainer(Class.forName(settings.get(Config.INTERNAL_SEGMENT_STRAINER).toString()));
			} catch (Exception ex) {
				settings.put(Config.INTERNAL_SEGMENT_STRAINER,mSegmentStrainer);
			}
		}
		
	}
	
	/**
	 * The parameter name for configuring how to split up segments
	 */
	public static final String SEGMENT_METHOD = "How to define segments";
	/**
	 * The parameter value for spliting up segments  every X number of reads
	 */
	public static final String SEGMENT_BY_READ_COUNT = "Number of reads";
	/**
	 * The parameter value for spliting up segments  every X number of bases
	 */
	public static final String SEGMENT_BY_LENGTH = "Number of bases";
	/**
	 * The parameter name for configuring how many reads or bases define the end of a sub-segment
	 */
	public static final String SEGMENT_SIZE = "Size of segments";
	
	private String mSegmentMethod = SEGMENT_BY_READ_COUNT;
	private String getSegmentMethod() { return mSegmentMethod; }
	private void setSegmentMethod(String pSM) { mSegmentMethod = pSM; }
	
	private int mSegmentSize = 40;
	private int getSegmentSize() { return mSegmentSize; }
	private void setSegmentSize(int pSS) { mSegmentSize = pSS; }

	private Class mSegmentStrainer = SimpleGeneCrawler.class;
	private Class getSegmentStrainer() { return mSegmentStrainer; }
	private void setSegmentStrainer(Class pAlg) { mSegmentStrainer = pAlg; }
	
	private StrainerResult result = null;
	
	// constructor
	public SegmentLinker () {
		readSettings();
	}

	/* (non-Javadoc)
	 * @see amd.strainer.algs.SegmentStrainer#getStrains()
	 */
	public StrainerResult getStrains() throws SegmentStrainerException {
		if (result==null) {
			findStrains();
		}
		return result;
	}
	
	private void findStrains() throws SegmentStrainerException {
		//////////////
		// override settings so nested calls work correctly
		Map<String,Object> settings = Config.getConfig().getSettings();
		Object oldKeepAllReads = settings.put(Config.KEEP_ALL_READS,Boolean.TRUE);
		Object oldRestrictToSegment = settings.put(Config.RESTRICT_TO_SEGMENT,Boolean.FALSE);
		
		//System.out.println("Starting SL");
		Collection<Read> reads;
		if (mReadIterator==null) {
			// get all reads if none are specified
			reads = getReferenceSequence().reads.values();
		} else {
			// get specified reads
			reads = new ArrayList<Read>();
			while (mReadIterator.hasNext()) {
				Read read = mReadIterator.next();
				reads.add(read);
			}
		}
		
		ArrayList<SequenceSegment> segments = getSegments(reads);
		
		if (mTask!=null) {
			mTask.setLengthOfTask(segments.size());
		}
		
		System.out.println("Straining " + segments.size() + " segments.");
		
		StrainerResult sr1 = doSegment(segments.get(0),reads.iterator());
		
		//System.out.println("Result 1: " + sr1.toString());
		
		// link segments by reads
		for (int i = 1; i < segments.size(); i++) {
			if (mTask!=null) {
				mTask.setCurrent(i);
			}

			//System.out.println("Straining segment " + (i+1));
			SequenceSegment ss = segments.get(i);
			StrainerResult sr2 = doSegment(ss,reads.iterator());

			//System.out.println("Result " + (i+1) + ": " + sr2.toString());

			/*System.out.println("Strainer combining " + 
				       sr2.getStrains().size() +
				       " new strains to previous " +
				       sr1.getStrains().size());*/
			sr1 = combineResults(sr1,sr2,ss.getStart());
			
		}

		if (mTask!=null) {
			mTask.setCurrent(segments.size());
		}

		result = new DefaultStrainerResult(getSegment(),sr1.getStrains()); 
		
		// reset settings to pre-override values
		settings.put(Config.KEEP_ALL_READS,oldKeepAllReads);
		settings.put(Config.RESTRICT_TO_SEGMENT,oldRestrictToSegment);
	}
	
	private StrainerResult combineResults(StrainerResult p1,
			StrainerResult p2, int pPos) {
		HashSet<Strain> linkedStrains = new HashSet<Strain>();
		HashSet<Strain> newStrains = new HashSet<Strain>();

		Iterator<Strain> sit2 = p2.getStrains().iterator();
		//System.out.println("looping over " + p2.getStrains().size() + " strains");
		int i2 = 0;
		while (sit2.hasNext()) {
			i2++;
			Strain strain2 = sit2.next();
			boolean linked = false;
			HashSet<Integer> starters = findReadsAtPosition(strain2,pPos);
			//System.out.print("starters for " + i2 + " are: ");
			//printSet(starters);
			if (starters.size()>0) {
				Iterator<Strain> sit1 = p1.getStrains().iterator();
				//System.out.println("looping over " + p1.getStrains().size() + " strains");
				int i1 = 0;
				while (sit1.hasNext()) {
					i1++;
					Strain strain1 = sit1.next();
					HashSet<Integer> enders = findReadsAtPosition(strain1,pPos);
					//System.out.print("enders for " + i1 + " are: ");
					//printSet(enders);
					if (enders.size()>0) {
					
						//System.out.println("comparing s" + i1 + " to s" + i2);
						
						boolean link = compareReadSets(starters,enders); 
						
						if (link) {
							//link strains
							//System.out.println("linking strains");
							linkedStrains.add(strain1);
							newStrains.add(linkStrains(strain1,strain2));
							linked = true;
						}
					}
				}
			}
			//System.out.println("linked is : " + linked);
			if (linked) {
				//System.out.println("removing");
				sit2.remove();
			}
		}
		
		//System.out.println("linked " + newStrains.size()
		//	   + " strains and removed " + linkedStrains.size());
		for (Strain linkedStrain : linkedStrains) {
			p1.getStrains().remove(linkedStrain);
		}

		//System.out.println(" there are " + p1.getStrains().size() + " strains in p1.");
		//System.out.println(" adding " + newStrains.size() + " new strains");
		p1.getStrains().addAll(newStrains);
		//System.out.println(" adding " + p2.getStrains().size() + " strains from p2");
		p1.getStrains().addAll(p2.getStrains());
		//System.out.println(" there are now " + p1.getStrains().size() + " strains in p1.");
		return p1;
	}
	
	private HashSet<Integer> findReadsAtPosition(Strain pS, int pPos) {
		//System.out.println("looking for reads at " + pPos);
		HashSet<Integer> reads = new HashSet<Integer>();
		Iterator<Read> rit = pS.getReadIterator();
		while (rit.hasNext()) {
			Read read = rit.next();
			//System.out.println("Read " + read.toString() + " spans " + read.getStart() + "-" + read.getEnd());
			if (read.intersectsRefereceSequenceAt(pPos)) {
				reads.add(read.getIdInteger());
			}
		}
		return reads;
	}

	/*
	private void printSet(Set s) {
		System.out.print("[");
		Iterator it = s.iterator();
		while (it.hasNext()) {
			System.out.print(it.next().toString());
			if (it.hasNext()) System.out.print(",");
		}
		System.out.println("]");
	}*/
	
	private boolean compareReadSets(HashSet<Integer> p1, HashSet<Integer> p2) {
		
		if (p1.size() != p2.size()) { return false; }

		// copy Sets to new containers
		HashSet<Integer> h1 = new HashSet<Integer>(p1);
		//printSet(h1);
		HashSet<Integer> h2 = new HashSet<Integer>(p2);
		//printSet(h2);
		
		Iterator<Integer> rit = h1.iterator();
		while (rit.hasNext()) {
			Integer read = rit.next();
			if (h2.remove(read)) {
				rit.remove();
			}
		}
		
		if ((h2.size()>0) || (h1.size()>0)) {
			return false;
		}
		
		return true;
	}
	
	private Strain linkStrains(Strain p1, Strain p2) {
		Strain strain = new Strain();
		strain.stealReads = false;
		
		// collect Reads
		Iterator<Readable> rit = p1.getReadableIterator();
		while (rit.hasNext()) {
			Readable r = rit.next();
			strain.putReadable(r.getIdInteger(),r);
		}
		rit = p2.getReadableIterator();
		while (rit.hasNext()) {
			Readable r = rit.next();
			strain.putReadable(r.getIdInteger(),r);
		}
		
		strain.setAlignmentFromReads();
		return strain;
	}
	
	private StrainerResult doSegment(SequenceSegment pSS, Iterator<Read> pReadIterator) throws SegmentStrainerException {
		//System.out.println("segment:" + pSS.getStart() + ":" + pSS.getEnd());
		SegmentStrainer ss;
		try {
			ss= Config.getSegmentStrainer(getSegmentStrainer());
		} catch (InstantiationException e) {
			throw new SegmentStrainerConfigurationException(e);
		} catch (IllegalAccessException e) {
			throw new SegmentStrainerConfigurationException(e);
		} catch (InvocationTargetException e) {
			throw new SegmentStrainerConfigurationException(e);
		} catch (NoSuchMethodException e) {
			throw new SegmentStrainerConfigurationException(e);
		}
		ss.setSegment(pSS);
		ss.setReads(pReadIterator);

		// get gene strains	
		return ss.getStrains();
	}
	
	private ArrayList<SequenceSegment> getSegments(Collection<Read> pReads) {
		ArrayList<SequenceSegment> segments = new ArrayList<SequenceSegment>();
		if (getSegmentMethod()==SEGMENT_BY_READ_COUNT) {
			
			// sort reads by start position
			Read [] readArray = pReads.toArray(new Read [0]);
			Arrays.sort(readArray,ReadStartComparator
					.getReadStartComparator());
			
			int last = 0;
			int pend = 1;
			boolean keepItUp = true;
			while (keepItUp) {
				int iend = last + getSegmentSize();
				int pstart = pend;
				pend = getReferenceSequence().getLength();
				if (readArray.length > iend) {
					pend = ((Readable) readArray[iend-1]).getStart();
				} else {
					keepItUp=false;
				}
				
				// clip at getSegment() boundaries
				if (pend<getSegment().getStart()) {
					// skip segments before main segment starts
					last = iend;
					continue;
				} else if (pstart<getSegment().getStart()) {
					pstart = getSegment().getStart();
				}
				if (pstart>getSegment().getEnd()) {
					// we're done don't use this segment
					keepItUp=false;
					continue;
				} else if (pend>getSegment().getEnd())  {
					// rached end.  Use this segment and quit
					pend = getSegment().getEnd();
					keepItUp=false;
				}
				
				SequenceSegment ss = 
					new SequenceSegment(getReferenceSequence(),pstart,pend);

				segments.add(ss);
				last = iend;
			}
		} else {
			// TODO:2 not tested yet
			int sstart = getSegment().getStart();
			int send = sstart + getSegmentSize();
			boolean keepItUp = true;
			while (keepItUp) {
				if (send>getSegment().getEnd()) {
					send = getSegment().getEnd();
					keepItUp = false;
				}
				SequenceSegment ss = new SequenceSegment(getReferenceSequence(), sstart, send);
				segments.add(ss);

				sstart = send;
				send = sstart + getSegmentSize();
			}
		}
		
		return segments;
	}
	
	private Task mTask = null;
	public void setTask(Task pTask) {
		mTask = pTask;
		
	}
	
}

