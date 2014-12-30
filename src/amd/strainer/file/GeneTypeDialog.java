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
 ***** END LICENSE BLOCK ***** */package amd.strainer.file;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

import amd.strainer.GlobalSettings;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.util.Util;
import amd.strainer.objects.Gene;

/**
 * Asks the user to choose from a list of annotation types to include as Genes in the display
 * <p>
 * Should be run using the showDialo() method
 * 
 * @author jmeppley	
 *
 */
public class GeneTypeDialog extends JDialog {
	
	private Map<String,Set<Gene>> mFeatureGroups;
	Map<String,JCheckBox> mFeatureChecks;
	
	private JPanel jContentPane = null;
	private JPanel jButtonPanel = null;
	private JPanel jTypeCheckPanel = null;
	private JPanel jHeaderPanel = null;
	private JLabel jHeaderLabel = null;
	private JButton jAcceptButton = null;
	private JButton jSelectAllButton = null;
	private JButton jSelectNoneButton = null;
	private JLabel jBlankCheckHeaderLabel = null;
	private JLabel jTypeHeaderLabel = null;
	private JLabel jTypeCountHeaderLabel = null;
	private JPanel jNameCheckPanel = null;
	private JCheckBox jNameCheckBox = null;
	private JTextField jNameTextField = null;
	
	/**
	 * Asks the user to select which types of features to display. Currently the user sees a table of annotation types and
	 * the number of annotations of each type. the user can then check the types to be included in the display.
	 * 
	 * @param pFeatureGroups HashMap mapping type names (CDS,tRNA,rRNA, etc) to lists of genes.  Enclosed
	 *  gene lists are HAshMaps of gene names (ProA, hyp, etc) to Gene objects
	 * @return Set of genes in selected groups (keyed on names, as above) sorted by start pos
	 */
	public static String [] showDialog(Map<String,Set<Gene>> pFeatureGroups) {
		GeneTypeDialog dialog = new GeneTypeDialog(pFeatureGroups);
		// show dialog and wait for response
		dialog.setVisible(true);

		List<String> typeList = dialog.getSelectedTypes();
		String [] types = typeList.toArray(new String [typeList.size()]);

		if ( dialog.jNameCheckBox.isSelected() ) {
			GlobalSettings.setGenePrefix ( dialog.jNameTextField.getText().trim());
		}
		
		return types;
	}
	
	private List<String> getSelectedTypes() {
		List<String> types = new ArrayList<String>();

		// loop over type/checkbox pairs
		for (Map.Entry<String,JCheckBox> e : mFeatureChecks.entrySet()) {
			JCheckBox check = e.getValue();

			// add type to list if check box is selected
			if (check.isSelected()) {
				types.add(e.getKey());
			}
		}

		// return selected type names
		return types;
	}
	
	/**
	 * This is the default constructor
	 */
	private GeneTypeDialog(Map<String,Set<Gene>> pFeatureGroups) {
		super(JOptionPane.getFrameForComponent(PaneledReferenceSequenceDisplay.frame),true);
		mFeatureGroups = pFeatureGroups;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setTitle("Select Feature Types");
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
			jContentPane.add(getJTypeCheckPanel(), java.awt.BorderLayout.WEST);
			JPanel buffer = new JPanel();
			buffer.add(getJNameCheckPanel());
			jContentPane.add(buffer, java.awt.BorderLayout.EAST);
			jContentPane.add(getJHeaderPanel(), java.awt.BorderLayout.NORTH);
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
			jButtonPanel.add(getJSelectAllButton(), null);
			jButtonPanel.add(getJSelectNoneButton(), null);
			jButtonPanel.add(getJAcceptButton(), null);
		}
		return jButtonPanel;
	}

	/**
	 * This method initializes jTypeCheckPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJTypeCheckPanel() {
		if (jTypeCheckPanel == null) {
			jBlankCheckHeaderLabel = new JLabel();
			jBlankCheckHeaderLabel.setText(" ");
			jTypeHeaderLabel = new JLabel();
			jTypeHeaderLabel.setText("Feature Type");
			jTypeCountHeaderLabel = new JLabel();
			jTypeCountHeaderLabel.setText("Count");
			jTypeCheckPanel = new JPanel();
			SpringLayout layout = new SpringLayout();
			jTypeCheckPanel.setLayout(layout);
//			jTypeCheckPanel.add(jBlankCheckHeaderLabel, null);
			jTypeCheckPanel.add(jTypeHeaderLabel,null);
			jTypeCheckPanel.add(jTypeCountHeaderLabel,null);
			
			int rows = 1;
			mFeatureChecks= new HashMap<String,JCheckBox>();
			for (Map.Entry<String,Set<Gene>> e : mFeatureGroups.entrySet()) {
				rows++;
				String type = e.getKey().toString();
				String count = String.valueOf(e.getValue().size());
				JCheckBox box = new JCheckBox();
				box.setText(type);
				mFeatureChecks.put(type,box);
				jTypeCheckPanel.add(box,null);
//				jTypeCheckPanel.add(new JLabel(type),null);
				jTypeCheckPanel.add(new JLabel(count),null);
			}
		
			int cols = 2;
			
			int initialX = 5;
			int initialY = 5;
			int xPad  = 5;
			int yPad = 5;
			
			Util.makeCompactGrid(jTypeCheckPanel,layout,rows,cols,initialX,initialY,xPad,yPad);
			
		}
		return jTypeCheckPanel;
	}

	/**
	 * This method initializes jNameCheckPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJNameCheckPanel() {
		if (jNameCheckPanel == null) {
			jNameCheckPanel = new JPanel();
			jNameCheckPanel.setLayout(new BoxLayout(jNameCheckPanel, BoxLayout.PAGE_AXIS));
			jNameCheckPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Gene naming options:"),
                    BorderFactory.createEmptyBorder(5,5,5,5)));
			jNameCheckBox = new JCheckBox("Number genes with this prefix.");
			jNameCheckPanel.add(jNameCheckBox);
			jNameTextField = new JTextField(20);
			jNameCheckPanel.add(jNameTextField);
		}
		return jNameCheckPanel;
	}

	/**
	 * This method initializes jHeaderPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJHeaderPanel() {
		if (jHeaderPanel == null) {
			jHeaderLabel = new JLabel();
			jHeaderLabel.setText("Which feature types are 'Genes'?");
			jHeaderPanel = new JPanel();
			jHeaderPanel.add(jHeaderLabel, null);
		}
		return jHeaderPanel;
	}

	/**
	 * This method initializes jAcceptButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJAcceptButton() {
		if (jAcceptButton == null) {
			Action acceptAction = new AbstractAction("Accept") {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			};


			jAcceptButton = new JButton(acceptAction);
			jAcceptButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RETURN"),"ret");
			jAcceptButton.getActionMap().put("ret",acceptAction);
			jAcceptButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"),"ent");
			jAcceptButton.getActionMap().put("ent",acceptAction);
		}
		return jAcceptButton;
	}
	
	/**
	 * This method initializes jSelectAllButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJSelectAllButton() {
		if (jSelectAllButton == null) {
			Action selectAllAction = new AbstractAction("Select All") {
				public void actionPerformed(ActionEvent e) {
					for (JCheckBox featureCheckBox : mFeatureChecks.values()) {
						featureCheckBox.setSelected(true);
					}
				}
			};
			jSelectAllButton = new JButton(selectAllAction);
		}
		return jSelectAllButton;
	}

	/**
	 * This method initializes jSelectNoneButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJSelectNoneButton() {
		if (jSelectNoneButton == null) {
			Action selectNoneAction = new AbstractAction("Select None") {
				public void actionPerformed(ActionEvent e) {
					for (JCheckBox featureCheckBox : mFeatureChecks.values()) {
						featureCheckBox.setSelected(false);
					}
				}
			};

			jSelectNoneButton = new JButton(selectNoneAction);
		}
		return jSelectNoneButton;
	}
}
