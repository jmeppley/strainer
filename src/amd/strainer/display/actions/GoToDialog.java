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
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import amd.strainer.GoToException;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.ReferenceSequenceDisplayComponent;
import amd.strainer.display.util.Util;

public class GoToDialog extends JDialog {

	/**
	 * Auto-generated serial id
	 */
	private static final long serialVersionUID = -4680253583362311381L;

	ReferenceSequenceDisplayComponent canvas = null; 
	
	private JPanel 		jContentPane = null;
	
	private JPanel  		 jHeaderPanel = null;
	private JLabel   	  jHeaderLabel = null;

	private JPanel 	 	 jCenterPanel = null;
	private JPanel 	 	  jSelectionPanel = null;
	private JRadioButton 	   jPositionButton = null;
	private JRadioButton    jGeneButton = null;
	private JRadioButton    jReadButton = null;
	private JPanel         jTextPanel = null;
	private JTextField 	   jGoToField = null;
	private JPanel 	 	  jZoomPanel = null;
	private JLabel		   jZoomLabel = null;
	private JRadioButton 	   jZoomExtentButton = null;
	private JRadioButton    jZoomCurrentButton = null;
	private JRadioButton    jZoomDiffsButton = null;

	private JPanel  		 jFooterPanel = null;
	private JButton 	      jGoToButton = null;
	private JButton        jCancelButton = null;

	/**
	 * This is the default constructor
	 */
	private GoToDialog(ReferenceSequenceDisplayComponent pCanvas) {
		super(JOptionPane.getFrameForComponent(PaneledReferenceSequenceDisplay.frame),true);
		canvas = pCanvas;
		initialize();
	}

	private static GoToDialog dialog = null;
	
	/**
	 * setup and show the dialog
	 * 
	 * @param pParent ReferenceSequenceDisplay object initiating this dialog
	 */
    public static void showDialog(ReferenceSequenceDisplayComponent pCanvas) {
    		if (dialog==null) {
    			dialog = new GoToDialog(pCanvas);
    		}
    		dialog.setVisible(true);
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
//		this.setSize(300, 170);
		this.setTitle("GoTo");
		this.setContentPane(getJContentPane());
		
		ButtonGroup group = new ButtonGroup();
		group.add(getJPositionButton());
		group.add(getJGeneButton());
		group.add(getJReadButton());
		
		group = new ButtonGroup();
		group.add(getJZoomExtentButton());
		group.add(getJZoomCurrentButton());
		group.add(getJZoomDiffsButton());
	
		this.pack();
		getJGoToField().requestFocusInWindow();
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
			jContentPane.add(getJHeaderPanel(), java.awt.BorderLayout.NORTH);
			jContentPane.add(getJCenterPanel(), java.awt.BorderLayout.CENTER);
			jContentPane.add(getJFooterPanel(), java.awt.BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jHeaderPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJHeaderPanel() {
		if (jHeaderPanel == null) {
			jHeaderLabel = new JLabel();
			jHeaderLabel.setText("Go to a position, gene, or read");
			jHeaderPanel = new JPanel();
			jHeaderPanel.add(jHeaderLabel, null);
		}
		return jHeaderPanel;
	}

	/**
	 * This method initializes jCenterPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJCenterPanel() {
		if (jCenterPanel == null) {
			jCenterPanel = new JPanel();
			jCenterPanel.setLayout(new BoxLayout(jCenterPanel, BoxLayout.PAGE_AXIS));
			jCenterPanel.add(getJSelectionPanel());
			jCenterPanel.add(getJTextPanel());
			jCenterPanel.add(getJZoomPanel());
		}
		return jCenterPanel;
	}

	/**
	 * This method initializes jFooterPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJFooterPanel() {
		if (jFooterPanel == null) {
			jFooterPanel = new JPanel();
			jFooterPanel.add(getJGoToButton(), null);
			jFooterPanel.add(getJCancelButton(), null);
		}
		return jFooterPanel;
	}

	/**
	 * This method initializes jSelectionPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJSelectionPanel() {
		if (jSelectionPanel == null) {
			jSelectionPanel = new JPanel();
			jSelectionPanel.add(getJPositionButton(), null);
			jSelectionPanel.add(getJGeneButton(), null);
			jSelectionPanel.add(getJReadButton(), null);
		}
		return jSelectionPanel;
	}

	/**
	 * This method initializes jTextPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJTextPanel() {
		if (jTextPanel == null) {
			jTextPanel = new JPanel();
			jTextPanel.add(getJGoToField(), null);
		}
		return jTextPanel;
	}

	/**
	 * This method initializes jZoomPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJZoomPanel() {
		if (jZoomPanel == null) {
			jZoomPanel = new JPanel();
			jZoomLabel = new JLabel("Zoom: ");
			jZoomPanel.add(jZoomLabel);
			jZoomPanel.add(getJZoomExtentButton(), null);
			jZoomPanel.add(getJZoomCurrentButton(), null);
			jZoomPanel.add(getJZoomDiffsButton(), null);
		}
		return jZoomPanel;
	}
	/**
	 * This method initializes jPositionButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJPositionButton() {
		if (jPositionButton == null) {
			jPositionButton = new JRadioButton();
			jPositionButton.setSelected(true);
			jPositionButton.setText("Position");
		}
		return jPositionButton;
	}

	/**
	 * This method initializes jGeneButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJGeneButton() {
		if (jGeneButton == null) {
			jGeneButton = new JRadioButton();
			jGeneButton.setText("Gene");
		}
		return jGeneButton;
	}

	/**
	 * This method initializes jReadButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJReadButton() {
		if (jReadButton == null) {
			jReadButton = new JRadioButton();
			jReadButton.setText("Read");
		}
		return jReadButton;
	}
	
	/**
	 * This method initializes jZoomExtentButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJZoomExtentButton() {
		if (jZoomExtentButton == null) {
			jZoomExtentButton = new JRadioButton();
			jZoomExtentButton.setText("extent");
		}
		return jZoomExtentButton;
	}

	/**
	 * This method initializes jZoomCurrentButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJZoomCurrentButton() {
		if (jZoomCurrentButton == null) {
			jZoomCurrentButton = new JRadioButton();
			jZoomCurrentButton.setSelected(true);
			jZoomCurrentButton.setText("as is");
		}
		return jZoomCurrentButton;
	}

	/**
	 * This method initializes jZoomDiffsButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJZoomDiffsButton() {
		if (jZoomDiffsButton == null) {
			jZoomDiffsButton = new JRadioButton();
			jZoomDiffsButton.setText("diffs");
		}
		return jZoomDiffsButton;
	}

	/**
	 * This method initializes jGoToField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJGoToField() {
		if (jGoToField == null) {
			jGoToField = new JTextField();
			jGoToField.setColumns(14);
		}
		return jGoToField;
	}

	/**
	 * This method initializes jGoToButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJGoToButton() {
		if (jGoToButton == null) {
			Action gotoAction = new AbstractAction("GoTo") {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doGoTo();
				}
			};

			jGoToButton = new JButton(gotoAction);
			jGoToButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"),"ent");
			jGoToButton.getActionMap().put("ent",gotoAction);
			jGoToButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RETURN"),"ret");
			jGoToButton.getActionMap().put("ret",gotoAction);
		}
		return jGoToButton;
	}

	/*
	 * This method does all the work here.  Look at the radio buttons, then pass the
	 * input string to the appropriate canvas method.
	 */
	void doGoTo() {
		String text = getJGoToField().getText();
		try {
			if (getJPositionButton().isSelected()) {
				if (getJZoomExtentButton().isSelected()) {
					throw new GoToException("A position has no extent, try 'as is' or 'diffs.'");
				}

				int position = Integer.parseInt(text);
				canvas.goToPosition(position,getJZoomDiffsButton().isSelected());
				canvas.repaint();
			} else if (getJGeneButton().isSelected()) {
				canvas.goToGene(text,
						getJZoomExtentButton().isSelected(),
						getJZoomCurrentButton().isSelected(),
						getJZoomDiffsButton().isSelected());
			} else {
				canvas.goToRead(text,
						getJZoomExtentButton().isSelected(),
						getJZoomCurrentButton().isSelected(),
						getJZoomDiffsButton().isSelected());
			}
			setVisible(false);
		} catch (NumberFormatException e) {
			Util.displayErrorMessage(this,"Could not turn \"" + text + "\" into a number.");
			return;
		} catch (amd.strainer.GoToException e) {
			Util.displayErrorMessage(this,"Error finding object: " + e.getMessage());
			return;
		}
	}

	/**
	 * This method initializes jGoToButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJCancelButton() {
		if (jCancelButton == null) {
			Action cancelAction = new AbstractAction("Cancel") {
				public void actionPerformed(ActionEvent e) {
					doCancel();
				}
			};

			jCancelButton = new JButton(cancelAction);
			jCancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"),"esc");
			jCancelButton.getActionMap().put("esc",cancelAction);
		}
		return jCancelButton;
	}

	/*
	 * Just close the dialog
	 */
	void doCancel() {
		this.setVisible(false);
	}
	
}
