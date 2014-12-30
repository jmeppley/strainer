package amd.strainer.display.util;

import java.awt.Color;

public class DiffLetterInfo {
	private char letter;
	private double x;
	private double y;
	
	public DiffLetterInfo (char pLetter, double pX, double pY) {
		letter = pLetter;
		x= pX;
		y = pY;
	}
	
	public char getLetter() {
		return letter;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
}
