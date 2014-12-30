package amd.strainer;

public class DuplicateReadNameException extends Exception {

	public DuplicateReadNameException(String readName) {
		super("Found more than one read named: " + readName);
	}

}
