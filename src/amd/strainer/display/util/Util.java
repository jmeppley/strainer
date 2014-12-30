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
package amd.strainer.display.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import amd.strainer.display.DisplayData;
import amd.strainer.display.DisplayGeometry;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.StrainDisplayGeometry;
import amd.strainer.objects.Gene;
import amd.strainer.objects.Read;
import amd.strainer.objects.Strain;

/**
 * A collection of public static methods for displaying objects and creating
 * shapes.
 * 
 * @author jmeppley
 * 
 */
public class Util {
	/**
	 * Create the shape to draw a line from a Read to its mate pair.
	 * 
	 * @param pData
	 *            the info about the current stae of the display (DisaplayData)
	 * @param pRead
	 *            the Read to connect
	 * @return a java.awt.Shape object connecting two reads. Null, if pRead has
	 *         no matepair
	 */
	public static Shape createRecombinantSpan(DisplayData pData, Read pRead) {
		if (pRead.getMatePair() == null) {
			return null;
		}

		// if (!pRead.getMatePair().strain.getDisplayGeometry().visible) {}
		//
		Strain mpStrain = pRead.getMatePair().getStrain();
		StrainDisplayGeometry mpsdg = (StrainDisplayGeometry) mpStrain
				.getDisplayGeometry();
		StrainDisplayGeometry rsdg = (StrainDisplayGeometry) pRead
				.getStrain()
				.getDisplayGeometry();

		double x1, x2, y1, y2;

		if (pRead.getStart() > pRead.getMatePair().getStart()) {
			x1 = DisplayGeometry.getX(pRead.getStart(), pData);
			x2 = DisplayGeometry.getX(pRead.getMatePair().getEnd(), pData);
		} else {
			x1 = DisplayGeometry.getX(pRead.getEnd(), pData);
			x2 = DisplayGeometry.getX(pRead.getMatePair().getStart(), pData);
		}

		if (pRead.getStrain().isOpen()) {
			y1 = pData.rowHeight
					/ 2
					+ DisplayGeometry.getY(rsdg.getRow()
							+ pRead.getDisplayGeometry().getRow(), pData);
		} else {
			y1 = pData.rowHeight / 2
					+ DisplayGeometry.getY(rsdg.getRow(), pData);
		}
		if (mpStrain.isOpen()) {
			y2 = pData.rowHeight
					/ 2
					+ DisplayGeometry.getY(
							mpsdg.getRow()
									+ pRead
											.getMatePair()
											.getDisplayGeometry()
											.getRow(),
							pData);
		} else {
			y2 = pData.rowHeight / 2
					+ DisplayGeometry.getY(mpsdg.getRow(), pData);
		}

		return new Line2D.Double(x1, y1, x2, y2);
	}

	/**
	 * Checks if if the given rectangle is displayed in the scrollpane
	 * 
	 * @param pX1
	 *            start position
	 * @param pX2
	 *            end position
	 * @param pY1
	 *            vertical position
	 * @param pH
	 *            height
	 * @param pData
	 *            DisplayData object with current state
	 * @return true if any part of the rectangle will be displayed
	 */
	public static boolean rectInDisplay(double pX1, double pX2, double pY1,
			double pH, DisplayData pData) {
		if (pX1 > pX2 || pH < 0) {
			System.err.println("Warning: negative width or height!");
		}

		// return false if no part of image displayed

		if (pX2 <= 0) {
			// whole thing is off to the left
			return false;
		}
		if (pX1 >= pData.getWidth()) {
			// whole thing is off to the right
			return false;
		}
		double[] vertRange = pData.getVerticalVisibleRange();
		if (vertRange == null) {
			return false;
		}
		if (pY1 >= vertRange[1]) {
			// whole thing is too high
			return false;
		}
		double y2 = pY1 + pH;
		if (y2 <= vertRange[0]) {
			// whole thing is too low
			return false;
		}

		// if we get here, then some part of the rectangle is on screen
		return true;
	}

	/**
	 * Create a Shape to draw the bar at the top of the display wich represents
	 * the reference sequence.
	 * 
	 * @param pData
	 *            the DisplayData object containing the current state
	 */
	public static void createReferenceSequenceShapes(DisplayData pData) {
		int eX = pData.border;
		int eW = pData.getWidth() - 2 * pData.border;
		int eY = pData.border;
		int eH = pData.rowHeight;
		pData.entireRefSeq = new Rectangle2D.Double(eX, eY, eW, eH);

		// pData.referenceSequenceGenes.clear();
		// for (Gene gene : pData.referenceSequence.genes.values()) {
		// int gX = pData.border +
		// (int)(((gene.getStart() - 1)) *
		// pData.refSeqScaleFactor);
		// int gW =
		// (int)(((gene.getEnd()-gene.getStart())) *
		// pData.refSeqScaleFactor);
		// pData.referenceSequenceGenes.add(new
		// Rectangle2D.Double(gX,eY,gW,eH));
		// }
	}

	/**
	 * Creates the box that overlays the ReferenceSequence shape to indicate
	 * what portion is currently displayed
	 * 
	 * @param pData
	 *            the DisplayData object containing the current state
	 */
	public static void createRegionShape(DisplayData pData) {
		int rX = pData.border
				+ (int) ((pData.getStart() - 1) * pData.refSeqScaleFactor);
		int rW = (int) (pData.getSize() * pData.refSeqScaleFactor);
		int rY = pData.border - pData.rowSpacing;
		int rH = pData.rowHeight + pData.rowSpacing * 2;
		pData.displayRegion = new Rectangle2D.Double(rX, rY, rW, rH);
	}

	/**
	 * Pop up an error window
	 * 
	 * @param pParent
	 *            Component to attach message to
	 * @param e
	 *            exception to display
	 */
	public static void displayErrorMessage(Component pParent, Exception e) {
		displayErrorMessage(pParent, e.getClass().getName(), e.getMessage());
	}

	/**
	 * Pop up an error window
	 * 
	 * @param e
	 *            exception to display
	 */
	public static void displayErrorMessage(Exception e) {
		displayErrorMessage(PaneledReferenceSequenceDisplay.frame, e);
	}

	/**
	 * Pop up an error window
	 * 
	 * @param pTitle
	 *            title of error message window
	 * @param pMsg
	 *            text of pop up window
	 */
	public static void displayErrorMessage(String pTitle, String pMsg) {
		displayErrorMessage(PaneledReferenceSequenceDisplay.frame, pTitle, pMsg);
	}

	/**
	 * Pop up an error window
	 * 
	 * @param pParent
	 *            Component to attach message to
	 * @param pMsg
	 *            message to display
	 */
	public static void displayErrorMessage(Component pParent, String pMsg) {
		displayErrorMessage(pParent, "Error", pMsg);
	}

	/**
	 * Pop up an error window
	 * 
	 * @param pParent
	 *            Component to attach message to
	 * @param pTitle
	 *            title of error message window
	 * @param pMsg
	 *            message to display
	 */
	public static void displayErrorMessage(Component pParent, String pTitle,
			String pMsg) {
		JOptionPane.showMessageDialog(
				pParent,
				pMsg,
				pTitle,
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * calculate the total height needed to display everything
	 * 
	 * @param pData
	 *            the DisplayData object containing the current state
	 * @return the number of pixels (vertically) needed to display all the reads
	 *         and such
	 */
	public static int getTotalHeight(DisplayData pData) {
		return pData.border + pData.refSeqAreaHeight + pData.geneAreaHeight
				+ pData.rowHeight * pData.totalRows + pData.rowSpacing
				* (pData.totalRows - 1);
	}

	// /**
	// * Create a shape to draw highlight the area around a selected gene
	// * @param pGene the selected gene
	// * @param pData the DisplayData object containing the current state
	// * @return a java.awt.Shape
	// */
	// public static Shape createTopGeneWindow(Gene pGene,DisplayData pData) {
	// int x = pData.border +
	// (int)(((pGene.getStart() - 1)) *
	// pData.refSeqScaleFactor);
	// int w =
	// (int)(((pGene.getEnd()-pGene.getStart())) *
	// pData.refSeqScaleFactor);
	// int y = 0;
	// int h = pData.refSeqAreaHeight - pData.rowSpacing;
	// return new Rectangle2D.Double(x, y, w, h);
	// }

	/**
	 * Create a shape to highlight the area corresponding to the selected gene
	 * but displayed over the reads
	 * 
	 * @param pGene
	 *            the selected Gene
	 * @param pData
	 *            the DisplayData object containing the current state
	 * @return a java.awt.Shape
	 */
	public static Shape createBottomGeneWindow(Gene pGene, DisplayData pData) {
		DisplayGeometry gdg = pGene.getDisplayGeometry();
		if (gdg.visible) {
			double x = pGene.getDisplayGeometry().shape.getBounds2D().getX();
			double w = pGene.getDisplayGeometry().shape
					.getBounds2D()
					.getWidth();
			int y = pData.refSeqAreaHeight;
			int h = pData.height - y;
			return new Rectangle2D.Double(x, y, w, h);
		} else {
			return null;
		}
	}

	/**
	 * @return a random color
	 */
	public static Color pickRandomColor() {
		double value = Math.random() * 3.0;
		return pickColor(0.0, 1.0, 2.0, 3.0, value, true);
	}

	private static Color pickColor(double t1, double t2, double t3, double t4,
			double value, boolean randomShade) {
		if (randomShade) {
			// set max between 128 and 255
			int max = (int) (128.0 + Math.random() * (255.0 - 128.0));
			// set min between 0 and 1/2 max
			int min = (int) (Math.random() * max / 2.0);
			return pickColor(t1, t2, t3, t4, value, max, min);
		} else {
			return pickColor(t1, t2, t3, t4, value, 255, 0);
		}
	}

	private static Color pickColor(double t1, double t2, double t3, double t4,
			double value, int maxVal, int minVal) {

		double range = (double) maxVal - minVal;
		int red = minVal;
		int blue = minVal;
		int green = minVal;

		if (value <= t1) {
			blue = maxVal;
		} else if (value < t2) {
			green += (int) (range * (value - t1) / (t2 - t1));
			blue += (int) (range * (t2 - value) / (t2 - t1));
		} else if (value < t3) {
			red += (int) (range * (value - t2) / (t3 - t2));
			green = maxVal;
		} else if (value < t4) {
			red = maxVal;
			green += (int) (range * (t4 - value) / (t4 - t3));
		} else {
			red = maxVal;
		}

		return new Color(blue + green * 256 + red * 65536);
	}

	// private static Color pickGrayscale(double pMin, double pMax, double
	// value) {
	// double range = pMax - pMin;
	// value = Math.max(Math.min(value,pMax),pMin); // clip to range
	// value = value - pMin; // start at 0
	// value = value/range; // turn into a scale factor
	// value = value * 255; // put in terms of 0-255
	// int val = (int) value; // cast to int
	//		
	// // scale is dropped from 255 to 225 and 30 ius added to blue
	// // to give everything a blue tint
	// return new Color(val + val*256 + val*65536);
	// }

	/**
	 * Picks a color based on the indicated min,max, and value params. The
	 * returned color is adjusted based on the tint parameters.
	 * 
	 * @param pMin
	 *            Value below which the returned color will be darkest
	 * @param pMax
	 *            Value above which the color will be lightest
	 * @param value
	 *            The value that determines the returned color
	 * @param pLowColor
	 *            Color darkest
	 * @param pHighColor
	 *            Color lightest
	 * 
	 * @return java.awt.Color object
	 */
	public static Color pickTintedColor(double pMin, double pMax, double value,
			Color pLowColor, Color pHighColor) {
		double range = pMax - pMin;
		value = Math.max(Math.min(value, pMax), pMin); // clip to range
		value = value - pMin; // start at 0
		value = value / range; // turn into a scale factor

		// get range between low and high in each color channel
		int blue = pHighColor.getBlue() - pLowColor.getBlue();
		int red = pHighColor.getRed() - pLowColor.getRed();
		int green = pHighColor.getGreen() - pLowColor.getGreen();

		// scale color channel ranges by value and add to low
		blue = pLowColor.getBlue() + (int) (blue * value);
		red = pLowColor.getRed() + (int) (red * value);
		green = pLowColor.getGreen() + (int) (green * value);

		// create new color

		return new Color(red, green, blue);
	}

	/**
	 * Taken verabatim from java.sun.com online tutorial for using the
	 * SpringLayout to make a grid
	 * 
	 * @param jMainGridPanel
	 *            panel to lay elemnts out into
	 * @param layout
	 *            the SpringLayour for the panel
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @param initialX
	 *            starting x position
	 * @param initialY
	 *            starting y position
	 * @param xPad
	 *            horizonatal padding between cells
	 * @param yPad
	 *            vertical padding between cells
	 * @author 
	 *         http://java.sun.com/docs/books/tutorial/uiswing/layout/spring.html
	 */
	public static void makeCompactGrid(JPanel jMainGridPanel,
			SpringLayout layout, int rows, int cols, int initialX,
			int initialY, int xPad, int yPad) {
		// Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width, layout.getConstraints(
						jMainGridPanel.getComponent(r * cols + c)).getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints = layout
						.getConstraints(jMainGridPanel.getComponent(r * cols
								+ c));
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		// Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height, layout.getConstraints(
						jMainGridPanel.getComponent(r * cols + c)).getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints = layout
						.getConstraints(jMainGridPanel.getComponent(r * cols
								+ c));
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		// Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(jMainGridPanel);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}

}
