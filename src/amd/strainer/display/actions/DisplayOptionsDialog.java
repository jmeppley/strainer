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
 ***** END LICENSE BLOCK ***** */package amd.strainer.display.actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

import amd.strainer.display.DisplayGeometry;
import amd.strainer.display.DisplaySettings;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.util.Util;

/**
 * Creates and displays dialog to change display options including the way reads and strains are stakced, sorted, and colored.
 * <p>
 * @author jmeppley
 * @see amd.strainer.display.util.Stacker
 * @see amd.strainer.display.DisplayGeometry#fillInColors(Graphics2D, Color)
 */
public class DisplayOptionsDialog extends JDialog {
	private JPanel 		jContentPane = null;

	private JPanel 		 jTitlePanel = null;
	
	private JTabbedPane   jTabbedPane = null;
	private JPanel 		 jReadsGridPanel = null;
	private JPanel 		 jStrainsGridPanel = null;
	private JLabel 		  jSortingLabel = null;
	private JComboBox 	  jReadSortingBox = null;
	private JComboBox 	  jStrainSortingBox = null;
	private JLabel 	      jStackingLabel = null;
	private JComboBox 	  jReadStackingBox = null;
	private JComboBox 	  jStrainStackingBox = null;
	private JLabel 	 	  jColorsLabel = null;
	
	private JPanel 		  jReadColorsPanel = null;
	private JPanel 		   jReadConstantCheckPanel = null;
	private JCheckBox   	    jReadConstantCheckBox = null;
	private JPanel 		   jReadConstantSettingsPanel = null;
	private JLabel 	        jReadConstantLabel = null;
	private JButton 		    jReadConstantButton = null;
//	private JPanel 		   jReadRandomCheckPanel = null;
//	private JCheckBox   	    jReadRandomCheckBox = null;
//	private JPanel 		   jReadRandomSettingsPanel = null;
//	private JLabel 	        jReadRandomLabel = null;
	private JPanel 		   jReadTintCheckPanel = null;
	private JCheckBox 	    jReadTintCheckBox = null;
	private JPanel 		   jReadTintSettingsPanel = null;
	private JPanel 		    jReadTintLowCutoffPanel = null;
	private JLabel 		     jReadTintLowCutoffLabel = null;
	private JTextField 	     jReadTintLowCutoffTextField = null;
	private JPanel 	    	    jReadTintColorsPanel = null;
	private JButton			 jReadTintLowColorButton = null;
	private JButton 			 jReadTintHighColorButton = null;
	private JPanel 		   jReadTwoToneCheckPanel = null;
	private JCheckBox 	    jReadTwoToneCheckBox = null;
	private JPanel 		   jReadTwoToneSettingsPanel = null;
	private JButton 			jReadLowColorButton = null;
	private JLabel 			jReadTwoToneSettingsLabel = null;
	private JButton 			jReadHighColorButton = null;
	private JPanel			jReadThresholdPanel = null;
	private JLabel 			 jReadThresholdLabel = null;
	private JTextField 		 jReadThresholdTextField = null;

	private JPanel 		  jStrainColorsPanel = null;
	private JPanel 		   jStrainConstantCheckPanel = null;
	private JCheckBox   	    jStrainConstantCheckBox = null;
	private JPanel 		   jStrainConstantSettingsPanel = null;
	private JLabel 	        jStrainConstantLabel = null;
	private JButton 		    jStrainConstantButton = null;
	private JPanel 		   jStrainRandomCheckPanel = null;
	private JCheckBox   	    jStrainRandomCheckBox = null;
	private JPanel 		   jStrainRandomSettingsPanel = null;
	private JLabel 	        jStrainRandomLabel = null;
	private JPanel 		   jStrainTintCheckPanel = null;
	private JCheckBox 	    jStrainTintCheckBox = null;
	private JPanel 		   jStrainTintSettingsPanel = null;
	private JPanel 		    jStrainTintLowCutoffPanel = null;
	private JLabel 		     jStrainTintLowCutoffLabel = null;
	private JTextField 	     jStrainTintLowCutoffTextField = null;
	private JPanel 	    	    jStrainColorChoicePanel = null;
	private JButton			 jStrainTintLowColorButton = null;
	private JButton 			 jStrainTintHighColorButton = null;
	private JPanel 		   jStrainTwoToneCheckPanel = null;
	private JCheckBox 	    jStrainTwoToneCheckBox = null;
	private JPanel 		   jStrainTwoToneSettingsPanel = null;
	private JLabel 			jStrainTwoToneLabel = null;
	private JButton 			jStrainLowColorButton = null;
	private JButton 			jStrainHighColorButton = null;
	private JPanel			jStrainThresholdPanel = null;
	private JLabel 			 jStrainThresholdLabel = null;
	private JTextField 		 jStrainThresholdTextField = null;

	private JPanel     jOtherOptionsPanel = null;
//	private JPanel jShowNsPanel = null;
//	private JCheckBox jShowNsCheckBox = null;
	private JPanel jRecombColorsPanel = null;
	private JPanel   jRecombColorPanel = null;
	private JLabel      jRecombColorLabel = null;
	private JButton 	jRecombColorButton = null;
	private JPanel      jShowRecombColorPanel = null;
	private JPanel   jSelectedRecombColorPanel = null;
	private JLabel      jSelectedRecombColorLabel = null;
	private JButton 	jRecombSelectColorButton = null;
	private JPanel      jShowSelectedColorPanel = null;
	private JPanel jMiscOptionsPanel = null;
	private JCheckBox jDrawAllLettersCheckBox = null;
	
	private JPanel 		 jButtonPanel = null;
	private JButton 	  	  jRevertButton = null;
	private JButton 		  jCancelButton = null;
	private JButton 		  jSaveButton = null;
	
	Color strainConstantColor;
	Color strainHighColor;
	Color strainLowColor;
	Color readConstantColor;
	Color readHighColor;
	Color readLowColor;
	Color recombColor;
	Color recombSelectColor;
	
	boolean status = false;
	private static DisplayOptionsDialog dialog = null;
	private DisplaySettings settings = DisplaySettings.getDisplaySettings();

	Color readTintLowColor;
	Color readTintHighColor;
	Color strainTintLowColor;
	Color strainTintHighColor;

	/**
	 * Displays the dialog
	 * @return TRUE if users clicks Save
	 */
	public static boolean showDialog() {
		if (dialog==null) {
			dialog = new DisplayOptionsDialog();
		}
		dialog.pack();
		dialog.setVisible(true);
		return dialog.status;
	}
	
	/**
	 * This is the default constructor
	 */
	private DisplayOptionsDialog() {
		super(JOptionPane.getFrameForComponent(PaneledReferenceSequenceDisplay.frame),true);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());

		// get dropdown options
		for (int i=0;i<settings.sortingOptions.length;i++) {
			getJReadSortingBox().addItem(settings.sortingOptions[i]);
			getJStrainSortingBox().addItem(settings.sortingOptions[i]);
		}
		
		for (int i=0; i< settings.readStackingOptions.length;i++) {
			getJReadStackingBox().addItem(settings.readStackingOptions[i]);
		}
		
		for (int i=0; i<settings.strainStackingOptions.length;i++) {
			getJStrainStackingBox().addItem(settings.strainStackingOptions[i]);
		}
		
		ButtonGroup group = new ButtonGroup();
		group.add(getJReadConstantCheckBox());
//		group.add(getJReadRandomCheckBox());
		group.add(getJReadTintCheckBox());
		group.add(getJReadTwoToneCheckBox());
		
		group = new ButtonGroup();
		group.add(getJStrainConstantCheckBox());
		group.add(getJStrainRandomCheckBox());
		group.add(getJStrainTintCheckBox());
		group.add(getJStrainTwoToneCheckBox());

		// fill fields from current settings
		revertSettings();
		
		// set size of window
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
			jContentPane.add(getJTitlePanel(), java.awt.BorderLayout.NORTH);
			jContentPane.add(getJTabbedPane(), java.awt.BorderLayout.CENTER);
			jContentPane.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jTitlePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJTitlePanel() {
		if (jTitlePanel == null) {
			jTitlePanel = new JPanel();
		}
		return jTitlePanel;
	}

	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("Read display", null, getJReadsGridPanel(), null);
			jTabbedPane.addTab("Strain display", null, getJStrainsGridPanel(), null);

			// add wrapper panel to allow space below elements
			JPanel tab3panel = new JPanel();
			tab3panel.add(getJOtherOptionsPanel());
			tab3panel.add(new JPanel());
			jTabbedPane.addTab("Other options", null, tab3panel, null);
		}
		return jTabbedPane;
	}
	
	/**
	 * This method initializes jReadsGridPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJReadsGridPanel() {
		if (jReadsGridPanel == null) {
			jColorsLabel = new JLabel();
			jColorsLabel.setText("Coloring");
			jStackingLabel = new JLabel();
			jStackingLabel.setText("Stacking");
			jSortingLabel = new JLabel();
			jSortingLabel.setText("Sorting");
			SpringLayout layout = new SpringLayout();
			jReadsGridPanel = new JPanel();
			jReadsGridPanel.setLayout(layout);
			jReadsGridPanel.add(jSortingLabel, null);
			jReadsGridPanel.add(getJReadSortingBox(), null);
			jReadsGridPanel.add(jStackingLabel, null);
			jReadsGridPanel.add(getJReadStackingBox(), null);
			jReadsGridPanel.add(jColorsLabel, null);
			jReadsGridPanel.add(getJReadColorsPanel(), null);

			// set up spring layout constraints
			
			int rows = 3;
			int cols = 2;
			
			int initialX = 5;
			int initialY = 5;
			int xPad  = 5;
			int yPad = 5;
			
			Util.makeCompactGrid(jReadsGridPanel,layout,rows,cols,initialX,initialY,xPad,yPad);
			 
		}		
		return jReadsGridPanel;
	}
	
	/**
	 * This method initializes jStrainsGridPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJStrainsGridPanel() {
		if (jStrainsGridPanel == null) {
			jColorsLabel = new JLabel();
			jColorsLabel.setText("Coloring");
			jStackingLabel = new JLabel();
			jStackingLabel.setText("Stacking");
			jSortingLabel = new JLabel();
			jSortingLabel.setText("Sorting");
			SpringLayout layout = new SpringLayout();
			jStrainsGridPanel = new JPanel();
			jStrainsGridPanel.setLayout(layout);
			jStrainsGridPanel.add(jSortingLabel, null);
			jStrainsGridPanel.add(getJStrainSortingBox(), null);
			jStrainsGridPanel.add(jStackingLabel, null);
			jStrainsGridPanel.add(getJStrainStackingBox(), null);
			jStrainsGridPanel.add(jColorsLabel, null);
			jStrainsGridPanel.add(getJStrainColorsPanel(), null);

			// set up spring layout constraints
			
			int rows = 3;
			int cols = 2;
			
			int initialX = 5;
			int initialY = 5;
			int xPad  = 5;
			int yPad = 5;
			
			Util.makeCompactGrid(jStrainsGridPanel,layout,rows,cols,initialX,initialY,xPad,yPad);
			 
		}		
		return jStrainsGridPanel;
	}

	private JPanel getJOtherOptionsPanel() {
		if (jOtherOptionsPanel == null) {
			jOtherOptionsPanel = new JPanel();
			jOtherOptionsPanel.setLayout(new BoxLayout(jOtherOptionsPanel, BoxLayout.PAGE_AXIS));

//			jOtherOptionsPanel.add(getJShowNsPanel(), null);
			jOtherOptionsPanel.add(getJRecombColorsPanel(), null);
			jOtherOptionsPanel.add(getJMiscOptionsPanel(), null);
			
		}
		return jOtherOptionsPanel;
	}
	
//	/**
//	 * This method initializes jShowNsPanel	
//	 * 	
//	 * @return javax.swing.JPanel	
//	 */
//	private JPanel getJShowNsPanel() {
//		if (jShowNsPanel == null) {
//			jShowNsPanel = new JPanel();
//			jShowNsPanel.setBorder(BorderFactory.createCompoundBorder(
//                    BorderFactory.createTitledBorder("Quality Data Settings"),
//                    BorderFactory.createEmptyBorder(5,5,5,5)));
//			jShowNsPanel.add(getJShowNsCheckBox(), null);
//		}
//		return jShowNsPanel;
//	}
//
//	/**
//	 * This method initializes jShowNsCheckBox	
//	 * 	
//	 * @return javax.swing.JCheckBox	
//	 */
//	private JCheckBox getJShowNsCheckBox() {
//		if (jShowNsCheckBox == null) {
//			jShowNsCheckBox = new JCheckBox();
//			jShowNsCheckBox.setText("Show Low Quality Diffs");
//		}
//		return jShowNsCheckBox;
//	}

	/**
	 * This method initializes jRecombColorPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJRecombColorPanel() {
		if (jRecombColorPanel == null) {
			jRecombColorLabel = new JLabel();
			jRecombColorLabel.setText("Outline Color:");
			jRecombColorPanel = new JPanel();
			jRecombColorPanel.add(jRecombColorLabel, null);
			jRecombColorPanel.add(getJRecombColorButton(), null);
			jRecombColorPanel.add(getJShowColorPanel(), null);
		}
		return jRecombColorPanel;
	}

	/**
	 * This method initializes jSelectedRecombColorPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJSelectedRecombColorPanel() {
		if (jSelectedRecombColorPanel == null) {
			jSelectedRecombColorLabel = new JLabel();
			jSelectedRecombColorLabel.setText("Selected Color:");
			jSelectedRecombColorPanel = new JPanel();
			jSelectedRecombColorPanel.add(jSelectedRecombColorLabel, null);
			jSelectedRecombColorPanel.add(getJRecombSelectColorButton(), null);
			jSelectedRecombColorPanel.add(getJShowSelectedColorPanel(), null);
		}
		return jSelectedRecombColorPanel;
	}

	/**
	 * This method initializes jRecombColorsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJRecombColorsPanel() {
		if (jRecombColorsPanel == null) {
			jRecombColorsPanel = new JPanel();
			jRecombColorsPanel.setLayout(new BoxLayout(jRecombColorsPanel, BoxLayout.PAGE_AXIS));

			jRecombColorsPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Recombinant Read Colors"),
                    BorderFactory.createEmptyBorder(5,5,5,5)));

			jRecombColorsPanel.add(getJRecombColorPanel(), null);
			jRecombColorsPanel.add(getJSelectedRecombColorPanel(), null);
		}
		return jRecombColorsPanel;
	}

	/**
	 * This method initializes jShowRecombColorPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJShowColorPanel() {
		if (jShowRecombColorPanel == null) {
			jShowRecombColorPanel = new JPanel();
		}
		return jShowRecombColorPanel;
	}

	/**
	 * This method initializes jShowSelectedColorPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJShowSelectedColorPanel() {
		if (jShowSelectedColorPanel == null) {
			jShowSelectedColorPanel = new JPanel();
		}
		return jShowSelectedColorPanel;
	}

	
	/**
	 * This method initializes jMiscOptionsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJMiscOptionsPanel() {
		if (jMiscOptionsPanel == null) {
			jMiscOptionsPanel = new JPanel();
			jMiscOptionsPanel.setLayout(new BoxLayout(jMiscOptionsPanel, BoxLayout.PAGE_AXIS));

			jMiscOptionsPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Other Options"),
                    BorderFactory.createEmptyBorder(5,5,5,5)));

			jMiscOptionsPanel.add(getJDrawAllLettersCheckBox(), null);
		}
		return jMiscOptionsPanel;
	}

	/**
	 * This method initializes jDrawAllLettersCheckBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JCheckBox getJDrawAllLettersCheckBox() {
		if (jDrawAllLettersCheckBox == null) {
			jDrawAllLettersCheckBox = new JCheckBox("Draw all letters when zoommed");
		}
		return jDrawAllLettersCheckBox;
	}
	
	/**
	 * This method initializes jReadSortingBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJReadSortingBox() {
		if (jReadSortingBox == null) {
			jReadSortingBox = new JComboBox();
		}
		return jReadSortingBox;
	}

	/**
	 * This method initializes jStrainSortingBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJStrainSortingBox() {
		if (jStrainSortingBox == null) {
			jStrainSortingBox = new JComboBox();
		}
		return jStrainSortingBox;
	}

	/**
	 * This method initializes jReadStackingBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJReadStackingBox() {
		if (jReadStackingBox == null) {
			jReadStackingBox = new JComboBox();
		}
		return jReadStackingBox;
	}

	/**
	 * This method initializes jStrainStackingBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJStrainStackingBox() {
		if (jStrainStackingBox == null) {
			jStrainStackingBox = new JComboBox();
		}
		return jStrainStackingBox;
	}

	/**
	 * This method initializes jReadColorsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJReadColorsPanel() {
		if (jReadColorsPanel == null) {
			jReadColorsPanel = new JPanel();
			jReadColorsPanel.setBorder(BorderFactory.createRaisedBevelBorder());
			SpringLayout layout = new SpringLayout();
			jReadColorsPanel.setLayout(layout);
			jReadColorsPanel.add(getJReadConstantCheckPanel(), null);
			jReadColorsPanel.add(getJReadConstantSettingsPanel(), null);
//			jReadColorsPanel.add(getJReadRandomCheckPanel(), null);
//			jReadColorsPanel.add(getJReadRandomSettingsPanel(), null);
			jReadColorsPanel.add(getJReadTintCheckPanel(), null);
			jReadColorsPanel.add(getJReadTintSettingsPanel(), null);
			jReadColorsPanel.add(getJReadTwoToneCheckPanel(), null);
			jReadColorsPanel.add(getJReadTwoToneSettingsPanel(), null);
		
			 Util.makeCompactGrid(jReadColorsPanel,layout,3,2,5,5,5,5);
		}
		return jReadColorsPanel;
	}

	/**
	 * This method initializes jStrainColorsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJStrainColorsPanel() {
		if (jStrainColorsPanel == null) {
			jStrainColorsPanel = new JPanel();
			SpringLayout layout = new SpringLayout();
			jStrainColorsPanel.setLayout(layout);
			jStrainColorsPanel.setBorder(BorderFactory.createRaisedBevelBorder());
			jStrainColorsPanel.add(getJStrainConstantCheckPanel(), null);
			jStrainColorsPanel.add(getJStrainConstantSettingsPanel(), null);
			jStrainColorsPanel.add(getJStrainRandomCheckPanel(), null);
			jStrainColorsPanel.add(getJStrainRandomSettingsPanel(), null);
			jStrainColorsPanel.add(getJStrainTintCheckPanel(), null);
			jStrainColorsPanel.add(getJStrainTintSettingsPanel(), null);
			jStrainColorsPanel.add(getJStrainTwoToneCheckPanel(), null);
			jStrainColorsPanel.add(getJStrainTwoToneSettingsPanel(), null);
		
			 Util.makeCompactGrid(jStrainColorsPanel,layout,4,2,5,5,5,5);
		}
		return jStrainColorsPanel;
	}

	/**
	 * This method initializes jButtonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJRevertButton(), null);
			jButtonPanel.add(getJCancelButton(), null);
			jButtonPanel.add(getJSaveButton(), null);
		}
		return jButtonPanel;
	}

	/**
	 * This method initializes jRevertButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJRevertButton() {
		if (jRevertButton == null) {
			jRevertButton = new JButton("Revert");
			jRevertButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					revertSettings();
				}
			});
		}
		return jRevertButton;
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
			jCancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"),"esc");
			jCancelButton.getActionMap().put("esc",cancelAction);

		}
		return jCancelButton;
	}

	/**
	 * This method initializes jSaveButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJSaveButton() {
		if (jSaveButton == null) {
			Action saveAction = new AbstractAction("Save") {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					saveSettings();
					status = true;
					setVisible(false);
				}
			};

			jSaveButton = new JButton(saveAction);
			jSaveButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"),"ent");
			jSaveButton.getActionMap().put("ent",saveAction);
			jSaveButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RETURN"),"ret");
			jSaveButton.getActionMap().put("ret",saveAction);
		}
		return jSaveButton;
	}
	
	void revertSettings() {
		getJReadSortingBox().setSelectedItem(settings.getReadSortingObject());
		getJStrainSortingBox().setSelectedItem(settings.getStrainSortingObject());
		getJReadStackingBox().setSelectedItem(settings.getReadStackingObject());
		getJStrainStackingBox().setSelectedItem(settings.getStrainStackingObject());
		
		getJReadConstantCheckBox().setSelected(true);
		getJReadTintCheckBox().setSelected(true);
		getJReadTwoToneCheckBox().setSelected(true);
		getJReadConstantCheckBox().setSelected(settings.getReadColorStyle()==DisplaySettings.COLOR_CONSTANT);
//		getJReadRandomCheckBox().setSelected(settings.getReadColorStyle()==DisplaySettings.COLOR_RANDOM);
		getJReadTintCheckBox().setSelected(settings.getReadColorStyle()==DisplaySettings.COLOR_TINT);
		getJReadTwoToneCheckBox().setSelected(settings.getReadColorStyle()==DisplaySettings.COLOR_TWO_TONE);

		readConstantColor = settings.getReadConstantColor();

		getJReadTintLowCutoffTextField().setText(String.valueOf(settings.getReadLowCutoffValue()));
		readTintLowColor = settings.getReadTintLowColor(); 
		readTintHighColor = settings.getReadTintHighColor();

		readHighColor = settings.getReadHighColor();
		readLowColor = settings.getReadLowColor();
		getJReadThresholdTextField().setText(String.valueOf(settings.getReadColorThreshold()));

		getJStrainConstantCheckBox().setSelected(true);
		getJStrainTintCheckBox().setSelected(true);
		getJStrainTwoToneCheckBox().setSelected(true);
		getJStrainConstantCheckBox().setSelected(settings.getStrainColorStyle()==DisplaySettings.COLOR_CONSTANT);
		getJStrainRandomCheckBox().setSelected(settings.getStrainColorStyle()==DisplaySettings.COLOR_RANDOM);
		getJStrainTintCheckBox().setSelected(settings.getStrainColorStyle()==DisplaySettings.COLOR_TINT);
		getJStrainTwoToneCheckBox().setSelected(settings.getStrainColorStyle()==DisplaySettings.COLOR_TWO_TONE);

		strainConstantColor = settings.getStrainConstantColor();
		
		getJStrainTintLowCutoffTextField().setText(String.valueOf(settings.getStrainTintLowCutoffValue()));
		strainTintLowColor = settings.getStrainTintLowColor();
		strainTintHighColor = settings.getStrainTintHighColor();

		strainHighColor = settings.getStrainHighColor();
		strainLowColor = settings.getStrainLowColor();
		getJStrainThresholdTextField().setText(String.valueOf(settings.getStrainColorThreshold()));

		setRecombColor(settings.getRecombinantColor());
		setRecombSelectColor(settings.getRecombinantSelectColor());

		getJDrawAllLettersCheckBox().setSelected(settings.isDrawAllLetters());
//		getJShowNsCheckBox().setSelected(settings.getShowNs());
	}
	
	void setRecombColor(Color pColor) {
		recombColor = pColor;
		getJShowColorPanel().setBackground(pColor);
	}
	
	void setRecombSelectColor(Color pColor) {
		recombSelectColor = pColor;
		getJShowSelectedColorPanel().setBackground(pColor);
	}

	void saveSettings() throws NumberFormatException {
		String field = "read tint value";
		int rtv,stv,rct,sct;
		try {
			rtv = Integer.parseInt(getJReadTintLowCutoffTextField().getText());
			field = "strain tint value";
			stv = Integer.parseInt(getJStrainTintLowCutoffTextField().getText());
			field = "read color threshold";
			rct = Integer.parseInt(getJReadThresholdTextField().getText());
			field = "strain color threshold";
			sct = Integer.parseInt(getJStrainThresholdTextField().getText());
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Could not parse text in: " + field);
		}
		
		settings.setReadSorting(getJReadSortingBox().getSelectedItem());
		settings.setStrainSorting(getJStrainSortingBox().getSelectedItem());
		settings.setReadStacking(getJReadStackingBox().getSelectedItem());
		settings.setStrainStacking(getJStrainStackingBox().getSelectedItem());

		if (getJReadConstantCheckBox().isSelected()) {
			settings.setReadColorStyle(DisplaySettings.COLOR_CONSTANT);
//		} else if (getJReadRandomCheckBox().isSelected()) {
//			settings.setReadColorStyle(DisplaySettings.COLOR_RANDOM);
		} else  if (getJReadTintCheckBox().isSelected()) {
			settings.setReadColorStyle(DisplaySettings.COLOR_TINT);
		} else {
			settings.setReadColorStyle(DisplaySettings.COLOR_TWO_TONE);
		}

		settings.setReadConstantColor(readConstantColor);
		
		settings.setReadLowCutoffValue(rtv);
		settings.setReadTintLowColor(readTintLowColor);
		settings.setReadTintHighColor(readTintHighColor);

		settings.setReadHighColor(readHighColor);
		settings.setReadLowColor(readLowColor);
		settings.setReadColorThreshold(rct);
		
		if (getJStrainConstantCheckBox().isSelected()) {
			settings.setStrainColorStyle(DisplaySettings.COLOR_CONSTANT);
		} else if (getJStrainRandomCheckBox().isSelected()) {
			settings.setStrainColorStyle(DisplaySettings.COLOR_RANDOM);
			
			// Ask user
			int response = JOptionPane.showConfirmDialog(PaneledReferenceSequenceDisplay.frame,
					"Pick new strain colors?","Pick new strain colors?",
					JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				// clear array in  StrainDisplayGeometry (or is it in each strain?)
				DisplayGeometry.clearRandomColors();
			}
		} else  if (getJStrainTintCheckBox().isSelected()) {
			settings.setStrainColorStyle(DisplaySettings.COLOR_TINT);
		} else {
			settings.setStrainColorStyle(DisplaySettings.COLOR_TWO_TONE);
		}
		
		settings.setStrainConstantColor(strainConstantColor);

		settings.setStrainTintLowCutoffValue(stv);
		settings.setStrainTintLowColor(strainTintLowColor);
		settings.setStrainTintHighColor(strainTintHighColor);

		settings.setStrainHighColor(strainHighColor);
		settings.setStrainLowColor(strainLowColor);
		settings.setStrainColorThreshold(sct);

		settings.setRecombinantColor(recombColor);
		settings.setRecombinantSelectColor(recombSelectColor);
		
		settings.setDrawAllLetters(getJDrawAllLettersCheckBox().isSelected());
//		settings.setShowNs(getJShowNsCheckBox().isSelected());
	}

	/**
	 * This method initializes jReadConstantCheckPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJReadConstantCheckPanel() {
		if (jReadConstantCheckPanel == null) {
			jReadConstantCheckPanel = new JPanel();
			jReadConstantCheckPanel.add(getJReadConstantCheckBox(), null);
		}
		return jReadConstantCheckPanel;
	}

//	/**
//	 * This method initializes jReadRandomCheckPanel	
//	 * 	
//	 * @return javax.swing.JPanel	
//	 */
//	private JPanel getJReadRandomCheckPanel() {
//		if (jReadRandomCheckPanel == null) {
//			jReadRandomCheckPanel = new JPanel();
//			jReadRandomCheckPanel.add(getJReadRandomCheckBox(), null);
//		}
//		return jReadRandomCheckPanel;
//	}

	/**
	 * This method initializes jReadConstantSettingsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJReadConstantSettingsPanel() {
		if (jReadConstantSettingsPanel == null) {
			jReadConstantSettingsPanel = new JPanel();
			jReadConstantSettingsPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			jReadConstantLabel = new JLabel();
			jReadConstantLabel.setText("One color: ");
			jReadConstantSettingsPanel.add(jReadConstantLabel, null);
			jReadConstantSettingsPanel.add(getJReadConstantButton(), null);
		}
		return jReadConstantSettingsPanel;
	}

//	/**
//	 * This method initializes jReadRandomSettingsPanel	
//	 * 	
//	 * @return javax.swing.JPanel	
//	 */
//	private JPanel getJReadRandomSettingsPanel() {
//		if (jReadRandomSettingsPanel == null) {
//			jReadRandomSettingsPanel = new JPanel();
//			jReadRandomSettingsPanel.setBorder(BorderFactory.createLoweredBevelBorder());
//			jReadRandomLabel = new JLabel();
//			jReadRandomLabel.setText("Random color");
//			jReadRandomSettingsPanel.add(jReadRandomLabel, null);
//		}
//		return jReadRandomSettingsPanel;
//	}

	/**
	 * This method initializes jReadConstantButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJReadConstantButton() {
		if (jReadConstantButton == null) {
			jReadConstantButton = new JButton("Choose...");
			jReadConstantButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Color newColor = JColorChooser.showDialog(	
                            DisplayOptionsDialog.this,
                            "Choose High Color",
                            DisplayOptionsDialog.this.readConstantColor);
					if (newColor != null) {
						DisplayOptionsDialog.this.readConstantColor=newColor;
					}
				}
			});
		}
		return jReadConstantButton;
	}
	
	/**
	 * This method initializes jReadTintCheckPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJReadTintCheckPanel() {
		if (jReadTintCheckPanel == null) {
			jReadTintCheckPanel = new JPanel();
			jReadTintCheckPanel.add(getJReadTintCheckBox(), null);
		}
		return jReadTintCheckPanel;
	}

	/**
	 * This method initializes jReadTintSettingsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJReadTintSettingsPanel() {
		if (jReadTintSettingsPanel == null) {
			jReadTintSettingsPanel = new JPanel();
			jReadTintSettingsPanel.setLayout(new BoxLayout(jReadTintSettingsPanel, BoxLayout.PAGE_AXIS));
			jReadTintSettingsPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			
			JPanel labelPanel = new JPanel();
			JLabel labelLabel = new JLabel("Range of solid colors by similarity to reference:");
			labelPanel.add(labelLabel,null);
			
			jReadTintSettingsPanel.add(labelPanel,null);
			jReadTintSettingsPanel.add(getJReadTintColorsPanel(), null);
			jReadTintSettingsPanel.add(getJReadTintLowCutoffPanel(), null);
		}
		return jReadTintSettingsPanel;
	}

	/**
	 * This method initializes jReadTwoToneCheckPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJReadTwoToneCheckPanel() {
		if (jReadTwoToneCheckPanel == null) {
			jReadTwoToneCheckPanel = new JPanel();
			jReadTwoToneCheckPanel.add(getJReadTwoToneCheckBox(), null);
		}
		return jReadTwoToneCheckPanel;
	}

	/**
	 * This method initializes jReadTwoToneSettingsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJReadTwoToneSettingsPanel() {
		if (jReadTwoToneSettingsPanel == null) {
			JPanel labelPanel = new JPanel();
			jReadTwoToneSettingsLabel = new JLabel();
			jReadTwoToneSettingsLabel.setText("Two colors by local similarity: ");
			labelPanel.add(jReadTwoToneSettingsLabel);
			jReadTwoToneSettingsPanel = new JPanel();
			jReadTwoToneSettingsPanel.setLayout(new BoxLayout(jReadTwoToneSettingsPanel, BoxLayout.PAGE_AXIS));
			jReadTwoToneSettingsPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			jReadTwoToneSettingsPanel.add(labelPanel, null);
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.add(getJReadLowColorButton(), null);
			buttonsPanel.add(getJReadHighColorButton(), null);
			jReadTwoToneSettingsPanel.add(buttonsPanel,null);
			jReadTwoToneSettingsPanel.add(getJReadThresholdPanel(), null);
		}
		return jReadTwoToneSettingsPanel;
	}

	/**
	 * This method initializes jReadTintValuePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJReadTintLowCutoffPanel() {
		if (jReadTintLowCutoffPanel == null) {
			jReadTintLowCutoffLabel = new JLabel();
			jReadTintLowCutoffLabel.setText("Low % identity cutoff: ");
			jReadTintLowCutoffPanel = new JPanel();
			jReadTintLowCutoffPanel.add(jReadTintLowCutoffLabel, null);
			jReadTintLowCutoffPanel.add(getJReadTintLowCutoffTextField(), null);
		}
		return jReadTintLowCutoffPanel;
	}

	/**
	 * This method initializes jReadColorChoicePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJReadTintColorsPanel() {
		if (jReadTintColorsPanel == null) {
			jReadTintColorsPanel = new JPanel();
//			jReadTintColorsPanel.add(getJReadTintGreenCheckBox(), null);
//			jReadTintColorsPanel.add(getJReadTintBlueCheckBox(), null);
//			jReadTintColorsPanel.add(getJReadTintRedCheckBox(), null);
			jReadTintColorsPanel.add(getJReadTintLowColorButton(), null);
			jReadTintColorsPanel.add(getJReadTintHighColorButton(), null);
		}
		return jReadTintColorsPanel;
	}

	/**
	 * This method initializes jReadTintLowColorButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	 JButton getJReadTintLowColorButton() {
		if (jReadTintLowColorButton == null) {
			jReadTintLowColorButton = new JButton("Choose Low Color...");
			jReadTintLowColorButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Color newColor = JColorChooser.showDialog(	
                            DisplayOptionsDialog.this,
                            "Choose Low Color",
                            DisplayOptionsDialog.this.readTintLowColor);
					if (newColor != null) {
						DisplayOptionsDialog.this.readTintLowColor=newColor;
					}
				}
			});
		}
		return jReadTintLowColorButton;
	}

	/**
	 * This method initializes jReadTintHighColorButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	 JButton getJReadTintHighColorButton() {
		if (jReadTintHighColorButton == null) {
			jReadTintHighColorButton = new JButton("Choose High Color...");
			jReadTintHighColorButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Color newColor = JColorChooser.showDialog(	
                            DisplayOptionsDialog.this,
                            "Choose High Color",
                            DisplayOptionsDialog.this.readTintHighColor);
					if (newColor != null) {
						DisplayOptionsDialog.this.readTintHighColor=newColor;
					}
				}
			});
		}
		return jReadTintHighColorButton;
	}

	/**
	 * This method initializes jStrainTintCheckPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJStrainTintCheckPanel() {
		if (jStrainTintCheckPanel == null) {
			jStrainTintCheckPanel = new JPanel();
			jStrainTintCheckPanel.add(getJStrainTintCheckBox(), null);
		}
		return jStrainTintCheckPanel;
	}

	/**
	 * This method initializes jStrainTintSettingsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJStrainTintSettingsPanel() {
		if (jStrainTintSettingsPanel == null) {
			jStrainTintSettingsPanel = new JPanel();
			jStrainTintSettingsPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			jStrainTintSettingsPanel.setLayout(new BoxLayout(jStrainTintSettingsPanel, BoxLayout.PAGE_AXIS));
			
			JPanel labelPanel = new JPanel();
			JLabel labelLabel = new JLabel("Range of solid colors by similarity to reference:");
			labelPanel.add(labelLabel,null);
			
			jStrainTintSettingsPanel.add(labelPanel);
			jStrainTintSettingsPanel.add(getJStrainColorChoicePanel(), null);
			jStrainTintSettingsPanel.add(getJStrainTintLowCutoffPanel(), null);
		}
		return jStrainTintSettingsPanel;
	}

	/**
	 * This method initializes jStrainConstantCheckPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJStrainConstantCheckPanel() {
		if (jStrainConstantCheckPanel == null) {
			jStrainConstantCheckPanel = new JPanel();
			jStrainConstantCheckPanel.add(getJStrainConstantCheckBox(), null);
		}
		return jStrainConstantCheckPanel;
	}

	/**
	 * This method initializes jStrainRandomCheckPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJStrainRandomCheckPanel() {
		if (jStrainRandomCheckPanel == null) {
			jStrainRandomCheckPanel = new JPanel();
			jStrainRandomCheckPanel.add(getJStrainRandomCheckBox(), null);
		}
		return jStrainRandomCheckPanel;
	}

	/**
	 * This method initializes jStrainConstantSettingsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJStrainConstantSettingsPanel() {
		if (jStrainConstantSettingsPanel == null) {
			jStrainConstantSettingsPanel = new JPanel();
			jStrainConstantLabel = new JLabel();
			jStrainConstantLabel.setText("One color: ");
			jStrainConstantSettingsPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			jStrainConstantSettingsPanel.add(jStrainConstantLabel, null);
			jStrainConstantSettingsPanel.add(getJStrainConstantButton(), null);
		}
		return jStrainConstantSettingsPanel;
	}

	/**
	 * This method initializes jStrainRandomSettingsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJStrainRandomSettingsPanel() {
		if (jStrainRandomSettingsPanel == null) {
			jStrainRandomSettingsPanel = new JPanel();
			jStrainRandomLabel = new JLabel();
			jStrainRandomLabel.setText("Random color");
			jStrainRandomSettingsPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			jStrainRandomSettingsPanel.add(jStrainRandomLabel, null);
		}
		return jStrainRandomSettingsPanel;
	}

	/**
	 * This method initializes jStrainConstantButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJStrainConstantButton() {
		if (jStrainConstantButton == null) {
			jStrainConstantButton = new JButton("Choose...");
			jStrainConstantButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Color newColor = JColorChooser.showDialog(	
                            DisplayOptionsDialog.this,
                            "Choose High Color",
                            DisplayOptionsDialog.this.strainConstantColor);
					if (newColor != null) {
						DisplayOptionsDialog.this.strainConstantColor=newColor;
					}
				}
			});
		}
		return jStrainConstantButton;
	}

	/**
	 * This method initializes jStrainTintValuePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJStrainTintLowCutoffPanel() {
		if (jStrainTintLowCutoffPanel == null) {
			jStrainTintLowCutoffLabel = new JLabel();
			jStrainTintLowCutoffLabel.setText("Low % identity cutoff: ");
			jStrainTintLowCutoffPanel = new JPanel();
			jStrainTintLowCutoffPanel.add(jStrainTintLowCutoffLabel, null);
			jStrainTintLowCutoffPanel.add(getJStrainTintLowCutoffTextField(), null);
			}
		return jStrainTintLowCutoffPanel;
	}

	/**
	 * This method initializes jStrainColorChoicePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJStrainColorChoicePanel() {
		if (jStrainColorChoicePanel == null) {
			jStrainColorChoicePanel = new JPanel();
//			jStrainColorChoicePanel.add(getJStrainTintGreenCheckBox(), null);
//			jStrainColorChoicePanel.add(getJStrainTintBlueCheckBox(), null);
//			jStrainColorChoicePanel.add(getJStrainTintRedCheckBox(), null);
			jStrainColorChoicePanel.add(getJStrainTintLowColorButton(), null);
			jStrainColorChoicePanel.add(getJStrainTintHighColorButton(), null);
		}
		return jStrainColorChoicePanel;
	}

	/**
	 * This method initializes jStrainTintLowColorButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	 JButton getJStrainTintLowColorButton() {
		if (jStrainTintLowColorButton == null) {
			jStrainTintLowColorButton = new JButton("Choose Low Color...");
			jStrainTintLowColorButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Color newColor = JColorChooser.showDialog(	
                            DisplayOptionsDialog.this,
                            "Choose Low Color",
                            DisplayOptionsDialog.this.strainTintLowColor);
					if (newColor != null) {
						DisplayOptionsDialog.this.strainTintLowColor=newColor;
					}
				}
			});
		}
		return jStrainTintLowColorButton;
	}

	/**
	 * This method initializes jStrainTintHighColorButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	 JButton getJStrainTintHighColorButton() {
		if (jStrainTintHighColorButton == null) {
			jStrainTintHighColorButton = new JButton("Choose High Color...");
			jStrainTintHighColorButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Color newColor = JColorChooser.showDialog(	
                            DisplayOptionsDialog.this,
                            "Choose High Color",
                            DisplayOptionsDialog.this.strainTintHighColor);
					if (newColor != null) {
						DisplayOptionsDialog.this.strainTintHighColor=newColor;
					}
				}
			});
		}
		return jStrainTintHighColorButton;
	}
	/**
	 * This method initializes jStrainTwoToneCheckPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJStrainTwoToneCheckPanel() {
		if (jStrainTwoToneCheckPanel == null) {
			jStrainTwoToneCheckPanel = new JPanel();
			jStrainTwoToneCheckPanel.add(getJStrainTwoToneCheckBox(), null);
		}
		return jStrainTwoToneCheckPanel;
	}

	/**
	 * This method initializes jStrainTwoToneSettingsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJStrainTwoToneSettingsPanel() {
		if (jStrainTwoToneSettingsPanel == null) {
			JPanel labelPanel = new JPanel();
			jStrainTwoToneLabel = new JLabel();
			jStrainTwoToneLabel.setText("Two colors by local similarity: ");
			labelPanel.add(jStrainTwoToneLabel,null);
			jStrainTwoToneSettingsPanel=new JPanel();
			jStrainTwoToneSettingsPanel.setLayout(new BoxLayout(jStrainTwoToneSettingsPanel, BoxLayout.PAGE_AXIS));
			jStrainTwoToneSettingsPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			jStrainTwoToneSettingsPanel.add(labelPanel, null);
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.add(getJStrainLowColorButton(), null);
			buttonsPanel.add(getJStrainHighColorButton(), null);
			jStrainTwoToneSettingsPanel.add(buttonsPanel, null);
			jStrainTwoToneSettingsPanel.add(buttonsPanel, null);
			jStrainTwoToneSettingsPanel.add(getJStrainThresholdPanel(), null);
		}
		return jStrainTwoToneSettingsPanel;
	}
	
	/**
	 * This method initializes jReadConstantCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	JCheckBox getJReadConstantCheckBox() {
		if (jReadConstantCheckBox == null) {
			jReadConstantCheckBox = new JCheckBox();
			jReadConstantCheckBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					boolean enabled = getJReadConstantCheckBox().isSelected();
					getJReadConstantButton().setEnabled(enabled);
				}
			});

		}
		return jReadConstantCheckBox;
	}

//	/**
//	 * This method initializes jReadRandomCheckBox	
//	 * 	
//	 * @return javax.swing.JCheckBox	
//	 */
//	private JCheckBox getJReadRandomCheckBox() {
//		if (jReadRandomCheckBox == null) {
//			jReadRandomCheckBox = new JCheckBox();
//		}
//		return jReadRandomCheckBox;
//	}

	/**
	 * This method initializes jReadTintCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	JCheckBox getJReadTintCheckBox() {
		if (jReadTintCheckBox == null) {
			jReadTintCheckBox = new JCheckBox();
			jReadTintCheckBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					boolean enabled = getJReadTintCheckBox().isSelected();
//					getJReadTintBlueCheckBox().setEnabled(enabled);
//					getJReadTintRedCheckBox().setEnabled(enabled);
//					getJReadTintGreenCheckBox().setEnabled(enabled);
					getJReadLowColorButton().setEnabled(enabled);
					getJReadHighColorButton().setEnabled(enabled);
					getJReadTintLowCutoffTextField().setEnabled(enabled);
				}
			});
		}
		return jReadTintCheckBox;
	}

	/**
	 * This method initializes jReadTwoToneCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	 JCheckBox getJReadTwoToneCheckBox() {
		if (jReadTwoToneCheckBox == null) {
			jReadTwoToneCheckBox = new JCheckBox();
			jReadTwoToneCheckBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					boolean enabled = getJReadTwoToneCheckBox().isSelected();
					getJReadHighColorButton().setEnabled(enabled);
					getJReadLowColorButton().setEnabled(enabled);
					getJReadThresholdTextField().setEnabled(enabled);
				}
			});
		}
		return jReadTwoToneCheckBox;
	}

	/**
	 * This method initializes jStrainTintCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	JCheckBox getJStrainTintCheckBox() {
		if (jStrainTintCheckBox == null) {
			jStrainTintCheckBox = new JCheckBox();
			jStrainTintCheckBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					boolean enabled = getJStrainTintCheckBox().isSelected();
//					getJStrainTintBlueCheckBox().setEnabled(enabled);
//					getJStrainTintRedCheckBox().setEnabled(enabled);
//					getJStrainTintGreenCheckBox().setEnabled(enabled);
					getJStrainTintLowColorButton().setEnabled(enabled);
					getJStrainTintHighColorButton().setEnabled(enabled);
					getJStrainTintLowCutoffTextField().setEnabled(enabled);
				}
			});
		}
		return jStrainTintCheckBox;
	}

	/**
	 * This method initializes jStrainConstantCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	JCheckBox getJStrainConstantCheckBox() {
		if (jStrainConstantCheckBox == null) {
			jStrainConstantCheckBox = new JCheckBox();
			jStrainConstantCheckBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					boolean enabled = getJStrainConstantCheckBox().isSelected();
					getJStrainConstantButton().setEnabled(enabled);
				}
			});

		}
		return jStrainConstantCheckBox;
	}

	/**
	 * This method initializes jStrainRandomCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJStrainRandomCheckBox() {
		if (jStrainRandomCheckBox == null) {
			jStrainRandomCheckBox = new JCheckBox();
		}
		return jStrainRandomCheckBox;
	}

	/**
	 * This method initializes jStrainTwoToneCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	 JCheckBox getJStrainTwoToneCheckBox() {
		if (jStrainTwoToneCheckBox == null) {
			jStrainTwoToneCheckBox = new JCheckBox();
			jStrainTwoToneCheckBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					boolean enabled = getJStrainTwoToneCheckBox().isSelected();
					getJStrainHighColorButton().setEnabled(enabled);
					getJStrainLowColorButton().setEnabled(enabled);
					getJStrainThresholdTextField().setEnabled(enabled);
				}
			});
		}
		return jStrainTwoToneCheckBox;
	}

	/**
	 * This method initializes jTintValueTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	 JTextField getJReadTintLowCutoffTextField() {
		if (jReadTintLowCutoffTextField == null) {
			jReadTintLowCutoffTextField = new JTextField();
			jReadTintLowCutoffTextField.setColumns(3);
		}
		return jReadTintLowCutoffTextField;
	}

//	/**
//	 * This method initializes jReadTintGreenCheckBox	
//	 * 	
//	 * @return javax.swing.JCheckBox	
//	 */
//	 JCheckBox getJReadTintGreenCheckBox() {
//		if (jReadTintGreenCheckBox == null) {
//			jReadTintGreenCheckBox = new JCheckBox();
//			jReadTintGreenCheckBox.setText("Green");
//		}
//		return jReadTintGreenCheckBox;
//	}
//
//	/**
//	 * This method initializes jReadTintBlueCheckBox	
//	 * 	
//	 * @return javax.swing.JCheckBox	
//	 */
//	JCheckBox getJReadTintBlueCheckBox() {
//		if (jReadTintBlueCheckBox == null) {
//			jReadTintBlueCheckBox = new JCheckBox();
//			jReadTintBlueCheckBox.setText("Blue");
//		}
//		return jReadTintBlueCheckBox;
//	}
//
//	/**
//	 * This method initializes jReadTintRedCheckBox	
//	 * 	
//	 * @return javax.swing.JCheckBox	
//	 */
//	 JCheckBox getJReadTintRedCheckBox() {
//		if (jReadTintRedCheckBox == null) {
//			jReadTintRedCheckBox = new JCheckBox();
//			jReadTintRedCheckBox.setText("Red");
//		}
//		return jReadTintRedCheckBox;
//	}

	/**
	 * This method initializes jReadLowColorButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	 JButton getJReadLowColorButton() {
		if (jReadLowColorButton == null) {
			jReadLowColorButton = new JButton("Choose Low Color...");
			jReadLowColorButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Color newColor = JColorChooser.showDialog(	
                            DisplayOptionsDialog.this,
                            "Choose Low Color",
                            DisplayOptionsDialog.this.readLowColor);
					if (newColor != null) {
						DisplayOptionsDialog.this.readLowColor=newColor;
					}
				}
			});
		}
		return jReadLowColorButton;
	}

	/**
	 * This method initializes jReadHighColorButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	 JButton getJReadHighColorButton() {
		if (jReadHighColorButton == null) {
			jReadHighColorButton = new JButton("Choose High Color...");
			jReadHighColorButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Color newColor = JColorChooser.showDialog(	
                            DisplayOptionsDialog.this,
                            "Choose High Color",
                            DisplayOptionsDialog.this.readHighColor);
					if (newColor != null) {
						DisplayOptionsDialog.this.readHighColor=newColor;
					}
				}
			});
		}
		return jReadHighColorButton;
	}

	private JPanel getJReadThresholdPanel() {
		if (jReadThresholdPanel == null) {
			jReadThresholdLabel = new JLabel();
			jReadThresholdLabel.setText("% Identity Threshold: ");
			jReadThresholdPanel = new JPanel();
			jReadThresholdPanel.add(jReadThresholdLabel, null);
			jReadThresholdPanel.add(getJReadThresholdTextField(), null);
		}
		return jReadThresholdPanel;
	}
		
	/**
	 * This method initializes jReadThresholdTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	 JTextField getJReadThresholdTextField() {
		if (jReadThresholdTextField == null) {
			jReadThresholdTextField = new JTextField();
			jReadThresholdTextField.setColumns(4);
		}
		return jReadThresholdTextField;
	}

	/**
	 * This method initializes jTintValueTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	 JTextField getJStrainTintLowCutoffTextField() {
		if (jStrainTintLowCutoffTextField == null) {
			jStrainTintLowCutoffTextField = new JTextField();
			jStrainTintLowCutoffTextField.setColumns(3);
		}
		return jStrainTintLowCutoffTextField;
	}

//	/**
//	 * This method initializes jStrainTintGreenCheckBox	
//	 * 	
//	 * @return javax.swing.JCheckBox	
//	 */
//	 JCheckBox getJStrainTintGreenCheckBox() {
//		if (jStrainTintGreenCheckBox == null) {
//			jStrainTintGreenCheckBox = new JCheckBox();
//			jStrainTintGreenCheckBox.setText("Green");
//		}
//		return jStrainTintGreenCheckBox;
//	}
//
//	/**
//	 * This method initializes jStrainTintBlueCheckBox	
//	 * 	
//	 * @return javax.swing.JCheckBox	
//	 */
//	JCheckBox getJStrainTintBlueCheckBox() {
//		if (jStrainTintBlueCheckBox == null) {
//			jStrainTintBlueCheckBox = new JCheckBox();
//			jStrainTintBlueCheckBox.setText("Blue");
//		}
//		return jStrainTintBlueCheckBox;
//	}
//
//	/**
//	 * This method initializes jStrainTintRedCheckBox	
//	 * 	
//	 * @return javax.swing.JCheckBox	
//	 */
//	 JCheckBox getJStrainTintRedCheckBox() {
//		if (jStrainTintRedCheckBox == null) {
//			jStrainTintRedCheckBox = new JCheckBox();
//			jStrainTintRedCheckBox.setText("Red");
//		}
//		return jStrainTintRedCheckBox;
//	}

	/**
	 * This method initializes jStrainLowColorButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	 JButton getJStrainLowColorButton() {
		if (jStrainLowColorButton == null) {
			jStrainLowColorButton = new JButton("Choose Low Color...");
			jStrainLowColorButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Color newColor = JColorChooser.showDialog(	
                            DisplayOptionsDialog.this,
                            "Choose Low Color",
                            DisplayOptionsDialog.this.strainLowColor);
					if (newColor != null) {
						DisplayOptionsDialog.this.strainLowColor=newColor;
					}
				}
			});
		}
		return jStrainLowColorButton;
	}

	/**
	 * This method initializes jStrainHighColorButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	 JButton getJStrainHighColorButton() {
		if (jStrainHighColorButton == null) {
			jStrainHighColorButton = new JButton("Choose High Color...");
			jStrainHighColorButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Color newColor = JColorChooser.showDialog(	
                            DisplayOptionsDialog.this,
                            "Choose High Color",
                            DisplayOptionsDialog.this.strainHighColor);
					if (newColor != null) {
						DisplayOptionsDialog.this.strainHighColor=newColor;
					}
				}
			});
		}
		return jStrainHighColorButton;
	}

	private JPanel getJStrainThresholdPanel() {
		if (jStrainThresholdPanel == null) {
			jStrainThresholdLabel = new JLabel();
			jStrainThresholdLabel.setText("% Identity Threshold: ");
			jStrainThresholdPanel = new JPanel();
			jStrainThresholdPanel.add(jStrainThresholdLabel, null);
			jStrainThresholdPanel.add(getJStrainThresholdTextField(), null);
		}
		return jStrainThresholdPanel;
	}
	
	/**
	 * This method initializes jStrainThresholdTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	 JTextField getJStrainThresholdTextField() {
		if (jStrainThresholdTextField == null) {
			jStrainThresholdTextField = new JTextField();
			jStrainThresholdTextField.setColumns(4);
		}
		return jStrainThresholdTextField;
	}

	/**
	 * This method initializes jRecombColorButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJRecombColorButton() {
		if (jRecombColorButton == null) {
			jRecombColorButton = new JButton("Set...");
			jRecombColorButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Color newColor = JColorChooser.showDialog(	
                            DisplayOptionsDialog.this,
                            "Choose Recombinant Color",
                            DisplayOptionsDialog.this.recombColor);
					if (newColor != null) {
						DisplayOptionsDialog.this.setRecombColor(newColor);
					}
				}
			});
		}
		return jRecombColorButton;
	}

	/**
	 * This method initializes jRecombSelectColorButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJRecombSelectColorButton() {
		if (jRecombSelectColorButton == null) {
			jRecombSelectColorButton = new JButton("Set...");
			jRecombSelectColorButton
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Color newColor = JColorChooser.showDialog(	
                            DisplayOptionsDialog.this,
                            "Choose Recombinant Color",
                            DisplayOptionsDialog.this.recombSelectColor);
					if (newColor != null) {
						DisplayOptionsDialog.this.setRecombSelectColor(newColor);
					}
				}
			});
		}
		return jRecombSelectColorButton;
	}
}
