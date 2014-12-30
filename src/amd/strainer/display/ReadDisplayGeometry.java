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
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import amd.strainer.display.util.DiffLetterInfo;
import amd.strainer.display.util.Util;
import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Difference;
import amd.strainer.objects.Read;

/**
 * Contains display data and methods for a Read object.
 * 
 * @author jmeppley
 * @see amd.strainer.objects.Read
 */
public class ReadDisplayGeometry extends ReadableDisplayGeometry {
	public final int height = 1;

	private HashMap<Shape, Color> ticks = new HashMap<Shape, Color>();

	public HashMap<Shape, Color> getTicks() {
		return ticks;
	}

	public void setTicks(HashMap<Shape, Color> pTicks) {
		ticks = pTicks;
	}

	private HashMap<Shape, Color> gaps = new HashMap<Shape, Color>();

	public HashMap<Shape, Color> getGaps() {
		return gaps;
	}

	public void setGaps(HashMap<Shape, Color> pGaps) {
		gaps = pGaps;
	}

	private HashSet<DiffLetterInfo> mLetters = new HashSet<DiffLetterInfo>();

	public HashSet<DiffLetterInfo> getLetters() {
		return mLetters;
	}

	public void setLetters(HashSet<DiffLetterInfo> pLetters) {
		mLetters = pLetters;
	}

	/**
	 * Creates the ReadDisplayGeometry object for the given read
	 * 
	 * @param pParent
	 *            Read object to be displayed
	 */
	public ReadDisplayGeometry(Read pParent) {
		mParent = pParent;
	}

	@Override
	public boolean update(DisplayData pData) {
		return update(pData, false);
	}

	public boolean update(DisplayData pData, boolean pAlwaysMakeShape) {

		Read r = (Read) mParent;

		double x1 = getX(r.getStart(), pData);
		double x2 = getX(r.getEnd() + 1, pData);

		int row = this.getRow() + r.getStrain().getDisplayGeometry().getRow();
		int y1 = getY(row, pData);
		int h = getH(height, pData);

		visible = Util.rectInDisplay(x1, x2, y1, h, pData);

		if (!visible && !pAlwaysMakeShape) {
			return false;
		}

		double w = x2 - x1;
		double[] x, y;

		if (r.getAlignment().isForward()) {
			if (w <= h) {
				x = new double[3];
				y = new double[3];
				x[0] = x1;
				y[0] = y1;
				x[1] = x[0];
				y[1] = y[0] + h;
				x[2] = x2;
				y[2] = (y[0] + y[1]) / 2;
			} else {
				x = new double[5];
				y = new double[5];
				x[0] = x1;
				y[0] = y1;
				x[1] = x[0];
				y[1] = y[0] + h;
				x[2] = x2 - (double) h / 2;
				y[2] = y[1];
				x[3] = x2;
				y[3] = (y[0] + y[1]) / 2;
				x[4] = x[2];
				y[4] = y[0];
			}
		} else {
			if (w <= h) {
				x = new double[3];
				y = new double[3];
				x[0] = x2;
				y[0] = y1;
				x[1] = x[0];
				y[1] = y[0] + h;
				x[2] = x1;
				y[2] = (y[0] + y[1]) / 2;
			} else {
				x = new double[5];
				y = new double[5];
				x[0] = x2;
				y[0] = y1;
				x[1] = x[0];
				y[1] = y[0] + h;
				x[2] = x1 + (double) h / 2;
				y[2] = y[1];
				x[3] = x1;
				y[3] = (y[0] + y[1]) / 2;
				x[4] = x[2];
				y[4] = y[0];
			}
		}
		shape = getGeneralPath(x, y);

		// Create objects (little rectangles) to draw diffs over read polygon
		if (pData.drawDiffTicks) {
			// clear graphic object collections
			HashMap<Shape, Color> ticks = getTicks();
			ticks.clear();
			HashMap<Shape, Color> gaps = getGaps();
			gaps.clear();
			HashSet<DiffLetterInfo> letters = getLetters();
			letters.clear();

			// get the width of a tick rectangle
			double width = pData.tickWidth;

			// loop over differences
			List<Difference> diffs = ((AlignedSequence) mParent)
					.getAlignment()
					.getDiffs();
			for (Difference diff : diffs) {
				int refSeqPos = diff.getPosition1();
				if (refSeqPos >= pData.getStart() && refSeqPos < pData.getEnd()) {
					x1 = getX(refSeqPos, pData);
					if (diff.getBase1() == '-') {
						// INSERTIONS:
						// if reference has a gap, then draw half a tick
						gaps.put(
								new Rectangle2D.Double(x1, y1, width, h / 2),
								pData.baseColors.get(Character.toLowerCase(diff
										.getBase2())));
						if (pData.drawDiffLetters) {
							double letterX = x1
									+ (width - pData.diffLetterCutoff) / 2.0;
							double letterY = y1 + h / 2.0;
							letters.add(new DiffLetterInfo(
									Character.toUpperCase(diff.getBase2()),
									letterX,
									letterY));
						}
					} else {
						// DIFFS:
						ticks.put(
								new Rectangle2D.Double(x1, y1, width, h),
								pData.baseColors.get(Character.toLowerCase(diff
										.getBase2())));
						if (pData.drawDiffLetters
								&& !settings.isDrawAllLetters()) {
							double letterX = x1
									+ (width - pData.diffLetterCutoff) / 2;
							letters.add(new DiffLetterInfo(Character
									.toUpperCase(diff.getBase2()), letterX, y1
									+ h - pData.rowHeight / 8.0));
						}
					}
				}
			}

			if (settings.isDrawAllLetters() && pData.drawDiffLetters) {
				int start = Math.max(pData.getStart(), mParent.getStart());
				int end = Math.min(pData.getEnd(), mParent.getEnd());
				for (int pos = start; pos < end; pos++) {
					x1 = getX(pos, pData);
					double letterX = width > pData.diffLetterCutoff ? x1
							+ (width - pData.diffLetterCutoff) / 2 : x1;
					char letter = ((AlignedSequence) mParent)
							.getAlignment()
							.getBaseFromReference(pos);
					letters.add(new DiffLetterInfo(Character
							.toUpperCase(letter), letterX, y1 + h
							- pData.rowHeight / 8));
				}
			}

			setTicks(ticks);
			setGaps(gaps);
			setLetters(letters);

		} else {
			if (settings.getReadColorStyle() == DisplaySettings.COLOR_TWO_TONE) {
				setColoringArrays(pData, (int) x1, y1, (int) w, h, settings
						.getReadColorThreshold());
			}
		}

		return true;
	}

	public void updateMatePairCarat(DisplayData pData) {
		double width = (double) (mParent.getStart() - mParent.getEnd()) / 2
				* pData.refSeqScaleFactor;

		double[] x = new double[3];
		double[] y = new double[3];
		x[0] = pData.border + (double) (mParent.getStart() + mParent.getEnd())
				/ 2 * pData.refSeqScaleFactor;
		y[0] = pData.border;
		x[1] = x[0] - width;
		y[1] = 1;
		x[2] = x[0] + width;
		y[2] = 1;
		shape = getGeneralPath(x, y);

		// there should be no ticks drawn
		getTicks().clear();
		getGaps().clear();
	}

	private void drawDiffTicks(Graphics2D pG2d, DisplayData pData) {
		// draw ticks
		for (Map.Entry<Shape, Color> entry : getTicks().entrySet()) {
			pG2d.setPaint(entry.getValue());
			pG2d.fill(entry.getKey());
		}

		// draw gap ticks last to make sure they show up ontop of any ticks in
		// the same position
		for (Map.Entry<Shape, Color> entry : getGaps().entrySet()) {
			pG2d.setPaint(entry.getValue());
			pG2d.fill(entry.getKey());
		}

		if (pData.drawDiffLetters) {
			// draw letters over ticks
			pG2d
					.setColor(DisplaySettings
							.getDisplaySettings()
							.getLetterColor());
			pG2d.setFont(pData.getLetterFont());

			for (DiffLetterInfo letter : getLetters()) {
				pG2d.drawString(
						String.valueOf(letter.getLetter()),
						(float) letter.getX(),
						(float) letter.getY());
			}
		}
	}

	@Override
	public void draw(Graphics2D pG2d, DisplayData pData) {
		if (recalcColor) {
			setColors(pData);
		}

		pG2d.setPaint(fill);
		pG2d.fill(shape);

		// draw difference tick marks
		if (visible) {
			if (pData.drawDiffTicks) {
				drawDiffTicks(pG2d, pData);
			} else {
				if (settings.getReadColorStyle() == DisplaySettings.COLOR_TWO_TONE) {
					fillInColors(pG2d, settings.getReadLowColor());
				}
			}
		}

		if (outline != null) {
			pG2d.setPaint(outline);
			pG2d.draw(shape);
		}
	}

	public void setColors(DisplayData pData) {
		Read read = (Read) mParent;
		// only set colors if we are not zoomed out so far that we're not
		// drawing reads
		if (!pData.hideCloneContents || read.isInClone()) {
			if (read.getMatePair() != null && read.getMatePair().isSelected()) {
				// color appropriately if mate pair is selected
				fill = READ_SELECTED_MATEPAIR_FILL;
			} else if (read.isSelected()) {
				// color blue if this read is selected
				fill = READ_SELECT_FILL;
			} else if (read.isInClone() && read.getClone().isSelected()) {
				// color blue if this read's enclosing Clone is selected
				fill = READ_SELECT_FILL;
			} else if (pData.drawDiffTicks) {
				// color white if we are drawing ticks
				fill = READ_FILL;
			} else if (settings.getReadColorStyle() == DisplaySettings.COLOR_TWO_TONE) {
				// color high color (low color will be draw on top)
				fill = settings.getReadHighColor();
			} else if (settings.getReadColorStyle() == DisplaySettings.COLOR_CONSTANT) {
				// color all reads a single color
				fill = settings.getReadConstantColor();
			} else if (settings.getReadColorStyle() == DisplaySettings.COLOR_RANDOM) {
				// random
				fill = getRandomColor();
			} else {
				// color based on % identity
				fill = getPctIDColor();
			}

			if (read.isRecombinant()) {
				if (read.inSelectedList) {
					outline = DisplaySettings
							.getDisplaySettings()
							.getRecombinantSelectColor();
				} else {
					outline = DisplaySettings
							.getDisplaySettings()
							.getRecombinantColor();
				}
			} else if (read.isBadClone()) {
				if (read.inSelectedList) {
					outline = READ_SELECTED_BADCLONE_OUTLINE;
				} else {
					outline = READ_BADCLONE_OUTLINE;
				}
			} else {
				if (read.inSelectedList) {
					outline = READ_SELECTED_OUTLINE;
				} else {
					outline = READ_OUTLINE;
				}
			}
		}

		recalcColor = false;
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

}
