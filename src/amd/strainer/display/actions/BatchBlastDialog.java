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
import amd.strainer.file.BatchBlastTask;

public class BatchBlastDialog extends JDialog {
	// TODO:1 Test this feature

	/**
	 * Defines an action that can be added to the GUI menu to open this dialog
	 * 
	 * @author jmeppley
	 */
	public static class ShowDialogAction extends AbstractAction implements
			Action {
		private static final String NAME = "Batch Blast Import ... ";
		private static final String DESC = "Convert a multi-reference sequence BLAST output into multipl Strainer files";
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

	private JFileChooser blastFC = null;
	private JFileChooser outputFC = null;
	private JFileChooser qualityFC = null;
	private File lastPath = new File(GlobalSettings.getSetting(
			GlobalSettings.INPUT_DIR_KEY, "~"));

	private JPanel jContentPane = null;
	private JPanel jButtonPanel = null;
	private JPanel jCenterPanel = null;
	private JPanel jBlastFilePanel = null;
	private JPanel jCloneSizePanel = null;
	private JPanel jOutputDirPanel = null;
	private JPanel jOutputPrefixPanel = null;
	private JButton jCancelButton = null;
	private JButton jLoadButton = null;
	private JTextField jBlastFileTextField = null;
	private JButton jBlastFileButton = null;
	private JLabel jMinimumCloneLabel = null;
	private JTextField jMinimumCloneTextField = null;
	private JLabel jMaximumCloneLabel = null;
	private JTextField jMaximumCloneTextField = null;
	private JLabel jCloneSpacerLabel = null;
	private JTextField jOutputDirTextField = null;
	private JButton jOutputDirButton = null;
	private JTextField jOutputPrefixTextField = null;

	private JPanel jQualityFilePanel = null;

	private JButton jQualityFileButton;

	private JTextField jQualityFileTextField;

	private JCheckBox jQualityFileCheck;

	public static void showDialog(PaneledReferenceSequenceDisplay pParent) {
		BatchBlastDialog dialog = new BatchBlastDialog(pParent);
		dialog.setVisible(true);
	}

	/**
	 * This is the default constructor
	 * 
	 * @param parent
	 */
	private BatchBlastDialog(PaneledReferenceSequenceDisplay parent) {
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
		// this.setSize(300, 200);
		this.setTitle("Batch BLAST import");
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
			jCenterPanel.add(getJBlastFilePanel(), null);
			jCenterPanel.add(getJCloneSizePanel(), null);
			jCenterPanel.add(getJQualityFilePanel(), null);
			jCenterPanel.add(getJOutputDirPanel(), null);
			jCenterPanel.add(getJOutputPrefixPanel(), null);
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
	 * This method initializes jBlastFilePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJBlastFilePanel() {
		if (jBlastFilePanel == null) {
			jBlastFilePanel = new JPanel();

			jBlastFilePanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Blast Output File:"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));

			jBlastFilePanel.add(getJBlastFileTextField(), null);
			jBlastFilePanel.add(getJBlastFileButton(), null);

		}
		return jBlastFilePanel;
	}

	/**
	 * This method initializes jCloneSizePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJCloneSizePanel() {
		if (jCloneSizePanel == null) {
			jCloneSpacerLabel = new JLabel();
			jCloneSpacerLabel.setText("     ");
			jMaximumCloneLabel = new JLabel();
			jMaximumCloneLabel.setText("Maximum:");
			jMinimumCloneLabel = new JLabel();
			jMinimumCloneLabel.setText("Minimum:");
			jCloneSizePanel = new JPanel();

			jCloneSizePanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Clone Size Range:"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			jCloneSizePanel.add(jMinimumCloneLabel, null);
			jCloneSizePanel.add(getJMinimumCloneTextField(), null);
			jCloneSizePanel.add(jCloneSpacerLabel, null);
			jCloneSizePanel.add(jMaximumCloneLabel, null);
			jCloneSizePanel.add(getJMaximumCloneTextField(), null);
		}
		return jCloneSizePanel;
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
								outputFC
										.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
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
									GlobalSettings.INPUT_DIR_KEY, lastPath
											.getAbsolutePath());
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
	 * This method initializes jOutputPrefixPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJOutputPrefixPanel() {
		if (jOutputPrefixPanel == null) {
			jOutputPrefixPanel = new JPanel();
			jOutputPrefixPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Output File Prefix:"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			jOutputPrefixPanel.add(getJOutputPrefixTextField(), null);
		}
		return jOutputPrefixPanel;
	}

	private JTextField getJOutputPrefixTextField() {
		if (jOutputPrefixTextField == null) {
			jOutputPrefixTextField = new JTextField();
			jOutputPrefixTextField.setColumns(10);
		}
		return jOutputPrefixTextField;
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
					if (doBatchLoad()) {
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

	protected boolean doBatchLoad() {
		String blastFile = getJBlastFileTextField().getText();
		String outputDir = getJOutputDirTextField().getText();
		String qualityFile = getJQualityFileTextField().getText();

		// TODO:3 sanity check inputs before loading starts

		int minClone, maxClone;
		try {
			minClone = Integer.parseInt(getJMinimumCloneTextField().getText());
			maxClone = Integer.parseInt(getJMaximumCloneTextField().getText());
		} catch (Exception e) {
			Util.displayErrorMessage("Cannot parse clone sizes",
					"Error parsing clone sizes");
			return false;
		}

		Task task = new BatchBlastTask(blastFile, outputDir, qualityFile,
				minClone, maxClone);

		SequenceDataLoader loader = new SequenceDataLoader(mParent, task);
		loader.load();

		// return true, task and loader will take care of handling any other
		// errors
		return true;

	}

	/**
	 * This method initializes jBlastFileTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJBlastFileTextField() {
		if (jBlastFileTextField == null) {
			jBlastFileTextField = new JTextField();
			jBlastFileTextField.setColumns(30);
		}
		return jBlastFileTextField;
	}

	/**
	 * This method initializes jBlastFileButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJBlastFileButton() {
		if (jBlastFileButton == null) {
			jBlastFileButton = new JButton();
			jBlastFileButton.setText("Browse");
			jBlastFileButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							// initialize flie chooser
							if (blastFC == null) {
								blastFC = new JFileChooser(lastPath);
							}

							// let user choose file
							int response = blastFC
									.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
							if (response == JFileChooser.APPROVE_OPTION) {
								getJBlastFileTextField().setText(
										blastFC.getSelectedFile().toString());
							}

							// save for later
							lastPath = blastFC.getCurrentDirectory();
							GlobalSettings.putSetting(
									GlobalSettings.INPUT_DIR_KEY, lastPath
											.getAbsolutePath());
						}
					});
		}
		return jBlastFileButton;
	}

	/**
	 * This method initializes jMinimumCloneTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJMinimumCloneTextField() {
		if (jMinimumCloneTextField == null) {
			jMinimumCloneTextField = new JTextField();
			jMinimumCloneTextField.setColumns(7);
		}
		return jMinimumCloneTextField;
	}

	/**
	 * This method initializes jMaximumCloneTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJMaximumCloneTextField() {
		if (jMaximumCloneTextField == null) {
			jMaximumCloneTextField = new JTextField();
			jMaximumCloneTextField.setColumns(7);

		}
		return jMaximumCloneTextField;
	}

}
