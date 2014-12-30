package amd.strainer.display.actions;

import jaligner.Sequence;
import jaligner.SmithWatermanGotoh;
import jaligner.matrix.MatrixLoader;
import jaligner.matrix.MatrixLoaderException;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;

import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.objects.Read;
import amd.strainer.objects.ReferenceSequence;
import amd.strainer.objects.Strain;



/**
 * @author jmeppley
 * 
 * Task (with internal Action) to allow user to rebuild the alignments in the current data set
 * 
 * TODO:2 Unfinished. Need to apply new alignment info to Alginment object 
 */

public class RealignReadsTask extends AbstractTask {
	public static class RealignAction extends AbstractAction {
		PaneledReferenceSequenceDisplay mParent = null;    
		RealignAction(PaneledReferenceSequenceDisplay pParent) {
			super("Re-align reads");
			putValue(SHORT_DESCRIPTION,"Rebuild data by aligning reads to reference.");
			mParent = pParent;
		}

		public void actionPerformed(ActionEvent e) {
			// rebuild data
			Task task = new RealignReadsTask(mParent);

			SequenceDataLoader loader = new SequenceDataLoader(mParent,task);
			loader.load();

		}
	}

	private PaneledReferenceSequenceDisplay mParent;

	public RealignReadsTask(PaneledReferenceSequenceDisplay parent) {
		mParent = parent;
	}

	@Override
	protected Object doStuff() {
		ReferenceSequence refSeq = mParent.getReferenceSequence();
		try {
			refSeq = realignRefSeq(refSeq);
		} catch (MatrixLoaderException e) {
			e.printStackTrace();
			errorTitle = "Could not find alignment matrix";
			message = e.toString();
			current = -1;
		} catch (RuntimeException e) {
			e.printStackTrace();
			errorTitle = "Unexpected Error";
			message = e.toString();
			current = -1;
		}
		return refSeq;
	}

	public void doOnError(PaneledReferenceSequenceDisplay parent) {
		// do nothing
	}

	private ReferenceSequence realignRefSeq(ReferenceSequence pReferenceSequence) throws MatrixLoaderException {
		Sequence alignableRefSeq = new Sequence(pReferenceSequence.getBases());

		// loop over strains
		Iterator<Strain>  strainIt = pReferenceSequence.strains.values().iterator();
		while (strainIt.hasNext()) {
			Strain strain = strainIt.next();
			
			// loop over reads
			Iterator<Read> readIt = strain.getReadIterator();
			while (readIt.hasNext()) {
				
				// get new read alignment
				Read read = readIt.next();
				Sequence alignableReadSeq = new Sequence(read.getBases());
		        jaligner.Alignment alignment = SmithWatermanGotoh.align(alignableRefSeq, 
		        		alignableReadSeq, MatrixLoader.load("BLOSUM62"), 10f, 0.5f);
		        
		        // update read alignment
		        // FIXME: apply info to Alignment object for read
		        //  (see mAlign flag in AceFileReader)

			}
		}

		// return modified referenceSequence
		return pReferenceSequence;
	}

}