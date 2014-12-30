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

import java.awt.Graphics2D;

import amd.strainer.display.util.Util;
import amd.strainer.objects.Gene;

/**
 * Contains display data and methods for a Gene object.
 * 
 * @author jmeppley
 * @see amd.strainer.objects.Gene
 */
public class GeneDisplayGeometry extends DisplayGeometry {

	@Override
	public void draw(Graphics2D pG2d, DisplayData pData) {
		if (visible) {
			pG2d.setPaint(fill);
			pG2d.fill(shape);
		}
	}

	/**
	 * Creates the GeneDisplayGeometry object for the given gene
	 * 
	 * @param pParent
	 *            Gene object to be displayed
	 */
	public GeneDisplayGeometry(Gene pParent) {
		mParent = pParent;
	}

	@Override
	public boolean update(DisplayData pData) {
		double x1 = getX(mParent.getStart(), pData);
		double x2 = getX(mParent.getEnd() + 1, pData);

		double y1 = pData.refSeqAreaHeight;
		double h = pData.geneHeight;

		visible = Util.rectInDisplay(x1, x2, y1, h, pData);

		if (!visible)
			return false;

		double w = x2 - x1;

		double[] x, y;

		if (((Gene) mParent).getDirection()) {
			if (w <= h) {
				x = new double[3];
				y = new double[3];
				x[0] = x1;
				y[0] = y1;
				x[1] = x[0];
				y[1] = y[0] + h;
				x[2] = x2;
				y[2] = (y[0] + y[1]) / 2;
			} else {
				x = new double[5];
				y = new double[5];
				x[0] = x1;
				y[0] = y1;
				x[1] = x[0];
				y[1] = y[0] + h;
				x[2] = x2 - h / 2;
				y[2] = y[1];
				x[3] = x2;
				y[3] = (y[0] + y[1]) / 2;
				x[4] = x[2];
				y[4] = y[0];
			}
		} else {
			if (w <= h) {
				x = new double[3];
				y = new double[3];
				x[0] = x2;
				y[0] = y1;
				x[1] = x[0];
				y[1] = y[0] + h;
				x[2] = x1;
				y[2] = (y[0] + y[1]) / 2;
			} else {
				x = new double[5];
				y = new double[5];
				x[0] = x2;
				y[0] = y1;
				x[1] = x[0];
				y[1] = y[0] + h;
				x[2] = x1 + h / 2;
				y[2] = y[1];
				x[3] = x1;
				y[3] = (y[0] + y[1]) / 2;
				x[4] = x[2];
				y[4] = y[0];
			}
		}
		shape = getGeneralPath(x, y);

		return true;
	}

}
