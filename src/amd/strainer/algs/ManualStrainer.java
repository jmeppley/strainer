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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import amd.strainer.objects.Read;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * Simply gets the existing (usu. manually created) strains that intersect the given segment
 * 
 * @author jmeppley
 *
 */
public class ManualStrainer extends AbstractSegmentStrainer implements SegmentStrainer {
	private static final String ALGORITHM_NAME = "Manual Strainer";
	private static final String ALGORITHM_DESCRIPTION = "Simply gets the strains from the display";

	public static final String MINIMUM_OVERLAP = "Minimum overlap (in fraction) for including strain in results";
	public static final String SKIP_CLOSED_STRAINS = "Do not include strains collapsed in the display";
	
	// tell GUI about the algorithm
	public String getName() {
		return ALGORITHM_NAME;
	}
	public String getDescription() {
		return ALGORITHM_DESCRIPTION;
	}
	
	public ManualStrainer() {
		readSettings();
	}

	private void readSettings() {
		// Get the settings hash map
		HashMap<String,Object> settings =  Config.getConfig().getSettings();  
		if (settings==null) {
			return;
		}
		
		// how much do strains have to overlap by?
		Object value = settings.get(MINIMUM_OVERLAP);
		if (value!=null) {
			setMinimumOverlap(value);
		} else {
			settings.put(MINIMUM_OVERLAP,new Double(mMinimumOverlap));
		}

		// how much do strains have to overlap by?
		value = settings.get(SKIP_CLOSED_STRAINS);
		if (value!=null) {
			setSkipClosedStrains(value);
		} else {
			settings.put(SKIP_CLOSED_STRAINS,new Boolean(mSkipClosedStrains));
		}
	}

	private boolean mSkipClosedStrains;
	private void setSkipClosedStrains(Object pValue) {
		if (pValue instanceof Boolean) {
			mSkipClosedStrains = ((Boolean)pValue).booleanValue();
		}
	}
	
	private double mMinimumOverlap = 0.75;
	private void setMinimumOverlap(Object pValue) {
		mMinimumOverlap = Double.parseDouble(pValue.toString());
	}

	
	@Override
	public StrainerResult getStrains() throws SegmentStrainerException {
		Set<Strain> strains = new HashSet<Strain>();

		// simply find intersecting strains
		ReferenceSequence refSeq = (ReferenceSequence) mSegment.getSequence();
		for (Strain strain : refSeq.strains.values()) {
			if (strain.intersects(mSegment) && ((!mSkipClosedStrains) || (strain.isOpen()))) {
				if (mMinimumOverlap==0 || calculateOverlap(strain)>=mMinimumOverlap) {
					Strain newStrain = createStrainForSegment(strain);
					if (newStrain!=null) {
						strains.add(newStrain);
					}
				}
			}
		}
		
		return new DefaultStrainerResult(getSegment(),strains);
	}
		
	// creates a new strain object conatining reads that intersect segment
	// if intersecting reads do not cove more than mMinium overlap, null is returned
	private Strain createStrainForSegment(Strain strain) {
		Strain segStrain = new Strain();
		segStrain.stealReads = false;
		Iterator<Read> rit = strain.getReadIterator();
		while (rit.hasNext()) {
			Read read = rit.next();
			if (read.intersects(mSegment)) {
				segStrain.putRead(read.getIdInteger(),read);
			}
		}
		
		// quit if no reads overlap
		if (segStrain.getSize()==0) return null;

		// build alignment
		segStrain.setAlignmentFromReads();

		// check amount of gene covered
		int uncoveredSize = 0;
		if (segStrain.getStart()>mSegment.getStart()) {
			uncoveredSize += segStrain.getStart() - mSegment.getStart();
		}
		if (segStrain.getEnd()<mSegment.getEnd()) {
			uncoveredSize += mSegment.getEnd() - segStrain.getEnd();
		}
		// check if gaps put us under
		List<SequenceSegment> holes = segStrain.getAlignment().getUnknownRegions();
		for (SequenceSegment hole : holes) {
			uncoveredSize += hole.getLength();
		}
		double coverage = (mSegment.getLength() - uncoveredSize)/(double)mSegment.getLength();
		if (coverage<mMinimumOverlap) return null;

		// everything checks out, return the new strain
		return segStrain;
	}

	private double calculateOverlap(Strain pStrain) {
		int first = Math.max(pStrain.getStart(),getSegment().getStart());
		int last = Math.min(pStrain.getEnd(),getSegment().getEnd());
		return (last - first + 1)/getSegment().getLength();
	}

	// tell GUI what the options are for this algorithm
	private HashMap<String,Object> mOptionsHash = null;
	@Override
	public HashMap<String,Object> getOptionsHash() {
		if (mOptionsHash==null) {
			mOptionsHash = new HashMap<String,Object>();
			mOptionsHash.put(Config.FILL_FROM_COMPOSITE,Boolean.FALSE);
			mOptionsHash.put(Config.CONVERT_TO_AA,Boolean.TRUE);
			mOptionsHash.put(MINIMUM_OVERLAP,new Double(mMinimumOverlap));
			mOptionsHash.put(SKIP_CLOSED_STRAINS,new Boolean(mSkipClosedStrains));
		}
		return mOptionsHash;
	}
}
