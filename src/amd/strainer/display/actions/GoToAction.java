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

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import amd.strainer.display.ReferenceSequenceDisplayComponent;
import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.objects.ReferenceSequence;

/**
 * GUI action that allows the user to jump to a location in the genome either by position or by looking for a 
 * specific Read or Gene. This Action calls GotoDialog.showDialog() which does all the work. This class exists to
 * fit in with the Swing framework.
 * 
 * @author jmeppley
 *
 */

public class GoToAction extends AbstractAction {
	static final long serialVersionUID = 101004;
	
	PaneledReferenceSequenceDisplay parent = null;
	ReferenceSequenceDisplayComponent canvas = null;
	
	String iconLoc = "/toolbarButtonGraphics/general/Find16.gif";
	URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);
	
	public GoToAction(PaneledReferenceSequenceDisplay pParent, ReferenceSequenceDisplayComponent pCanvas) {
		super("GoTo position");
		putValue(SHORT_DESCRIPTION,"GoTo position or find an object.");
		putValue(SMALL_ICON,new ImageIcon(iconURL));
		parent = pParent;
		canvas = pCanvas;
	}
	
	public void actionPerformed(ActionEvent e) {
		// get reference sequence object
		ReferenceSequence refSeq = parent.getReferenceSequence();
		if (refSeq==null) {
			amd.strainer.display.util.Util.displayErrorMessage(PaneledReferenceSequenceDisplay.frame,"No reference sequence is loaded!");
			return;
		}

		// ask user to choose name
		try {
			parent.disableAllActions();

			GoToDialog.showDialog(canvas);
			
			parent.enableAllActions();
			
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			parent.enableAllActions();
		}
	}
}

