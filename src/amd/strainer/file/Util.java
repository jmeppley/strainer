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

import jaligner.matrix.Matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biojava.bio.seq.NucleotideTools;
import org.biojava.bio.symbol.BasisSymbol;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleAtomicSymbol;
import org.biojava.bio.symbol.Symbol;

import amd.strainer.display.ReferenceSequenceDisplayComponent;
import amd.strainer.objects.Alignment;
import amd.strainer.objects.Clone;
import amd.strainer.objects.Difference;
import amd.strainer.objects.QualifiedDifference;
import amd.strainer.objects.Read;
import amd.strainer.objects.ReadHolder;
import amd.strainer.objects.Readable;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * Utility methods for dealing with files. Mostly a collection of nested calls
 * for writing objects to XML. Also contains lookForMatePair() which is used to
 * check for mate-pair associations when reading in a data file.
 * 
 * @author jmeppley
 * 
 */
public class Util {
	private final static Pattern phrapReadRE = Pattern
			.compile("^([-0-9a-zA-Z_]+)\\.([a-zA-Z])[0-9]+");
	private final static Pattern illuminaReadRE = Pattern
			.compile("^(.+)[/:][12]");

	/**
	 * @param pReadHolder
	 *            an object containing a collection of reads that can be
	 *            iterated over
	 * @return a HashMap of read names (Strings) to Read objects
	 */
	public static HashMap<String, Read> getReadNameMap(ReadHolder pReadHolder) {
		HashMap<String, Read> reads = new HashMap<String, Read>();
		Iterator<Read> rit = pReadHolder.getReadIterator();
		while (rit.hasNext()) {
			Read read = rit.next();
			reads.put(read.getName(), read);
		}
		return reads;
	}

	/**
	 * @param pReadHolders
	 *            an iterator over a collection of ReadHolder objects. A
	 *            ReadHolder is an object containing a collection of reads that
	 *            can be iterated over. For example, a ReferenceSequence.
	 * @return a HashMap of read names (Strings) to Read objects
	 */
	public static HashMap<String, Read> getReadNameMap(Iterator pReadHolders) {
		HashMap<String, Read> reads = new HashMap<String, Read>();

		while (pReadHolders.hasNext()) {
			ReadHolder readHolder = (ReadHolder) pReadHolders.next();
			Iterator<Read> rit = readHolder.getReadIterator();
			while (rit.hasNext()) {
				Read read = rit.next();
				reads.put(read.getName(), read);
			}
		}

		return reads;
	}

	/**
	 * Writes a reference sequence object to XML.
	 * 
	 * @param pRefSeq
	 *            The object to be translated into XML
	 * @param pFile
	 *            the File to write XML to
	 * @param pLabel
	 *            a string identifying this version of the strain groupings
	 * @param pWriteAlignments
	 *            true if alignments should be included (else, just write strain
	 *            groups)
	 * @throws IOException
	 *             if there is an error creating the XML file
	 */
	public static void writeStrainsToXML(ReferenceSequence pRefSeq, File pFile,
			String pLabel, boolean pWriteAlignments) throws IOException {
		FileWriter fw = new FileWriter(pFile, false);
		PrintWriter pw = new PrintWriter(fw);

		writeStrainsToXML(pRefSeq, pw, pLabel, "", pWriteAlignments);
		pRefSeq.strainsFile = pFile.getAbsolutePath();

		pw.close();
		fw.close();
	}

	/**
	 * Writes a reference sequence object to ACE format.
	 * 
	 * @param pRefSeq
	 *            The object to be translated into ACE
	 * @param pFile
	 *            the ACE File to create
	 * @param pLabel
	 *            a string identifying this version of the strain groupings
	 * @throws IOException
	 *             if there is an error creating the ACE file
	 */
	public static void writeStrainsToAce(ReferenceSequence pRefSeq, File pFile)
			throws IOException {
		FileWriter fw = new FileWriter(pFile, false);
		PrintWriter pw = new PrintWriter(fw);

		writeStrainsToAce(pRefSeq, pw);
		pRefSeq.strainsFile = pFile.getAbsolutePath();

		pw.close();
		fw.close();
	}

	/**
	 * Search hash map for a mate pair to this read, return the mate pair if
	 * it's found otherwise, add this read to hash map. Connect reads to
	 * eachother using read.setMatePair().
	 */
	public static Read lookForMatePair(Map<String, Read> pReadMap, Read pRead) {
		return lookForMatePair(pReadMap, pRead, true);
	}

	/**
	 * Search hash map for a mate pair to this read, return the mate pair if
	 * it's found otherwise, add this read to hash map
	 * 
	 * @param pLinkReads
	 *            true if reads should be combined into clones, false if that
	 *            should be postponed
	 */
	public static Read lookForMatePair(Map<String, Read> pReadMap, Read pRead,
			boolean pLinkReads) {

		// System.out.println("Looking for matepair for " + pRead.getName());
		String base = getReadNameBase(pRead.getName());

		Read matePair = pReadMap.remove(base);
		if (matePair != null) {
			if (pLinkReads) {
				// associate reads as matepairs
				pRead.setMatepair(matePair);
			}
			return matePair;
		} else {
			pReadMap.put(base, pRead);
			return null;
		}

	}

	/**
	 * 
	 * @param pName
	 * @return
	 */
	public static String getReadNameBase(String pName) {
		String base = null;
		Matcher readmatch = phrapReadRE.matcher(pName);
		if (readmatch.find()) {
			base = readmatch.group(1);
		} else {
			// If we get here, it wasn't sanger
			readmatch = illuminaReadRE.matcher(pName);
			if (readmatch.find()) {
				base = readmatch.group(1);
			} else {
				base = pName;
			}
		}
		return base;
	}

	public static void writeStrainsToAce(ReferenceSequence pRefSeq,
			PrintWriter pW) {
		// FIXME: is there a biojava too for creating ACE files?

		// first line is: AS #contigs #reads
		pW.print("AS 1 ");
		pW.println(pRefSeq.reads.size());
		// blank line
		pW.println();

		// one contig per ReferenceSequence object
		// "CO contigName length #reads #referenceSegments #U/C
		pW.print("CO contig00001 ");
		pW.print(pRefSeq.getLength());
		pW.print(" ");
		pW.print(pRefSeq.reads.size());
		// TODO: build consensus from reads (and put number of BS lines instead
		// of '0' in next line)
		pW.println(" 0 U");
		// sequence
		pW.println(pRefSeq.getBases());
		// blank line
		pW.println();
		// TODO: quality scores

		for (Read read : pRefSeq.reads.values()) {
			// read position line: AF readname U/C start
			pW.print("AF ");
			pW.print(read.getName());
			pW.print(read.getAlignment().isForward() ? " U " : " C ");
			pW.println(read.getAlignment().getStart());
		}
		// blank line
		pW.println();

		// TODO: build consensus from reads, print BS lines

		for (Read read : pRefSeq.reads.values()) {
			// Read line: RD name length 0 0
			pW.print("RD ");
			pW.print(read.getName());
			pW.print(" ");
			pW.print(read.getLength());
			pW.println(" 0 0");

			pW.println();

			// quality line: QA start end start end
			pW.print("QA 1 ");
			pW.print(read.getLength());
			pW.print(" 1 ");
			pW.println(read.getLength());

			pW.println();
		}

	}

	public static void writeStrainsToXML(ReferenceSequence pRefSeq,
			PrintWriter pW, String pLabel, String pPrefix,
			boolean pWriteAlignments) {
		pW.print(pPrefix);
		pW.print("<Strains Size=\"");
		pW.print(pRefSeq.strains.size());
		pW.print("\" EntryId=\"");
		pW.print(pRefSeq.getId());
		pW.print("\" EntryName=\"");
		pW.print(pRefSeq.getName());
		if (pLabel != null) {
			pW.print("\" Label=\"");
			pW.print(pLabel);
		}
		pW.print("\" HasQualityData=\"");
		pW.print(pRefSeq.hasQualityData);
		pW.println("\">");

		String prefix1 = pPrefix + " ";
		String prefix2 = pPrefix + "  ";
		for (Strain strain : pRefSeq.strains.values()) {
			if (strain.getSize() == 0) {
				// Skip empty strains and warn
				pW.println();
				System.err.println("Not writing empty strain to file. ID: "
						+ strain.getId());
				continue;
			}

			pW.print(prefix1);
			pW.print("<Strain Size=\"");
			pW.print(strain.getSize());
			pW.print("\" Open=\"");
			if (strain.areGraphicsInitialized()) {
				// only check open stae if we are getting info from an active
				// display
				pW.print(strain.isOpen());
			} else {
				// assume he strain is open
				pW.print(true);
			}
			pW.println("\">");

			Iterator<Readable> rit = strain.getReadableIterator();
			if (!pWriteAlignments) {
				writeReadIdsToXML(rit, pW, prefix2);
			} else {
				writeReadsToXML(rit, pW, prefix2);
			}
			pW.print(prefix1);
			pW.println("</Strain>");
		}

		pW.print(pPrefix);
		pW.println("</Strains>");
	}

	/**
	 * Writes Read ids in XML format. each read is written using
	 * writeReadIdToXML()
	 * 
	 * @param pIt
	 *            iterator over a collection of Read objects
	 * @param pW
	 *            Writer to which XML is sent
	 * @param pPrefix
	 *            whitespace string for indenting
	 */
	public static void writeReadIdsToXML(Iterator<Readable> pIt,
			PrintWriter pW, String pPrefix) {
		while (pIt.hasNext()) {
			Readable r = pIt.next();
			if (r instanceof Clone) {
				Clone clone = (Clone) r;
				writeReadIdToXML(clone.reads[0], pW, pPrefix);
				writeReadIdToXML(clone.reads[1], pW, pPrefix);
			} else {
				writeReadIdToXML((Read) r, pW, pPrefix);
			}
		}
	}

	/**
	 * Writes Read id in XML format: <Read Id="1" IsRecombinant="0"/>
	 * 
	 * @param pRead
	 *            Read object
	 * @param pW
	 *            Writer to which XML is sent
	 * @param pPrefix
	 *            whitespace string for indenting
	 */
	private static void writeReadIdToXML(Read pRead, PrintWriter pW,
			String pPrefix) {
		pW.print(pPrefix);
		pW.print("<Read Id=\"");
		pW.print(pRead.getId());
		pW.print("\" IsRecombinant=\"");
		pW.print(pRead.isRecombinant() ? "1" : "0");
		pW.println("\"/>");
	}

	/**
	 * Writes Reads in XML format including Alignments
	 * 
	 * @param pIt
	 *            iterator over a collection of Readable objects
	 * @param pW
	 *            Writer to which XML is sent
	 * @param pPrefix
	 *            whitespace string for indenting
	 */
	public static void writeReadsToXML(Iterator<Readable> pIt, PrintWriter pW,
			String pPrefix) {
		while (pIt.hasNext()) {
			Readable r = pIt.next();
			if (r instanceof Clone) {
				Clone clone = (Clone) r;
				writeReadToXML(clone.reads[0], pW, pPrefix);
				writeReadToXML(clone.reads[1], pW, pPrefix);
			} else {
				writeReadToXML((Read) r, pW, pPrefix);
			}
		}
	}

	/**
	 * Writes Read in XML format
	 * 
	 * @param pRead
	 *            Read object
	 * @param pW
	 *            Writer to which XML is sent
	 * @param pPrefix
	 *            whitespace string for indenting
	 */
	private static void writeReadToXML(Read pRead, PrintWriter pW,
			String pPrefix) {
		pW.print(pPrefix);
		pW.print("<Read Id=\"");
		pW.print(pRead.getId());
		pW.print("\" Name=\"");
		pW.print(pRead.getName());
		pW.print("\" Length=\"");
		pW.print(pRead.getLength());
		pW.print("\" MatePairId=\"");
		if (pRead.getMatePair() == null) {
			pW.print("-1");
		} else {
			pW.print(pRead.getMatePair().getId());
		}
		pW.print("\" IsRecombinant=\"");
		pW.print(pRead.isRecombinant() ? "1" : "0");
		pW.print("\" IsBadClone=\"");
		pW.print(pRead.isBadClone() ? "1" : "0");
		pW.println("\">");

		writeAlignmentToXML(pRead.getAlignment(), pW, pPrefix + " ");

		pW.print(pPrefix);
		pW.println("</Read>");
	}

	/**
	 * Write Alignment object to XML
	 * 
	 * @param pA
	 *            Object to write to XML
	 * @param pW
	 *            Writer to send XML to
	 * @param pPrefix
	 *            indent level
	 */
	private static void writeAlignmentToXML(Alignment pA, PrintWriter pW,
			String pPrefix) {
		pW.print(pPrefix);
		pW.print("<Alignment Start=\"");
		pW.print(pA.getStart());
		pW.print("\" End=\"");
		pW.print(pA.getEnd());
		pW.print("\" Dir=\"");
		pW.print(pA.isForward() ? "1" : "0");
		pW.print("\" Score=\"");
		pW.print(pA.score);
		pW.println("\">");

		for (Difference d : pA.getDiffs()) {
			pW.print(pPrefix);
			pW.print(" <Diff EntryPos=\"");
			pW.print(d.getPosition1());
			pW.print("\" EntryBase=\"");
			pW.print(d.getBase1());
			pW.print("\" QueryPos=\"");
			pW.print(d.getPosition2());
			pW.print("\" QueryBase=\"");
			if (d instanceof QualifiedDifference) {
				QualifiedDifference qd = (QualifiedDifference) d;
				pW.print(qd.getBase2Actual());
				pW.print("\" Quality=\"");
				pW.print(qd.getQuality());
			} else {
				pW.print(d.getBase2());
			}
			pW.println("\"/>");
		}

		pW.print(pPrefix);
		pW.println("</Alignment>");

	}

	/**
	 * reverse the order of a string
	 * 
	 * @param seq
	 *            a sequence of characters
	 * @return the reversed sequence of characters
	 */
	static String reverseString(String seq) {
		StringBuffer rSeq = new StringBuffer(seq.length());
		int i = seq.length() - 1;
		while (i >= 0) {
			rSeq.append(seq.charAt(i));
			i--;
		}
		return rSeq.toString();
	}

	/**
	 * Reverse complement a sequence (and convert to lowercase)
	 * 
	 * @param seq
	 *            a DNA sequence as a string
	 * @return teh reverse complement sequence
	 */
	static String reverseSeq(String seq) {
		StringBuffer rSeq = new StringBuffer(seq.length());
		seq = seq.toLowerCase();
		int i = seq.length() - 1;
		while (i >= 0) {
			char c = seq.charAt(i);
			if (c == 'a') {
				rSeq.append('t');
			} else if (c == 't') {
				rSeq.append('a');
			} else if (c == 'c') {
				rSeq.append('g');
			} else if (c == 'g') {
				rSeq.append('c');
			} else {
				rSeq.append(c);
			}
			i--;
		}

		return rSeq.toString();
	}

	/**
	 * Returns an Alignment using the given blast data
	 * 
	 * @param pRead
	 *            the Read that matched the reference seqence
	 * @param pRefSeq
	 *            the ReferenceSequence
	 * @param pDir
	 *            the direction or strand of the match
	 * @param pAStart
	 *            the first position on the ReferenceSequence matched
	 * @param pAEnd
	 *            the last position matched
	 * @param pScore
	 *            the BLAST score
	 * @param pLength
	 *            the length of the alignment
	 * @param pEValue
	 *            the alignment's e-value
	 * @param pQueryString
	 *            the string of bases matched in the read (with gaps inserted)
	 * @param pMatchString
	 *            the tring of bars and spaces indicating the bases that match
	 * @param pHitString
	 *            the string of bases matched in the ReferenceSequence
	 * @return amd.strainer.objects.Alignment object representing this BLAST
	 *         result
	 */
	static Alignment buildAlignmentFromBlastData(Read pRead,
			ReferenceSequence pRefSeq, boolean pDir, int pAStart, int pAEnd,
			int pScore, int pLength, String pEValue, String pQueryString,
			String pMatchString, String pHitString) {

		// System.out.println("Creating alignment for " +
		// currentRead.getName());
		// System.out.println(" dir: " + dir + " -- " + astart + ":" + aend +
		// ":" + length +
		// " -- score: " + score + ":/" + eValue);
		// System.out.println(query);
		// System.out.println(match);
		// System.out.println(hit);

		if (!pDir) {
			// flip starts and ends (since read was the reference in blast
			// and I want scaffold to be the reference here)
			int temp = pAStart;
			pAStart = pAEnd;
			pAEnd = temp;
		}

		// trim strings to same length if they are not already
		if (pQueryString.length() != pHitString.length()
				|| pQueryString.length() != pMatchString.length()) {
			System.err
					.println("Match string lengths are not the same...may be a BioJava parsing error!!!");
			/*
			 * most instances of this error seem to have arbitrary data from the
			 * rest of the file appended to the complete match string ...so
			 * trimming is OK
			 */
			// System.err.println(pQueryString);
			// System.err.println(pMatchString);
			// System.err.println(pHitString);
			/*
			 * Example: query: AGAAAAAATAGGTT match: ||||||||||||||[MAR-25-2007]
			 * SCHUL, STEPHEN ZHENG ZHANG, AND PSI-BLAST:CLEIC ACIDS RE19.Y2
			 * CHROMATHD.1 CHEM: TER15019 DIRECTIOLETTERS) hit: AGAAAAAATAGGTT
			 */
			int length = Math.min(
					Math.min(pQueryString.length(), pHitString.length()),
					pMatchString.length());
			pMatchString = pMatchString.substring(0, length);
		}

		// get diffs from blast alignment
		ArrayList<Difference> diffs = new ArrayList<Difference>();
		if (!pDir) {
			pQueryString = reverseSeq(pQueryString);
			pHitString = reverseSeq(pHitString);
			pMatchString = reverseString(pMatchString);
		} else {
			pQueryString = pQueryString.toLowerCase();
			pHitString = pHitString.toLowerCase();
		}
		int qGaps = 0;
		int hGaps = 0;
		int d = pMatchString.indexOf(" ");
		while (d > 0) {
			char q = pQueryString.charAt(d); // query seq is the read
			char h = pHitString.charAt(d); // hit seq is the scaffold

			int qPos = d - qGaps + 1; // actual position in read segment
			int ePos = d - hGaps + 1; // actual position in refrenceSequence
			// segment

			// adjust refSEq position to be relative to entire refSeq
			// (we don't do this for reads, since we only use matched part)
			ePos = ePos + pAStart - 1;

			// if diff is X or x, change it to n
			if (q == 'X')
				q = 'N';
			else if (q == 'x')
				q = 'n';

			// create diff object and add it to the array
			diffs.add(new Difference(ePos, h, qPos, q));

			// count gaps so we can adjust future positions by -1 per gap
			if (q == '-') {
				qGaps = qGaps + 1;
			}
			if (h == '-') {
				hGaps = hGaps + 1;
			}

			// next diff:
			d = pMatchString.indexOf(" ", d + 1);
		}

		// create alignment object
		SequenceSegment ess = new SequenceSegment(pRefSeq, pAStart, pAEnd);
		SequenceSegment rss = new SequenceSegment(pRead, 1, pLength);
		Alignment a = new Alignment(ess, rss, pDir, diffs);
		a.score = pScore;
		try {
			a.eValue = Double.parseDouble(pEValue);
		} catch (NumberFormatException e) {
			int exp = Integer.parseInt(pEValue.substring(1, pEValue.length()));
			a.eValue = 10.0 * exp;
		}

		// return alignment
		return a;
	}

	public static String getNameFromFastaHeader(String line) {
		if (line == null || line.trim().length() == 0) {
			return null;
		}
		int first = line.charAt(0) == '>' ? 1 : 0;
		int end = line.indexOf(" ");
		if (end < 0)
			return line.substring(first);
		return line.substring(first, end);
	}

	/**
	 * The size of the scoring matrix. It is the number of the characters in the
	 * ASCII table. It is more than the 20 amino acids just to save the
	 * processing time of the mapping.
	 */
	private static final int SIZE = 127;
	public static final float GAP_EXTEND = 2f;
	public static float GAP_START = 3f;
	private static Matrix mNucleotideMatrix;
	private static final int MATCH = 1;
	private static final int MISMATCH = -2;

	public static Matrix getNucleotideMatrix() {
		if (mNucleotideMatrix == null) {
			float[][] scores = new float[SIZE][SIZE];
			scores['a']['a'] = MATCH;
			scores['a']['c'] = MISMATCH;
			scores['a']['t'] = MISMATCH;
			scores['a']['g'] = MISMATCH;
			scores['a']['n'] = 0;
			scores['a']['A'] = MATCH;
			scores['a']['C'] = MISMATCH;
			scores['a']['T'] = MISMATCH;
			scores['a']['G'] = MISMATCH;
			scores['a']['N'] = 0;
			scores['c']['a'] = MISMATCH;
			scores['c']['c'] = MATCH;
			scores['c']['t'] = MISMATCH;
			scores['c']['g'] = MISMATCH;
			scores['c']['n'] = 0;
			scores['c']['A'] = MISMATCH;
			scores['c']['C'] = MATCH;
			scores['c']['T'] = MISMATCH;
			scores['c']['G'] = MISMATCH;
			scores['c']['N'] = 0;
			scores['t']['a'] = MISMATCH;
			scores['t']['c'] = MISMATCH;
			scores['t']['t'] = MATCH;
			scores['t']['g'] = MISMATCH;
			scores['t']['n'] = 0;
			scores['t']['A'] = MISMATCH;
			scores['t']['C'] = MISMATCH;
			scores['t']['T'] = MATCH;
			scores['t']['G'] = MISMATCH;
			scores['t']['N'] = 0;
			scores['g']['a'] = MISMATCH;
			scores['g']['c'] = MISMATCH;
			scores['g']['t'] = MISMATCH;
			scores['g']['g'] = MATCH;
			scores['g']['n'] = 0;
			scores['g']['A'] = MISMATCH;
			scores['g']['C'] = MISMATCH;
			scores['g']['T'] = MISMATCH;
			scores['g']['G'] = MATCH;
			scores['g']['N'] = 0;
			scores['n']['a'] = 0;
			scores['n']['c'] = 0;
			scores['n']['t'] = 0;
			scores['n']['g'] = 0;
			scores['n']['n'] = 0;
			scores['n']['A'] = 0;
			scores['n']['C'] = 0;
			scores['n']['T'] = 0;
			scores['n']['G'] = 0;
			scores['n']['N'] = 0;
			scores['N']['a'] = 0;
			scores['N']['c'] = 0;
			scores['N']['t'] = 0;
			scores['N']['g'] = 0;
			scores['N']['n'] = 0;
			scores['N']['A'] = 0;
			scores['N']['C'] = 0;
			scores['N']['T'] = 0;
			scores['N']['G'] = 0;
			scores['N']['N'] = 0;
			scores['A']['a'] = MATCH;
			scores['A']['c'] = MISMATCH;
			scores['A']['t'] = MISMATCH;
			scores['A']['g'] = MISMATCH;
			scores['A']['n'] = 0;
			scores['A']['A'] = MATCH;
			scores['A']['C'] = MISMATCH;
			scores['A']['T'] = MISMATCH;
			scores['A']['G'] = MISMATCH;
			scores['A']['N'] = 0;
			scores['C']['a'] = MISMATCH;
			scores['C']['c'] = MATCH;
			scores['C']['t'] = MISMATCH;
			scores['C']['g'] = MISMATCH;
			scores['C']['n'] = 0;
			scores['C']['A'] = MISMATCH;
			scores['C']['C'] = MATCH;
			scores['C']['T'] = MISMATCH;
			scores['C']['G'] = MISMATCH;
			scores['C']['N'] = 0;
			scores['T']['a'] = MISMATCH;
			scores['T']['c'] = MISMATCH;
			scores['T']['t'] = MATCH;
			scores['T']['g'] = MISMATCH;
			scores['T']['n'] = 0;
			scores['T']['A'] = MISMATCH;
			scores['T']['C'] = MISMATCH;
			scores['T']['T'] = MATCH;
			scores['T']['G'] = MISMATCH;
			scores['T']['N'] = 0;
			scores['G']['a'] = MISMATCH;
			scores['G']['c'] = MISMATCH;
			scores['G']['t'] = MISMATCH;
			scores['G']['g'] = MATCH;
			scores['G']['n'] = 0;
			scores['G']['A'] = MISMATCH;
			scores['G']['C'] = MISMATCH;
			scores['G']['T'] = MISMATCH;
			scores['G']['G'] = MATCH;
			scores['G']['N'] = 0;
			mNucleotideMatrix = new Matrix("BLASTNish", scores);
		}
		return mNucleotideMatrix;
	}

	public static ReferenceSequence createRefSeqFromBioJava(
			org.biojava.bio.symbol.Alignment a) throws IllegalSymbolException,
			IndexOutOfBoundsException {
		ReferenceSequence refseq = new ReferenceSequence();
		refseq.setBases(getBJAlignmentSequence(a));
		refseq.setLength(a.length());
		Strain strain = new Strain();
		int readCount = 0;
		for (Object label : a.getLabels()) {
			readCount++;
			Read read = new Read();
			read.setId(readCount);
			read.setName(label.toString());
			List<Difference> diffs = new ArrayList<Difference>();
			int rLength = 0;
			int aStart = -1;
			int aEnd = 0;
			List<Difference> gaps = new ArrayList<Difference>();
			for (int i = 1; i <= a.length(); i++) {
				Symbol symbol = a.symbolAt(label, i);
				char rBase = getChar(symbol);
				char aBase = getChar(a.symbolAt(i));
				if (isGap(symbol)) {
					if (aStart > 0)
						gaps.add(new Difference(i, aBase, rLength, '-'));
				} else {
					if (aStart < 0)
						aStart = i;
					aEnd = i;
					rLength++;
					if (gaps.size() > 0) {
						for (Difference gap : gaps) {
							diffs.add(gap);
						}
						gaps.clear();
					}

					if (rBase != aBase) {
						diffs.add(new Difference(i, aBase, rLength, rBase));
					}
				}
			}
			read.setLength(rLength);
			SequenceSegment seg1 = new SequenceSegment(refseq, aStart, aEnd);
			SequenceSegment seg2 = new SequenceSegment(read, 1, rLength);
			Alignment alig = new Alignment(seg1, seg2, true, diffs);
			read.setAlignment(alig);
			strain.putRead(read.getIdInteger(), read);
			strain.setAlignmentFromReads();
		}
		refseq.addStrainWithNoId(strain);
		return refseq;
	}

	private static String getBJAlignmentSequence(
			org.biojava.bio.symbol.Alignment a) throws IllegalSymbolException,
			IndexOutOfBoundsException {
		StringBuffer bases = new StringBuffer(a.length());
		for (int i = 1; i <= a.length(); i++) {
			Symbol s = a.symbolAt(i);
			char c = getChar(s);
			bases.append(c);
		}
		return bases.toString();
	}

	/**
	 * @param symbol
	 * @return The char representation of this symbol. NOTE: seems to return '~'
	 *         for gaps.
	 * @throws IllegalSymbolException
	 */
	private static char getChar(Symbol symbol) throws IllegalSymbolException {
		try {
			if (symbol instanceof BasisSymbol) {
				// SimpleBasisSymbol, SimpleAtomicSymbol
				List symbols = ((BasisSymbol) symbol).getSymbols();
				Map<Object, Integer> counts = new HashMap<Object, Integer>();
				int max = 0;
				SimpleAtomicSymbol x = null;
				Object maxSym = null;
				for (Object s : symbols) {
					int count;
					Integer i = counts.get(s);
					if (i == null) {
						count = 1;
					} else {
						count = i.intValue() + 1;
					}
					if (count > max) {
						max = count;
						maxSym = s;

					}
					counts.put(s, count);
				}
				return NucleotideTools.nucleotideToken((Symbol) maxSym);

			} else {
				char c = NucleotideTools.nucleotideToken(symbol);
				return c;
			}
		} catch (IllegalSymbolException e) {
			return 'n';
		}
	}

	private static boolean isGap(Symbol symbol) {
		if (symbol.getMatches().getGapSymbol() == symbol) {
			return true;
		} else {
			if (symbol instanceof BasisSymbol) {
				List symbols = ((BasisSymbol) symbol).getSymbols();
				switch (symbols.size()) {
				case 0:
					return true;
				case 1:
					return symbol.getMatches().getGapSymbol() == symbols.get(0);
				default:
					return false;
				}
			} else {
				return false;
			}
		}
	}

	public static String[] parseFileName(String name) {
		// TODO Auto-generated method stub
		int dotIndex = name.lastIndexOf('.');
		String[] names = { name.substring(0, dotIndex),
				name.substring(dotIndex + 1) };
		return names;
	}

	public static String getFileNameBase(String name) {
		return parseFileName(name)[0];
	}

	public static void applyStrainsFromFile(
			ReferenceSequenceDisplayComponent pCanvas, File pFile)
			throws IOException {

		// Each internal array is a list of read names representing a strain.
		List<List<String>> strains = new ArrayList<List<String>>();

		// Open file reader
		BufferedReader br = new BufferedReader(new FileReader(pFile));

		if (pFile.getName().toLowerCase().endsWith(".caf")) {
			// Silently handle CAF files
			String line = br.readLine();
			List<String> strain = new ArrayList<String>();
			while (line != null) {
				if (line.startsWith("Is_contig")) {
					if (strain.size() > 0) {
						strains.add(strain);
					}
					strain = new ArrayList<String>();
				} else if (line.startsWith("Assembled_from")) {
					StringTokenizer st = new StringTokenizer(line);
					st.nextToken();
					strain.add(st.nextToken());
				}
				line = br.readLine();
			}
			if (strain.size() > 0) {
				strains.add(strain);
			}
		} else {
			// Parse file into Array of arrays. Assume each line is a space
			// separated list of read names
			String line = br.readLine();
			while (line != null) {
				line = line.trim();
				if (line.length() == 0) {
					continue;
				}
				List<String> strain = new ArrayList<String>();
				StringTokenizer st = new StringTokenizer(line);
				while (st.hasMoreElements()) {
					strain.add(st.nextElement().toString());
				}
				strains.add(strain);
				line = br.readLine();
			}
		}

		// close file
		br.close();

		// pass groups to canvas
		pCanvas.updateStrainsFromReadLists(strains);
	}

	/**
	 * Decode SAM hit data into an Alignment object
	 * 
	 * M alignment match (can be a sequence match or mismatch) I insertion to
	 * the reference D deletion from the reference N skipped region from the
	 * reference S soft clipping (clipped sequences present in SEQ) H hard
	 * clipping (clipped sequences NOT present in SEQ) P padding (silent
	 * deletion from padded reference) = sequence match X sequence mismatch
	 * 
	 * eg: 3S6=1X1=1P1I4=
	 * 
	 * @param pRead
	 *            Read
	 * @param pReferenceSequence
	 *            Reference
	 * @param pDir
	 *            True -> same strainer, False -> opposite
	 * @param pStart
	 *            leftmost position on Reference
	 * @param pScore
	 * @param pEValue
	 * @param pQuerySequence
	 *            Matching bases from Query
	 * @param pCigarString
	 *            Encoded match
	 * @return
	 */
	public static Alignment buildAlignmentFromCigarString(Read pRead,
			ReferenceSequence pReferenceSequence, boolean pDir, int pStart,
			int pScore, String pMapQual, String pQuerySequence,
			String pCigarString) {
		// get diffs from blast alignment
		ArrayList<Difference> diffs = new ArrayList<Difference>();
		int astart = pStart - 1;
		int qpos = 0;
		int apos = 0;
		int qstart = 0;

		Matrix scoreMatrix = getNucleotideMatrix();

		Pattern cigarRE = Pattern.compile("([0-9]+)([MIDNSHP=X])");
		Matcher cigarElements = cigarRE.matcher(pCigarString);
		while (cigarElements.find()) {
			int bitLen = Integer.parseInt(cigarElements.group(1));
			String bitType = cigarElements.group(2);

			char key = bitType.charAt(0);
			switch (key) {
			// H hard clipping (clipped sequences NOT present in SEQ)
			case 'H':
				// This can be ignored
				break;
			// S soft clipping (clipped sequences present in SEQ)
			case 'S':
				// clip bases from start/end
				if (qpos == 0 && apos == 0) {
					qstart += bitLen;
				}
				break;

			// X sequence mismatch
			// M alignment match (can be a sequence match or mismatch)
			case 'M':
			case 'X':
				// With 'M' we need to check each base
				for (int i = 0; i < bitLen; i++) {
					qpos++;
					apos++;
					char qChar = pQuerySequence.charAt(qstart + qpos - 1);
					char rChar = pReferenceSequence.getBase(astart + apos);
					if (scoreMatrix.getScore(qChar, rChar) != MATCH) {
						// create diff object and add it to the array
						diffs.add(new Difference(astart + apos, rChar, qstart
								+ qpos, qChar));
					}
				}
				break;
			// = sequence match
			case '=':
				qpos += bitLen;
				apos += bitLen;
				break;
			// I insertion to the reference
			case 'I':
				for (int i = 0; i < bitLen; i++) {
					qpos++;
					char qChar = pQuerySequence.charAt(qstart + qpos - 1);
					char rChar = '-';
					diffs.add(new Difference(astart + apos, rChar, qstart
							+ qpos, qChar));
				}
				break;
			// N skipped region from the reference, functionally same as
			// deletion...
			case 'N':
				// D deletion from the reference
			case 'D':
				for (int i = 0; i < bitLen; i++) {
					apos++;
					char qChar = '-';
					char rChar = pReferenceSequence.getBase(astart + apos);
					diffs.add(new Difference(astart + apos, rChar, qstart
							+ qpos, qChar));
				}
				break;
			// P padding (silent deletion from padded reference)
			case 'P':
				// it's silent. Do nothing.
			default:
				break;
			}
		}

		// create alignment object
		SequenceSegment ess = new SequenceSegment(pReferenceSequence, pStart,
				pStart + apos - 1);
		SequenceSegment rss = new SequenceSegment(pRead, qstart + 1, qstart
				+ qpos);
		Alignment a = new Alignment(ess, rss, pDir, diffs);
		a.score = pScore;
		a.eValue = getEvalueFromMapQual(pMapQual);

		// return alignment
		return a;
	}

	/**
	 * qual is -log_10(p). And e-value is sort of a probability of being
	 * wrong...
	 * 
	 * @param pMapQual
	 * @return eValue-like probability (10^(qual/-10))
	 */
	private static double getEvalueFromMapQual(String pMapQual) {
		int qual = Integer.parseInt(pMapQual);
		if (qual == 255) {
			// special value indicating no quality available
			return -1.;
		}
		return Math.pow(10., (qual / -10.));
	}

}
