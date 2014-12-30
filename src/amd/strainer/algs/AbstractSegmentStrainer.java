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

package amd.strainer.algs;

import java.util.HashMap;
import java.util.Iterator;

import amd.strainer.display.actions.Task;
import amd.strainer.objects.Read;
import amd.strainer.objects.SequenceSegment;

public abstract class AbstractSegmentStrainer implements SegmentStrainer {

	/** Sub-sequence to be processed to find variants */
	protected SequenceSegment mSegment = null;
	/**
     * Define the reference sequence and range to process using a SequenceSegment object
     * @param pSegment A SequenceSegment
     */
	public void setSegment(SequenceSegment pSegment) { mSegment = pSegment; }
    /**
     * @return The SequenceSegment (to be) processed
     */
	public SequenceSegment getSegment() {return mSegment;}

	protected Task mTask = null;
	public void setTask(Task pTask) {
		mTask = pTask;
	}

	protected Iterator<Read> mReadIterator = null;
	public void setReads(Iterator<Read> pReads) {
		mReadIterator = pReads;
	}
	
	public abstract StrainerResult getStrains() throws SegmentStrainerException;
	public abstract HashMap<String, Object> getOptionsHash();
}
