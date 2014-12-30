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
import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;

import org.biojava.bio.BioException;

import amd.strainer.GlobalSettings;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.ReferenceSequenceDisplayComponent;
import amd.strainer.display.util.SequenceGrabber;
import amd.strainer.display.util.Util;
import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Read;

/**
 * Allow user to customize the output sequences.
 * 
 * @author jmeppley
 * 
 */
public class GetSequencesDialog extends JDialog {

	private JPanel jContentPane = null;

	private JPanel jFileSelectionPanel = null;
	private JLabel jOutputFileNameLabel = null;
	private JButton jBrowseButton = null;

	private JPanel jButtonPanel = null;
	private JButton jSaveButton = null;
	private JButton jCancelButton = null;

	private JPanel jElementsOptionsPanel = null;
	private JRadioButton jElementsOptionsIndReadsRadioButton = null;
	private JRadioButton jElementsOptionsStrainsRadioButton = null;
	private JRadioButton jElementsOptionsSelectionRadioButton = null;

	private JPanel jScopeOptionsPanel = null;
	private JRadioButton jScopeOptionsVisibleRadioButton = null;
	private JRadioButton jScopeOptionsGeneRadioButton = null;
	private JRadioButton jScopeOptionsSelectionRadioButton = null;

	private JPanel jFillOptionsPanel = null;
	private JRadioButton jFillOptionsNsRadioButton = null;
	private JRadioButton jFillOptionsReferenceRadioButton = null;

	private JPanel jIncludePartialsOptionsPanel = null;
	private JRadioButton jIncPartialOptionsYesRadioButton = null;
	private JRadioButton jIncPartialOptionsNoRadioButton = null;

	private JPanel jFormatOptionsPanel = null;
	private JRadioButton jFormatOptionsFastaRadioButton = null;
	private JRadioButton jFormatOptionsMSARadioButton = null;

	ReferenceSequenceDisplayComponent canvas = null;
	JFileChooser fc = null;
	File outputFile = null;
	static GetSequencesDialog dialog = null;
	boolean status = false;

	final String overwriteQuestion = " exists.  Do you want to overwrite it?";
	final String overwriteTitle = "File exists.";

	private JRadioButton jScopeOptionsAllReadsRadioButton;

	private JRadioButton jLimitToSelectionOptionsNoRadioButton;

	private JRadioButton jLimitToSelectionOptionsYesRadioButton;

	private JRadioButton jDitionaryOptionsAARadioButton;

	private JRadioButton jDitionaryOptionsDNARadioButton;

	/**
	 * opens dialog and creates requested file
	 * 
	 * @return True if file was successfully written
	 */
	public static boolean showDialog(ReferenceSequenceDisplayComponent pCanvas) {
		if (dialog == null) {
			dialog = new GetSequencesDialog(pCanvas);
			dialog.pack();
			try {
				// try to create file chooser
				dialog.fc = new JFileChooser();
			} catch (java.security.AccessControlException ace) {
				amd.strainer.display.util.Util.displayErrorMessage(
						PaneledReferenceSequenceDisplay.frame,
						ace);
				System.err.println("Failed to create file chooser: "
						+ ace.toString());
				return false;
			}
		}

		// reset the status
		dialog.status = false;
		// make sure options reflect current state of the canvas
		dialog.fixSelectionEnabling();

		// display dialog (will return when it's closed)
		dialog.setVisible(true);
		// return the status
		return dialog.status;
	}

	/**
	 * This is the default constructor
	 */
	private GetSequencesDialog(ReferenceSequenceDisplayComponent pCanvas) {
		super(
				JOptionPane
						.getFrameForComponent(PaneledReferenceSequenceDisplay.frame),
				true);
		canvas = pCanvas;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		// this.setSize(500, 300);
		// this.setMinimumSize(new Dimension(400,200));
		this.setTitle("Output Sequences");

		ButtonGroup group = new ButtonGroup();
		group.add(getJElementsOptionsIndReadsRadioButton());
		group.add(getJElementsOptionsSelectionRadioButton());
		group.add(getJElementsOptionsStrainsRadioButton());

		group = new ButtonGroup();
		group.add(getJFillOptionsReferenceRadioButton());
		group.add(getJFillOptionsNsRadioButton());

		group = new ButtonGroup();
		group.add(getJScopeOptionsGeneRadioButton());
		// group.add(getJScopeOptionsAllGenesRadioButton());
		group.add(getJScopeOptionsSelectionRadioButton());
		group.add(getJScopeOptionsVisibleRadioButton());

		group = new ButtonGroup();
		group.add(getJIncPartialOptionsNoRadioButton());
		group.add(getJIncPartialOptionsYesRadioButton());

		group = new ButtonGroup();
		group.add(getJFormatOptionsFastaRadioButton());
		group.add(getJFormatOptionsMSARadioButton());

		group = new ButtonGroup();
		group.add(getJLimitToSelectionOptionsNoRadioButton());
		group.add(getJLimitToSelectionOptionsYesRadioButton());

		group = new ButtonGroup();
		group.add(getJDictionaryOptionsAARadioButton());
		group.add(getJDictionaryOptionsDNARadioButton());

		this.setContentPane(getJContentPane());
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
			jContentPane.add(
					getJFileSelectionPanel(),
					java.awt.BorderLayout.NORTH);
			jContentPane.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(
					getJElementOptionsPanel(),
					java.awt.BorderLayout.WEST);
			jContentPane.add(
					getJRangeOptionsPanel(),
					java.awt.BorderLayout.CENTER);
			jContentPane.add(
					getJOutputOptionsPanel(),
					java.awt.BorderLayout.EAST);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jFileSelectionPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJFileSelectionPanel() {
		if (jFileSelectionPanel == null) {
			jOutputFileNameLabel = new JLabel();
			jOutputFileNameLabel.setText("Select an output file: ");
			jFileSelectionPanel = new JPanel();
			jFileSelectionPanel.add(jOutputFileNameLabel, null);
			jFileSelectionPanel.add(getJBrowseButton(), null);
		}
		return jFileSelectionPanel;
	}

	/**
	 * This method initializes jButtonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJSaveButton(), null);
			jButtonPanel.add(getJCancelButton(), null);
		}
		return jButtonPanel;
	}

	private JPanel getJElementOptionsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(getJElementsOptionsPanel(), null);
		panel.add(getJIncludePartialsOptionsPanel(), null);
		panel.add(getJLimitToSelectionsOptionsPanel(), null);
		return panel;
	}

	private JPanel getJRangeOptionsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(getJScopeOptionsPanel(), null);
		panel.add(getJFillOptionsPanel(), null);
		panel.add(new JPanel(), null);
		return panel;
	}

	private JPanel getJOutputOptionsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(getJDictionaryOptionsPanel(), null);
		panel.add(getJFormatOptionsPanel(), null);
		panel.add(new JPanel(), null);
		return panel;
	}

	private Component getJDictionaryOptionsPanel() {
		JPanel borderPanel = new JPanel();
		borderPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Output Dictionary"), BorderFactory
				.createEmptyBorder(5, 5, 5, 5)));
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(getJDictionaryOptionsDNARadioButton(), null);
		panel.add(getJDictionaryOptionsAARadioButton(), null);
		borderPanel.add(panel, null);
		return borderPanel;
	}

	private Component getJLimitToSelectionsOptionsPanel() {
		JPanel borderPanel = new JPanel();
		borderPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Limit to Selection"), BorderFactory
				.createEmptyBorder(5, 5, 5, 5)));
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(getJLimitToSelectionOptionsYesRadioButton(), null);
		panel.add(getJLimitToSelectionOptionsNoRadioButton(), null);
		borderPanel.add(panel, null);
		return borderPanel;
	}

	/**
	 * This method initializes jElementsOptionsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJElementsOptionsPanel() {
		if (jElementsOptionsPanel == null) {
			jElementsOptionsPanel = new JPanel();
			jElementsOptionsPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Output Elements"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.add(getJElementsOptionsIndReadsRadioButton(), null);
			panel.add(getJElementsOptionsStrainsRadioButton(), null);
			panel.add(getJElementsOptionsSelectionRadioButton(), null);
			jElementsOptionsPanel.add(panel, null);
		}
		return jElementsOptionsPanel;
	}

	/**
	 * This method initializes jScopeOptionsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJScopeOptionsPanel() {
		if (jScopeOptionsPanel == null) {
			jScopeOptionsPanel = new JPanel();
			jScopeOptionsPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Sequence Scope"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.add(getJScopeOptionsVisibleRadioButton(), null);
			panel.add(getJScopeOptionsGeneRadioButton(), null);
			// panel.add(getJScopeOptionsAllGenesRadioButton(), null);
			panel.add(getJScopeOptionsSelectionRadioButton(), null);
			jScopeOptionsPanel.add(panel, null);
		}
		return jScopeOptionsPanel;
	}

	/**
	 * This method initializes jFillOptionsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJFillOptionsPanel() {
		if (jFillOptionsPanel == null) {
			jFillOptionsPanel = new JPanel();
			jFillOptionsPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Fill Unknown With"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.add(getJFillOptionsNsRadioButton(), null);
			panel.add(getJFillOptionsReferenceRadioButton(), null);
			jFillOptionsPanel.add(panel, null);
		}
		return jFillOptionsPanel;
	}

	/**
	 * This method initializes jIncludePartialsOptionsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJIncludePartialsOptionsPanel() {
		if (jIncludePartialsOptionsPanel == null) {
			jIncludePartialsOptionsPanel = new JPanel();
			jIncludePartialsOptionsPanel
					.setBorder(BorderFactory
							.createCompoundBorder(
									BorderFactory
											.createTitledBorder("Include Partial Elements"),
									BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.add(getJIncPartialOptionsYesRadioButton(), null);
			panel.add(getJIncPartialOptionsNoRadioButton(), null);
			jIncludePartialsOptionsPanel.add(panel, null);
		}
		return jIncludePartialsOptionsPanel;
	}

	/**
	 * This method initializes jFormatOptionsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJFormatOptionsPanel() {
		if (jFormatOptionsPanel == null) {
			jFormatOptionsPanel = new JPanel();
			jFormatOptionsPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Output Format"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.add(getJFormatOptionsFastaRadioButton(), null);
			panel.add(getJFormatOptionsMSARadioButton(), null);
			jFormatOptionsPanel.add(panel, null);
		}
		return jFormatOptionsPanel;
	}

	/**
	 * This method initializes jSaveButton
	 * 
	 * @return javax.swing.JButton
	 */
	JButton getJSaveButton() {
		if (jSaveButton == null) {
			Action saveAction = new AbstractAction("Save") {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					status = saveSequencesToFile();
					if (status) {
						// close dialog
						setVisible(false);
					}
				}
			};

			jSaveButton = new JButton(saveAction);
			jSaveButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("ENTER"),
					"ent");
			jSaveButton.getActionMap().put("ent", saveAction);
			jSaveButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke("RETURN"),
					"ret");
			jSaveButton.getActionMap().put("ret", saveAction);

			// in the beginning, we don't have a file to save to
			jSaveButton.setEnabled(false);
		}

		return jSaveButton;
	}

	boolean saveSequencesToFile() {
		// if the file exists, check with user before overwriting
		if (outputFile.exists()) {
			int response = JOptionPane.showOptionDialog(
					PaneledReferenceSequenceDisplay.frame,
					outputFile.getName() + overwriteQuestion,
					overwriteTitle,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					null,
					null);
			if (response != JOptionPane.YES_OPTION) {
				return false;
			}
		}

		// ask user if gaps should be filled with X's or bases from RefSeq
		boolean fillFromConsensus = this
				.getJFillOptionsReferenceRadioButton()
				.isSelected();

		// output consensus of selection, each read, or consensus of each strain
		boolean useSelection = this
				.getJElementsOptionsSelectionRadioButton()
				.isSelected();
		boolean useStrains = this
				.getJElementsOptionsStrainsRadioButton()
				.isSelected();

		// only print selected reads
		boolean onlySelected = this
				.getJLimitToSelectionOptionsYesRadioButton()
				.isSelected();

		// set scope to gene, visible, or all selected
		boolean scopeGene = this.getJScopeOptionsGeneRadioButton().isSelected();
		boolean scopeVisible = this
				.getJScopeOptionsVisibleRadioButton()
				.isSelected();

		// include elements that only partially cover scope
		boolean usePartial = this
				.getJIncPartialOptionsYesRadioButton()
				.isSelected();

		// write to Fasta (true) or multiple sequence alignment (false)
		boolean writeFasta = this
				.getJFormatOptionsFastaRadioButton()
				.isSelected();

		// convert sequence to amino acids?
		int convertToAA = 0;
		if (this.getJDictionaryOptionsAARadioButton().isSelected()) {
			if (canvas.dData.selectedGene.getDirection()) {
				convertToAA = SequenceGrabber.CONVERT_TO_AA_FORWARDS;
			} else {
				convertToAA = SequenceGrabber.CONVERT_TO_AA_BACKWARDS;
			}
		}

		// write to file
		try {

			// define scope of printed seqs
			int start, end;
			if (scopeVisible) {
				// define scope to visible window
				start = canvas.dData.getStart();
				end = canvas.dData.getEnd();
			} else if (scopeGene) {
				// define scope to selected gene
				start = canvas.dData.selectedGene.getStart();
				end = canvas.dData.selectedGene.getEnd();
			} else {
				// define scope to selected reads
				start = canvas.dData.selectedReadList.getStart();
				end = canvas.dData.selectedReadList.getEnd();
			}

			// collect sequences to print
			HashSet<AlignedSequence> sequences = new HashSet<AlignedSequence>();
			if (useSelection) {
				// make sure selection and scope overlap
				// TODO:1 check this and warn user now

				// this call makes sure Strain object is set up
				canvas.dData.selectedReadList.setAlignmentFromReads();

				// this call makes sure Strain object is set up
				canvas.dData.selectedReadList.getAlignment().getDiffs();
				canvas.dData.selectedReadList.setName("SelectedObjectMerged");
				// add coolection of selected reads to list of sequences to be
				// printed
				sequences.add(canvas.dData.selectedReadList);

				// always use sequence if they asked for it
				usePartial = true;

				// // get selected reads singly
				// Iterator<Read> readit =
				// canvas.dData.selectedReadList.getReadIterator();
				// while (readit.hasNext()) {
				// sequences.add(readit.next());
				// }

			} else {
				if (onlySelected) {
					if (useStrains) {
						for (Iterator<Read> it = canvas.dData.selectedReadList
								.getReadIterator(); it.hasNext();) {
							// sequences is a java.util.Set, so it shouldn't
							// take duplicates of strains
							// the following will give the set of strains
							// represented in the selection
							sequences.add(it.next().getStrain());
						}
						sequences.addAll(canvas.dData.referenceSequence.strains
								.values());
					} else {
						for (Iterator<Read> it = canvas.dData.selectedReadList
								.getReadIterator(); it.hasNext();) {
							sequences.add(it.next());
						}
					}
				} else {
					if (useStrains) {
						sequences.addAll(canvas.dData.referenceSequence.strains
								.values());
					} else {
						sequences.addAll(canvas.dData.referenceSequence.reads
								.values());
					}
				}
			}

			// open file
			FileWriter fw = new FileWriter(outputFile, false);
			PrintWriter pw = new PrintWriter(fw);

			// write sequences
			int written = 0;
			if (writeFasta) {
				written = writeFastaSequences(
						pw,
						sequences.iterator(),
						SequenceGrabber.getSequenceGrabber(
								canvas.dData.referenceSequence,
								start,
								end,
								usePartial,
								fillFromConsensus,
								convertToAA));
			} else {
				written = writeMSASequences(
						pw,
						sequences.iterator(),
						SequenceGrabber.getMSAGrabber(
								canvas.dData.referenceSequence,
								sequences.iterator(),
								start,
								end,
								usePartial,
								fillFromConsensus,
								convertToAA));
				// (pw,
				// sequences.iterator,canvas.dData.referenceSequence,start,end,usePartial,fillFromConsensus);
			}

			// close file
			pw.close();
			fw.close();

			if (written == 0) {
				Util
						.displayErrorMessage(
								"No output",
								"The selected options resulted in no sequences being written");
				return false;
			}

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			Util.displayErrorMessage(PaneledReferenceSequenceDisplay.frame, ex
					.toString());
			return false;
		}
	}

	private int writeFastaSequences(PrintWriter pw, Iterator pSeqIt,
			SequenceGrabber pGrabber) {
		// TODO:3 Include %ID to ref in Header
		int count = 0;
		;
		while (pSeqIt.hasNext()) {
			AlignedSequence as = (AlignedSequence) pSeqIt.next();
			String seq;
			try {
				seq = pGrabber.grab(as);
			} catch (BioException e) {
				System.err.println("Error converting sequence, skipping...");
				e.printStackTrace();
				continue;
			}
			if (seq != null) {
				pw.println(">" + as.getName());
				pw.println(seq);
				count++;
			}
		}
		return count;
	}

	private int writeMSASequences(PrintWriter pw, Iterator pSeqIt,
			SequenceGrabber pGrabber) {
		int count = 0;
		while (pSeqIt.hasNext()) {
			AlignedSequence as = (AlignedSequence) pSeqIt.next();
			String seq;
			try {
				seq = pGrabber.grab(as);
			} catch (BioException e) {
				System.err.println("Error converting sequence, skipping...");
				e.printStackTrace();
				continue;
			}
			if (seq != null) {
				count++;

				// start with name
				if (as.getName() == null) {
					as.setName("Sequence " + count);
				}
				pw.print(as.getName());
				// justify with spaces
				for (int i = as.getName().length(); i < 20; i++) {
					pw.print(" ");
				}
				// finish with sequence
				pw.println(seq);
			}
		}
		return count;
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
					status = false;
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
	 * This method initializes jBrowseButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJBrowseButton() {
		if (jBrowseButton == null) {
			jBrowseButton = new JButton();
			jBrowseButton.setText("Browse");
			jBrowseButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							// fc.setSelectedFile(strainsFile);
							// fc.setCurrentDirectory(strainsFile.getParentFile());
							String cwd = GlobalSettings
									.getSetting(GlobalSettings.OUTPUT_DIR_KEY);
							if (cwd != null) {
								fc.setCurrentDirectory(new File(cwd));
							}

							int returnVal = fc
									.showSaveDialog(PaneledReferenceSequenceDisplay.frame);
							if (!(returnVal == JFileChooser.APPROVE_OPTION)) {
								System.out
										.println("Leaving file name unchanged");
							} else {
								outputFile = fc.getSelectedFile();
								setFileLabelText();
								// we can now save the file, now that it's set
								getJSaveButton().setEnabled(true);
								GlobalSettings.putSetting(
										GlobalSettings.OUTPUT_DIR_KEY,
										fc
												.getCurrentDirectory()
												.getAbsolutePath());
							}
						}
					});
		}
		return jBrowseButton;
	}

	void setFileLabelText() {
		String fileName = outputFile.getAbsolutePath();
		if (fileName.length() > 43) {
			fileName = fileName.substring(0, 20)
					+ "..."
					+ fileName.substring(fileName.length() - 20, fileName
							.length());
		}
		jOutputFileNameLabel.setText(fileName);
	}

	/**
	 * This method initializes jElementsOptionsIndReadsRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJElementsOptionsIndReadsRadioButton() {
		if (jElementsOptionsIndReadsRadioButton == null) {
			jElementsOptionsIndReadsRadioButton = new JRadioButton();
			jElementsOptionsIndReadsRadioButton.setText("Individual Reads");
			jElementsOptionsIndReadsRadioButton.setSelected(true);
			// jElementsOptionsIndReadsRadioButton
			// .addChangeListener(new javax.swing.event.ChangeListener() {
			// public void stateChanged(javax.swing.event.ChangeEvent e) {
			// // The following get's run too many times, and
			// // it would b faster to do specific stuff here than check
			// everything
			// // but this is not a bottle neck, so it stays for now.
			// fixSelectionEnabling();
			// }
			// });
		}
		return jElementsOptionsIndReadsRadioButton;
	}

	/**
	 * This method initializes jElementsOptionsStrainsRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJElementsOptionsStrainsRadioButton() {
		if (jElementsOptionsStrainsRadioButton == null) {
			jElementsOptionsStrainsRadioButton = new JRadioButton();
			jElementsOptionsStrainsRadioButton.setText("Strain Groups");
			// jElementsOptionsStrainsRadioButton
			// .addChangeListener(new javax.swing.event.ChangeListener() {
			// public void stateChanged(javax.swing.event.ChangeEvent e) {
			// // The following get's run too many times, and
			// // it would b faster to do specific stuff here than check
			// everything
			// // but this is not a bottle neck, so it stays for now.
			// fixSelectionEnabling();
			// }
			// });
		}
		return jElementsOptionsStrainsRadioButton;
	}

	/**
	 * This method initializes jElementsOptionsSelectionRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJElementsOptionsSelectionRadioButton() {
		if (jElementsOptionsSelectionRadioButton == null) {
			jElementsOptionsSelectionRadioButton = new JRadioButton();
			jElementsOptionsSelectionRadioButton.setText("Selection Consensus");
			jElementsOptionsSelectionRadioButton
					.addChangeListener(new javax.swing.event.ChangeListener() {
						public void stateChanged(javax.swing.event.ChangeEvent e) {
							if (canvas.dData.selectedReadList.getSize() > 0) {
								// is the selection the chosen element
								if (getJElementsOptionsSelectionRadioButton()
										.isSelected()) {
									// disable the incomplete options since they
									// are meaningless
									getJIncPartialOptionsNoRadioButton()
											.setEnabled(false);
									getJIncPartialOptionsYesRadioButton()
											.setEnabled(false);
									getJLimitToSelectionOptionsYesRadioButton()
											.setEnabled(false);
									getJLimitToSelectionOptionsNoRadioButton()
											.setEnabled(false);
								} else {
									// make sure the incomplete options are
									// enabled
									getJIncPartialOptionsNoRadioButton()
											.setEnabled(true);
									getJIncPartialOptionsYesRadioButton()
											.setEnabled(true);
									getJLimitToSelectionOptionsYesRadioButton()
											.setEnabled(true);
									getJLimitToSelectionOptionsNoRadioButton()
											.setEnabled(true);
								}
							}
						}
					});
		}
		return jElementsOptionsSelectionRadioButton;
	}

	/**
	 * This method initializes jScopeOptionsSelectionRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJScopeOptionsSelectionRadioButton() {
		if (jScopeOptionsSelectionRadioButton == null) {
			jScopeOptionsSelectionRadioButton = new JRadioButton();
			jScopeOptionsSelectionRadioButton.setText("Selection Span");
			// jScopeOptionsSelectionRadioButton
			// .addChangeListener(new javax.swing.event.ChangeListener() {
			// public void stateChanged(javax.swing.event.ChangeEvent e) {
			// // The following get's run too many times, and
			// // it would b faster to do specific stuff here than check
			// everything
			// // but this is not a bottle neck, so it stays for now.
			// fixSelectionEnabling();
			// }
			// });
		}
		return jScopeOptionsSelectionRadioButton;
	}

	/**
	 * This method initializes jScopeOptionsVisibleRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJScopeOptionsVisibleRadioButton() {
		if (jScopeOptionsVisibleRadioButton == null) {
			jScopeOptionsVisibleRadioButton = new JRadioButton();
			jScopeOptionsVisibleRadioButton.setText("Visible Region");
			jScopeOptionsVisibleRadioButton.setSelected(true);
			// jScopeOptionsVisibleRadioButton
			// .addChangeListener(new javax.swing.event.ChangeListener() {
			// public void stateChanged(javax.swing.event.ChangeEvent e) {
			// // The following get's run too many times, and
			// // it would b faster to do specific stuff here than check
			// everything
			// // but this is not a bottle neck, so it stays for now.
			// fixSelectionEnabling();
			// }
			// });
		}
		return jScopeOptionsVisibleRadioButton;
	}

	/**
	 * This method initializes jScopeOptionsGeneRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJScopeOptionsGeneRadioButton() {
		if (jScopeOptionsGeneRadioButton == null) {
			jScopeOptionsGeneRadioButton = new JRadioButton();
			jScopeOptionsGeneRadioButton.setText("Gene Span");
			jScopeOptionsGeneRadioButton
					.addChangeListener(new javax.swing.event.ChangeListener() {
						public void stateChanged(javax.swing.event.ChangeEvent e) {
							// The following get's run too many times, and
							// it would b faster to do specific stuff here than
							// check everything
							// but this is not a bottle neck, so it stays for
							// now.
							if (getJScopeOptionsAllGenesRadioButton()
									.isSelected()
									|| getJScopeOptionsGeneRadioButton()
											.isSelected()) {
								getJDictionaryOptionsAARadioButton()
										.setEnabled(true);
								getJDictionaryOptionsDNARadioButton()
										.setEnabled(true);
							} else {
								getJDictionaryOptionsAARadioButton()
										.setEnabled(false);
								getJDictionaryOptionsDNARadioButton()
										.setEnabled(false);
							}
						}
					});
		}
		return jScopeOptionsGeneRadioButton;
	}

	/**
	 * This method initializes jIncPartialOptionsNoRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJIncPartialOptionsNoRadioButton() {
		if (jIncPartialOptionsNoRadioButton == null) {
			jIncPartialOptionsNoRadioButton = new JRadioButton();
			jIncPartialOptionsNoRadioButton.setText("No");
			jIncPartialOptionsNoRadioButton.setSelected(true);

		}
		return jIncPartialOptionsNoRadioButton;
	}

	/**
	 * This method initializes jIncPartialOptionsYesRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJIncPartialOptionsYesRadioButton() {
		if (jIncPartialOptionsYesRadioButton == null) {
			jIncPartialOptionsYesRadioButton = new JRadioButton();
			jIncPartialOptionsYesRadioButton.setText("Yes");
		}
		return jIncPartialOptionsYesRadioButton;
	}

	/**
	 * This method initializes jFormatOptionsFastaRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJFormatOptionsFastaRadioButton() {
		if (jFormatOptionsFastaRadioButton == null) {
			jFormatOptionsFastaRadioButton = new JRadioButton();
			jFormatOptionsFastaRadioButton.setText("Fasta");
			jFormatOptionsFastaRadioButton.setSelected(true);

		}
		return jFormatOptionsFastaRadioButton;
	}

	/**
	 * This method initializes jFormatOptionsMSARadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJFormatOptionsMSARadioButton() {
		if (jFormatOptionsMSARadioButton == null) {
			jFormatOptionsMSARadioButton = new JRadioButton();
			jFormatOptionsMSARadioButton.setText("MSA");
		}
		return jFormatOptionsMSARadioButton;
	}

	/**
	 * This method initializes jFillOptionsReferenceRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJFillOptionsReferenceRadioButton() {
		if (jFillOptionsReferenceRadioButton == null) {
			jFillOptionsReferenceRadioButton = new JRadioButton();
			jFillOptionsReferenceRadioButton.setText("Reference Bases");
			jFillOptionsReferenceRadioButton.setSelected(true);
		}
		return jFillOptionsReferenceRadioButton;
	}

	/**
	 * This method initializes jFillOptionsXsRadioButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJFillOptionsNsRadioButton() {
		if (jFillOptionsNsRadioButton == null) {
			jFillOptionsNsRadioButton = new JRadioButton();
			jFillOptionsNsRadioButton.setText("N's");
		}
		return jFillOptionsNsRadioButton;
	}

	private JRadioButton getJScopeOptionsAllGenesRadioButton() {
		if (jScopeOptionsAllReadsRadioButton == null) {
			jScopeOptionsAllReadsRadioButton = new JRadioButton();
			jScopeOptionsAllReadsRadioButton.setText("All Genes");
			jScopeOptionsGeneRadioButton
					.addChangeListener(new javax.swing.event.ChangeListener() {
						public void stateChanged(javax.swing.event.ChangeEvent e) {
							// The following get's run too many times, and
							// it would b faster to do specific stuff here than
							// check everything
							// but this is not a bottle neck, so it stays for
							// now.
							if (getJScopeOptionsAllGenesRadioButton()
									.isSelected()
									|| getJScopeOptionsGeneRadioButton()
											.isSelected()) {
								getJDictionaryOptionsAARadioButton()
										.setEnabled(true);
								getJDictionaryOptionsDNARadioButton()
										.setEnabled(true);
							} else {
								getJDictionaryOptionsAARadioButton()
										.setEnabled(false);
								getJDictionaryOptionsDNARadioButton()
										.setEnabled(false);
							}
						}
					});
		}
		return jScopeOptionsAllReadsRadioButton;
	}

	private JRadioButton getJLimitToSelectionOptionsNoRadioButton() {
		if (jLimitToSelectionOptionsNoRadioButton == null) {
			jLimitToSelectionOptionsNoRadioButton = new JRadioButton();
			jLimitToSelectionOptionsNoRadioButton.setText("All Reads");
		}
		return jLimitToSelectionOptionsNoRadioButton;
	}

	private JRadioButton getJLimitToSelectionOptionsYesRadioButton() {
		if (jLimitToSelectionOptionsYesRadioButton == null) {
			jLimitToSelectionOptionsYesRadioButton = new JRadioButton();
			jLimitToSelectionOptionsYesRadioButton.setText("Just Selected");
		}
		return jLimitToSelectionOptionsYesRadioButton;
	}

	private JRadioButton getJDictionaryOptionsAARadioButton() {
		if (jDitionaryOptionsAARadioButton == null) {
			jDitionaryOptionsAARadioButton = new JRadioButton();
			jDitionaryOptionsAARadioButton.setText("Amino Acids");
		}
		return jDitionaryOptionsAARadioButton;
	}

	private JRadioButton getJDictionaryOptionsDNARadioButton() {
		if (jDitionaryOptionsDNARadioButton == null) {
			jDitionaryOptionsDNARadioButton = new JRadioButton();
			jDitionaryOptionsDNARadioButton.setText("Nucleotides");
		}
		return jDitionaryOptionsDNARadioButton;
	}

	// makes sure radio buttons are properly enabled based on what's currently
	// possible
	void fixSelectionEnabling() {
		// Are there reads selected?
		if (canvas.dData.selectedReadList.getSize() > 0) {
			// make sure options are enabled
			getJElementsOptionsSelectionRadioButton().setEnabled(true);
			getJLimitToSelectionOptionsNoRadioButton().setEnabled(true);
			getJLimitToSelectionOptionsYesRadioButton().setEnabled(true);

			// make sure the selection scope option is enabled
			getJScopeOptionsSelectionRadioButton().setEnabled(true);

			// is the selection the chosen element
			if (getJElementsOptionsSelectionRadioButton().isSelected()) {
				// disable the incomplete options since they are meaningless
				getJIncPartialOptionsNoRadioButton().setEnabled(false);
				getJIncPartialOptionsYesRadioButton().setEnabled(false);
			} else {
				// make sure the incomplete options are enabled
				getJIncPartialOptionsNoRadioButton().setEnabled(true);
				getJIncPartialOptionsYesRadioButton().setEnabled(true);
			}
		} else {
			// make sure options are disabled and NOT selected
			getJScopeOptionsSelectionRadioButton().setEnabled(false);
			if (getJScopeOptionsSelectionRadioButton().isSelected()) {
				getJScopeOptionsVisibleRadioButton().setSelected(true);
			}
			getJElementsOptionsSelectionRadioButton().setEnabled(false);
			if (getJElementsOptionsSelectionRadioButton().isSelected()) {
				getJElementsOptionsStrainsRadioButton().setSelected(true);
			}
			getJLimitToSelectionOptionsNoRadioButton().setEnabled(false);
			getJLimitToSelectionOptionsYesRadioButton().setEnabled(false);
		}

		// is there a gene selected?
		if (canvas.dData.selectedGene != null) {
			// make sure option is enabled
			getJScopeOptionsGeneRadioButton().setEnabled(true);
		} else {
			// make sure option is disabled and NOT selected
			getJScopeOptionsGeneRadioButton().setEnabled(false);
			if (getJScopeOptionsGeneRadioButton().isSelected()) {
				getJScopeOptionsVisibleRadioButton().setSelected(true);
			}
		}

	}
}
