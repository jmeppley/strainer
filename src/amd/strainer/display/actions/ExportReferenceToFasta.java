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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import amd.strainer.GlobalSettings;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.ReferenceSequenceDisplayComponent;
import amd.strainer.display.util.Util;

/**
 * Writes current reference sequence to a Fasta file.
 * 
 * @author jmeppley
 * 
 */
public class ExportReferenceToFasta extends AbstractAction {

	ReferenceSequenceDisplayComponent canvas = null;
	// String iconLoc = "/toolbarButtonGraphics/general/Open16.gif";
	// URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);

	private final JFileChooser fc = new JFileChooser();
	File outFile = null;

	final String overwriteQuestion = " exists.  Do you want to overwrite it?";
	final String overwriteTitle = "File exists.";

	public ExportReferenceToFasta(ReferenceSequenceDisplayComponent pCanvas) {
		super("Export Reference");
		putValue(SHORT_DESCRIPTION, "Write reference sequence to a FASTA file");
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		// put the following question in a loop so we can come back to it if
		// something
		// goes wrong. We will exit manually (break) when we have what we need
		while (true) {
			String cwd = GlobalSettings
					.getSetting(GlobalSettings.OUTPUT_DIR_KEY);
			if (cwd != null) {
				fc.setCurrentDirectory(new File(cwd));
			}

			// get file name from user
			int response = fc
					.showSaveDialog(PaneledReferenceSequenceDisplay.frame);
			if (response == JFileChooser.APPROVE_OPTION) {
				outFile = fc.getSelectedFile();
				GlobalSettings.putSetting(GlobalSettings.OUTPUT_DIR_KEY, fc
						.getCurrentDirectory().getAbsolutePath());
			} else {
				System.err.println("user canceled export");
				return;
			}

			if (outFile.exists()) {
				response = JOptionPane.showOptionDialog(
						PaneledReferenceSequenceDisplay.frame,
						overwriteQuestion, outFile.getName() + overwriteTitle,
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
						null, null, null);
				if (response != JOptionPane.YES_OPTION) {
					continue;
				}
			}

			// if we're stil here, the file can be saved, so continue (ie leave
			// the loop)
			break;
		}

		// write file
		try {
			FileWriter fw = new FileWriter(outFile, false);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(">" + canvas.dData.referenceSequence.getName() + " "
					+ canvas.dData.referenceSequence.getLength());
			// Ideally, we'd break it up into multiple lines (TODO:3)
			pw.println(canvas.dData.referenceSequence.getBases());
			pw.close();
		} catch (IOException ex) {
			Util.displayErrorMessage(PaneledReferenceSequenceDisplay.frame,
					"Error writing to file (" + outFile.getAbsolutePath()
							+ "):" + ex.getMessage());
		}

	}
}
