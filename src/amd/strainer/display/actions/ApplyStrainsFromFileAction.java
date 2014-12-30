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
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import amd.strainer.GlobalSettings;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.ReferenceSequenceDisplayComponent;
import amd.strainer.display.util.Util;

/**
 * Reads text file of grouped read names to assign strains.
 * 
 * @author jmeppley
 * 
 */
public class ApplyStrainsFromFileAction extends AbstractAction {

	ReferenceSequenceDisplayComponent canvas = null;
	// String iconLoc = "/toolbarButtonGraphics/general/Open16.gif";
	// URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);

	private final JFileChooser fc = new JFileChooser();
	File inFile = null;

	public ApplyStrainsFromFileAction(ReferenceSequenceDisplayComponent pCanvas) {
		super("Import read groups");
		putValue(
				SHORT_DESCRIPTION,
				"Group reads into strains based on a selected text file");
		canvas = pCanvas;
	}

	public void actionPerformed(ActionEvent e) {
		String cwd = GlobalSettings.getSetting(GlobalSettings.OUTPUT_DIR_KEY);
		if (cwd != null) {
			fc.setCurrentDirectory(new File(cwd));
		}

		// get file name from user
		int response = fc.showOpenDialog(PaneledReferenceSequenceDisplay.frame);
		if (response == JFileChooser.APPROVE_OPTION) {
			inFile = fc.getSelectedFile();
			GlobalSettings.putSetting(GlobalSettings.OUTPUT_DIR_KEY, fc
					.getCurrentDirectory()
					.getAbsolutePath());
		} else {
			System.err.println("user canceled export");
			return;
		}

		// read file
		try {
			amd.strainer.file.Util.applyStrainsFromFile(canvas, inFile);
		} catch (IOException ex) {
			Util.displayErrorMessage(
					PaneledReferenceSequenceDisplay.frame,
					"Error reading strains file (" + inFile.getAbsolutePath()
							+ "):" + ex.getMessage());
		}

	}
}
