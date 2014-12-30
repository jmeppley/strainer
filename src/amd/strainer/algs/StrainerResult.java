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

import java.util.Iterator;
import java.util.Set;

import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * This lays out the API expected for results from autostraining algorithms.
 * 
 * @author jmeppley
 * @see amd.strainer.algs.SegmentStrainer
 */
public interface StrainerResult {
	/**
	 * @return The number of strains (groups of reads) found 
	 */
	public int size();

	/**
	 * @return an Iterator over the found strains. elements are Strain objects
	 */
	public Iterator<Strain> getStrainIterator();

	/**
	 * @return all Strains as a Set
	 */
	public Set<Strain> getStrains();
	
	/**
	 * The sequence segment used as a reference. Needed to set strain seqeuences.
	 * @return pSegment SequenceSegment object defining range in reference seqeunce that was processed
	 */
	public SequenceSegment getSequenceSegment();
			
	/**
	 * Clear out arrays and close objects. Some algorithms may create objects with circular links that can prevent garbage collection.
	 */
	public void close();
		
	/**
	 * Set the sequence strings for the Strains that are part of this result. SequenceSegment must be set.
	 * 
	 * implementing methods must check the Config setting CONVERT_TO_AA
	 */
	public void setStrainsSequences();
	
}
