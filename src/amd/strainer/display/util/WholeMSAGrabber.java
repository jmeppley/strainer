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

import java.util.Iterator;

import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;

import amd.strainer.algs.Util;
import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Alignment;
import amd.strainer.objects.Sequence;

/**

 * this class is initialized with a range of bases in the reference sequence. The grab() method then
 * tests an AlignedSequence vs that  range. If it overlaps the entire range, a sequence is returned for the
 * given aligned sequence over the range.
 * 
 * @author jmeppley
 *
 */
public class WholeMSAGrabber extends MSAGrabber {
	/**
	 * Creates grabber
	 * @param pReference the reference sequence
	 * @param pStart the begining of the range
	 * @param pEnd end of the range
	 * @param pFillFromReference TRUE if uncovered regions should be filled from the reference sequence
	 * @param pConvertToAA 
	 */
	public WholeMSAGrabber(Sequence pReference, Iterator pSeqIt, int pStart, int pEnd, boolean pFillFromReference, int pConvertToAA) {
		super(pReference, pSeqIt,pStart,pEnd,pFillFromReference,pConvertToAA);
	}

	public String grab(AlignedSequence pAS) throws IllegalAlphabetException, IllegalSymbolException {
		// make sure sequence overlaps
		if (pAS.getStart()<=getStart() && pAS.getEnd()>=getEnd()) {
			Alignment al = pAS.getAlignment();
			String seq = al.getMSABases(mFillFromReference,
					mReferenceGaps,
					al.getPosFromReference(getStart()),
					al.getPosFromReference(getEnd()));
			if (mConvertToAA==0) {
				return seq;
			} else {
				return Util.getProteinSequence(seq,mConvertToAA!=CONVERT_TO_AA_BACKWARDS);
			}
		} else {
			return null;
		}
	}
}
