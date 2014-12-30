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

import amd.strainer.display.DisplayData;
import amd.strainer.display.ReadDisplayGeometry;

/**
 * A single sequence of bases read during shotgun sequencing. A Read needs to know where it aligns to the
 * ReferenceSequence, can be part of a user defined Strain, and can (along with its MatePair) be part of a 
 * Clone.
 * 
 * @author jmeppley
 *
 */
public class Read extends AbstractAlignedSequence implements Readable {
	public int [] quals = null;

	//	/**
//	 * Id of mate pair. if it is positive, then mate pair exists. DONOT set to positive withou
//	 *  setting the matepair object.
//	 */
//	public int matePairId = -1;
	
	// the Read object that represents the mate pair of this object 
	private Read matepair = null;
	/**
	 * Set the matepair for this read. This should only need to be run once for each pair. Also sets this read as 
	 * the mate pair of the passed in read. And sets matePairId values for both. Also creates Clone oobject for pair and associates it with
	 * both reads.
	 * 
	 * @param matepair this read's mate pair
	 */
	public void setMatepair(Read matepair) {
		this.matepair = matepair;

		if (matepair != null) {
			// associate pair
//			matePairId = matepair.getId();
			matepair.matepair = this;
//			matepair.matePairId = this.getId();
			
			// create Clone object
			clone = new Clone(this,matepair);
			matepair.clone = clone;
		} else {
//			matePairId = -1;
		}
	}
	
	// the Clone that this read is a part of 
	// (null if it has no mate pair or they are not grouped together into a clone object)
	private Clone clone = null;
	/**
	 * @return the Clone that contains this read
	 */
	public Clone getClone() { return clone; }
	/**
	 * Set the Clone for this read
	 * @param clone the Clone this read should belong to
	 */
	public void setClone(Clone clone) {this.clone = clone;}

	private boolean inClone = false;
	/**
	 * @return true if read is displayed within its clone
	 */
	public boolean isInClone() {return inClone;}
	/**
	 * @param inClone whether read should be displayed in its clone
	 */
	public void setInClone(boolean inClone) {this.inClone = inClone;}
	
	private boolean badClone = false;
	
	public String toString() {
		StringBuffer ret = new StringBuffer("Read ");
		ret.append(getName()).append("(").append(getStart());
		ret.append("-").append(getEnd()).append(")");
		return ret.toString();
	}
	
	public void initializeGraphics() {
		setDisplayGeometry(new ReadDisplayGeometry(this));
	}
	
	private Strain strain = null;
	public Strain getStrain() { return strain; }
	public void setStrain(Strain pStrain) { strain = pStrain; }
	public Read getMatePair() { return matepair; }
	
	public boolean intersectsRefereceSequenceAt(int pPos) {
		return getAlignment().getStart()<=pPos && 
		getAlignment().getEnd()>=pPos;
	}
	
	private boolean recombinant = false;
	public void toggleRecombinant() { 
		recombinant = !recombinant;
		if (inClone&&clone.isRecombinant()!=recombinant) {
			clone.toggleRecombinant();
		}
	}
	public boolean isRecombinant() { return recombinant; }
	
	public boolean isBadClone() {
		return badClone;
	}
	public void setBadClone(boolean badClone) {
		this.badClone = badClone;
	}
	
	public boolean inSelectedList = false;
	
	/**
	 * Selects read.  In particular is sets the selected flag to true.  Sets this
	 * object to data.selectedObject.  Adds all contained reads to prev selection list.
	 * Updates colors.
	 */
	public void select(DisplayData pData) {
		selected = true;
		pData.selectedObject = this;
		addToSelectedList(pData);
		//matePair will be null for loners and matepairs

		if (matepair!=null) {
			// create shape if nec.
			if ((!matepair.getStrain().getDisplayGeometry().visible) ||
					(!matepair.getDisplayGeometry().visible)) {
				((ReadDisplayGeometry) matepair.getDisplayGeometry()).updateMatePairCarat(pData);
				pData.matePairCarat=true;
			}
			
			// set colors
			matepair.recalcColors();
		}
	}
	
	/**
	 * Deselects a strain.  This merely changes the state and colors.
	 */
	public void deselect(DisplayData pData) {
		if (matepair!=null) {
			pData.matePairCarat=false;
			matepair.recalcColors();
		}
		selected = false;
		recalcColors();
	}

	public void addToSelectedList(DisplayData pData) {
		inSelectedList = true;
		pData.selectedReadList.putRead(getIdInteger(),this);
		recalcColors();
		if (clone!=null) {
			clone.recalcColors();
		}
	}

	/** 
	 * Removes the read from the prevSelected read list in DsiplayData
	 *  and calls the color setting method of the read
	 */
	public void removeFromSelectedList(DisplayData pData) {
		// remove from list
		pData.selectedReadList.removeRead(this);

		// reset state and make sure colors are updated
		inSelectedList=false;
		recalcColors();
		if (clone!=null) {
			clone.recalcColors();
		}
	}
}
