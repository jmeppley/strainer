package amd.strainer.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import amd.strainer.GlobalSettings;
import amd.strainer.algs.SegmentLinker;
import amd.strainer.algs.SegmentStrainer;
import amd.strainer.algs.SimpleGeneCrawler;
import amd.strainer.algs.StrainerResult;
import amd.strainer.algs.Util;
import amd.strainer.file.ReadsLoader;
import amd.strainer.file.ReferenceSequenceLoader;
import amd.strainer.objects.Gene;
import amd.strainer.objects.ReferenceSequence;

public class TestSegmentStrainers extends TestCase {
	ReferenceSequence referenceSequence = null;
	List<Gene> genes = new ArrayList<Gene>();
	
	protected void setUp() throws Exception {
		super.setUp();
		
		GlobalSettings.setGenePrefix ( Constants.GENE_PREF);
		GlobalSettings.setAnnotationList ( GlobalSettings.parseCommaList(Constants.ANNOTATION_LIST));
		
		referenceSequence = ReferenceSequenceLoader.getRefSeqFromSequenceFile(Constants.REF_SEQ_FILE,ReferenceSequenceLoader.GENBANK);
		ReadsLoader.addStrainedReadsFromFileToReferenceSequence(referenceSequence,new File(Constants.STRAINER_XML_FILE));

		// find specific genes
		genes.add(referenceSequence.genes.get(Constants.TEST_GENE_1));
		genes.add(referenceSequence.genes.get(Constants.TEST_GENE_2));
		genes.add(referenceSequence.genes.get(Constants.TEST_GENE_3));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		referenceSequence = null;
		genes.clear();
	}

	/*
	 * Test method for segment linker and gene crawler
	 */
	public void testSegmentLinkerVsSimpleGeneCrawler() {
		try {
			for (Gene gene : genes) {
				SegmentStrainer sgc = new SimpleGeneCrawler();
				sgc.setSegment(gene);
				StrainerResult sgcr = sgc.getStrains();
				
				SegmentStrainer sl = new SegmentLinker();
				sl.setSegment(gene);
				StrainerResult slr = sl.getStrains();
				
				assertTrue(Util.compareStrainGroups(slr,sgcr));
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.assertTrue(e.getMessage(),false);
		}
	}

}
