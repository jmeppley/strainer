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
 * Represents a difference at one position in an alignment
 * 
 * @author jmeppley
 *
 */
public class Difference implements Cloneable {
	
	private int position1 = -1;
	/** the position this diff falls on sequence 1 */
	public int getPosition1() {return position1;}
	/** the position this diff falls on sequence 1 */
	public void setPosition1(int pPosition1) {position1 = pPosition1;}
	
	private char base1 = 'n';
	/** the base in sequence 1 at this position */
	public char getBase1() {return base1;}
	/** the base in sequence 1 at this position */
	public void setBase1(char pBase1) {base1 = pBase1;}
	
	private int position2 = -1;
	/** the position this diff falls on sequence 2 */
	public int getPosition2() {return position2;}
	/** the position this diff falls on sequence 2 */
	public void setPosition2(int pPosition2) {position2 = pPosition2;}
	
	private char base2 = 'n';
	/** the base in sequence 2 at this position */
	public char getBase2() { return base2;}
	/** the base in sequence 2 at this position */
	public void setBase2(char pBase2) {base2 = pBase2;}
	
	/**
	 * @param pPos1 position in sequence 1
	 * @param pBase1 base in sequence 1 at position 1
	 * @param pPos2 position in sequence 2
	 * @param pBase2 base in sequence 2 at position 2
	 */
	public Difference(int pPos1, char pBase1, int pPos2, char pBase2) {
		position1 = pPos1;
		base1 = pBase1;
		position2 = pPos2;
		base2 = pBase2;
	}
	
	public String toString() {
		return "Diff: " + position1 + ":" + base1 + "/" + position2 + ":"  + base2;
	}
	
	public Object clone() {
		return new Difference(position1, base1, position2, base2);
	}
	
}
