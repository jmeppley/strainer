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
import java.util.List;

import amd.strainer.objects.Alignment;
import amd.strainer.objects.Difference;
import amd.strainer.objects.SequenceSegment;

/**
 * Creates an interface useful for comparing sequences.  Works somewhat like jave.util.Iterator, with
 * the major difference that you have to specifically increment the walker, so that multiple calls to
 * next() return the same object, unless increment() is called.  Additionally, next() returns a
 * amd.strainer.objects.Difference object and there are some shortcuts like nextPos() which 
 * calls next().getPosition1().
 * 
 * @author jmeppley
 *
 */
public class DiffWalker {
	private int start,end;
	private List exclusions;
	private Iterator diffIt;
	private boolean hasNext;
	private Difference next;
	private int nextPos;
	
	/**
	 * @param pA sequence alignment to work from (source of diffs)
	 * @param pStart skip everything before this position
	 * @param pEnd skip everything after this position
	 * @param pExclusions skip everything in any regions in this list
	 */
	public DiffWalker (Alignment pA, int pStart, int pEnd, List pExclusions) {
		start = pStart;
		end = pEnd;
		
		exclusions = pExclusions;
		
		diffIt = pA.getDiffs().iterator();
		increment();
	}

	/**
	 * Move the DiffWalker to the next Difference in the alignment
	 */
	public void increment() {
		while (diffIt.hasNext()) {
			next = (Difference) diffIt.next();
			nextPos = next.getPosition1();
			
			// if position outside range or in exclusions, keep looking, otherwise return next diff
			if (nextPos < start) {
				continue;
			}
			
			if (nextPos > end) {
				// we could just continue, but all the rest will also fail this test
				hasNext = false;
				next = null;
				nextPos = end+1;
				return;
			}

			// check exclusions size to reduce unneccesary calls
			if (exclusions.size()>0 && isExcluded(nextPos)) {
				continue;
			}
			
			// if we're still here, then use this diff
			hasNext=true;
			return;
		}
		hasNext = false;
		next = null;
		nextPos = end+1;
	}
	
	/**
	 * @return TRUE if increment() found another Difference, FALSE if the last increment() call reched the end of the alignment.
	 */
	public boolean hasNext() {
		return hasNext;
	}
	
	/**
	 * Returns the current Difference object. Does not increment the DiffWalker, successive calls to next() will return the same object. 
	 * Use increment() between calls to next() and hasNext().
	 * @return The current Difference object.
	 */
	public Difference next() {
		return next;
	}
	
	/**
	 * A shortcut to next().getPosition1();
	 * @return the position (relative to the reference sequence) of the Difference returned by next()
	 */
	public int nextPos() {
		return nextPos;
	}
	
	private boolean isExcluded(int pPos) {
		// we could keep track of where we are in exclusions and not have to loop over
		//  all of them each time, but I don't think it would be a huge savings.  These
		//  arrays should be short or null.
		
		for (int i=0; i<exclusions.size(); i++) {
			SequenceSegment excl = (SequenceSegment) exclusions.get(i);
			if (excl.containsPosition(pPos)) {
				return true;
			}
		}
		return false;
	}
}
