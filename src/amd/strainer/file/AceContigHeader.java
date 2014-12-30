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

import java.util.StringTokenizer;

/**
 * Container for information parsed from a CO line (contig header) in a
 * phred/phrap ace file. Includes methods for parsing such a line.
 * 
 * @author jmeppley
 *
 */
public class AceContigHeader {

	private int mLength = -1;
	private String mName;
	private long mNumber = -1;
	private int mReadCount = -1;

	private AceContigHeader() {
	}

	/**
	 * Parses a contig header line ("CO ...") from and ACE file and returns an
	 * instance of ContigHeader loaded with name, number, length, and number of
	 * reads
	 * 
	 * @param pCOLine
	 *            the full text of the contig line (ncluding "CO")
	 * @return ContigHeader object
	 */
	public static AceContigHeader parseHeaderLine(String pCOLine) {
		AceContigHeader co = new AceContigHeader();

		// CO Contig2729 1070 2 26 U
		try {
			// parse CO line
			StringTokenizer st = new StringTokenizer(pCOLine, " ");
			st.nextToken(); // skip "CO"

			// get name (second token)
			String name = st.nextToken().toString();
			co.setName(name);

			// pull digits out of name to get number
			StringBuffer digits = new StringBuffer();
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);
				if (Character.isDigit(c)) {
					digits.append(c);
				}
			}
			co.setNumber(Long.parseLong(digits.toString()));

			// get length and number of reads
			co.setLength(Integer.parseInt(st.nextToken()));
			co.setReadCount(Integer.parseInt(st.nextToken()));
		} catch (Exception e) {
			System.err.println("Could not parse CO line: " + pCOLine);
			System.err.println(e.toString());
			System.err.println(co.toString());
			throw new RuntimeException("Unable to parse ACE file");
		}

		return co;
	}

	/**
	 * @return Returns the length.
	 */
	public int getLength() {
		return mLength;
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
	 * @param length
	 *            The length to set.
	 */
	private void setLength(int length) {
		this.mLength = length;
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

	/**
	 * @return Returns the readCount.
	 */
	public int getReadCount() {
		return mReadCount;
	}

	/**
	 * @param readCount
	 *            The readCount to set.
	 */
	private void setReadCount(int readCount) {
		mReadCount = readCount;
	}

	/**
	 * @return a String that lists details about htis object of the form:
	 *         "CO $name $number $length $reads"
	 */
	@Override
	public String toString() {
		return "CO " + getName() + " " + getNumber() + " " + getLength() + " "
				+ getReadCount();
	}
}
