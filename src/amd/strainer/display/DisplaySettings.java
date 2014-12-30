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

package amd.strainer.display;

import java.awt.Color;

// TODO:2 DOCS
// TODO:2 Make settings persistent

/**
 * Singleton class that holds global settings for the display. Sorting, colors, etc.
 * 
 * @author jmeppley
 */
public class DisplaySettings {
	// singelton class
	private static DisplaySettings instance = null;

	/**
	 * Get the sole instance of this class
	 * @return The DisplaySetings object
	 */
	public static DisplaySettings getDisplaySettings() {
		if (instance==null) {
			instance = new DisplaySettings();
		}
		return instance;
	}
	
	// private constructor
	private DisplaySettings() { initialize(); }
	
	// constants
	/** value to sort strains by number of reads */
	public static final int SORT_BY_SIZE = 0;
	/** sort reads or strains by number of nucleotides */
	public static final int SORT_BY_LENGTH = 1;
	/** sort reads or strains by similarity to reference sequence */
	public static final int SORT_BY_IDENTITY = 2;
	/** array of mappings between setting values and strings for display */
	public Object[] sortingOptions = {
			new Setting(SORT_BY_SIZE,"Size"),
			new Setting(SORT_BY_LENGTH,"Length"),
			new Setting(SORT_BY_IDENTITY,"Identity")
	};	
	/**don't let anything be out of order vertially, even if it's way off to the side*/
	public static final int STACK_STRICTLY = 0;
	/** allow some objects to be out of vertically as long as they don't overlap. This makes for a more compact display.*/
	public static final int STACK_COMPACTLY= 1;
	/**	complete clones (pairs of reads) are stacked compactly, but solo reads are not allowed to be out of order with any clones */
	public static final int STACK_HYBRID = 2;
	/** array of mappings between setting values and strings for display */
	public Object[] strainStackingOptions = {		
			new Setting(STACK_STRICTLY,"Strict Ordering"),
			new Setting(STACK_COMPACTLY,"Compact")
	};	
	/** array of mappings between setting values and strings for display */
	public Object[] readStackingOptions = {
			new Setting(STACK_STRICTLY,"Strict Ordering"),
			new Setting(STACK_COMPACTLY,"Compact"),
			new Setting(STACK_HYBRID,"Hybrid")
	};	
	/**
	 * color all objects one color
	 */
	public static final int COLOR_CONSTANT = 0;
	/**
	 * tint objects based on identity (matching to reference sequence)
	 */
	public static final int COLOR_TINT = 1;
	/**
	 * color each vertical column of pixels based on whether undelying bases are over or under a threshold of similarity to reference sequence
	 */
	public static final int COLOR_TWO_TONE = 2;
	/**
	 * pick random colors for each object
	 */
	public static final int COLOR_RANDOM = 3;

	// variables/accessors
	
	private int readSorting = SORT_BY_IDENTITY;
	/**
	 * @return the selected method for sorting reads
	 * @see #SORT_BY_IDENTITY
	 * @see #SORT_BY_LENGTH
	 * @see #SORT_BY_SIZE
	 */
	public int getReadSorting() { return readSorting; }
	/**
	 * @return the Setting object (for use in drop down list) for the currently selected option
	 */
	public Object getReadSortingObject() { return sortingOptions[readSorting]; }
	/**
	 * set the method for sorting reads
	 * @see #SORT_BY_IDENTITY
	 * @see #SORT_BY_LENGTH
	 * @see #SORT_BY_SIZE
	 */
	public void setReadSorting(int pReadSorting) {readSorting = pReadSorting; }
	/**
	 * set the method for sorting reads using an object taken from a dropdown list
	 * @param pReadSorting a Setting object
	 */
	public void setReadSorting(Object pReadSorting) {
		readSorting = ((Setting) pReadSorting).getId(); 
	}
	
	private int strainSorting = SORT_BY_SIZE;
	/**
	 * @return the current method of strain sorting
	 * @see #SORT_BY_IDENTITY
	 * @see #SORT_BY_LENGTH
	 * @see #SORT_BY_SIZE
	 */
	public int getStrainSorting() { return strainSorting; }
	/**
	 * @return the Setting object (for use in drop down list) for the currently selected option
	 */
	public Object getStrainSortingObject() { return sortingOptions[strainSorting]; }
	/**
	 * set the method for sorting strains
	 * @see #SORT_BY_IDENTITY
	 * @see #SORT_BY_LENGTH
	 * @see #SORT_BY_SIZE
	 */
	public void setStrainSorting(int pStrainSorting) {strainSorting = pStrainSorting; }
	/**
	 * set the method for sorting strains using an object taken from a dropdown list
	 * @param pStrainSorting a Setting object
	 */
	public void setStrainSorting(Object pStrainSorting) {
		strainSorting = ((Setting) pStrainSorting).getId(); 
	}

	private int readStacking = STACK_COMPACTLY;
	public int getReadStacking() { return readStacking; }
	/**
	 * @return the Setting object (for use in drop down list) for the currently selected option
	 */
	public Object getReadStackingObject() { return readStackingOptions[readStacking]; }
	public void setReadStacking(int pReadStacking) {readStacking = pReadStacking; }
	public void setReadStacking(Object pReadStacking) {
		readStacking = ((Setting) pReadStacking).getId(); 
	}
	
	private int strainStacking = STACK_COMPACTLY;
	public int getStrainStacking() { return strainStacking; }
	/**
	 * @return the Setting object (for use in drop down list) for the currently selected option
	 */
	public Object getStrainStackingObject() { return strainStackingOptions[strainStacking]; }
	public void setStrainStacking(int pStrainStacking) {strainStacking = pStrainStacking; }
	public void setStrainStacking(Object pStrainStacking) {
		strainStacking = ((Setting) pStrainStacking).getId(); 
	}

	private Color readHighColor = new Color(204,204,255);
	public Color getReadHighColor() {
		return readHighColor;
	}
	public void setReadHighColor(Color readHighColor) {
		this.readHighColor = readHighColor;
	}
	
	private boolean showNs = true;
	/**User variable to hide or show n's*/
	public boolean getShowNs() { return showNs; }
	/**User variable to hide or show n's*/
	public void setShowNs(boolean pShowNs) { showNs = pShowNs; }
	
	private Color readLowColor = new Color(0,0,102);
	public Color getReadLowColor() {
		return readLowColor;
	}
	public void setReadLowColor(Color readLowColor) {
		this.readLowColor = readLowColor;
	}
	
	private Color strainHighColor = Color.pink;
	public Color getStrainHighColor() {
		return strainHighColor;
	}
	public void setStrainHighColor(Color strainHighColor) {
		this.strainHighColor = strainHighColor;
	}

	private Color strainLowColor = Color.yellow;
	public Color getStrainLowColor() {
		return strainLowColor;
	}
	public void setStrainLowColor(Color strainLowColor) {
		this.strainLowColor = strainLowColor;
	}
	
	private Color recombinantColor = Color.red;
	private Color recombinantSelectColor = new Color(204,153,255);
		
	private Color readConstantColor = Color.white;
	private int readColorStyle = COLOR_TWO_TONE;
	private int readColorThreshold = 92;
	private int strainColorStyle = COLOR_CONSTANT;
	private Color strainConstantColor = Color.darkGray;
	private int strainColorThreshold = 92;
	private int readLowCutoffValue = 70;
	private int strainTintLowCutoffValue = 70;
	// methods
	
	public int getReadColorStyle() {
		return readColorStyle;
	}
	public void setReadColorStyle(int readColorStyle) {
		this.readColorStyle = readColorStyle;
	}
	public int getReadColorThreshold() {
		return readColorThreshold;
	}
	public void setReadColorThreshold(int readTintThreshold) {
		this.readColorThreshold = readTintThreshold;
	}
	public int getStrainColorStyle() {
		return strainColorStyle;
	}
	public void setStrainColorStyle(int strainColorStyle) {
		this.strainColorStyle = strainColorStyle;
	}
	public Color getStrainConstantColor() {
		return strainConstantColor;
	}
	public void setStrainConstantColor(Color pColor) {
		strainConstantColor = pColor;
	}
	public int getStrainColorThreshold() {
		return strainColorThreshold;
	}
	public void setStrainColorThreshold(int strainTintThreshold) {
		this.strainColorThreshold = strainTintThreshold;
	}
	private void initialize() {
		
	}
	
	// special class
	private class Setting {
		Setting(int pId, String pText) {
			mId = pId;
			mText = pText;
		}
		
		private int mId = 0;
		int getId() { return mId; }
		private String mText = null;
		public String toString() { return mText; }
	}

	public int getReadLowCutoffValue() {
		return readLowCutoffValue;
	}
	public void setReadLowCutoffValue(int readLowCutoffValue) {
		this.readLowCutoffValue = readLowCutoffValue;
	}
	public int getStrainTintLowCutoffValue() {
		return strainTintLowCutoffValue;
	}
	public void setStrainTintLowCutoffValue(int strainLowCutoffValue) {
		this.strainTintLowCutoffValue = strainLowCutoffValue;
	}
	public Color getReadConstantColor() {
		return readConstantColor;
	}
	public void setReadConstantColor(Color readConstantColor) {
		this.readConstantColor = readConstantColor;
	}
	public Color getRecombinantColor() {
		return recombinantColor;
	}
	public void setRecombinantColor(Color recombinantColor) {
		this.recombinantColor = recombinantColor;
	}
	public Color getRecombinantSelectColor() {
		return recombinantSelectColor;
	}
	public void setRecombinantSelectColor(Color recombinantSelectColor) {
		this.recombinantSelectColor = recombinantSelectColor;
	}
	
	public boolean isStrainDiffsOn() {
		return ((getStrainSorting()==SORT_BY_IDENTITY)
				||
				(getStrainColorStyle()!=COLOR_CONSTANT));
	}

	private Color readTintHighColor = Color.WHITE;
	private Color readTintLowColor = Color.BLACK;
	private Color strainTintHighColor = Color.WHITE;
	private Color strainTintLowColor = Color.BLACK;
	private boolean drawAllLetters = false;

	public Color getReadTintHighColor() {
		return readTintHighColor;
	}

	public void setReadTintHighColor(Color readTintHighColor) {
		this.readTintHighColor = readTintHighColor;
	}

	public Color getReadTintLowColor() {
		return readTintLowColor;
	}

	public void setReadTintLowColor(Color readTintLowColor) {
		this.readTintLowColor = readTintLowColor;
	}

	public Color getStrainTintHighColor() {
		return strainTintHighColor;
	}

	public void setStrainTintHighColor(Color strainTintHighColor) {
		this.strainTintHighColor = strainTintHighColor;
	}

	public Color getStrainTintLowColor() {
		return strainTintLowColor;
	}

	public void setStrainTintLowColor(Color strainTintLowColor) {
		this.strainTintLowColor = strainTintLowColor;
	}

	public Color getLetterColor() {
		// TODO:1 add to settings interface
		return Color.BLACK;
	}

	/**
	 * If true, draw letters in all positions. Set from GUI
	 */
	public boolean isDrawAllLetters() {
		return drawAllLetters;
	}

	/**
	 * If true, draw letters in all positions. Set from GUI
	 */
	public void setDrawAllLetters(boolean drawAllLetters) {
		this.drawAllLetters = drawAllLetters;
	}
	

}
