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
 ***** END LICENSE BLOCK ***** */package amd.strainer.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import amd.strainer.display.actions.QualityDataDialog;
import amd.strainer.display.actions.Task;
import amd.strainer.display.util.Util;
import amd.strainer.objects.Difference;
import amd.strainer.objects.QualifiedDifference;
import amd.strainer.objects.Read;
import amd.strainer.objects.ReferenceSequence;

/**
 * Holds the quality data for a Read in an array of small integers.
 * <p>
 * Also comes with some static methods to read quality data from a file
 * <p>
 * Quality data can optionally be imported into any existing data set using a quality file from phred phrap and optionally a file that will
 *  indicate to strainer how to align qulaity data with (usually) trimmed reads. For now that file will be the untrimmed reads. I hope to add functionality to allow the use of an
 *  ace file or a simple text file listing read names and trim positions.
 * 
 * @author jmeppley
 * @see QualityDataDialog
 */
public class QualityData {
	/////////
	// Object variables
	
	private String mReadName;
	private short [] mQualities;
	
	//////////
	// Constructor
	
	/**
	 * Creates a new QualityData instance for the given read with the given values
	 * @param pReadName String: the name of the read
	 * @param pQualities array of shorts containing one value for each position in the read
	 */
	public QualityData(String pReadName, short [] pQualities) {
		mReadName = pReadName;
		mQualities = pQualities;
	}
	
	//////////
	// public object methods
	
	///////
	// accessors
	/**
	 * @return the name of the Read for which this QualityData object is storing quality values 
	 */
	public String getReadName() { return mReadName; }

	/**
	 * @param pPos the position in the read sequence (starts with 1 and ends with length)
	 * @return the quality value for that position
	 */
	public short getQualityForPosition(int pPos) {
		return mQualities[pPos - 1];
	}
	
	///////////
	// public static methods
	
	/**
	 * imports quality data into a referenceSEquence.
	 * @param pRefSeq The reference sequence to update
	 * @param pQualFile The file with quality data in it
	 * @param pAlignmentFile The file (optional) to indicate where the trimmed reads fall in the quality sequence
	 * @throws InterruptedException 
	 */
	public static void loadQualityData(ReferenceSequence pRefSeq, File pQualFile, File pAlignmentFile,Task pTask) throws FileNotFoundException, IOException, InterruptedException {
		// get map of read names to first trim pos
		Map<String,Integer> starts = readAlignmentData(pAlignmentFile, pRefSeq);

		// put all reads in map by name
		Map<String,Read> reads = amd.strainer.file.Util.getReadNameMap(pRefSeq);

		// do all the hard work
		addQualityToDiffs(reads, starts, pQualFile, pTask);

		// mark this as done
		pRefSeq.hasQualityData=true;
	}
	
	private static void addQualityToDiffs(Map<String,Read> pReads, Map<String,Integer> pStarts, File pQualFile, Task pTask) throws IOException, InterruptedException {
		// update task for progress bar
		int readCount = 0;
		if (pTask!=null) {
			pTask.setLengthOfTask(pReads.size());
			pTask.setCurrent(readCount);
		}

		//TODO:3 scan by line (instead of char by char) should be faster 
		//  (can we fudge line break as in perl (use > instead of (crlf))
		
		// scan qual file
		FileReader f = new FileReader(pQualFile);
		int r = f.read();
		Read read = null;
		StringBuffer characters = new StringBuffer(20);
		while (r>=0) {
			char c = (char) r;
			if (c=='>') {
				if (pTask!=null && pTask.isInterrupted()) {
					throw new InterruptedException("Interrupted!");
				}
				
				// new record
				characters.delete(0,characters.length());
				
				// get name
				r = f.read();
				while(r>=0 && !Character.isWhitespace((char)r)) {
					characters.append((char)r);
					r=f.read();
				}
				
				// get read from name
				read = pReads.remove(characters.toString());
				
				// if read is null (name doesn't match anything in this RefSeq)
				//  stop and look for next caret (carat?)
				if (read==null) {
					continue;
//				} else {
//					// DEBUG
//					System.out.print("reading quals for " + read.getName() + ":");
//					if (read.getName().equals("BGUC20047.g1")) {
//						System.out.println("found BGUC20047.g1");
//					}
//
				}

				// skip to next line (we are currently still on the header line)
				while (r>=0 && ((char)r)!='\n') {
					r = f.read();
				}
				
				// read diffs and update read as we go.
				List<Difference> diffs = read.getAlignment().getDiffs();
				int diffIndex = read.getAlignment().isForward() ? 0 : diffs.size()-1;

				// skip read, if no diffs
				if (diffs.size()<=0) {
					// DEBUG newline
					System.out.println();
					continue;
				}

				Difference diff = diffs.get(diffIndex);
				int position = read.getAlignment().isForward() ? 0 : read.getLength()+1;
				try {
					// sync qual data (if there is a start for this read)
					position = pStarts.remove(read.getName())-1;
				} catch (NullPointerException e) {
					// do nothing, let the default of 0 stand.
				}
				
				// clear string buffer
				characters.delete(0,characters.length());

				r=f.read();
				while (r>=0) {
					c = (char)r;
					// we're done at the next caret
					if (c=='>') break;
					
					if (Character.isWhitespace(c)) {
						if (characters.length()>0) {
							// between values
							short qual=-1;
							try {
								qual = Short.parseShort(characters.toString());
								// next (or previous) position
								if (read.getAlignment().isForward()) { position++; } else { position--; }
								if (position==diff.getPosition2()) {
									// DEBUG
//									System.out.print(" d:" + position);
									
									// update diff
									QualifiedDifference qd = new QualifiedDifference(diff,qual);
									diffs.remove(diffIndex);
									diffs.add(diffIndex,qd);
									if (read.getAlignment().isForward()) { diffIndex++; } else { diffIndex--; }
									if (diffIndex>=diffs.size() || diffIndex<0) break; 	// we're done, if no diffs left
									diff = diffs.get(diffIndex);
								}
							} catch (NumberFormatException e) {
								// not a value, skip
							}
							// clear string buffer
							characters.delete(0,characters.length());
						}
					} else {
						// if it's not white space, add to characters
						characters.append(c);
					}

					r = f.read();
				}

				// clear diffs of containing clone, so old Difference objecs don't stick around
				if (read.getClone()!=null) {
					read.getClone().getAlignment().setDiffs(null);
				}
				
				// update progress
				if (pTask!=null) {
					readCount++;
					pTask.setCurrent(readCount);
//					pTask.setCurrent(pTask.getLengthOfTask()-pReads.size());
				}

				// linefeed for DEBUG
				System.out.println();
				
				// don't read next char, we're here because we (maybe) saw the ">" already
				continue;
			} else {
				r = f.read();
			}
		}

		// did we miss any?
		System.out.println("leftovers:");
		System.out.println(pReads.keySet().toString());
		
	}
	
	/**
	 * loads quality data for a set of ReferenceSequences
	 * does not take alignment data, quality data must start at same point as reads.
	 * hasQualityData is never set to true. It is up to the caller to do so after a
	 * successful return
	 * 
	 * @param pRefSeqs Iterator over the reference sequences to update
	 * @param pQualFile The file with quality data in it
	 * @throws InterruptedException 
	 * @see java.util.Iterator
	 */
	public static void bulkLoadQualityData(Iterator<ReferenceSequence> pRefSeqs, File pQualFile, Task pTask) throws FileNotFoundException, IOException, InterruptedException {
		// build map of all reads
		System.out.println("collecting all reads");

		// get all reads as a Map of name to read
		HashMap<String,Read> reads = amd.strainer.file.Util.getReadNameMap(pRefSeqs);

		if (pTask!=null &&pTask.isInterrupted()) {
			throw new InterruptedException("Interrupted!");
		}
		
		// Quality data maust line up with reads in bulk mode:
		HashMap<String,Integer> starts = new HashMap<String,Integer>();

		// do the work
		addQualityToDiffs(reads,starts,pQualFile,pTask);
		
		System.out.println("finished reading quality data");

	}
	
	/**
	 * @param pAlignmentFile a file containing either a FASTA list of untrimed reads or a text list of read names and start positions separated by spaces
	 * @param pRefSeq The referenceSequence object containing trimmed reads
	 * @return HashMap mapping read names to first position included in trimmed read
	 * @throws FileNotFoundException if alignment file is missing
	 * @throws IOException if there is any parsing error
	 */
	private static Map<String,Integer> readAlignmentData(File pAlignmentFile, ReferenceSequence pRefSeq) throws FileNotFoundException, IOException {
		HashMap<String,Integer> starts = new HashMap<String,Integer>();
		
		// if file is null, return empty map
		if (pAlignmentFile==null) {
			return starts;
		}
		
		// set up reader and get first line
		BufferedReader br = new BufferedReader(new FileReader(pAlignmentFile));
		String line = br.readLine();

		// call appropriate subroutine based on first line
		if (line!=null) {
			if (line.charAt(0)=='>') {
				getStartsFromUntrimmedData(starts,pRefSeq,br,line);
			} else {
				try {
					getStartsFromTextFile(starts,br,line);
				} catch (Exception ex) {
					System.err.println("Error reading alignment file:");
					ex.printStackTrace();
					Util.displayErrorMessage("Error reading alignments","The alignments file must be either FASTA untrimed data or a list of read names and trim postions separated by spaces");
					throw new IOException(ex.toString());
				}
			}
		} else {
			throw new IOException("Empty file.");
		}

		return starts;
	}
	
	/**
	 * get (from a file) first position included in trimmed data for each read
	 * @param pStarts an empty HashMap to fill
	 * @param pBR a bufferedreader pointing at a file where each line is a readname startpos pair (one line has already been read)
	 * @param pFirstLine first line of the file
	 */
	private static void getStartsFromUntrimmedData(Map<String,Integer> pStarts, ReferenceSequence pRefSeq, BufferedReader pBR, String pFirstLine) throws IOException {

		String name = null;
		StringBuffer sequence = new StringBuffer();
		
		String line = pFirstLine;
		// for each record
		while (line != null) {
			if (line.trim().length()>0) {
				if (line.charAt(0)=='>') {
					if (name!= null) {
						// get trimmed read
						Read read = pRefSeq.reads.get(name);
						//  compare sequence to trimmed read sequence

						// shouldn't need this
						int bestStart = -1;
						int bestStartLength = -1;
						
						int start = 1;

						// try all the possible starts
						while (start<=sequence.length()) {

							// see if all the bases match if we start here
							boolean match = true;
							for (int i=0; i<read.getLength(); i++) {
								// get bases
								char trimmedBase = Character.toLowerCase(read.getBase(i+1));
								char untrimmedBase = Character.toLowerCase(sequence.charAt(i+start-1));
								
								// compare, but allow for x's or n's
								if (trimmedBase!=untrimmedBase &&
										trimmedBase!='n' &&
										trimmedBase!='x' &&
										untrimmedBase!='n' &&
										untrimmedBase!='x') {
									match=false;
									if (i>bestStartLength) {
										bestStartLength=i;
										bestStart=start;
									}
									i = read.getLength() + 1;
								}
							}
							
							if (match) {
								break;
							} else {
								start++;
							}
						}
						
						if (start>sequence.length()) {
							start = bestStart;
						}
						
						pStarts.put(name,new Integer(start));
						
					}
					
					// clear sequence
					sequence.delete(0,sequence.length());
					
					//   save new read name (everything between > and 1st space, initialize new vector of values
					int spaceIndex = line.indexOf(" ");
					if (spaceIndex==1 || line.length()==1) {
						// empty name
						name = null;
						System.err.println("Couldn't find name in fasta header: " + line);
					} else if (spaceIndex>1) {
						name=line.substring(1,spaceIndex);
					} else {
						name=line.substring(1,line.length());
					}
				} else {
					if (name!=null) {
						sequence.append(line.trim());
					}
				}
			}
			line= pBR.readLine();
		}
	}
	
	/**
	 * get (from a file) first position included in trimmed data for each read
	 * @param pStarts an empty HashMap to fill
	 * @param pBR a bufferedreader pointing at a file where each line is a readname startpos pair (one line has already been read)
	 * @param pFirstLine first line of the file
	 * @throws IOException 
	 */
	private static void getStartsFromTextFile(Map<String,Integer> pStarts, BufferedReader pBR, String pFirstLine) throws IOException {
		String line = pFirstLine;
		while (line!=null) {
			if (line.trim().length()>0) {
				// just take first two elements. If there is an error, just let the calling method catch the exception
				StringTokenizer st = new StringTokenizer(line);
				String name = st.nextToken();
				Integer start = new Integer(st.nextToken());
				pStarts.put(name,start);
			}
			line = pBR.readLine();
		}
	}
	
}
