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

/**
 * A subsequence (segment) of a parental sequence
 * 
 * @author jmeppley
 *
 */
public class SequenceSegment {
	//////////////////
	// Accessors
	
	private Sequence sequence = null;
	/** the sequence object that this SequenceSegment is a part of */
	public Sequence getSequence() {return sequence;}
	
	private int start = -1;
	/** the start of this sequence segment */
	public int getStart() {return start;}
	/** the start of this sequence segment */
	public void setStart(int pStart) { start = pStart;}
	
	private int end = -1;
	/** the end of this sequence segment */
	public int getEnd() {return end;}
	/** the end of this sequence segment */
	public void setEnd(int pEnd) {end = pEnd;}
	
	/**
	 * @return the length of this segment in bases
	 */
	public int getLength() {return end-start+1;}
	
	/**
	 * Create a new segment
	 * @param pSeq the parent Sequence
	 * @param pStart the first base in the segment
	 * @param pEnd the last base in the segment
	 */
	public SequenceSegment(Sequence pSeq, int pStart, int pEnd) {
		sequence = pSeq;
		start = pStart;
		end = pEnd;
	}
	
	/**
	 * @param pQuery SequenceSegment to compare to
	 * @return true if there is any overlap
	 */
	public boolean intersects(SequenceSegment pQuery) {
		return (pQuery.getStart()<=getEnd() && pQuery.getEnd()>=getStart());
	}

	/**
	 * @param pQuery SequenceFragment to compare to
	 * @return true if there is any overlap
	 */
	public boolean intersects(SequenceFragment pQuery) {
		return (pQuery.getStart()<=getEnd() && pQuery.getEnd()>=getStart());
	}

	/**
	 * @param pPos position in the parent sequence to check
	 * @return True if pPos is contained in this segment
	 */
	public boolean containsPosition(int pPos) {
		return (start<=pPos && end>=pPos);
	}
	
	public boolean equals(SequenceSegment pSS) {
		return sequence==pSS.sequence && start==pSS.start && end==pSS.end;
	}
	
	public String toString() {
		return new StringBuffer("ss:")
		.append(getStart()).append("-")
		.append(getEnd()).toString();
	}
	
	public Object clone() {
		return new SequenceSegment(sequence, start, end);
	}
	public Object detailsString() {
		return new StringBuffer("ss(")
		.append(sequence).append("):")
		.append(getStart()).append("-")
		.append(getEnd()).toString();
	}

}

