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

import amd.strainer.display.DisplayGeometry;
import amd.strainer.display.GeneDisplayGeometry;

/**
 * A represenation of an annotation on a ReferenceSequence. Like an AlignedSequence,
 * a Gene needs to have alignment information, but does not get stacked, so it
 * implements the SequenceFragment interface.
 * 
 * @author jmeppley
 *
 */
public class Gene extends SequenceSegment implements SequenceFragment {
	
	private String name = null;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	private int id = -1;
	/** the number of this gene */
	public int getId() {return id;}
	private void setId(int pId) {
		id = pId;
		idInteger = new Integer(id);
	}
	private Integer idInteger = new Integer(-1);
	public Integer getIdInteger() { return idInteger; }
	
	public Alignment getAlignment() { return null; }
	
	private boolean direction = true; 
	
	/**
	 * @return Returns the direction.
	 */
	public boolean getDirection() {
		return direction;
	}
	/**
	 * @param direction The direction to set.
	 */
	public void setDirection(boolean direction) {
		this.direction = direction;
	}
	private String description = "";
	/**
	 * @return A description of this annotation
	 */
	public String getDescription() { return description;}
	/**
	 * Set the descriptio of this Gene (or other annotation)
	 * @param pDescription
	 */
	public void setDescription(String pDescription) {description = pDescription;}
	
	// accessor methods needed for display
	private DisplayGeometry displayGeometry = new GeneDisplayGeometry(this);
	public DisplayGeometry getDisplayGeometry() {return displayGeometry;}
	public void setDisplayGeometry(DisplayGeometry pDG){displayGeometry = pDG;}
	
	/**
	 * Create a new Gene annotation
	 * @param pId a unique ID for this object
	 * @param pRefSeq the ReferenceSequence it is an annotation on
	 * @param pStart the first position coded
	 * @param pEnd the last position coded
	 * @param pDir the direction or strand (true -> same as sequence given for the ReferenceSequence)
	 * @param pDescription a description of the annotation
	 */
	public Gene(int pId, ReferenceSequence pRefSeq, int pStart, int pEnd, boolean pDir, String pDescription) {
		this(Integer.toString(pId),pRefSeq,pStart,pEnd,pDir,pDescription);
		setId(id);
	}

	/**
	 * Create a new Gene annotation
	 * @param pName te name of the Gene
	 * @param pRefSeq the ReferenceSequence it is an annotation on
	 * @param pStart the first position coded
	 * @param pEnd the last position coded
	 * @param pDir the direction or strand (true -> same as sequence given for the ReferenceSequence)
	 * @param pDescription a description of the annotation
	 */
	public Gene(String pName, ReferenceSequence pRefSeq, int pStart, int pEnd, boolean pDir, String pDescription) {
		super(pRefSeq, pStart, pEnd);
		setName(pName);
		direction = pDir;
		description = pDescription;
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		
		s.append(getName()).append(":").append(getStart()).append("-").append(getEnd());
		s.append("\n").append(getDescription());
		
		return s.toString();
	}

}
