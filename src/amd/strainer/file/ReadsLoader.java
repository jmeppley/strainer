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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.biojava.bio.program.sax.BlastLikeSAXParser;
import org.biojava.bio.program.sax.SAMSAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import amd.strainer.display.actions.GetReferenceFromFileTask;
import amd.strainer.display.actions.Task;
import amd.strainer.objects.ReferenceSequence;

/**
 * A collection of static methods to get read alignment data from files.
 * 
 * @author jmeppley
 * 
 */
public class ReadsLoader {

	/**
	 * Reads in alignments from a BLAST output file and adds Read objects to
	 * ReferenceSequence. Initial strains are formed by joining mate-pairs
	 * together into strains with 2 Reads in them. If reads have no mate-pair in
	 * the data set or the pair's alignments are farther apart than expected for
	 * a clone, reads are left in solo strains. In the latter case, Reads also
	 * have the isBadClone() flag set to true.
	 * 
	 * @param pRefSeq
	 *            the ReferenceSequence object to add reads to
	 * @param pReadsFileName
	 *            The name of the BLAST output file
	 * @param pSmallClone
	 *            smallest expected clone size
	 * @param pBigClone
	 *            largest expected clone size
	 * @throws SAXException
	 *             if there is an error parsing the BLAST file
	 * @throws IOException
	 *             if there is an error accessing the BLAST file
	 */
	public static void addReadsFromFileToReferenceSequence(
			ReferenceSequence pRefSeq, String pReadsFileName,
			String pAlignmentFileType, int pSmallClone, int pBigClone)
			throws SAXException, IOException {
		loadRefSeqReadAlignmentsFromFile(new File(pReadsFileName), pRefSeq,
				pAlignmentFileType, pSmallClone, pBigClone);
	}

	/**
	 * Reads in alignments from a BLAST output file and adds Read objects to
	 * ReferenceSequence. Initial strains are formed by joining mate-pairs
	 * together into strains with 2 Reads in them. If reads have no mate-pair in
	 * the data set or the pair's alignments are farther apart than expected for
	 * a clone, reads are left in solo strains. In the latter case, Reads also
	 * have the isBadClone() flag set to true.
	 * 
	 * @param pRefSeq
	 *            the ReferenceSequence object to add reads to
	 * @param pReadsFileName
	 *            The name of the BLAST/SAM alignment file
	 * @param pAlignmentFileType
	 *            The type of alignment file (BLAST with reads in DB, BLAST with
	 *            reference in DB, SAM)
	 * @param pSmallClone
	 *            smallest expected clone size
	 * @param pBigClone
	 *            largest expected clone size
	 * @param pTask
	 *            task to take progress updates (may be null)
	 * @throws SAXException
	 *             if there is an error parsing the BLAST file
	 * @throws IOException
	 *             if there is an error accessing the BLAST file
	 */
	public static void addReadsFromFileToReferenceSequence(
			ReferenceSequence pRefSeq, String pReadsFileName,
			String pAlignmentFileType, int pSmallClone, int pBigClone,
			Task pTask) throws SAXException, IOException {
		loadRefSeqReadAlignmentsFromFile(new File(pReadsFileName), pRefSeq,
				pAlignmentFileType, pSmallClone, pBigClone, pTask);
	}

	/**
	 * Reads in alignments from a BLAST output file and adds Read objects to
	 * ReferenceSequence. Initial strains are formed by joining mate-pairs
	 * together into strains with 2 Reads in them. If reads have no mate-pair in
	 * the data set or the pair's alignments are farther apart than expected for
	 * a clone, reads are left in solo strains. In the latter case, Reads also
	 * have the isBadClone() flag set to true.
	 * 
	 * @param pAlignmentsFile
	 *            The BLAST output file
	 * @param pRefSeq
	 *            the ReferenceSequence object to add reads to
	 * @param pAlignmentFileType
	 *            The type of alignment file (BLAST with reads in DB, BLAST with
	 *            reference in DB, SAM)
	 * @param pSmallClone
	 *            smallest expected clone size
	 * @param pBigClone
	 *            largest expected clone size
	 * @throws SAXException
	 *             if there is an error parsing the BLAST file
	 * @throws IOException
	 *             if there is an error accessing the BLAST file
	 */
	public static void loadRefSeqReadAlignmentsFromFile(File pAlignmentsFile,
			ReferenceSequence pRefSeq, String pAlignmentFileType,
			int pSmallClone, int pBigClone) throws SAXException, IOException {
		loadRefSeqReadAlignmentsFromFile(pAlignmentsFile, pRefSeq,
				pAlignmentFileType, pSmallClone, pBigClone, null);
	}

	/**
	 * Reads in alignments from a BLAST output file and adds Read objects to
	 * ReferenceSequence. Initial strains are formed by joining mate-pairs
	 * together into strains with 2 Reads in them. If reads have no mate-pair in
	 * the data set or the pair's alignments are farther apart than expected for
	 * a clone, reads are left in solo strains. In the latter case, Reads also
	 * have the isBadClone() flag set to true.
	 * 
	 * @param pAlignmentsFile
	 *            The BLAST output file
	 * @param pRefSeq
	 *            the ReferenceSequence object to add reads to
	 * @param readsAreQuery
	 * @param pSmallClone
	 *            smallest expected clone size
	 * @param pBigClone
	 *            largest expected clone size
	 * @param pTask
	 *            Task to take progress updates, may be null
	 * @throws SAXException
	 *             if there is an error parsing the BLAST file
	 * @throws IOException
	 *             if there is an error accessing the BLAST file
	 */
	public static void loadRefSeqReadAlignmentsFromFile(File pAlignmentsFile,
			ReferenceSequence pRefSeq, String pAlignmentFileType,
			int pSmallClone, int pBigClone, Task pTask) throws SAXException,
			IOException {

		// TODO: get score cutoff from user, for now, default to 0 for blast and
		// no-cutoff for SAM
		Integer scoreCutoff = null;

		// get the Alignment input as a Stream
		InputStream is = new FileInputStream(pAlignmentsFile);

		XMLReader parser;
		if (pAlignmentFileType.equals(GetReferenceFromFileTask.SAM)) {
			parser = new SAMSAXParser();
		} else {
			// If it's not SAM, it's BLAST
			// make a BlastLikeSAXParser
			parser = new BlastLikeSAXParser();
			((BlastLikeSAXParser) parser).setModeLazy();
			scoreCutoff = 0;
		}

		// create handler to turn events into read alignments
		ContentHandler handler;
		if (pAlignmentFileType.equals(GetReferenceFromFileTask.BLAST_READS_DB)) {
			// fasta of reference seq(s) was blasted against db of reads
			handler = new ReverseBlastEventHandler(pRefSeq, pSmallClone,
					pBigClone, pTask);
		} else {
			// referenceSeq(s) was/were in the db and a lsit of reads was
			// blasted against that
			handler = new BlastEventHandler(pRefSeq, pSmallClone, pBigClone,
					scoreCutoff, pTask);
		}

		// set the parsers SAX event adapter
		parser.setContentHandler(handler);

		// parse the file, after this the result List will be populated with
		// SeqSimilaritySearchResults
		parser.parse(new InputSource(is));
	}

	/**
	 * Reads in the details of a Strainer XML file into a ReferenceSequence
	 * object.
	 * 
	 * @param pRefSeq
	 *            The ReferenceSequence bject to which Reads and Strains will be
	 *            added
	 * @param pStrainsFile
	 *            Strainer XML file
	 * @throws IOException
	 *             if the Strainer file cannot be accessed
	 * @throws SAXException
	 *             if the Strainer XML file cannot be parsed
	 * @throws ParserConfigurationException
	 *             if there is an configuration error
	 * @throws FactoryConfigurationError
	 *             if there is an configuration error
	 */
	public static void addStrainedReadsFromFileToReferenceSequence(
			ReferenceSequence pRefSeq, File pStrainsFile) throws IOException,
			SAXException, ParserConfigurationException,
			FactoryConfigurationError {
		addStrainedReadsFromFileToReferenceSequence(pRefSeq, pStrainsFile, null);
	}

	/**
	 * Reads in the details of a Strainer XML file into a ReferenceSequence
	 * object.
	 * 
	 * @param pRefSeq
	 *            The ReferenceSequence bject to which Reads and Strains will be
	 *            added
	 * @param pStrainsFile
	 *            Strainer XML file
	 * @throws IOException
	 *             if the Strainer file cannot be accessed
	 * @throws SAXException
	 *             if the Strainer XML file cannot be parsed
	 * @throws ParserConfigurationException
	 *             if there is an configuration error
	 * @throws FactoryConfigurationError
	 *             if there is an configuration error
	 */
	public static void addStrainedReadsFromFileToReferenceSequence(
			ReferenceSequence pRefSeq, File pStrainsFile, Task pTask)
			throws IOException, SAXException, ParserConfigurationException,
			FactoryConfigurationError {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		DefaultHandler handler = new StrainXMLHandler3(pRefSeq, pTask);
		parser.parse(pStrainsFile, handler);
	}
}
