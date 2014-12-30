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
import java.io.IOException;

import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.file.QualityData;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.Strain;

/**
 * Takes an AssemblyFileReader object (Ace or CAF) and generates a
 * ReferenceSequence object
 * 
 * @author jmeppley
 *
 */
public class GetContigFromAssemblyByNumberTask extends AbstractTask {
	private AssemblyFileReader assemblyFileReader = null;
	private long contigNumber = -1;
	PaneledReferenceSequenceDisplay mParent;
	private final File qualityFile;

	public GetContigFromAssemblyByNumberTask(
			PaneledReferenceSequenceDisplay pParent,
			AssemblyFileReader pAssemblyFileReader, long pContigNumber) {
		this(pParent, pAssemblyFileReader, pContigNumber, null);
	}

	public GetContigFromAssemblyByNumberTask(
			PaneledReferenceSequenceDisplay pParent,
			AssemblyFileReader pAssemblyFileReader, long pContigNumber,
			File pQualityFile) {
		mParent = pParent;
		assemblyFileReader = pAssemblyFileReader;
		contigNumber = pContigNumber;
		qualityFile = pQualityFile;
	}

	/**
	 * The actual long running task. This runs in a SwingWorker thread.
	 */
	@Override
	protected Object doStuff() {
		ReferenceSequence refSeq = null;
		try {
			message = "Loading Reads...";

			// TODO:5 it would be nice to measure progress by file position (?)
			refSeq = assemblyFileReader.getContigDetailsFromNumber(
					contigNumber, this);

			// make sure strains are initialized
			for (Strain strain : refSeq.strains.values()) {
				strain.initializeGraphics();
			}

			// load quality data
			if (qualityFile != null) {
				message = "Loading Quality...";
				QualityData.loadQualityData(refSeq, qualityFile, null, this);
			}

			message = "rendering data ...";
			System.out.println("Finished loading data");
		} catch (IOException e) {
			message = e.getMessage();
			errorTitle = "error accessing file: "
					+ assemblyFileReader.getAssemblyFileName();
			System.err.println(errorTitle);
			e.printStackTrace();
			current = -1;
		} catch (InterruptedException e) {
			errorTitle = "Interrupted";
			message = e.toString();
			current = -1;
		} catch (Exception e) {
			errorTitle = "unanticipated error";
			message = e.toString();
			System.err.println(errorTitle);
			e.printStackTrace(System.err);
			current = -1;
		}

		// set the reference seqeucen
		mParent.setReferenceSequence(refSeq);

		// notify timer that we're done
		done = true;

		// return something
		return refSeq;
	}

	public void doOnError(PaneledReferenceSequenceDisplay pParent) {
		// do nothing for now
	}
}
