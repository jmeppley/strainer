package amd.strainer.display.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.naming.OperationNotSupportedException;
import javax.swing.ProgressMonitorInputStream;

import org.biojava.bio.seq.io.AlignmentFormat;
import org.biojava.bio.seq.io.FastaAlignmentFormat;
import org.biojava.bio.seq.io.MSFAlignmentFormat;
import org.biojava.bio.symbol.Alignment;
import org.biojava.bio.symbol.IllegalSymbolException;

import amd.strainer.display.PaneledReferenceSequenceDisplay;
import amd.strainer.display.util.Util;
import amd.strainer.objects.ReferenceSequence;

public class ImportMSATask extends AbstractTask implements Task {

	static final String MSAF_ALIGNMENT = "MSAF";
	static final String FASTA_ALIGNMENT = "FASTA";
	static final Object [] FORMAT_LIST = { FASTA_ALIGNMENT, MSAF_ALIGNMENT };
	private PaneledReferenceSequenceDisplay mParent;
	private File mMSAFile;
	private File mQualityFile;
	private String mFormat;

	public ImportMSATask(PaneledReferenceSequenceDisplay parent, File msaFile, String pFormat,
			File qualityFile) {
		mParent = parent;
		mMSAFile = msaFile;
		mFormat = pFormat;
		mQualityFile = qualityFile;
	}

	@Override
	protected Object doStuff() {
		ReferenceSequence refSeq = null;
		try {
			setMessage("loading " + mMSAFile.getName());
			ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(
					PaneledReferenceSequenceDisplay.frame,
					"Reading " + mMSAFile,
					new FileInputStream(mMSAFile));
			AlignmentFormat af;
			if (mFormat==FASTA_ALIGNMENT) {
				af = new FastaAlignmentFormat();
			} else if (mFormat==MSAF_ALIGNMENT) {
				af = new MSFAlignmentFormat();
			} else {
				pmis.close();
				throw new OperationNotSupportedException("Unknown format: " + mFormat);
			}
			setMessage("converting from BioJava to internal models");
			Alignment a = af.read(new BufferedReader(new InputStreamReader(pmis)));
			refSeq = amd.strainer.file.Util.createRefSeqFromBioJava(a);
			setMessage("preparing to display");
			mParent.setReferenceSequence(refSeq);
			pmis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Util.displayErrorMessage(e);
		} catch (OperationNotSupportedException e) {
			System.err.println(e.getMessage());
			Util.displayErrorMessage(PaneledReferenceSequenceDisplay.frame, e.getMessage());
		} catch (IllegalSymbolException e) {
			e.printStackTrace();
			Util.displayErrorMessage(e);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			Util.displayErrorMessage(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block from pmis.close()
			e.printStackTrace();
			Util.displayErrorMessage(e);
		}

		// notify timer thread
		done = true;
		
		// return anything
		return refSeq;	
	}

	public void doOnError(PaneledReferenceSequenceDisplay parent) {
		// TODO Auto-generated method stub

	}

}
