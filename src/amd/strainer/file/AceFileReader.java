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
package amd.strainer.file;

import jaligner.SmithWatermanGotoh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import jme.tools.misc.CmdLineOptions;
import jme.tools.misc.CmdLineOptions.CmdLineParsingException;
import jme.tools.misc.CmdLineOptions.Option;
import amd.strainer.display.actions.AssemblyFileReader;
import amd.strainer.display.actions.Task;
import amd.strainer.objects.Alignment;
import amd.strainer.objects.Difference;
import amd.strainer.objects.Read;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * Reads an ACE file produced by phred/phrap and outputs ReferenceSequence
 * objects containing one contig each.
 * <P/>
 * The class can scan a file an return all headers, or a specific Contig, or
 * return an iterator that works it's way over the file returning one contig at
 * a time.
 * <p/>
 * The main() function takes 2 or 3 arguments: and ACE file name, an output file
 * prefix. and (optionally) a contig number. If no contig number is specified, a
 * pair of files will be created for every contig. The output files (for each
 * contig) are one FASTA file of contig sequence and one strainer-ready XML
 * file.
 * <p/>
 * The following assumptions are made about file structure:
 * <ul>
 * <li/>Each record begins with a contig line that begins with "CO". These are
 * parsed by AceContigHeader.java
 * <li/>Following the contig line are multiple lines of contig sequence. This is
 * terminated by an empty line.
 * <li/>Lines beginning with "AF" indicate read names and starting positions
 * <li/>Lines beginning with "RD" indicate read names and lenghts
 * <li/>Following each RD line are multiple lines of read sequence terminated by
 * a blank line.
 * <li/>QA indicate trim positions and are after RD and AF lines
 * <li/>The total number of reads(and hence AF,RD, and QA lines) is indicated in
 * the CO header
 * </ul>
 * 
 * @author jmeppley
 * 
 */
public class AceFileReader implements AssemblyFileReader {
	private File mAceFile = null;
	private boolean mRealign = false;
	private boolean mTrimEnds = true;
	private int contigCount = 0;

	/**
	 * @return the full path to the ace file
	 */
	public String getAssemblyFileName() {
		if (mAceFile != null) {
			return mAceFile.getAbsolutePath();
		} else {
			return "No file is set for thie AceFileReader!";
		}
	}

	/**
	 * @param pAceFile
	 *            The location of the phred/phrap output in ace file format
	 */
	public AceFileReader(File pAceFile) {
		mAceFile = pAceFile;
	}

	/**
	 * @param pAceFile
	 *            The location of the phred/phrap output in ace file format
	 * @param pRealign
	 *            If set to true, alignments are calculated from scratc instead
	 *            of trusting ACE file
	 */
	public AceFileReader(File pAceFile, boolean pRealign, boolean pTrimEnds) {
		mAceFile = pAceFile;
		mRealign = pRealign;
		mTrimEnds = pTrimEnds;
	}

	/**
	 * Scans file for contig headers and returns a hashmap of positions and
	 * ContigHeader objects
	 * 
	 * @return HashMap where each key is an Integer location in the file and
	 *         each value is a ContigHeader object
	 * @throws FileNotFound
	 *             exception if mAceFile does not exist
	 * @throws IOException
	 *             if mAceFile cannot be read from
	 */
	public HashMap<Integer, AceContigHeader> getContigList()
			throws FileNotFoundException, IOException {
		HashMap<Integer, AceContigHeader> contigs = new HashMap<Integer, AceContigHeader>();

		// Open file reader
		BufferedReader br = new BufferedReader(new FileReader(mAceFile));

		// loop over lines
		int linesRead = 0;
		String line = br.readLine();
		while (line != null) {
			linesRead++;
			// if line starts with CO,
			if (line.length() > 3 && line.substring(0, 3).equals("CO ")) {
				// this line is a contig header line
				// parse line into ContigHeader object
				AceContigHeader contigHeader = AceContigHeader
						.parseHeaderLine(line);
				// add to map
				contigs.put(new Integer(linesRead), contigHeader);
			}
			line = br.readLine();
		}
		br.close();
		return contigs;
	}

	/**
	 * Retrieves the specified contig from an ace file and returns a Reference
	 * Sequence object.
	 * 
	 * @param pLocation
	 *            line number of contig header
	 * @param pContig
	 *            The ContigHeader object parsed from indicated line
	 * @return A ReferenceSequecne object representing the contig with aligned
	 *         reads
	 * @throws FileNotFoundException
	 *             if ACE file not found
	 * @throws IOException
	 *             if ACE file unreadable
	 */
	public amd.strainer.objects.ReferenceSequence getContigDetails(
			int pLocation, AceContigHeader pContig)
			throws FileNotFoundException, IOException {
		// Open file reader
		BufferedReader br = new BufferedReader(new FileReader(mAceFile));
		// seek to location
		for (int i = 0; i < pLocation; i++) {
			br.readLine();
		}

		br.close();
		return createContigFromReader(br, pContig);
	}

	/**
	 * Retrieves the specified contig from an ace file and returns a Reference
	 * Sequence object.
	 * 
	 * @param pContigNumber
	 *            The contig number
	 * @param pTask
	 *            a Task that can be notified with status updates (may be null
	 *            for no updates)
	 * @return A ReferenceSequecne object representing the contig with aligned
	 *         reads
	 * @throws FileNotFoundException
	 *             if ACE file not found
	 * @throws IOException
	 *             if ACE file unreadable
	 * @throws InterruptedException
	 */
	public ReferenceSequence getContigDetailsFromNumber(long pContigNumber,
			Task pTask) throws IOException, InterruptedException {
		// Open file reader
		BufferedReader br = new BufferedReader(new FileReader(mAceFile));

		int lineCount = 0;
		if (pTask != null) {
			pTask.setMessage("Scanning ACE file...");
			// pTask.setLengthOfTask(pContigNumber);
			pTask.setCurrent(lineCount);
			// pTask.setCurrent(0);
		}

		// loop over lines
		String line = br.readLine();
		while (line != null) {
			lineCount++;
			// if line starts with CO,
			if (line.length() > 3 && line.substring(0, 3).equals("CO ")) {

				if (pTask != null && pTask.isInterrupted()) {
					br.close();
					throw new InterruptedException("Cancelled");
				}

				// this line is a contig header line
				// parse line into ContigHeader object
				AceContigHeader contigHeader = AceContigHeader
						.parseHeaderLine(line);

				if (pTask != null) {
					// contigs should mostly be in order...skip the occasional
					// oddity
					if (contigHeader.getNumber() <= pContigNumber) {
						pTask.setCurrent(lineCount);
					}
				}

				// DEBUG
				// int nu = contigHeader.getNumber();
				// if (nu%500==0) {
				// System.out.println("Contig number: " + nu);
				// }
				// end DEBUG

				// check if it's the correct contig
				if (contigHeader.getNumber() == pContigNumber) {
					if (pTask != null) {
						// set to zero for next phase
						pTask.setCurrent(0);
					}
					return createContigFromReader(br, contigHeader, pTask);
				}
			}
			line = br.readLine();
		}

		br.close();
		throw new ContigNotFoundException("Contig " + pContigNumber
				+ " not found in " + mAceFile.getAbsolutePath());
	}

	/**
	 * Retrieves the first contig with (at least) the specified number of reads
	 * from an ace file and returns a Reference Sequence object.
	 * 
	 * @param pNReads
	 *            The minimum number of reads
	 * @return A ReferenceSequecne object representing the contig with aligned
	 *         reads
	 * @throws FileNotFoundException
	 *             if ACE file not found
	 * @throws IOException
	 *             if ACE file unreadable
	 */
	public ReferenceSequence getFirstContigWithNReads(int pNReads)
			throws IOException {
		// Open file reader
		BufferedReader br = new BufferedReader(new FileReader(mAceFile));

		// loop over lines
		String line = br.readLine();
		while (line != null) {
			// if line starts with CO,
			if (line.length() > 3 && line.substring(0, 3).equals("CO ")) {
				// this line is a contig header line
				// parse line into ContigHeader object
				AceContigHeader contigHeader = AceContigHeader
						.parseHeaderLine(line);

				// DEBUG
				// int nu = contigHeader.getNumber();
				// if (nu%500==0) {
				// System.out.println("Contig number: " + nu);
				// }
				// end DEBUG

				// check if it's the correct contig
				if (contigHeader.getReadCount() >= pNReads) {
					return createContigFromReader(br, contigHeader);
				}
			}
			line = br.readLine();
		}

		br.close();
		throw new IOException("No Contigs found with " + pNReads + " in "
				+ mAceFile.getAbsolutePath());
	}

	/**
	 * @return Iterator where each returned object is a ReferenceSequence object
	 *         created from a contig record in the ace file
	 * @throws FileNotFoundException
	 *             if ACE file not found
	 * @throws IOException
	 *             if ACE file unreadable
	 */
	public Iterator<ReferenceSequence> getContigIterator()
			throws FileNotFoundException, IOException {
		return new AceFileContigIterator(mAceFile);
	}

	/**
	 * @param pNums
	 *            HashSet of contig numers to return (skip others)
	 * @return Iterator where each returned object is a ReferenceSequence object
	 *         created from a contig record in the ace file
	 * @throws FileNotFoundException
	 *             if ACE file not found
	 * @throws IOException
	 *             if ACE file unreadable
	 */
	public Iterator<ReferenceSequence> getContigIterator(Set<Long> pNnums)
			throws FileNotFoundException, IOException {
		return new AceFileContigIterator(mAceFile, pNnums);
	}

	/**
	 * Creates a ReferenceSequence, complete with reads and alignments, based on
	 * the next Contig in the ace (created by PhredPhrap) file that is currently
	 * being read by the passed FileReader.
	 * <p>
	 * The reader is assumed to have just read the contig header line. The next
	 * line should be the start of the contig sequence. This method counts reads
	 * and stops after the last QA line, leaving some lines left o read before
	 * the next contig.
	 * 
	 * @param br
	 *            BufferedReader connected to AceFile just after a contig header
	 * @param pContig
	 *            The ContigHeader object from the last line read
	 * @return a ReferenceSequence object with aligned reads
	 * @throws IOException
	 *             if file can't be read from
	 */
	ReferenceSequence createContigFromReader(BufferedReader br,
			AceContigHeader pContig) throws IOException {
		return createContigFromReader(br, pContig, null);
	}

	/**
	 * Creates a ReferenceSequence, complete with reads and alignments, based on
	 * the next Contig in the ace (created by PhredPhrap) file that is currently
	 * being read by the passed FileReader.
	 * <p>
	 * The reader is assumed to have just read the contig header line. The next
	 * line should be the start of the contig sequence. This method counts reads
	 * and stops after the last QA line, leaving some lines left o read before
	 * the next contig.
	 * 
	 * @param br
	 *            BufferedReader connected to AceFile just after a contig header
	 * @param pContig
	 *            The ContigHeader object from the last line read
	 * @param pTask
	 *            a Task object to recieve status updates (may be null for no
	 *            updates)
	 * @return a ReferenceSequence object with aligned reads
	 * @throws IOException
	 *             if file can't be read from
	 */
	ReferenceSequence createContigFromReader(BufferedReader br,
			AceContigHeader pContig, Task pTask) throws IOException {
		/*
		 * This is a complicated method, here's the outline: 1) contig a) read
		 * contig sequence b) create contig object 2) read headers (AF): simply
		 * read these (unprocessed) into an array 3) reads (one at a time) a)
		 * read sequence (RD) and trimmunt (QA) data b) combine with headers
		 * (AF) c) create read objects 4) clean up: make some final
		 * initializations
		 */

		// set up task with stating info
		if (pTask != null) {
			pTask.setCurrent(0);
			pTask.setMessage("Parsing Contig");
			pTask.setLengthOfTask(pContig.getReadCount());
		}

		// //////////////
		// CONTIG

		// loop over following lines
		String line = br.readLine();

		// set up variables for capturing data while looping
		boolean inContigSequence = true;
		StringBuffer contigSequence = new StringBuffer(pContig.getLength());

		// loop over lines to get contig sequence
		while (line != null && inContigSequence) {
			// get contig sequence
			if (line.length() > 0) {
				contigSequence.append(line);
			} else {
				// once we hit an empty line, the contig sequence is done
				inContigSequence = false;
			}
			// read next line before next trip through loop
			line = br.readLine();
		}

		// create contig
		// read and contig sequences have a "*" for any gap. these have to be
		// removed.

		// get array of gaps in Contig sequence
		// also remove them from sequence string and adjust length
		ArrayList<Integer> contigGaps = new ArrayList<Integer>();
		String newContigSequence = removeContigGaps(contigSequence.toString(),
				contigGaps);

		// if (mRealign) {
		// // create re-usable object for computing alignments if we'll need it
		// mAlignableContigSequence = new jaligner.Sequence(newContigSequence);
		// }

		// create ReferenceSequence object to represent contig
		ReferenceSequence contig = new ReferenceSequence();
		contig.setName(pContig.getName());
		contig.setId(contigCount++);
		contig.setBases(newContigSequence);
		contig.setLength(newContigSequence.length());

		// for now all reads grouped in one strain
		Strain strain = new Strain();

		// ////////////////////
		// READ HEADERS

		// AF lines are next (but sometimes there is a BQ quality section, just
		// skip to first AF
		while (line != null) {
			if (line.length() > 2 && line.substring(0, 3).equals("AF ")) {
				break;
			}
			// read next line before next trip through loop
			line = br.readLine();
		}

		// Map<String, Boolean> readDirs = new HashMap<String, Boolean>();
		// Map<String, Integer> readStarts = new HashMap<String, Integer>();
		List<Boolean> afDirs = new ArrayList<Boolean>();
		List<Integer> afStarts = new ArrayList<Integer>();
		// String readName;
		boolean dir;
		int rawStart;
		// loop over lines until we hit something that is not AF
		while (line != null) {
			if (line.length() > 2) {
				String first3 = line.substring(0, 3);
				// AF line indicates where the read is placed in the contig
				if (first3.equals("AF ")) {
					/*
					 * AF XYD11111.y1 U -67 AF XYG24419.g1 C 1106
					 */
					if (pTask != null) {
						// pTask.setCurrent(readDirs.size());
						pTask.setCurrent(afDirs.size());
						pTask.setMessage("Getting read headers");
					}

					try {
						// parse AF line
						StringTokenizer st = new StringTokenizer(line, " ");
						st.nextToken(); // skip "AF"
						// get read object using name (second token)
						st.nextToken();
						// get direction and start of alignment.
						dir = st.nextToken().charAt(0) == 'U';
						rawStart = Integer.parseInt(st.nextToken());
					} catch (Exception e) {
						System.err.println("Could not parse AF line: " + line);
						System.err.println(e.toString());
						throw new RuntimeException("Unable to parse ACE file");
					}
					// save line to be processed later
					// readDirs.put(readName, dir);
					// readStarts.put(readName, rawStart);
					afDirs.add(dir);
					afStarts.add(rawStart);
				} else if (first3.equals("RD ")) {
					// we made it to the RD lines, switch modes
					break;
				}
			}
			// read next line before next trip through loop
			line = br.readLine();
		}

		// ///////////////////
		// READS

		// RD and QA lines are last, process these as we go
		boolean inReadSequence = false;
		StringBuffer readSequence = null;
		Read currentRead = null;
		int readCount = 0;
		// hashmap used to find matepairs
		HashMap<String, Read> soloReads = new HashMap<String, Read>();

		// loop over lines
		while (line != null) {
			// CASE1: reading sequence for a read.
			if (inReadSequence) {
				// get read sequence
				if (line.length() > 0) {
					readSequence.append(line);
				} else {
					// an empty line marks the end of the sequence, do some
					// clean up
					inReadSequence = false;
				}

				// CASE2: look for specific lines with 2 character tags (RD, QA)
			} else if (line.length() > 2) {
				String first3 = line.substring(0, 3);

				// CASE 2.1: header line for read sequence data
				if (first3.equals("RD ")) {

					// update Task status (if a Task was supplied)
					if (pTask != null) {
						pTask.setCurrent(readCount);
						pTask.setMessage("Getting read sequences");
					}

					/*
					 * RD XYG4009.g2 748 0 4
					 * nnnggttggctggaccgtgatcgccattcttatGACGGATACCCATGCAT
					 * GTTCCAGCACATCCATTCTCTGTATCTTGTCGTACAGGGAATGAAATCTT ...
					 * CGGTCATATCTGTTACCAGTACGACCTTGACCCTCGCTTCATAgttcc
					 */

					// get info for read and store in currentRead variable
					currentRead = new Read();
					try {
						StringTokenizer st = new StringTokenizer(line, " ");
						st.nextToken();
						currentRead.setName(st.nextToken());
						currentRead.setLength(Integer.parseInt(st.nextToken()));
					} catch (Exception e) {
						System.err.println("Could not parse RD line: " + line);
						System.err.println(e.toString());
						throw new RuntimeException("Unable to parse ACE file");
					}
					// make sure we recognize the next few lines as sequence
					inReadSequence = true;
					readSequence = new StringBuffer();
					// CASE 2.2 QA line tells us where to trim the read
				} else if (first3.equals("QA ")) {
					// get trim positions from QA line
					// QA 32 744 32 746

					// QA line is the last bit of data for a read.
					// send read and QA line for processing
					try {
						boolean crdir = afDirs.get(readCount);
						int crstart = afStarts.get(readCount);
						readCount++;
						// boolean crdir =
						// readDirs.remove(currentRead.getName());
						// int crstart =
						// readStarts.remove(currentRead.getName());
						if (processAceFileRead(currentRead,
								readSequence.toString(), line, crdir, crstart,
								contigGaps, contig, contigSequence)) {

							// add read to contig
							currentRead.setId(readCount);
							contig.reads.put(currentRead.getIdInteger(),
									currentRead);

							// check for matePair
							Util.lookForMatePair(soloReads, currentRead);

							// add to strain
							strain.putRead(currentRead.getIdInteger(),
									currentRead);
						}
					} catch (NullPointerException npe) {
						System.err.println("Error: Skipping read: "
								+ currentRead.getName());
						npe.printStackTrace();
					}
					// if (readDirs.size() == 0) {
					if (afDirs.size() <= readCount) {
						// we've used all the AF lines...the contig is over
						break;
					}
				}
			}

			// read next line before next trip through loop
			line = br.readLine();
		}

		// //////////
		// CLEAN UP

		// initialize strain object for display
		// strain.initializeGraphics();
		strain.setAlignmentFromReads();
		contig.addStrainWithNoId(strain);

		// // set quality threshold:
		// // quality data is faked to be 10->low 40->good since ace file data
		// is yes or no
		// QualifiedDifference.setQualityThreshold((short)20);
		// contig.hasQualityData = true;

		// return ReferenceSequence object
		return contig;
	}

	private boolean processAceFileRead(Read read, String readSeq,
			String pQALine, boolean dir, int rawStart,
			List<Integer> contigGaps, ReferenceSequence contig,
			StringBuffer contigSequence) {

		int contigGapsPassed = 0;
		int trimmedStart, gappedStart, qStart, qEnd;
		StringBuffer cleanedSequence;

		try {
			if (mTrimEnds) {
				// parse QA line
				StringTokenizer st = new StringTokenizer(pQALine);
				st.nextToken(); // skip the "QA" token that identifies the line

				// take first reasonable pair of values
				qStart = Integer.parseInt(st.nextToken());
				qEnd = Integer.parseInt(st.nextToken());
			} else {
				qStart = 1;
				qEnd = readSeq.length();
			}
		} catch (Exception e) {
			System.err.println("Could not parse QA line: " + pQALine);
			System.err.println(e.toString());
			throw new RuntimeException("Unable to parse ACE file");
		}

		if (mRealign) {
			// calculate alignment fro scratch
			// prepare read sequence
			cleanedSequence = removeStarsFromSequence(readSeq.substring(
					qStart - 1, qEnd));
			jaligner.Sequence alignableReadSequence = new jaligner.Sequence(
					cleanedSequence.toString());
			// take segment of contig (padded by 5% of read length on each end)
			int contigAlignmentStart = rawStart + qStart - 1
					- (int) ((qEnd - qStart) / 20.0);
			if (contigAlignmentStart < 1) {
				contigAlignmentStart = 1;
			}
			int contigAlignmentEnd = rawStart + qStart
					+ (int) ((qEnd - qStart) * 1.05);
			if (contigAlignmentEnd > contigSequence.length()) {
				contigAlignmentEnd = contigSequence.length();
			}
			jaligner.Sequence alignableContigSequence = new jaligner.Sequence(
					removeStarsFromSequence(
							contigSequence.substring(contigAlignmentStart - 1,
									contigAlignmentEnd)).toString());
			// run alignment
			jaligner.Alignment alignment;
			alignment = SmithWatermanGotoh.align(alignableContigSequence,
					alignableReadSequence, Util.getNucleotideMatrix(),
					Util.GAP_START, Util.GAP_EXTEND);

			// create alignment object from jAlign output
			read.setAlignment(buildAlignmentFromJAlign(read, contig, alignment,
					qStart, contigAlignmentStart, contigGaps, dir));
		} else {
			// use alignment from ace file

			// adjust start point for QA trimming
			trimmedStart = rawStart + qStart - 1;

			// adjust start point for earlier gaps in contig
			contigGapsPassed = countContigGaps(contigGaps, trimmedStart);

			// deal with starts < 0
			if (trimmedStart < 1) {
				int adj = 1 - trimmedStart;
				qStart += adj;
				rawStart += adj;
				trimmedStart = 1;
			}

			// adjust start point for earlier gaps in contig
			gappedStart = trimmedStart - contigGapsPassed;

			// make sure read doesn't extend past end of contig
			int trimmedEnd = trimmedStart + qEnd - qStart;
			if (trimmedEnd > contigSequence.length()) {
				int adj = trimmedEnd - contigSequence.length();
				trimmedEnd -= adj;
				qEnd -= adj;
			}

			// compares read sequence to snippet of contig sequence and creates
			// Alignment object
			// gaps are removed from read sequence in this process
			try {
				Alignment a = getAlignmentFromAceData(read,
						readSeq.substring(qStart - 1, qEnd), contig,
						contigSequence.substring(trimmedStart - 1, trimmedEnd),
						gappedStart, dir, qStart, qEnd);
				read.setAlignment(a);
			} catch (StringIndexOutOfBoundsException sioobe) {
				System.err.println("Error getting alignment for: "
						+ read.getName() + " skipping...");
				System.err.println(sioobe.toString());
				System.err.println(pQALine);
				System.err.println("Contig length:" + contigSequence.length());
				System.err.println("Read length:" + readSeq.length());
				System.err.println("last start:" + trimmedStart);
				System.err.println("start:" + rawStart);
				System.err.println("qa start:" + qStart);
				System.err.println("qa end:" + qEnd);
				return false;
			}

			// remove *'s from read sequence and adjust length and alignment
			// position
			// ace file inserts stars for gaps, and includes them in position
			// counts
			// quality file does not, so we need to remove the stars for quality
			// scores to line up
			StringTokenizer stars = new StringTokenizer(readSeq, "*");
			cleanedSequence = new StringBuffer(readSeq.length());
			SequenceSegment aligOnRead = read.getAlignment()
					.getSequenceSegment2();
			while (stars.hasMoreTokens()) {
				String segment = stars.nextToken();

				// add star-less segment to cleaned sequence
				cleanedSequence.append(segment);

				// Adjust where alignment falls on read
				if (cleanedSequence.length() < aligOnRead.getEnd()) {
					aligOnRead.setEnd(aligOnRead.getEnd() - 1);
					if (cleanedSequence.length() < aligOnRead.getStart()) {
						aligOnRead.setStart(aligOnRead.getStart() - 1);
					}
				}
			}
		}

		read.setLength(cleanedSequence.length());
		// read.setBases(cleanedSequence.toString());

		return true;
	}

	// private int countCharOccurences(String s, char c) {
	// int count = 0;
	// for (int i = 0; i < s.length(); i++) {
	// if (s.charAt(i)==c) {
	// count++;
	// }
	// }
	// return count;
	// }

	private Alignment buildAlignmentFromJAlign(Read pRead,
			ReferenceSequence pRefSeq, jaligner.Alignment alignment,
			int pQStart, int aStart, List<Integer> pContigGaps, boolean pDir) {

		ArrayList<Difference> diffs = new ArrayList<Difference>();

		// adjust start point for earlier gaps in contig
		aStart -= countContigGaps(pContigGaps, aStart);

		// adjust start points if alignments aren't from beginning
		aStart += alignment.getStart1();
		pQStart += alignment.getStart2();

		int readGaps = 0;
		int contigGaps = 0;
		char[] readSequence = alignment.getSequence2();
		char[] contigSequence = alignment.getSequence1();
		for (int i = 0; i < readSequence.length; i++) {
			char rawRBase = readSequence[i];
			char rBase = Character.toLowerCase(rawRBase);
			char cBase = Character.toLowerCase(contigSequence[i]);
			if (rBase != cBase) {
				// figure out where we are
				int readPos = i - readGaps + pQStart; // actual position in read
				// segment
				int contigPos = i - contigGaps + 1; // actual position in contig
				// segment
				contigPos = contigPos + aStart - 1; // position in entire contig

				diffs.add(new Difference(contigPos, cBase, readPos, rawRBase));

				// update gap counts
				if (rBase == '-') {
					readGaps++;
				} else if (cBase == '-') {
					contigGaps++;
				}

				// } else if (rBase=='-'){
				// readGaps++;
				// contigGaps++;
			}
		}

		// calculate ends (adjusting for gaps)
		int matchLength = readSequence.length;
		int aend = aStart + matchLength - contigGaps - 1;
		int readEnd = pQStart + matchLength - readGaps - 1;

		// create alignment object
		SequenceSegment contigSegment = new SequenceSegment(pRefSeq, aStart,
				aend);
		SequenceSegment readSegment = new SequenceSegment(pRead, pQStart,
				readEnd);
		Alignment a = new Alignment(contigSegment, readSegment, pDir, diffs);
		a.score = (int) alignment.calculateScore();
		return a;
	}

	// Simply remove stars (*) from string and return cleaned string
	private StringBuffer removeStarsFromSequence(String readSeq) {
		StringTokenizer stars = new StringTokenizer(readSeq, "*");
		StringBuffer cleanedSequence = new StringBuffer(readSeq.length());
		while (stars.hasMoreTokens()) {
			String segment = stars.nextToken();
			// add star-less segment to cleaned sequence
			cleanedSequence.append(segment);
		}
		return cleanedSequence;
	}

	// get array of gaps in Contig sequence
	// also remove them from sequence string and adjust length
	private String removeContigGaps(String pGappedSequence,
			List<Integer> pEmptyGapList) {
		StringBuffer newSequence = new StringBuffer(pGappedSequence.length());

		// loop over bases and remove *'s
		int lastGap = -1;
		for (int i = 0; i < pGappedSequence.length(); i++) {
			char base = pGappedSequence.charAt(i);
			if (base == '*') {
				pEmptyGapList.add(new Integer(i + 1));
				newSequence.append(pGappedSequence.substring(lastGap + 1, i));
				lastGap = i;
			}
		}

		newSequence.append(pGappedSequence.substring(lastGap + 1));

		return newSequence.toString();
	}

	/* count gaps before position */
	private int countContigGaps(List<Integer> pGapPositions, int pTo) {
		int count = 0;

		for (int pos : pGapPositions) {
			if (pos < pTo) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}

	// compares read sequence to snippet of contig seuqnce and creates
	// Alignement object
	private Alignment getAlignmentFromAceData(Read pRead, String pReadSequence,
			ReferenceSequence pRefSeq, String pContigSequence, int pAStart,
			boolean pDir, int pQStart, int pQEnd) {
		if (pReadSequence.length() != pContigSequence.length()) {
			System.out.println("ReadSeq: " + pReadSequence.length()
					+ " :: ContigSeq: " + pContigSequence.length());
			throw new StringIndexOutOfBoundsException(
					"Sequecne lengths do not match!");
		}

		ArrayList<Difference> diffs = new ArrayList<Difference>();
		// StringBuffer newReadSequence = new
		// StringBuffer(readSequence.length());
		int readGaps = 0;
		int contigGaps = 0;
		// int lastReadGap = -1;
		for (int i = 0; i < pReadSequence.length(); i++) {
			char rawRBase = pReadSequence.charAt(i);
			char rBase = Character.toLowerCase(rawRBase);
			char cBase = Character.toLowerCase(pContigSequence.charAt(i));
			if (rBase != cBase) {
				// change *'s to -'s for compatability
				if (rBase == '*') {
					rBase = '-';
					rawRBase = '-';
				} else if (cBase == '*') {
					cBase = '-';
				}

				// figure out where we are
				int readPos = i - readGaps + pQStart; // actual position in read
				// segment
				int contigPos = i - contigGaps + 1; // actual position in contig
				// segment
				contigPos = contigPos + pAStart - 1; // position in entire
				// contig

				// // create Difference object with faked quality data
				// // ACE file has low quality bases as lower case.
				// // but no actual numbers...so lowercase->10 upper case->40
				// short quality = 40;
				// if (rawRBase==rBase) {
				// // base was lower case => poor quality
				// quality = 10;
				// }
				// diffs.add(new
				// QualifiedDifference(contigPos,cBase,readPos,rawRBase,quality));
				diffs.add(new Difference(contigPos, cBase, readPos, rawRBase));

				// update gap counts
				if (rBase == '-') {
					readGaps++;
				} else if (cBase == '-') {
					contigGaps++;
				}

			} else if (rBase == '*') {
				readGaps++;
				contigGaps++;
			}
		}

		// calculate ends (adjusting for gaps)
		int matchLength = pQEnd - pQStart + 1;
		int aend = pAStart + matchLength - contigGaps - 1;
		int readEnd = pQEnd - readGaps;

		// create alignment object
		SequenceSegment ess = new SequenceSegment(pRefSeq, pAStart, aend);
		SequenceSegment rss = new SequenceSegment(pRead, pQStart, readEnd);
		Alignment a = new Alignment(ess, rss, pDir, diffs);
		return a;
	}

	private class AceFileContigIterator implements Iterator<ReferenceSequence> {
		private AceContigHeader nextContig = null;
		private BufferedReader br = null;
		private final File mAceFile;
		private Set<Long> nums = null;

		AceFileContigIterator(File pAceFile) throws IOException,
				FileNotFoundException {
			mAceFile = pAceFile;

			// Open file reader
			br = new BufferedReader(new FileReader(mAceFile));

			// set up first contig
			queueNextContig();
		}

		AceFileContigIterator(File pAceFile, Set<Long> pNums)
				throws IOException, FileNotFoundException {
			mAceFile = pAceFile;
			nums = pNums;

			// set to null to indicate all contigs if no nums given
			if (nums.size() == 0) {
				nums = null;
			}

			// Open file reader
			br = new BufferedReader(new FileReader(mAceFile));

			System.out.println("finding next contig");

			// set up first contig
			queueNextContig();
		}

		private void queueNextContig() throws IOException {
			// loop to next contig
			String line = br.readLine();
			while (line != null) {
				if (line.length() > 3 && line.substring(0, 3).equals("CO ")) {
					nextContig = AceContigHeader.parseHeaderLine(line);

					if (nums != null) {
						// if there is alist of numbers only stop if this contig
						// is in the list
						if (nums.contains(new Long(nextContig.getNumber()))) {
							return;
						}
					} else {
						// otherwise, we'll take any contig
						return;
					}
				}
				line = br.readLine();
			}

			// if we get here, we didn't find another contig line
			nextContig = null;
			br.close();
		}

		public boolean hasNext() {
			return nextContig != null;
		}

		public ReferenceSequence next() {
			try {
				// get contig from file (skip if it breaks)
				while (true) {
					try {
						ReferenceSequence ret = createContigFromReader(br,
								nextContig);
						// before we leave, queue up next contig
						queueNextContig();

						// return current contig
						return ret;
					} catch (Exception e) {
						System.err.println("Could not parse "
								+ nextContig.getName());
						e.printStackTrace();
						queueNextContig();
						continue;
					}
				}
			} catch (IOException ex) {
				throw new RuntimeException(
						"Suddenly can't read from ace file: " + mAceFile);
			}
		}

		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException(
					"We're not modifying files here.");
		}
	}

	private static final String PROGRAM_NAME = "AceFileReader";
	private static final String ARGUMENT_USAGE = "[CONTIG_NUM_LIST]";

	/**
	 * Reads ace file and generates a pair of files for each contig, WARNING:
	 * this creates a LOT of files. If a third option is specified, it is
	 * assumed to be a contig number, and just that contig is generated.
	 * 
	 * @param args
	 *            Takes an ace file name and output file prefix. OutFile prefix
	 *            must refer to an existing directory.
	 */
	public static void main(String[] args) {
		// TODO:2 test new options

		Set<Option> opts = new HashSet<Option>();

		Option outputDirOption = new CmdLineOptions.Option("out-dir", 'o',
				true, "directory to put generated files in.");
		Option qualsFileOption = new CmdLineOptions.Option("qual-file", 'q',
				true, "the list of quality data");
		Option aceFileOption = new CmdLineOptions.Option("ace-file", 'a', true,
				"the assembly file to be parsed");

		opts.add(aceFileOption);
		opts.add(outputDirOption);
		opts.add(qualsFileOption);

		CmdLineOptions options = new CmdLineOptions(PROGRAM_NAME, opts,
				ARGUMENT_USAGE);

		// parse input
		Set<Long> cNums = new HashSet<Long>();
		try {
			// parse options
			String[] leftoverArgs = options.parse(args);

			// get list of contig numbers (if given)
			for (int i = 0; i < leftoverArgs.length; i++) {
				System.out.println("Leftover: " + leftoverArgs[i]);
				try {
					cNums.add(new Long(leftoverArgs[i]));
				} catch (NumberFormatException e) {
					System.err.println("Not an integer: " + leftoverArgs[i]
							+ "  ... skipping");
				}
			}
		} catch (CmdLineParsingException e1) {
			System.err.println(e1.toString());
			System.err.println(options.getUsageString());
			System.exit(-1);
		}

		// get option values
		String aceFileName = aceFileOption.getValue();
		String outputDir = outputDirOption.getValue();
		String qualFile = qualsFileOption.getValue();

		// do the work
		BatchAssemblyImportTask task = new BatchAssemblyImportTask(aceFileName,
				outputDir, qualFile, cNums);
		Object result = task.doStuff();
		if (result == null) {
			System.err.println(task.getErrorTitle());
			System.err.println(task.getMessage());
		}
	}
}
