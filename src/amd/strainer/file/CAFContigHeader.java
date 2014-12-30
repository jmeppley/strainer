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

import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import amd.strainer.file.CAFFileReader.RawRead;

/**
 * Container for information parsed from a contig header line in a CAF file.
 * Includes methods for parsing such a line.
 * 
 * @author jmeppley
 *
 */
public class CAFContigHeader {

	private String mName;
	private Map<String, RawRead> mReadMap;
	private long mNumber = -1;

	private final static Pattern contigNumberRE = Pattern
			.compile("_c([0-9]+)$");

	private CAFContigHeader() {
	}

	/**
	 * Parses a contig header line ("SEQUENCE : ..._c###") from a CAF file and
	 * returns an instance of ContigHeader loaded with name and number\
	 * 
	 * @param pHeaderLine
	 *            the full text of the contig header line (including
	 *            "Sequence : ")
	 * @return ContigHeader object
	 */
	public static CAFContigHeader parseHeaderLine(String pHeaderLine,
			Map<String, RawRead> pReadMap) {
		CAFContigHeader co = new CAFContigHeader();

		// CO Contig2729 1070 2 26 U
		try {
			// parse CO line
			StringTokenizer st = new StringTokenizer(pHeaderLine, " ");
			st.nextToken(); // skip "Sequence"
			st.nextToken(); // skip ":"

			// get name (second token)
			String name = st.nextToken().toString();
			co.setName(name);

			// pull digits out of name to get number;
			String numberString;
			Matcher readmatch = contigNumberRE.matcher(name);
			if (readmatch.find()) {
				numberString = readmatch.group(1);
			} else {
				// If we get here, the contig name didn't end in _c{num} as
				// expected, just pull digits
				StringBuffer digits = new StringBuffer();
				for (int i = 0; i < name.length(); i++) {
					char c = name.charAt(i);
					if (Character.isDigit(c)) {
						digits.append(c);
					}
				}
				numberString = digits.toString();
			}
			co.setNumber(Long.parseLong(numberString));
			co.mReadMap = pReadMap;

		} catch (Exception e) {
			System.err.println("Could not parse contig line: " + pHeaderLine);
			System.err.println(e.toString());
			System.err.println(co.toString());
			throw new RuntimeException("Unable to parse CAF file");
		}

		return co;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @return Returns the number.
	 */
	public long getNumber() {
		return mNumber;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	private void setName(String name) {
		mName = name;
	}

	/**
	 * @param number
	 *            The number to set.
	 */
	private void setNumber(long number) {
		mNumber = number;
	}

	public RawRead popRawRead(String pReadName) {
		return mReadMap.remove(pReadName);
	}

	/**
	 * @return a String that lists details about htis object of the form:
	 *         "Contig Sequence $name $number"
	 */
	@Override
	public String toString() {
		return "Contig Sequence " + getName() + " " + getNumber();
	}
}
