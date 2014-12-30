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
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import amd.strainer.display.util.Util;
import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Clone;
import amd.strainer.objects.Gene;
import amd.strainer.objects.Read;
import amd.strainer.objects.Readable;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.SequenceFragment;
import amd.strainer.objects.Strain;

/**
 * Stores info about the current display state
 * 
 * @author jmeppley
 */
public class DisplayData {
	/** the ReferenceSeqence being displayed */
	public ReferenceSequence referenceSequence = null;

	/** The subset of strains that actually appear onscreen */
	public HashSet<Strain> visibleStrains = new HashSet<Strain>();
	/** The subset of genes that actually appear on screen */
	public HashSet<Gene> visibleGenes = new HashSet<Gene>();
	/**
	 * A map used to create lines between recombinant reads assigned to
	 * different strains. Initially it is a map of Clone ID's to reads, the
	 * reads are later replaced with Shape objects of the lines to be drawn.
	 */
	public HashMap<Integer, Object> splitRecombinantsMap = new HashMap<Integer, Object>();
	/** The total number of rows needed to draw everything (NOTE: rows!=pixels) */
	public int totalRows = 0;

	/**
	 * contains last few operations in case UNDO requested
	 */
	public UndoData undoData = null;

	/**
	 * Do we need to draw a "matepair carat"? IE. Is the selected object a solo
	 * read and is it's mate pair offscreen?
	 */
	public boolean matePairCarat = false;

	// what region is being displayed
	/** the first base shown onscreen */
	private int start = -1;
	/** the last base shown onscreen */
	private int end = -1;

	public int getEnd() {
		return end;
	}

	public int getStart() {
		return start;
	}

	/* the number of bases shown onscreen */
	private int size = -1;

	/** the number of bases shown onscreen */
	public int getSize() {
		return size;
	}

	/** adjust the number of bases shown onscreen */
	private void setSize(int pSize) {
		size = pSize;

		boolean oldDDT = drawDiffTicks;
		calculateScaleFactor();

		// if we have switched from showing diff ticks to not (or vice versa)
		// make sure read
		// background colors are up to date
		if (oldDDT != drawDiffTicks) {
			resetReadColors();
		}
	}

	/**
	 * loop over reads and make sure background colors are current (calls
	 * .recalcColors())
	 */
	public void resetReadColors() {
		for (Read r : referenceSequence.reads.values()) {
			r.recalcColors();
		}
	}

	/**
	 * loop over all strains and enclosed reads and make sure colors are current
	 * (calls .recalcColors())
	 */
	public void resetColors() {
		for (Strain s : referenceSequence.strains.values()) {
			s.recalcColors();
			Iterator rit = s.getReadableIterator();
			while (rit.hasNext()) {
				Readable r = (Readable) rit.next();
				r.recalcColors();
				if (r instanceof Clone) {
					Clone clone = (Clone) r;
					clone.reads[0].recalcColors();
					clone.reads[1].recalcColors();
				}
			}
		}
	}

	// graphics settings
	/** number of pixels wide the canvas is */
	private int width = 800;

	/** number of pixels wide the canvas is */
	public int getWidth() {
		return width;
	}

	/** number of pixels wide the canvas is */
	public void setWidth(int pW) {
		width = pW;
		calculateScaleFactor();
		calculateRefSeqScaleFactor();
	}

	/** number of pixels high the canvas is */
	public int height = 400;
	/** number of pixels used as border on all sides */
	public int border = 10;

	// vertical spacing settings
	/* an array of possible configurations */
	private final ArrayList<VerticalSpacingSet> availableVerticalSpacings = new ArrayList<VerticalSpacingSet>();
	/** The current vertical spacing configuration (bigger/smaller rows) */
	protected int currentVerticalSpacing = 0;
	// the following are the components of each vertical spacing configuration
	/**
	 * total height of area containing the entire ref seq bar with genes,
	 * position box, and matepair carat
	 */
	public int refSeqAreaHeight = 25;
	/** total height of area containing zoomed-in genes */
	public int geneAreaHeight = 15;
	/** hiegth of row containing genes */
	public int geneHeight = 8;
	/** height of each row containing reads */
	public int rowHeight = 6;
	/** space between rows */
	public int rowSpacing = 3;

	/**
	 * calculated conversion between bases and pixels in zoomed region showing
	 * reads
	 */
	public double scaleFactor = 1;
	/** width of a tick */
	public double tickWidth = 1.2;
	/**
	 * calculated conversion between bases and pixels in reference seq region
	 * showing whole reference
	 */
	public double refSeqScaleFactor = 1;
	/** true if zoomed in enough to show individual SNPs */
	public boolean drawDiffTicks = false;
	/** value of scaleFactor at which drawDiffTicks changes */
	public final double diffTickCutoff = .5;
	/**
	 * If true, letters will be drain in ticks, automatically set when zoom is
	 * adjusted
	 */
	public boolean drawDiffLetters = false;
	/** diff width at which we write letters over them, =.8 * rowHeight. */
	public double diffLetterCutoff = 0.8 * rowHeight;
	// TODO:3 .8 is a rough number, ideally should be calc'ed from font metrics
	/** true if scale factor is too large to draw separate reads */
	public boolean hideCloneContents = false;

	// selection pointers
	/** the currently selected object (Strain or read) */
	public AlignedSequence selectedObject = null;
	/** the currently selected gene */
	public Gene selectedGene = null;
	/**
	 * list of all reads that have been selected sin the last time clear was
	 * pressed
	 */
	public Strain selectedReadList = new Strain();

	// position indicator shapes
	/** The bar at top representing the reference sequence */
	public Rectangle2D entireRefSeq = null;
	/**
	 * the red rectangle that indicates where on the entireRefSeq the zoomed
	 * region is now
	 */
	public Rectangle2D displayRegion = null;

	// Some constants
	/** a mapping between base and diffTick color */
	public HashMap<Character, Color> baseColors = new HashMap<Character, Color>(
			7);

	/**
	 * Each DisplayData incarnation is linked to a
	 * canvas(ReferenceSequenceDisplayComponent) and a reference sequence
	 */
	public DisplayData(ReferenceSequenceDisplayComponent pComponent,
			ReferenceSequence pRefSeq) {
		System.out.println("Canvas has size: "
				+ pComponent.getSize().toString());

		referenceSequence = pRefSeq;
		undoData = new UndoData(pRefSeq);

		start = 1;
		end = pRefSeq.getLength();
		size = end - start + 1;

		setWidth((int) pComponent.getSize().getWidth());
		System.out.println("Setting data width to " + width);

		// TODO:5 the next few things should be done in some global
		// initialization and not called every time
		// new reference seq is loaded, but it's rare enough that this is low
		// priority

		// make sure selectedReadList behaves properly
		selectedReadList.stealReads = false;

		// create vertical spacing options
		initializeVerticalSpacings();
		// create array of diff tick colors
		initializeBaseColors();
	}

	/*
	 * sets hash of SNP colors
	 */
	private void initializeBaseColors() {
		// initialize base color hash
		baseColors.put(new Character('a'), new Color(0, 150, 255)); // blue-blue-gree
		baseColors.put(new Character('c'), Color.RED);
		baseColors.put(new Character('t'), Color.magenta);
		baseColors.put(new Character('g'), Color.green);
		baseColors.put(new Character('-'), Color.black);

		setBaseColorForNs();
	}

	/**
	 * changes the color used for difference ticks when the base is 'n'
	 * (unknown). If the showNs variable is true, the color is Color.lightGray,
	 * otherwise, the color is set to be completely transparent
	 * 
	 * @see DisplaySettings#getShowNs()
	 * @see Color#lightGray
	 */
	public void setBaseColorForNs() {
		Color c;
		if (DisplaySettings.getDisplaySettings().getShowNs()) {
			c = Color.lightGray;
		} else {
			// completely clear
			c = new Color(0, 0, 0, 255);
		}
		baseColors.put(new Character('n'), c);
	}

	/*
	 * sets up the different configurations for vertical spacings
	 * (bigger/smaller rows)
	 */
	private void initializeVerticalSpacings() {
		availableVerticalSpacings.add(new VerticalSpacingSet(17, 10, 4, 2, 1));
		availableVerticalSpacings.add(new VerticalSpacingSet(21, 13, 6, 4, 2));
		availableVerticalSpacings.add(new VerticalSpacingSet(25, 15, 8, 6, 3));
		availableVerticalSpacings.add(new VerticalSpacingSet(30, 20, 10, 8, 5));
		availableVerticalSpacings
				.add(new VerticalSpacingSet(35, 25, 12, 10, 7));
		currentVerticalSpacing = 3;
		setVerticalSpacing(currentVerticalSpacing);
	}

	/** calculates how many pixels (what fraction) represent a base. */
	public void calculateScaleFactor() {
		// System.out.println("new scale factor");
		scaleFactor = (double) (width - 2 * border) / (double) size;
		drawDiffTicks = scaleFactor >= diffTickCutoff;
		hideCloneContents = scaleFactor <= 0.002;

		tickWidth = scaleFactor + .2;
		resetLetterDrawingVariables();
	}

	/**
	 * calculates how many pixels (what fraction) represent a base for the
	 * reference sequence bar on the top of the window.
	 */
	public void calculateRefSeqScaleFactor() {
		refSeqScaleFactor = (double) (width - 2 * border)
				/ (double) referenceSequence.getLength();
	}

	@Override
	public String toString() {
		return "stt\tend\tsze\t\n" + start + "\t" + end + "\t" + size;
	}

	/**
	 * reset the Containers indicating what's currently visible
	 */
	public void clearVisible() {
		splitRecombinantsMap.clear();
		visibleStrains.clear();
	}

	/**
	 * Add a read to the list for drawing recombinant connectors accross strains
	 * 
	 * @param pRead
	 *            must have a non-null matePair
	 */
	public void addSoloRecombinant(Read pRead) {
		Integer mpid = pRead.getMatePair().getIdInteger();
		splitRecombinantsMap.put(mpid, pRead);
	}

	/**
	 * Turns map of solo recombinant reads into map of line shapes
	 */
	public void createRecombinantConnectors() {
		Iterator<Map.Entry<Integer, Object>> rit = splitRecombinantsMap
				.entrySet()
				.iterator();
		while (rit.hasNext()) {
			Map.Entry<Integer, Object> e = rit.next();
			try {
				Shape shape = Util.createRecombinantSpan(this, (Read) e
						.getValue());
				if (shape != null) {
					splitRecombinantsMap.put(e.getKey(), shape);
				} else {
					rit.remove();
				}
			} catch (Exception ex) {
				// remove entry if it's broken
				String x = e.getValue().toString();
				rit.remove();
				System.out.println("Error creating connector for " + x);
				ex.printStackTrace();
			}
		}
	}

	// /**
	// * Copies strains from strainBackupData into ReferenceSequence and makes
	// sure all
	// * reads have the correct strain assignment.
	// */
	// public void restoreStrains() {
	// if (strainUndoData.size()==0) {
	// return;
	// }
	//		
	// referenceSequence.strains.clear();
	// Iterator sit = strainUndoData.iterator();
	// while (sit.hasNext()) {
	// Strain bak = (Strain) sit.next();
	// Iterator rit = bak.reads.values().iterator();
	// boolean hasReads = false;
	// while (rit.hasNext()) {
	// hasReads = true;
	// Readable readable = (Readable) rit.next();
	// readable.setStrain(bak);
	// }
	// bak.setAlignmentFromReads();
	// if (hasReads) {
	// referenceSequence.addStrainWithNoId(bak);
	// } else {
	// System.out.println("Strain had no reads.  Skipping");
	// }
	// }
	// strainUndoData.clear();
	// }

	/**
	 * change the vertical spacing settings
	 * 
	 * @return
	 */
	public boolean biggerRows() {
		if (currentVerticalSpacing < availableVerticalSpacings.size() - 1) {
			currentVerticalSpacing++;
			setVerticalSpacing(currentVerticalSpacing);
			return true;
		}
		return false;
	}

	/**
	 * change the vertical spacing settings
	 * 
	 * @return
	 */
	public boolean smallerRows() {
		if (currentVerticalSpacing > 0) {
			currentVerticalSpacing--;
			setVerticalSpacing(currentVerticalSpacing);
			return true;
		}
		return false;
	}

	/** method behind the make rows bigger/smaller action */
	private void setVerticalSpacing(int pIndex) {
		VerticalSpacingSet v = availableVerticalSpacings.get(pIndex);
		refSeqAreaHeight = v.getRefSeqAreaHeight();
		geneAreaHeight = v.getGeneAreaHeight();
		geneHeight = v.getGeneHeight();
		rowHeight = v.getRowHeight();
		rowSpacing = v.getRowSpacing();

		diffLetterCutoff = 0.8 * rowHeight;
		resetLetterDrawingVariables();
	}

	private Font letterFont = null;

	private double[] mVertivalVisibleRange;

	private void resetLetterDrawingVariables() {
		int size = rowHeight + 2;
		letterFont = new Font("monospaced", Font.PLAIN, size);

		drawDiffLetters = scaleFactor >= diffLetterCutoff;

	}

	public Font getLetterFont() {
		return letterFont;
	}

	/**
	 * Determines position in reference sequence (upper)from display x
	 * coordinate
	 */
	public int getDataPositionFromX(int pX) {
		int lengthFromStart = pX - border;
		int position = (int) (lengthFromStart / refSeqScaleFactor);
		return position;
	}

	/** Determines position in zoomed sequences (lower)from display x coordinate */
	public int getZoomedDataPositionFromX(int pX) {
		int lengthFromStart = pX - border;
		int position = start + (int) (lengthFromStart / scaleFactor);
		return position;
	}

	void zoomIn() {
		int middle = (start + end) / 2;
		int newSize = Math.max((size / 2), 1);
		setSize(newSize);
		start = middle - newSize / 2;
		end = start + newSize - 1;
	}

	void zoomOut() {
		int middle = (start + end) / 2;
		int newSize = size * 2;
		start = middle - newSize / 2;
		if (start < 1) {
			start = 1;
		}
		end = start + newSize - 1;
		if (end > referenceSequence.getLength()) {
			end = referenceSequence.getLength();
		}
		setSize(end - start + 1);
	}

	void panRight() {
		end = end + size / 2;
		if (end > referenceSequence.getLength()) {
			end = referenceSequence.getLength();
		}
		start = end - size + 1;
	}

	void panLeft() {
		start = start - size / 2;
		if (start < 1) {
			start = 1;
		}
		end = start + size - 1;
	}

	void zoomToRegion(int pMaxX, int pMinX) {
		end = getZoomedDataPositionFromX(pMaxX);
		if (end < 1) {
			end = 1;
		} else if (end > referenceSequence.getLength()) {
			end = referenceSequence.getLength();
		}
		start = getZoomedDataPositionFromX(pMinX);
		if (start < 1) {
			start = 1;
		} else if (start > referenceSequence.getLength()) {
			start = referenceSequence.getLength();
		}
		setSize(end - start + 1);
	}

	void goToPosition(int pPos, boolean pZoomToDiffs) {
		if (pZoomToDiffs) {
			setSize((int) ((getWidth() - 2 * border) / (diffTickCutoff * 2.0)));
		}

		start = pPos - getSize() / 2;
		if (start < 1) {
			start = 1;
		}
		end = start + size - 1;
		if (end > referenceSequence.getLength()) {
			end = referenceSequence.getLength();
			start = end - size + 1;
		}
	}

	void goToSequenceFragment(SequenceFragment pSF, boolean pExtent,
			boolean pCurrent, boolean pDiffs) {
		if (pExtent) {
			start = pSF.getStart();
			end = pSF.getEnd();
		} else {
			int center = (pSF.getStart() + pSF.getEnd()) / 2;
			int halfWidth;
			if (pCurrent) {
				halfWidth = size / 2;
			} else {
				halfWidth = (int) ((getWidth() - 2.0 * border)
						/ (diffTickCutoff * 2.0) / 2);
			}
			start = center - halfWidth;
			end = center + halfWidth;
		}
		setSize(end - start + 1);
	}

	public void setVerticalVisibleRange(double[] range) {
		mVertivalVisibleRange = range;
	}

	public double[] getVerticalVisibleRange() {
		return mVertivalVisibleRange;
	}

}
