package lsedit;

/**
 * Developed by: Ian Davis in Summer 1997 for Grant Weddell
 * This class allows Hi Graph Exceptions to be generated
 */

public class HiGraphException extends Exception {
	
	/**
	 * Constructor.
	 */

	HiGraphException() {
		super();
	}

	HiGraphException(String s) {
		super(s);
	}
}
