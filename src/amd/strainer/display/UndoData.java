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
 ***** END LICENSE BLOCK ***** */package amd.strainer.display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;

import amd.strainer.objects.Clone;
import amd.strainer.objects.Read;
import amd.strainer.objects.Readable;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.Strain;

/**
 * Stores actions so that they can be reversed.
 * @author jmeppley
 *
 */
public class UndoData {
	ReferenceSequence mRefSeq = null;
	
	private List<Move> undoMoves = new ArrayList<Move>();
	private List<Move> redoMoves = new ArrayList<Move>();
	private StrainMove currentMove = null;
	
	/**
	 * @param pRefSeq the current reference sequence
	 */
	public UndoData(ReferenceSequence pRefSeq) {
		mRefSeq = pRefSeq;
	}

	public void clear() {
		undoMoves.clear();
		redoMoves.clear();
	}
	
	public boolean hasUndoMoves() {
		return undoMoves.size()>0;
	}
	
	public boolean hasRedoMoves() {
		return redoMoves.size()>0;
	}
	
	/**
	 * undo last move
	 */
	public void undo() {
		undo(1);
	}
	
	/**
	 * redo last move
	 */
	public void redo() {
		redo(1);
	}

	/**
	 * undo last N moves
	 * @param pMoveCount number of moves to undo
	 */
	public void undo(int pMoveCount) {
		//DEBUG for debugging purposes
//		System.err.println("Checking for empty strains before undo");
//		checkForEmptyStrains();
		
		while (pMoveCount>0 && undoMoves.size()>0) {
			Move lastMove = undoMoves.remove(undoMoves.size()-1);
			lastMove.undo();
			pMoveCount--;
			redoMoves.add(lastMove);
		}
		
		//DEBUG for debugging purposes
		System.err.println("Checking for empty strains after undo");
//		checkForEmptyStrains();
	}

	/**
	 * redo N moves
	 * @param pMoveCount number of moves to redo
	 */
	public void redo(int pMoveCount) {
		while (pMoveCount>0 && redoMoves.size()>0) {
			Move lastMove = redoMoves.remove(redoMoves.size()-1);
			lastMove.redo();
			undoMoves.add(lastMove);
			pMoveCount--;
		}
	}

	/**
	 * Record that a readable object has had its recomb status changed
	 * @param pReadable
	 */
	public void newRecombMove(Readable pReadable) {
		undoMoves.add(new RecombMove(pReadable));
		redoMoves.clear();
	}
	
	/**
	 * Record that a collection of readable objects has had their recomb status changed
	 * @param pReadable
	 */
	public void newBulkRecombMove(Collection<Readable>pReadables) {
		undoMoves.add(new BulkRecombMove(pReadables));
		redoMoves.clear();
	}

	/**
	 * Start recording a complex move
	 */
	public void startMove() {
		if (currentMove!=null) {
			throw new ConcurrentModificationException("Two processes acceding undo system simultaneously, or undo sesssion left open.");
		}
		
		currentMove = new StrainMove();
	}
	
	/**
	 * finish recording a complex move
	 */
	public void endMove() {
		if (currentMove==null) {
			throw new ConcurrentModificationException("No currently open move to close.");
		}
		
		undoMoves.add(currentMove);
		currentMove = null;
		redoMoves.clear();
		
//		//DEBUG for debugging purposes
//		System.err.println("Checking for empty strains after new undo move created");
//		checkForEmptyStrains();
	}

//	private void checkForEmptyStrains() {
//		for (Strain s : mRefSeq.strains.values()) {
//			if (s.getSize()==0) {
//				System.err.println("Strain " + s.getId() + " is empty!!");
//			}
//		}
//	}
	
	/**
	 * Add an action to a complex move
	 * @param pReadable Readable object that was moved
	 * @param pOldStrainId id of the strain it came from
	 */
	public void addToMove(Readable pReadable, Integer pOldStrainId) {
		if (currentMove==null) {
			throw new ConcurrentModificationException("No currently open move to extend.");
		}

		currentMove.addToMove(pReadable, pOldStrainId);
	}
	
	// define the move interface (it's very simple)
	private interface Move {
		public void undo();
		public void redo();
	}

	// a move that changes strain associations
	private class StrainMove implements Move {
		private ArrayList<Readable> reads = new ArrayList<Readable>();
		private ArrayList<Integer> oldStrains = new ArrayList<Integer>();
		private StrainMove redoMove = null;
		
		public StrainMove() {}
		
		public void addToMove(Readable pReadable, Integer pOldStrainId) {
			reads.add(pReadable);
			oldStrains.add(pOldStrainId);
		}
		
		public void undo() {
			// keep track of what we do for redo
			redoMove = new StrainMove();
			
			// create map to keep track of changed strains	
			HashMap<Integer,Strain> modifiedStrains = new HashMap<Integer,Strain>();
			
			// loop over Reads and their old strain assignments
			for (int i = reads.size()-1; i>=0; i--) {
				// get objects from arrays
				Readable r = reads.get(i);
				Integer oldId = oldStrains.get(i);
								
				// move read back into old strain
								
				// try to find old strain
				Strain oldStrain = mRefSeq.strains.get(oldId);
				if (oldStrain==null) {
					// oldStrain was emptied, must recreate
					oldStrain = new Strain();
					oldStrain.setId(oldId.intValue());
					oldStrain.initializeGraphics();
					mRefSeq.strains.put(oldId,oldStrain);
				}
				
				// get strain that read is in now
				Strain newStrain = r.getStrain();

//				System.out.println("Read " + r.getName() + "moved back to " + oldId + " from " + newStrain.getId());

				// remove read from newStrain and place in oldStrain
				newStrain.removeReadable(r);
				oldStrain.putReadable(r.getIdInteger(),r);

				// record that we modified these strains
				modifiedStrains.put(oldId,oldStrain);
				modifiedStrains.put(newStrain.getIdInteger(),newStrain);
				redoMove.addToMove(r,newStrain.getIdInteger());
			}
			
			// loop over modified strains and update alignments
			for (Strain s : modifiedStrains.values()) {
				if (s.getSize()==0) {
					// remove if size is now zero
					mRefSeq.strains.remove(s.getIdInteger());
				} else {
					// caclulate alignment
					s.setAlignmentFromReads();
				}
			}
		}
		
		public void redo() {
			redoMove.undo();
		}
	}
	
	private class RecombMove implements Move {
		Readable mReadable = null;
		
		public RecombMove(Readable pReadable) {
			mReadable = pReadable;
		}
		
		public void undo() {
			toggleReadableRecombinant(mReadable);
		}

		public void redo() {
			toggleReadableRecombinant(mReadable);
		}
	}

	private void toggleReadableRecombinant(Readable pReadable) {
		// fix status
		pReadable.toggleRecombinant();

		// make sure colors are redrawn
		if (pReadable instanceof Clone) {
			Clone clone = (Clone) pReadable;
			clone.recalcColors();
			clone.reads[0].recalcColors();
			clone.reads[1].recalcColors();
		} else {
			Read read = (Read) pReadable;
			read.recalcColors();
			if (read.getMatePair()!=null) {
				read.getClone().recalcColors();
				read.getMatePair().recalcColors();
			}
		}
	}
	
	private class BulkRecombMove implements Move {
		Collection<Readable> mReadables = null;
		
		public BulkRecombMove(Collection<Readable> pReadables) {
			mReadables = pReadables;
		}
		
		public void undo() {
			for (Readable readable : mReadables) {
				toggleReadableRecombinant(readable);
			}
		}

		public void redo() {
			// toggle is same in either direction
			undo();
		}
	}
}
