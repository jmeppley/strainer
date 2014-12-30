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

package amd.strainer.display;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JFrame;

import amd.strainer.GlobalSettings;
import amd.strainer.display.actions.GetReferenceFromFileTask;
import amd.strainer.display.actions.SequenceDataLoader;
import amd.strainer.display.actions.Task;

/**
 * 
 * Contains main() method to launch the GUI
 * @author jmeppley
 *
 */
public class Main {
	static PaneledReferenceSequenceDisplay p;
	static WindowListener wl = new WindowListener() {

		public void windowOpened(WindowEvent arg0) {
		}

		public void windowClosing(WindowEvent arg0) {
			p.exitWithSaveCheck();
			
			// if we get here, user cancelled exit
			// re-open window
			
			openGUI();
		}

		public void windowClosed(WindowEvent arg0) {
		}

		public void windowIconified(WindowEvent arg0) {
		}

		public void windowDeiconified(WindowEvent arg0) {
		}

		public void windowActivated(WindowEvent arg0) {
		}

		public void windowDeactivated(WindowEvent arg0) {
		}
	};
	
	private static void openGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("Strainer Display");

		frame.addWindowListener(wl);
		p.buildInFrame(frame);

		//Display the window.
		frame.pack();
		
		int w = 800;
		int h = 500;
		frame.setSize(w,h);

		frame.setVisible(true);
	}
	
	private static void createAndShowGUI() {
		//System.out.println("creating GUI");
		
		//Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		
		//		frame.setDefaultCloseOperation(JFram);
		p = new PaneledReferenceSequenceDisplay();
		
		// create window and open
		openGUI();

		// auto load some files
		if (iargs.length>=3 && iargs.length<=5) {
			String refSeqFileName = iargs[0];
			System.out.print("Reference Sequence File: " + refSeqFileName);
			String refSeqFileType = iargs[1];
			System.out.println(" and File Type: " + refSeqFileType);
			String strainsFileName=iargs[2];
			System.out.println("Strains File: " + strainsFileName);
			
			if (iargs.length>=4) {
				GlobalSettings.setAnnotationList(GlobalSettings.parseCommaList(iargs[3]));
				
				if (iargs.length==5) {
					GlobalSettings.setGenePrefix(iargs[4]);
					System.out.println("naming genes: " + GlobalSettings.getGenePrefix());
				}
			}
			Task task = new GetReferenceFromFileTask(p,new File(refSeqFileName),new File(strainsFileName),refSeqFileType);
			SequenceDataLoader loader = new SequenceDataLoader(p,task);
//			p.progressBar.setIndeterminate(true);
//			p.disableAllActions();
//			p.progressBar.setStringPainted(true);
			loader.load();
//			p.enableAllActions();
//			p.progressBar.setStringPainted(false);
		}

	}
	
	static String [] iargs = null;
	
	/**
	 * Launches program GUI
	 * @param args optional command line arguments, if used, should be three words: reference sequence file name, rerefence sequence file type, and strains file name
	 */
	public static void main(String[] args) {
		iargs = args;
		
		// set autostrain defaults
//		HashMap settings = amd.strainer.algs.Config.getConfig().getSettings();
//		settings.put(GeneCrawler.MINIMUM_OVERLAP,new Integer(1));
//		settings.put(GeneCrawler.COMPLETION_DIFF,new Double(0.0));
//		settings.put(SegmentLinker.SEGMENT_METHOD,SegmentLinker.SEGMENT_BY_READ_COUNT);
//		settings.put(SegmentLinker.SEGMENT_SIZE,new Integer(60));
		
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities
		.invokeLater(
				new Runnable() {
					public void run() {
						createAndShowGUI();
						//reallySimpleGUI();
					}
				}
		);
	}
}
