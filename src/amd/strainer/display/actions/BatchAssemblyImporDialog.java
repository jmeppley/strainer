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
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import amd.strainer.GlobalSettings;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.util.Util;
import amd.strainer.file.BatchAssemblyImportTask;

public class BatchAssemblyImporDialog extends JDialog {
	// TODO:2 fix out of range alignments

	/**
	 * Defines an action that can be added to the GUI menu to open this dialog
	 * 
	 * @author jmeppley
	 */
	public static class ShowDialogAction extends AbstractAction implements
			Action {
		private static final String NAME = "Batch Assembly Contig Import ... ";
		private static final String DESC = "Convert PHRAP generated ACE or Mira generated CAF file of contig assemblies (other sources may work, but are untested) into a set of Strainer XML files";
		private final PaneledReferenceSequenceDisplay mParent;

		public ShowDialogAction(PaneledReferenceSequenceDisplay pParent) {
			super(NAME);
			putValue(SHORT_DESCRIPTION, DESC);
			mParent = pParent;
		}

		public void actionPerformed(ActionEvent arg0) {
			showDialog(mParent);
		}
	}

	private PaneledReferenceSequenceDisplay mParent = null;

	private JFileChooser assemblyFC = null;
	private JFileChooser outputFC = null;
	private JFileChooser qualityFC = null;
	private File lastPath = new File(
			GlobalSettings.getSetting(GlobalSettings.INPUT_DIR_KEY), "~");

	private JPanel jContentPane = null;
	private JPanel jButtonPanel = null;
	private JPanel jCenterPanel = null;
	private JPanel jAssemblyFilePanel = null;
	private JPanel jContigNumbersPanel = null;
	private JPanel jOutputDirPanel = null;
	private JButton jCancelButton = null;
	private JButton jLoadButton = null;
	private JTextField jAssemblyFileTextField = null;
	private JButton jAssemblyFileButton = null;
	private JLabel jContigNumbersLabel = null;
	private JTextField jContigNumbersTextField = null;
	private JTextField jOutputDirTextField = null;
	private JButton jOutputDirButton = null;

	private JPanel jQualityFilePanel = null;

	private JButton jQualityFileButton;

	private JTextField jQualityFileTextField;

	private JCheckBox jQualityFileCheck;

	public static void showDialog(PaneledReferenceSequenceDisplay pParent) {
		BatchAssemblyImporDialog dialog = new BatchAssemblyImporDialog(pParent);
		dialog.setVisible(true);
	}

	/**
	 * This is the default constructor
	 * 
	 * @param parent
	 */
	private BatchAssemblyImporDialog(PaneledReferenceSequenceDisplay parent) {
		super(JOptionPane
				.getFrameForComponent(PaneledReferenceSequenceDisplay.frame),
				true);
		mParent = parent;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setTitle("Batch Assembly contig import");
		this.setContentPane(getJContentPane());
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
			jContentPane.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJCenterPanel(), java.awt.BorderLayout.CENTER);
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
			jButtonPanel.add(getJCancelButton(), null);
			jButtonPanel.add(getJLoadButton(), null);
		}
		return jButtonPanel;
	}

	/**
	 * This method initializes jCenterPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJCenterPanel() {
		if (jCenterPanel == null) {
			jCenterPanel = new JPanel();
			jCenterPanel.setLayout(new BoxLayout(jCenterPanel,
					BoxLayout.PAGE_AXIS));
			jCenterPanel.add(getJAssemblyFilePanel(), null);
			jCenterPanel.add(getJContigNumbersPanel(), null);
			jCenterPanel.add(getJQualityFilePanel(), null);
			jCenterPanel.add(getJOutputDirPanel(), null);
		}
		return jCenterPanel;
	}

	private JPanel getJQualityFilePanel() {
		if (jQualityFilePanel == null) {
			jQualityFilePanel = new JPanel();
			jQualityFilePanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Quality Data File:"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			jQualityFilePanel.add(getJQualityFileCheck(), null);
			jQualityFilePanel.add(getJQualityFileTextField(), null);
			jQualityFilePanel.add(getJQualityFileButton(), null);
		}
		return jQualityFilePanel;
	}

	private JButton getJQualityFileButton() {
		if (jQualityFileButton == null) {
			jQualityFileButton = new JButton();
			jQualityFileButton.setText("Browse");
			jQualityFileButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					// initialize flie chooser
					if (qualityFC == null) {
						qualityFC = new JFileChooser(lastPath);
					}

					// let user choose file
					int response = qualityFC
							.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
					if (response == JFileChooser.APPROVE_OPTION) {
						getJQualityFileTextField().setText(
								qualityFC.getSelectedFile().toString());
					}

					// save for later
					lastPath = qualityFC.getCurrentDirectory();
					GlobalSettings.putSetting(GlobalSettings.INPUT_DIR_KEY,
							lastPath.getAbsolutePath());
				}
			});
		}
		return jQualityFileButton;
	}

	private JTextField getJQualityFileTextField() {
		if (jQualityFileTextField == null) {
			jQualityFileTextField = new JTextField();
			jQualityFileTextField.setColumns(30);
		}
		return jQualityFileTextField;
	}

	private JCheckBox getJQualityFileCheck() {
		if (jQualityFileCheck == null) {
			jQualityFileCheck = new JCheckBox();
			jQualityFileCheck.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					boolean enabled = getJQualityFileCheck().isSelected();
					getJQualityFileButton().setEnabled(enabled);
					getJQualityFileTextField().setEnabled(enabled);
				}
			});
		}
		return jQualityFileCheck;
	}

	/**
	 * This method initializes jAssemblyFilePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAssemblyFilePanel() {
		if (jAssemblyFilePanel == null) {
			jAssemblyFilePanel = new JPanel();

			jAssemblyFilePanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Assembly File:"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));

			jAssemblyFilePanel.add(getJAssemblyFileTextField(), null);
			jAssemblyFilePanel.add(getJAssemblyFileButton(), null);

		}
		return jAssemblyFilePanel;
	}

	/**
	 * This method initializes jCloneSizePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContigNumbersPanel() {
		if (jContigNumbersPanel == null) {
			jContigNumbersLabel = new JLabel();
			jContigNumbersLabel
					.setText("Separate with commas (blank means all contigs):");
			jContigNumbersPanel = new JPanel();

			jContigNumbersPanel
					.setBorder(BorderFactory.createCompoundBorder(BorderFactory
							.createTitledBorder("Contig numbers to load:"),
							BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			jContigNumbersPanel.add(jContigNumbersLabel, null);
			jContigNumbersPanel.add(getJContigNumbersTextField(), null);
		}
		return jContigNumbersPanel;
	}

	/**
	 * This method initializes jOutputDirPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJOutputDirPanel() {
		if (jOutputDirPanel == null) {
			jOutputDirPanel = new JPanel();
			jOutputDirPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Output Directory:"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			jOutputDirPanel.add(getJOutputDirTextField(), null);
			jOutputDirPanel.add(getJOutputDirButton(), null);
		}
		return jOutputDirPanel;
	}

	private JButton getJOutputDirButton() {
		if (jOutputDirButton == null) {
			jOutputDirButton = new JButton();
			jOutputDirButton.setText("Browse");
			jOutputDirButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							// initialize flie chooser
							if (outputFC == null) {
								outputFC = new JFileChooser(lastPath);
								outputFC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							}

							// let user choose file
							int response = outputFC
									.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
							if (response == JFileChooser.APPROVE_OPTION) {
								getJOutputDirTextField().setText(
										outputFC.getSelectedFile().toString());
							}

							// save for later
							lastPath = outputFC.getCurrentDirectory();
							GlobalSettings.putSetting(
									GlobalSettings.INPUT_DIR_KEY,
									lastPath.getAbsolutePath());
						}
					});
		}
		return jOutputDirButton;
	}

	private JTextField getJOutputDirTextField() {
		if (jOutputDirTextField == null) {
			jOutputDirTextField = new JTextField();
			jOutputDirTextField.setColumns(30);
		}
		return jOutputDirTextField;
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
	 * This method initializes jLoadButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJLoadButton() {
		if (jLoadButton == null) {
			Action loadAction = new AbstractAction("Load") {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (doAssemblyFileLoad()) {
						setVisible(false);
					}
				}
			};

			jLoadButton = new JButton(loadAction);
			jLoadButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("ENTER"), "ent");
			jLoadButton.getActionMap().put("ent", loadAction);
			jLoadButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("RETURN"), "ret");
			jLoadButton.getActionMap().put("ret", loadAction);
		}
		return jLoadButton;
	}

	protected boolean doAssemblyFileLoad() {
		String assemblyFile = getJAssemblyFileTextField().getText();
		String outputDir = getJOutputDirTextField().getText();
		String qualityFile = getJQualityFileTextField().getText();

		File assemblyFileFile = new File(assemblyFile);
		if (!assemblyFileFile.exists()) {
			Util.displayErrorMessage("Assembly file not found",
					"Could not find: " + assemblyFile);
			return false;
		}

		if (!assemblyFileFile.canRead()) {
			Util.displayErrorMessage("Assembly file not readable",
					"Could not read: " + assemblyFile);
			return false;
		}

		File outputDirFile = new File(outputDir);
		if (!outputDirFile.exists()) {
			// create directory (and parents if necessary)
			outputDirFile.mkdirs();
		} else {
			if (!outputDirFile.isDirectory()) {
				Util.displayErrorMessage("Output location is not a directory!",
						outputDir);
			}
		}
		if (outputDir.charAt(outputDir.length() - 1) != File.separatorChar) {
			outputDir = outputDir + File.separator;
		}

		if (getJQualityFileCheck().isSelected()) {
			File qFileFile = new File(qualityFile);
			if (!qFileFile.exists()) {
				Util.displayErrorMessage("Qual file not found",
						"Could not find: " + qualityFile);
				return false;
			}

			if (!qFileFile.canRead()) {
				Util.displayErrorMessage("Qual file not readable",
						"Could not read: " + qualityFile);
				return false;
			}

		}

		Set<Long> contigNumbers = new HashSet<Long>();
		try {
			StringTokenizer st = new StringTokenizer(
					getJContigNumbersTextField().getText(), ",");
			while (st.hasMoreTokens()) {
				contigNumbers.add(new Long(st.nextToken().trim()));
			}
		} catch (Exception e) {
			Util.displayErrorMessage("Cannot parse contig numbers",
					"Error parsing contig numbers.Enter only integers and separate using commas. ");
			return false;
		}

		Task task;
		if (getJQualityFileCheck().isSelected()) {
			task = new BatchAssemblyImportTask(assemblyFile,
					outputDirFile.getAbsolutePath(), qualityFile, contigNumbers);
		} else {
			task = new BatchAssemblyImportTask(assemblyFile,
					outputDirFile.getAbsolutePath(), contigNumbers);
		}

		SequenceDataLoader loader = new SequenceDataLoader(mParent, task);
		loader.load();

		// return true, task and loader will take care of handling any other
		// errors
		return true;

	}

	/**
	 * This method initializes jAssemblyFileTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJAssemblyFileTextField() {
		if (jAssemblyFileTextField == null) {
			jAssemblyFileTextField = new JTextField();
			jAssemblyFileTextField.setColumns(30);
		}
		return jAssemblyFileTextField;
	}

	/**
	 * This method initializes jAssemblyFileButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJAssemblyFileButton() {
		if (jAssemblyFileButton == null) {
			jAssemblyFileButton = new JButton();
			jAssemblyFileButton.setText("Browse");
			jAssemblyFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							// initialize flie chooser
							if (assemblyFC == null) {
								assemblyFC = new JFileChooser(lastPath);
							}

							// let user choose file
							int response = assemblyFC
									.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
							if (response == JFileChooser.APPROVE_OPTION) {
								getJAssemblyFileTextField()
										.setText(
												assemblyFC.getSelectedFile()
														.toString());
							}

							// save for later
							lastPath = assemblyFC.getCurrentDirectory();
							GlobalSettings.putSetting(
									GlobalSettings.INPUT_DIR_KEY,
									lastPath.getAbsolutePath());
						}
					});
		}
		return jAssemblyFileButton;
	}

	/**
	 * This method initializes jContigNnumbersTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJContigNumbersTextField() {
		if (jContigNumbersTextField == null) {
			jContigNumbersTextField = new JTextField();
			jContigNumbersTextField.setColumns(7);
		}
		return jContigNumbersTextField;
	}
}
