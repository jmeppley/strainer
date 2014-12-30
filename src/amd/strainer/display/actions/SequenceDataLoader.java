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
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.Timer;

import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.util.Util;

/**
 * Handles the messy details of running a long task and managing the progress bar. This whole system is still quite convoluted.
 * It needs to be cleaned up. 
 * 
 * TODO:3 streamline progress bar usage and background tasks
 * 
 * @author jmeppley
 *
 */
public class SequenceDataLoader {
	
	PaneledReferenceSequenceDisplay mParent;
	Task task;
	Timer timer;
	static SequenceDataLoader currentProcess = null;
	final static int ONE_SECOND = 1000;

	public SequenceDataLoader(PaneledReferenceSequenceDisplay pParent, Task pTask) {
		mParent = pParent;
		task = pTask;
		currentProcess = this;
	}
	
	/**
	 * perform the task and start a timer
	 */
	public void load() {
		mParent.progressBar.setIndeterminate(true);
		mParent.disableAllActions();
		mParent.progressBar.setStringPainted(true);
		progressCancelAction.setEnabled(true);

		try {
			timer = new Timer(ONE_SECOND/5, this.new TimerListener());
			task.go();
			timer.start();
		} catch (RuntimeException e) {
			e.printStackTrace();
			mParent.progressBar.setValue(mParent.progressBar.getMinimum());
			mParent.progressBar.setIndeterminate(false);
			mParent.progressBar.setStringPainted(false);
			progressCancelAction.setEnabled(false);
			task.stop();
			timer.stop();
		}
	}
	
	/**
	 * The actionPerformed method in this class is called each time the Timer
	 * "goes off".
	 */
	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (task.getLengthOfTask() > 0) {
				if(mParent.progressBar.isIndeterminate()) {
					mParent.progressBar.setIndeterminate(false);
					mParent.progressBar.setMaximum(task.getLengthOfTask());
					mParent.progressBar.setMinimum(0);
				}
			} else if (!mParent.progressBar.isIndeterminate()) {
				mParent.progressBar.setIndeterminate(true);
				mParent.progressBar.setMaximum(0);
				mParent.progressBar.setMinimum(0);
			}

			// TODO:3 get messages to print when indeterminate is true

			StringBuffer message = new StringBuffer(task.getMessage());
			int current = task.getCurrent();
			int expectedLength = task.getLengthOfTask();
			mParent.progressBar.setValue(current);
			if (current>0) {
				message.append(": ");
				if (expectedLength>0) {
					message.append(100*current/expectedLength).append("%");
				} else { 
					message.append(current);
				}
			}
			mParent.progressBar.setString(message.toString());

			if (task.isDone()) {
				mParent.progressBar.setValue(mParent.progressBar.getMinimum());
				mParent.progressBar.setIndeterminate(false);
				mParent.progressBar.setStringPainted(false);
				mParent.enableAllActions();
				progressCancelAction.setEnabled(false);
				timer.stop();
			}

			if (task.getCurrent() < 0) {
				// died foer some reason, close objects
				// System.out.println("killing task and timer");
				mParent.progressBar.setValue(mParent.progressBar.getMinimum());
				mParent.progressBar.setIndeterminate(false);
				mParent.progressBar.setStringPainted(false);
				mParent.enableAllActions();
				progressCancelAction.setEnabled(false);
				task.stop();
				timer.stop();
				
				// notify user
				if (task.getErrorTitle()!=null) {
					Util.displayErrorMessage(task.getErrorTitle(),task.getMessage());
				}
				
				// let task do any final clean-up things
				task.doOnError(mParent);
			}
		}
	}

	private static Action progressCancelAction = null;
	public static Action getProgressCancelAction() {
		if (progressCancelAction==null) {
			progressCancelAction = new ProgressCancelAction();
		}
		return progressCancelAction;
	}
	
	private static class ProgressCancelAction extends AbstractAction {
//		private PaneledReferenceSequenceDisplay mParent;
		String iconLoc = "/toolbarButtonGraphics/general/Stop16.gif";
		URL iconURL = PaneledReferenceSequenceDisplay.class.getResource(iconLoc);

		ProgressCancelAction() {
//			super("Cancel Load");
			putValue(SHORT_DESCRIPTION,"Abort currently running process");
			putValue(SMALL_ICON,new ImageIcon(iconURL));
			setEnabled(false);
//			mParent = p;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			currentProcess.task.stop();
		}
		
	}
}
