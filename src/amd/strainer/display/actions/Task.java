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

import amd.strainer.display.PaneledReferenceSequenceDisplay;

public interface Task {
	/**
	 * Called to start the task.
	 */
	public void go();
	
	/**
	 * Called to find out how much work needs
	 * to be done.
	 */
	public int getLengthOfTask();
	
	
	
	/**
	 * Called to set how much work needs to be done
	 */
	public void setLengthOfTask(int pLength);
	
	/**
	 * Called to find out how much has been done.
	 */
	public int getCurrent();
	
	/**
	 * @return The object generated by running the task. (Will wait for completion, use isDone() to check first).
	 */
	public Object getResult();

	/**
	 * Called to set amount of work completed
	 */
	public void setCurrent(int pCurrent);
	
	/**
	 * Called from ProgressBarDemo to find out if the task has completed.
	 */
	public boolean isDone();
	
	/**
	 * Signals that the task shold be aborted.
	 */
	public void stop();
	
	/**
	 * @param pMessage String to be displayed in progress bar
	 */
	public void setMessage(String pMessage);
	
	/**
	 * Returns the most recent status message, or null
	 * if there is no current status message.
	 * <p>
	 * if current is non-zero, it is appended to the message. (as a percent, if the expected length is set)
	 */
	public String getMessage();

	/**
	 * Called by the timer thread if an 
	 * error is encountered and can be used to clean up, notify the user, and re-open a dialog.
	 */
	public void doOnError(PaneledReferenceSequenceDisplay pParent);

	/**
	 * @return any title associated with the error
	 */
	public String getErrorTitle();

	public boolean isInterrupted();
}
