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
package amd.strainer.display.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import amd.strainer.GlobalSettings;
import amd.strainer.algs.Config;
import amd.strainer.algs.SegmentStrainer;
import amd.strainer.algs.SegmentStrainerException;
import amd.strainer.algs.Util;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.ReferenceSequenceDisplayComponent;
import amd.strainer.objects.Gene;
import amd.strainer.objects.Read;
import amd.strainer.objects.Strain;

public class GetVariantsDialog extends JDialog {

	private JPanel jContentPane = null;

	private JPanel jButtonPanel = null;
	private JButton jCancelButton = null;
	private JButton jGoButton = null;

	private JPanel jLeftPanel = null;
	private JPanel jTopLeftPanel = null;
	private JPanel jBottomLeftPanel = null;

	private JPanel jSelectScopePanel = null;
	private JRadioButton jAllGenesButton = null;
	private JRadioButton jSelectedGeneButton = null;
	private JRadioButton jVisibleRegionButton = null;
	private JRadioButton jSelectedObjectsButton = null;

	private JPanel jOutputOptionsPanel = null;
	private JCheckBox jSaveToFastaCheckBox = null;
	// private JCheckBox jSaveToCsvCheckBox = null;
	private JCheckBox jGroupReadsCheckBox = null;

	private JPanel jSelectReadsPanel = null;
	private JRadioButton jAllReadsButton = null;
	private JRadioButton jSelectedReadsButton = null;

	private JPanel jRightPanel = null;
	private JPanel jTopRightPanel = null;
	private JPanel jBottomRightPanel = null;

	private JPanel jSettingsPanel = null;

	private JPanel jAlgorithmPanel = null;
	// private JLabel jAlgorithmLabel = null;
	private JComboBox jAlgorithmComboBox = null;
	private JButton jConfigureAlgorithmButton = null;
	private JButton jAddAlgorithmButton = null;
	private JButton jRemoveAlgorithmButton = null;

	// boolean fillFromConsensus = true;
	boolean writeToFasta = true;
	// boolean writeToCsv = true;
	boolean updateDisplay = false;
	double readCutoff = 1.0;
	double strainCutoff = 1.0;

	private JFileChooser fc = null;
	final String overwriteQuestion = " exists.  Do you want to overwrite it?";
	final String overwriteTitle = "File exists.";
	PrintWriter fastaFile = null;
	// PrintWriter csvFile = null;

	PaneledReferenceSequenceDisplay mParent = null;
	ReferenceSequenceDisplayComponent mCanvas = null;
	final static int ONE_SECOND = 1000;

	HashMap<String, Object> settings = Config.getConfig().getSettings();

	Task task;

	Timer timer;

	public static final int SUCCESS_RESPONSE = 1;
	public static final int CANCEL_RESPONSE = 0;

	// only allow one instance of the dialog, and keep it around, so settings
	// persist
	private static GetVariantsDialog dialog = null;

	/**
	 * setup and show the dialog
	 * 
	 * @param pParent
	 *            ReferenceSequenceDisplay object initiating this dialog
	 */
	public static int showDialog(PaneledReferenceSequenceDisplay pParent,
			ReferenceSequenceDisplayComponent pCanvas) {
		if (dialog == null) {
			dialog = new GetVariantsDialog(pParent, pCanvas);
		} else {
			dialog.task = null;
		}

		dialog.enableStuff();

		dialog.setVisible(true);

		if (dialog.task == null) {
			// quit if nothing chosen
			return CANCEL_RESPONSE;
		} else {
			SequenceDataLoader loader = new SequenceDataLoader(
					pParent,
					dialog.task);
			loader.load();

			// pParent.progressBar.setStringPainted(true);
			//						
			// dialog.timer = new Timer(ONE_SECOND, dialog.new TimerListener());
			//
			// pParent.progressBar.setIndeterminate(true);
			// pParent.disableAllActions();
			//			
			// dialog.task.go();
			// dialog.timer.start();

			return SUCCESS_RESPONSE;
		}
	}

	private GetVariantsDialog(PaneledReferenceSequenceDisplay pParent,
			ReferenceSequenceDisplayComponent pCanvas) {
		super(
				JOptionPane
						.getFrameForComponent(PaneledReferenceSequenceDisplay.frame),
				true);

		// Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				// pretend the user clicked cancel
				setVisible(false);
			}
		});

		mParent = pParent;
		mCanvas = pCanvas;
		fc = new JFileChooser();

		initialize();
	}

	private void enableStuff() {
		// only enable gene related options if there are annotations in the ref
		// seq
		if (mCanvas.dData.referenceSequence.genes != null
				&& mCanvas.dData.referenceSequence.genes.size() > 0) {
			getJAllGenesButton().setEnabled(true);
			if (mCanvas.dData.selectedGene == null) {
				getJSelectedGeneButton().setEnabled(false);
				if (getJSelectedGeneButton().isSelected()) {
					// select something else
					getJAllGenesButton().setSelected(true);
				}
			} else {
				getJSelectedGeneButton().setEnabled(true);
			}
		} else {
			getJAllGenesButton().setEnabled(false);
			getJSelectedGeneButton().setEnabled(false);
			if (getJAllGenesButton().isSelected()
					|| getJSelectedGeneButton().isSelected()) {
				// select something else
				getJVisibleRegionButton().setSelected(true);
			}
		}

		// check to see if any reads are selected
		if (mCanvas.dData.selectedReadList.getSize() == 0) {
			getJSelectedObjectsButton().setEnabled(false);
			if (getJSelectedObjectsButton().isSelected()) {
				getJVisibleRegionButton().setSelected(true);
			}
			getJSelectedReadsButton().setEnabled(false);
			if (getJSelectedReadsButton().isSelected()) {
				getJAllReadsButton().setSelected(true);
			}
		} else {
			getJSelectedReadsButton().setEnabled(true);
			getJSelectedObjectsButton().setEnabled(true);
		}
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setTitle("Find Variant Sequences");

		// set up button groups
		ButtonGroup group1 = new ButtonGroup();
		group1.add(getJAllGenesButton());
		group1.add(getJSelectedGeneButton());
		group1.add(getJVisibleRegionButton());
		group1.add(getJSelectedObjectsButton());

		ButtonGroup group5 = new ButtonGroup();
		group5.add(getJAllReadsButton());
		group5.add(getJSelectedReadsButton());

		// default selections
		getJSelectedGeneButton().setSelected(true);
		getJAllReadsButton().setSelected(true);
		getJGroupReadsCheckBox().setSelected(true);

		// create dialog components
		this.setContentPane(getJContentPane());

		// sort out positions
		this.pack();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJLeftPanel(), java.awt.BorderLayout.WEST);
			jContentPane.add(getJRightPanel(), java.awt.BorderLayout.EAST);
			jContentPane.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jLeftPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJLeftPanel() {
		if (jLeftPanel == null) {
			jLeftPanel = new JPanel();
			jLeftPanel
					.setLayout(new BoxLayout(jLeftPanel, BoxLayout.PAGE_AXIS));
			jLeftPanel.add(getJTopLeftPanel(), null);
			jLeftPanel.add(getJBottomLeftPanel(), null);
		}
		return jLeftPanel;
	}

	/**
	 * This method initializes jTopLeftPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJTopLeftPanel() {
		if (jTopLeftPanel == null) {
			jTopLeftPanel = new JPanel();
			jTopLeftPanel.add(getJSelectScopePanel(), null);
		}
		return jTopLeftPanel;
	}

	/**
	 * This method initializes jSelectScopePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJSelectScopePanel() {
		if (jSelectScopePanel == null) {
			jSelectScopePanel = new JPanel();
			jSelectScopePanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Select scope:"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			jSelectScopePanel.setLayout(new BoxLayout(
					jSelectScopePanel,
					BoxLayout.PAGE_AXIS));
			jSelectScopePanel.add(getJAllGenesButton(), null);
			jSelectScopePanel.add(getJSelectedGeneButton(), null);
			jSelectScopePanel.add(getJVisibleRegionButton(), null);
			jSelectScopePanel.add(getJSelectedObjectsButton(), null);
		}

		return jSelectScopePanel;
	}

	/**
	 * This method initializes jAllGenesButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJAllGenesButton() {
		if (jAllGenesButton == null) {
			jAllGenesButton = new JRadioButton();
			jAllGenesButton.setText("All Genes");
		}
		return jAllGenesButton;
	}

	/**
	 * This method initializes jSelectedGeneButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJSelectedGeneButton() {
		if (jSelectedGeneButton == null) {
			jSelectedGeneButton = new JRadioButton();
			jSelectedGeneButton.setText("Selected Gene");
		}
		return jSelectedGeneButton;
	}

	/**
	 * This method initializes jVisibleRegionButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJVisibleRegionButton() {
		if (jVisibleRegionButton == null) {
			jVisibleRegionButton = new JRadioButton();
			jVisibleRegionButton.setText("Visible Region");
		}
		return jVisibleRegionButton;
	}

	/**
	 * This method initializes jSelectedObjectsButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJSelectedObjectsButton() {
		if (jSelectedObjectsButton == null) {
			jSelectedObjectsButton = new JRadioButton();
			jSelectedObjectsButton.setText("Selected Reads");
		}
		return jSelectedObjectsButton;
	}

	/**
	 * This method initializes jBottomLeftPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJBottomLeftPanel() {
		if (jBottomLeftPanel == null) {
			jBottomLeftPanel = new JPanel();
			jBottomLeftPanel.add(getJSelectReadsPanel(), null);
		}
		return jBottomLeftPanel;
	}

	/**
	 * This method initializes jOutputOptionsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJOutputOptionsPanel() {
		if (jOutputOptionsPanel == null) {
			jOutputOptionsPanel = new JPanel();
			jOutputOptionsPanel.setLayout(new BoxLayout(
					jOutputOptionsPanel,
					BoxLayout.PAGE_AXIS));
			jOutputOptionsPanel.add(getJSaveToFastaCheckBox(), null);
			// jOutputOptionsPanel.add(getJSaveToCsvCheckBox(), null);
			jOutputOptionsPanel.add(getJGroupReadsCheckBox(), null);
		}
		return jOutputOptionsPanel;
	}

	/**
	 * This method initializes jSaveToFastaCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJSaveToFastaCheckBox() {
		if (jSaveToFastaCheckBox == null) {
			jSaveToFastaCheckBox = new JCheckBox();
			jSaveToFastaCheckBox.setText("Save to FASTA list");
		}
		return jSaveToFastaCheckBox;
	}

	// /**
	// * This method initializes jSaveToCsvCheckBox
	// *
	// * @return javax.swing.JCheckBox
	// */
	// private JCheckBox getJSaveToCsvCheckBox() {
	// if (jSaveToCsvCheckBox == null) {
	// jSaveToCsvCheckBox = new JCheckBox();
	// jSaveToCsvCheckBox.setText("Save to FASTA list");
	// }
	// return jSaveToCsvCheckBox;
	// }

	/**
	 * This method initializes jGroupReadsCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJGroupReadsCheckBox() {
		if (jGroupReadsCheckBox == null) {
			jGroupReadsCheckBox = new JCheckBox();
			jGroupReadsCheckBox.setText("Adjust read groupings");
		}
		return jGroupReadsCheckBox;
	}

	/**
	 * This method initializes jSelectReadsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJSelectReadsPanel() {
		if (jSelectReadsPanel == null) {
			jSelectReadsPanel = new JPanel();
			jSelectReadsPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Which reads?"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			jSelectReadsPanel.setLayout(new BoxLayout(
					jSelectReadsPanel,
					BoxLayout.PAGE_AXIS));
			jSelectReadsPanel.add(getJAllReadsButton(), null);
			jSelectReadsPanel.add(getJSelectedReadsButton(), null);

		}
		return jSelectReadsPanel;
	}

	/**
	 * This method initializes jSelectedReadsButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJSelectedReadsButton() {
		if (jSelectedReadsButton == null) {
			jSelectedReadsButton = new JRadioButton();
			jSelectedReadsButton.setText("Selected Reads");
		}
		return jSelectedReadsButton;
	}

	/**
	 * This method initializes jAllReadsButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJAllReadsButton() {
		if (jAllReadsButton == null) {
			jAllReadsButton = new JRadioButton();
			jAllReadsButton.setText("All Reads");
		}
		return jAllReadsButton;
	}

	/**
	 * This method initializes jRightPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJRightPanel() {
		if (jRightPanel == null) {
			jRightPanel = new JPanel();
			jRightPanel.setLayout(new BoxLayout(
					jRightPanel,
					BoxLayout.PAGE_AXIS));
			jRightPanel.add(getJTopRightPanel(), null);
			jRightPanel.add(getJBottomRightPanel(), null);
		}
		return jRightPanel;
	}

	/**
	 * This method initializes jTopRightPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJTopRightPanel() {
		if (jTopRightPanel == null) {
			jTopRightPanel = new JPanel();
			jTopRightPanel.add(getJAlgorithmPanel(), null);
		}
		return jTopRightPanel;
	}

	/**
	 * This method initializes jBottomRightPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJBottomRightPanel() {
		if (jBottomRightPanel == null) {
			jBottomRightPanel = new JPanel();
			jBottomRightPanel.add(getJSettingsPanel(), null);
		}
		return jBottomRightPanel;
	}

	/**
	 * This method initializes jSettingsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJSettingsPanel() {
		if (jSettingsPanel == null) {
			jSettingsPanel = new JPanel();
			jSettingsPanel.setLayout(new BoxLayout(
					jSettingsPanel,
					BoxLayout.PAGE_AXIS));
			jSettingsPanel.add(getJOutputOptionsPanel(), null);
		}
		return jSettingsPanel;
	}

	/**
	 * This method initializes jButtonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJCancelButton(), null);
			jButtonPanel.add(getJGoButton(), null);
		}
		return jButtonPanel;
	}

	/**
	 * This method initializes jCancelButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJCancelButton() {
		if (jCancelButton == null) {
			Action cancelAction = new AbstractAction("Cancel") {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			};

			jCancelButton = new JButton(cancelAction);
			jCancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("ESCAPE"),
					"esc");
			jCancelButton.getActionMap().put("esc", cancelAction);

		}
		return jCancelButton;
	}

	/**
	 * This method initializes jGoButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJGoButton() {
		if (jGoButton == null) {
			Action goAction = new AbstractAction("Go") {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					go();
				}
			};

			jGoButton = new JButton(goAction);
			jGoButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("ENTER"),
					"ent");
			jGoButton.getActionMap().put("ent", goAction);
			jGoButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("RETURN"),
					"ret");
			jGoButton.getActionMap().put("ret", goAction);

		}
		return jGoButton;
	}

	void go() {
		// initialize Task
		task = this.new AutostrainerTask();
		// close dialog and start processing
		this.setVisible(false);

	}

	void doAutostraining(AutostrainerTask pTask) throws InterruptedException,
			IOException {
		// disable main window
		mParent.disableAllActions();

		// what kind of outputs do we need
		writeToFasta = this.getJSaveToFastaCheckBox().isSelected();
		// writeToCsv = this.getJSaveToCsvCheckBox().isSelected();
		updateDisplay = this.getJGroupReadsCheckBox().isSelected();

		// set up output file
		FileWriter fwFasta = null;
		if (writeToFasta) {
			String cwd = GlobalSettings
					.getSetting(GlobalSettings.OUTPUT_DIR_KEY);
			if (cwd != null) {
				fc.setCurrentDirectory(new File(cwd));
			}

			// get output file name
			int returnVal = fc
					.showSaveDialog(PaneledReferenceSequenceDisplay.frame);
			if (!(returnVal == JFileChooser.APPROVE_OPTION)) {
				System.out.println("Save command cancelled by user.");
				pTask.stop();
				return;
			}
			File file = fc.getSelectedFile();
			System.out.println("saving FASTA to " + file.getAbsolutePath());
			GlobalSettings.putSetting(GlobalSettings.OUTPUT_DIR_KEY, fc
					.getCurrentDirectory()
					.getAbsolutePath());

			// check if file exists
			if (file.exists()) {
				int response = JOptionPane.showOptionDialog(
						PaneledReferenceSequenceDisplay.frame,
						overwriteQuestion,
						file.getName() + overwriteTitle,
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null,
						null,
						null);
				if (response != JOptionPane.YES_OPTION) {
					pTask.stop();
					return;
				}
			}

			// open file writer
			fwFasta = new FileWriter(file, false);
			this.fastaFile = new PrintWriter(fwFasta);
		}

		// FileWriter fwCsv = null;
		// if (writeToCsv) {
		// // get output file name
		//			
		// int returnVal = fc.showSaveDialog(mParent.frame);
		// if (!(returnVal == JFileChooser.APPROVE_OPTION)) {
		// System.out.println("Save command cancelled by user.");
		// pTask.stop();
		// return;
		// }
		//			
		// File file = fc.getSelectedFile();
		//			
		// System.out.println("saving to " + file.getAbsolutePath());
		//			
		// if (file.exists()) {
		// int response = JOptionPane
		// .showOptionDialog(mParent.frame,
		// overwriteQuestion,
		// file.getName() + overwriteTitle,
		// JOptionPane.YES_NO_OPTION,
		// JOptionPane.WARNING_MESSAGE,
		// null,null,null);
		// if (response != JOptionPane.YES_OPTION) {
		// pTask.stop();
		// return;
		// }
		// }
		// fwCsv = new FileWriter(file,false);
		// this.csvFile = new PrintWriter(fwCsv);
		// }

		// set algorithm for substrainer to use.
		// if (!jFromScratchButton.isSelected()) {
		// settings.put(Substrainer.SEGMENT_STRAINER,getJAlgorithmComboBox().getSelectedItem());
		// }

		if (updateDisplay) {
			// back up read groupings before we muck around with things
			mCanvas.dData.undoData.startMove();
			// keep track of all reads, not just representatives
			settings.put(SegmentStrainer.COMBINE_STRAINS, new Boolean(true));
		}

		try {
			// get some variables

			if (pTask != null)
				pTask.setMessage("Autostraining");

			// figure out what to do and call the appropriate method
			if (jAllGenesButton.isSelected()) {
				strainAllGenes(pTask);
			} else if (getJSelectedGeneButton().isSelected()) {
				strainSelectedGene(pTask);
			} else if (getJSelectedObjectsButton().isSelected()) {
				// if user limits scope to selected objects
				// pretend that region is the scope of a Gene
				Strain selections = mCanvas.dData.selectedReadList;
				selections.setAlignmentFromReads();
				Gene g = new Gene(
						"Selected",
						mCanvas.dData.referenceSequence,
						selections.getStart(),
						selections.getEnd(),
						true,
						"This is a dummy Gene object created to srtain the selected reads");
				strainOneGene(g, pTask);
			} else {
				// then use visible region
				// pretend visible region is the scope of a Gene
				Gene g = new Gene(
						"Visible",
						mCanvas.dData.referenceSequence,
						mCanvas.dData.getStart(),
						mCanvas.dData.getEnd(),
						true,
						"This is a dummy Gene object created to srtain the reads in the viewing window");
				strainOneGene(g, pTask);
			}

			if (pTask != null) {
				if (pTask.isInterrupted()) {
					throw new InterruptedException("Cancelled");
				} else {
					pTask.setMessage("Cleaning up...");
				}
			}

			if (writeToFasta) {
				// close file handles and streams if we opened them
				this.fastaFile.close();
				fwFasta.close();
			}
			// if (writeToCsv) {
			// // close file handles and streams if we opened them
			// this.csvFile.close();
			// fwCsv.close();
			// }

		} catch (InvocationTargetException ite) {
			mCanvas.dData.undoData.endMove();
			amd.strainer.display.util.Util.displayErrorMessage(
					PaneledReferenceSequenceDisplay.frame,
					"Error configuring algorithm: " + ite.toString());
			ite.printStackTrace(System.err);
		} catch (InstantiationException ite) {
			mCanvas.dData.undoData.endMove();
			amd.strainer.display.util.Util.displayErrorMessage(
					PaneledReferenceSequenceDisplay.frame,
					"Error configuring algorithm: " + ite.toString());
			ite.printStackTrace(System.err);
		} catch (IllegalAccessException ite) {
			mCanvas.dData.undoData.endMove();
			amd.strainer.display.util.Util.displayErrorMessage(
					PaneledReferenceSequenceDisplay.frame,
					"Error configuring algorithm: " + ite.toString());
			ite.printStackTrace(System.err);
		} catch (NoSuchMethodException ite) {
			mCanvas.dData.undoData.endMove();
			amd.strainer.display.util.Util.displayErrorMessage(
					PaneledReferenceSequenceDisplay.frame,
					"Error configuring algorithm: " + ite.toString());
			ite.printStackTrace(System.err);
		} catch (InterruptedException e) {
			mCanvas.dData.undoData.endMove();
			mCanvas.dData.undoData.undo();
		} catch (RuntimeException e) {
			mCanvas.dData.undoData.endMove();
			amd.strainer.display.util.Util.displayErrorMessage(
					PaneledReferenceSequenceDisplay.frame,
					"Unexpected error: " + e.toString());
			e.printStackTrace(System.err);
		} catch (SegmentStrainerException e) {
			mCanvas.dData.undoData.endMove();
			amd.strainer.display.util.Util.displayErrorMessage(
					PaneledReferenceSequenceDisplay.frame,
					"Autostraining error: " + e.toString());
			e.printStackTrace(System.err);
		}

		if (updateDisplay) {

			// close undo data
			mCanvas.dData.undoData.endMove();

			// update info boxes
			mParent.updateDisplay(mCanvas.dData);

			// make sure canvas refreshes next time it redraws
			mCanvas.restack = true;
			mCanvas.recalcShapes = true;

		}

		// tell canvas to redraw
		mCanvas.repaint();
		// enable main window
		mParent.enableAllActions();
	}

	void strainAllGenes(Task pTask) throws InterruptedException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		if (pTask != null)
			pTask.setLengthOfTask(mParent.getReferenceSequence().genes.size());
		for (Gene gene : mParent.getReferenceSequence().genes.values()) {
			if (pTask != null)
				pTask.setCurrent(pTask.getCurrent() + 1);
			try {
				strainOneGene(gene, null);
			} catch (SegmentStrainerException e) {
				amd.strainer.display.util.Util.displayErrorMessage(
						PaneledReferenceSequenceDisplay.frame,
						"Can't strain gene " + gene.getName() + ": "
								+ e.toString());
			}
		}
	}

	void strainSelectedGene(AutostrainerTask pTask)
			throws InterruptedException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, SegmentStrainerException {
		strainOneGene(mCanvas.dData.selectedGene, pTask);
	}

	void strainOneGene(Gene pGene, AutostrainerTask pTask)
			throws InterruptedException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, SegmentStrainerException {

		if (pTask != null && pTask.isInterrupted()) {
			throw new InterruptedException("Autostraining Cancelled");
		}

		// get algorithm class: an instance of SegmentStrainer
		Class algClass = ((AlgBoxOption) getJAlgorithmComboBox()
				.getSelectedItem()).getAlg();

		Iterator<Read> reads = null;
		if (getJSelectedReadsButton().isSelected()) {
			reads = mCanvas.dData.selectedReadList.getReadIterator();
		}

		Util.autostrainGene(
				algClass,
				pGene,
				pTask,
				reads,
				(writeToFasta ? fastaFile : null),
				(updateDisplay ? mCanvas : null),
				null);
	}

	class AutostrainerTask extends AbstractTask {
		public AutostrainerTask() {
			message = "Initializing...";
		}

		// AutostrainerTask instance = null;

		/**
		 * Called to start the task.
		 */
		@Override
		protected Object doStuff() {
			// instance = this;
			try {
				doAutostraining(this);
			} catch (Exception e) {
				e.printStackTrace(System.err);
				errorTitle = "Straining error";
				message = e.getMessage();
				current = -1;
			}
			done = true;
			return null;
		}

		public void doOnError(PaneledReferenceSequenceDisplay pParent) {
			// do nothing
		}
	}

	/**
	 * The actionPerformed method in this class is called each time the Timer
	 * "goes off".
	 * 
	 * TODO:3 merge with SequenceDataLoader architecture
	 */
	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (task.getLengthOfTask() > 0
					&& mParent.progressBar.isIndeterminate()) {
				mParent.progressBar.setIndeterminate(false);
				mParent.progressBar.setMaximum(task.getLengthOfTask());
				mParent.progressBar.setMinimum(0);
			}

			mParent.progressBar.setValue(task.getCurrent());
			mParent.progressBar.setString(task.getMessage());

			if (task.isDone()) {
				mParent.progressBar.setValue(mParent.progressBar.getMinimum());
				mParent.progressBar.setIndeterminate(false);
				mParent.progressBar.setStringPainted(false);
				mParent.enableAllActions();
				timer.stop();
			}

			if (task.getCurrent() < 0) {
				// died foer some reason, close objects
				// System.out.println("killing task and timer");
				mParent.progressBar.setValue(mParent.progressBar.getMinimum());
				mParent.progressBar.setIndeterminate(false);
				mParent.progressBar.setStringPainted(false);
				mParent.enableAllActions();
				task.stop();
				timer.stop();
			}
		}
	}

	/**
	 * This method initializes jAlgorithmPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAlgorithmPanel() {
		if (jAlgorithmPanel == null) {
			jAlgorithmPanel = new JPanel();
			jAlgorithmPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Strainer Algorithm"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			jAlgorithmPanel.setLayout(new BoxLayout(
					jAlgorithmPanel,
					BoxLayout.PAGE_AXIS));
			jAlgorithmPanel.add(getJConfigureAlgorithmButton(), null);
			jAlgorithmPanel.add(getJAlgorithmComboBox(), null);
			JPanel algListModifyPanel = new JPanel();
			algListModifyPanel.add(getJAddAlgorithmButton(), null);
			algListModifyPanel.add(getJDeleteAlgorithmButton(), null);
			jAlgorithmPanel.add(algListModifyPanel, null);
		}
		return jAlgorithmPanel;
	}

	/**
	 * This method initializes jAlgorithmComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	JComboBox getJAlgorithmComboBox() {
		if (jAlgorithmComboBox == null) {
			jAlgorithmComboBox = new JComboBox();
			getAlgorithmOptions(jAlgorithmComboBox);
		}
		return jAlgorithmComboBox;
	}

	static class AlgBoxOption {
		private final Class mAlg;
		private final String mText;

		AlgBoxOption(String pText, Class pAlg) {
			mText = pText;
			mAlg = pAlg;
		}

		@Override
		public String toString() {
			return mText;
		}

		Class getAlg() {
			return mAlg;
		}

		@Override
		public boolean equals(Object pO) {
			if (pO == null) {
				return false;
			}
			if (!(pO instanceof AlgBoxOption)) {
				return false;
			}
			return ((AlgBoxOption) pO).getAlg().equals(mAlg);
		}
	}

	private void getAlgorithmOptions(JComboBox pBox) {
		pBox.removeAllItems();

		Object defaultStrainer = Config.getConfig().getSettings().get(
				Config.INTERNAL_SEGMENT_STRAINER);
		List<Class> algs = Config.getAlgorithmList();

		for (Class alg : algs) {
			try {
				String algName = Config.getAlgorithmName(alg);
				pBox.addItem(new AlgBoxOption(algName, alg));
				if (alg.equals(defaultStrainer)) {
					pBox.setSelectedItem(alg);
				}
			} catch (Exception e) {
				System.out.println("Can't configure algorithm: " + alg);
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method initializes jConfigureAlgorithmButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJConfigureAlgorithmButton() {
		if (jConfigureAlgorithmButton == null) {
			Action confAlgAction = new AbstractAction("Configure") {
				public void actionPerformed(ActionEvent e) {
					SegmentStrainerSettingsDialog
							.showDialog(((AlgBoxOption) getJAlgorithmComboBox()
									.getSelectedItem()).getAlg());
				}
			};

			jConfigureAlgorithmButton = new JButton(confAlgAction);
		}
		return jConfigureAlgorithmButton;
	}

	/**
	 * This method initializes jAddAlgorithmButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJAddAlgorithmButton() {
		if (jAddAlgorithmButton == null) {
			Action addAlgAction = new AbstractAction("Add") {
				public void actionPerformed(ActionEvent e) {
					// get class name from user
					// TODO:3 search for appropriate classes in classpath
					String response = JOptionPane
							.showInputDialog(
									PaneledReferenceSequenceDisplay.frame,
									"Enter the name of a class (including package name) that implements the SegmentStrainer interface:",
									"Add an algorithm",
									JOptionPane.QUESTION_MESSAGE);

					if (response == null) {
						// user canceled or entered nothing
						return;
					} else {
						// try to find class and add it
						try {
							Config.addAlgorithm(response.trim());
							getAlgorithmOptions(getJAlgorithmComboBox());
						} catch (Exception ex) {
							ex.printStackTrace();
							amd.strainer.display.util.Util
									.displayErrorMessage(ex);
						}

					}
				}
			};
			String iconLoc = "/toolbarButtonGraphics/general/Edit16.gif";
			addAlgAction.putValue(
					Action.SHORT_DESCRIPTION,
					"Add a new straining class to this list.");
			addAlgAction.putValue(Action.SMALL_ICON, new ImageIcon(
					GetVariantsDialog.class.getResource(iconLoc)));

			jAddAlgorithmButton = new JButton(addAlgAction);
		}
		return jAddAlgorithmButton;
	}

	/**
	 * This method initializes jDeleteAlgorithmButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJDeleteAlgorithmButton() {
		if (jRemoveAlgorithmButton == null) {
			Action delAlgAction = new AbstractAction("Del") {
				public void actionPerformed(ActionEvent e) {
					AlgBoxOption algOption = (AlgBoxOption) getJAlgorithmComboBox()
							.getSelectedItem();
					Config.removeAlgorithm(algOption.getAlg());
					getAlgorithmOptions(getJAlgorithmComboBox());
				}
			};
			String iconLoc = "/toolbarButtonGraphics/general/Delete16.gif";
			delAlgAction.putValue(
					Action.SHORT_DESCRIPTION,
					"Remove selected algorithm from this list.");
			delAlgAction.putValue(Action.SMALL_ICON, new ImageIcon(
					GetVariantsDialog.class.getResource(iconLoc)));

			jRemoveAlgorithmButton = new JButton(delAlgAction);
		}
		return jRemoveAlgorithmButton;
	}

	/**
	 * Defines an action that can be added to the GUI menu to open this dialog
	 * 
	 * @author jmeppley
	 */
	public static class ShowDialogAction extends AbstractAction {
		private static final String NAME = "Variant Sequences";
		private static final String DESC = "Write all possible sequences for genes (or other region) to FASTA file.";
		private final PaneledReferenceSequenceDisplay mParent;
		private final ReferenceSequenceDisplayComponent mCanvas;

		public ShowDialogAction(PaneledReferenceSequenceDisplay pParent,
				ReferenceSequenceDisplayComponent pCanvas) {
			super(NAME);
			putValue(SHORT_DESCRIPTION, DESC);
			mParent = pParent;
			mCanvas = pCanvas;
		}

		public void actionPerformed(ActionEvent arg0) {
			showDialog(mParent, mCanvas);
		}
	}

} // @jve:decl-index=0:visual-constraint="10,10"
