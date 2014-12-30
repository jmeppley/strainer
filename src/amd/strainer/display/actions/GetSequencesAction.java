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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import amd.strainer.display.ReferenceSequenceDisplayComponent;
import amd.strainer.display.util.Util;

/**
 * Returns the composite sequence for a set of reads. User can choose the following:
 * <ul>
 * <il>to use selected reads or all visible reads
 * <li>to fill uncovered regions with "x" or from the reference sequence
 * <li>to limit returned sequence to range of selected gene
 * <li>to limit returned sequence to the displayed range
 * </ul>
 * @author jmeppley
 *
 */
public class GetSequencesAction extends AbstractAction {
	private static final long serialVersionUID = -6639592456640542812L;
	Component parent = null;
	ReferenceSequenceDisplayComponent canvas = null;    
//	JFileChooser fc = null;

	public GetSequencesAction(Component pParent, 
			ReferenceSequenceDisplayComponent pCanvas) {
		super("Get Sequence");
		putValue(SHORT_DESCRIPTION,"Save composite sequence of selected objects to a FASTA file.");
		parent = pParent;
		canvas = pCanvas;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (!isEnabled()) {
			Util.displayErrorMessage(parent,"Action is disabled!");
			return;
		}

		// dialog does all the work
		GetSequencesDialog.showDialog(canvas);
	}	
}
