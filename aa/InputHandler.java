package aa;
import java.util.HashSet;

import javax.swing.tree.DefaultTreeModel;

abstract class InputHandler {

	abstract public DefaultTreeModel readInput(String filename);
	public HashSet allFeatures; // The set of all features represented as Strings

}
