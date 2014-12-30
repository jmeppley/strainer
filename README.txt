Strainer is a visualization and analysis tool for metagenomics data

To obtain or learn more about strainer visit: 
  http://bioinformatics.org/strainer
 
Copyright (c) 2007 The Regents of the University of California.  All rights
reserved.

This program is free software; you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published 
by the Free Software Foundation; either version 2.1 of the License, or 
(at your option) any later version. You may not use this file except in 
compliance with the License. 

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  
USA or visit http://www.gnu.org/licenses/lgpl.html

IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT,
INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING
LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
DOCUMENTATION, EVEN IF REGENTS HAS BEEN ADVISED OF THE POSSIBILITY
OF SUCH DAMAGE.
 
REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE. THE SOFTWARE AND ACCOMPANYING
DOCUMENTATION, IF ANY, PROVIDED HEREUNDER IS PROVIDED "AS
IS". REGENTS HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
UPDATES, ENHANCEMENTS, OR MODIFICATIONS. 

Contributor(s):
 John Eppley <jmeppley@berkeley.edu>

License information for bundled software is at the end of this document.

To run strainer:

 1. unpack the distribution archive:
   > jar -xf Strainer-0.1b.jar
  
 2. execute the jar file
   > java -jar Strainer-0.1b/lib/strainer.jar
   (in most modern graphical operating systems, 
    you may be able to double-click on the jar file)

Memory issues:

 Large data sets may require more memory than Java will supply by default. 
 To adjust the maximum heap size (i.e. allocate more memory) run strainer
 from the command line with the -Xmx option. The following instructs java
 to let strainer use upt to 1GB of RAM:
   > java -jar Strainer-0.1b/lib/strainer.jar -Xmx1024m

Bundled Software and Libraries:

BIOJAVA
BioJava is distributed under the Lesser Gnu Public License. The  
unmodified BioJava code is provided in binary form as a convenience. 
Modified BioJava class files are included in the Strainer source 
distribution and re-distributed under the LGPL.

JAVA LOOK AND FEEL GRAPHICS REPOSITORY
SWING WORKER
The Java-Look-and-Feel graphics and the SwingWorker class repository is distributed 
by Sun Microsystems under the following terms:

Copyright 1994-2007 Sun Microsystems, Inc. All Rights Reserved.
Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistribution of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistribution in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.

Neither the name of Sun Microsystems, Inc. or the names of contributors may be 
used to endorse or promote products derived from this software without specific
prior written permission.

This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS 
OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED 
WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, 
ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT 
BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING 
OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS 
LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, 
SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND 
REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO 
USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

You acknowledge that this software is not designed, licensed or intended for use in
 the design, construction, operation or maintenance of any nuclear facility.  
