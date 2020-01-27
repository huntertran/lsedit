package lsedit;

import java.util.Vector;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

class MyCompoundEdit extends CompoundEdit implements UndoableEdit
{
	String	m_presentationName;

	MyCompoundEdit(String presentationName)
	{
		m_presentationName = presentationName;
	}

	public String getPresentationName() 
	{
		return m_presentationName;
	}

	public Vector getEdits()
	{
		return edits;
	}

	public boolean replaceEdit(UndoableEdit last)
	{
		// We don't create this compound object if we have less than two UndoableEdits in the set
		// So we are creating this when we are the second so add back in the first
		if (edits.size() == 1) {
			edits.add(0, last);
			return(true);
		}
		return(false);
	}

	public boolean canUndo()
	{
		return true;
	}

	public boolean canRedo()
	{
		return true;
}	}	