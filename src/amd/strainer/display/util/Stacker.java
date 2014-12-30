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
package amd.strainer.display.util;

import java.util.ArrayList;
import java.util.List;

import amd.strainer.objects.AlignedSequence;
import amd.strainer.objects.Clone;
import amd.strainer.objects.Readable;

/**
 * Contains static methods for figuring out how to place reads and strains on
 * the screen
 * 
 * @author jmeppley
 */
public class Stacker {
	/**
	 * Figures out how to best fit the sequences onto the canvas. The row
	 * variable is set for each sequenceses DisplayGeometry object. This version
	 * tries to stack as tightly as possible.
	 * 
	 * @param pAS
	 *            a Collection of AlignedSequence objects
	 * @return the number of rows used
	 */
	public static <U extends AlignedSequence> int stackPositions(List<U> pAS) {
		ArrayList<List<U>> posByRow = new ArrayList<List<U>>();

		for (U seq : pAS) {
			placeInOpenRow(seq, posByRow);
		}

		return posByRow.size();
	}

	/**
	 * Figures out how to best fit the sequences onto the canvas. The row
	 * variable is set for each sequences DisplayGeometry object. This version
	 * tries to stack Clone objects as tightly as possible and makes solo reads
	 * respect the sorting more than space savings.
	 * 
	 * @param pReadables
	 *            a Collection of AlignedSequence objects
	 * @return the number of rows used
	 */
	public static int stackReadables(List<Readable> pReadables) {
		ArrayList<List<AlignedSequence>> posByRow = new ArrayList<List<AlignedSequence>>();

		for (Readable readable : pReadables) {
			if (readable instanceof Clone) {
				placeInOpenRow(readable, posByRow);
			} else {
				placeInOpenRowStrictly(readable, posByRow);
			}
		}

		return posByRow.size();
	}

	private static <U extends AlignedSequence> void placeInOpenRow(U seq,
			ArrayList<List<U>> posByRow) {
		// System.out.println(pos);
		int row = 0;
		int height = seq.getDisplayGeometry().getHeight();
		int[] indexes = new int[height];
		while (true) {
			// System.out.print("row: ");
			// System.out.println(row);

			// make sure row list is long enough
			while (posByRow.size() < row + height) {
				posByRow.add(new ArrayList<U>());
			}

			// check for conflicts
			boolean conflict = false;
			for (int h = 0; h < height; h++) {
				try {
					indexes[h] = placePosInRow(seq, posByRow.get(row + h));
				} catch (SequenceOverlapException e) {
					// skip ahead to next row that occupying sequence isn't
					// in...
					row = e.conflictingSequence.getDisplayGeometry().getRow()
							+ e.conflictingSequence.getDisplayGeometry()
									.getHeight();
					conflict = true;

					// get out of for loop
					break;
				}
			}

			if (!conflict) {
				// we found an open row,
				// get out of while loop
				break;
			}
		}

		for (int h = 0; h < height; h++) {
			List<U> posInRow = posByRow.get(row + h);
			posInRow.add(indexes[h], seq);
		}
		seq.getDisplayGeometry().setRow(row);

	}

	/**
	 * Figures out how to best fit the sequences onto the canvas. The row
	 * variable is set for each sequenceses DisplayGeometry object. This version
	 * tries to respect the List's internal sorting above all else.
	 * 
	 * @param pAS
	 *            a Collection of AlignedSequence objects
	 * @return the number of rows used
	 */
	public static <U extends AlignedSequence> int stackPositionsStrictly(
			List<U> pAS) {
		ArrayList<List<U>> posByRow = new ArrayList<List<U>>();

		for (U seq : pAS) {
			placeInOpenRowStrictly(seq, posByRow);
		}

		return posByRow.size();
	}

	// find lowest row which this seq fits in but don't let it fall under a
	// previously placed seq.
	// return array of indexes indicating where in each row this seq goes. Last
	// element is the
	// starting row number. Index is -1 if there is a connflict
	private static <U extends AlignedSequence> void placeInOpenRowStrictly(
			U seq, ArrayList<List<U>> posByRow) {
		// System.out.println(pos);
		int rows = posByRow.size();
		int[] indexes = new int[rows];
		int bestRow = 0;
		for (int row = 0; row < rows; row++) {

			// CHECK THIS ROW FOR CONFLICTS
			try {
				indexes[row] = placePosInRow(seq, posByRow.get(row));
			} catch (SequenceOverlapException e) {
				// flag this row as bad
				indexes[row] = -1;
				// skip ahead to next row that conflicting sequence isn't in...
				row = e.conflictingSequence.getDisplayGeometry().getRow()
						+ e.conflictingSequence.getDisplayGeometry()
								.getHeight() - 1;
				// set best row (+/- 1 becuse loop autamatically increments
				// once)
				bestRow = row + 1;
			}

		}

		int height = seq.getDisplayGeometry().getHeight();
		int row = bestRow;

		for (int h = row; h < row + height; h++) {
			try {
				// get list for this row
				List<U> posInRow = posByRow.get(h);
				// add seq at indicated index
				posInRow.add(indexes[h], seq);
			} catch (IndexOutOfBoundsException e) {
				// if row hasn't been reached before, initialize it
				List<U> l = new ArrayList<U>();
				l.add(seq);
				posByRow.add(l);
			}
		}
		seq.getDisplayGeometry().setRow(row);
	}

	/**
	 * Searches list to see if any positions intersect the given position. Uses
	 * a halving algorithm to reduce the number of comparisons needed
	 * 
	 * @param pos
	 *            new sequence position (AlignedSequence object) to place in row
	 * @param posInRow
	 *            list of positions (ALignedSEquence objects) already in row
	 * @return the index pos should be placed in list
	 * @throws SequenceOverlapException
	 *             if pos intersects any existing positions
	 */
	private static <U extends AlignedSequence> int placePosInRow(U pos,
			List<U> posInRow) throws SequenceOverlapException {
		int high = posInRow.size();
		if (high == 0) {
			// empty list, pos will fit, return 0 as index
			return 0;
		}

		// set up variables
		int low = -1;
		int step = (high) / 2;
		int index = step;

		// recursively look at middle position
		while (true) {
			AlignedSequence current = posInRow.get(index);
			if (pos.getStart() < current.getEnd()) {
				if (pos.getEnd() > current.getStart()) {
					// sequences overlap... throw exception
					throw new SequenceOverlapException(current);
				} else {
					// pos lies to the left of current, go down
					high = index;
					step = (high - low) / 2;
					index = low + step;
				}
			} else {
				// pos lies to the right go up
				low = index;
				step = (high - low) / 2;
				index = low + step;
			}

			// high and low indexes have already been checked, we're done.
			if (index == low) {
				index++;
				break;
			} else if (index == high) {
				break;
			}
		}

		return index;
	}

}

class SequenceOverlapException extends Exception {
	private static final long serialVersionUID = -100918163938278078L;
	public AlignedSequence conflictingSequence = null;

	public SequenceOverlapException(AlignedSequence pPos) {
		conflictingSequence = pPos;
	}
}
