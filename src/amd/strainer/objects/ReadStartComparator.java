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

import java.util.*;

public class ReadStartComparator implements Comparator<Readable> {
	// TODO:3 merge with StrainStartComparator using SequenceSegment
	
	// this is not thread safe.  Another thread can change the dir
	//  of your comparator
	
	private static ReadStartComparator instance = null;
	private ReadStartComparator() {}
	
	public static ReadStartComparator getReadStartComparator(int pDir) {
		if (instance==null) {
			instance = new ReadStartComparator();
		}
		instance.dir=pDir;
		return instance;
	}
	
	public static ReadStartComparator getReadStartComparator() {
		if (instance==null) {
			instance = new ReadStartComparator();
		}
		instance.dir=ASC;
		return instance;
	}
	
	public static final int ASC = -1;
	public static final int DESC = 1;
	private int dir = DESC;
	
	public boolean equals(Object pObject) {
		return pObject==instance;
	}
	
	public int compare(Readable r1, Readable r2) throws ClassCastException {
		return dir * (r2.getStart() - r1.getStart());
	}
}
