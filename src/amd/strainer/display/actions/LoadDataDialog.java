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
import java.io.InterruptedIOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import amd.strainer.GlobalSettings;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.util.Util;
import amd.strainer.file.AceFileReader;
import amd.strainer.file.CAFFileReader;
import amd.strainer.file.ReferenceSequenceLoader;
import amd.strainer.objects.QualifiedDifference;

// 

/**
 * Unified data import dialog. Allows the user to import data from a few
 * different types. Ace, BLAST, SAM, CAF, Strainer XML, FASTA, GenBank.
 * QualityData can also be included.
 * 
 * @author jmeppley
 */
public class LoadDataDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1624718986154256826L;

	public static final int ACE = 0;
	public static final int CAF = 1;

	private static LoadDataDialog instance = null;

	// ////
	// object variables
	private final PaneledReferenceSequenceDisplay mParent;
	File qualityFile = null;

	// ////
	// GUI variables
	private JPanel jContentPane = null;

	private JPanel jButtonPanel = null;
	private JButton jCancelFilesButton = null;
	private JButton jLoadFilesButton = null;

	private JPanel jQualityPanel = null;

	private JPanel jQualityFilePanel = null;
	private JLabel jQualityFileLabel = null;
	private JTextField jQualityFileTextField = null;
	private JButton jQualityFileButton = null;

	private JPanel jThresholdPanel = null;
	private JLabel jThresholdLabel = null;
	private JTextField jThresholdTextField = null;

	// TODO: CAF

	private JPanel jDataFilesPanel = null;
	private JTabbedPane jDataFilesTabbedPane = null;
	private JPanel jStrainerFileTabPanel = null;
	private JPanel jAceFileTabPanel = null;
	private JPanel jAlignmentFileTabPanel = null;

	private JPanel jRefSeqFilePanel = null;
	private JLabel jRefSeqFileLabel = null;
	private JTextField jRefSeqFileField = null;
	private JButton jRefSeqFileButton = null;
	private JCheckBox jRefSeqFormatGenbankCheck = null;
	private JCheckBox jRefSeqFormatFastaCheck = null;
	private JPanel jRefSeqFormatPanel = null;

	private JPanel jRefSeq2FilePanel = null;
	private JLabel jRefSeq2FileLabel = null;
	private JTextField jRefSeq2FileField = null;
	private JButton jRefSeq2FileButton = null;
	private JCheckBox jRefSeq2FormatGenbankCheck = null;
	private JCheckBox jRefSeq2FormatFastaCheck = null;
	private JPanel jRefSeq2FormatPanel = null;

	private JPanel jAceFilePanel = null;
	private JLabel jAceFileLabel = null;
	private JTextField jAceFileField = null;
	private JButton jAceFileButton = null;
	private JLabel jAceContigLabel = null;
	private JTextField jAceContigField = null;
	private JPanel jAceContigPanel = null;
	private JCheckBox jAceRealignCheck = null;

	private JPanel jMSAFilePanel = null;
	private JLabel jMSAFileLabel = null;
	private JTextField jMSAFileField = null;
	private JButton jMSAFileButton = null;
	private JLabel jMSAFormatLabel = null;
	private JComboBox jMSAFormatBox = null;
	private JPanel jMSAFormatPanel = null;

	private JPanel jAlignmentFilePanel = null;
	private JLabel jAlignmentFileLabel = null;
	private JTextField jAlignmentFileField = null;
	private JButton jAlignmentFileButton = null;

	private JPanel jAlignmentTypePanel = null;
	private JLabel jAlignmentTypeLabel = null;
	private JComboBox jAlignmentTypeBox = null;

	private JPanel jStrainerFilePanel = null;
	private JLabel jStrainerFileLabel = null;
	private JTextField jStrainerFileField = null;
	private JButton jStrainerFileButton = null;

	protected JFileChooser refSeqFC;
	protected JFileChooser aceFC;
	protected JFileChooser strainerFC;
	protected JFileChooser alignmentFC;
	protected JFileChooser qualsFC;

	private File lastPath;

	private JCheckBox jQualityCheckBox = null;

	private JLabel jSpacerLabel = null;

	private JCheckBox jAceTrimCheck;
	private JCheckBox jKeepQualCheck;

	private JPanel jMSAFileTabPanel;

	protected File getLastPath() {
		if (lastPath == null) {
			lastPath = new File(GlobalSettings.getSetting(
					GlobalSettings.INPUT_DIR_KEY, "~"));
		}
		return lastPath;
	}

	/**
	 * Displays this dialog to the user.
	 */
	public static void showDialog(PaneledReferenceSequenceDisplay pParent) {
		if (instance == null) {
			instance = new LoadDataDialog(pParent);
		}
		instance.setVisible(true);
		GlobalSettings.putSetting(GlobalSettings.INPUT_DIR_KEY, instance
				.getLastPath().getAbsolutePath());
	}

	private LoadDataDialog(PaneledReferenceSequenceDisplay pParent) {
		super(JOptionPane
				.getFrameForComponent(PaneledReferenceSequenceDisplay.frame),
				true);
		mParent = pParent;
		// mCanvas = pCanvas;
		initialize();
	}

	// public LoadDataDialog() {
	// super(JOptionPane.getFrameForComponent(PaneledReferenceSequenceDisplay.frame),true);
	// initialize();
	// }

	// ////
	// private methods to do the work
	private void cancelLoadData() {
		// do nothing
	}

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

		// set threshold
		QualifiedDifference.setQualityThreshold(qual);

		return true;
	}

	private boolean loadDataFiles() {
		// check quality data
		qualityFile = null;
		if (getJQualityCheckBox().isSelected()) {
			// get quality file and make sure it's accessible
			qualityFile = new File(getJQualityFileTextField().getText());

			if (!qualityFile.exists()) {
				Util.displayErrorMessage("Missing Quality File",
						"Cannot find quality file: "
								+ getJQualityFileTextField().getText());
				return false;
			}

			if (!qualityFile.canRead()) {
				Util.displayErrorMessage("Inaccessible Quality File",
						"Cannot read quality file: "
								+ getJQualityFileTextField().getText());
				return false;
			}

			if (qualityFile.length() > 5000000) {
				int response = JOptionPane
						.showConfirmDialog(
								PaneledReferenceSequenceDisplay.frame,
								"Do you want to proceed? Loading quality can take a long time.",
								"Quality can be slow",
								JOptionPane.YES_NO_OPTION);
				if (response == JOptionPane.NO_OPTION) {
					return false;
				}
			}

			// set the threshold (and check that it's a real number)
			if (!setQualityThreshold()) {
				// quit if something went wrong (the user should already have
				// been notified)
				return false;
			}
		}

		int tab = getJDataFilesTabbedPane().getSelectedIndex();
		switch (tab) {
		case 0:
			// Strainer Tab
			return loadStrainerDataFiles();
		case 1:
			// Alignments (BLAST/SAM) tab
			return loadBlastFiles();
		case 2:
			// Ace Tab
			return loadAceContig();
		case 3:
			// MSA Tab
			return loadMSAContig();
		default:
			Util.displayErrorMessage("Fatal configuration error",
					"Cannot figure out which tab is active");
			return false;
		}
	}

	private boolean loadMSAContig() {

		// get files and make sure they are accessible
		String msaFileName = getJMSAFileField().getText();

		if (msaFileName == null || msaFileName.trim().length() == 0) {
			Util.displayErrorMessage("Missing data",
					"Both files must be specified");
			return false;
		}

		File msaFile = new File(msaFileName);

		if (!msaFile.exists()) {
			Util.displayErrorMessage("Missing MSA File",
					"Cannot find MSA file: " + getJMSAFileField().getText());
			return false;
		}

		if (!msaFile.canRead()) {
			Util.displayErrorMessage("Inaccessible MSA File",
					"Cannot read MSA file: " + getJMSAFileField().getText());
			return false;
		}

		String format = getJMSAFormatBox().getSelectedItem().toString();

		// find contig
		try {
			// set up task to load data
			Task task = new ImportMSATask(mParent, msaFile, format, qualityFile);
			SequenceDataLoader loader = new SequenceDataLoader(mParent, task);

			// clear old data
			mParent.setReferenceSequence(null);

			// load new data from contig
			loader.load();

		} catch (Exception ex) {
			Util.displayErrorMessage(PaneledReferenceSequenceDisplay.frame,
					"Could not open MSA: " + ex.getMessage());
			return false;
		}

		// return true, task and loader will take care of handling any other
		// errors
		return true;
	}

	private boolean loadAceContig() {
		// TODO:1 allow user to skip mate pair search when loading from ace
		// boolean linkMatePairs;
		// try {
		// linkMatePairs = queryUserToLinkMatePairs();
		// } catch (InterruptedIOException e1) {
		// // user cancelled
		// return false;
		// }

		// get files and make sure they are accessible
		String aceFileName = getJAceFileField().getText();

		if (aceFileName == null || aceFileName.trim().length() == 0) {
			Util.displayErrorMessage("Missing data",
					"Both files must be specified");
			return false;
		}

		File assemblyFile = new File(aceFileName);

		if (!assemblyFile.exists()) {
			Util.displayErrorMessage("Missing ace File",
					"Cannot find ace assembly file: "
							+ getJAceFileField().getText());
			return false;
		}

		if (!assemblyFile.canRead()) {
			Util.displayErrorMessage("Inaccessible ace File",
					"Cannot read ace assembly file: "
							+ getJAceFileField().getText());
			return false;
		}

		long contigNumber = 0;
		try {
			contigNumber = Long.parseLong(getJAceContigField().getText());
		} catch (NumberFormatException nfe) {
			Util.displayErrorMessage("Bad Contig Number",
					"Contig number is missing or not an integer");
			return false;
		}

		boolean realign = getJAceRealignCheck().isSelected();
		boolean trim = getJAceTrimCheck().isSelected();
		boolean keepQuals = getJKeepQualCheck().isSelected();

		// find contig
		try {
			// get file type from extension
			int assemblyFileType = getAssemblyFileType(assemblyFile);
			AssemblyFileReader afr;
			if (assemblyFileType == ACE) {
				afr = new AceFileReader(assemblyFile, realign, trim);
			} else if (assemblyFileType == CAF) {
				afr = new CAFFileReader(assemblyFile, realign, trim, keepQuals);
			} else {
				Util.displayErrorMessage(
						PaneledReferenceSequenceDisplay.frame,
						"Unknown file type: "
								+ Integer.toString(assemblyFileType));
				return false;
			}

			// TODO:2 test progress updates

			// set up task to load contig
			Task task = new GetContigFromAssemblyByNumberTask(mParent, afr,
					contigNumber, qualityFile);
			SequenceDataLoader loader = new SequenceDataLoader(mParent, task);

			// clear old data
			mParent.setReferenceSequence(null);

			// load new data from contig
			loader.load();

		} catch (Exception ex) {
			Util.displayErrorMessage(PaneledReferenceSequenceDisplay.frame,
					"Could not open contig: " + ex.getMessage());
			return false;
		}

		// return true, task and loader will take care of handling any other
		// errors
		return true;
	}

	private boolean loadStrainerDataFiles() {
		String refSeqFileType = jRefSeq2FormatGenbankCheck.isSelected() ? ReferenceSequenceLoader.GENBANK
				: ReferenceSequenceLoader.FASTA;

		// get files and make sure it'sthey are accessible
		String refSeqFileName = jRefSeq2FileField.getText();
		String strainerFileName = jStrainerFileField.getText();

		if (refSeqFileName == null || refSeqFileName.trim().length() == 0
				|| strainerFileName == null
				|| strainerFileName.trim().length() == 0) {
			Util.displayErrorMessage("Missing data",
					"Both files must be specified");
			return false;
		}

		File refSeqFile = new File(refSeqFileName);
		File strainerFile = new File(strainerFileName);

		if (!refSeqFile.exists()) {
			Util.displayErrorMessage("Missing reference File",
					"Cannot find reference sequence file: "
							+ getJRefSeq2FileField().getText());
			return false;
		}

		if (!refSeqFile.canRead()) {
			Util.displayErrorMessage("Inaccessible reference File",
					"Cannot read reference sequence file: "
							+ getJRefSeq2FileField().getText());
			return false;
		}

		if (!strainerFile.exists()) {
			Util.displayErrorMessage("Missing Strainer File",
					"Cannot find XML file: "
							+ getJStrainerFileField().getText());
			return false;
		}

		if (!strainerFile.canRead()) {
			Util.displayErrorMessage("Inaccessible Strainer File",
					"Cannot read XML file: "
							+ getJStrainerFileField().getText());
			return false;
		}

		// load data
		Task task = new GetReferenceFromFileTask(mParent, refSeqFile,
				strainerFile, refSeqFileType, qualityFile);

		SequenceDataLoader loader = new SequenceDataLoader(mParent, task);
		loader.load();

		// return true, task and loader will take care of handling any other
		// errors
		return true;
	}

	private boolean loadBlastFiles() {
		String alignmentFileType = getJAlignmentTypeBox().getSelectedItem()
				.toString();

		// add check boxes under file name (may have to add space in other tabs)
		boolean linkMatePairs;
		try {
			linkMatePairs = queryUserToLinkMatePairs();
		} catch (InterruptedIOException e1) {
			// user cancelled
			return false;
		}

		String refSeqFileType = jRefSeqFormatGenbankCheck.isSelected() ? ReferenceSequenceLoader.GENBANK
				: ReferenceSequenceLoader.FASTA;

		// get files and make sure it'sthey are accessible
		String refSeqFileName = jRefSeqFileField.getText();
		String blastFileName = jAlignmentFileField.getText();

		if (refSeqFileName == null || refSeqFileName.trim().length() == 0
				|| blastFileName == null || blastFileName.trim().length() == 0) {
			Util.displayErrorMessage("Missing data",
					"Both files must be specified");
			return false;
		}

		File refSeqFile = new File(refSeqFileName);
		File blastFile = new File(blastFileName);

		if (!refSeqFile.exists()) {
			Util.displayErrorMessage("Missing reference File",
					"Cannot find reference sequence file: "
							+ getJRefSeqFileField().getText());
			return false;
		}

		if (!refSeqFile.canRead()) {
			Util.displayErrorMessage("Inaccessible reference File",
					"Cannot read reference sequence file: "
							+ getJRefSeqFileField().getText());
			return false;
		}

		if (!blastFile.exists()) {
			Util.displayErrorMessage("Missing Strainer File",
					"Cannot find XML file: "
							+ getJAlignmentFileField().getText());
			return false;
		}

		if (!blastFile.canRead()) {
			Util.displayErrorMessage("Inaccessible SrainerT File",
					"Cannot read XML file: "
							+ getJAlignmentFileField().getText());
			return false;
		}

		// ////
		// if loading a BLAST file, get clone constraints

		// ask for low bound
		int low = 0, high = -1;
		if (linkMatePairs) {
			String s = null;
			boolean notAnInt = true;
			while (notAnInt) {
				try {
					s = JOptionPane.showInputDialog(
							PaneledReferenceSequenceDisplay.frame,
							"Enter the lower bound for clone sizes",
							"Minimum clone size", JOptionPane.PLAIN_MESSAGE);

					if (s == null) {
						// bail out completely if user cancelled
						return false;
					}

					low = Integer.parseInt(s);
					notAnInt = false;
				} catch (NumberFormatException e) {
					// try again
					JOptionPane.showMessageDialog(
							PaneledReferenceSequenceDisplay.frame,
							"Your response, " + s + ", is not an integer.");
				}
			}

			// ask for high bound
			notAnInt = true;
			while (notAnInt) {
				try {
					s = JOptionPane.showInputDialog(
							PaneledReferenceSequenceDisplay.frame,
							"Enter the upper bound for clone sizes",
							"Maximum clone size", JOptionPane.PLAIN_MESSAGE);

					if (s == null) {
						// bail out completely if user cancelled
						return false;
					}

					high = Integer.parseInt(s);
					notAnInt = false;
				} catch (NumberFormatException e) {
					// try again
					JOptionPane.showMessageDialog(
							PaneledReferenceSequenceDisplay.frame,
							"Your response, " + s + ", is not an integer.");
				}
			}
		}

		// load data
		Task task = new GetReferenceFromFileTask(mParent, refSeqFile,
				blastFile, refSeqFileType, alignmentFileType, low, high,
				qualityFile);

		SequenceDataLoader loader = new SequenceDataLoader(mParent, task);
		loader.load();

		// return true, task and loader will take care of handling any other
		// errors
		return true;
	}

	private boolean queryUserToLinkMatePairs() throws InterruptedIOException {
		int response = JOptionPane.showConfirmDialog(
				PaneledReferenceSequenceDisplay.frame,
				"Do you want to attempt to link mate pairs?",
				"Link mate pairs?", JOptionPane.YES_NO_CANCEL_OPTION);
		if (response == JOptionPane.CANCEL_OPTION) {
			// bail out completely if user cancelled
			throw new InterruptedIOException("User cancelled");
		} else if (response == JOptionPane.YES_OPTION) {
			return true;
		} else {
			return false;
		}
	}

	// ///
	// inner classes
	/**
	 * Defines an action that can be added to the GUI menu to open this dialog
	 * 
	 * @author jmeppley
	 */
	public static class ShowDialogAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 387528248862629190L;
		private static final String NAME = "Load";
		private static final String DESC = "Load a new data set";
		private final PaneledReferenceSequenceDisplay mParent;
		String iconLoc = "/toolbarButtonGraphics/general/Open16.gif";
		URL iconURL = PaneledReferenceSequenceDisplay.class
				.getResource(iconLoc);

		public ShowDialogAction(PaneledReferenceSequenceDisplay pParent) {
			super(NAME);
			putValue(SHORT_DESCRIPTION, DESC);
			putValue(SMALL_ICON, new ImageIcon(iconURL));
			mParent = pParent;
		}

		public void actionPerformed(ActionEvent arg0) {
			showDialog(mParent);
		}
	}

	// ///
	// private methods to create interface
	/**
	 * Initializes the dialog interface
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setTitle("Load Data");

		ButtonGroup group = new ButtonGroup();
		group.add(getJRefSeq2FormatFastaCheck());
		group.add(getJRefSeq2FormatGenbankCheck());

		group = new ButtonGroup();
		group.add(getJRefSeqFormatFastaCheck());
		group.add(getJRefSeqFormatGenbankCheck());

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
			jContentPane.add(getJDataFilesPanel(), java.awt.BorderLayout.NORTH);
			jContentPane.add(getJQualityPanel(), java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButtonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJCancelFilesButton(), null);
			jButtonPanel.add(getJLoadFilesButton(), null);
		}
		return jButtonPanel;
	}

	/**
	 * This method initializes jCancelButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJCancelFilesButton() {
		if (jCancelFilesButton == null) {
			Action cancelAction = new AbstractAction("Cancel") {
				public void actionPerformed(ActionEvent e) {
					cancelLoadData();
					setVisible(false);
				}
			};

			jCancelFilesButton = new JButton(cancelAction);

			jCancelFilesButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
					.put(KeyStroke.getKeyStroke("ESCAPE"), "esc");
			jCancelFilesButton.getActionMap().put("esc", cancelAction);

		}
		return jCancelFilesButton;
	}

	/**
	 * This method initializes jLoadFilesButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJLoadFilesButton() {
		if (jLoadFilesButton == null) {
			Action loadFilesAction = new AbstractAction("Load") {
				private static final long serialVersionUID = 872173574761121473L;

				public void actionPerformed(ActionEvent e) {
					if (loadDataFiles()) {
						setVisible(false);
					}
				}
			};

			jLoadFilesButton = new JButton(loadFilesAction);

			jLoadFilesButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
					.put(KeyStroke.getKeyStroke("ENTER"), "ent");
			jLoadFilesButton.getActionMap().put("ent", loadFilesAction);
			jLoadFilesButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
					.put(KeyStroke.getKeyStroke("RETURN"), "ret");
			jLoadFilesButton.getActionMap().put("ret", loadFilesAction);

		}
		return jLoadFilesButton;
	}

	/**
	 * This method initializes jDataFilesPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJDataFilesPanel() {
		if (jDataFilesPanel == null) {
			jDataFilesPanel = new JPanel();
			jDataFilesPanel.add(getJDataFilesTabbedPane(), null);
		}
		return jDataFilesPanel;
	}

	/**
	 * This method initializes jQualityPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJQualityPanel() {
		if (jQualityPanel == null) {
			jQualityPanel = new JPanel();
			jQualityPanel.setLayout(new BoxLayout(jQualityPanel,
					BoxLayout.PAGE_AXIS));
			jQualityPanel.add(getJThresholdPanel(), null);
			jQualityPanel.add(getJQualityFilePanel(), null);
		}
		return jQualityPanel;
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
	 * This method initializes jQualityFileTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJQualityFileTextField() {
		if (jQualityFileTextField == null) {
			jQualityFileTextField = new JTextField();
			jQualityFileTextField.setColumns(30);
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
							// initialize flie chooser
							if (qualsFC == null) {
								qualsFC = new JFileChooser(getLastPath());
							}

							// let user choose file
							int response = qualsFC
									.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
							if (response == JFileChooser.APPROVE_OPTION) {
								getJQualityFileTextField().setText(
										qualsFC.getSelectedFile().toString());
							}

							// save for later
							lastPath = qualsFC.getCurrentDirectory();
						}

					});
		}
		return jQualityFileButton;
	}

	/**
	 * This method initializes jThresholdPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJThresholdPanel() {
		if (jThresholdPanel == null) {
			jSpacerLabel = new JLabel();
			jSpacerLabel.setText("    ");
			jThresholdPanel = new JPanel();
			jThresholdPanel.add(getJQualityCheckBox(), null);
			jThresholdLabel = new JLabel();
			jThresholdLabel.setText("Threshold:");
			jThresholdPanel.add(getJQualityCheckBox(), null);
			jThresholdPanel.add(jSpacerLabel, null);
			jThresholdPanel.add(jThresholdLabel, null);
			jThresholdPanel.add(getJThresholdTextField(), null);
		}
		return jThresholdPanel;
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
			jThresholdTextField.setText(Short.toString(QualifiedDifference
					.getQualityThreshold()));
		}
		return jThresholdTextField;
	}

	/**
	 * This method initializes jDataFilesTabbedPane
	 * 
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getJDataFilesTabbedPane() {
		if (jDataFilesTabbedPane == null) {
			jDataFilesTabbedPane = new JTabbedPane();
			jDataFilesTabbedPane.addTab("Strainer XML", null,
					getJStrainerFileTabPanel(), null);
			jDataFilesTabbedPane.addTab("Alignments", null,
					getJAlignmentFileTabPanel(), null);
			jDataFilesTabbedPane.addTab("Assembly File", null,
					getJAceFileTabPanel(), null);
			jDataFilesTabbedPane.addTab("MSA file", null,
					getJMSAFileTabPanel(), null);
		}
		return jDataFilesTabbedPane;
	}

	/**
	 * This method initializes jStrainerFileTabPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJStrainerFileTabPanel() {
		if (jStrainerFileTabPanel == null) {
			jStrainerFileTabPanel = new JPanel();
			jStrainerFileTabPanel.setLayout(new BoxLayout(
					jStrainerFileTabPanel, BoxLayout.PAGE_AXIS));
			jStrainerFileTabPanel.add(getJRefSeq2FilePanel(), null);
			jStrainerFileTabPanel.add(getJRefSeq2FormatPanel(), null);
			jStrainerFileTabPanel.add(getJStrainerFilePanel(), null);
		}
		return jStrainerFileTabPanel;
	}

	/**
	 * This method initializes jAceFileTabPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJMSAFileTabPanel() {
		if (jMSAFileTabPanel == null) {
			jMSAFileTabPanel = new JPanel();
			jMSAFileTabPanel.setLayout(new BoxLayout(jMSAFileTabPanel,
					BoxLayout.PAGE_AXIS));
			jMSAFileTabPanel.add(getJMSAFilePanel(), null);
			jMSAFileTabPanel.add(getJMSAFormatPanel(), null);
		}
		return jMSAFileTabPanel;
	}

	/**
	 * This method initializes jAceFileTabPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAceFileTabPanel() {
		if (jAceFileTabPanel == null) {
			jAceFileTabPanel = new JPanel();
			jAceFileTabPanel.setLayout(new BoxLayout(jAceFileTabPanel,
					BoxLayout.PAGE_AXIS));
			jAceFileTabPanel.add(getJAceFilePanel(), null);
			jAceFileTabPanel.add(getJAceContigPanel(), null);
			jAceFileTabPanel.add(getJAceChecksPanel(), null);
		}
		return jAceFileTabPanel;
	}

	private JPanel getJAceChecksPanel() {
		JPanel panel = new JPanel();
		panel.add(getJAceRealignCheck(), null);
		panel.add(getJAceTrimCheck(), null);
		panel.add(getJKeepQualCheck(), null);
		return panel;
	}

	/**
	 * This method initializes jAlignmentFileTabPanel for loading Blast or SAM
	 * alignments
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAlignmentFileTabPanel() {
		if (jAlignmentFileTabPanel == null) {
			jAlignmentFileTabPanel = new JPanel();
			jAlignmentFileTabPanel.setLayout(new BoxLayout(
					jAlignmentFileTabPanel, BoxLayout.PAGE_AXIS));
			jAlignmentFileTabPanel.add(getJRefSeqFilePanel(), null);
			jAlignmentFileTabPanel.add(getJRefSeqFormatPanel(), null);
			jAlignmentFileTabPanel.add(getJAlignmentFilePanel(), null);
			jAlignmentFileTabPanel.add(getJAlignmentTypePanel(), null);
		}
		return jAlignmentFileTabPanel;
	}

	/**
	 * This method initializes jRefSeq2FilePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJRefSeq2FilePanel() {
		if (jRefSeq2FilePanel == null) {
			jRefSeq2FileLabel = new JLabel();
			jRefSeq2FileLabel.setText("Reference Sequence File:");
			jRefSeq2FilePanel = new JPanel();
			jRefSeq2FilePanel.add(jRefSeq2FileLabel, null);
			jRefSeq2FilePanel.add(getJRefSeq2FileField(), null);
			jRefSeq2FilePanel.add(getJRefSeq2FileButton(), null);
		}
		return jRefSeq2FilePanel;
	}

	/**
	 * This method initializes jRefSeq2FileField
	 * 
	 * @return javax.swing.JTextField
	 */
	JTextField getJRefSeq2FileField() {
		if (jRefSeq2FileField == null) {
			jRefSeq2FileField = new JTextField();
			jRefSeq2FileField.setColumns(20);
		}
		return jRefSeq2FileField;
	}

	/**
	 * This method initializes jRefSeq2FileButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJRefSeq2FileButton() {
		if (jRefSeq2FileButton == null) {
			jRefSeq2FileButton = new JButton();
			jRefSeq2FileButton.setText("Browse...");
			jRefSeq2FileButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							// initialize flie chooser
							if (refSeqFC == null) {
								refSeqFC = new JFileChooser(getLastPath());
							}

							// let user choose file
							int response = refSeqFC
									.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
							if (response == JFileChooser.APPROVE_OPTION) {
								getJRefSeq2FileField().setText(
										refSeqFC.getSelectedFile().toString());
								// TODO:2 check first char of file and set type
							}

							// save for later
							lastPath = refSeqFC.getCurrentDirectory();
						}
					});
		}
		return jRefSeq2FileButton;
	}

	/**
	 * This method initializes jRefSeq2FormatPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJRefSeq2FormatPanel() {
		if (jRefSeq2FormatPanel == null) {
			jRefSeq2FormatPanel = new JPanel();
			JLabel jRefSeqFormatLabel = new JLabel(
					"Reference Sequence Format: ");
			jRefSeq2FormatPanel.add(jRefSeqFormatLabel, null);
			jRefSeq2FormatPanel.add(getJRefSeq2FormatGenbankCheck(), null);
			jRefSeq2FormatPanel.add(getJRefSeq2FormatFastaCheck(), null);
		}
		return jRefSeq2FormatPanel;
	}

	/**
	 * This method initializes jRefSeq2FormatGenbankCheck
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJRefSeq2FormatGenbankCheck() {
		if (jRefSeq2FormatGenbankCheck == null) {
			jRefSeq2FormatGenbankCheck = new JCheckBox();
			jRefSeq2FormatGenbankCheck.setText("Genbank");
			jRefSeq2FormatGenbankCheck.setSelected(true);
		}
		return jRefSeq2FormatGenbankCheck;
	}

	/**
	 * This method initializes jRefSeq2FormatFastaCheck
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJRefSeq2FormatFastaCheck() {
		if (jRefSeq2FormatFastaCheck == null) {
			jRefSeq2FormatFastaCheck = new JCheckBox();
			jRefSeq2FormatFastaCheck.setText("FASTA");
			jRefSeq2FormatFastaCheck.setSelected(false);
		}
		return jRefSeq2FormatFastaCheck;
	}

	/**
	 * This method initializes jRefSeqFilePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJRefSeqFilePanel() {
		if (jRefSeqFilePanel == null) {
			jRefSeqFileLabel = new JLabel();
			jRefSeqFileLabel.setText("Reference Sequence File:");
			jRefSeqFilePanel = new JPanel();
			jRefSeqFilePanel.add(jRefSeqFileLabel, null);
			jRefSeqFilePanel.add(getJRefSeqFileField(), null);
			jRefSeqFilePanel.add(getJRefSeqFileButton(), null);
		}
		return jRefSeqFilePanel;
	}

	/**
	 * This method initializes jRefSeqFileField
	 * 
	 * @return javax.swing.JTextField
	 */
	JTextField getJRefSeqFileField() {
		if (jRefSeqFileField == null) {
			jRefSeqFileField = new JTextField();
			jRefSeqFileField.setColumns(20);
		}
		return jRefSeqFileField;
	}

	/**
	 * This method initializes jRefSeqFileButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJRefSeqFileButton() {
		if (jRefSeqFileButton == null) {
			jRefSeqFileButton = new JButton();
			jRefSeqFileButton.setText("Browse...");
			jRefSeqFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							// initialize flie chooser
							if (refSeqFC == null) {
								refSeqFC = new JFileChooser(getLastPath());
							}

							// let user choose file
							int response = refSeqFC
									.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
							if (response == JFileChooser.APPROVE_OPTION) {
								getJRefSeqFileField().setText(
										refSeqFC.getSelectedFile().toString());
								// TODO:2 check first char of file and set type
							}

							// save for later
							lastPath = refSeqFC.getCurrentDirectory();
						}
					});
		}
		return jRefSeqFileButton;
	}

	/**
	 * This method initializes jRefSeqFormatPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJRefSeqFormatPanel() {
		if (jRefSeqFormatPanel == null) {
			jRefSeqFormatPanel = new JPanel();
			JLabel jRefSeqFormatLabel = new JLabel(
					"Reference Sequence Format: ");
			jRefSeqFormatPanel.add(jRefSeqFormatLabel, null);
			jRefSeqFormatPanel.add(getJRefSeqFormatGenbankCheck(), null);
			jRefSeqFormatPanel.add(getJRefSeqFormatFastaCheck(), null);
		}
		return jRefSeqFormatPanel;
	}

	/**
	 * This method initializes jRefSeqFormatGenbankCheck
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJRefSeqFormatGenbankCheck() {
		if (jRefSeqFormatGenbankCheck == null) {
			jRefSeqFormatGenbankCheck = new JCheckBox();
			jRefSeqFormatGenbankCheck.setText("Genbank");
			jRefSeqFormatGenbankCheck.setSelected(true);
		}
		return jRefSeqFormatGenbankCheck;
	}

	/**
	 * This method initializes jRefSeqFormatFastaCheck
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJRefSeqFormatFastaCheck() {
		if (jRefSeqFormatFastaCheck == null) {
			jRefSeqFormatFastaCheck = new JCheckBox();
			jRefSeqFormatFastaCheck.setText("FASTA");
			jRefSeqFormatFastaCheck.setSelected(false);
		}
		return jRefSeqFormatFastaCheck;
	}

	/**
	 * This method initializes jStrainerFilePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJStrainerFilePanel() {
		if (jStrainerFilePanel == null) {
			jStrainerFileLabel = new JLabel();
			jStrainerFileLabel.setText("Strainer File:");
			jStrainerFilePanel = new JPanel();
			jStrainerFilePanel.add(jStrainerFileLabel, null);
			jStrainerFilePanel.add(getJStrainerFileField(), null);
			jStrainerFilePanel.add(getJStrainerFileButton(), null);
		}
		return jStrainerFilePanel;
	}

	/**
	 * This method initializes jStrainerFileField
	 * 
	 * @return javax.swing.JTextField
	 */
	JTextField getJStrainerFileField() {
		if (jStrainerFileField == null) {
			jStrainerFileField = new JTextField();
			jStrainerFileField.setColumns(20);
		}
		return jStrainerFileField;
	}

	/**
	 * This method initializes jStrainerFileButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJStrainerFileButton() {
		if (jStrainerFileButton == null) {
			jStrainerFileButton = new JButton();
			jStrainerFileButton.setText("Browse...");
			jStrainerFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							// initialize flie chooser
							if (strainerFC == null) {
								strainerFC = new JFileChooser(getLastPath());
							}

							// let user choose file
							int response = strainerFC
									.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
							if (response == JFileChooser.APPROVE_OPTION) {
								getJStrainerFileField()
										.setText(
												strainerFC.getSelectedFile()
														.toString());
							}

							// save for later
							lastPath = strainerFC.getCurrentDirectory();
						}
					});
		}
		return jStrainerFileButton;
	}

	/**
	 * This method initializes jAlignmentFilePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAlignmentFilePanel() {
		if (jAlignmentFilePanel == null) {
			jAlignmentFileLabel = new JLabel();
			jAlignmentFileLabel.setText("ALignment File:");
			jAlignmentFilePanel = new JPanel();
			jAlignmentFilePanel.add(jAlignmentFileLabel, null);
			jAlignmentFilePanel.add(getJAlignmentFileField(), null);
			jAlignmentFilePanel.add(getJAlignmentFileButton(), null);
		}
		return jAlignmentFilePanel;
	}

	/**
	 * This method initializes jAlignmentFileField: text box to enter alignment
	 * file path
	 * 
	 * @return javax.swing.JTextField
	 */
	JTextField getJAlignmentFileField() {
		if (jAlignmentFileField == null) {
			jAlignmentFileField = new JTextField();
			jAlignmentFileField.setColumns(20);
		}
		return jAlignmentFileField;
	}

	/**
	 * This method initializes jAlignmentFileButton: to launch FileChooser for
	 * selecting alignment file
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJAlignmentFileButton() {
		if (jAlignmentFileButton == null) {
			jAlignmentFileButton = new JButton();
			jAlignmentFileButton.setText("Browse...");
			jAlignmentFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							// initialize flie chooser
							if (alignmentFC == null) {
								alignmentFC = new JFileChooser(getLastPath());
							}

							// let user choose file
							int response = alignmentFC
									.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
							if (response == JFileChooser.APPROVE_OPTION) {
								getJAlignmentFileField().setText(
										alignmentFC.getSelectedFile()
												.toString());
							}

							// save for later
							lastPath = alignmentFC.getCurrentDirectory();
						}
					});
		}
		return jAlignmentFileButton;
	}

	/**
	 * This method initializes jAlignmentTypePanel to set the alignment type to
	 * Blast or SAM
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAlignmentTypePanel() {
		if (jAlignmentTypePanel == null) {
			jAlignmentTypeLabel = new JLabel();
			jAlignmentTypeLabel.setText("Alignment is:");
			jAlignmentTypePanel = new JPanel();
			jAlignmentTypePanel.add(jAlignmentTypeLabel, null);
			jAlignmentTypePanel.add(getJAlignmentTypeBox(), null);
		}
		return jAlignmentTypePanel;
	}

	/**
	 * This method initializes jAlignmentTypeBox to choose the style of
	 * alignment file
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJAlignmentTypeBox() {
		if (jAlignmentTypeBox == null) {
			jAlignmentTypeBox = new JComboBox();
			jAlignmentTypeBox.setModel(new DefaultComboBoxModel(
					GetReferenceFromFileTask.ALIGNMENT_TYPE_LIST));
		}
		return jAlignmentTypeBox;
	}

	/**
	 * This method initializes jAceFilePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAceFilePanel() {
		if (jAceFilePanel == null) {
			jAceFileLabel = new JLabel();
			jAceFileLabel.setText("Ace/CAF File:");
			jAceFilePanel = new JPanel();
			jAceFilePanel.add(jAceFileLabel, null);
			jAceFilePanel.add(getJAceFileField(), null);
			jAceFilePanel.add(getJAceFileButton(), null);
		}
		return jAceFilePanel;
	}

	/**
	 * This method initializes jAceFileField
	 * 
	 * @return javax.swing.JTextField
	 */
	JTextField getJAceFileField() {
		if (jAceFileField == null) {
			jAceFileField = new JTextField();
			jAceFileField.setColumns(20);
		}
		return jAceFileField;
	}

	/**
	 * This method initializes jAceFileButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJAceFileButton() {
		if (jAceFileButton == null) {
			jAceFileButton = new JButton();
			jAceFileButton.setText("Browse...");
			jAceFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							// initialize flie chooser
							if (aceFC == null) {
								aceFC = new JFileChooser(getLastPath());
							}

							// let user choose file
							int response = aceFC
									.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
							if (response == JFileChooser.APPROVE_OPTION) {
								getJAceFileField().setText(
										aceFC.getSelectedFile().toString());

								// look for quality data..ace files usually come
								// with quality data
								lookForQualityFile(aceFC.getSelectedFile());
							}

							// save for later
							lastPath = aceFC.getCurrentDirectory();
						}
					});
		}
		return jAceFileButton;
	}

	protected void lookForQualityFile(File aceFile) {
		File phrapDir = aceFile.getParentFile();
		String aceName = aceFile.getName();

		// first try to guess a name
		StringTokenizer st = new StringTokenizer(aceName, ".");
		StringBuffer prefix = new StringBuffer(aceName.length());
		Set<String> guesses = new HashSet<String>();
		while (st.hasMoreElements()) {
			String part = st.nextToken();
			if (part.equalsIgnoreCase("ace")) {
				guesses.add(prefix.toString() + "qual");
			}
			prefix.append(part).append(".");
			if (part.equalsIgnoreCase("screen")) {
				guesses.add(prefix.toString() + "qual");
			}
		}

		long maxSize = 0;
		File biggestQualFile = null;
		for (String guess : guesses) {
			File guessFile = new File(phrapDir.getAbsolutePath()
					+ File.separator + guess);
			if (guessFile.exists() && guessFile.canRead()) {
				if (guessFile.length() > maxSize) {
					maxSize = guessFile.length();
					biggestQualFile = guessFile;
				}
			}
		}

		if (biggestQualFile != null) {
			String oldQFile = getJQualityFileTextField().getText();
			boolean setQualFile = false;
			if (oldQFile == null || oldQFile.trim().length() == 0) {
				setQualFile = true;
			} else {
				int response = JOptionPane
						.showConfirmDialog(
								PaneledReferenceSequenceDisplay.frame,
								"Strainer thinks the following quality file goes with the selected ace file. Do you want to use it?\n"
										+ biggestQualFile.getName() + "'",
								"Use matching qual file?",
								JOptionPane.YES_NO_OPTION);
				setQualFile = response == JOptionPane.YES_OPTION;
			}

			if (setQualFile) {
				getJQualityCheckBox().setSelected(true);
				getJQualityFileTextField().setText(
						biggestQualFile.getAbsolutePath());
			}
		}
	}

	/**
	 * This method initializes jAceContigPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAceContigPanel() {
		if (jAceContigPanel == null) {
			jAceContigPanel = new JPanel();
			jAceContigLabel = new JLabel("Contig Number: ");
			jAceContigPanel.add(jAceContigLabel, null);
			jAceContigPanel.add(getJAceContigField(), null);
		}
		return jAceContigPanel;
	}

	/**
	 * This method initializes jAceContigField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJAceContigField() {
		if (jAceContigField == null) {
			jAceContigField = new JTextField(10);
		}
		return jAceContigField;
	}

	private JCheckBox getJAceRealignCheck() {
		if (jAceRealignCheck == null) {
			jAceRealignCheck = new JCheckBox("Recompute alignments");
		}
		return jAceRealignCheck;
	}

	private JCheckBox getJAceTrimCheck() {
		if (jAceTrimCheck == null) {
			jAceTrimCheck = new JCheckBox("Trim alignments");
			jAceTrimCheck.setSelected(true);
		}
		return jAceTrimCheck;
	}

	private JCheckBox getJKeepQualCheck() {
		if (jKeepQualCheck == null) {
			jKeepQualCheck = new JCheckBox("Keep CAF Qual Scores");
			jKeepQualCheck.setSelected(true);
		}
		return jKeepQualCheck;
	}

	/**
	 * This method initializes jQualityCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJQualityCheckBox() {
		if (jQualityCheckBox == null) {
			jQualityCheckBox = new JCheckBox();
			jQualityCheckBox.setText("Add Quality Data");
			jQualityCheckBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					boolean enabled = getJQualityCheckBox().isSelected();
					getJQualityFileButton().setEnabled(enabled);
					getJQualityFileTextField().setEnabled(enabled);
					getJThresholdTextField().setEnabled(enabled);
				}
			});
			jQualityCheckBox.setSelected(true);
			jQualityCheckBox.setSelected(false);
		}
		return jQualityCheckBox;
	}

	/**
	 * This method initializes jMSAFilePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJMSAFilePanel() {
		if (jMSAFilePanel == null) {
			jMSAFileLabel = new JLabel();
			jMSAFileLabel.setText("MSA File:");
			jMSAFilePanel = new JPanel();
			jMSAFilePanel.add(jMSAFileLabel, null);
			jMSAFilePanel.add(getJMSAFileField(), null);
			jMSAFilePanel.add(getJMSAFileButton(), null);
		}
		return jMSAFilePanel;
	}

	/**
	 * This method initializes jMSAFileField
	 * 
	 * @return javax.swing.JTextField
	 */
	JTextField getJMSAFileField() {
		if (jMSAFileField == null) {
			jMSAFileField = new JTextField();
			jMSAFileField.setColumns(20);
		}
		return jMSAFileField;
	}

	/**
	 * This method initializes jMSAFileButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJMSAFileButton() {
		if (jMSAFileButton == null) {
			jMSAFileButton = new JButton();
			jMSAFileButton.setText("Browse...");
			jMSAFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						private JFileChooser msaFC;

						public void actionPerformed(java.awt.event.ActionEvent e) {
							// initialize flie chooser
							if (msaFC == null) {
								msaFC = new JFileChooser(getLastPath());
							}

							// let user choose file
							int response = msaFC
									.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
							if (response == JFileChooser.APPROVE_OPTION) {
								getJMSAFileField().setText(
										msaFC.getSelectedFile().toString());

								// look for quality data..ace files usually come
								// with quality data
								lookForQualityFile(msaFC.getSelectedFile());
							}

							// save for later
							lastPath = msaFC.getCurrentDirectory();
						}
					});
		}
		return jMSAFileButton;
	}

	/**
	 * This method initializes jMSAFormatPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJMSAFormatPanel() {
		if (jMSAFormatPanel == null) {
			jMSAFormatLabel = new JLabel();
			jMSAFormatLabel.setText("MSA Format:");
			jMSAFormatPanel = new JPanel();
			jMSAFormatPanel.add(jMSAFormatLabel, null);
			jMSAFormatPanel.add(getJMSAFormatBox(), null);
		}
		return jMSAFormatPanel;
	}

	/**
	 * This method initializes jMSAFormatBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJMSAFormatBox() {
		if (jMSAFormatBox == null) {
			jMSAFormatBox = new JComboBox();
			jMSAFormatBox.setModel(new DefaultComboBoxModel(
					ImportMSATask.FORMAT_LIST));
		}
		return jMSAFormatBox;
	}

	public static int getAssemblyFileType(File assemblyFile) {
		String afName = assemblyFile.getName();
		if (afName.length() > 3) {
			if (afName.substring(afName.length() - 3).equalsIgnoreCase("caf")) {
				return CAF;
			} else if (afName.substring(afName.length() - 3).equalsIgnoreCase(
					"ace")) {
				return ACE;
			}
		}
		return -1;

	}
}
