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
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import amd.strainer.algs.Config;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.util.Util;

public class SegmentStrainerSettingsDialog extends JDialog {
	
	////////////////////////
	// only outside interface
	///////////////////////
	
	/**
	 * Display dialog for configuring the indicated Strainer algorithm
	 * @param pSegmentStrainer
	 */
	public static void showDialog(Class pSegmentStrainer) {
		SegmentStrainerSettingsDialog dialog = null;
		try {
			dialog = getDialogForAlgorithm(pSegmentStrainer);
		} catch (Exception e) {
			Util.displayErrorMessage(PaneledReferenceSequenceDisplay.frame,e.toString());
			e.printStackTrace();
			return;
		}

		// this will make sure onscreen values are all set correctly
		dialog.revertSettings();
		
		// show dialog and wait for it to be closed
		dialog.setVisible(true);
		
		// we're done, the dialog will take care of changing settings when it's closed
	}
	
	//////////////////////////
	// private methods and variables for creating new dialogs
	//////////////////////////
	
	// hash of instances, one for each algorithm
	private static HashMap<Class,SegmentStrainerSettingsDialog> instanceMap = new HashMap<Class,SegmentStrainerSettingsDialog>();

	// get the appropriate dialog from the hash
	private static SegmentStrainerSettingsDialog getDialogForAlgorithm(Class pSegmentStrainer) 
	throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IllegalArgumentException, InstantiationException
	{
		SegmentStrainerSettingsDialog dialog = instanceMap.get(pSegmentStrainer);
		if (dialog==null) {
			dialog = new SegmentStrainerSettingsDialog(Config.getDefaultValuesForAlgClass(pSegmentStrainer));
			instanceMap.put(pSegmentStrainer,dialog);
		}
		return dialog;
	}
	
	// private constructor
	private SegmentStrainerSettingsDialog(Map<String,Object> pOptionsHash) {
		super(JOptionPane.getFrameForComponent(PaneledReferenceSequenceDisplay.frame), true);
//		mOptionsHash = pOptionsHash;
		initialize(pOptionsHash);
	}
	
	// pointer to the algorithm settings hash
	private static HashMap<String,Object> settings = Config.getConfig().getSettings();
	
	////////////////////////////
	// Object variables (different in each instance)
	////////////////////////////
	private HashMap<String,JComponent> mSettingsComponents = null;
	
	///////////////////////////
	// Object methods for doing work
	///////////////////////////
	// save the settings
	void saveSettings() {
		// do this in two steps to check values
		
		// go through options and get values
		HashMap<String,Object> newValues = new HashMap<String,Object>();

		try {
			for (String key : mSettingsComponents.keySet()) {
				newValues.put(key,getUserInputForOption(key));
			}
		} catch (Exception e) {
			// bail if any one didn't work
			Util.displayErrorMessage(PaneledReferenceSequenceDisplay.frame,"Error saving options: " + e.toString());
			return;
		}
		
		// save values to global settings
		for (Map.Entry<String,Object> e : newValues.entrySet()) {
			settings.put(e.getKey(),e.getValue());
		}
		
		Config.getConfig().saveSettings();
	}

	// get the type and component for this key from hashes and return the user's value
	private Object getUserInputForOption(Object pOption) {
		Object component = mSettingsComponents.get(pOption);
		Object example = settings.get(pOption);
			
		// go through the possibilities
		if (component instanceof JCheckBox) {
			return new Boolean(((JCheckBox)component).isSelected());
		} else if (component instanceof JComboBox) {
			if (example instanceof Class) {
				return ((GetVariantsDialog.AlgBoxOption)((JComboBox)component).getSelectedItem()).getAlg();
			} else {
				return ((JComboBox)component).getSelectedItem();
			}
		} else if (component instanceof JTextField) {
			if (example instanceof Integer) {
				return new Integer(((JTextField)component).getText());
			} else if (example instanceof Double) {
				return new Double (((JTextField)component).getText());
			} else {
				return ((JTextField)component).getText();
			}
		} else {
			throw new RuntimeException("Unkown option type for " + pOption + ": " + example.getClass());
		}
	}
	
	// revert the settings
	void revertSettings() {
		// loop over components
		Iterator<Map.Entry<String,JComponent>> sit = mSettingsComponents.entrySet().iterator();
		while (sit.hasNext()) {
			Map.Entry<String,JComponent> e = sit.next();
			JComponent component = e.getValue();
			
			if (component instanceof JCheckBox) {
				((JCheckBox)component).setSelected(((Boolean)settings.get(e.getKey())).booleanValue());
			} else if (component instanceof JTextField) {
				((JTextField)component).setText(settings.get(e.getKey()).toString());
			} else if (component instanceof JComboBox) {
				Object value = settings.get(e.getKey());
				if (value instanceof Class) {
					Class alg = (Class) value;
					try {
						GetVariantsDialog.AlgBoxOption opt = 
							new GetVariantsDialog.AlgBoxOption(Config.getAlgorithmName(alg),alg);
						((JComboBox)component).setSelectedItem(opt);
					} catch (Exception ex) {
						// ignore this error because it requires so many other things to go wrong
						System.err.println("Error getting alg name from Class object");
					}
				} else {
					((JComboBox)component).setSelectedItem(value);
				}
			}
		}
	}
	
	///////////////////////////
	// Object methods for creating interface
	///////////////////////////
	private void initialize(Map<String,Object> pOptionsHash) {
		// use options hash to put default values in settings
		amd.strainer.algs.Util.setDefaultsFromOptionsHash(pOptionsHash);
		
		// set the title
		setTitle("Settings for the selected segment strainer");
		
		// set up the main panel
		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
		mSettingsComponents = new HashMap<String,JComponent>();
		
		// add options
		for (Map.Entry<String,Object> e : pOptionsHash.entrySet()) {
			// get info from hash
			String key = e.getKey();
			Object value = e.getValue();
			
			// create component for option (values will be set later(in showDialog()) by calling revertSettings())
			JComponent component = null;

			if (key.equals(Config.INTERNAL_SEGMENT_STRAINER)) {
				// special case for algs that use another alg:
				// get up to date list of classes
				List<Class> algs = Config.getAlgorithmList();

				// get default class
				Class defaultAlg = null;
				if (value instanceof List) {
					defaultAlg = (Class) ((List)value).get(0);
				} else if (value instanceof Class) {
					defaultAlg = (Class) value;
				} else {
					try {
						defaultAlg = Class.forName(value.toString());
					} catch (Exception ex) {
						defaultAlg = algs.get(0);
					}
				}

				// set up dropdown list
				JComboBox box = new JComboBox();
				// initialize options
				for (Class alg : algs) {
					GetVariantsDialog.AlgBoxOption option = null;
					try {
						option = new GetVariantsDialog.AlgBoxOption(Config.getAlgorithmName(alg),alg);
					} catch (Exception ex) {
						Util.displayErrorMessage("Skiping algorithm:","Unable to get info for " + alg.toString() + ": " + ex.toString());
						continue;
					}
					box.addItem(option);
					if (alg == defaultAlg) {
						box.setSelectedItem(option);
					}
				}
				
				component = box;

				// remaining cases based on default value
			} else if (value instanceof Boolean) {
				component = new JCheckBox();
			} else if (value instanceof List) {
				JComboBox box = new JComboBox();
				// initialize options
				Iterator it = ((List)value).iterator();
				while (it.hasNext()) {
					box.addItem(it.next());
				}
				component = box;
			} else {
				component = new JTextField(12);
			}
//			System.out.println("k: " + key + " v: " + value + " c:" + component);
			mSettingsComponents.put(key,component);
			
			// put in panel with label
			JLabel label = new JLabel(key.toString());
			JPanel panel = new JPanel();
			panel.add(label);
			panel.add(component);
			
			// add this panel to main panel
			optionsPanel.add(panel);
		}

		// set up bottons
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(getSaveButton());
		buttonsPanel.add(getRevertButton());
		buttonsPanel.add(getCancelButton());
		
		// Add panels to dialog
		JPanel jContentPane = new JPanel();
		jContentPane.setLayout(new BorderLayout());
		jContentPane.add(optionsPanel, java.awt.BorderLayout.CENTER);
		jContentPane.add(buttonsPanel, java.awt.BorderLayout.SOUTH);

		this.setContentPane(jContentPane);
		this.pack();
	}
	
	private JButton getCancelButton() {
		Action cancelAction = new AbstractAction("Close") {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setVisible(false);
			}
		};
		
		JButton jCancelButton = new JButton(cancelAction);
		jCancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"),"esc");
		jCancelButton.getActionMap().put("esc",cancelAction);
		
		return jCancelButton;
	}
	
	private JButton getSaveButton() {
		Action saveAction = new AbstractAction("Save") {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				saveSettings();
			}
		};
		
		JButton jSaveButton = new JButton(saveAction);
		jSaveButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"),"ent");
		jSaveButton.getActionMap().put("ent",saveAction);
		jSaveButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RETURN"),"ret");
		jSaveButton.getActionMap().put("ret",saveAction);

		return jSaveButton;
	}

	private JButton getRevertButton() {
		Action saveAction = new AbstractAction("Revert") {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				revertSettings();
			}
		};
		
		JButton jRevertButton = new JButton(saveAction);
		return jRevertButton;
	}
}
