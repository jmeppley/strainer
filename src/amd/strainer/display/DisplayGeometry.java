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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Difference;
import amd.strainer.objects.SequenceFragment;

/**
 * A display geometry handles display related tasks for strainer objects
 * (Read,Clone,Strain,Gene). It stores information about if, where, and how an
 * object should be drawn. The update() method should be called any time this
 * information may change (zoom, pan, new data, new strains).
 * 
 * @author jmeppley
 * 
 */
public abstract class DisplayGeometry {

	// ///////////////////
	// constants
	final static Color STRAIN_OUTLINE = Color.black;
	final static Color STRAIN_SELECT_OUTLINE = Color.blue;
	final static Color READ_OUTLINE = null;
	final static Color READ_SELECTED_OUTLINE = Color.blue;
	// public final static Color READ_RECOMB_OUTLINE = Color.magenta;
	final static Color READ_SELECTED_RECOMB_OUTLINE = Color.red;
	final static Color READ_BADCLONE_OUTLINE = Color.yellow;
	final static Color READ_SELECTED_BADCLONE_OUTLINE = Color.green;
	final static Color READ_SELECT_FILL = Color.blue;
	final static Color READ_FILL = Color.white;
	final static Color READ_SELECTED_MATEPAIR_FILL = Color.orange;

	boolean recalcColor = true;

	public void recalcColors() {
		recalcColor = true;
	}

	private int row = 0;
	private int height = 1;

	/**
	 * @return the hight (in rows) of this object
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Set the height (in rows) of this object
	 * 
	 * @param pHeight
	 *            the desired height in rows (translation to pixels in
	 *            DisplayData.java)
	 */
	protected void setHeight(int pHeight) {
		height = pHeight;
	}

	protected SequenceFragment mParent = null;

	/**
	 * @param pParent
	 *            the object (Read,Clone,Strain,Gene) that is drawn by this
	 *            class
	 */
	public void setParent(SequenceFragment pParent) {
		mParent = pParent;
	}

	/** The background color */
	public Color fill = null;
	/** the outline color */
	public Paint outline = null;
	/** the java.awt.Shape object that defines how the object is drawn */
	public Shape shape = null;
	/** true if this object should be drawn */
	public boolean visible = false;

	protected Rectangle2D coloringBar = null;
	protected boolean[] coloringColors = null;

	/**
	 * return which row this object should be drawn in
	 */
	public int getRow() {
		return row;
	}

	/**
	 * @param row
	 *            the row in which to draw the object
	 */
	public void setRow(int row) {
		this.row = row;
	}

	/**
	 * @return A rectangle defining the outer extent of this object when drawn
	 */
	public Rectangle2D getBounds2D() {
		return shape.getBounds2D();
	}

	/**
	 * pointer to the display settings object
	 */
	public DisplaySettings settings = DisplaySettings.getDisplaySettings();

	/**
	 * updates the information about drawing this object
	 * 
	 * @param pData
	 *            DisplayData containing details about the current display
	 * @return true if this object will be visible using the current display
	 *         settings
	 */
	public abstract boolean update(DisplayData pData);

	/**
	 * utility method to get the x position given the base position in the
	 * reference sequence
	 * 
	 * @param pPos
	 *            the row number
	 * @param pData
	 *            the global display state (zoom level, etc)
	 * @return the x position on the canvas
	 */
	public static double getX(int pPos, DisplayData pData) {
		return pData.border + (pPos - pData.getStart()) * pData.scaleFactor;
	}

	/**
	 * utility method to get the y position given the row number
	 * 
	 * @param pRow
	 *            the row number
	 * @param pData
	 *            the global display state (zoom level, etc)
	 * @return the y position on the canvas
	 */
	public static int getY(int pRow, DisplayData pData) {
		return pData.refSeqAreaHeight + pData.geneAreaHeight
				+ (pData.rowHeight + pData.rowSpacing) * pRow;
	}

	/**
	 * utility method to get the object height in pixels given the number of
	 * rows it spans
	 * 
	 * @param pHeight
	 *            number of rows spanned
	 * @param pData
	 *            the global display state (zoom level, etc)
	 * @return the height in pixels
	 */
	protected static int getH(int pHeight, DisplayData pData) {
		return pData.rowHeight + (pHeight - 1)
				* (pData.rowHeight + pData.rowSpacing);
	}

	/**
	 * Create a GeneralPath shape from two arrays of numbers (x,y coords)
	 */
	public static GeneralPath getGeneralPath(double[] xPoints, double[] yPoints) {
		int size = Math.min(xPoints.length, yPoints.length);
		GeneralPath gp = new GeneralPath();
		gp.moveTo((float) xPoints[0], (float) yPoints[0]);
		for (int i = 1; i < size; i++) {
			gp.lineTo((float) xPoints[i], (float) yPoints[i]);
		}
		gp.closePath();
		return gp;
	}

	// create the coloring arrays. These are two arrays--one of shapes (lines or
	// rects)
	// the other of colors--that are used to fill in sequences with specialized
	// colors
	protected void setColoringArrays(DisplayData pData, int pX, int pY, int pW,
			int pH, int pThreshold) {
		// This is a rectangle that covers the first column of pixels
		coloringBar = new Rectangle2D.Double(pX - 0.5, pY, 1, pH);

		// the color the bar should be over each column of pixels in this object
		coloringColors = new boolean[pW];

		// get diffs from aligment
		List<Difference> diffs = null;
		AlignedSequence parent = null;
		if (mParent instanceof AlignedSequence) {
			parent = (AlignedSequence) mParent;
			diffs = parent.getAlignment().getDiffs();
		} else {
			diffs = new ArrayList<Difference>();
		}

		// average over
		// all covered pixels.
		double pWindowSize = (int) Math.ceil(1.0 / pData.scaleFactor);

		// loop over horizontal pixels and get the average identity near each
		// one
		for (int i = pX; i < pX + pW; i++) {
			double identity = 1.0;

			// if the read has any diffs
			if (diffs.size() > 0) {
				// where are we on the sequence
				int pos = pData.getZoomedDataPositionFromX(i);

				// will only be here if there are diffs (i.e. parent is an
				// AlignedSequence and not null)
				if (parent.getAlignment().isUncovered(pos)) {
					// if there is no sequence data here
					// (e.g. unsequenced space between mate pairs)
					// just use average ID of entire sequence
					identity = parent.getAlignment().getIdentity();
				} else {
					// over the window centered at this position
					double halfWidth = pWindowSize / 2.0;
					int startPos = Math.max(
							mParent.getStart(),
							(int) (pos - halfWidth));
					int endPos = Math.min(
							mParent.getEnd(),
							(int) (pos + halfWidth));

					// count the diffs
					int diffCount = parent.getAlignment().countDiffsInRange(
							startPos,
							endPos);

					// calculate the identity
					int length = 1 + endPos - startPos;
					identity = (double) (length - diffCount) / (double) length;
				}
			}

			// set color based on whether calculated ID is over/under the
			// threshold
			// (booleans in this array get converted to colors later)
			coloringColors[i - pX] = identity > pThreshold / 100.0;
		}
	}

	private static HashMap<Integer, Color> randomColors = new HashMap<Integer, Color>();

	public static void clearRandomColors() {
		randomColors.clear();
	}

	/**
	 * Gets the randomized color for this strain. Colors are linked to strain
	 * ID's by a hash map. The colors will change if Strainer is restarted, but
	 * colors should be static in a single session
	 * 
	 * @return the Color for the strain when using randomized colors
	 */
	public Color getRandomColor() {
		Color randomColor = randomColors.get(mParent.getIdInteger());
		if (randomColor == null) {
			randomColor = amd.strainer.display.util.Util.pickRandomColor();
			setRandomColor(randomColor);
		}
		return randomColor;
	}

	/**
	 * Changes the color assignment for this strain
	 * 
	 * @param pColor
	 *            a Color
	 */
	public void setRandomColor(Color pColor) {
		randomColors.put(mParent.getIdInteger(), pColor);
	}

	/**
	 * Draw the second background color (used when the two-tone coloring option
	 * is set)
	 * 
	 * @param pG2d
	 *            the graphics object to draw to
	 * @param pColor
	 *            the color to use
	 */
	public void fillInColors(Graphics2D pG2d, Color pColor) {
		double x = coloringBar.getMinX();

		double y1 = coloringBar.getMinY();
		double h = coloringBar.getHeight();
		for (int i = 0; i < coloringColors.length; i++) {
			// only need to draw one color, other will show through from strain
			// fill
			if (!coloringColors[i]) {
				coloringBar.setRect(x + i, y1, 1, h);
				pG2d.setColor(pColor);
				pG2d.fill(coloringBar);
			}
		}
		coloringBar.setRect(x, y1, 1, h);
	}

	public void resetColor() {
		color = null;
	}

	protected Color color = null;

	@Override
	public String toString() {
		return new StringBuffer("DisplayGeometry: ")
				.append(shape.toString())
				.toString();
	}

	public abstract void draw(Graphics2D pG2d, DisplayData pData);

	public void setOutline(Color pOutline) {
		outline = pOutline;
	}

	public void setFill(Color pFill) {
		fill = pFill;
	}

}
