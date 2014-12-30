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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import amd.strainer.DuplicateReadNameException;
import amd.strainer.GoToException;
import amd.strainer.algs.DefaultStrainerResult;
import amd.strainer.algs.SegmentLinker;
import amd.strainer.algs.SegmentStrainer;
import amd.strainer.algs.StrainerResult;
import amd.strainer.display.util.Stacker;
import amd.strainer.display.util.Util;
import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Alignment;
import amd.strainer.objects.Clone;
import amd.strainer.objects.Difference;
import amd.strainer.objects.Gene;
import amd.strainer.objects.QualifiedDifference;
import amd.strainer.objects.Read;
import amd.strainer.objects.Readable;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.SequenceFragment;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * This is the "canvas" that displays the data. The variable dData is a
 * DisplayData object that keeps track of all the necessary info including the
 * REferenceSequence object which holds the sequence data. The canvas will
 * resize itself to be as tall as necessary, but will conform to whatever width
 * it is given. It is designed to be placed in a JScrollPane with a vertical
 * scroll bar. Horizontal movement is handled with buttons.
 * 
 * TODO:6 (long term) Unify navigation: There are some non-optimal desgin
 * choices here due to the different ways vertical and horizontal scrolling and
 * zooming are handled.
 * 
 * @author jmeppley
 * 
 */
public class ReferenceSequenceDisplayComponent extends JPanel implements
		MouseListener, MouseMotionListener, ChangeListener {

	// Calling object
	private ReferenceSequenceDisplay parent = null;

	/**
	 * The current state of the display in a DisplayData object. Contains the
	 * displayed ReferenceSequence object as well as the current position, zoom
	 * level, selected objects, and undo-data
	 */
	public DisplayData dData = null;
	private final Rectangle2D eraser = new Rectangle2D.Double(0, 0, 800, 400);

	// variables for mouse listener methods
	private int lastPressX = 0;
	private int lastPressY = 0;
	private int dragState = NO_DRAG_STATE;
	private final static int NO_DRAG_STATE = 0;
	private final static int DRAG_VIEW_WINDOW = 1;
	private final static int DRAG_ZOOM_REGION = 2;
	private final static int DRAG_SELECTION_RECTANGLE = 3;
	private final Rectangle2D selectionBox = new Rectangle2D.Double(1, 2, 3, 4);

	// the user choices for displaying data
	private final DisplaySettings settings = DisplaySettings
			.getDisplaySettings();

	// color constants
	final static Color BACKGROUND_COLOR = Color.lightGray;
	final static Color REFERENCE_SEQUENCE_COLOR = Color.black;
	final static Color DISPLAY_REGION_COLOR = Color.red;
	final static Color DRAG_SELECTION_BOX_COLOR = Color.black;
	final static Color DRAG_ZOOM_BOX_COLOR = new Color(0, 0, 0, 63);
	final static Color GENE_OUTLINE_COLOR = Color.black;
	final static Color GENE_FILL_COLOR = Color.darkGray;
	final static Color GENE_SELECT_OUTLINE = Color.green;
	final static Color GENE_SELECT_FILL = Color.white;
	final static Color GENE_WINDOW = new Color(255, 255, 255, 63);

	// line widths
	final static BasicStroke DEFAULT_STROKE = new BasicStroke(1.0f);
	final static BasicStroke WIDE_STROKE = new BasicStroke(2.0f);

	Shape geneWindowRectangle = null;

	// context (pop-up) menu
	// JPopupMenu contextMenu;
	//

	// redraw flags
	boolean recalcRefSeq = false;
	boolean recalcRegion = false;
	boolean recalcGenes = false;
	boolean recalcGeneWindow = false;
	public boolean restack = false;
	public boolean recalcShapes = false;

	String toolTipBaseText = "No data: open something!";

	int lastCanvasWidth = 0;

	private LettersDisplayGeometry refSeqLetters;

	// called when component is resized
	@Override
	public void setSize(int pW, int pH) {
		lastCanvasWidth = pW;

		if (dData != null) {
			if (dData.getWidth() != pW) {
				dData.setWidth(pW);
				recalcGenes = true;
				recalcShapes = true;
				recalcRefSeq = true;
			}
		}
		eraser.setFrame(0, 0, pW, pH);
		super.setSize(pW, pH);
	}

	/**
	 * Creates the new canvas to draw data on. Must be given a
	 * ReferenceSequenceDisplay object that will contain it.
	 * 
	 * @param pParent
	 */
	public ReferenceSequenceDisplayComponent(ReferenceSequenceDisplay pParent) {
		parent = pParent;
		setToolTipText(toolTipBaseText);
		setBackground(BACKGROUND_COLOR);
		addMouseListener(this);
		addMouseMotionListener(this);
		instance = this;
	}

	public static ReferenceSequenceDisplayComponent instance = null;

	/**
	 * Reset the canvas with a new ReferenceSequence object.
	 * 
	 * @param ReferenceSequence
	 *            detailing how reads match up to a genome (fragment)
	 */
	public void drawReferenceSequence(ReferenceSequence pRefSeq) {
		if (pRefSeq == null) {
			dData = null;
			toolTipBaseText = "No data: open something!";
			parent.getCanvasView().setVerticalScrollBarPolicy(
					ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			refSeqLetters = null;
		} else {
			// reset display data and save ReferenceSequnce object
			dData = new DisplayData(this, pRefSeq);

			recalcGenes = true;
			restack = true;
			recalcShapes = true;
			recalcRefSeq = true;

			toolTipBaseText = "Reference Sequence Name: " + pRefSeq.getName();

			parent.getCanvasView().setVerticalScrollBarPolicy(
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

			// This is a bit of a hack to make sure outline colors are set
			// correctly.
			// TODO:4 track down why this is necessary, it's probably wating
			// cycles
			// selectAllReads();
			// clearSelections();

			// create object for drawing letters when zoomed in
			refSeqLetters = new LettersDisplayGeometry(new Gene(-1, pRefSeq, 1,
					pRefSeq.getLength(), true, ""));
		}

		setToolTipText(toolTipBaseText);
		repaint();
	}

	/**
	 * Move up to next level of row heights
	 */
	public void biggerRows() {
		if (dData.biggerRows()) {
			dData.height = Util.getTotalHeight(dData);
			setPreferredSize(new Dimension(dData.getWidth(), dData.height));
			recalcShapes = true;
			recalcGenes = true;
			recalcRefSeq = true;
			repaint();
		}
	}

	/**
	 * Move down to next level of row heights
	 */
	public void smallerRows() {
		if (dData.smallerRows()) {
			dData.height = Util.getTotalHeight(dData);
			setPreferredSize(new Dimension(dData.getWidth(), dData.height));
			recalcShapes = true;
			recalcGenes = true;
			recalcRefSeq = true;
			repaint();
		}
	}

	/**
	 * Move display to indicated postion on the ReferenceSequence
	 * 
	 * @param pPos
	 *            position in ReferenceSequence
	 * @param zoomToDiffs
	 *            true if zoom level should be set to just make diffs visible
	 */
	public void goToPosition(int pPos, boolean zoomToDiffs) {
		dData.goToPosition(pPos, false);

		recalcShapes = true;
		recalcGenes = true;
		recalcRegion = true;
		repaint();
	}

	/**
	 * Move display to indicated sequence position
	 * 
	 * @param position
	 *            on the reference sequence
	 */
	public void goToPosition(int pPos) {
		goToPosition(pPos, false);
	}

	private void zoomToSelectedRegion() {
		dData.zoomToRegion((int) selectionBox.getMaxX(),
				(int) selectionBox.getMinX());
		parent.updateDisplayWithString("New Position: " + dData.getStart()
				+ "-" + dData.getEnd());

		recalcShapes = true;
		recalcGenes = true;
		recalcRegion = true;
	}

	/**
	 * Move the display to the left (one half window width)
	 */
	public void panLeft() {
		dData.panLeft();

		recalcShapes = true;
		recalcGenes = true;
		recalcRegion = true;
		repaint();
	}

	/**
	 * Move the display to the right (one half window width)
	 */
	public void panRight() {
		dData.panRight();

		recalcShapes = true;
		recalcGenes = true;
		recalcRegion = true;
		repaint();
	}

	/**
	 * Halves the number of bases displayed
	 */
	public void zoomIn() {
		dData.zoomIn();

		recalcShapes = true;
		recalcGenes = true;
		recalcRegion = true;
		repaint();
	}

	/**
	 * doubles the number of bases displayed ( if there are enough undisplayed
	 * bases, else shows all)
	 */
	public void zoomOut() {
		dData.zoomOut();

		recalcShapes = true;
		recalcGenes = true;
		recalcRegion = true;
		repaint();
	}

	/**
	 * Move display to user specified read.
	 * 
	 * @param pText
	 *            Name of read to be searched for
	 * @param pExtent
	 *            if true, zoom display to read's extent
	 * @param pCurrent
	 *            if true, leave display at current zoom level
	 * @param pDiffs
	 *            if tru, zoom to widest view showing individual diffs
	 * @throws GoToException
	 *             if read cannot be located
	 */
	public void goToRead(String pText, boolean pExtent, boolean pCurrent,
			boolean pDiffs) throws GoToException {
		try {
			Integer readId = new Integer(pText);

			// get read by integer id
			Read r = dData.referenceSequence.reads.get(readId);

			if (r != null) {
				if (dData.selectedObject != null) {
					dData.selectedObject.deselect(dData);
				}
				r.select(dData);
				parent.updateDisplay(dData);
				goToSequenceFragment(r, pExtent, pCurrent, pDiffs);
				return;
			} else {
				throw new GoToException("No read with id: " + pText);
			}
		} catch (NumberFormatException e) {
			// INPUT NOT AN INTEGER, TRY TO FIND READ BY NAME
			Iterator<Read> rit = dData.referenceSequence.getReadIterator();
			while (rit.hasNext()) {
				Read r = rit.next();
				if (pText.equals(r.getName())) {
					if (dData.selectedObject != null) {
						dData.selectedObject.deselect(dData);
					}
					r.select(dData);
					parent.updateDisplay(dData);
					goToSequenceFragment(r, pExtent, pCurrent, pDiffs);
					return;
				}
			}

			// if we're still here, the read doesn't exist
			throw new GoToException("Read \"" + pText + "\" not found.");
		}
	}

	private void goToSequenceFragment(SequenceFragment pSF, boolean pExtent,
			boolean pCurrent, boolean pDiffs) {
		dData.goToSequenceFragment(pSF, pExtent, pCurrent, pDiffs);

		recalcShapes = true;
		recalcGenes = true;
		recalcRegion = true;
		repaint();
	}

	/**
	 * display the indicated gene
	 * 
	 * @param pText
	 *            user input name or ID number of gene to go to
	 * @param pExtent
	 *            if true, zoom to width of gene
	 * @param pCurrent
	 *            if true, leave zoom as is
	 * @param pDiffs
	 *            if true, zoom to just see diffs
	 * @throws GoToException
	 *             if the Gene cannot be located
	 */
	public void goToGene(String pText, boolean pExtent, boolean pCurrent,
			boolean pDiffs) throws GoToException {
		try {
			Integer geneNumber = new Integer(pText);

			// get read by integer id
			Gene g = dData.referenceSequence.genes.get(geneNumber);

			if (g != null) {
				if (dData.selectedGene != g) {
					updateSelectedGene(g);
				}
				parent.updateDisplay(dData);
				goToSequenceFragment(g, pExtent, pCurrent, pDiffs);
				return;
			} else {
				// in some cases the names are also numbers, so let's
				// also try searching names. (done in catch statement)
				throw new NumberFormatException("No gene with number: " + pText);
				// throw new GoToException("No gene with number: " + pText);
			}
		} catch (NumberFormatException e) {
			// INPUT NOT AN INTEGER, TRY TO FIND READ BY NAME
			Iterator<Gene> git = dData.referenceSequence.genes.values()
					.iterator();
			while (git.hasNext()) {
				Gene g = git.next();
				if (pText.equals(g.getName())) {
					updateSelectedGene(g);
					parent.updateDisplay(dData);
					goToSequenceFragment(g, pExtent, pCurrent, pDiffs);
					return;
				}
			}

			// if we're still here, the read doesn't exist
			throw new GoToException("Gene \"" + pText + "\" not found.");
		}
	}

	/** called from menu action. Adds all reads to selection */
	public void selectAllReads() {
		Iterator<Strain> sit = dData.referenceSequence.strains.values()
				.iterator();
		while (sit.hasNext()) {
			sit.next().addToSelectedList(dData);
		}
		parent.updateDisplay(dData);
		repaint();
	}

	/**
	 * @return true if no reads or strains are selected
	 */
	public boolean isSelectionEmpty() {
		return dData.selectedReadList.getSize() == 0;
	}

	/**
	 * @return true if there are no uncovered bases between any covered bases in
	 *         the selection
	 */
	public boolean isSelectionConnected() {
		Alignment a = dData.selectedReadList.getAlignment();
		if (a == null) {
			dData.selectedReadList.setAlignmentFromReads();
			a = dData.selectedReadList.getAlignment();
		}
		List<SequenceSegment> holes = a.getUnknownRegions();
		return holes.size() == 0;
	}

	/**
	 * @param pFindMatePairs
	 *            include all mate pairs if true
	 * @return String list of selected read names
	 */
	public String getSelectionList(boolean pFindMatePairs) {
		return dData.selectedReadList.printList(pFindMatePairs);
	}

	/**
	 * undo the last action
	 */
	public void undo() {
		dData.undoData.undo();

		// clear selections
		clearSelectionsNoRepaint();
		parent.updateDisplay(dData);

		// now draw it.
		restack = true;
		recalcShapes = true;

		repaint();
	}

	/**
	 * redo the last action
	 */
	public void redo() {
		dData.undoData.redo();

		// clear selections
		clearSelectionsNoRepaint();
		parent.updateDisplay(dData);

		// now draw it.
		restack = true;
		recalcShapes = true;

		repaint();
	}

	/**
	 * @return SequenceSegment indicating the current reference sequence and
	 *         start end pints of the display
	 */
	public SequenceSegment getSequenceSegment() {
		if (dData == null) {
			return null;
		}
		return new SequenceSegment(dData.referenceSequence, dData.getStart(),
				dData.getEnd());
	}

	/**
	 * working down from largest strain to smallest, check to make sure every
	 * read is in the same Strain as its mate pair. Bring the mate-pair in if
	 * necessary.
	 */
	public void getAllMatePairsBySize() {
		// take snapshot of strains incase user wants to go back to this
		dData.undoData.startMove();

		Iterator<Strain> sit = new ArrayList<Strain>(
				Strain.sortStrainsBySize(dData.referenceSequence.strains
						.values())).iterator();
		while (sit.hasNext()) {
			Strain strain = sit.next();
			if (strain.getSize() == 0) {
				continue;
			}

			// clear selections
			clearSelectionsNoRepaint();

			// select all reads in the strain and any missing mate pairs
			Iterator<Readable> rit = strain.getReadableIterator();
			while (rit.hasNext()) {
				Readable read = rit.next();
				read.addToSelectedList(dData);
				if (read.getMatePair() != null && !read.isBadClone()
						&& !read.isRecombinant()) {
					read.getMatePair().addToSelectedList(dData);
				}
			}

			// clear some space in memory
			sit.remove();

			// group
			makeStrainFromSelectionNoRedraw(true);

		}

		// clear selections
		clearSelectionsNoRepaint();

		// close undo move
		dData.undoData.endMove();

		// update info boxes
		parent.updateDisplay(dData);

		// now draw it.
		restack = true;
		recalcShapes = true;

		repaint();
	}

	/**
	 * check to make sure every read is in the same Strain as its mate pair.
	 * Bring the matepair in if necessary.
	 */
	public void bringMatePairsIntoSelectedStrain() {
		// System.out.println("Checking selection");

		if (dData.selectedObject == null
				|| dData.selectedObject instanceof Clone) {

			// if nothing is selected or a complete clone is selected
			// then there is nothing to do
			return;
		}

		// take snapshot of strains incase user wants to go back to this
		dData.undoData.startMove();

		// System.out.println("Saving selection");

		AlignedSequence selection = dData.selectedObject;

		// System.out.println("Clearing selection");

		// clear selections
		clearSelectionsNoRepaint();

		if (selection instanceof Read) {
			// System.out.println("Its a read");

			Read read = (Read) selection;

			if (read.getMatePair() != null && !read.isBadClone()
					&& !read.isRecombinant()) {

				// select strain for this read
				read.getStrain().addToSelectedList(dData);
				read.getMatePair().addToSelectedList(dData);

				// group
				Strain newStrain = makeStrainFromSelectionNoRedraw(true);

				// make sure all reads are selected
				newStrain.select(dData);
			} else {
				read.select(dData);
			}
		} else {
			Strain strain = (Strain) selection;

			// select all reads in the strain and any missing mate pairs
			Iterator<Readable> rit = strain.getReadableIterator();
			while (rit.hasNext()) {
				Readable read = rit.next();
				read.addToSelectedList(dData);
				if (read.getMatePair() != null && !read.isBadClone()) {
					read.getMatePair().addToSelectedList(dData);
				}
			}

			// group
			Strain newStrain = makeStrainFromSelectionNoRedraw(true);

			// make sure all reads are selected
			newStrain.select(dData);
		}

		dData.undoData.endMove();

		// update info boxes
		parent.updateDisplay(dData);

		// now draw it.
		restack = true;
		recalcShapes = true;

		repaint();
	}

	private Strain makeStrainFromSelectionNoRedraw(boolean pCheckDisconnects) {
		// build new strain

		// keep track of which strains lost how many reads
		HashMap<Strain, Integer> oldStrainRemovalCounts = new HashMap<Strain, Integer>();
		// we want to know which was the biggest strain to be emptied
		int biggestClearedStrainSize = 0;
		Strain biggestClearedStrain = null;
		// set up empty strain
		Strain newStrain = new Strain();
		newStrain.initializeGraphics();
		// need to have the strain ID before the undo data is set
		newStrain.setId(dData.referenceSequence.getNextStrainId());

		// loop over reads to be added to new strain
		Iterator<Read> rit = dData.selectedReadList.getReadIterator();
		while (rit.hasNext()) {
			// get the current read
			Read read = rit.next();
			// find its old strain assignment
			Strain oldStrain = read.getStrain();

			// move read
			oldStrain.removeRead(read);
			newStrain.putRead(read.getIdInteger(), read);

			// record move in undo data
			dData.undoData.addToMove(read, oldStrain.getIdInteger());

			// keep track of how many reads removed from each strain

			// get counter for this strain or start a new one
			int count = 1;
			try {
				count = oldStrainRemovalCounts.get(oldStrain);
				count++;
			} catch (NullPointerException npe) {
				// no value, leave default of 1
			}

			// save updated count
			oldStrainRemovalCounts.put(oldStrain, count);

			// if strain is empty,
			// see if it lost more reads than other emptied strains
			if (oldStrain.getSize() == 0) {
				// did it have the most reads?
				if (count > biggestClearedStrainSize) {
					biggestClearedStrain = oldStrain;
				}

				// remove strain from reference either way
				dData.referenceSequence.strains
						.remove(oldStrain.getIdInteger());
				oldStrainRemovalCounts.remove(oldStrain);
				oldStrain.close();
			}
		}

		// set start/end of new strain
		newStrain.setAlignmentFromReads();
		dData.referenceSequence.putStrain(newStrain.getIdInteger(), newStrain);

		// get random color from biggest emptied strain or random
		if (biggestClearedStrain != null) {
			newStrain.getDisplayGeometry().setRandomColor(
					biggestClearedStrain.getDisplayGeometry().getRandomColor());
		}

		// clean up emptied strains
		for (Strain strain : oldStrainRemovalCounts.keySet()) {
			// reset alignment
			strain.setAlignmentFromReads();

			if (pCheckDisconnects) {
				// check all old strains to see if any have disconnects
				breakUpStrain(strain, false);
			}
		}

		return newStrain;
	}

	private void breakUpStrain(Strain pStrain, boolean debug) {
		int pos = pStrain.getStart();
		HashSet<Readable> readList = pStrain
				.findDisconnectedReadsAfterPosition(pos, debug);
		while (readList != null) {
			clearSelectionsNoRepaint();
			for (Readable r : readList) {
				r.addToSelectedList(dData);
			}

			pos = makeStrainFromSelectionNoRedraw(false).getEnd();

			readList = pStrain.findDisconnectedReadsAfterPosition(pos, debug);
		}
	}

	/**
	 * Put all the currently selected reads (in dData.selectedReadsList) into
	 * the same strain
	 */
	public void makeStrainFromSelection() {
		// bail out if nothing is selection
		if (isSelectionEmpty()) {
			return;
		}

		// take snapshot of strains incase user wants to go back to this
		dData.undoData.startMove();

		// do the work
		Strain newStrain = makeStrainFromSelectionNoRedraw(false);

		dData.undoData.endMove();

		// reset selections so no stale objects hang around
		clearSelectionsNoRepaint();

		// make sure all reads are selected
		newStrain.select(dData);

		// update info boxes
		parent.updateDisplay(dData);

		// now draw it.
		restack = true;
		recalcShapes = true;

		repaint();
	}

	/**
	 * @return true if all bases are displayed
	 */
	public boolean isZoomedOut() {
		return dData.getSize() == dData.referenceSequence.getLength() - 1;
	}

	/**
	 * Attempt to separate reads into Strains accross the entire
	 * ReferenceSequence
	 */
	public void autoStrainReferenceSequence() {
		// take snapshot of strains incase user wants to go back to this
		dData.undoData.startMove();

		// clear selections
		clearSelectionsNoRepaint();

		SequenceSegment wdt = new SequenceSegment(dData.referenceSequence, 1,
				dData.referenceSequence.getLength());

		SegmentStrainer ss = new SegmentLinker();
		ss.setSegment(wdt);

		try {
			// get gene strains
			DefaultStrainerResult sr = (DefaultStrainerResult) ss.getStrains();

			// makeinto strains
			updateStrainsFromStrainerResults(sr);
		} catch (Exception e) {
			Util.displayErrorMessage(PaneledReferenceSequenceDisplay.frame, e);
		}

		dData.undoData.endMove();

		// update info boxes
		parent.updateDisplay(dData);

		// now draw it.
		restack = true;
		recalcShapes = true;

		repaint();
	}

	/**
	 * Alter strain groupings based on autostrainer results.
	 * <p>
	 * // reads can be in multiple strains, so we are useing the simple // rule
	 * here that the bigger strains win. So rather than figure // out what the
	 * biggest strain each read is in, I'm just // doing them in reverse order
	 * and only the last (biggest) will be kept
	 * 
	 * @param pSr
	 *            strain assocciations returned from a straining algorithm
	 * @param refresh
	 *            save undo data if true
	 */
	public void updateStrainsFromStrainerResults(StrainerResult pSr,
			boolean refresh) {
		if (refresh) {
			// take snapshot of strains in case user wants to go back to this
			dData.undoData.startMove();
		}

		updateStrainsFromStrainerResults(pSr);

		if (refresh) {
			dData.undoData.endMove();

			// update info boxes
			parent.updateDisplay(dData);

			// now draw it.
			restack = true;
			recalcShapes = true;

			repaint();
		}
	}

	private void updateStrainsFromStrainerResults(StrainerResult pSr) {
		clearSelectionsNoRepaint();

		// sort gene strains
		java.util.List<Strain> sortedStrains = Strain.sortStrainsBySize(pSr
				.getStrains());

		// reads can be in multiple strains, so we are using the simple
		// rule here that the bigger strains win. So rather than figure
		// out what the biggest strain each read is in, I'm just
		// doing them in reverse order and only the last (biggest) will be kept
		for (int i = sortedStrains.size() - 1; i >= 0; i--) {
			Strain strain = sortedStrains.get(i);
			Iterator<Read> rit = strain.getReadIterator();
			while (rit.hasNext()) {
				Read algRead = rit.next();
				// this is convoluted because read objects in strainer results
				// are not same objects used in gene strainer
				Read realRead = dData.referenceSequence.reads.get(algRead
						.getIdInteger());
				realRead.addToSelectedList(dData);
			}
			makeStrainFromSelectionNoRedraw(true);
			clearSelectionsNoRepaint();
		}
	}

	public void updateStrainsFromReadLists(List<List<String>> strains) {
		// take snapshot of strains in case user wants to go back to this
		dData.undoData.startMove();

		try {
			clearSelectionsNoRepaint();
			Map<String, Integer> readIdMap = dData.referenceSequence
					.getReadIdMap();

			for (List<String> strain : strains) {

				for (String readName : strain) {
					Read read = dData.referenceSequence.reads.get(readIdMap
							.get(readName));
					if (read == null) {
						// throw new IndexOutOfBoundsException(
						// "Cannot find read: " + readName);
						System.err.println("WARNING: read not found: "
								+ readName);
					} else {
						read.addToSelectedList(dData);
					}
				}
				// TODO: don't create empty strains!!!!
				if (dData.selectedReadList.getSize() > 0) {
					makeStrainFromSelectionNoRedraw(true);
				}
				clearSelectionsNoRepaint();
			}
		} catch (IndexOutOfBoundsException iobe) {
			Util.displayErrorMessage(iobe);
		} catch (DuplicateReadNameException drne) {
			Util.displayErrorMessage(drne);
		} catch (RuntimeException re) {
			Util.displayErrorMessage("Unexpected Error:", re.toString());
		}

		dData.undoData.endMove();

		// update info boxes
		parent.updateDisplay(dData);

		// now draw it.
		restack = true;
		recalcShapes = true;

		repaint();
	}

	/**
	 * Toggle the recombinant status of the currently selected read or clone
	 */
	public void labelRecomb() {
		if (dData.selectedObject == null) {
			return;
		}
		if (dData.selectedObject instanceof Readable) {

			// undo data
			dData.undoData.newRecombMove((Readable) dData.selectedObject);

			// do the work
			((Readable) dData.selectedObject).toggleRecombinant();

			// make sure colors are updated for this and other objects that
			// might have been updated
			dData.selectedObject.recalcColors();

			if (dData.selectedObject instanceof Clone) {
				Clone clone = (Clone) dData.selectedObject;
				clone.reads[0].recalcColors();
				clone.reads[1].recalcColors();
			} else {
				Read read = (Read) dData.selectedObject;
				if (read.isInClone()) {
					read.getClone().recalcColors();
					read.getMatePair().recalcColors();
				}
			}

			repaint();
			parent.updateActions();
		}
	}

	/**
	 * Toggle the recombinant status of the all selected reads or clones
	 */
	public void labelAllRecomb() {
		if (dData.selectedReadList.getSize() > 0) {

			Collection<Readable> readables = dData.selectedReadList
					.getReadables();
			List<Readable> toggledReadables = new ArrayList<Readable>();

			try {
				for (Readable readable : readables) {
					// do the work

					if (readable instanceof Clone) {
						Clone clone = (Clone) readable;
						clone.toggleRecombinant();
						clone.recalcColors();
						clone.reads[0].recalcColors();
						clone.reads[1].recalcColors();
					} else {
						Read read = (Read) readable;
						if (read.getMatePair() != null) {
							read.getClone().toggleRecombinant();
							read.getClone().recalcColors();
							read.recalcColors();
							read.getMatePair().recalcColors();
						} else {
							read.toggleRecombinant();
							read.recalcColors();
						}
					}

					// add to completed list for undo
					toggledReadables.add(readable);
				}
			} catch (Exception e) {
				Util.displayErrorMessage(e);
			}

			// undo data
			dData.undoData.newBulkRecombMove(toggledReadables);

			repaint();
			parent.updateActions();
		}
	}

	private void stackSequences() {
		if (restack) {
			/*
			 * This calls stackPositions() in util.Stacker.java to determine how
			 * best to fit rectangles on canvas It only needs to be done once
			 * per ReferenceSequence, unless strain groupings are changed
			 */

			List<Strain> strains = null;

			// strains = Strain.sortStrainsBySize(strains);
			if (settings.getStrainSorting() == DisplaySettings.SORT_BY_SIZE) {
				strains = Strain
						.sortStrainsBySize(dData.referenceSequence.strains
								.values());
			} else if (settings.getStrainSorting() == DisplaySettings.SORT_BY_IDENTITY) {
				strains = Strain
						.sortStrainsByIdentity(dData.referenceSequence.strains
								.values());
			} else if (settings.getStrainSorting() == DisplaySettings.SORT_BY_LENGTH) {
				strains = Strain
						.sortStrainsByLength(dData.referenceSequence.strains
								.values());
			}

			// get row placements
			for (Strain strain : dData.referenceSequence.strains.values()) {

				// stack reads within strain
				List<Readable> reads = null;

				if (settings.getReadSorting() == DisplaySettings.SORT_BY_SIZE) {
					reads = strain.getReadablesSortedByLength(); // reads don't
					// have a
					// size, use
					// length
				} else if (settings.getReadSorting() == DisplaySettings.SORT_BY_IDENTITY) {
					reads = strain.getReadablesSortedByIdentity();
				} else if (settings.getReadSorting() == DisplaySettings.SORT_BY_LENGTH) {
					reads = strain.getReadablesSortedByLength();
				}

				StrainDisplayGeometry sdg = (StrainDisplayGeometry) strain
						.getDisplayGeometry();
				if (strain.isOpen()) {
					if (settings.getReadStacking() == DisplaySettings.STACK_COMPACTLY) {
						sdg.setHeight(Stacker.stackPositions(reads));
					} else if (settings.getReadStacking() == DisplaySettings.STACK_STRICTLY) {
						sdg.setHeight(Stacker.stackPositionsStrictly(reads));
					} else {
						sdg.setHeight(Stacker.stackReadables(reads));
					}
				} else {
					sdg.setHeight(1);
				}
			}

			// stack strains on canvas
			if (settings.getStrainStacking() == DisplaySettings.STACK_COMPACTLY) {
				dData.totalRows = Stacker.stackPositions(strains);
			} else {
				dData.totalRows = Stacker.stackPositionsStrictly(strains);
			}

			// adjust canvas size
			dData.height = Util.getTotalHeight(dData);
			setPreferredSize(new Dimension(dData.getWidth(), dData.height));
			// setSize(new Dimension(dData.width,dData.height));

			restack = false;
		}
	}

	void recalculateGenes() {
		if (recalcGenes) {
			dData.visibleGenes.clear();

			// initialize gene shapes
			for (Gene gene : dData.referenceSequence.genes.values()) {
				if (gene.getDisplayGeometry().update(dData)) {
					dData.visibleGenes.add(gene);
					gene.getDisplayGeometry().fill = GENE_FILL_COLOR;
					gene.getDisplayGeometry().outline = GENE_OUTLINE_COLOR;
				}
			}

			// do lower gene window to match new gene shapes
			if (dData.selectedGene != null) {
				geneWindowRectangle = Util.createBottomGeneWindow(
						dData.selectedGene, dData);

				// make sure selected gene is correct color
				// (it gets reset above in gene shape init. loop)
				dData.selectedGene.getDisplayGeometry().fill = GENE_SELECT_FILL;
				dData.selectedGene.getDisplayGeometry().outline = GENE_SELECT_OUTLINE;
			}

			// update object that draws ref seq letters over top of genes
			if (refSeqLetters != null)
				refSeqLetters.update(dData);

			// reset redraw flag
			recalcGenes = false;
		}
	}

	void recalculateGeneWindow() {
		if (recalcGeneWindow) {
			if (dData.selectedGene != null) {
				geneWindowRectangle = Util.createBottomGeneWindow(
						dData.selectedGene, dData);
			}

			recalcGeneWindow = false;
		}
	}

	void recalculateRefSeq() {
		if (recalcRefSeq) {
			// this makes the position bar at the top and the selection rect
			Util.createReferenceSequenceShapes(dData);

			recalcRegion = true;
			recalculateRegion();

			// if (dData.selectedGene != null) {
			// geneWindowTop =
			// Util.createTopGeneWindow(dData.selectedGene,dData);
			// }

			recalcRefSeq = false;
		}
	}

	void recalculateRegion() {
		if (recalcRegion) {
			// this makes the position bar at the top and the selection rect
			Util.createRegionShape(dData);

			recalcRegion = false;
		}
	}

	private void recalculateShapes() {
		if (recalcShapes) {

			// clear visible strains
			dData.clearVisible();
			// dData.printRecombinantMap("should be clear");

			// create shpae objects now,
			// so we don't recreate them on each redraw

			// create strains and reads
			for (Strain strain : dData.referenceSequence.strains.values()) {
				if (strain.getDisplayGeometry().update(dData)) {
					dData.visibleStrains.add(strain);
					// strain.recalcColors();
					strain.getDisplayGeometry().outline = strain
							.getDisplayGeometry().fill;

					// reset list of visible reads
					((StrainDisplayGeometry) strain.getDisplayGeometry())
							.clearVisibleReads();

					if (!strain.isOpen()) {
						// leave all reads invisible if strain not open
						// but look for recombinants
						Iterator<Readable> rit = strain.getReadableIterator();
						while (rit.hasNext()) {
							Readable read = rit.next();
							if (!(read instanceof Clone)
									&& read.isRecombinant()) {
								Read r = (Read) read;
								// only draw connection if mate-pair exists and
								// is also tagged as recombinant
								if (r.getMatePair() != null
										&& r.getMatePair().isRecombinant()) {
									dData.addSoloRecombinant(r);
								}
							}
						}

						// don't do anything else for this strain
						continue;
					}

					Iterator<Readable> rit = strain.getReadableIterator();
					while (rit.hasNext()) {
						Readable read = rit.next();
						if (read.getDisplayGeometry().update(dData)) {
							((StrainDisplayGeometry) strain
									.getDisplayGeometry()).addVisibleRead(read);
							// read.recalcColors();

							if (read instanceof Clone) {
								((ReadDisplayGeometry) ((Clone) read).reads[0]
										.getDisplayGeometry()).setColors(dData);
								((ReadDisplayGeometry) ((Clone) read).reads[1]
										.getDisplayGeometry()).setColors(dData);
							} else
							// if readable is recomibinant and not a MatePair,
							// add to list of
							// separated recombinant reads
							if (read.isRecombinant()) {
								Read r = (Read) read;
								// only draw connection if matepair exists and
								// is also tagged as recombinant
								if (r.getMatePair() != null
										&& r.getMatePair().isRecombinant()) {
									dData.addSoloRecombinant(r);
								}
							}

						}
					}
				}
			}

			// check if selected read has a matePair somewhere
			if (dData.selectedObject instanceof Read) {
				Read r = (Read) dData.selectedObject;
				if (!r.isInClone() && r.getMatePair() != null
						&& !r.getMatePair().getDisplayGeometry().visible) {
					((ReadDisplayGeometry) r.getMatePair().getDisplayGeometry())
							.updateMatePairCarat(dData);
					r.getMatePair().recalcColors();
					dData.matePairCarat = true;
				} else {
					dData.matePairCarat = false;
				}
			}

			// generate recomb connectors from list of separated mate pairs
			// dData.printRecombinantMap("all reads?");
			dData.createRecombinantConnectors();
			// dData.printRecombinantMap("all lines");

			// restoreSelectionColors();

			recalcShapes = false;
		}
	}

	/**
	 * simply calls DisplayData.resetColors() on this.dData
	 */
	public void resetColors() {
		if (dData != null) {
			dData.resetColors();
		}
	}

	void updateSelectedObjects(AlignedSequence pSelection) {
		AlignedSequence previousSelection = dData.selectedObject;

		dData.selectedObject = null;

		// this call should change the state and colors of this object
		if (previousSelection != null) {
			previousSelection.deselect(dData);
		}

		if (pSelection != null) {
			if (pSelection == previousSelection) {
				pSelection.removeFromSelectedList(dData);
			} else {
				if (previousSelection != null) {
					updateDisplayWithDiff(pSelection, previousSelection);
				}
				// this call should change the colors and state of the object
				// and call .addToSelectedList
				pSelection.select(dData);
			}
		}
	}

	void updateDisplayWithDiff(AlignedSequence p1, AlignedSequence p2) {
		// this conditional is ugly, but the idea is to calculate the difference
		// ...
		// IF
		// neither sequence is a strain
		// OR
		// the user has indicated a willingness to wait for strain calculations
		// by
		// selecting "sort strains by ID" or "color strains by ID"

		if (!(p1 instanceof Strain) && !(p2 instanceof Strain)
				|| DisplaySettings.getDisplaySettings().isStrainDiffsOn()) {

			// only get AA sequence identity if a gene is selected and visible
			boolean comparingGeneSequence = dData.selectedGene != null
					&& dData.selectedGene.getDisplayGeometry().visible;

			int start, end;
			if (comparingGeneSequence) {
				start = dData.selectedGene.getStart();
				end = dData.selectedGene.getEnd();
			} else {
				start = dData.getStart();
				end = dData.getEnd();
			}
			int diffCount = amd.strainer.algs.Util.countDiffsBetweenSequences(
					p1, p2, start, end);
			// System.out.println("Found  " + diffCount + " diffs");
			double length = amd.strainer.algs.Util.calculateOverlapLength(p1,
					p2, start, end);
			// System.out.println("In  " + length + " bases");
			if (length <= 0) {
				// don't print anything if they don't overlap
				return;
			}
			double pctId = 100 * (length - diffCount) / length;

			StringBuffer sb = new StringBuffer();
			String p1Name = p1.getName();
			if (p1 instanceof Strain) {
				p1Name = p1.getIdInteger().toString();
			} else if (p1 instanceof Clone) {
				p1Name = p1.toString();
			}
			String p2Name = p2.getName();
			if (p2 instanceof Strain) {
				p2Name = p2.getIdInteger().toString();
			} else if (p2 instanceof Clone) {
				p2Name = p2.toString();
			}

			// output is different if gene is selected
			if (comparingGeneSequence) {
				double pctAAId = amd.strainer.algs.Util
						.calculateAminoAcidIdentity(p1, p2, dData.selectedGene);
				if (pctAAId == -1) {
					// aa id didn't work, skip it (the next line is a copy of
					// the one below
					sb.append(pctId).append("% identity between ")
							.append(p1Name).append(" and ").append(p2Name);
				} else {
					// include AA id in comparison
					sb.append(pctId).append("% nulceotide and ")
							.append(pctAAId)
							.append("% amino acid identity between ")
							.append(p1Name).append(" and ").append(p2Name);
				}
			} else {
				// just include DNA identity
				sb.append(pctId).append("% identity between ").append(p1Name)
						.append(" and ").append(p2Name);
			}
			parent.updateDisplayWithString(sb.toString());
		}
	}

	void updateSelectedGene(Gene pGene) {
		Gene prevSel = dData.selectedGene;
		clearSelectedGene();
		if (pGene == null || prevSel == pGene) {
			dData.selectedGene = null;
		} else {
			dData.selectedGene = pGene;
			dData.selectedGene.getDisplayGeometry().fill = GENE_SELECT_FILL;
			dData.selectedGene.getDisplayGeometry().outline = GENE_SELECT_OUTLINE;

			recalcGeneWindow = true;
		}
	}

	void clearSelectedGene() {
		if (dData.selectedGene != null) {
			// revert previous selevction to default colors
			dData.selectedGene.getDisplayGeometry().fill = GENE_FILL_COLOR;
			dData.selectedGene.getDisplayGeometry().outline = GENE_OUTLINE_COLOR;
			// remove selection object
			dData.selectedGene = null;
		}
	}

	/**
	 * purge all selected objects from current display state
	 */
	public void clearSelections() {
		clearSelectionsNoRepaint();
		parent.updateDisplay(dData);
		repaint();
	}

	void clearSelectionsNoRepaint() {
		clearSelectedGene();
		if (dData.selectedObject != null) {
			dData.selectedObject.deselect(dData);
			dData.selectedObject = null;
		}
		clearPrevSelections();
	}

	/*
	 * This is ugly and dangerous, but it will have to stand for now Normally,
	 * we'd call Readable.removeFromSelectedList, but that requires looping
	 * through the list once for each read plus we'd be editing the list while
	 * looping through it. Instead we do it all here, but that requires
	 * accessing things that should be private (inSelectedList, setColors())
	 */
	void clearPrevSelections() {
		Iterator<Read> pit = dData.selectedReadList.getReadIterator();
		while (pit.hasNext()) {
			Read read = pit.next();
			// this call doesn't work, clear collection later
			// pit.remove();

			// fix state variable
			read.inSelectedList = false;
			read.recalcColors();

			if (read.isInClone()) {
				read.getClone().recalcColors();
			}
		}

		dData.selectedReadList.close();
		dData.selectedReadList = new Strain();
		dData.selectedReadList.stealReads = false;
	}

	private void clearComponent(Graphics2D pG2d) {
		pG2d.setPaint(BACKGROUND_COLOR);
		pG2d.draw(eraser);
		pG2d.fill(eraser);
	}

	@Override
	public void repaint() {
		// make any necessary calculations here, so they don't get done
		// everytime
		// update or paint is called
		// Also, make sure the preferred size is set as early as possible
		// if (recalcRefSeq || recalcRegion || recalcGenes || recalcGeneWindow
		// || restack || recalcShapes) {
		doNecessaryCalculations();
		// }
		super.repaint();
	}

	/**
	 * checks to see if any calculations have been flagged to be done
	 */
	public void doNecessaryCalculations() {
		// make any necessary calculations here, so they don't get done
		// everytime
		// update or paint is called
		// Also, make sure the preferred size is set as early as possible
		if (dData != null) {
			// Thread calcThread = new Thread(new Runnable() {

			// public void run() {
			synchronized (dData.referenceSequence) {
				recalculateGenes();
				stackSequences();
				recalculateShapes();
				recalculateRefSeq();
				recalculateRegion();
				recalculateGeneWindow();
				// repaint();
			}
			// }});
			// calcThread.start();
		}
	}

	private void drawReads(Graphics2D pG2d) {
		// set default line width
		pG2d.setStroke(DEFAULT_STROKE);

		// clear canvas if display has been altered
		clearComponent(pG2d);

		if (dData.entireRefSeq == null) {
			// we're not ready yet
			return;
		}

		// draw reference sequence bar
		pG2d.setPaint(REFERENCE_SEQUENCE_COLOR);
		pG2d.fill(dData.entireRefSeq);

		// use wide line for region box that indicates relative position
		pG2d.setStroke(WIDE_STROKE);

		// draw position indicator
		pG2d.setPaint(DISPLAY_REGION_COLOR);
		pG2d.draw(dData.displayRegion);

		// only use narrower stroke (for outlines) if row heights are small
		// otherwise leave stroke wide for now
		if (dData.currentVerticalSpacing <= 3)
			pG2d.setStroke(DEFAULT_STROKE);

		// draw strains
		for (Strain strain : dData.visibleStrains) {
			strain.getDisplayGeometry().draw(pG2d, dData);
		}

		// draw recombinant connectors
		pG2d.setPaint(DisplaySettings.getDisplaySettings()
				.getRecombinantColor());
		for (Object connector : dData.splitRecombinantsMap.values()) {
			pG2d.draw((Shape) connector);
		}

		// draw matePair carat if needed
		if (dData.matePairCarat) {
			Read matePair = ((Read) dData.selectedObject).getMatePair();
			matePair.getDisplayGeometry().draw(pG2d, dData);
		}

		// System.out.println("genes");
		// draw gene window
		if (dData.selectedGene != null) {
			if (geneWindowRectangle != null) {
				pG2d.setPaint(GENE_WINDOW);
				pG2d.fill(geneWindowRectangle);
				pG2d.draw(geneWindowRectangle);
			}
		}

		// draw genes
		for (Gene gene : dData.visibleGenes) {
			gene.getDisplayGeometry().draw(pG2d, dData);
		}

		// draw reference sequence letters over genes
		refSeqLetters.draw(pG2d, dData);

		// draw user drag area (with a wide border)
		pG2d.setStroke(WIDE_STROKE);

		if (dragState == DRAG_SELECTION_RECTANGLE) {
			pG2d.setPaint(DRAG_SELECTION_BOX_COLOR);
			pG2d.draw(selectionBox);
		} else if (dragState == DRAG_ZOOM_REGION) {
			// System.out.println(selectionBox.toString());
			pG2d.setPaint(DRAG_ZOOM_BOX_COLOR);
			pG2d.fill(selectionBox);
		}
	}

	/**
	 * oveverrides paint() method in java.awt.Canvas
	 */
	@Override
	public void paint(Graphics pG) {
		// System.out.println("painting");
		// System.out.println("painting canvas");
		Graphics2D g2d = (Graphics2D) pG;

		if (dData == null) {
			clearComponent(g2d);
		} else {
			int canvasWidth = parent.getCanvasView().getViewport().getWidth();
			if (canvasWidth != lastCanvasWidth) {
				// System.out.println("widths:" + canvasWidth + ":" +
				// lastCanvasWidth + ":" + dData.width);
				setPreferredSize(new Dimension(canvasWidth, dData.height));
				lastCanvasWidth = canvasWidth;
			}

			drawReads(g2d);
		}

		// System.out.println("done painting");
	}

	/**
	 * oveverrides update() method in java.awt.Canvas
	 */
	@Override
	public void update(Graphics pG) {
		// System.out.println("updating canvas");
		paint(pG);
	}

	private Gene findIndicatedGene(int x, int y) {
		for (Gene gene : dData.visibleGenes) {
			if (gene.getDisplayGeometry().shape.contains(x, y)) {
				return gene;
			}
		}
		return null;
	}

	private Strain findIndicatedStrain(int x, int y) {
		for (Strain strain : dData.visibleStrains) {
			if (strain.getDisplayGeometry().shape.contains(x, y)) {
				return strain;
			}
		}
		return null;
	}

	/*
	 * Look for objects completely surrounded by selection box and select them
	 */
	/**
	 * 
	 */
	private void selectReadsFromBox() {
		if (selectionBox.getWidth() == 0 || selectionBox.getHeight() == 0) {
			// do nothing if box has no area
			return;
		}

		// loop over visible strains
		for (Strain strain : dData.visibleStrains) {
			if (selectionBox.intersects(strain.getDisplayGeometry().shape
					.getBounds2D())) {
				if (selectionBox.contains(strain.getDisplayGeometry().shape
						.getBounds2D())) {
					// if entire strain boxed, select all reads
					strain.addToSelectedList(dData);
				} else {
					// if only part of strain boxed, look inside for boxed reads
					Iterator<Readable> rit = ((StrainDisplayGeometry) strain
							.getDisplayGeometry())
							.getBoxedReadsIterator(selectionBox);
					while (rit.hasNext()) {
						// System.out.println("add to selected objects");
						rit.next().addToSelectedList(dData);
					}
				}
			}
		}
	}

	private void setSelectionBox(int x1, int y1, int x2, int y2) {
		double x, y, w, h;
		if (x1 > x2) {
			x = x2;
			w = x1 - x2;
		} else {
			x = x1;
			w = x2 - x1;
		}
		if (y1 > y2) {
			y = y2;
			h = y1 - y2;
		} else {
			y = y1;
			h = y2 - y1;
		}
		selectionBox.setRect(x, y, w, h);
	}

	// //////////////////////
	// MouseListener methods

	// Handles the event of the user pressing down the mouse button.
	public void mouseClicked(MouseEvent e) {
		if (dData == null
				|| !(e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3)) {
			return;
		}

		int x = e.getX();
		int y = e.getY();

		// Under unix, this seems to be called at mouseUp, so abort if a box has
		// been drawn
		// check to see if a box was drawn and it is bigger than rowHeight
		if (dragState == DRAG_SELECTION_RECTANGLE) {
			setSelectionBox(lastPressX, lastPressY, e.getX(), e.getY());
			if (selectionBox.getHeight() > dData.rowHeight
					|| selectionBox.getWidth() > dData.rowHeight) {
				// if user drew a box, don't select anything here, just quit
				return;
			}
		} else if (dragState == DRAG_VIEW_WINDOW) {
			setSelectionBox(lastPressX, lastPressY, e.getX(), e.getY());
			if (selectionBox.getWidth() > 2) {
				// if user drew a box, don't select anything here, just quit
				return;
			}
		}

		// System.out.println(" click at " + x + "," + y);

		if (y < dData.border) {
			return;
		}

		int ctrlClick = InputEvent.CTRL_MASK;
		if (e.getButton() == MouseEvent.BUTTON3
				|| (e.getModifiers() & ctrlClick) == ctrlClick) {

			if (y >= dData.refSeqAreaHeight + dData.geneAreaHeight
					&& dData.drawDiffTicks) {
				AlignedSequence aSeq;

				// Checks whether or not the cursor is inside a strain
				Strain strain = findIndicatedStrain(x, y);
				Readable read = null;
				if (strain != null) {
					// check reads
					read = ((StrainDisplayGeometry) strain.getDisplayGeometry())
							.findIndicatedRead(x, y);

					if (read instanceof Clone) {
						Clone clone = (Clone) read;
						if (clone.reads[0].getDisplayGeometry().visible
								&& clone.reads[0].getDisplayGeometry()
										.getBounds2D().contains(x, y)) {
							read = clone.reads[0];
						} else if (clone.reads[1].getDisplayGeometry().visible
								&& clone.reads[1].getDisplayGeometry()
										.getBounds2D().contains(x, y)) {
							read = clone.reads[1];
						}
					}

					if (read != null) {
						aSeq = read;
					} else {
						// if
						// (DisplaySettings.getDisplaySettings().isStrainDiffsOn())
						// {
						aSeq = strain;
						// } else {
						// return;
						// }
					}

					int pos = dData.getZoomedDataPositionFromX(x);
					// check for diff
					Difference diff = null;
					if (!(aSeq instanceof Strain)) {
						diff = aSeq.getAlignment().getDiffAtReferencePosition(
								pos);
					}
					if (diff != null) {
						// change ref
						int response = JOptionPane
								.showConfirmDialog(
										PaneledReferenceSequenceDisplay.frame,
										"Change reference sequence to have a '"
												+ diff.getBase2()
												+ "' at pos "
												+ diff.getPosition1()
												+ "\n Warning: generated diffs will have no quality data",
										"Update Reference?",
										JOptionPane.YES_NO_OPTION);

						if (response == JOptionPane.YES_OPTION) {
							dData.referenceSequence.setBase(
									diff.getPosition1(), diff.getBase2(),
									diff.getBase1());

							// diff ticks will need to be updated
							recalcShapes = true;
						}

					} else {
						String name = "";
						if (read != null) {
							name = "read " + read.getName();
						} else {
							name = "strain no " + strain.getIdInteger();
						}

						// change ref for all diffs in seq (read or strain)
						int response = JOptionPane
								.showConfirmDialog(
										PaneledReferenceSequenceDisplay.frame,
										"Change reference sequence to match the sequence of "
												+ name
												+ ". \n Warning: generated diffs will have no quality data",
										"Update Reference?",
										JOptionPane.YES_NO_OPTION);

						if (response == JOptionPane.YES_OPTION) {
							dData.referenceSequence.setToSequence(aSeq);

							// diff ticks will need to be updated
							recalcShapes = true;
						}

					}
				}
			}

		} else if (e.getButton() == MouseEvent.BUTTON1) {

			if (y < dData.refSeqAreaHeight) {
				goToPosition(dData.getDataPositionFromX(x));
			} else if (y < dData.refSeqAreaHeight + dData.geneAreaHeight) {
				Gene gene = findIndicatedGene(x, y);
				updateSelectedGene(gene);
			} else {
				// Checks whether or not the cursor is inside a strain
				Strain strain = findIndicatedStrain(x, y);
				Readable read = null;
				if (strain != null) {
					// check reads
					read = ((StrainDisplayGeometry) strain.getDisplayGeometry())
							.findIndicatedRead(x, y);

					if (read != null) {
						updateSelectedObjects(read);
						if (e.getClickCount() == 2) {
							if (read instanceof Clone) {
								Clone clone = (Clone) read;
								if (clone.reads[0].getDisplayGeometry().visible
										&& clone.reads[0].getDisplayGeometry()
												.getBounds2D().contains(x, y)) {
									updateSelectedObjects(clone.reads[0]);
								} else if (clone.reads[1].getDisplayGeometry().visible
										&& clone.reads[1].getDisplayGeometry()
												.getBounds2D().contains(x, y)) {
									updateSelectedObjects(clone.reads[1]);
								}
							}
						}
					} else {
						updateSelectedObjects(strain);
						if (e.getClickCount() == 2) {
							strain.toggleOpen();
							restack = true;
							recalcShapes = true;
						}
					}
				}
			}

			parent.updateDisplay(dData);
		}

		// apply changes
		repaint();
	}

	public void mouseExited(MouseEvent e) {
		// System.out.println("exited");
	}

	public void mouseEntered(MouseEvent e) {
		// System.out.println("entered");
	}

	public void mousePressed(MouseEvent e) {
		if (dData == null) {
			return;
		}

		lastPressX = e.getX();
		lastPressY = e.getY();
		if (e.getY() < dData.refSeqAreaHeight) {
			if (dData.displayRegion.contains(e.getX(), e.getY())) {
				dragState = DRAG_VIEW_WINDOW;
			} else {
				dragState = NO_DRAG_STATE;
			}
		} else if (e.getY() < dData.refSeqAreaHeight + dData.geneAreaHeight) {
			dragState = NO_DRAG_STATE;
		} else {
			setSelectionBox(lastPressX, lastPressY, e.getX(), e.getY());
			int ctrlClick = InputEvent.CTRL_DOWN_MASK
					| InputEvent.BUTTON1_DOWN_MASK;
			if (e.getButton() == MouseEvent.BUTTON3
					|| (e.getModifiersEx() & ctrlClick) == ctrlClick) {
				dragState = DRAG_ZOOM_REGION;
			} else if (e.getButton() == MouseEvent.BUTTON1) {
				dragState = DRAG_SELECTION_RECTANGLE;
			} else {
				dragState = DRAG_ZOOM_REGION;
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (dData == null) {
			return;
		}

		if (dragState == DRAG_VIEW_WINDOW) {
			goToPosition(dData.getDataPositionFromX(e.getX()));
			recalcRegion = true;
			recalcGenes = true;
			recalcShapes = true;
			repaint();
		} else if (dragState == DRAG_ZOOM_REGION) {
			setSelectionBox(lastPressX, dData.refSeqAreaHeight + 1, e.getX(),
					this.getHeight());
			if (selectionBox.getWidth() > 2) {
				zoomToSelectedRegion();
				parent.updateDisplay(dData);
				repaint();
			}
		} else if (dragState == DRAG_SELECTION_RECTANGLE) {
			setSelectionBox(lastPressX, lastPressY, e.getX(), e.getY());
			selectReadsFromBox();
			parent.updateDisplay(dData);
			repaint();
		}
		dragState = NO_DRAG_STATE;
	}

	public void mouseMoved(MouseEvent e) {
		/**/
	}

	public void mouseDragged(MouseEvent e) {
		if (dData == null) {
			return;
		}

		if (dragState == DRAG_VIEW_WINDOW) {
			goToPosition(dData.getDataPositionFromX(e.getX()));
			recalcRegion = true;
			repaint();
		} else if (dragState == DRAG_ZOOM_REGION) {
			setSelectionBox(lastPressX, dData.refSeqAreaHeight + 1, e.getX(),
					this.getHeight());
			// System.out.println("box: " + selectionBox.toString());
			repaint();
		} else if (dragState == DRAG_SELECTION_RECTANGLE) {
			setSelectionBox(lastPressX, lastPressY, e.getX(), e.getY());
			repaint();
		}
	}

	// ///////////////////////
	// Tool tip API

	/**
	 * Called by JAVA when mouse hovers over this component. Here we figure out
	 * what text to display the popup (tooltip) and return it.
	 */
	@Override
	public String getToolTipText(MouseEvent me) {
		// figure out what the mouse is over
		int x = me.getX();
		int y = me.getY();

		if (dData == null || x <= dData.border
				|| x >= dData.getWidth() - dData.border) {
			// if nothing is loaded, return some default string
			// also, don't do anything if we're in the border region
			return toolTipBaseText;
		} else {
			// build string gradually
			StringBuffer sb = new StringBuffer();

			// if we have data, always return reference sequence name and
			// position
			sb.append("<html><body>");
			sb.append(toolTipBaseText);
			sb.append("<br>Position: ");
			char refSeqBase = ' ';
			int pos;
			if (y < dData.refSeqAreaHeight) {
				// if we're in the nav bar at the top, use nav scale
				pos = dData.getDataPositionFromX(x);
				sb.append(pos);
			} else {
				// other wise use scale from main display
				pos = dData.getZoomedDataPositionFromX(x);
				sb.append(pos);
				if (dData.drawDiffTicks) {
					refSeqBase = dData.referenceSequence.getBase(pos);
					sb.append("<br>reference sequence base: ");
					sb.append(refSeqBase);
				}
				if (y < dData.refSeqAreaHeight + dData.geneAreaHeight) {
					// is mouse over a gene?
					Gene gene = findIndicatedGene(x, y);
					if (gene != null) {
						sb.append("<br>Gene: ").append(gene.getName());
					}
				} else {
					// Checks whether or not the cursor is inside a strain
					Strain strain = findIndicatedStrain(x, y);
					Readable read = null;
					if (strain != null) {
						if (DisplaySettings.getDisplaySettings()
								.isStrainDiffsOn() && dData.drawDiffTicks) {
							char strainBase = strain.getAlignment()
									.getBaseFromReference(pos);
							sb.append("<br>strain base: ").append(strainBase);
						}

						// check reads
						read = ((StrainDisplayGeometry) strain
								.getDisplayGeometry()).findIndicatedRead(x, y);
						if (read != null) {
							if (dData.drawDiffTicks) {
								// instead of calling
								// alignment.getBaseFromReferencePos.
								// we get the diff, so we can get qual info, if
								// it's here
								Difference d = read.getAlignment()
										.getDiffAtReferencePosition(pos);
								if (d == null) {
									// no diff here, can use refSeqBase
									sb.append("<br>read base: ").append(
											refSeqBase);
								} else {
									// if read has extra base, insert reference
									// base
									StringBuffer insertRef = new StringBuffer(1);
									if (d.getBase1() == '-') {
										insertRef.append(refSeqBase);
									}

									if (d instanceof QualifiedDifference) {
										sb.append("<br>read base: ");
										char readbase = ((QualifiedDifference) d)
												.getBase2Actual();
										sb.append(insertRef).append(readbase);
										sb.append("<br>read base qulaity: ")
												.append(((QualifiedDifference) d)
														.getQuality());
									} else {
										sb.append("<br>read base: ")
												.append(d.getBase2())
												.append(insertRef);
									}
								}
							}
							sb.append("<br>Read: ");
							if (read instanceof Clone) {
								Read r = ((CloneDisplayGeometry) read
										.getDisplayGeometry())
										.findIndicatedRead(x, y);
								if (r == null) {
									sb.append(read.toString());
								} else {
									sb.append(r.getName());
								}
							} else {
								sb.append(read.getName());
							}
							sb.append("<br>%ID: ").append(
									read.getAlignment().getIdentity());
						} else {
							sb.append("<br>Strain: ").append(strain.getId());
							if (DisplaySettings.getDisplaySettings()
									.isStrainDiffsOn()) {
								// only if sorting/coloring is set up that %ID
								// is already calculated
								sb.append("<br>%ID: ").append(
										strain.getAlignment().getIdentity());
							}
						}

					}
				}
			}
			sb.append("</body></html>");
			return sb.toString();
		}
	}

	// fired by viewport if scrollpane moved
	public void stateChanged(ChangeEvent e) {
		if (dData != null) {
			try {
				JViewport port = (JViewport) e.getSource();
				// re-draw objects
				recalcShapes = true;
				double y1 = port.getViewRect().getMinY();
				double y2 = port.getViewRect().getMaxY();
				double[] range = { y1, y2 };
				dData.setVerticalVisibleRange(range);
				repaint();
			} catch (ClassCastException cce) {
				// ignore (shouldn't ever get here unless canvas added as change
				// listener of something else)
			}
		}
	}

}
