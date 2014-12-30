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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import amd.strainer.GlobalSettings;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.util.Util;

public class SaveStrainsFileDialog extends JDialog {

	// Main panel
	private JPanel jContentPane = null;
	private JPanel jCenterPanel = null;

	// buttons
	private JPanel jButtonPanel = null;
	private JButton jCancelButton = null;
	private JButton jSaveFileButton = null;

	// File controls
	private JPanel jStrainsFilePanel = null;
	private JLabel jStrainsFileLabel = null;
	private JPanel jStrainsFileHeaderPanel = null;
	private JLabel jStrainsFileHeaderLabel = null;
	private JButton jStrainsFileButton = null;
	private JPanel jStrainsFormatHeaderPanel = null;
	private JPanel jStrainsFormatPanel;
	private JCheckBox jStrainsFormatACECheck;
	private JCheckBox jStrainsFormatXMLCheck;

	JFileChooser fc = null;

	boolean save = false;

	public static File strainsFile = new File(GlobalSettings.getSetting(
			GlobalSettings.INPUT_DIR_KEY,
			"~")
			+ "/strains.xml");

	/**
	 * setup and show the dialog
	 * 
	 * @param pParent
	 *            RefrenceSequenceDisplay object initiating this dialog
	 */
	public static boolean showDialog(PaneledReferenceSequenceDisplay pParent) {
		if (pParent.getReferenceSequence().strainsFile != null) {
			strainsFile = new File(pParent.getReferenceSequence().strainsFile);
		}

		SaveStrainsFileDialog dialog = new SaveStrainsFileDialog();

		dialog.setVisible(true);

		if (dialog.save) {
			try {
				if (dialog.getJStrainsFormatXMLButton().isSelected()) {
					amd.strainer.file.Util.writeStrainsToXML(pParent
							.getReferenceSequence(), strainsFile, null, true);
				} else {
					amd.strainer.file.Util.writeStrainsToAce(pParent
							.getReferenceSequence(), strainsFile);
				}
				return true;
			} catch (IOException ex) {
				ex.printStackTrace(System.err);
				Util.displayErrorMessage(
						PaneledReferenceSequenceDisplay.frame,
						ex);
			}
		}
		return false;
	}

	private SaveStrainsFileDialog() {
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
				cancelAction();
			}
		});

		// set up filee chooser
		try {
			fc = new JFileChooser();
			fc.setDialogTitle("Choose file to save strain data as XML");
		} catch (java.security.AccessControlException ace) {
			System.out
					.println("Cannot initialize file chooser, there may be a permissions problem: "
							+ ace.getMessage());
			fc = null;
		}

		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(400, 250);

		ButtonGroup group = new ButtonGroup();
		group.add(getJStrainsFormatACEButton());
		group.add(getJStrainsFormatXMLButton());

		this.setContentPane(getJContentPane());
		this.setLocationRelativeTo(PaneledReferenceSequenceDisplay.frame);
		this.setLocation(
				(PaneledReferenceSequenceDisplay.frame.getWidth() - this
						.getWidth()) / 2,
				(PaneledReferenceSequenceDisplay.frame.getHeight() - this
						.getHeight()) / 2);
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
			jButtonPanel.add(getJSaveFileButton(), null);
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
			jCenterPanel.add(getJStrainsFileHeaderPanel());
			jCenterPanel.add(getJStrainsFilePanel());
			jCenterPanel.add(getJStrainsFormatHeaderPanel());
			jCenterPanel.add(getJStrainsFormatPanel());
		}
		return jCenterPanel;
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
					cancelAction();
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
	 * Called when CANCEL button pressed. Closes dialog.
	 */
	void cancelAction() {
		System.out.println("Cancel!");
		save = false;
		this.setVisible(false);
	}

	/**
	 * This method initializes jSaveButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJSaveFileButton() {
		if (jSaveFileButton == null) {
			Action saveAction = new AbstractAction("Save to file") {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					saveAction();
				}
			};
			jSaveFileButton = new JButton(saveAction);
			jSaveFileButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("ENTER"),
					"ent");
			jSaveFileButton.getActionMap().put("ent", saveAction);
			jSaveFileButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("RETURN"),
					"ret");
			jSaveFileButton.getActionMap().put("ret", saveAction);
		}
		return jSaveFileButton;
	}

	void saveAction() {
		save = true;
		this.setVisible(false);
	}

	/**
	 * This method initializes jStrainsFileHeaderPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJStrainsFileHeaderPanel() {
		if (jStrainsFileHeaderPanel == null) {
			jStrainsFileHeaderPanel = new JPanel();
			jStrainsFileHeaderPanel.add(getJStrainsFileHeaderLabel());
		}
		return jStrainsFileHeaderPanel;
	}

	/**
	 * This method initializes jStrainsFilePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJStrainsFilePanel() {
		if (jStrainsFilePanel == null) {
			jStrainsFilePanel = new JPanel();
			jStrainsFilePanel.add(getJStrainsFileLabel());
			jStrainsFilePanel.add(getJStrainsFileButton());
		}
		return jStrainsFilePanel;
	}

	/**
	 * This method initializes jStrainsFileButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJStrainsFileButton() {
		if (jStrainsFileButton == null) {
			jStrainsFileButton = new JButton();
			if (fc != null) {
				jStrainsFileButton.setText("Set...");
				jStrainsFileButton
						.addActionListener(new java.awt.event.ActionListener() {
							public void actionPerformed(
									java.awt.event.ActionEvent e) {
								fc.setSelectedFile(strainsFile);
								fc.setCurrentDirectory(strainsFile
										.getParentFile());
								int returnVal = fc
										.showSaveDialog(PaneledReferenceSequenceDisplay.frame);
								if (!(returnVal == JFileChooser.APPROVE_OPTION)) {
									System.out
											.println("Leaving file name unchanged");
								} else {
									strainsFile = fc.getSelectedFile();
									setStrainsFileLabelText();
								}
							}
						});
			} else {
				jStrainsFileButton.setEnabled(false);
			}
		}
		return jStrainsFileButton;
	}

	/**
	 * This method initializes jStrainsFileHeaderLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private JLabel getJStrainsFileHeaderLabel() {
		if (jStrainsFileHeaderLabel == null) {
			jStrainsFileHeaderLabel = new JLabel();
			jStrainsFileHeaderLabel.setText("File to save strain info:");
		}
		return jStrainsFileHeaderLabel;
	}

	/**
	 * This method initializes jStrainsFileLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private JLabel getJStrainsFileLabel() {
		if (jStrainsFileLabel == null) {
			jStrainsFileLabel = new JLabel();
			setStrainsFileLabelText();
		}
		return jStrainsFileLabel;
	}

	void setStrainsFileLabelText() {
		String fileName = strainsFile.getAbsolutePath();
		if (fileName.length() > 43) {
			fileName = fileName.substring(0, 20)
					+ "..."
					+ fileName.substring(fileName.length() - 20, fileName
							.length());
		}
		jStrainsFileLabel.setText(fileName);
	}

	/**
	 * This method initializes jStrainsFormatHeaderPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJStrainsFormatHeaderPanel() {
		if (jStrainsFormatHeaderPanel == null) {
			JLabel strainsFormatHeaderLabel = new JLabel();
			strainsFormatHeaderLabel.setText("Strain file format:");
			jStrainsFormatHeaderPanel = new JPanel();
			jStrainsFormatHeaderPanel.add(strainsFormatHeaderLabel);
		}
		return jStrainsFormatHeaderPanel;
	}

	/**
	 * This method initializes jStrainsFormatPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJStrainsFormatPanel() {
		if (jStrainsFormatPanel == null) {
			jStrainsFormatPanel = new JPanel();
			jStrainsFormatPanel.add(getJStrainsFormatXMLButton());
			jStrainsFormatPanel.add(getJStrainsFormatACEButton());
		}
		return jStrainsFormatPanel;
	}

	/**
	 * This method initializes jStrainsFormatACEButton
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJStrainsFormatACEButton() {
		if (jStrainsFormatACECheck == null) {
			jStrainsFormatACECheck = new JCheckBox();
			jStrainsFormatACECheck.setText("ACE");
			jStrainsFormatACECheck.setSelected(true);
		}
		return jStrainsFormatACECheck;
	}

	/**
	 * This method initializes jStrainsFormatXMLButton
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJStrainsFormatXMLButton() {
		if (jStrainsFormatXMLCheck == null) {
			jStrainsFormatXMLCheck = new JCheckBox();
			jStrainsFormatXMLCheck.setText("XML");
			jStrainsFormatXMLCheck.setSelected(true);
		}
		return jStrainsFormatXMLCheck;
	}

}
