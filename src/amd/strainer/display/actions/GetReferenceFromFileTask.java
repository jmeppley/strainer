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
package amd.strainer.display.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.biojava.bio.BioException;
import org.xml.sax.SAXException;

import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.file.QualityData;
import amd.strainer.objects.ReferenceSequence;

/**
 * Generates a ReferenceSequence object from two files. The first file indicates
 * the ReferenceSequence file which can be in Fasta or GenBank format as
 * specified in the second constructor argument. The second file indicates read
 * alignments and can be a BLAST output or Strainer XML. If the reads are in a
 * BLAST file, minimum and maximum clone sizes should be specified.
 * <P>
 * When parsing a BLAST output, the parameter ReadsAreQuery indicates the
 * "direction" of the BLAST search. If the reads were used as query sequences
 * against a database with potential reference sequences, set this to true. Only
 * reads for which the given reference sequence is the best will be used. If the
 * reference sequence was used as a query against a database of reads, then all
 * matched reads will be used, so be sure to set a reasonable cutoff.
 * 
 * 
 * @author jmeppley
 * 
 */
public class GetReferenceFromFileTask extends AbstractTask {
	public static final String BLAST_READS_DB = "BLAST: reads in database";
	public static final String BLAST_READS_QUERY = "BLAST: reads as query";
	public static final String SAM = "SAM";
	public static final String[] ALIGNMENT_TYPE_LIST = { BLAST_READS_DB,
			BLAST_READS_QUERY, SAM };
	private File refSeqFile = null;
	private File readsFile = null;
	private File strainsFile = null;
	private File qualityFile = null;
	private String refSeqFileType = "GENBANK";
	private int smallClone;
	private int bigClone;

	private final PaneledReferenceSequenceDisplay mParent;
	private String alignmentFileType;

	/**
	 * @param pRefSeqFile
	 *            Location of ReferenceSequence file
	 * @param pReadsFile
	 *            Location of reads file
	 * @param pRefSeqFileType
	 *            Type of reference sequence file (Fasta or GenBank)
	 * @param pSmallClone
	 *            the minimum expeceted insert size (Clones smaller than this
	 *            are flagged)
	 * @param pBigClone
	 *            the maximum expeceted insert size (Clones larger than this are
	 *            flagged)
	 * @param pQualityFile
	 *            the list of quality score sequences
	 */
	public GetReferenceFromFileTask(PaneledReferenceSequenceDisplay pParent,
			File pRefSeqFile, File pReadsFile, String pRefSeqFileType,
			String pAlignmentFileType, int pSmallClone, int pBigClone,
			File pQualityFile) {
		// Compute length of task...
		mParent = pParent;
		refSeqFile = pRefSeqFile;
		readsFile = pReadsFile;
		alignmentFileType = pAlignmentFileType;
		refSeqFileType = pRefSeqFileType;
		smallClone = pSmallClone;
		bigClone = pBigClone;
		qualityFile = pQualityFile;
	}

	/**
	 * @param pRefSeqFile
	 *            Location of ReferenceSequence file
	 * @param pReadsFile
	 *            Location of reads file
	 * @param pRefSeqFileType
	 *            Type of reference sequence file (Fasta or GenBank)
	 * @param pSmallClone
	 *            the minimum expeceted insert size (Clones smaller than this
	 *            are flagged)
	 * @param pBigClone
	 *            the maximum expeceted insert size (Clones larger than this are
	 *            flagged)
	 */
	public GetReferenceFromFileTask(PaneledReferenceSequenceDisplay pParent,
			File pRefSeqFile, File pReadsFile, String pRefSeqFileType,
			String pAlignmentFileType, int pSmallClone, int pBigClone) {
		// Compute length of task...
		mParent = pParent;
		refSeqFile = pRefSeqFile;
		readsFile = pReadsFile;
		refSeqFileType = pRefSeqFileType;
		alignmentFileType = pAlignmentFileType;
		smallClone = pSmallClone;
		bigClone = pBigClone;
	}

	/**
	 * @param pRefSeqFile
	 *            Location of ReferenceSequence file
	 * @param pStrainsFile
	 *            Location of strainer XML file
	 * @param pRefSeqFileType
	 *            Type of reference sequence file (Fasta or GenBank)
	 * @param pQualityFile
	 *            the list of quality score sequences
	 */
	public GetReferenceFromFileTask(PaneledReferenceSequenceDisplay pParent,
			File pRefSeqFile, File pStrainsFile, String pRefSeqFileType,
			File pQualityFile) {
		// Compute length of task...
		mParent = pParent;
		refSeqFile = pRefSeqFile;
		strainsFile = pStrainsFile;
		refSeqFileType = pRefSeqFileType;
		qualityFile = pQualityFile;
	}

	/**
	 * @param pRefSeqFile
	 *            Location of ReferenceSequence file
	 * @param pStrainsFile
	 *            Location of strainer XML file
	 * @param pRefSeqFileType
	 *            Type of reference sequence file (Fasta or GenBank)
	 */
	public GetReferenceFromFileTask(PaneledReferenceSequenceDisplay pParent,
			File pRefSeqFile, File pStrainsFile, String pRefSeqFileType) {
		// Compute length of task...
		mParent = pParent;
		refSeqFile = pRefSeqFile;
		strainsFile = pStrainsFile;
		refSeqFileType = pRefSeqFileType;
	}

	@Override
	protected Object doStuff() {
		ReferenceSequence refSeq = null;
		try {
			message = "Loading Reference Sequence...";

			// TODO:5 it would be nice to measure progress by file position
			// (1) set length of task to total length of files
			// (2) set current to position in file (+ length of previous files)
			// (this depends on being able to get the info from a file reader)

			refSeq = amd.strainer.file.ReferenceSequenceLoader
					.getRefSeqFromSequenceFile(refSeqFile, refSeqFileType);

			if (isInterrupted()) {
				throw new InterruptedException("Cancelled");
			}

			message = "Loading Reads...";
			if (readsFile != null) {
				// load from blast output
				amd.strainer.file.ReadsLoader.loadRefSeqReadAlignmentsFromFile(
						readsFile, refSeq, alignmentFileType, smallClone,
						bigClone, this);
			} else {
				// load from STrainer XML
				amd.strainer.file.ReadsLoader
						.addStrainedReadsFromFileToReferenceSequence(refSeq,
								strainsFile, this);
				refSeq.strainsFile = strainsFile.getAbsolutePath();
			}

			if (qualityFile != null) {
				message = "Loading Quality...";
				QualityData.loadQualityData(refSeq, qualityFile, null, this);
			}

			// load graphics
			mParent.setReferenceSequence(refSeq);

		} catch (FactoryConfigurationError e) {
			errorTitle = "Configuration error";
			message = "unable to get a document builder factory";
			System.err.println(message);
			e.printStackTrace();
			current = -1;
		} catch (ParserConfigurationException e) {
			errorTitle = "Configuration error";
			message = "parser was unable to be configured";
			System.err.println(message);
			e.printStackTrace();
			current = -1;
		} catch (SAXException e) {
			message = "error parsing file: " + strainsFile;
			errorTitle = "Parsing error";
			System.err.println(message);
			if (e.getException() != null) {
				e.getException().printStackTrace();
			} else {
				e.printStackTrace();
			}
			current = -1;
		} catch (BioException e) {
			// Util.displayErrorMessage("Error parsing file",e.getMessage());
			errorTitle = "Error parsing file";
			Throwable cause = e.getCause();
			if (cause != null) {
				message = cause.toString();
			} else {
				message = e.toString();
			}
			System.err.println(message);
			e.printStackTrace();
			current = -1;
		} catch (FileNotFoundException e) {
			errorTitle = "File not found";
			message = "error: missing file: " + strainsFile;
			System.err.println(message);
			e.printStackTrace();
			current = -1;
		} catch (IOException e) {
			errorTitle = "Error accessing file";
			message = "error accessing file: " + strainsFile;
			System.err.println(message);
			e.printStackTrace();
			current = -1;
		} catch (InterruptedException e) {
			e.printStackTrace();
			errorTitle = "Loading cancelled";
			message = e.getMessage();
			current = -1;
		} catch (Throwable t) {
			errorTitle = "Unanticipated error";
			message = "unanticipated error:" + t.getMessage();
			System.err.println(message);
			t.printStackTrace(System.err);
			current = -1;
		}

		// notify timer thread
		done = true;

		// return anything
		return refSeq;
	}

	public void doOnError(PaneledReferenceSequenceDisplay pParent) {
		LoadDataDialog.showDialog(pParent);
	}
}
