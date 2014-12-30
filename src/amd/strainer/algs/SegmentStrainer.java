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

/**
 * Interface to be implemented by autostraining algorithms
 * <P>
 * Algorithms may be added to strainer as long as they implement this interface. Just compile them, insert them into strainer's
 * startup calsspath, and add them in the GetVariants dialog.
 * 
 * @author jmeppley
 * @see amd.strainer.algs.StrainerResult
 */
public interface SegmentStrainer {
	/**
	 * Constant used to indicate the combineStrains option in the settings hash map
	 */
	public static final String COMBINE_STRAINS = "Track all strain reads";

	/**
	 * @return The SequenceSegment (to be) processed
	 */
	public SequenceSegment getSegment();
	/**
     * Define the reference sequence and range to process using a SequenceSegment object
     * @param pSegment A SequenceSegment
     */
	public void setSegment(SequenceSegment pSegment);
	/**
	 * Indicate the task to be notified of progress updates
	 * @param pTask the Task object, may be null for no updates
	 */
	public void setTask(Task pTask);
	/**
	 * An iterator over the reads that are to be considered
	 * @param pReads Iterator<Reads>
	 */
	public void setReads(Iterator<Read> pReads);
    /**
     * @return The resulting Strains as a StrainerResult object
     */
    public StrainerResult getStrains() throws SegmentStrainerException;

    /**
     * @return The name of this SegmentStrainer
     */
    public String getName();
    /**
     * @return A brief description of how this SegmentStrainer works
     */
    public String getDescription();
    
    /**
     * Returns a map of options. Keys are names of options that can be configured (in Config.getSettings())
     * for this SegmentStrainer object. Values are one of:
     * <ul>
     * <li> A list of String values to be chosen from
     * <li> A default value in the expected type (String, Integer, Boolean, or Double)
     * </ul>
     * @return java.util.HashMap of options
     */
    public HashMap<String,Object> getOptionsHash();
}
