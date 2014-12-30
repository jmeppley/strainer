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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.biojava.bio.program.sax.BlastLikeSAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.actions.AbstractTask;
import amd.strainer.display.actions.BatchBlastDialog;
import amd.strainer.objects.ReferenceSequence;

/**
 * Turns a BLAST output into a collection of strainer XML files.<p>
 * provides command line main() method and Task implementation for loading reads aligned against multiple reference sequences. Each mate-pair is
 * associated with the reference sequence it aligns to best. The first
 * argument is a BLAST file in M0 format created by blasing a list of reads agains a database of
 * reference sequences. The second argument points to the directory to save the resulting XML files.
 * Arguments 3 and 4 indicate how far apart mate pairs are expected to
 * be. (average insert size). They are (in order) the min and max size.
 * The fifth argument is optional and points to a quality file for the reads.
 * 
 * @author jmeppley
 *
 */
public class BatchBlastTask extends AbstractTask {
	private String alignmentsFile;
	private String outputDir;
	private String qualityFile;
	private int minClone = 0;
	private int maxClone = 10000;

	public BatchBlastTask(String pBlastOutputFile, String pOutputDir, String pQualityFile, int pMinClone, int pMaxClone) {
		alignmentsFile = pBlastOutputFile;
		outputDir = pOutputDir;
		qualityFile = pQualityFile;
		minClone = pMinClone;
		maxClone = pMaxClone;
		message="Initializing...";
	}
	
	@Override
	protected Object doStuff() {
		try {
			//get the Blast input as a Stream
			InputStream is = new FileInputStream(alignmentsFile);
			//make a BlastLikeSAXParser
			BlastLikeSAXParser parser = new BlastLikeSAXParser();
			parser.setModeLazy();

			//create handler to turn events into read alignments
			BatchBlastEventHandler handler = new BatchBlastEventHandler(minClone,maxClone,this);

			//set the parsers SAX event adapter
			parser.setContentHandler(handler);
			
			//parse the file, after this the result List will be populated with
			//SeqSimilaritySearchResults
			parser.parse(new InputSource(is));
			
			System.out.println("done parsing");
			
			// import quality
			if (qualityFile!=null) {
				System.out.println("loading quality data");
				Iterator<ReferenceSequence> rit = handler.refSeqs.values().iterator();
				QualityData.bulkLoadQualityData(rit,new File(qualityFile),this);
			}
			
			System.out.println("writing to XML");
			
			// write XML
			handler.writeReferenceSequencesToXML(new File(outputDir));

			System.out.println("done");

		} catch (SAXException ex) {
			//XML problem
			ex.printStackTrace();
			errorTitle = "XML Parsing Error";
			message = ex.getMessage();
			current = -1;
		} catch (IOException ex) {
			//IO problem, possibly file not found
			ex.printStackTrace();
			message = ex.getMessage();
			errorTitle = "Error accessing file";
			current = -1;
		} catch (InterruptedException e) {
			message = e.getMessage();
			errorTitle = "Cancelled";
			current = -1;
		} catch (RuntimeException e) {
			message = e.getMessage();
			errorTitle = "Runtime Exception";
			current = -1;
		} catch (OutOfMemoryError e) {
			message = e.getMessage();
			errorTitle = "Out of memory";
			current = -1;
		}
		
		// return something
		return Boolean.TRUE;
	}

	public void doOnError(PaneledReferenceSequenceDisplay pParent) {
		BatchBlastDialog.showDialog(pParent);
	}
	
	public static void main(String [] args) {
		// argument 0 is location of blast file
		
		// set up vars
		String alignmentsFile = args[0];
		String outputDir = args[1];
		
		int maxClone=10000, minClone=0;
		try {
			maxClone = Integer.parseInt(args[2]);
			minClone = Integer.parseInt(args[3]);
			System.out.println("Using a clone size range of " + minClone + " to " + maxClone);
		} catch (NumberFormatException e) {
			System.err.println("Could not parse arguments.");
			System.exit(-1);
		}

		String qualityFile = null;
		if (args.length>4) {
			qualityFile = args[4];
		}

		BatchBlastTask task = new BatchBlastTask(alignmentsFile,outputDir,qualityFile,minClone,maxClone);
		task.doStuff();
	}
}
