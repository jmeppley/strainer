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

import amd.strainer.display.SwingWorker;

/**
 * Sets up the basic Task architecture. Extending classes should only need to implement a constructor,
 * some member variables to store data, and the doStuff() method. The constructor might set expectedLength 
 * if the doStuff() method will update the current status as it goes. The doStuff() method
 * 
 * @author jmeppley
 */
public abstract class AbstractTask implements Task {
// TODO:4 add a registry for tasks, so UI can abort if there is a problem
	
	// the expected length in any units
	protected int expectedLength = -1;
	// the number of units completed
	protected int current = 0;
	// true if the task has finished
	protected boolean done = false;
	// the status message
	protected String message = null;
	// the error title (If this is set, a dialog will be displayed by SequenceDataLoader to notify user)
	protected String errorTitle = null;
	
	public boolean isInterrupted() {
		if (worker!=null) {
			return worker.isInterupted();
		} else {
			return false;
		}
	}
	
	// a cool utility from javasoft.com that helps run threads in Swing
	private SwingWorker worker = null;

	/**
	 * Called to start the task.
	 */
	public void go() {
		//instance = this;
		worker = new SwingWorker() {
			public Object construct() {
				current = 0;
				done = false;
//				canceled = false;
				return doStuff();
			}
		};
		worker.start();
	}
		
	/**
	 * Called to find out how much work needs
	 * to be done.
	 */
	public int getLengthOfTask() {
		return expectedLength;
	}

	public String getErrorTitle() {
		return errorTitle;
	}
	
	public void setLengthOfTask(int pRecords) {
		expectedLength = pRecords;
	}
		
	/**
	 * Called to find out how much has been done.
	 */
	public int getCurrent() {
		return current;
	}
	
	/**
	 * Sets how much work has been done
	 */
	public void setCurrent(int pCurrent) {
		current = pCurrent;
	}
	
	public void stop() {
		if (!done) {
			worker.interrupt();
			current = -1;
			message = "cancelled";
			errorTitle = message;
		}
	}
	
	/**
	 * Called from to find out if the task has completed.
	 */
	public boolean isDone() {
		return done;
	}
	
	public void setMessage(String pMessage) {
		message = pMessage;
	}
	
	/**
	 * Returns the most recent status message, or null
	 * if there is no current status message.
	 */
	public String getMessage() {
		return message;
	}

	/** REturns the generated object. This will wait for completion, so check isDone() first. */
	public Object getResult() {
		return worker.get();
	}
	
	/**
	 * This should be implemented by any extending classes. Here the work of the task is done
	 */
	protected abstract Object doStuff();
}
