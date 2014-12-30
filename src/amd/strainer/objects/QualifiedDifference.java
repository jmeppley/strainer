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
 * Adds a quality score to a difference. The static qualityThreshold affects all calls to getBase2(). If the qual of
 * a difference is less than the threshold, n will be returned.
 * 
 * @author jmeppley
 *
 */
public class QualifiedDifference extends Difference {
	
	private static short qualityThreshold = 20;
	/**
	 * threshold at which differences will be ignored
	 */
	public static short getQualityThreshold() {return qualityThreshold;}
	/**
	 * @param pQT short value to which to set the threshold at which differences will be ignored
	 */
	public static void setQualityThreshold(short pQT) {qualityThreshold = pQT;}
	
	// the qulaity value for this base
	private short mQual;
	
	/**
	 * @param pDiff the difference to copy
	 * @param pQual the quality score
	 */
	public QualifiedDifference(Difference pDiff, short pQual) {
		super(pDiff.getPosition1(), pDiff.getBase1(), pDiff.getPosition2(), pDiff.getBase2());
		mQual = pQual;
	}
	
	/**
	 * @param pPos1 position in sequence 1
	 * @param pBase1 base in sequence 1 at position 1
	 * @param pPos2 position in sequence 2
	 * @param pBase2 base in sequence 2 at position 2
	 * @param pQual the quality of base 2
	 */
	public QualifiedDifference(int pPos1, char pBase1, int pPos2, char pBase2, short pQual) {
		super(pPos1, pBase1, pPos2, pBase2);
		mQual = pQual;
	}
	
	// override getBase2 method to check threshold first
	/**
	 * returns the base for the aligned sequence if the quality score is over the threshold, otherwise, 'n'
	 * @see amd.strainer.objects.Difference#getBase2()
	 */
	public char getBase2() {
		if (mQual>=qualityThreshold) {
			return super.getBase2();
		} else {
			return 'n';
		}
	}
	
	/**
	 * @return the actual base for sequence 2 at this position (don't check quality)
	 */
	public char getBase2Actual() {
		return super.getBase2();
	}
	
	/**
	 * @return the quality of this base
	 */
	public short getQuality() {
		return mQual;
	}
	
	public Object clone() {
		return new QualifiedDifference(getPosition1(), getBase1(), getPosition2(), getBase2Actual(), getQuality());
	}
}
