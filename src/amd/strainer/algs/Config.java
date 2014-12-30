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

package amd.strainer.algs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Singelton class that holds configuration options for auto-straining in a HashMap. Settings are defined by individual algorithms.
 * 
 * @author John Eppley
 */
public class Config {
	
	///////////
	// variables for commonly used settings
	public static final String FILL_FROM_COMPOSITE = "Fill unknown bases from reference sequence";
	public static final String CONVERT_TO_AA = "Output amino acid sequences";
	public static final String INTERNAL_SEGMENT_STRAINER = "Underlying algorithm";
	public static final String SEGMENT_STRAINER = "Segment straining algorithm";
	public static final String KEEP_ALL_READS = "Store all reads for each strain";
	public static final String RESTRICT_TO_SEGMENT = "Only compare reads inside segment";
	
	private HashMap<String,Object> settings = null;
	/**
	 * @return The HashMap containting setting name:value pairs.
	 */
	public HashMap<String,Object> getSettings() { 
		if (settings==null) {
			settings = new HashMap<String,Object>();
			
			try {
				// try to get settings from prefs
				if (prefs==null) {
					prefs = Preferences.userNodeForPackage(getConfig().getClass());
				}

				// settings from prefs are Strings and need to be converted to the proper type
				// get defaults from strainer algs and update types
				List<Class> algs = getAlgorithmList();
				for (Class alg : algs) {
					try {
						settings.putAll(getDefaultValuesForAlgClass(alg));
					} catch (Exception e) {
						System.err.println("Cannot configure algoritm, removing from list: " + alg.toString());
						e.printStackTrace();
					}
				}

				// get String values from prefs and convert type base on default values
				String [] keys = prefs.keys();
				for (int i = 0; i< keys.length;i++) {
					try {
						setOptionFromString(keys[i],prefs.get(keys[i],null));
					} catch (ClassNotFoundException e) {
						System.err.println("Saved setting lost: could not convert to class: " + prefs.get(keys[i],null));
						e.printStackTrace();
					}
				}

			} catch (BackingStoreException e) {
				System.err.println("Cannot read system prefs, settings won't be preserved between runs.");
				e.printStackTrace();
			}
		}
		return settings; 
	}
	/**
	 * @param pSettings HashMap to replace the existing settings HashMap with
	 */
	public void saveSettings() {
		if (prefs==null) {
			prefs = Preferences.userNodeForPackage(getConfig().getClass());
		}

		for (Map.Entry<String,Object> e : settings.entrySet()) {
			prefs.put(e.getKey().toString(),e.getValue().toString());
		}
	}
	
	// make singleton
	private static Config instance = null;
	
	private Config() {}
	
	public static Config getConfig() {
		if (instance==null) {
			instance = new Config();
		}
		return instance;
	}
	
	/**
	 * Saves the given setting value pair to the Settings hash after converting the value string
	 * to the appropriate type (Boolean, nteger, ...) based on the type of the default value
	 * 
	 * <p>
	 * 
	 * @param pSetting the name of the option
	 * @param pValue the string representation of the new value
	 * @throws ClassNotFoundException 
	 */
	public static void  setOptionFromString(String pSetting, String pValue) throws ClassNotFoundException {
		HashMap<String,Object> settings = getConfig().getSettings();
		Object defaultValue = settings.get(pSetting);
		Object value = pValue;
		if (defaultValue instanceof Boolean) {
			value = new Boolean(pValue);
		} else if (defaultValue instanceof Integer) {
			value = new Integer(pValue);
		} else if (defaultValue instanceof Double) {
			value = new Double(pValue);
		} else if (defaultValue instanceof Class) {
			if (pValue.startsWith("class ")) {
				int spint = pValue.indexOf(" ");
				value = Class.forName(pValue.substring(spint+1));
			} else {
				value = Class.forName(pValue);
			}
		}
		settings.put(pSetting,value);
	}
	
	/////////////
	//  methods to figure out which algorihms are installed
	/////////////
	static Preferences prefs = null;
	static List<Class> algClasses = null;

	private static void setAlgorithmList(List<Class> pAlgs) {
		// set main list to passed list and save to prefs
		StringBuffer pref = new StringBuffer();
		Iterator<Class> it = pAlgs.iterator();
		while( it.hasNext()) {
			Class c = it.next();
			pref.append(c.getName());
			if (it.hasNext()) {
				pref.append(',');
			}
		}
		if (prefs==null) {
			prefs = Preferences.userNodeForPackage(getConfig().getClass());
		}
		prefs.put(ALG_LIST_PREF,pref.toString());
		algClasses = pAlgs;
	}
	
	private final static String ALG_LIST_PREF = "AlgorithmList";
	
	/**
	 * Get list of installed algorithms from amd.strainer.algs.SegmentStrainers.properties.
	 * @return List object with Class elements indicating implementations of SegmentStrainer interface
	 */
	public static List<Class> getAlgorithmList() {
		if (algClasses==null) {
			// build list if it's not already in memory
			if (prefs==null) {
				prefs = Preferences.userNodeForPackage(getConfig().getClass());
			}
			
			algClasses = new ArrayList<Class>();
			
			// get algorithm list from preferences
			String algList = prefs.get(ALG_LIST_PREF,"amd.strainer.algs.ManualStrainer," +
					"amd.strainer.algs.SimpleGeneCrawler," +
					"amd.strainer.algs.Substrainer," +
					"amd.strainer.algs.SegmentLinker");

			try {
				prefs.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// split string into List
			StringTokenizer st = new StringTokenizer(algList,",");
			while (st.hasMoreTokens()) {
				String className = st.nextToken();
				try {
					Class algClass = Class.forName(className);
					algClasses.add(algClass);
				} catch (Exception e) {
					System.err.println("Cannot find algorithm class: " + className);
					e.printStackTrace();
				}
			}
		}		
		return algClasses;
	}
	
	/**
	 * @param pAlg Class to be removed from the list of straining algorithms
	 * @throws NoSuchElementException if pAlg is not found in the list
	 */
	public static void removeAlgorithm(Class pAlg) {
		List<Class> algs = getAlgorithmList();
		if (!algs.remove(pAlg)) {
			throw new NoSuchElementException("Can't find " + pAlg.toString() + " in algorithm list");
		} else {
			// save new list to gobal var and system prefs
			setAlgorithmList(algs);
		}
	}
	
	/**
	 * Add the specified class as a Straining algorithm. It must be in the classpath and implement amd.strainer.algs.SegmentStrainer
	 * @param pAlgName name of algorithm to add. A class name (e.g. some.package.Class)
	 * @throws ClassNotFoundException
	 */
	public static void addAlgorithm(String pAlgName) throws ClassNotFoundException {
		// make sure list is already set up
		List<Class> algs = getAlgorithmList();

		// add new alg (will throw Exception if it's not found)
		algs.add(getAlgClassFromName(pAlgName));

		// save new list to gobal var and system prefs
		setAlgorithmList(algs);
	}
	
	public static Class getAlgClassFromName(String pAlgName) throws ClassNotFoundException {
		// get as a Class object
		Class algClass = Class.forName(pAlgName.trim());
		Object ssInterface = SegmentStrainer.class;

		if (implementsInterface(algClass,ssInterface)) {
			return algClass;
		} 
		
		throw new ClassCastException(pAlgName + " does not implement the SegmentStrainer interface.");
		
	}
	
	private static boolean implementsInterface(Class algClass, Object ssInterface) {
		// TODO:2 check super classes so we can use an abstractsegmenttrainer without explicitly implementing the interface
		// does it implement the necessary interface?
		Object [] interfaces = algClass.getInterfaces();
		for (int i = 0; i<interfaces.length; i++) {
			if (ssInterface.equals(interfaces[0])) {
				// This implements the necessary interface
				return true;
			}
		}
		return false;
	}
	/**
	 * Create an instance of the SegmentStrainer indicated by the passed Class object
	 * @param pAlgClass a Class object (must implement the SegmentStrainer interface)
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public static SegmentStrainer getSegmentStrainer(Class pAlgClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return (SegmentStrainer) pAlgClass
			.getConstructor(new Class [0])
			.newInstance(new Object [0]);
	}
	
	/**
	 * Get the name of the algorithm from the given algorithm Class.
	 * @param pSegmentStrainer The Class of the SegmentStrainer algorithm
	 * @return the name of this algorithm as indicated by its getName() method
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static String getAlgorithmName(Class pSegmentStrainer) throws InstantiationException,NoSuchMethodException,InvocationTargetException,IllegalAccessException {
		// have to create an object to call getName()
		// could make it static, but then I can't put it in the interface...
		Method getName = pSegmentStrainer.getMethod("getName",new Class[0]);
		Constructor getAlg = pSegmentStrainer.getConstructor(new Class [0]);
		Object alg = getAlg.newInstance(new Object [0]);
		return getName.invoke(alg, (Object[]) null).toString();
	}
	
	/**
	 * Adds the default values from the indicated SegmentStrainer to the settings hash
	 * 
	 * @param pSegmentStrainer Class implementing SegmentStrainer interface
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 */
	public static void setDefaultValuesForAlgClass (Class pSegmentStrainer) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
		Map<String,Object> defaultValues = getDefaultValuesForAlgClass(pSegmentStrainer);
		HashMap<String,Object> settings = getConfig().getSettings();
		settings.putAll(defaultValues);
	}
	
	/**
	 * @param pSegmentStrainer Class implementing SegmentStrainer interface
	 * @return the Hash of settings used by the given SegmentStrainer mapped to default values
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 */
	public static Map<String,Object> getDefaultValuesForAlgClass (Class pSegmentStrainer) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		Method getDefaults = pSegmentStrainer.getMethod("getOptionsHash",new Class[0]);
		Constructor getAlg = pSegmentStrainer.getConstructor(new Class [0]);
		Object alg = getAlg.newInstance(new Object [0]);
		Object ret = getDefaults.invoke(alg,(Object[])null);
		if (ret instanceof Map) {
			// no easy way to check contents, we just have to trust the SegmentStrainer interface 
			return (Map) ret;
		} else {
			throw new ClassCastException("Class " + pSegmentStrainer.getName() + "appears to violate SegmentStrainer interface by returning a " + ret.getClass().getName() + " from getOptionHash() instead of a Map");
		}
	}
}
