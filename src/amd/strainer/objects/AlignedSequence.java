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

import amd.strainer.display.DisplayData;

/**
 * Defines how to use Sequencces that have alignment information. 
 * 
 * @author jmeppley
 *
 */
public interface AlignedSequence extends Sequence, SequenceFragment {
    ////////////
    // Accessors

    /** 
     * the Alignment indicationg where and how this sequence aligns to 
     *  the reference sequence 
     */
    public Alignment getAlignment();
    /** 
     * the Alignment indicationg where and how this sequence aligns to 
     *  the reference sequence 
     */
    public void setAlignment(Alignment pAlignment);

//    public int getStart();
//    public int getEnd();

    // accessor methods needed for display
//    public DisplayGeometry getDisplayGeometry();
//    public void setDisplayGeometry(DisplayGeometry pDG);

    /**
     * Make this object the currenlty selected Sequence
     * @param pData the DisplayData object holding all the current display infor for the program
     */
    public void select(DisplayData pData);
    /**
     * Make this object no longer be the currenlty selected Sequence
     * @param pData the DisplayData object holding all the current display infor for the program
     */
    public void deselect(DisplayData pData);
    /**
     * Adds this object to the list of previously selected sequences
     * @param pData the DisplayData object holding all the current display infor for the program
     */
    public void addToSelectedList(DisplayData pData);
    /**
     * Removes this object to the list of previously selected sequences
     * @param pData the DisplayData object holding all the current display infor for the program
     */
    public void removeFromSelectedList(DisplayData pData);

    /**
     * Force the object to re-assess what color it should be when drawn. Called when the coloring settings are changed.
     */
    public void recalcColors();

    
    /**
     * @param pQuery any object implementing the SequenceSegment interface
     * @return true if the alignment of this Sequence overlaps the aligment of pQuery
     */
    public boolean intersects(SequenceSegment pQuery);
    /**
     * @return a String describing this AlignedSequence. Used to display info in the main text box when a sequence is selected by the User
     */
    public String detailsString();
    /**
     * Make sure the classes needed to display this object have been initialized
     */
    public void initializeGraphics();
}
