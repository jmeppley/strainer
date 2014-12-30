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
 ***** END LICENSE BLOCK ***** */package amd.strainer.objects;

/**
 * The parent class for all sequence objects: REferenceSequence, Gene, Read ...
 * 
 * @author jmeppley
 *
 */
public class AbstractSequence implements Sequence, Cloneable {
	//////////////////
	// Accessors
	
	private int id = -1;
	/** the id number of this sequence */
	public int getId() {return id;}
	/** the id number of this sequence */
	public void setId(int pId) {
		id = pId;
		idInteger = new Integer(id);
	}
	private Integer idInteger = new Integer(-1);
	public Integer getIdInteger() { return idInteger; }
	
	private String name = null;
	/** the name of this sequence */
	public String getName() {return name;}
	/** the name of this sequence */
	public void setName(String pName) {name = pName;}
	
	private int length = -1;
	/** the length of this sequence */
	public int getLength() {return length;}
	/** the length of this sequence */
	public void setLength(int pLength) {length = pLength;}
	
	protected String bases = null;
	/** the sting of bases that make up this sequence */
	public String getBases() { return bases;}
	/** the sting of bases that make up this sequence */
	public void setBases(String pBases) {bases = pBases;}
	
	/** return the base at the specified position */
	public char getBase(int pPos) {
//		if (pPos>getBases().length()) {
//			return '-';
//		}
		return getBases().charAt(pPos-1);
	}
	
	public Object clone() {
		try {
			AbstractSequence c = (AbstractSequence) super.clone();
			c.setBases(bases);
			c.setId(id);
			c.setName(name);
			c.setLength(length);
			return c;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone issues!!!");
		}
	}

	protected boolean closed = false;

	public void close() {
		closed=true;
		bases = null;
	}
}
