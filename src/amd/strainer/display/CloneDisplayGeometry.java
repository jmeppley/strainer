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
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import amd.strainer.display.util.Util;
import amd.strainer.objects.Clone;
import amd.strainer.objects.Read;

/**
 * Contains display data and methods for a Clone object (which contains a pair
 * of liked Reads). Calling update() on this object calls update on the
 * ReadDisplayGeometry objects for the Clone's two Reads.
 * 
 * @author jmeppley
 * @see amd.strainer.objects.Clone
 * @see amd.strainer.display.ReadDisplayGeometry
 */
public class CloneDisplayGeometry extends ReadableDisplayGeometry {

	private Rectangle2D bounds = new Rectangle2D.Double(0, 0, 0, 0);

	@Override
	public Rectangle2D getBounds2D() {
		return bounds;
	}

	/**
	 * Creates the CloneDisplayGeometry object for the given clone
	 * 
	 * @param pParent
	 *            CLone object to be displayed
	 */
	public CloneDisplayGeometry(Clone pParent) {
		mParent = pParent;
		if (pParent.reads[0].intersects(pParent.reads[1])) {
			setHeight(2);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * amd.strainer.display.DisplayGeometry#update(amd.strainer.display.DisplayData
	 * )
	 * 
	 * Called by ReferenceSequenceDsiplayComponent when doing pre-drawing
	 * calculations to figure out where to place this object on screen.
	 */
	@Override
	public boolean update(DisplayData pData) {
		double x1 = getX(mParent.getStart(), pData);
		double x2 = getX(mParent.getEnd() + 1, pData);

		Clone clone = (Clone) mParent;
		int row = getRow() + clone.getStrain().getDisplayGeometry().getRow();

		int y1 = getY(row, pData);
		int h = getH(getHeight(), pData);

		visible = Util.rectInDisplay(x1, x2, y1, h, pData);

		if (!visible)
			return false;

		if (pData.hideCloneContents) {
			// if we are zoomed way out, just draw a rectangle
			bounds = new Rectangle2D.Double(x1, y1, x2 - x1, h);
			shape = bounds;

			// don't show reads
			clone.reads[0].getDisplayGeometry().visible = false;
			clone.reads[1].getDisplayGeometry().visible = false;

			if (settings.getReadColorStyle() == DisplaySettings.COLOR_TWO_TONE) {
				setColoringArrays(
						pData,
						(int) x1,
						y1,
						(int) (x2 - x1),
						h,
						settings.getReadColorThreshold());
			}

		} else {
			// set up reads shapes
			ReadDisplayGeometry rdg0 = (ReadDisplayGeometry) clone.reads[0]
					.getDisplayGeometry();
			rdg0.update(pData, true);
			ReadDisplayGeometry rdg1 = (ReadDisplayGeometry) clone.reads[1]
					.getDisplayGeometry();
			rdg1.update(pData, true);

			x1 = clone.reads[0].getAlignment().isForward() ? rdg0.shape
					.getBounds()
					.getMaxX() : rdg0.shape.getBounds().getMinX();
			x2 = clone.reads[1].getAlignment().isForward() ? rdg1.shape
					.getBounds()
					.getMaxX() : rdg1.shape.getBounds().getMinX();

			// find the union of the two read rectangles and put into bounds:
			Rectangle2D.union(rdg0.shape.getBounds2D(), rdg1.shape
					.getBounds2D(), bounds);

			// connecting line
			int y = getY(row, pData) + getH(1, pData) / 2;
			if (getHeight() == 1) {
				Shape connector = new Line2D.Double(x1, y, x2, y);
				shape = connector;
			} else {
				int y2 = getY(row + 1, pData) + getH(1, pData) / 2;
				Shape connector = new Line2D.Double(x1, y, x2, y2);
				shape = connector;
			}
		}

		return true;
	}

	@Override
	public void draw(Graphics2D pG2d, DisplayData pData) {
		if (recalcColor) {
			setColors(pData);
		}

		// clone has 2 display forms depending on zoom level

		if (pData.hideCloneContents) {
			// if we are zoomed out, draw a rectangle to represent clone
			pG2d.setPaint(fill);
			pG2d.fill(shape);

			// add second color if two tone coloring is selected by user
			if (settings.getReadColorStyle() == DisplaySettings.COLOR_TWO_TONE) {
				fillInColors(pG2d, settings.getReadLowColor());
			}

			// only outline if outline color is not null (i.e. it is selected or
			// recomb or something)
			if (outline != null) {
				pG2d.setPaint(outline);
				pG2d.draw(shape);
			}
		} else {
			// if we are zoomed in enough, draw reads separately and connect
			// with a line

			// in this case, this.shape will be the connecting line
			if (outline != null) {
				// use outline color if it's set
				pG2d.setPaint(outline);
			} else {
				// otherwise use fill color
				pG2d.setPaint(fill);
			}
			// use draw (not fill) since the Shape is a Line
			pG2d.draw(shape);

			// only draw read if read is visible
			if (((Clone) mParent).reads[0].getDisplayGeometry().visible) {
				((ReadDisplayGeometry) ((Clone) mParent).reads[0]
						.getDisplayGeometry()).draw(pG2d, pData);
			}
			if (((Clone) mParent).reads[1].getDisplayGeometry().visible) {
				((ReadDisplayGeometry) ((Clone) mParent).reads[1]
						.getDisplayGeometry()).draw(pG2d, pData);
			}
		}

	}

	// public void drawDiffTicks(Graphics2D pG2d, boolean pDrawLetters) {
	// ((ReadDisplayGeometry)((Clone)mParent).reads[0].getDisplayGeometry()).drawDiffTicks(pG2d,
	// pDrawLetters);
	// ((ReadDisplayGeometry)((Clone)mParent).reads[1].getDisplayGeometry()).drawDiffTicks(pG2d,
	// pDrawLetters);
	// }

	/**
	 * set colors for Clone based on it's selection, recomb states
	 */
	public void setColors(DisplayData pData) {
		if (((Clone) mParent).isSelected()) {
			fill = READ_SELECT_FILL;
		} else if (settings.getReadColorStyle() == DisplaySettings.COLOR_TWO_TONE) {
			// color high color (low color will be draw on top)
			fill = settings.getReadHighColor();
		} else if (settings.getReadColorStyle() == DisplaySettings.COLOR_CONSTANT) {
			// color high color (low color will be draw on top)
			fill = settings.getReadConstantColor();
		} else {
			fill = getPctIDColor();
		}

		boolean inSelectedList = ((Clone) mParent).reads[0].inSelectedList
				&& ((Clone) mParent).reads[1].inSelectedList;

		if (((Clone) mParent).isRecombinant()) {
			if (inSelectedList) {
				outline = DisplaySettings
						.getDisplaySettings()
						.getRecombinantSelectColor();
			} else {
				outline = DisplaySettings
						.getDisplaySettings()
						.getRecombinantColor();
			}
		} else if (((Clone) mParent).isBadClone()) {
			if (inSelectedList) {
				outline = READ_SELECTED_BADCLONE_OUTLINE;
			} else {
				outline = READ_BADCLONE_OUTLINE;
			}
		} else {
			if (inSelectedList) {
				outline = READ_SELECTED_OUTLINE;
			} else {
				outline = READ_OUTLINE;
			}
		}
	}

	@Override
	protected Color getTintLowColor() {
		return settings.getReadTintLowColor();
	}

	@Override
	protected Color getTintHighColor() {
		return settings.getReadTintHighColor();
	}

	@Override
	protected int getTintLowCutoff() {
		return settings.getReadLowCutoffValue();
	}

	@Override
	public void setRow(int pRow) {
		super.setRow(pRow);
		((Clone) mParent).reads[0].getDisplayGeometry().setRow(pRow);
		if (getHeight() == 1) {
			((Clone) mParent).reads[1].getDisplayGeometry().setRow(pRow);
		} else {
			((Clone) mParent).reads[1].getDisplayGeometry().setRow(pRow + 1);
		}
	}

	/**
	 * Returns the Read (if any) that belongs to this Clone and encloses the
	 * point (x,y) on the display
	 * 
	 * @param x
	 * @param y
	 * @return a Read object or null
	 */
	public Read findIndicatedRead(int x, int y) {
		for (int i = 0; i < 2; i++) {
			Read read = ((Clone) mParent).reads[i];
			try {
				if (read.getDisplayGeometry().visible
						&& read.getDisplayGeometry().getBounds2D().contains(
								x,
								y)) {
					return read;
				}
			} catch (NullPointerException e) {
				System.out.println("Can't get bounds: " + read);
				e.printStackTrace();
			}
		}
		return null;
	}

}
