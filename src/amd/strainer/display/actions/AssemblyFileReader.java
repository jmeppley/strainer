package amd.strainer.display.actions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import amd.strainer.objects.ReferenceSequence;

public interface AssemblyFileReader {

	String getAssemblyFileName();

	ReferenceSequence getContigDetailsFromNumber(long contigNumber,
			Task getContigFromAceByNumberTask) throws IOException,
			InterruptedException;

	Iterator<ReferenceSequence> getContigIterator(Set<Long> contigNumbers)
			throws FileNotFoundException, IOException;

}
