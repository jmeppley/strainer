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

import java.awt.Color;

import amd.strainer.display.util.Util;
import amd.strainer.objects.AlignedSequence;

public abstract class AlignedSequenceDisplayGeometry extends DisplayGeometry {
	protected abstract Color getTintLowColor();
	protected abstract Color getTintHighColor();
	protected abstract int getTintLowCutoff();
    
	/**
	 * Calculates the fill color for this sequence.  Currently,
	 * this means picking a color based on sequence similarity.  Future
	 * version will probaly let the use choose the color scheme.  The 
	 * user's choices will get save in the DisplayData var and all objects 
	 * colors will be reset.  This method could also do different things based
	 * on the actual class type (Read, Strain, etc.
	 * 
	 */
	protected Color getPctIDColor() {
		double maxValue = 1.0;     		// 100% the same
		double minValue = getTintLowCutoff()/100.0;			
		
		double index;
		if (mParent instanceof AlignedSequence) {
			index = Math.max(minValue,((AlignedSequence)mParent).getAlignment().getIdentity());
		} else {
			index = maxValue;
		}
		
		color = Util.pickTintedColor(minValue,maxValue,index,getTintLowColor(),getTintHighColor());
		return color;
	}

}
