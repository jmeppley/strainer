/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
package org.biojava.bio.program.sax;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A SAX-like parser for dealing with SAM formatted output (a single dataset).
 * The events are made similar enough to biojava's BlastLikeSAXParser to be
 * compatible with my BlastEventHandler and BatchBlastEventHandler
 * 
 * This is not guaranteed to work on all SAM outputs and certainly not outside
 * of Strainer. But, it's a start.
 * 
 * @author jmeppley (MIT)
 * 
 *         Based heavily on the biojava SAX Blast parsers: Copyright 2000
 *         Cambridge Antibody Technology Group plc.
 * 
 *         This code released to the biojava project, May 2000 under the LGPL
 *         license.
 * 
 * @author Simon Brocklehurst (CAT)
 * @author Tim Dilks (CAT)
 * @author Colin Hardman (CAT)
 * @author Stuart Johnston (CAT)
 * @author Mathieu Wiepert (Mayo Foundation)
 * @author Keith James (Sanger Institute)
 * @author Mark Schreiber (NITD)
 * @author Travis Banks (AAFC)
 */
public final class SAMSAXParser extends AbstractNativeAppSAXParser {

	private final AttributesImpl oAtts = new AttributesImpl();
	private final QName oAttQName = new QName(this);

	private String oPrevQueryId = null;
	private final int oPrevEnd = 0;
	private static final int STARTUP = 0;
	private static final int IN_REFERENCES = 1;
	private static final int IN_HITS = 2;
	private static final int FINISHED = 3;

	/**
	 * Creates a new <code>BlastSAXParser</code> instance.
	 * 
	 * @exception SAXException
	 *                if an error occurs
	 */
	public SAMSAXParser() throws SAXException {
		// centralised setting of namespace prefix
		// the setting is cascaded everywhere else
		this.setNamespacePrefix("biojava");
		this.addPrefixMapping("biojava", "http://www.biojava.org");

		this.changeState(STARTUP);
	}

	/**
	 * <code>parse</code> initiates the parsing operation.
	 * 
	 * @param poSource
	 *            an <code>InputSource</code>.
	 * @exception IOException
	 *                if an error occurs.
	 * @exception SAXException
	 *                if an error occurs.
	 */
	@Override
	public void parse(InputSource poSource) throws IOException, SAXException {
		BufferedReader oContents;
		String oLine;

		this.changeState(STARTUP);

		// Use method form superclass
		oContents = this.getContentStream(poSource);
		// This sets contentHandler document for XSLT
		this.getContentHandler().startDocument();

		// set up qnames for tags
		QName collection = new QName(this,
				this.prefix("BlastLikeDataSetCollection"));
		QName dataset = new QName(this, this.prefix("BlastLikeDataSet"));
		QName queryIdTag = new QName(this, this.prefix("QueryId"));
		QName hitIdTag = new QName(this, this.prefix("HitId"));
		QName hitTag = new QName(this, this.prefix("Hit"));
		QName hspTag = new QName(this, this.prefix("HSP"));
		QName hspSummaryTag = new QName(this, this.prefix("HSPSummary"));
		QName blastLikeAlignmentTag = new QName(this,
				this.prefix("BlastLikeAlignment"));
		QName querySequenceTag = new QName(this, this.prefix("QuerySequence"));
		QName cigarStringTag = new QName(this, this.prefix("CigarString"));
		QName hitSequenceTag = new QName(this, this.prefix("HitSequence"));

		oAtts.clear();
		oAttQName.setQName("xmlns");
		// check if namespace configuration means attribute
		// should not be reported.
		if (!oAttQName.getLocalName().equals("")) {
			oAtts.addAttribute(oAttQName.getURI(), oAttQName.getLocalName(),
					oAttQName.getQName(), "CDATA", "");
		}

		oAttQName.setQName("xmlns:biojava");
		// check if namespace configuration means attribute
		// should not be reported.
		if (!oAttQName.getLocalName().equals("")) {
			oAtts.addAttribute(oAttQName.getURI(), oAttQName.getLocalName(),
					oAttQName.getQName(), "CDATA", "http://www.biojava.org");
		}
		this.startElement(collection, oAtts);

		oAtts.clear();
		this.startElement(dataset, oAtts);

		try {
			// loop over file
			oLine = oContents.readLine();

			// Skip over Reference headers, the don't give us much info. (I may
			// have to go back and build a hash of lengths...)
			this.changeState(IN_REFERENCES);
			while (oLine != null && oLine.startsWith("@")) {
				oLine = oContents.readLine();
			}
			this.changeState(IN_HITS);

			// Parse hits one line at a time
			while (oLine != null) {

				// Parse line first
				StringTokenizer st = new StringTokenizer(oLine, "\t");
				String queryName = st.nextToken();
				BigInteger flag = BigInteger.valueOf(Long.parseLong(st
						.nextToken()));

				// check bit 4 to see if it is mapped
				if (flag.testBit(2)) {
					// there was no match
					oLine = oContents.readLine();
					continue;
				}

				String referenceName = st.nextToken();
				String referenceStart = st.nextToken();
				String mappingQual = st.nextToken();
				String cigarString = st.nextToken();
				String mateRef = st.nextToken();
				String matePos = st.nextToken();
				String templateLength = st.nextToken();
				String sequence = st.nextToken();
				String qual = st.nextToken();
				String score = mappingQual; // Default to the mapping qual value
											// if no other score available
				while (true) {
					try {
						String tagstr = st.nextToken();
						if (tagstr.startsWith("AS")) {
							// Get score from string: AS:i:##
							score = tagstr.substring(5);
						}
					} catch (NoSuchElementException e) {
						// no more tokens
						break;
					}
				}

				// Calculations
				// queryStrand ("plus"/"minus" based on tag (16)
				String queryStrand = "plus";
				if (flag.testBit(4)) {
					queryStrand = "minus";
				}

				// check bit 1 to see if has a mate pair
				int end = 0;
				if (flag.testBit(0)) {
					if (flag.testBit(6)) {
						end = 1;
					} else {
						end = -1;
					}
				}

				// Send events
				if ((!queryName.equals(oPrevQueryId)) || (end != oPrevEnd)) {
					// biojava:QueryId, id
					oAtts.clear();
					if (oPrevQueryId != null) {
						// end previous read
						this.endElement(dataset);
					}
					// start new read
					this.startElement(dataset, oAtts);
					putAttribute(oAtts, "id", queryName);
					startElement(queryIdTag, oAtts);
					endElement(queryIdTag);
				}
				oPrevQueryId = queryName;

				// Start the Hit
				oAtts.clear();
				// if we need reference length:
				// putAttribute(oAtts, "sequenceLength",
				// referenceLengths.get(referenceName));
				startElement(hitTag, oAtts);

				// HitId (id, metadata "none")
				oAtts.clear();
				putAttribute(oAtts, "id", referenceName);
				putAttribute(oAtts, "metadata", "none");
				startElement(hitIdTag, oAtts);
				endElement(hitIdTag);

				// Start HSP, one per hit (in future, could split on Ns in Cigar
				// String)
				oAtts.clear();
				startElement(hspTag, oAtts);
				putAttribute(oAtts, "percentageIdentity", "0");
				putAttribute(oAtts, "queryStrand", queryStrand);
				putAttribute(oAtts, "score", score);
				putAttribute(oAtts, "alignmentSize",
						String.valueOf(sequence.length()));
				putAttribute(oAtts, "expectValue", mappingQual);
				putAttribute(oAtts, "numberOfIdentities", "0");
				putAttribute(oAtts, "hitStrand", "plus");
				startElement(hspSummaryTag, oAtts);
				endElement(hspSummaryTag);

				// alignment details
				oAtts.clear();
				startElement(blastLikeAlignmentTag, oAtts);
				putAttribute(oAtts, "startPosition", "-1");
				putAttribute(oAtts, "stopPosition", "-1");
				startElement(querySequenceTag, oAtts);
				// send sequence string
				this.characters(sequence.toCharArray(), 0, sequence.length());
				endElement(querySequenceTag);

				oAtts.clear();
				startElement(cigarStringTag, oAtts);
				// send cigar string
				this.characters(cigarString.toCharArray(), 0,
						cigarString.length());
				endElement(cigarStringTag);
				putAttribute(oAtts, "startPosition", referenceStart);
				putAttribute(oAtts, "stopPosition", "-1");
				startElement(hitSequenceTag, oAtts);
				endElement(hitSequenceTag);

				// TODO: Send quality string
				// TODO: Send mate pair info

				endElement(blastLikeAlignmentTag);
				endElement(hspTag);
				endElement(hitTag);

				oLine = oContents.readLine();

			} // end while
			this.changeState(FINISHED);
		} catch (IOException x) {
			System.out.println(x.getMessage());
			System.out.println("File read interrupted");
		} // end try/catch

		// at end of file...
		oContents.close();

		this.endElement(dataset);

		this.endElement(collection);
	}

	/**
	 * Conveinience method for setting attribute value
	 * 
	 * @param pAtts
	 *            Attribute map
	 * @param pKey
	 *            Attribute name
	 * @param pValue
	 *            Attribute value
	 */
	private void putAttribute(AttributesImpl pAtts, String pKey, String pValue) {
		oAttQName.setQName(pKey);
		pAtts.addAttribute(oAttQName.getURI(), oAttQName.getLocalName(),
				oAttQName.getQName(), "CDATA", pValue);
	}
}
