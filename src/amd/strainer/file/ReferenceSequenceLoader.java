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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.symbol.Location;

import amd.strainer.GlobalSettings;
import amd.strainer.objects.Gene;
import amd.strainer.objects.ReferenceSequence;

/**
 * Three versions of the static getRefSeqFromSequenceFile method that reads a sequence
 * file (FASTA, GenBank, ...) into a ReferenceSequence object.
 * 
 * @author jmeppley
 */
public class ReferenceSequenceLoader {
	/**
	 * String constant to indicate Fasta format 
	 */
	public static final String FASTA = "FASTA";
	/**
	 * String constant to indicate GenBank format
	 */
	public static final String GENBANK = "GENBANK";
	/**
	 * String constant to indicate EMBL format. (Completely untested)
	 */
	public static final String EMBL = "EMBL";
	/**
	 * String constant to indicate GenPept format
	 */
	public static final String GENPEPT = "GENPEPT";
	/**
	 * String constant indiacting DNA alphabet (as opposed to AminoAcid or some such) 
	 */
	public static final String DNA = "DNA";
	
	/**
	 * This method will read any DNA seq file supported by SeqIOTools it takes 
	 * two arguments, the first is the file name the second is the format type
	 *<p>
	 * Allowed formats are: (case insensitive).
	 *<ul>
	 * FASTA<br>
	 * EMBL<br>
	 * GENBANK<br>
	 * GENPEPT<br>
	 *</ul>
	 *Only FASTA and GENBANK have been rigorously tested 
	 * <p>
	 * Sequence types are assumed to be: DNA
	 * 
	 * @param pFileName path to the data file
	 * @param fileType the file's format (FASTA, GENBANK, etc)
	 * @return a ReferenceSequence object. Initial strain associations are mate-pairs
	 * @throws BioException if BioJava methods can't parse file
	 * @throws FileNotFoundException if file does not exist on filesystem
	 */
	public static ReferenceSequence getRefSeqFromSequenceFile(String pFileName, String fileType) throws BioException, FileNotFoundException {
		//prepare a BufferedReader for file io
		BufferedReader br = new BufferedReader(new FileReader(pFileName));
		
		return ReferenceSequenceLoader.getRefSeqFromSequenceFile(br, fileType);
	}
	
	
	/**
	 * This method will read any DNA seq file supported by SeqIOTools it takes 
	 * two arguments, the first is the file name the second is the format type
	 *<p>
	 * Allowed formats are: (case insensitive).
	 *<ul>
	 * FASTA<br>
	 * EMBL<br>
	 * GENBANK<br>
	 * GENPEPT<br>
	 *</ul>
	 *Only FASTA and GENBANK have been rigorously tested 
	 * <p>
	 * Sequence types are assumed to be: DNA
	 * 
	 * @param pFile the data file
	 * @param fileType the file's format (FASTA, GENBANK, etc)
	 * @return a ReferenceSequence object. Initial strain associations are mate-pairs
	 * @throws BioException if BioJava methods can't parse file
	 * @throws FileNotFoundException if file does not exist on filesystem
	 */
	public static ReferenceSequence getRefSeqFromSequenceFile(File pFile, String fileType) throws BioException, FileNotFoundException {
		//prepare a BufferedReader for file io
		BufferedReader br = new BufferedReader(new FileReader(pFile));
		
		return ReferenceSequenceLoader.getRefSeqFromSequenceFile(br, fileType);
	}
	
	/**
	 * This method will read any DNA seq file supported by SeqIOTools it takes 
	 * two arguments, the first is the file name the second is the format type
	 *<p>
	 * Allowed formats are: (case insensitive).
	 *<ul>
	 * FASTA<br>
	 * EMBL<br>
	 * GENBANK<br>
	 * GENPEPT<br>
	 *</ul>
	 *Only FASTA and GENBANK have been rigorously tested 
	 * <p>
	 * Sequence types are assumed to be: DNA
	 * 
	 * @param pBR a BufferedReader from the data file
	 * @param fileType the file's format (FASTA, GENBANK, etc)
	 * @return a ReferenceSequence object. Initial strain associations are mate-pairs
	 * @throws BioException if BioJava methods can't parse file
	 */
	public static ReferenceSequence getRefSeqFromSequenceFile(BufferedReader pBR, String fileType) throws BioException {
		//the Alphabet
		String alpha = DNA;
		
		/*
		 * get a Sequence Iterator over all the sequences in the file.
		 * SeqIOTools.fileToBiojava() returns an Object. If the file read
		 * is an alignment format like MSF an Alignment object is returned
		 * otherwise a SequenceIterator is returned.
		 */
		SequenceIterator iter =
			(SequenceIterator)SeqIOTools.fileToBiojava(fileType, alpha, pBR);
		
		if (iter.hasNext()) {
			// just get first entry
			Sequence entrySequence = iter.nextSequence();
			
			ReferenceSequence refSeq = new ReferenceSequence();
			refSeq.setId(-1);
			refSeq.setName(entrySequence.getName());
			refSeq.setLength(entrySequence.length());
			refSeq.setBases(entrySequence.seqString());
			
			System.out.println("Reference sequence name: " + refSeq.getName());
			System.out.println("Sequence: " + refSeq.getBases().substring(0,20) + "...");
			
			if (entrySequence.getAnnotation().keys().size()>0) {
				setRefSeqGenes(refSeq,entrySequence.features());
			}
			
			return refSeq;
		}
		throw new BioException("No records found in Reference Sequence file");
	}
	
	private static void setRefSeqGenes(ReferenceSequence pRefSeq, Iterator pFeatures) {
		// get all features and group by type
		Map<String,Set<Gene>> featureGroups = new HashMap<String,Set<Gene>>();
		if (pFeatures.hasNext()) {
			while (pFeatures.hasNext()) {
				Object o = pFeatures.next();
				if (o instanceof StrandedFeature)  {
					StrandedFeature f = (StrandedFeature) o;
					
					String type = f.getType();
					Set<Gene> typesGenes = featureGroups.get(type);
					if (typesGenes==null) {
						typesGenes = new HashSet<Gene>();
						featureGroups.put(type,typesGenes);
					}
//					if (f.getType().equals("CDS")) {
					
					Annotation a = f.getAnnotation();
					
					// try to get name
					String name = null;
					try {
						name = a.getProperty("gene").toString();
					} catch (NoSuchElementException e) {
					}
					
					// if no name or "-" set to "hypothetical"
					if ((name==null)||name.equals("-")) {
						name = "hyp";
					}
					
					Location l = f.getLocation();
					boolean dir = (f.getStrand()==StrandedFeature.POSITIVE);
					StringBuffer description = new StringBuffer(name);

					for (Object key : a.keys()) {
						description.append(" ## " + key + ":" + a.getProperty(key));
					}
					
					// create the gene object using this info
					Gene g = new Gene(name,pRefSeq,l.getMin(),l.getMax(),dir,description.toString());
					
					// add gene to list for this type
					typesGenes.add(g);
//					pRefSeq.genes.put(g.getName(),g);
				}
				
			}
			
			// TODO:5 disconnect GUI classes
			//  the following pops up a dialog if no annotation types list is passed in ahead of time
			// This means that command line programs using this code must have access to a display
			//  since simply loading a swing class requires a display.
			// So in the GUI there should be a step between loading the ref sequence and 
			//  the strains where the type dialog appears. This means the ref sequence will need to
			//  store all annotations (and types) until then
			
			
			// set that is automatically sorted by start pos
			Set<Gene> genes = new TreeSet<Gene>(new Comparator<Gene>() {

				public int compare(Gene arg0, Gene arg1) {
					return arg0.getStart() - arg1.getStart();
				}

				});
			
			String [] annotations = GlobalSettings.getAnnotationList();
			if (annotations == null) {
				// ask user to choose annotation types
				annotations = GeneTypeDialog.showDialog(featureGroups);
			}
			
			// use list of annotation types
			for(int i = 0; i<annotations.length; i++) {
				String type = annotations[i];
				Set<Gene> typeGenes = featureGroups.get(type);
				if (typeGenes!=null) {
					genes.addAll(typeGenes);
				}
			}

			if (GlobalSettings.getGenePrefix()==null) {
				// name genes from gene= tag
				Map<String,Integer> nameCounts = new HashMap<String,Integer>();
				for (Gene gene : genes) {
					String name = gene.getName();
					Integer count = nameCounts.get(name);
					if (count==null) {
						nameCounts.put(name,1);
					} else {
						count = count+1;
						nameCounts.put(name,count);
						name = name + " " + count.toString();
						gene.setName(name);
					}
					pRefSeq.genes.put(name,gene);
				}
			} else {
				// use user defined gene prefix to name genes
				int count = 1;
				for (Gene gene : genes) {
					String name = GlobalSettings.getGenePrefix() + count;
					count++;
					gene.setName(name);
					pRefSeq.genes.put(name,gene);
				}
			}
		}
		
		// clear global settings for next time
		GlobalSettings.setAnnotationList(null);
		GlobalSettings.setGenePrefix(null);
		// TODO:2 give user the option of making choices permanent
		//  currently "GlobalSettings" is a misnomer, as values only get used once
		// TODO:2 instead of clearing here, use old values to initialize dialog for the next load 
		
	}
	
}
