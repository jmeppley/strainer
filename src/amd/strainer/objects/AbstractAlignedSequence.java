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

import amd.strainer.display.DisplayGeometry;

/**
 * Parent class of Strain, Read, and Clone. Short sequence segments that are placed relative to the
 * reference sequence. Codes many of the common methods for these types.
 * 
 * @author jmeppley
 *
 */
public abstract class AbstractAlignedSequence extends AbstractSequence implements AlignedSequence, SequenceFragment {
	
	//////////////////
	// Accessors
	
	private Alignment alignment = null;
	/** 
	 * get the Alignment indicationg where and how this sequence aligns to 
	 *  the reference sequence 
	 */
	public Alignment getAlignment() {
		return alignment;
	}
	/** 
	 * set the Alignment indicationg where and how this sequence aligns to 
	 *  the reference sequence 
	 */
	public void setAlignment(Alignment pAlignment) {alignment = pAlignment;}
	
	public int getStart() { 
		return alignment.getSequenceSegment1().getStart();
	}
	public int getEnd() { 
		return alignment.getSequenceSegment1().getEnd();
	}
	public int getLength() {
		if (super.getLength()<0) {
			setLength(getAlignment().getLength());
		}
		return super.getLength();
	}
	
	public Sequence getSequence() {
		return getAlignment().getSequenceSegment1().getSequence();
	}

	public void recalcColors() { getDisplayGeometry().recalcColors(); }
	
	/** 
	 * Returns the sting of bases that make up this sequence.  Calls alignment.getBases() unless
	 * setBases() has been used to set the string of sequences to something non-null. <p>
	 * Any uncovered regions will be filled in from the reference sequence
	 */
	public String getBases() { 
		if (bases==null) {
			return getAlignment().getBases(true); 
		}
		return bases;
	}	

	/** 
	 * Returns the sting of bases that make up this sequence.  Calls alignment.getBases() 
	 * @param pFillFromConsensus fill gaps from the reference sequence or use 'N's
	 */
	public String getBases(boolean pFillFromConsensus) { 
		return getAlignment().getBases(pFillFromConsensus); 
	}

	/** return the base at the specified position (relative to this sequence: 1 to getLength()) */
	public char getBase(int pPos) {
		return getAlignment().getBase(pPos);
	}
	
	/** return the base of the reference sequence at the specified position (relative to this sequence: 1 to getLength()) */
	public char getParentBase(int pPos) {
		return getAlignment().getSequenceSegment1().getSequence().getBase(getAlignment().getReferencePos(pPos));
	}

	// accessor methods needed for display
	private DisplayGeometry displayGeometry = null;
	public DisplayGeometry getDisplayGeometry() {
		if (displayGeometry==null) {
			initializeGraphics();
		}
		return displayGeometry;
	}
	public void setDisplayGeometry(DisplayGeometry pDG){displayGeometry = pDG;}
	/**
	 * @return true if the  graphics classes have been set up for this object (ie the XXXDisplayGeometry object)
	 */
	public boolean areGraphicsInitialized() {return displayGeometry!=null;}

	public abstract void initializeGraphics();
	
	public Object clone() {
		AbstractAlignedSequence c = (AbstractAlignedSequence) super.clone();
		c.setAlignment((Alignment)alignment.clone());
		return c;
	}

	// check if it intersects a given segment
	public boolean intersects(SequenceSegment pQuery) {
		return pQuery.intersects(getAlignment().getSequenceSegment1());
	}
	
	// check if it intersects another alignedsequence
	public boolean intersects(AlignedSequence pQuery) {
		return pQuery.getAlignment().getSequenceSegment1()
		.intersects(getAlignment().getSequenceSegment1());
	}

	protected boolean selected = false;
	/**
	 * @return true if this object has been clicked on in the GUI
	 */
	public boolean isSelected() { return selected; }

	public String detailsString() {
		String bases = getBases();
		if (bases!=null) {
			String baseString = null;
			if(bases.length() < 20) {
				baseString = bases;
			} else {
				baseString = bases.substring(0,10) + 
					"..." + 
					bases.substring(bases.length()-10,
							             bases.length());
			}
			return "[" + getStart()+ ":" + baseString + ":" + getEnd() + "]";
		} else {
			return "[" + getStart()+ ":" + getEnd() + "]";
		}
	}
}
