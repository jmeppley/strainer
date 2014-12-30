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
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import amd.strainer.display.util.Util;
import amd.strainer.objects.Clone;
import amd.strainer.objects.Readable;
import amd.strainer.objects.Strain;

/**
 * Contains display data and methods for a String object.
 * 
 * @author jmeppley
 * @see amd.strainer.objects.Strain
 */
public class StrainDisplayGeometry extends AlignedSequenceDisplayGeometry {

	private final HashMap<Integer, Readable> visibleReads = new HashMap<Integer, Readable>();

	/**
	 * @return Iterator over the Readables in the underlying Strain that are
	 *         currently visble
	 */
	public Iterator<Readable> getVisibleReadsIterator() {
		return visibleReads.values().iterator();
	}

	/**
	 * Clears the visible Read hash
	 */
	public void clearVisibleReads() {
		visibleReads.clear();
	}

	/**
	 * Adds the given Readable to the list of visible Readables
	 * 
	 * @param pRead
	 *            a Readable object
	 */
	public void addVisibleRead(Readable pRead) {
		visibleReads.put(pRead.getIdInteger(), pRead);
	}

	/**
	 * Creates the StrainDisplayGeometry object for the given Strain
	 * 
	 * @param pParent
	 *            Strain object to be displayed
	 */
	public StrainDisplayGeometry(Strain pParent) {
		mParent = pParent;
	}

	@Override
	public boolean update(DisplayData pData) {
		double x1 = getX(mParent.getStart(), pData);
		double x2 = getX(mParent.getEnd() + 1, pData);

		int y1 = getY(getRow(), pData);
		int h = getH(getHeight(), pData);

		visible = Util.rectInDisplay(x1, x2, y1, h, pData);

		if (!visible)
			return false;

		double w = x2 - x1;

		// if (!open)
		// System.out.println("height:" + height + ":" + h);

		shape = new Rectangle2D.Double(x1, y1, w, h);
		// TODO:6 It would be cool to show read depth somehow when the strain is
		// closed

		if (settings.getStrainColorStyle() == DisplaySettings.COLOR_TWO_TONE) {
			// this method sets up the necessary objects for coling the strain
			// two toned.
			setColoringArrays(pData, (int) x1, y1, (int) w, h, settings
					.getReadColorThreshold());
		}

		return true;
	}

	public void setColors(DisplayData pData) {
		// System.out.println("tint color is " + GRAYSCALE_TINT_COLOR);
		if (settings.getStrainColorStyle() == DisplaySettings.COLOR_TINT) {
			// color based on % id
			fill = getPctIDColor();
		} else if (settings.getStrainColorStyle() == DisplaySettings.COLOR_CONSTANT) {
			fill = settings.getStrainConstantColor();
		} else if (settings.getStrainColorStyle() == DisplaySettings.COLOR_RANDOM) {
			fill = getRandomColor();
		} else {
			fill = settings.getStrainHighColor();
		}

		if (((Strain) mParent).isSelected()) {
			outline = STRAIN_SELECT_OUTLINE;
		} else {
			outline = null;
		}

		recalcColor = false;
	}

	@Override
	public void draw(Graphics2D pG2d, DisplayData pData) {
		if (recalcColor) {
			setColors(pData);
		}

		pG2d.setPaint(fill);
		pG2d.fill(shape);
		if (settings.getStrainColorStyle() == DisplaySettings.COLOR_TWO_TONE) {
			fillInColors(pG2d, settings.getStrainLowColor());
		}

		if (outline != null) {
			pG2d.setPaint(outline);
			pG2d.draw(shape);
		}

		// draw reads
		Iterator<Readable> rit = getVisibleReadsIterator();
		while (rit.hasNext()) {
			Readable read = rit.next();
			((ReadableDisplayGeometry) read.getDisplayGeometry()).draw(
					pG2d,
					pData);
		}
	}

	@Override
	protected Color getTintLowColor() {
		return settings.getStrainTintLowColor();
	}

	@Override
	protected Color getTintHighColor() {
		return settings.getStrainTintHighColor();
	}

	@Override
	protected int getTintLowCutoff() {
		return settings.getStrainTintLowCutoffValue();
	}

	public Readable findIndicatedRead(int x, int y) {
		Iterator<Readable> rit = getVisibleReadsIterator();
		while (rit.hasNext()) {
			Readable read = rit.next();
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

	/**
	 * Get an iterator over reads that fall completely within the box.
	 * 
	 * @param pBox
	 *            Rectangle2D indicating search space
	 * @return Iterator that will loop over reads that are completely within
	 *         pBox. Objects in iterator are Readable (i.e. Read or Clone).
	 */
	public Iterator<Readable> getBoxedReadsIterator(Rectangle2D pBox) {
		return new BoxedReadsIterator(pBox);
	}

	private class BoxedReadsIterator implements Iterator<Readable> {
		// use visible reads iterator as the core of this object
		private final Iterator<Readable> rit = getVisibleReadsIterator();
		private Readable next = null;
		private Rectangle2D box = null;

		BoxedReadsIterator(Rectangle2D pBox) {
			box = pBox;
		}

		public void remove() {
			rit.remove();
		}

		public boolean hasNext() {
			// if this has already been called, the next read will be queued up
			// in this.next
			if (next != null) {
				return true;
			}

			// look through remaining visible reads for another read in the box
			while (rit.hasNext()) {
				Readable readable = rit.next();
				// see if it's visible (it should be) and it intersects the box
				if (readable.getDisplayGeometry().visible
						&& box.intersects(readable
								.getDisplayGeometry()
								.getBounds2D())) {
					try {
						if (box.contains(readable
								.getDisplayGeometry()
								.getBounds2D())) {
							// if it's completely in the box return the whole
							// thing
							next = readable;
							return true;
						} else {
							if (readable instanceof Clone) {
								// if it's a Clone, check individual reads
								// (if the whole thing isn't in the box, at most
								// only one of its reads can be)
								Clone clone = (Clone) readable;
								if (box.contains(clone.reads[0]
										.getDisplayGeometry()
										.getBounds2D())) {
									next = clone.reads[0];
									return true;
								}
								if (box.contains(clone.reads[1]
										.getDisplayGeometry()
										.getBounds2D())) {
									next = clone.reads[1];
									return true;
								}
							}
						}
					} catch (NullPointerException e) {
						System.out.println("Problem checking: " + readable);
						e.printStackTrace();
					}
				}
			}

			// if we've run out of reads, return false
			next = null;
			return false;
		}

		public Readable next() {
			if (hasNext()) {
				Readable r = next;
				next = null;
				return r;
			} else {
				throw new NoSuchElementException("No more boxed reads.");
			}
		}
	}
}
