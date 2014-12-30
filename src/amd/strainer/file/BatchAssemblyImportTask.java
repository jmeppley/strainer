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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.actions.AbstractTask;
import amd.strainer.display.actions.AssemblyFileReader;
import amd.strainer.display.actions.LoadDataDialog;
import amd.strainer.objects.ReferenceSequence;

public class BatchAssemblyImportTask extends AbstractTask {
	private String assemblyFileName = null;
	private String outputPrefix = null;
	private Set<Long> contigNumbers = null;
	private String qualityFileName = null;

	public BatchAssemblyImportTask(String pAssemblyFileName,
			String pOutputPrefix, Set<Long> pContigNumbers) {
		assemblyFileName = pAssemblyFileName;
		outputPrefix = pOutputPrefix;
		contigNumbers = pContigNumbers;
		message = "Initializing...";
	}

	public BatchAssemblyImportTask(String pAssemblyFileName,
			String pOutputPrefix, String pQualityFile, Set<Long> pContigNumbers) {
		assemblyFileName = pAssemblyFileName;
		outputPrefix = pOutputPrefix;
		contigNumbers = pContigNumbers;
		qualityFileName = pQualityFile;
		message = "Initializing...";
	}

	@Override
	protected Object doStuff() {
		File assemblyFile = new File(assemblyFileName);
		if (!assemblyFile.exists()) {
			current = -1;
			message = "Assembly file not found: " + assemblyFileName;
			errorTitle = "Missing file";
			return null;
		}
		if (!assemblyFile.canRead()) {
			current = -1;
			message = "Assembly file unreadable: " + assemblyFileName;
			errorTitle = "Inaccessible file";
			return null;
		}

		int assemblyFileType = LoadDataDialog.getAssemblyFileType(assemblyFile);
		AssemblyFileReader afr;
		if (assemblyFileType == LoadDataDialog.ACE) {
			afr = new AceFileReader(assemblyFile);
		} else if (assemblyFileType == LoadDataDialog.CAF) {
			afr = new CAFFileReader(assemblyFile);
		} else {
			current = -1;
			message = "Unknown file type: "
					+ Integer.toString(assemblyFileType);
			errorTitle = "Uknown file type";
			return null;
		}

		message = "Creating iterator";
		System.out.println("Creating iterator...");
		Iterator<ReferenceSequence> cit;
		try {
			cit = afr.getContigIterator(contigNumbers);

			if (qualityFileName == null || qualityFileName.trim().length() == 0) {
				message = "reading ace file";

				// if there is no quality data to load, don't save data as we
				// go, just write
				// out each contig when it's finished
				while (cit.hasNext()) {
					if (isInterrupted()) {
						throw new InterruptedException("Load Canceled");
					}

					ReferenceSequence refSeq = cit.next();

					if (isInterrupted()) {
						throw new InterruptedException("Load Canceled");
					}

					System.out.println("Writing contig: " + refSeq.getName());
					writeStrainerFiles(refSeq, outputPrefix);
				}
			} else {
				// The quality data is the big memory hog, so when requested, it
				// should be
				// better to save all the contigs and run through all the
				// quality data in one go
				message = "reading ace file";
				Set<ReferenceSequence> refSeqs = new HashSet<ReferenceSequence>();
				while (cit.hasNext()) {
					if (isInterrupted()) {
						throw new InterruptedException("Load Canceled");
					}
					refSeqs.add(cit.next());
				}

				if (isInterrupted()) {
					throw new InterruptedException("Load Canceled");
				}

				message = "loading quality data";
				QualityData.bulkLoadQualityData(refSeqs.iterator(), new File(
						qualityFileName), this);

				if (isInterrupted()) {
					throw new InterruptedException("Load Canceled");
				}

				message = "writing files";
				cit = refSeqs.iterator();
				while (cit.hasNext()) {
					if (isInterrupted()) {
						throw new InterruptedException("Load Canceled");
					}
					writeStrainerFiles(cit.next(), outputPrefix);
				}

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			current = -1;
			message = "Ace file not found: " + assemblyFileName;
			errorTitle = "Missing file";
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			current = -1;
			message = "Ace file unreadable: " + assemblyFileName;
			errorTitle = "Inaccessible file";
			return null;
		} catch (InterruptedException e) {
			current = -1;
			return null;
		} catch (OutOfMemoryError e) {
			message = e.getMessage();
			errorTitle = "Out of memory";
			current = -1;
			return null;
		}

		// return any object
		return Boolean.TRUE;
	}

	private void writeStrainerFiles(ReferenceSequence pRefSeq,
			String pFilePrefix) throws IOException {
		String sep = "_";
		if (pFilePrefix.charAt(pFilePrefix.length() - 1) == '/') {
			sep = "";
		}

		String fastaFile = pFilePrefix + sep + pRefSeq.getName() + ".fasta";
		String xmlFile = pFilePrefix + sep + pRefSeq.getName() + ".xml";

		// writeFastaFile(pRefSeq,fastaFile);
		PrintStream ps = new PrintStream(new FileOutputStream(fastaFile));
		ps.println(">" + pRefSeq.getName() + " " + pRefSeq.getLength());
		ps.println(pRefSeq.getBases());
		ps.close();

		// writeStrainerXML(pRefSeq,xmlFile);
		amd.strainer.file.Util.writeStrainsToXML(pRefSeq, new File(xmlFile),
				"default", true);
	}

	public void doOnError(PaneledReferenceSequenceDisplay pParent) {
		// do nothing
	}
}
