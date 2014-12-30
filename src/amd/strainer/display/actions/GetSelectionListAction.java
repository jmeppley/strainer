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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import amd.strainer.GlobalSettings;
import amd.strainer.display.ReferenceSequenceDisplayComponent;
import amd.strainer.display.util.Util;

/**
 * Action to print out a file of all the selected read names. User can shoose to
 * include all matepairs of selected reads even if the mate pairs were not
 * selected.
 * 
 * @author jmeppley
 * 
 */
public class GetSelectionListAction extends AbstractAction {
	private static final long serialVersionUID = 5084459763801239443L;
	Component parent = null;
	ReferenceSequenceDisplayComponent canvas = null;
	JFileChooser fc = null;
	final Object[] options = { "Copy from consensus", "Fill with x's", "Cancel" };
	final String emptyMessage = "There are no reads selected.";
	final String emptyTitle = "Empty selection!";
	final String matePairQuestion = "Do you want to pull in missing mate pairs to the list?";
	final String matePairTitle = "find mate pairs?";
	final String overwriteQuestion = " exists.  Do you want to overwrite it?";
	final String overwriteTitle = "File exists.";

	public GetSelectionListAction(Component pParent,
			ReferenceSequenceDisplayComponent pCanvas) {
		super("Get Read List");
		putValue(SHORT_DESCRIPTION,
				"Save list of selected reads to a text file. ");
		parent = pParent;
		canvas = pCanvas;
		try {
			fc = new JFileChooser();
		} catch (java.security.AccessControlException ace) {
			setEnabled(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (!isEnabled()) {
			Util.displayErrorMessage(parent, "Action is disabled!");
			return;
		}

		if (canvas.isSelectionEmpty()) {
			// ask user if an empty file should be written
			JOptionPane.showMessageDialog(parent, emptyMessage, emptyTitle,
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		boolean findMatePairs = false;
		// ask user if missing mate pairs should be included
		int response = JOptionPane.showOptionDialog(parent, matePairQuestion,
				matePairTitle, JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (response == JOptionPane.YES_OPTION) {
			findMatePairs = true;
		} else if (response == JOptionPane.NO_OPTION) {
			findMatePairs = false;
		} else {
			return;
		}

		String cwd = GlobalSettings.getSetting(GlobalSettings.OUTPUT_DIR_KEY);
		if (cwd != null) {
			fc.setCurrentDirectory(new File(cwd));
		}

		int returnVal = fc.showSaveDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			GlobalSettings.putSetting(GlobalSettings.OUTPUT_DIR_KEY, fc
					.getCurrentDirectory().getAbsolutePath());

			System.out.println("saving to " + file.getAbsolutePath());

			// if the file exists, check with user before overwriting
			if (file.exists()) {
				response = JOptionPane.showOptionDialog(parent,
						overwriteQuestion, file.getName() + overwriteTitle,
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
						null, null, null);
				if (response != JOptionPane.YES_OPTION) {
					return;
				}
			}

			// write to file
			try {
				FileWriter fw = new FileWriter(file, false);
				PrintWriter pw = new PrintWriter(fw);

				String readstring = canvas.getSelectionList(findMatePairs);
				// System.out.println(readstring);
				pw.print(readstring);
				pw.close();
				fw.close();
			} catch (Exception ex) {
				Util.displayErrorMessage(parent, ex);
			}
		} else {
			System.out.println("Save command cancelled by user.");
		}
	}
}
