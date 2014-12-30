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
 ***** END LICENSE BLOCK ***** */package amd.strainer.display.util;

import java.util.Arrays;
import java.util.Iterator;

import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Sequence;

/**

 * this class is initialized with a range of bases in the reference sequence. The grab() method then
 * tests an AlignedSequence vs that  range. If it overlaps the entire range, a sequence is returned for the
 * given aligned sequence over the range.
 * 
 * @author jmeppley
 *
 */
public abstract class MSAGrabber extends SequenceGrabber {
	boolean mFillFromReference;
	int [] mReferenceGaps = new int [0];
	protected int mConvertToAA;
	
	/**
	 * Creates grabber
	 * @param pReference the reference sequence
	 * @param pStart the begining of the range
	 * @param pEnd end of the range
	 * @param pFillFromReference TRUE if uncovered regions should be filled from the reference sequence
	 * @param pConvertToAA if true, return sequences as amino acids, not nucleotides
	 */
	public MSAGrabber(Sequence pReference, Iterator pSeqs, int pStart, int pEnd, boolean pFillFromReference, int pConvertToAA) {
		super(pReference, pStart,pEnd);
		mFillFromReference = pFillFromReference;
		mConvertToAA = pConvertToAA;
		
		// initialize reference gaps list
		while (pSeqs.hasNext()) {
			mReferenceGaps = updateReferenceGaps(mReferenceGaps,((AlignedSequence)pSeqs.next()).getAlignment().getReferenceGaps(),pStart,pEnd);
		}
	}

	private int [] updateReferenceGaps(int [] pGaps, int [] pNewGaps, int pStart, int pEnd) {
		// store unique new gaps in here
		int [] tempGaps = new int [pNewGaps.length];
		// keep track of new gaps
		int count =0;
		
		// find unique new gaps
		for (int i = 0; i<pNewGaps.length; i++) {
			if (pNewGaps[i]>=pStart && pNewGaps[i]<=pEnd) {
				if (Arrays.binarySearch(pGaps,pNewGaps[i])<0) {
					tempGaps[count]=pNewGaps[i];
					count++;
				}
			}
		}
		
		// combine new and old gaps into one
		int [] allGaps = new int [pGaps.length+count];
		for (int i=0; i<pGaps.length; i++)  {
			// get gaps from original list
			allGaps[i] = pGaps[i];
		}
		for (int i=0;i<count;i++) {
			// get new gaps that are unique
			allGaps[i+pGaps.length]=tempGaps[i];
		}
		
		return allGaps;
		
	}
}
