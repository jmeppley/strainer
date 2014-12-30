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

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import amd.strainer.display.actions.Task;
import amd.strainer.objects.Alignment;
import amd.strainer.objects.Difference;
import amd.strainer.objects.QualifiedDifference;
import amd.strainer.objects.Read;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.SequenceSegment;
import amd.strainer.objects.Strain;

/**
 * Loads reads and strains into given REferenceSequence from XML file.  
 * This assumes that the Diff elemnts for each alignment are read in ascending order of pos.
 * 
 * @author jmeppley
 *
 */
public class StrainXMLHandler3 extends DefaultHandler {
	Task mTask = null;
	
	HashMap<Integer,Strain> strains = new HashMap<Integer,Strain>();
	Strain currentStrain = null;
	Integer currentReadId = null;
	Read currentRead = null;
	Integer currentMatePairId = null;
	ArrayList<Difference> currentDiffs = null;
	ReferenceSequence refSeq = null;
	int index=0;
	
	/**
	 * Simple constructor
	 *
	 */
	public StrainXMLHandler3(ReferenceSequence pRefSeq) {
		super();
		refSeq = pRefSeq;
	}

	/**
	 * Simple constructor
	 *
	 */
	public StrainXMLHandler3(ReferenceSequence pRefSeq, Task pTask) {
		super();
		refSeq = pRefSeq;
		mTask = pTask;
	}
	
	/**
	 *     Receive notification of the beginning of a document.	
	 */
	public void startDocument() {
	}

	/**
	 *     Receive notification of the beginning of an element.
	 */
	public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes atts) {
		if (mTask!=null && mTask.isInterrupted()) {
			throw new RuntimeException("Interrupted!");
		}

		//System.out.println("START: " + qName);
		if (qName.equals("Strains")) {
			try {
				if (atts.getValue("HasQualityData").equalsIgnoreCase("true")) {
					refSeq.hasQualityData = true;
				}	
			} catch (NullPointerException e) {
				// ignore if there is no HasQualityData param
			}
			
			try {
				// this sets up the progress bar to count strains hich is not ideal, but
				//  to correct this would require a change to the XML 
				//  (TODO:4 add number of reads to Strains XML header)
				mTask.setLengthOfTask(Integer.parseInt(atts.getValue("Size")));
			} catch (NullPointerException e) {
				// ignore if there is no HasQualityData param
			}

		} else if (refSeq!=null) { 
			if (qName.equals("Strain")) {
				currentStrain = new Strain();
				index++;
				
				if (mTask!=null && mTask.getLengthOfTask()>0) {
					mTask.setCurrent(index);
				}
				
				currentStrain.setId(index);
				try {
					if (atts.getValue("Open").equalsIgnoreCase("false")) {
						currentStrain.toggleOpen();
					}	
				} catch (NullPointerException e) {
					// ignore if there is no Open param
				}
			} else if (currentStrain!=null) {
				if (qName.equals("Read")) {
					currentRead = new Read();
					currentReadId = new Integer(atts.getValue("Id"));
					currentRead.setId(currentReadId.intValue());
					currentRead.setName(atts.getValue("Name"));
					currentRead.setLength(Integer.parseInt(atts.getValue("Length")));
					currentRead.setBadClone(atts.getValue("IsBadClone").equals("1"));
					if (currentRead.isRecombinant()!=(atts.getValue("IsRecombinant").equals("1"))) {
						currentRead.toggleRecombinant();
					}

					// record the mate pair id for this read
					currentMatePairId = new Integer(atts.getValue("MatePairId"));
					
					refSeq.reads.put(currentReadId,currentRead);
					// The following was moved to endElement because it
					//  needs alignment info if there is a mate pair involved
					//currentStrain.putRead(readId,currentRead);
				} else if (currentRead!=null) {
					if (qName.equals("Alignment")) {
						int start = Integer.parseInt(atts.getValue("Start"));
						int end = Integer.parseInt(atts.getValue("End"));
						boolean dir = atts.getValue("Dir").equals("1");
						currentDiffs=new ArrayList<Difference>();
//						a.setDiffs(currentDiffs);
						Alignment a = new Alignment(
								new SequenceSegment(refSeq,start,end),
								new SequenceSegment(currentRead,1,currentRead.getLength()),
								dir,
								currentDiffs);
						a.score = Integer.parseInt(atts.getValue("Score"));
						currentRead.setAlignment(a);
//						System.out.println(currentRead);
//						System.out.println(currentRead.getAlignment());
					} else if (currentDiffs!=null) {
						if (qName.equals("Diff")) {
							String qual = atts.getValue("Quality");
							if (qual==null) {
								currentDiffs.add(
									new Difference(
									// for historical reasons, XML label for the reference sequence's base nuber and nucleotide at a polymorphic site is "EntryPos" and "EntryBase"
									Integer.parseInt(atts.getValue("EntryPos")),
									atts.getValue("EntryBase").charAt(0),
									Integer.parseInt(atts.getValue("QueryPos")),
									atts.getValue("QueryBase").charAt(0)
								));	
							} else {
								refSeq.hasQualityData=true;
								currentDiffs.add(
									new QualifiedDifference(
										// for historical reasons, XML label for the reference sequence's base nuber and nucleotide at a polymorphic site is "EntryPos" and "EntryBase"
										Integer.parseInt(atts.getValue("EntryPos")),
										atts.getValue("EntryBase").charAt(0),
										Integer.parseInt(atts.getValue("QueryPos")),
										atts.getValue("QueryBase").charAt(0),
										Short.parseShort(qual)
								));	
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 *      Receive notification of character data.
	 */
	public void characters(char[] ch, int start, int length) {
	}

	/**
	 *      Receive notification of the end of an element.
	 */
	public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) {
		if (mTask!=null && mTask.isInterrupted()) {
			throw new RuntimeException("Interrupted!");
		}

		//		System.out.println("END: " + qName);
		if (refSeq!=null) {
			if (qName.equals("Strains")) {
				refSeq.strains = strains;
				refSeq.maxStrainId = index;
			} else if (currentStrain!=null) { 
				if (qName.equals("Strain")) {
					try {
						currentStrain.setAlignmentFromReads();
					} catch (RuntimeException e) {
						System.out.println("Id:" + currentStrain.getId());
//						throw e;
						e.printStackTrace();
					}
					strains.put(new Integer(index),currentStrain);
					currentStrain=null;
					currentRead=null;
					currentDiffs=null;
				} else if (currentRead!=null) {
					if (qName.equals("Read")) {
						// Current read is done. finish up
						
						// check for matePair
						int mpId = currentMatePairId.intValue();
						if (mpId>=0) {
							Read mp = refSeq.reads.get(currentMatePairId);
							if (mp!=null) {
								// this call associates pair in both directions
								currentRead.setMatepair(mp);
							}
						}
						
						// add to strain
						currentStrain.putRead(currentReadId,currentRead);
						currentRead=null;
						currentDiffs=null;
					} else if (currentDiffs!=null) {
						if (qName.equals("Alignment")) {
							// this must be done here to set up some arrays in Alignment
							//  because the diff array has changed since it was set.
							currentRead.getAlignment().setDiffs(currentDiffs);
							currentDiffs = null;
						}
					}
				}
			}
		}
	}		
	
 	/**
 	 *    Receive notification of the end of a document.
 	 */
 	public void endDocument() {
 		
 	} 	 
	
}
