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

package amd.strainer;

import java.util.StringTokenizer;
import java.util.prefs.Preferences;

/*
 * Holds values that need to be access from multiple places in the program. 
 * Some values (the ones for genbank parsing) are just used temporarily. The
 * one kept in the parameters map will persist between sessions. The genbank 
 * params are legacy and should be migrated somewhere else or folded into the map.
 *  
 * @author jeppley
 */
public class GlobalSettings {
	public static final String INPUT_DIR_KEY = "INPUT_DIR";
	public static final String OUTPUT_DIR_KEY = "OUTPUT_DIR";
	/*
	 * List of genbank feature codes to keep (CDS, gene, tRNA, RNA...) If blank
	 * at time of genbank parsing, a dialog will appear
	 */
	private static String[] annotationList = null;

	public static String[] getAnnotationList() {
		return annotationList;
	}

	public static void setAnnotationList(String[] pAnnotationList) {
		annotationList = pAnnotationList;
	}

	/*
	 * If this is set going into genbank parsing, genes are renamed using this
	 * prefix and a numerical index
	 */
	private static String genePrefix = null;

	public static String getGenePrefix() {
		return genePrefix;
	}

	public static void setGenePrefix(String pGenePrefix) {
		genePrefix = pGenePrefix;
	}

	private static Preferences prefs;

	private static void checkPrefs() {
		if (prefs == null) {
			prefs = Preferences.userNodeForPackage(GlobalSettings.class);
		}
	}

	public static String getSetting(String pKey) {
		checkPrefs();
		return prefs.get(pKey, null);
	}

	public static String getSetting(String pKey, String pDefault) {
		checkPrefs();
		return prefs.get(pKey, pDefault);
	}

	public static void putSetting(String pKey, String pValue) {
		checkPrefs();
		prefs.put(pKey, pValue);
	}

	/**
	 * Will turn a comma separated list into a String array. Used for parsing
	 * command line options.
	 * 
	 * @param pCSL
	 *            comma separated list of string values
	 * @return String array
	 */
	public static String[] parseCommaList(String pCSL) {
		StringTokenizer st = new StringTokenizer(pCSL, ",");
		int size = st.countTokens();
		String[] annotations = new String[size];
		for (int i = 0; i < size; i++) {
			annotations[i] = st.nextToken();
		}
		return annotations;
	}
}
