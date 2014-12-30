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

import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Sequence;
import amd.strainer.objects.SequenceSegment;

/**
*  The classes that extend this abstract class are initialized with a range of bases in the reference sequence. The grab() method then
 * tests an AlignedSequence vs that  range. If it overlaps at all, a sequence is returned for the
 * given aligned sequence over the range. The different implementing classes place different resrictions on which sequences map. At the moment
 * the choices are complete range overlap or partial range overlap.
 * 
 *TODO:3 make sure this is used everywhere it can be
 *TODO:2 testing
 * 
 * @author jmeppley
 *
 */
public abstract class SequenceGrabber extends SequenceSegment {
	
	public static final int CONVERT_TO_AA_FORWARDS = 1;
	public static final int CONVERT_TO_AA_BACKWARDS = 2;
	
	public SequenceGrabber(Sequence pReference, int pStart, int pEnd) {
		super(pReference, pStart, pEnd);
	}
	
	/**
	 * Creates the requested SequenceGrabber object
	 * @param pReference the reference seuqce
	 * @param pStart start of the range
	 * @param pEnd end of the range
	 * @param pPartial whether partial overlaps will be included
	 * @param pFillFromReference whther to get bases for unccovered regions from the refrence (TRUE) or use X's (FALSE)
	 * @param pConvertToAA if nonzero, convert nucleotidesequence to amino acids
	 * @return a SequenceGrabber object
	 */
	public static SequenceGrabber getSequenceGrabber(Sequence pReference, int pStart, int pEnd, boolean pPartial, boolean pFillFromReference, int pConvertToAA) {
		if (pPartial) {
			return new PartialSequenceGrabber(pReference,pStart,pEnd,pFillFromReference,pConvertToAA);
		} else {
			return new WholeSequenceGrabber(pReference,pStart,pEnd,pFillFromReference,pConvertToAA);
		}
	}
		
	/**
	 * Creates the requested SequenceGrabber object for multiple sequence alignments
	 * @param pReference the reference seuqce
	 * @param pStart start of the range
	 * @param pEnd end of the range
	 * @param pPartial whether partial overlaps will be included
	 * @param pFillFromReference whther to get bases for unccovered regions from the refrence (TRUE) or use X's (FALSE)
	 * @param pConvertToAA if nonzero convert output sequence to amino acids from nucleotides
	 * @return a SequenceGrabber object
	 */
	public static SequenceGrabber getMSAGrabber(Sequence pReference, Iterator pSeqIt, int pStart, int pEnd, boolean pPartial, boolean pFillFromReference, int pConvertToAA) {
		if (pPartial) {
			return new PartialMSAGrabber(pReference,pSeqIt,pStart,pEnd,pFillFromReference,pConvertToAA);
		} else {
			return new WholeMSAGrabber(pReference,pSeqIt,pStart,pEnd,pFillFromReference,pConvertToAA);
		}
	}

	/**Test the given sequence vs. the range for this grabber
	 * @param pAS an aligned sequence
	 * @return a string containing the passed elements sequence for this grabber's range. null if there is no overlap.
	 * @throws IllegalSymbolException 
	 * @throws IllegalAlphabetException 
	 */
	public abstract String grab(AlignedSequence pAS) throws IllegalAlphabetException, IllegalSymbolException;
}


