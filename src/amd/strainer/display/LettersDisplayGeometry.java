package amd.strainer.display;

import java.awt.Graphics2D;
import java.util.HashSet;

import amd.strainer.display.util.DiffLetterInfo;
import amd.strainer.objects.SequenceFragment;

public class LettersDisplayGeometry extends DisplayGeometry {
	private HashSet<DiffLetterInfo> mLetters = new HashSet<DiffLetterInfo>();

	public HashSet<DiffLetterInfo> getLetters() {
		return mLetters;
	}

	public void setLetters(HashSet<DiffLetterInfo> pLetters) {
		mLetters = pLetters;
	}

	/**
	 * Creates the LettersDisplayGeometry object for the given fragment
	 * 
	 * @param pParent
	 *            Fragment (usually this will be a Gene the length of the entire
	 *            reference)
	 */
	public LettersDisplayGeometry(SequenceFragment pParent) {
		mParent = pParent;
	}

	@Override
	public void draw(Graphics2D pG2d, DisplayData pData) {
		if (pData.drawDiffLetters) {
			// draw letters over ticks
			pG2d
					.setColor(DisplaySettings
							.getDisplaySettings()
							.getLetterColor());
			pG2d.setFont(pData.getLetterFont());

			for (DiffLetterInfo letter : getLetters()) {
				pG2d.drawString(
						String.valueOf(letter.getLetter()),
						(float) letter.getX(),
						(float) letter.getY());
			}
		}
	}

	@Override
	public boolean update(DisplayData pData) {
		if (pData.drawDiffLetters) {
			HashSet<DiffLetterInfo> letters = getLetters();
			letters.clear();

			double width = pData.tickWidth;
			double x;
			int y = pData.refSeqAreaHeight + pData.rowHeight;

			for (int pos = pData.getStart(); pos <= pData.getEnd(); pos++) {
				x = getX(pos, pData);
				double letterX = width > pData.diffLetterCutoff ? x
						+ (width - pData.diffLetterCutoff) / 2 : x;
				char letter = mParent.getSequence().getBase(pos);
				letters.add(new DiffLetterInfo(
						Character.toUpperCase(letter),
						letterX,
						y));
			}

			setLetters(letters);

			return true;
		} else {
			return false;
		}

	}

}
