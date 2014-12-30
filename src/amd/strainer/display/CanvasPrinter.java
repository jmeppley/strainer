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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.RepaintManager;

/**
 * Class for generating a jrint job from an ReferenceSequenceDisplayComponent object
 * 
 * @author jmeppley
 *
 */
public class CanvasPrinter implements Printable {
	
	private ReferenceSequenceDisplayComponent mCanvas;
	private boolean printDiffs = true;
	private boolean landscape = false;
	private boolean customPaper = true;
	public boolean isPrintDiffs() {
		return printDiffs;
	}

	/**
	 * Set to true to override the DisplayData.displayDiffTicks settings.  I.E. if the display
	 * is zoomed out too far to see the individual SNP colors, setting this to true will cause 
	 * the colored ticks to print anyway.
	 * @param pPrintDiffs
	 */
	public void setPrintDiffs(boolean pPrintDiffs) {
		printDiffs = pPrintDiffs;
	}

	/**
	 * Static way to access this calsses functionality.  Instantiates a new object of this type and
	 * calls print().
	 *	
	 * @param c An EntryDisplayComponent object to be printed
	 */
	public static void printComponent(ReferenceSequenceDisplayComponent c) {
		new CanvasPrinter(c).print();
	}
	
	/**
	 * @param pCanvas The canvas to be printed
	 */
	public CanvasPrinter(ReferenceSequenceDisplayComponent pCanvas) {
		this.mCanvas = pCanvas;
	}
	
	/**
	 * created a print job for the canvas and sends it to the print system
	 */
	public void print() {
		// create a new print job
		PrinterJob printJob = PrinterJob.getPrinterJob();
		PageFormat p = printJob.defaultPage();
		
		if (landscape) {
			// print in landscape mode
			p.setOrientation(PageFormat.LANDSCAPE);
		}
		
		if (customPaper) {
			// set custom paper size (this seems to be non-functioning)
			Paper paper = new Paper();
			paper.setSize(mCanvas.getWidth(),mCanvas.getHeight());
			paper.setImageableArea(0,0,mCanvas.getWidth(),mCanvas.getHeight());
			p.setPaper(paper);
		}
		
		// tell the print job to use this as its data source
		printJob.setPrintable(this,p);
		
		// open the print dialog
		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch(PrinterException pe) {
				System.out.println("Error printing: " + pe);
			}
		}
	}
	
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		if (pageIndex > 0) {
			// this implementation is simplistic and will only print one page
			return(NO_SUCH_PAGE);
		} else {
			Graphics2D g2d = (Graphics2D)g;
			
//			System.out.println(pageFormat.getImageableX() + "x" + pageFormat.getImageableY() +" - " +
//					pageFormat.getImageableWidth() + "x" + pageFormat.getImageableHeight());
			
			if (!customPaper) {
				// scale image so current view fits onto page
				g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
				double scaleX = (pageFormat.getImageableWidth())/mCanvas.getWidth(); 
				double scaleY = (pageFormat.getImageableHeight())/mCanvas.getHeight(); 
				// keep the aspect ratio
				double scale = Math.min(scaleX,scaleY);
				g2d.transform(AffineTransform.getScaleInstance(scale,scale));
			}

//			System.out.println(g2d.getTransform());
			
			disableDoubleBuffering(mCanvas);
			boolean realDiffs = mCanvas.dData.drawDiffTicks;
			if (!realDiffs && isPrintDiffs()) {
				// override tick setting
				mCanvas.dData.drawDiffTicks = true;
				mCanvas.recalcShapes = true;
			}
			// make sure objects are adjusted to paper settings	
			mCanvas.doNecessaryCalculations();
			// draw the canvas to the printer
			mCanvas.paint(g2d);

			// reset modified settings
			mCanvas.dData.drawDiffTicks = realDiffs;
			mCanvas.recalcShapes = true;
			enableDoubleBuffering(mCanvas);

			return(PAGE_EXISTS);
		}
	}
	
	public static void disableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(false);
	}
	
	public static void enableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(true);
	}

	public boolean isLandscape() {
		return landscape;
	}

	public void setLandscape(boolean landscape) {
		this.landscape = landscape;
	}

	public boolean isCustomPaper() {
		return customPaper;
	}

	public void setCustomPaper(boolean customPaper) {
		this.customPaper = customPaper;
	}
	
}
