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
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import jme.tools.misc.CmdLineOptions;
import jme.tools.misc.CmdLineOptions.CmdLineParsingException;
import jme.tools.misc.CmdLineOptions.Option;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import amd.strainer.display.actions.AssemblyFileReader;
import amd.strainer.display.actions.Task;
import amd.strainer.objects.Alignment;
import amd.strainer.objects.Difference;
import amd.strainer.objects.Read;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * Reads an CAF file produced by an assembly program and outputs
 * ReferenceSequence objects containing one contig each. It was developed using
 * the output of Mira 4.0.3.
 * <P/>
 * The class can scan a file an return all headers, or a specific Contig, or
 * return an iterator that works it's way over the file returning one contig at
 * a time.
 * <p/>
 * The main() function takes 2 or 3 arguments: a CAF file name, an output file
 * prefix. and (optionally) a contig number. If no contig number is specified, a
 * pair of files will be created for every contig. The output files (for each
 * contig) are one FASTA file of contig sequence and one strainer-ready XML
 * file.
 * <p/>
 * The following assumptions are made about file structure:
 * <ul>
 * <li/>Each record begins with a sequence ID line (EG:
 * "Sequence : SAR324_J029_c1") which is immediately followed by a line that
 * just says "Is_contig" or "Is_read" contigs and reads are interspersed, though
 * it seems that reads typically precede their contig
 * <li/>Following these lines are multiple tags. The one we are interested
 * differ by sequence typ in reads, we are interested in the template name and
 * assembly direction: "Template "XXX"" and "Strand Forward"
 * <li/>In contigs, we need the assembled locations of the reads: (EG:
 * "Assembled_from HOT237_0500m_32502512/1 1 260 28 287")
 * <li/>Each "Sequence" record is has DNA and Quality sections starting with a
 * header line (EG: "DNA : DNA : SAR324_J029_c1" and
 * "BaseQuality : SAR324_J029_c1") followed by lines of sequence terminated by a
 * blank line. Quality scores are space-separated integers.
 * </ul>
 * 
 * @author jmeppley
 * 
 */
public class CAFFileReader implements AssemblyFileReader {

	private File mCafFile = null;
	private boolean mRealign = false;
	private boolean mTrimEnds = true;
	private boolean mSaveQualityData = false;
	private int contigCount = 0;

	/**
	 * @return the full path to the ace file
	 */
	public String getAssemblyFileName() {
		if (mCafFile != null) {
			return mCafFile.getAbsolutePath();
		} else {
			return "No file is set for thie CAFFileReader!";
		}
	}

	/**
	 * @param pCafFile
	 *            The location of the phred/phrap output in ace file format
	 */
	public CAFFileReader(File pCafFile) {
		mCafFile = pCafFile;
	}

	/**
	 * @param pCafFile
	 *            The location of the phred/phrap output in ace file format
	 * @param pRealign
	 *            If set to true, alignments are calculated from scratch instead
	 *            of trusting ACE file
	 */
	public CAFFileReader(File pCafFile, boolean pRealign, boolean pTrimEnds,
			boolean pSaveQualityData) {
		mCafFile = pCafFile;
		mRealign = pRealign;
		mTrimEnds = pTrimEnds;
		mSaveQualityData = pSaveQualityData;
	}

	/**
	 * Retrieves the specified contig from an ace file and returns a Reference
	 * Sequence object.
	 * 
	 * @param pLocation
	 *            line number of contig header
	 * @param pContig
	 *            The CAFContigHeader object parsed from indicated line
	 * @return A ReferenceSequecne object representing the contig with aligned
	 *         reads
	 * @throws FileNotFoundException
	 *             if CAF file not found
	 * @throws IOException
	 *             if CAF file unreadable
	 */
	public amd.strainer.objects.ReferenceSequence getContigDetails(
			int pLocation, CAFContigHeader pContig)
			throws FileNotFoundException, IOException {
		// Open file reader
		BufferedReader br = new BufferedReader(new FileReader(mCafFile));
		// seek to location
		for (int i = 0; i < pLocation; i++) {
			br.readLine();
		}

		return createContigFromReader(br, pContig, null);
	}

	/**
	 * Retrieves the specified contig from a CAF file and returns a Reference
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
	 *             if CAF file not found
	 * @throws IOException
	 *             if CAF file unreadable
	 * @throws InterruptedException
	 */
	public ReferenceSequence getContigDetailsFromNumber(long pContigNumber,
			Task pTask) throws IOException, InterruptedException {

		Set<Long> contigNumsToReturn = new HashSet<Long>();
		contigNumsToReturn.add(pContigNumber);
		Iterator<ReferenceSequence> contigIterator = getContigIterator(contigNumsToReturn);
		if (contigIterator.hasNext()) {
			return contigIterator.next();
		} else {
			throw new ContigNotFoundException("Contig " + pContigNumber
					+ " not found in " + mCafFile.getAbsolutePath());
		}

		// // Open file reader
		// BufferedReader br = new BufferedReader(new FileReader(mCafFile));
		//
		// // Store reads as we go
		// Map<String, RawRead> readMap = new HashMap<String, RawRead>();
		// Map<String, String> dnaSequenceMap = new HashMap<String, String>();
		// Map<String, int[]> qualScoreMap = new HashMap<String, int[]>();
		//
		// int lineCount = 0;
		// if (pTask != null) {
		// pTask.setMessage("Scanning CAF file...");
		// // pTask.setLengthOfTask(pContigNumber);
		// pTask.setCurrent(lineCount);
		// // pTask.setCurrent(0);
		// }
		//
		// // loop over lines
		// String line = br.readLine();
		// while (line != null) {
		// lineCount++;
		//
		// // find DNA lines
		// if (line.length() > 4 && line.substring(0, 4).equals("DNA ")) {
		// String sequenceName = getSequenceNameFromLine(line);
		// String dnaSequence = getSequenceDNA(br);
		// if (readMap.containsKey(sequenceName)) {
		// readMap.get(sequenceName).setSequence(dnaSequence);
		// } else {
		// dnaSequenceMap.put(sequenceName, dnaSequence);
		// }
		// } else if (line.length() > 12
		// && line.substring(0, 12).equals("BaseQuality ")
		// && mSaveQualityData) {
		// String sequenceName = getSequenceNameFromLine(line);
		// int[] qualScores = getQualScores(br,
		// dnaSequenceMap.get(sequenceName).length());
		// if (readMap.containsKey(sequenceName)) {
		// readMap.get(sequenceName).setQuality(qualScores);
		// } else {
		// qualScoreMap.put(sequenceName, qualScores);
		// }
		// } else if (line.length() > 9
		// && line.substring(0, 9).equals("Sequence ")) {
		// // this line is a sequence header line
		// StringTokenizer st = new StringTokenizer(line, " ");
		// st.nextToken(); // skip "Sequence"
		// st.nextToken(); // skip ":"
		//
		// // get name (second token)
		// String sequenceName = st.nextToken().toString();
		//
		// // read or contig
		// String type = br.readLine().trim();
		// if (type.equals("Is_read")) {
		// RawRead rawRead = parseCAFRead(sequenceName, br,
		// mSaveQualityData);
		// String dna = dnaSequenceMap.remove(sequenceName);
		// if (dna != null) {
		// rawRead.setSequence(dna);
		// }
		// int[] qualityScores = qualScoreMap
		// .remove(rawRead.getName());
		// if (qualityScores != null) {
		// rawRead.setQuality(qualityScores);
		// }
		// readMap.put(sequenceName, rawRead);
		// } else if (type.equals("Is_contig")) {
		// CAFContigHeader contigHeader = CAFContigHeader
		// .parseHeaderLine(line, readMap);
		//
		// if (pTask != null) {
		// // contigs should mostly be in order...skip the
		// // occasional
		// // oddity
		// if (contigHeader.getNumber() <= pContigNumber) {
		// pTask.setCurrent(lineCount);
		// }
		// }
		//
		// // check if it's the correct contig
		// if (contigHeader.getNumber() == pContigNumber) {
		// if (pTask != null) {
		// // set to zero for next phase
		// pTask.setCurrent(0);
		// }
		//
		// String contigName = contigHeader.getName();
		// String contigDNASequence = dnaSequenceMap
		// .remove(contigName);
		// return createContigFromReader(br, contigHeader,
		// contigDNASequence, pTask);
		// } else {
		// // clear DNA and qual arrays
		// dnaSequenceMap.clear();
		// qualScoreMap.clear();
		// }
		// }
		// }
		// line = br.readLine();
		// }
		//
		// br.close();
		// throw new ContigNotFoundException("Contig " + pContigNumber
		// + " not found in " + mCafFile.getAbsolutePath());
	}

	private String getSequenceNameFromLine(String line) {
		// get sequence name from line
		StringTokenizer st = new StringTokenizer(line, " ");
		st.nextToken(); // skip "Sequence"
		st.nextToken(); // skip ":"
		return st.nextToken();
	}

	private int[] getQualScores(BufferedReader br, int sequenceLength)
			throws IOException {
		// loop over Quality score lines
		String line = br.readLine().trim();
		int[] qualityData = new int[sequenceLength];
		int index = 0;
		while (line.length() > 0) {
			StringTokenizer st = new StringTokenizer(line);
			while (st.hasMoreTokens()) {
				qualityData[index] = Integer.parseInt(st.nextToken());
				index++;
			}
			line = br.readLine();
		}
		return qualityData;
	}

	private String getSequenceDNA(BufferedReader br) throws IOException {
		// loop over DNA lines
		String line = br.readLine().trim();
		StringBuffer dna = new StringBuffer(151);
		while (line.length() > 0) {
			dna.append(line);
			line = br.readLine().trim();
		}
		return dna.toString();
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
		return new CAFFileContigIterator(mCafFile);
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
		return new CAFFileContigIterator(mCafFile, pNnums);
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
	 *            BufferedReader connected to CAFFile just after a contig header
	 * @param pContig
	 *            The ContigHeader object from the last line read
	 * @param contigDNAsequence
	 *            if known, otherwise send null
	 * @return a ReferenceSequence object with aligned reads
	 * @throws IOException
	 *             if file can't be read from
	 */
	ReferenceSequence createContigFromReader(BufferedReader br,
			CAFContigHeader pContig, String pContigDNAsequence)
			throws IOException {
		return createContigFromReader(br, pContig, pContigDNAsequence, null);
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
	 *            BufferedReader connected to CAFFile just after a contig header
	 * @param pContig
	 *            The ContigHeader object from the last line read
	 * @param contigDNAsequence
	 *            if known, otherwise send null
	 * @param pTask
	 *            a Task object to recieve status updates (may be null for no
	 *            updates)
	 * @return a ReferenceSequence object with aligned reads
	 * @throws IOException
	 *             if file can't be read from
	 */
	ReferenceSequence createContigFromReader(BufferedReader br,
			CAFContigHeader pContig, String contigDNAsequence, Task pTask)
			throws IOException {
		/*
		 * This is a complicated method, here's the outline: 1) read tags into
		 * list of reads and alignments 2) read contig sequence and create
		 * ReferenceSequence object 3) create read objects and alignments 4)
		 * clean up: make some final initializations
		 */

		// set up task with starting info
		if (pTask != null) {
			pTask.setCurrent(0);
			pTask.setMessage("Parsing Read Positions");
			// pTask.setLengthOfTask(pContig.getReadCount());
		}

		// //////////////
		// READ POSITOINS

		// set up variables for capturing data while looping
		HashMap<String, int[]> readPositions = new HashMap<String, int[]>();
		// loop over following lines
		String line = br.readLine().trim();
		while (line.length() > 0) {
			if (line.length() > 15
					&& line.substring(0, 15).equals("Assembled_from ")) {
				StringTokenizer st = new StringTokenizer(line.substring(15)
						.trim());
				String readName = st.nextToken();
				int[] positions = new int[4];
				for (int i = 0; i < positions.length; i++) {
					positions[i] = Integer.parseInt(st.nextToken());
				}
				readPositions.put(readName, positions);
			}
			line = br.readLine().trim();
		}
		int numReads = readPositions.size();

		// update task with rough estimate of progress
		if (pTask != null) {
			pTask.setCurrent(numReads / 2);
			pTask.setMessage("Parsing Contig Sequence");
			pTask.setLengthOfTask(2 * numReads + numReads / 10);
		}

		// //////////////////
		// CONTIG SEQUENCE

		// find DNA lines after contig "Sequence" block if we didn't see it
		// before
		if (contigDNAsequence == null) {
			while (true) {
				if (line.length() > 4 && line.substring(0, 4).equals("DNA ")) {
					break;
				}
				line = br.readLine();
			}

			// loop over DNA lines
			line = br.readLine().trim();
			StringBuffer contigDNAbuffer = new StringBuffer(151);
			while (line.length() > 0) {
				contigDNAbuffer.append(line);
				line = br.readLine().trim();
			}
			contigDNAsequence = contigDNAbuffer.toString();
		}

		// create contig
		// read and contig sequences have a "-" for any gap. these have to be
		// removed.

		// get array of gaps in Contig sequence
		// also remove them from sequence string and adjust length
		ArrayList<Integer> contigGaps = new ArrayList<Integer>();
		String newContigSequence = removeContigGaps(contigDNAsequence,
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

		// update task with rough estimate of progress
		if (pTask != null) {
			pTask.setCurrent(numReads);
			pTask.setMessage("Aligning Reads");
		}

		// ////////////////////
		// READ ALIGNMENTS
		// We have all the data parsed, we just need to build alignments
		// Map<String, Boolean> readDirs = new HashMap<String, Boolean>();
		// Map<String, Integer> readStarts = new HashMap<String, Integer>();
		// List<Boolean> afDirs = new ArrayList<Boolean>();
		// List<Integer> afStarts = new ArrayList<Integer>();

		// hashmap used to find matepairs
		HashMap<String, Read> soloReads = new HashMap<String, Read>();

		// loop over alignments
		int readCount = 0;
		for (Map.Entry<String, int[]> entry : readPositions.entrySet()) {
			String readName = entry.getKey();
			int[] positions = entry.getValue();
			RawRead rawRead = pContig.popRawRead(readName);

			// update task progress
			readCount++;
			if (pTask != null) {
				pTask.setCurrent(numReads + readCount);
				pTask.setMessage("Aligning Reads");
			}

			try {
				// boolean crdir = afDirs.get(readCount);
				// int crstart = afStarts.get(readCount);
				// boolean crdir =
				// readDirs.remove(currentRead.getName());
				// int crstart =
				// readStarts.remove(currentRead.getName());
				Read newRead = processCAFFileRead(rawRead, positions,
						contigGaps, contig, contigDNAsequence);
				if (newRead != null) {
					// add read to contig
					newRead.setId(readCount);
					contig.reads.put(newRead.getIdInteger(), newRead);

					// check for matePair
					String baseName = rawRead.getTemplate();
					Read matePair = soloReads.remove(baseName);
					if (matePair != null) {
						// associate reads as matepairs
						newRead.setMatepair(matePair);
					} else {
						soloReads.put(baseName, newRead);
					}

					// add to strain
					strain.putRead(newRead.getIdInteger(), newRead);
				}
			} catch (NullPointerException npe) {
				System.err.println("Error: Skipping read: " + readName);
				npe.printStackTrace();
			} catch (IllegalAlphabetException e) {
				System.err.println("Error: Skipping read: " + readName);
				System.err.println("Bad Sequence: " + rawRead.getSequence());
				e.printStackTrace();
			} catch (IllegalSymbolException e) {
				System.err.println("Error: Skipping read: " + readName);
				System.err.println("Bad Sequence: " + rawRead.getSequence());
				e.printStackTrace();
			}
		}

		// //////////
		// CLEAN UP

		if (pTask != null) {
			pTask.setMessage("Clean Up");
		}

		// initialize strain object for display
		// strain.initializeGraphics();
		strain.setAlignmentFromReads();
		if (pTask != null) {
			pTask.setCurrent(numReads + numReads / 5);
		}
		contig.addStrainWithNoId(strain);

		if (pTask != null) {
			pTask.setCurrent(numReads + numReads / 10);
			pTask.setMessage("Complete");
		}

		// // set quality threshold:
		// // quality data is faked to be 10->low 40->good since ace file data
		// is yes or no
		// QualifiedDifference.setQualityThreshold((short)20);
		// contig.hasQualityData = true;

		// return ReferenceSequence object
		return contig;
	}

	/**
	 * Build a Read object based on the parsed data from the CAF file
	 * 
	 * @param rawRead
	 *            The read name, sequence, strand, and trim data
	 * @param readPositions
	 *            The positions relevant for alignment (from Assembled_from
	 *            line)
	 * @param contigGaps
	 *            Positions in the raw contig DNA sequence that are gapped
	 * @param contig
	 *            The contig (as ReferenceSequence object) to which the read
	 *            should be aligned
	 * @param contigSequence
	 * @return the Read object
	 * @throws IllegalSymbolException
	 * @throws IllegalAlphabetException
	 */
	private Read processCAFFileRead(RawRead rawRead, int[] readPositions,
			List<Integer> contigGaps, ReferenceSequence contig,
			String contigSequence) throws IllegalAlphabetException,
			IllegalSymbolException {

		// // This is a little debugging check to make sure I have the format
		// // correct
		// if (readPositions[2] != rawRead.getTrimPositions()[0]
		// || readPositions[3] != rawRead.getTrimPositions()[1]) {
		// System.err
		// .print("WARNING: Trim and align positions are different: ");
		// System.err.println(rawRead.getName());
		// }

		// Initialize
		int contigGapsPassed = 0;
		int contigStart, contigEnd, gappedStart, qStart, qEnd;
		String cleanedSequence;
		String rawReadSequence = rawRead.getSequence();
		Read read = new Read();
		read.setName(rawRead.getName());
		// Positions array is start/stop in contig, start/stop in read
		// TODO: Some contig start/stop are backwards and it seems to be by
		// pair. FWD/1 and REV/1 will either both be start/stop or stop/start.
		// ...So I think we need to toggle the direction based on this... (and
		// get the correct start)
		contigStart = readPositions[0];
		contigEnd = readPositions[1];
		boolean dir = true;
		if (contigStart > contigEnd) {
			dir = !dir;
			int temp = contigEnd;
			contigEnd = contigStart;
			contigStart = temp;
		}

		// account for trimming
		if (mTrimEnds) {
			qStart = readPositions[2];
			qEnd = readPositions[3];
		} else {
			qStart = 1;
			qEnd = rawReadSequence.length();
			contigStart = contigStart - readPositions[2] + 1;
		}

		if (mRealign) {
			// calculate alignment from scratch
			// prepare read sequence
			cleanedSequence = removeDashesFromSequence(
					rawReadSequence.substring(qStart - 1, qEnd)).toString();
			if (!dir) {
				SymbolList cleanedSymbols = DNATools.createDNA(cleanedSequence);
				SymbolList finalSymbols = DNATools
						.reverseComplement(cleanedSymbols);
				cleanedSequence = finalSymbols.seqString();
				// cleanedSequence = DNATools.reverseComplement(
				// DNATools.reverseComplement(DNATools
				// .createDNA(cleanedSequence))).toString();
			}
			jaligner.Sequence alignableReadSequence = new jaligner.Sequence(
					cleanedSequence.toString());

			// take segment of contig (padded by 5% of read length on each end)
			int contigAlignmentStart = contigStart
					- (int) ((qEnd - qStart) / 20.0);
			if (contigAlignmentStart < 1) {
				contigAlignmentStart = 1;
			}
			int contigAlignmentEnd = contigStart + qStart
					+ (int) ((qEnd - qStart) * 1.05);
			if (contigAlignmentEnd > contigSequence.length()) {
				contigAlignmentEnd = contigSequence.length();
			}
			jaligner.Sequence alignableContigSequence = new jaligner.Sequence(
					removeDashesFromSequence(
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
			// just use alignment from CAF file

			contigGapsPassed = countContigGaps(contigGaps, contigStart);

			// deal with starts < 0
			if (contigStart < 1) {
				int adj = 1 - contigStart;
				qStart += adj;
				contigStart = 1;
			}

			// adjust start point for earlier gaps in contig
			gappedStart = contigStart - contigGapsPassed;

			// make sure read doesn't extend past end of contig
			int trimmedEnd = contigStart + qEnd - qStart;
			if (trimmedEnd > contigSequence.length()) {
				int adj = trimmedEnd - contigSequence.length();
				trimmedEnd -= adj;
				qEnd -= adj;
			}

			// compares read sequence to snippet of contig sequence and creates
			// Alignment object
			// gaps are removed from read sequence in this process
			try {
				Alignment a = getAlignmentFromCAFData(read,
						rawReadSequence.substring(qStart - 1, qEnd), contig,
						contigSequence.substring(contigStart - 1, trimmedEnd),
						gappedStart, dir, qStart, qEnd);
				read.setAlignment(a);
			} catch (StringIndexOutOfBoundsException sioobe) {
				System.err.println("Error getting alignment for: "
						+ read.getName() + " skipping...");
				System.err.println(sioobe.toString());
				System.err.println(rawRead.toString());
				System.err.println("Contig length:" + contigSequence.length());
				System.err.println("Read length:" + rawReadSequence.length());
				System.err.println("last start:" + contigStart);
				System.err.println("qa start:" + qStart);
				System.err.println("qa end:" + qEnd);
				return null;
			} catch (IllegalSymbolException ise) {
				System.err.println("Error getting alignment for: "
						+ read.getName() + " skipping...");
				System.err.println(ise.toString());
				System.err.println(rawRead.toString());
				System.err.println("Contig length:" + contigSequence.length());
				System.err.println("Read length:" + rawReadSequence.length());
				System.err.println("last start:" + contigStart);
				System.err.println("qa start:" + qStart);
				System.err.println("qa end:" + qEnd);
				return null;
			}

			// remove -'s from read sequence and adjust length and alignment
			// position
			// CAF file inserts stars for gaps, and includes them in position
			// counts
			StringTokenizer stars = new StringTokenizer(rawReadSequence, "-");
			StringBuffer gaplessSequence = new StringBuffer(
					rawReadSequence.length());
			SequenceSegment aligOnRead = read.getAlignment()
					.getSequenceSegment2();
			while (stars.hasMoreTokens()) {
				String segment = stars.nextToken();

				// add star-less segment to cleaned sequence
				gaplessSequence.append(segment);

				// Adjust where alignment falls on read
				if (gaplessSequence.length() < aligOnRead.getEnd()) {
					aligOnRead.setEnd(aligOnRead.getEnd() - 1);
					if (gaplessSequence.length() < aligOnRead.getStart()) {
						aligOnRead.setStart(aligOnRead.getStart() - 1);
					}
				}
			}
			cleanedSequence = gaplessSequence.toString();
		}

		read.setLength(cleanedSequence.length());
		// read.setBases(cleanedSequence.toString());

		return read;
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
	private StringBuffer removeDashesFromSequence(String readSeq) {
		StringTokenizer stars = new StringTokenizer(readSeq, "-");
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
	private Alignment getAlignmentFromCAFData(Read pRead, String pReadSequence,
			ReferenceSequence pRefSeq, String pContigSequence, int pAStart,
			boolean pDir, int pQStart, int pQEnd) throws IllegalSymbolException {

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
			char rawRBase;
			if (pDir) {
				rawRBase = pReadSequence.charAt(i);
			} else {
				rawRBase = getComplement(pReadSequence.charAt(pReadSequence
						.length() - 1 - i));
			}
			char rBase = Character.toLowerCase(rawRBase);
			char cBase = Character.toLowerCase(pContigSequence.charAt(i));
			if (!areBasesEquivalent(rBase, cBase)) {
				// change *'s to -'s for compatability
				// if (rBase == '*') {
				// rBase = '-';
				// rawRBase = '-';
				// } else if (cBase == '*') {
				// cBase = '-';
				// }

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

			} else if (rBase == '-') {
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

	private char getComplement(char base) throws IllegalSymbolException {
		return DNATools.dnaToken(DNATools.complement(DNATools.forSymbol(base)));
	}

	/**
	 * Return true if read base matches contig base. (This is where we'd account
	 * for ambiguous IUPAC nucleotides, if we were going to do such a thing.)
	 * 
	 * @param rBase
	 *            The read base
	 * @param cBase
	 *            The contig base
	 * @return
	 */
	private boolean areBasesEquivalent(char rBase, char cBase) {
		return rBase == cBase;
	}

	private class CAFFileContigIterator implements Iterator<ReferenceSequence> {
		private CAFContigHeader nextContig = null;
		private final Map<String, RawRead> mReadMap = new HashMap<String, RawRead>();
		private final Map<String, String> mDNASequenceMap = new HashMap<String, String>();
		private final Map<String, int[]> mQualScoreMap = new HashMap<String, int[]>();
		private BufferedReader br = null;
		private final File mCAFFile;
		private Set<Long> nums = null;

		CAFFileContigIterator(File pCAFFile) throws IOException,
				FileNotFoundException {
			mCAFFile = pCAFFile;

			// Open file reader
			br = new BufferedReader(new FileReader(mCAFFile));

			// set up first contig
			queueNextContig();
		}

		CAFFileContigIterator(File pCAFFile, Set<Long> pNums)
				throws IOException, FileNotFoundException {
			mCAFFile = pCAFFile;
			nums = pNums;

			// set to null to indicate all contigs if no nums given
			if (nums.size() == 0) {
				nums = null;
			}

			// Open file reader
			br = new BufferedReader(new FileReader(mCAFFile));

			System.out.println("finding next contig");

			// set up first contig
			queueNextContig();
		}

		private void queueNextContig() throws IOException {
			// loop over lines
			int lineCount = 0;
			String line = br.readLine();
			while (line != null) {
				lineCount++;

				// find DNA lines
				if (line.length() > 4 && line.substring(0, 4).equals("DNA ")) {
					String sequenceName = getSequenceNameFromLine(line);
					String dnaSequence = getSequenceDNA(br);
					if (mReadMap.containsKey(sequenceName)) {
						mReadMap.get(sequenceName).setSequence(dnaSequence);
					} else {
						mDNASequenceMap.put(sequenceName, dnaSequence);
					}
				} else if (line.length() > 12
						&& line.substring(0, 12).equals("BaseQuality ")
						&& mSaveQualityData) {
					String sequenceName = getSequenceNameFromLine(line);
					if (!mDNASequenceMap.containsKey(sequenceName)) {
						System.err.println("Cannot find DNA for "
								+ sequenceName + ". Skipping Quality scores");
						line = br.readLine();
						continue;
					}
					int[] qualScores = getQualScores(br,
							mDNASequenceMap.get(sequenceName).length());
					if (mReadMap.containsKey(sequenceName)) {
						mReadMap.get(sequenceName).setQuality(qualScores);
					} else {
						mQualScoreMap.put(sequenceName, qualScores);
					}
				} else if (line.length() > 9
						&& line.substring(0, 9).equals("Sequence ")) {
					// this line is a sequence header line
					StringTokenizer st = new StringTokenizer(line, " ");
					st.nextToken(); // skip "Sequence"
					st.nextToken(); // skip ":"

					// get name (second token)
					String sequenceName = st.nextToken().toString();

					// read or contig
					String type = br.readLine().trim();
					if (type.equals("Is_read")) {
						RawRead rawRead = parseCAFRead(sequenceName, br,
								mSaveQualityData);
						String dna = mDNASequenceMap.remove(sequenceName);
						if (dna != null) {
							rawRead.setSequence(dna);
						}
						int[] qualityScores = mQualScoreMap.remove(rawRead
								.getName());
						if (qualityScores != null) {
							rawRead.setQuality(qualityScores);
						}
						mReadMap.put(sequenceName, rawRead);
					} else if (type.equals("Is_contig")) {
						nextContig = CAFContigHeader.parseHeaderLine(line,
								mReadMap);
						if (nums != null) {
							// if there is a list of numbers only stop if this
							// contig
							// is in the list
							if (nums.contains(new Long(nextContig.getNumber()))) {
								return;
							} else {
								// clear DNA and qual arrays
								mDNASequenceMap.clear();
								mQualScoreMap.clear();
							}
						} else {
							// otherwise, we'll take any contig
							return;
						}
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
						String contigName = nextContig.getName();
						String contigDNASequence = mDNASequenceMap
								.remove(contigName);
						ReferenceSequence ret = createContigFromReader(br,
								nextContig, contigDNASequence);
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
						"Suddenly can't read from ace file: " + mCAFFile);
			}
		}

		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException(
					"We're not modifying files here.");
		}
	}

	private static final String PROGRAM_NAME = "CAFFileReader";
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
		BatchCAFImportTask task = new BatchCAFImportTask(aceFileName,
				outputDir, qualFile, cNums);
		Object result = task.doStuff();
		if (result == null) {
			System.err.println(task.getErrorTitle());
			System.err.println(task.getMessage());
		}
	}

	/**
	 * Parses the essential info from a read "Sequence" entry in a CAF file
	 * 
	 * Essential lines: Template "HOT237_0500m_34118649" Strand Forward Clipping
	 * QUAL 16 283
	 * 
	 * @param sequenceName
	 *            The name of the entry
	 * @param br
	 *            The buffered file reader
	 * @return a RawRead object with sequence, quality, trim data, and direction
	 * @throws IOException
	 *             if unable to read file
	 */
	private RawRead parseCAFRead(String sequenceName, BufferedReader br,
			boolean storeQualityData) throws IOException {
		// Parse the essential data from a REad entry in a CAF file
		//

		RawRead rawRead = new RawRead();
		rawRead.setName(sequenceName);

		// loop over tag lines
		String line = br.readLine();
		while (line.trim().length() > 0) {
			if (line.length() > 9 && line.substring(0, 9).equals("Template ")) {
				rawRead.setTemplate(line.substring(9).trim());
			} else if (line.length() > 7
					&& line.substring(0, 7).equals("Strand ")) {
				rawRead.setForward(line.substring(7).trim().equals("Forward"));
			} else if (line.length() > 8
					&& line.substring(0, 8).equals("Seq_vec ")) {
				StringTokenizer st = new StringTokenizer(line.substring(9));
				st.nextToken(); // skip vector type
				int[] vectorPositions = new int[2];
				vectorPositions[0] = Integer.parseInt(st.nextToken());
				vectorPositions[1] = Integer.parseInt(st.nextToken());
				rawRead.setVectorPositions(vectorPositions);
			} else if (line.length() > 9
					&& line.substring(0, 9).equals("Clipping ")) {
				StringTokenizer st = new StringTokenizer(line.substring(9));
				st.nextToken(); // skip "QUAL"
				int[] trimPositions = new int[2];
				trimPositions[0] = Integer.parseInt(st.nextToken());
				trimPositions[1] = Integer.parseInt(st.nextToken());
				rawRead.setTrimPositions(trimPositions);
			}
			line = br.readLine();
		}

		// // find DNA lines
		// while (true) {
		// if (line.length() > 4 && line.substring(0, 4).equals("DNA ")) {
		// break;
		// }
		// line = br.readLine();
		// }
		//
		// // loop over DNA lines
		// line = br.readLine().trim();
		// StringBuffer dna = new StringBuffer(151);
		// while (line.length() > 0) {
		// dna.append(line);
		// line = br.readLine().trim();
		// }
		// rawRead.setSequence(dna.toString());
		//
		// if (storeQualityData) {
		//
		// // find Quality lines
		// line = br.readLine();
		// while (true) {
		// if (line.length() > 12
		// && line.substring(0, 12).equals("BaseQuality ")) {
		// break;
		// }
		// line = br.readLine();
		// }
		//
		// // loop over Quality score lines
		// line = br.readLine().trim();
		// int[] qualityData = new int[dna.length()];
		// int index = 0;
		// while (line.length() > 0) {
		// StringTokenizer st = new StringTokenizer(line);
		// while (st.hasMoreTokens()) {
		// qualityData[index] = Integer.parseInt(st.nextToken());
		// index++;
		// }
		// line = br.readLine();
		// }
		// rawRead.setQuality(qualityData);
		// }

		// return read object
		return rawRead;
	}

	public class RawRead {
		/**
		 * @return the nucleotide sequence
		 */
		public String getSequence() {
			return sequence;
		}

		/**
		 * @param sequence
		 *            the nucleotide sequence to set
		 */
		public void setSequence(String sequence) {
			this.sequence = sequence;
		}

		/**
		 * @return the quality scores
		 */
		public int[] getQuality() {
			return quality;
		}

		/**
		 * @param quality
		 *            the quality scores to set
		 */
		public void setQuality(int[] quality) {
			this.quality = quality;
		}

		/**
		 * @return the read name/id
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name
		 *            the read name/id to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the read template/id
		 */
		public String getTemplate() {
			return template;
		}

		/**
		 * @param template
		 *            the read template/id to set
		 */
		public void setTemplate(String template) {
			this.template = template;
		}

		/**
		 * @return true if read is aligned in the forward strand
		 */
		public boolean isForward() {
			return forward;
		}

		/**
		 * @param forward
		 *            Is the read aligned in the forward strand?
		 */
		public void setForward(boolean forward) {
			this.forward = forward;
		}

		/**
		 * @return the trimPositions
		 */
		public int[] getTrimPositions() {
			return trimPositions;
		}

		/**
		 * @param trimPositions
		 *            the trimPositions to set
		 */
		public void setTrimPositions(int[] trimPositions) {
			this.trimPositions = trimPositions;
		}

		/**
		 * @return the vectorPositions
		 */
		public int[] getVectorPositions() {
			return vectorPositions;
		}

		/**
		 * @param vectorPositions
		 *            the vectorPositions to set
		 */
		public void setVectorPositions(int[] vectorPositions) {
			this.vectorPositions = vectorPositions;
		}

		private String sequence;
		private int[] quality;
		private String name;
		private String template;
		private boolean forward;
		private int[] trimPositions;
		private int[] vectorPositions;

		@Override
		public String toString() {
			StringBuffer output = new StringBuffer(80);
			output.append("RawRead('");
			if (sequence.length() > 8) {
				output.append(sequence.substring(0, 5));
				output.append("...'(");
				output.append(sequence.length());
				output.append(" bases)");
			} else {
				output.append(sequence);
				output.append("'");
			}
			if (!forward) {
				output.append(" (rev-comp) ");
			} else {
				output.append(" ");
			}
			output.append(trimPositions);

			return output.toString();
		}
	}
}
