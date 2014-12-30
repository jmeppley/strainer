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


import amd.strainer.display.CloneDisplayGeometry;
import amd.strainer.display.DisplayData;
import amd.strainer.file.Util;

/**
 * A representation of a pair of Reads (mate-pairs) sequenced from the same cloned insert. Contains the
 * two Read objects in a 2 element array.
 * 
 * @author jmeppley
 *
 */
public class Clone extends AbstractAlignedSequence implements Readable {
	/**
	 * The Reads making up this Clone
	 */
	public Read [] reads = new Read [2];
	/**
	 * The Strain in which this Clone has been placed
	 */
	private Strain strain = null;
	
	/**
	 * Calculates the Clone ID for a Read (and it's MatePair) as the negative of the
	 * highest ID value of the two reads in the Clone.
	 * <p>
	 * -1*Math.max(pR.getId(),pR.getMatePair().getId());
	 * 
	 * @param pR A Read object
	 * @return the Id for the Clone that the given Read would belong to.
	 * will throw null pointer exception if Read has no mate-pair
	 */
	public static int calculateCloneID(Read pR) {
		return -1*Math.max(pR.getId(),pR.getMatePair().getId());
	}
	
	/**
	 * Creates a new Clone object for two Reads
	 */
	public Clone (Read pR1, Read pR2) {
		this(calculateCloneID(pR1),pR1,pR2);
	}
	
	/**
	 * Creates a new Clone object for two Reads
	 * @param pId The ID for the new Clone
	 */
	public Clone (int pId, Read pR1, Read pR2) {
		setId(pId);
		setName(Util.getReadNameBase(pR1.getName()));

		if (pR1.getStart() < pR2.getStart()) {
			reads[0] = pR1;
			reads[1] = pR2;
		} else {
			reads[0] = pR2;
			reads[1] = pR1;
		}
		
		// create alignment
		setAlignment(new CloneAlignment(this, reads[0].getAlignment(),reads[1].getAlignment()));
		
		// Synchronize recombinant status
		if (pR1.isRecombinant()) {
			recombinant = true;
			if (!pR2.isRecombinant()) {
				pR2.toggleRecombinant();
			}	
		} else {
			if (pR2.isRecombinant()) {
				recombinant = true;
				pR1.toggleRecombinant();
			}	
		}		
	}
	
	public void initializeGraphics() {
		setDisplayGeometry(new CloneDisplayGeometry(this));
	}

	// methods for the Readable interface
	
	public Strain getStrain() { return strain; }
	public void setStrain(Strain pStrain) { 
		strain = pStrain; 
		// male sure this Clone's reads are also set to pStrain
		reads[0].setStrain(pStrain);
		reads[1].setStrain(pStrain);
	}
	// this is part of the Readable interface, but is meaningless for a Clone
	public Read getMatePair() { return null; }
	
	public boolean isBadClone() {
		return reads[0].isBadClone() || reads[1].isBadClone();
	}
	
	public String toString() {
		StringBuffer ret = new StringBuffer("[");
		
		for (int i = 0; i < reads.length; i++) {
			if (i!=0) {
				ret.append(",");
			}
			ret.append(reads[i].toString());
		}
		
		ret.append("]");
		return ret.toString();
	}
	
	/**
	 * Selects this clone.  In particular, sets the selected flag to true.  Sets this
	 * object to data.selectedObject.  Adds all contained reads to prev selection list.
	 * Updates colors.
	 */
	public void select(DisplayData pData) {
		selected = true;
		pData.selectedObject = this;
		reads[0].addToSelectedList(pData);
		reads[1].addToSelectedList(pData);
		recalcColors();
	}
	
	/**
	 * Deselects clone.  This merely changes the state and colors.
	 */
	public void deselect(DisplayData pData) {
		selected = false;
		recalcColors();
		reads[0].recalcColors();
		reads[1].recalcColors();
	}

	public void addToSelectedList(DisplayData pData) {
		reads[0].addToSelectedList(pData);
		reads[1].addToSelectedList(pData);
	}
	
	/** 
	 * For each contained read, calls the read's version of this method
	 *  which removes the read from the prevSelected read list in DsiplayData
	 *  and calls the color setting method of the read
	 */
	public void removeFromSelectedList(DisplayData pData) {
		// remove member reads from list
		reads[0].removeFromSelectedList(pData);
		reads[1].removeFromSelectedList(pData);
	}

	public String detailsString() {
		StringBuffer details = new StringBuffer("[").append(getStart()).append(":");
		String mpr0bases = reads[0].getBases();
		if (mpr0bases.length()>10) {
			details.append(mpr0bases.substring(0,10));
		} else {
			details.append(mpr0bases);
		}
		details.append("...");
		String mpr1bases = reads[1].getBases();
		if (mpr1bases.length()>10) {
			int r2end = mpr1bases.length();
			details.append(mpr1bases.substring(r2end-10,r2end));
		} else {
			details.append(mpr1bases);
		}
		details.append(":").append(getEnd()).append("]");
		return details.toString();
	}
	
	public boolean intersectsRefereceSequenceAt(int pPos) {
		return reads[0].intersectsRefereceSequenceAt(pPos) || 
		reads[1].intersectsRefereceSequenceAt(pPos);
	}
	
	private boolean recombinant = false;
	public void toggleRecombinant() { 
		recombinant = !recombinant;
		if (reads[0].isRecombinant()!=recombinant) {
			reads[0].toggleRecombinant();
		}
		if (reads[1].isRecombinant()!=recombinant) {
			reads[1].toggleRecombinant();
		}
	}
	public boolean isRecombinant() { return recombinant; }

}
