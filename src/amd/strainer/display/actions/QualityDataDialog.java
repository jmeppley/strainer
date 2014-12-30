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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import amd.strainer.GlobalSettings;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.ReferenceSequenceDisplayComponent;
import amd.strainer.display.util.Util;
import amd.strainer.file.QualityData;
import amd.strainer.objects.QualifiedDifference;

/**
 * Allows the user to import quality data into the current data set.
 * 
 * @author jmeppley
 */
public class QualityDataDialog extends JDialog implements CaretListener {
	// TODO:4 unify actions and dialogs with an AbsrtactStrainerDialog based on
	// this one

	private static QualityDataDialog instance = null;

	// ////
	// object variables
	private final PaneledReferenceSequenceDisplay mParent;
	private final ReferenceSequenceDisplayComponent mCanvas;
	private final JFileChooser mQualityFC = new JFileChooser();
	private final JFileChooser mAlignmentFC = new JFileChooser();
	private File mQualityFile = null;
	private File mAlignmentFile = null;

	// ////
	// GUI variables
	private JPanel jContentPane = null;

	private JPanel jButtonPanel = null;
	private JPanel jMainPanel = null;
	private JPanel jImportQualityPanel = null;

	private JButton jCancelButton = null;
	private JButton jThresholdButton = null;
	private JButton jImportButton = null;

	private JPanel jQualityFilePanel = null;
	private JPanel jAlignmentFilePanel = null;

	private JLabel jQualityFileLabel = null;
	private JTextField jQualityFileTextField = null;
	private JButton jQualityFileButton = null;
	private JLabel jAlignmentFileLabel = null;
	private JTextField jAlignmentFileTextField = null;
	private JButton jAlignmentFileButton = null;

	private JPanel jThresholdPanel = null;
	private JLabel jThresholdLabel = null;
	private JTextField jThresholdTextField = null;

	/**
	 * Displays this dialog to the user.
	 */
	public static void showDialog(PaneledReferenceSequenceDisplay pParent,
			ReferenceSequenceDisplayComponent pCanvas) {
		if (instance == null) {
			instance = new QualityDataDialog(pParent, pCanvas);
		}
		instance.setVisible(true);
	}

	private QualityDataDialog(PaneledReferenceSequenceDisplay pParent,
			ReferenceSequenceDisplayComponent pCanvas) {
		super(JOptionPane
				.getFrameForComponent(PaneledReferenceSequenceDisplay.frame),
				true);
		mParent = pParent;
		mCanvas = pCanvas;
		initialize();
	}

	// ////
	// private methods to do the work
	private boolean setQualityThreshold() {
		short qual;
		try {
			qual = Short.parseShort(getJThresholdTextField().getText());
		} catch (RuntimeException ex) {
			// notify user if the value coudn't be parsed
			Util.displayErrorMessage(ex);
			// bail out and let the user try again
			return false;
		}

		// warn user if there is no data to work with
		if (!mParent.getReferenceSequence().hasQualityData) {
			Util
					.displayErrorMessage(
							"No quality data!",
							"The current data set contains no quality data.\n Setting a threshod will have no effect.");
		}

		// set threshold
		QualifiedDifference.setQualityThreshold(qual);

		// make sure new colors are applied
		mCanvas.recalcShapes = true;
		mCanvas.repaint();

		return true;
	}

	private boolean importQualityData() {
		// get quality file and make sure it's accessible
		mQualityFile = new File(getJQualityFileTextField().getText());

		if (!mQualityFile.exists()) {
			Util.displayErrorMessage("Missing Quality File",
					"Cannot find quality file: "
							+ getJQualityFileTextField().getText());
			return false;
		}

		if (!mQualityFile.canRead()) {
			Util.displayErrorMessage("Inaccessible Quality File",
					"Cannot read quality file: "
							+ getJQualityFileTextField().getText());
			return false;
		}

		// same for alignment file
		if (getJAlignmentFileTextField().getText().trim().length() == 0) {
			int response = JOptionPane
					.showConfirmDialog(
							PaneledReferenceSequenceDisplay.frame,
							"Quality data will be assumed to line up exactly with read sequences (ie reads are not trimmed or quality data is trimmed). Continue?",
							"No quality alignments found",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
			if (response == JOptionPane.YES_OPTION) {
				mAlignmentFile = null;
			} else {
				return false;
			}
		} else {

			mAlignmentFile = new File(getJAlignmentFileTextField().getText());

			if (!mAlignmentFile.exists()) {
				Util.displayErrorMessage("Missing Alignment File",
						"Cannot find alignment file: "
								+ getJAlignmentFileTextField().getText());
				return false;
			}

			if (!mAlignmentFile.canRead()) {
				Util.displayErrorMessage("Inaccessible Alignment File",
						"Cannot read alignment file: "
								+ getJAlignmentFileTextField().getText());
				return false;
			}
		}

		// load data
		Task task = new ImportTask();
		SequenceDataLoader loader = new SequenceDataLoader(mParent, task);
		loader.load();

		// return true, task and loader will take care of handling any other
		// errors
		return true;
	}

	// ///
	// private methods to create interface
	/**
	 * Initializes the dialog interface
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setTitle("Quality Data");

		this.setContentPane(getJContentPane());
		pack();
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
			jContentPane.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJMainPanel(), java.awt.BorderLayout.NORTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jCloseButtonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJCancelButton(), null);
			jButtonPanel.add(getJThresholdButton(), null);
			jButtonPanel.add(getJImportButton(), null);
		}
		return jButtonPanel;
	}

	/**
	 * This method initializes jMainPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJMainPanel() {
		if (jMainPanel == null) {
			jMainPanel = new JPanel();
			jMainPanel
					.setLayout(new BoxLayout(jMainPanel, BoxLayout.PAGE_AXIS));
			jMainPanel.add(getJImportQualityPanel(), null);
			jMainPanel.add(getJThresholdPanel(), null);
		}
		return jMainPanel;
	}

	/**
	 * This method initializes jImportQualityPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJImportQualityPanel() {
		if (jImportQualityPanel == null) {
			jImportQualityPanel = new JPanel();
			jImportQualityPanel.setLayout(new BoxLayout(jImportQualityPanel,
					BoxLayout.PAGE_AXIS));
			jImportQualityPanel.add(getJQualityFilePanel(), null);
			jImportQualityPanel.add(getJAlignmentFilePanel(), null);
		}
		return jImportQualityPanel;
	}

	/**
	 * This method initializes jCancelButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJCancelButton() {
		if (jCancelButton == null) {
			Action cancelAction = new AbstractAction("Cancel") {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			};

			jCancelButton = new JButton(cancelAction);
			jCancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("ESCAPE"), "esc");
			jCancelButton.getActionMap().put("esc", cancelAction);

		}
		return jCancelButton;
	}

	/**
	 * This method initializes jThresholdButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJThresholdButton() {
		if (jThresholdButton == null) {
			Action setThresholdAction = new AbstractAction("Just Set Threshold") {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					boolean status = setQualityThreshold();
					if (status) {
						// close dialog
						setVisible(false);
					}
				}
			};

			jThresholdButton = new JButton(setThresholdAction);
			jThresholdButton.setEnabled(false);
		}

		return jThresholdButton;
	}

	/**
	 * This method initializes jImportButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJImportButton() {
		if (jImportButton == null) {
			Action importAction = new AbstractAction(
					"Import Data and Set Threshold") {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					boolean status = importQualityData();
					if (status) {
						// close dialog
						setVisible(false);
					}
				}
			};

			jImportButton = new JButton(importAction);
			jImportButton.setEnabled(false);
		}
		return jImportButton;
	}

	/**
	 * This method initializes jThresholdPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJThresholdPanel() {
		if (jThresholdPanel == null) {
			jThresholdLabel = new JLabel();
			jThresholdLabel.setText("Quality Threshold:");
			jThresholdPanel = new JPanel();
			jThresholdPanel.add(jThresholdLabel, null);
			jThresholdPanel.add(getJThresholdTextField(), null);
		}
		return jThresholdPanel;
	}

	/**
	 * This method initializes jQualityFilePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJQualityFilePanel() {
		if (jQualityFilePanel == null) {
			jQualityFileLabel = new JLabel();
			jQualityFileLabel.setText("Quality File:");
			jQualityFilePanel = new JPanel();
			jQualityFilePanel.add(jQualityFileLabel, null);
			jQualityFilePanel.add(getJQualityFileTextField(), null);
			jQualityFilePanel.add(getJQualityFileButton(), null);
		}
		return jQualityFilePanel;
	}

	/**
	 * This method initializes jAlignmentFilePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAlignmentFilePanel() {
		if (jAlignmentFilePanel == null) {
			jAlignmentFileLabel = new JLabel();
			jAlignmentFileLabel.setText("Alignment File:");
			jAlignmentFilePanel = new JPanel();
			jAlignmentFilePanel.add(jAlignmentFileLabel, null);
			jAlignmentFilePanel.add(getJAlignmentFileTextField(), null);
			jAlignmentFilePanel.add(getJAlignmentFileButton(), null);
		}
		return jAlignmentFilePanel;
	}

	/**
	 * This method initializes jQualityFileTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJQualityFileTextField() {
		if (jQualityFileTextField == null) {
			jQualityFileTextField = new JTextField();
			jQualityFileTextField.setColumns(30);
			jQualityFileTextField.addCaretListener(this);
		}
		return jQualityFileTextField;
	}

	/**
	 * This method initializes jQualityFileButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJQualityFileButton() {
		if (jQualityFileButton == null) {
			jQualityFileButton = new JButton();
			jQualityFileButton.setText("Browse");
			jQualityFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							String cwd = GlobalSettings
									.getSetting(GlobalSettings.OUTPUT_DIR_KEY);
							if (cwd != null) {
								mQualityFC.setCurrentDirectory(new File(cwd));
							}

							int returnVal = mQualityFC
									.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
							if (!(returnVal == JFileChooser.APPROVE_OPTION)) {
								System.out
										.println("Leaving file name unchanged");
							} else {
								getJQualityFileTextField().setText(
										mQualityFC.getSelectedFile()
												.getAbsolutePath());
								GlobalSettings.putSetting(
										GlobalSettings.OUTPUT_DIR_KEY,
										mQualityFC.getCurrentDirectory()
												.getAbsolutePath());

							}
						}
					});
		}
		return jQualityFileButton;
	}

	/**
	 * This method initializes jAlignmentFileTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJAlignmentFileTextField() {
		if (jAlignmentFileTextField == null) {
			jAlignmentFileTextField = new JTextField();
			jAlignmentFileTextField.setColumns(30);
		}
		return jAlignmentFileTextField;
	}

	/**
	 * This method initializes jAlignmentFileButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJAlignmentFileButton() {
		if (jAlignmentFileButton == null) {
			jAlignmentFileButton = new JButton();
			jAlignmentFileButton.setText("Browse");
			jAlignmentFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							String cwd = GlobalSettings
									.getSetting(GlobalSettings.OUTPUT_DIR_KEY);
							if (cwd != null) {
								mAlignmentFC.setCurrentDirectory(new File(cwd));
							}

							int returnVal = mAlignmentFC
									.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
							if (!(returnVal == JFileChooser.APPROVE_OPTION)) {
								System.out
										.println("Leaving file name unchanged");
							} else {
								getJQualityFileTextField().setText(
										mAlignmentFC.getSelectedFile()
												.getAbsolutePath());
								GlobalSettings.putSetting(
										GlobalSettings.OUTPUT_DIR_KEY,
										mAlignmentFC.getCurrentDirectory()
												.getAbsolutePath());

							}
						}
					});
		}
		return jAlignmentFileButton;
	}

	/**
	 * This method initializes jTresholdTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJThresholdTextField() {
		if (jThresholdTextField == null) {
			jThresholdTextField = new JTextField();
			jThresholdTextField.setColumns(5);
			jThresholdTextField.addCaretListener(this);

		}
		return jThresholdTextField;
	}

	/**
	 * Checks the the quality file and threshold text fields and
	 * enables/disables the action buttons appropriately. Should be called for
	 * any changes to either of those fields.
	 * <p>
	 * TODO:5 check fields for validity before enabling buttons eg, check that
	 * the threshold is really a short
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void caretUpdate(CaretEvent arg0) {
		// check threshold
		if (getJThresholdTextField().getText().trim().length() > 0) {
			getJThresholdButton().setEnabled(true);
			if (getJQualityFileTextField().getText().trim().length() > 0) {
				getJImportButton().setEnabled(true);
			} else {
				getJImportButton().setEnabled(false);
			}
		} else {
			getJThresholdButton().setEnabled(false);
			getJImportButton().setEnabled(false);
		}
	}

	/**
	 * Defines an action that can be added to the GUI menu to open this dialog
	 * 
	 * @author jmeppley
	 */
	public static class ShowDialogAction extends AbstractAction {
		private static final String NAME = "Quality Data ... ";
		private static final String DESC = "Manage quality data for this reference sequence";
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

	public class ImportTask extends AbstractTask {
		String errorTitle = null;
		Exception error = null;

		@Override
		protected Object doStuff() {
			try {
				QualityData.loadQualityData(mParent.getReferenceSequence(),
						mQualityFile, mAlignmentFile, this);
				setQualityThreshold();
				done = true;
				return Boolean.TRUE;
			} catch (FileNotFoundException e) {
				error = e;
				errorTitle = "Could not find file";
				e.printStackTrace();
				current = -1;
			} catch (IOException e) {
				error = e;
				errorTitle = "Could not access file";
				e.printStackTrace();
				current = -1;
			} catch (InterruptedException e) {
				error = e;
				errorTitle = "Interrupted";
				message = e.getMessage();
				current = -1;
			}

			// have to return something, it's not used
			return Boolean.FALSE;
		}

		public void doOnError(PaneledReferenceSequenceDisplay pParent) {
			Util.displayErrorMessage(errorTitle, error.toString());
			QualityDataDialog.showDialog(mParent, mCanvas);
		}

	}

}
