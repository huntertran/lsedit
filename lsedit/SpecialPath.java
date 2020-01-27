package lsedit;

/* This interface decouples LSEdit from the CMDB software so that
 * LSEdit can be compiled without JDBC
 * That allows it to be used in other products more easily
 */

public interface SpecialPath {


	// Hook for CMDB parsing
	
	abstract boolean isSpecialPath(String path);
	abstract String	 parseSpecialPath(Ta ta, ResultBox resultBox, String path);
	abstract void	 specialPathOptions(Option option);
}

