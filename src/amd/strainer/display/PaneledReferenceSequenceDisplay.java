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

/*
 * This implementation of ReferenceSequenceDisplay provides a bunch of menu options and a
 * output text box for status.
 */
package amd.strainer.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import amd.strainer.display.actions.BatchAssemblyImporDialog;
import amd.strainer.display.actions.BatchBlastDialog;
import amd.strainer.display.actions.DisplayOptionsAction;
import amd.strainer.display.actions.ExportReferenceToFasta;
import amd.strainer.display.actions.GetSelectionListAction;
import amd.strainer.display.actions.GetSequencesAction;
import amd.strainer.display.actions.GetVariantsDialog;
import amd.strainer.display.actions.GoToAction;
import amd.strainer.display.actions.LoadDataDialog;
import amd.strainer.display.actions.QualityDataDialog;
import amd.strainer.display.actions.SaveStrainsAction;
import amd.strainer.display.actions.SequenceDataLoader;
import amd.strainer.display.util.Util;
import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Clone;
import amd.strainer.objects.Gene;
import amd.strainer.objects.Readable;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

public class PaneledReferenceSequenceDisplay implements
		ReferenceSequenceDisplay {

	// master window, frame, or applet
	public static Container frame = null;

	// current reference sequence (all read data is tied to refrence)
	ReferenceSequence currentReferenceSequence = null;

	/**
	 * Returns the ReferenceSequence object currently displayed. Null if no data
	 * loaded
	 * 
	 * @see amd.strainer.display.ReferenceSequenceDisplay#getReferenceSequence()
	 */
	public ReferenceSequence getReferenceSequence() {
		return currentReferenceSequence;
	}

	public void setReferenceSequence(ReferenceSequence pRefSeq) {
		if (currentReferenceSequence != null) {
			currentReferenceSequence.close();
		}

		currentReferenceSequence = pRefSeq;
		canvas.drawReferenceSequence(pRefSeq);

		if (pRefSeq != null) {
			infoArea.insert("Loaded reference sequence: " + pRefSeq.getName()
					+ "\n", 0);
			enableActions(true);
		} else {
			enableActions(false);
		}
	}

	/**
	 * @return SequenceSegment indicating the current reference sequence and
	 *         start end pints of the display
	 */
	public SequenceSegment getSequenceSegment() {
		return canvas.getSequenceSegment();
	}

	boolean disableAll = false;

	// UI features
	JPanel bottomPanel = new JPanel();
	JPanel controlPanel = new JPanel();

	// actions
	Action panLeftAction, panRightAction, zoomInAction, zoomOutAction,
			loadAction, saveStrainsAction, importStrainsAction,
			clearSelectionAction, selectAllAction, getSelectionSequenceAction,
			getSelectionListAction, makeStrainAction, getVariantsAction,
			exitAction, findMatePairsAction, undoStrainsAction,
			redoStrainsAction, biggerRowsAction, smallerRowsAction,
			getAllMatePairsAction, toggleRecombAction, toggleAllRecombAction,
			selectionInfoAction, importQualityDataAction, displayOptionsAction,
			goToAction, printCanvasAction, exportFastaAction, batchBlastAction,
			batchAceAction;
	// Action pathViewAction;

	JMenuBar menuBar = new JMenuBar();
	JMenu refSeqMenu = new JMenu("Data");
	JMenu viewMenu = new JMenu("View");
	JMenu selectionMenu = new JMenu("Selection");
	JMenu autoStrainMenu = new JMenu("Auto");
	JToolBar toolBar = new JToolBar();

	// THE CANVAS Where everything is displayed
	// JPanel canvasPanel = new JPanel();
	ReferenceSequenceDisplayComponent canvas = new ReferenceSequenceDisplayComponent(
			this);
	JScrollPane canvasPane = new JScrollPane(
			canvas,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

	public JScrollPane getCanvasView() {
		return canvasPane;
	}

	// progressbar to show whats up
	public JProgressBar progressBar = new JProgressBar();

	// text boxes to show info
	JLabel label = new JLabel("Information on selected object:");
	JTextArea infoArea = new JTextArea(5, 40);
	JScrollPane infoPane = new JScrollPane(
			infoArea,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

	private AlignedSequence selectedObject = null;
	private Gene selectedGene = null;

	public void buildInFrame(JFrame pFrame) {
		frame = pFrame.getContentPane();
		frame.add(content);
	}

	private final JPanel content = new JPanel();

	public PaneledReferenceSequenceDisplay() {
		// System.out.println("adding ocmponents to frame");

		canvasPane.getViewport().addChangeListener(canvas);

		content.setLayout(new BorderLayout());

		// create Action objects for panning
		panLeftAction = new PanLeftAction(canvas);
		panRightAction = new PanRightAction(canvas);
		zoomInAction = new ZoomInAction(canvas);
		zoomOutAction = new ZoomOutAction(canvas);
		biggerRowsAction = new BiggerRowsAction(canvas);
		smallerRowsAction = new SmallerRowsAction(canvas);
		goToAction = new GoToAction(this, canvas);
		displayOptionsAction = new DisplayOptionsAction(canvas);

		// create Actions for loading and saving data
		loadAction = new LoadDataDialog.ShowDialogAction(this);
		exportFastaAction = new ExportReferenceToFasta(canvas);
		saveStrainsAction = new SaveStrainsAction(this, canvas);
		importStrainsAction = new amd.strainer.display.actions.ApplyStrainsFromFileAction(
				canvas);
		importQualityDataAction = new QualityDataDialog.ShowDialogAction(
				this,
				canvas);

		// create batch actions
		batchAceAction = new BatchAssemblyImporDialog.ShowDialogAction(this);
		batchBlastAction = new BatchBlastDialog.ShowDialogAction(this);

		// actions to operate on selected objects
		clearSelectionAction = new ClearSelectionAction(canvas);
		selectAllAction = new SelectAllAction(canvas);
		getSelectionSequenceAction = new GetSequencesAction(frame, canvas);
		getSelectionListAction = new GetSelectionListAction(frame, canvas);
		makeStrainAction = new MakeStrainAction(canvas);
		getVariantsAction = new GetVariantsDialog.ShowDialogAction(this, canvas);
		selectionInfoAction = new SelectionInfoAction(this, canvas);
		getAllMatePairsAction = new GetAllMatePairsAction(canvas);
		findMatePairsAction = new FindMatePairsAction(frame, canvas);
		undoStrainsAction = new UndoStrainsAction(canvas);
		redoStrainsAction = new RedoStrainsAction(canvas);
		toggleRecombAction = new ToggleRecombAction(this, canvas);
		toggleAllRecombAction = new ToggleAllRecombAction(this, canvas);

		// action to close everything
		exitAction = new ExitAction(this);

		// action to print canvas
		printCanvasAction = new PrintCanvasAction(canvas);

		// set keyboard shortcuts
		canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK),
				"Ctrl-O");
		canvas.getActionMap().put("Ctrl-O", loadAction);
		canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK),
				"Ctrl-S");
		canvas.getActionMap().put("Ctrl-S", saveStrainsAction);
		canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK),
				"Ctrl-Q");
		canvas.getActionMap().put("Ctrl-Q", exitAction);
		canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK),
				"Ctrl-G");
		canvas.getActionMap().put("Ctrl-G", goToAction);
		canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK),
				"Ctrl-P");
		canvas.getActionMap().put("Ctrl-P", printCanvasAction);
		canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK),
				"Ctrl-R");
		canvas.getActionMap().put("Ctrl-R", toggleAllRecombAction);
		canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK),
				"Ctrl-M");
		canvas.getActionMap().put("Ctrl-M", makeStrainAction);
		canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK),
				"Ctrl-Z");
		canvas.getActionMap().put("Ctrl-Z", undoStrainsAction);

		Object[] dataItems = { loadAction, saveStrainsAction,
				importStrainsAction, null, importQualityDataAction,
				exportFastaAction, null, printCanvasAction, null,
				batchBlastAction, batchAceAction, null, exitAction };
		Object[] viewItems = { panLeftAction, panRightAction, zoomInAction,
				zoomOutAction, smallerRowsAction, biggerRowsAction, goToAction,
				displayOptionsAction };
		Object[] selectionItems = { undoStrainsAction, redoStrainsAction,
				makeStrainAction, getSelectionSequenceAction,
				getSelectionListAction, findMatePairsAction,
				toggleRecombAction, toggleAllRecombAction,
				clearSelectionAction, selectAllAction, selectionInfoAction };
		Object[] autoStrainItems = { getVariantsAction, getAllMatePairsAction };
		Action[] toolBarActions = { loadAction, saveStrainsAction,
				panLeftAction, panRightAction, zoomInAction, zoomOutAction,
				clearSelectionAction, makeStrainAction, undoStrainsAction,
				redoStrainsAction, goToAction };

		// build menu
		buildMenu(refSeqMenu, dataItems);
		buildMenu(viewMenu, viewItems);
		buildMenu(selectionMenu, selectionItems);
		buildMenu(autoStrainMenu, autoStrainItems);
		menuBar.add(refSeqMenu);
		menuBar.add(viewMenu);
		menuBar.add(selectionMenu);
		menuBar.add(autoStrainMenu);
		content.add(menuBar, BorderLayout.PAGE_START);

		// add actions to toolbar
		buildToolBar(toolBar, toolBarActions);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		menuBar.add(toolBar);
		menuBar.add(progressBar);
		Action progressCancelAction = SequenceDataLoader
				.getProgressCancelAction();
		JButton progressCancelButton = new JButton(progressCancelAction);
		menuBar.add(progressCancelButton);

		// add canvas (inside scrolling pane)
		content.add(canvasPane, BorderLayout.CENTER);

		// add components below canvas
		// text area to disply info to user (inside scrolling pane)
		infoArea.setEditable(false);
		content.add(infoPane, BorderLayout.PAGE_END);

		setReferenceSequence(null);
	}

	void buildMenu(JMenu pMenu, Object[] pItems) {
		for (int i = 0; i < pItems.length; i++) {
			if (pItems[i] == null) {
				pMenu.addSeparator();
			} else if (pItems[i] instanceof Action) {
				pMenu.add((Action) pItems[i]);
			} else if (pItems[i] instanceof JMenuItem) {
				pMenu.add((JMenuItem) pItems[i]);
			} else if (pItems[i] instanceof Component) {
				pMenu.add((Component) pItems[i]);
			} else {
				pMenu.add(pItems[i].toString());
			}
		}
	}

	void buildToolBar(JToolBar pBar, Action[] pActions) {
		for (int i = 0; i < pActions.length; i++) {
			JButton button = new JButton(pActions[i]);
			if (button.getIcon() != null) {
				button.setText(""); // an icon-only button
			}
			pBar.add(button);
		}
	}

	// .... add funtionality

	/**
	 * Check for unsaved changes. Ask user before closing window.
	 */
	public void exitWithSaveCheck() {
		if (!checkSaveStatus()) {
			int response = JOptionPane.showConfirmDialog(
					frame,
					"Save strains?",
					"Unsaved data!",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (response == JOptionPane.CANCEL_OPTION) {
				// do nothing (cancel exit)
				return;
			} else if (response == JOptionPane.YES_OPTION) {
				// save strains
				saveStrainsAction.actionPerformed(null);

				// if still unsaved, user must have hit cancel...do nothing
				// (cancel exit)
				if (!checkSaveStatus()) {
					return;
				}

				// otherwise, the strains are saved and we can exit...
			}
		}
		// exit
		System.exit(0);
	}

	private boolean checkSaveStatus() {
		// return true if nothing has changed since last save
		return !canvas.dData.undoData.hasUndoMoves();
	}

	/**
	 * Notifies this (main widow) that the data has changed and the display
	 * should be updated
	 */
	public void updateDisplay(DisplayData pData) {
		updateActions();
		updateTextBox(pData);
	}

	void updateTextBox(DisplayData pData) {
		if (pData.selectedObject != selectedObject
				&& pData.selectedObject != null) {
			selectedObject = pData.selectedObject;
			StringBuffer line = new StringBuffer();
			if (pData.selectedObject instanceof Strain) {
				// line.append("Strain " + pData.selectedObject.getId() + ": ");
				line.append(pData.selectedObject.detailsString());
				if (DisplaySettings.getDisplaySettings().isStrainDiffsOn()) {
					line
							.append("\nStrain % identity to reference: "
									+ pData.selectedObject
											.getAlignment()
											.getIdentity());
				}
			} else {
				Readable read = (Readable) pData.selectedObject;

				if (read instanceof Clone) {
					line.append("Clone ");
					line.append(read.toString());
					line.append(" in Strain ");
					line.append(read.getStrain().getId());
					line.append(" with %ID: ");
					line.append(read.getAlignment().getIdentity());
				} else {
					line.append(read.toString());
					line.append(" in Strain ");
					line.append(read.getStrain().getId());
					line.append(" with %ID: ");
					line.append(read.getAlignment().getIdentity());
					if (read.getMatePair() != null) {
						line.append("\nMatePair ");
						line.append(read.getMatePair().getName());
						line.append(" in Strain ");
						line.append(read.getMatePair().getStrain().getId());
						line.append(" with %ID: ");
						line.append(read.getAlignment().getIdentity());
					} else {
						line.append("\nNo matePair.");
					}
				}
			}
			line.append("\n");
			infoArea.insert(line.toString(), 0);
			infoArea.setCaretPosition(0);
		}
		if (pData.selectedGene != selectedGene && pData.selectedGene != null) {
			selectedGene = pData.selectedGene;
			StringBuffer line = new StringBuffer();
			line.append("Gene ");
			line.append(selectedGene.toString());
			line.append("\n");
			infoArea.insert(line.toString(), 0);
			infoArea.setCaretPosition(0);
		}
	}

	public void updateDisplayWithString(String pText) {
		infoArea.insert(pText + "\n", 0);
		infoArea.setCaretPosition(0);
	}

	public void disableAllActions() {
		disableAll = true;
		enableActions(false);
	}

	public void enableAllActions() {
		disableAll = false;
		updateActions();
	}

	public void updateActions() {
		DisplayData data = canvas.dData;
		if (data == null) {
			enableActions(false);
			return;
		}
		boolean refSeq = data.referenceSequence != null;
		if (refSeq) {
			boolean selection = data.selectedObject != null;
			boolean selections = selection
					|| data.selectedReadList.getSize() > 0;
			boolean change = data.undoData.hasUndoMoves();
			boolean redo = data.undoData.hasRedoMoves();
			boolean gene = data.selectedGene != null;
			enableActions(refSeq, selection, selections, change, redo, gene);
		} else {
			enableActions(false);
		}
	}

	private void enableActions(boolean pRefSeq) {
		enableActions(pRefSeq, false, false, false, false, false);
	}

	private void enableActions(boolean pRefSeq, boolean pSelection,
			boolean pSelections, boolean pChange, boolean pRedo, boolean pGene) {

		if (disableAll) {

			loadAction.setEnabled(false);
			saveStrainsAction.setEnabled(false);
			importQualityDataAction.setEnabled(false);
			exportFastaAction.setEnabled(false);

			batchAceAction.setEnabled(false);
			batchBlastAction.setEnabled(false);

			panLeftAction.setEnabled(false);
			panRightAction.setEnabled(false);
			zoomInAction.setEnabled(false);
			zoomOutAction.setEnabled(false);
			smallerRowsAction.setEnabled(false);
			biggerRowsAction.setEnabled(false);
			displayOptionsAction.setEnabled(false);
			goToAction.setEnabled(false);

			selectAllAction.setEnabled(false);
			undoStrainsAction.setEnabled(false);
			redoStrainsAction.setEnabled(false);
			makeStrainAction.setEnabled(false);
			getSelectionSequenceAction.setEnabled(false);
			getSelectionListAction.setEnabled(false);
			findMatePairsAction.setEnabled(false);
			toggleRecombAction.setEnabled(false);
			toggleAllRecombAction.setEnabled(false);
			clearSelectionAction.setEnabled(false);
			selectionInfoAction.setEnabled(false);

			getVariantsAction.setEnabled(false);
			getAllMatePairsAction.setEnabled(false);

		} else {

			loadAction.setEnabled(true);
			saveStrainsAction.setEnabled(pRefSeq);
			importQualityDataAction.setEnabled(pRefSeq);
			exportFastaAction.setEnabled(pRefSeq);

			batchAceAction.setEnabled(true);
			batchBlastAction.setEnabled(true);

			panLeftAction.setEnabled(pRefSeq);
			panRightAction.setEnabled(pRefSeq);
			zoomInAction.setEnabled(pRefSeq);
			zoomOutAction.setEnabled(pRefSeq);
			smallerRowsAction.setEnabled(pRefSeq);
			biggerRowsAction.setEnabled(pRefSeq);
			displayOptionsAction.setEnabled(true);
			goToAction.setEnabled(pRefSeq);

			selectAllAction.setEnabled(pRefSeq);
			undoStrainsAction.setEnabled(pChange);
			redoStrainsAction.setEnabled(pRedo);
			makeStrainAction.setEnabled(pSelections);
			getSelectionSequenceAction.setEnabled(pRefSeq); // this seems odd,
			// but
			// getSelectionSequence
			// can do things if
			// nothing is
			// selected
			getSelectionListAction.setEnabled(pSelections);
			findMatePairsAction.setEnabled(pSelection);
			toggleRecombAction.setEnabled(pSelection);
			toggleAllRecombAction.setEnabled(pSelections);
			clearSelectionAction.setEnabled(pSelections || pGene);
			selectionInfoAction.setEnabled(pSelection);

			getVariantsAction.setEnabled(pRefSeq);
			getAllMatePairsAction.setEnabled(pRefSeq);

		}
	}

	public void printDetails(AlignedSequence pSelection) {
		infoArea.insert(pSelection.detailsString(), 0);
		infoArea.setCaretPosition(0);
	}

	public void buildInApplet(ReferenceSequenceDisplayApplet applet) {
		frame = applet.getContentPane();
		frame.add(content);
	}

}

class ExitAction extends AbstractAction {
	private static final long serialVersionUID = 8845670801900973647L;
	PaneledReferenceSequenceDisplay parent = null;

	ExitAction(PaneledReferenceSequenceDisplay pParent) {
		super("Exit");
		parent = pParent;
		putValue(SHORT_DESCRIPTION, "Close this program.");
	}

	public void actionPerformed(ActionEvent e) {
		parent.exitWithSaveCheck();
	}
}

class PanLeftAction extends AbstractAction {
	ReferenceSequenceDisplayComponent canvas = null;
	String iconLoc = "/toolbarButtonGraphics/navigation/Back16.gif";
	URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);

	PanLeftAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Pan Left");
		putValue(SHORT_DESCRIPTION, "Move view to the left");
		putValue(SMALL_ICON, new ImageIcon(iconURL));
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.panLeft();
	}
}

class PanRightAction extends AbstractAction {
	ReferenceSequenceDisplayComponent canvas = null;
	String iconLoc = "/toolbarButtonGraphics/navigation/Forward16.gif";
	URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);

	PanRightAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Pan Right");
		putValue(SHORT_DESCRIPTION, "Move view to the right");
		putValue(SMALL_ICON, new ImageIcon(iconURL));
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.panRight();
	}
}

class ZoomInAction extends AbstractAction {
	ReferenceSequenceDisplayComponent canvas = null;
	String iconLoc = "/toolbarButtonGraphics/general/ZoomIn16.gif";
	URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);

	ZoomInAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Zoom In");
		putValue(SHORT_DESCRIPTION, "Zoom in");
		putValue(SMALL_ICON, new ImageIcon(iconURL));
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.zoomIn();
	}
}

class ZoomOutAction extends AbstractAction {
	ReferenceSequenceDisplayComponent canvas = null;
	String iconLoc = "/toolbarButtonGraphics/general/ZoomOut16.gif";
	URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);

	ZoomOutAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Zoom Out");
		putValue(SHORT_DESCRIPTION, "Zoom out");
		putValue(SMALL_ICON, new ImageIcon(iconURL));
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.zoomOut();
	}
}

class BiggerRowsAction extends AbstractAction {
	ReferenceSequenceDisplayComponent canvas = null;

	BiggerRowsAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Bigger Rows");
		putValue(SHORT_DESCRIPTION, "increase vertical size of objects");
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.biggerRows();
	}
}

class SmallerRowsAction extends AbstractAction {
	ReferenceSequenceDisplayComponent canvas = null;

	SmallerRowsAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Smaller Rows");
		putValue(SHORT_DESCRIPTION, "decrease vertical size of objects");
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.smallerRows();
	}
}

class ClearSelectionAction extends AbstractAction {
	ReferenceSequenceDisplayComponent canvas = null;
	String iconLoc = "/toolbarButtonGraphics/general/New16.gif";
	URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);

	ClearSelectionAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Clear");
		putValue(SHORT_DESCRIPTION, "Unselect all objects.");
		putValue(SMALL_ICON, new ImageIcon(iconURL));
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.clearSelections();
	}
}

class SelectAllAction extends AbstractAction {
	PaneledReferenceSequenceDisplay parent = null;
	ReferenceSequenceDisplayComponent canvas = null;

	// String iconLoc = "/toolbarButtonGraphics/general/New16.gif";
	// URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);

	SelectAllAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Select All");
		putValue(SHORT_DESCRIPTION, "Select all objects.");
		// putValue(SMALL_ICON,new ImageIcon(iconURL));
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.selectAllReads();
	}
}

class MakeStrainAction extends AbstractAction {
	ReferenceSequenceDisplayComponent canvas = null;
	String iconLoc = "/toolbarButtonGraphics/general/Add16.gif";
	URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);

	MakeStrainAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Make Strain");
		putValue(SHORT_DESCRIPTION, "Make current selections into a strain.");
		putValue(SMALL_ICON, new ImageIcon(iconURL));
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.makeStrainFromSelection();
	}
}

class FindMatePairsAction extends AbstractAction {
	Component parent = null;
	ReferenceSequenceDisplayComponent canvas = null;

	FindMatePairsAction(Component pParent,
			ReferenceSequenceDisplayComponent pCanvas) {
		super("Get mate pairs");
		putValue(
				SHORT_DESCRIPTION,
				"Bring in all stray mate pairs for the selected strain.");
		canvas = pCanvas;
		parent = pParent;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			canvas.bringMatePairsIntoSelectedStrain();
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			Util.displayErrorMessage(parent, ex);
		}
	}
}

class GetAllMatePairsAction extends AbstractAction {
	ReferenceSequenceDisplayComponent canvas = null;

	GetAllMatePairsAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Get All Mate Pairs");
		putValue(
				SHORT_DESCRIPTION,
				"Automatically pull mate pairs into all strains.");
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.getAllMatePairsBySize();
	}
}

class SelectionInfoAction extends AbstractAction {
	PaneledReferenceSequenceDisplay parent = null;
	ReferenceSequenceDisplayComponent canvas = null;

	SelectionInfoAction(PaneledReferenceSequenceDisplay pParent,
			ReferenceSequenceDisplayComponent pCanvas) {
		super("Details");
		putValue(SHORT_DESCRIPTION, "Print details about current selection.");
		parent = pParent;
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		parent.printDetails(canvas.dData.selectedObject);
	}
}

class ToggleRecombAction extends AbstractAction {
	PaneledReferenceSequenceDisplay parent = null;
	ReferenceSequenceDisplayComponent canvas = null;

	public ToggleRecombAction(PaneledReferenceSequenceDisplay pParent,
			ReferenceSequenceDisplayComponent pCanvas) {
		super("Toggle Recombinant");
		putValue(
				SHORT_DESCRIPTION,
				"Specify whether you think this read or clone contains a recombination event.");
		parent = pParent;
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.labelRecomb();
	}
}

class ToggleAllRecombAction extends AbstractAction {
	PaneledReferenceSequenceDisplay parent = null;
	ReferenceSequenceDisplayComponent canvas = null;

	public ToggleAllRecombAction(PaneledReferenceSequenceDisplay pParent,
			ReferenceSequenceDisplayComponent pCanvas) {
		super("Toggle All Recombinant");
		putValue(
				SHORT_DESCRIPTION,
				"Toggle the recombinant status of the all selected reads or clones.");
		parent = pParent;
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.labelAllRecomb();
	}
}

class UndoStrainsAction extends AbstractAction {
	ReferenceSequenceDisplayComponent canvas = null;
	String iconLoc = "/toolbarButtonGraphics/general/Undo16.gif";
	URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);

	UndoStrainsAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Undo");
		putValue(SHORT_DESCRIPTION, "Undo last change to strain assignments.");
		putValue(SMALL_ICON, new ImageIcon(iconURL));
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.undo();
	}
}

class RedoStrainsAction extends AbstractAction {
	ReferenceSequenceDisplayComponent canvas = null;
	String iconLoc = "/toolbarButtonGraphics/general/Redo16.gif";
	URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);

	RedoStrainsAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Redo");
		putValue(
				SHORT_DESCRIPTION,
				"Redo last undone change to strain assignments.");
		putValue(SMALL_ICON, new ImageIcon(iconURL));
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		canvas.redo();
	}
}

class PrintCanvasAction extends AbstractAction {
	ReferenceSequenceDisplayComponent canvas = null;
	CanvasPrinter printer = null;

	final String printQuestion = "Do you want to force colored ticks?";
	final String printTitle = "Print ticks?";

	final String paperQuestion = "Create custom paper size to get entire image?\n(Use this if you''re creating a file.)";
	final String paperTitle = "Cusom paper?";

	final String orientQuestion = "Print with landscape orientation?";
	final String orientTitle = "Landscape?";

	PrintCanvasAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Print");
		putValue(SHORT_DESCRIPTION, "Print contents of canvas.");
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		printer = new CanvasPrinter(canvas);

		// force colored ticks?
		int response = JOptionPane.showOptionDialog(
				PaneledReferenceSequenceDisplay.frame,
				printQuestion,
				printTitle,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				null,
				null);
		if (response == JOptionPane.YES_OPTION) {
			printer.setPrintDiffs(true);
		} else if (response == JOptionPane.NO_OPTION) {
			printer.setPrintDiffs(false);
		} else {
			return;
		}

		// custom paper size for print to file?
		response = JOptionPane.showOptionDialog(
				PaneledReferenceSequenceDisplay.frame,
				paperQuestion,
				paperTitle,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				null,
				null);
		if (response == JOptionPane.YES_OPTION) {
			printer.setCustomPaper(true);
			printer.setLandscape(false);
		} else if (response == JOptionPane.NO_OPTION) {
			printer.setCustomPaper(false);

			// if using normal paper, switch to landscape?
			response = JOptionPane.showOptionDialog(
					PaneledReferenceSequenceDisplay.frame,
					orientQuestion,
					orientTitle,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					null,
					null);
			if (response == JOptionPane.YES_OPTION) {
				printer.setLandscape(true);
			} else if (response == JOptionPane.NO_OPTION) {
				printer.setLandscape(false);
			} else {
				return;
			}
			printer.setLandscape(false);
		} else {
			return;
		}

		// print now
		printer.print();
	}
}
