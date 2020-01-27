package lsedit;

import java.awt.FontMetrics;
import java.util.Vector;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/* This class extends Ta with the ability to undo things
 * Unlike Ta.java it does know about LandscapeEditorCore at least at present
 */

class MyUndoManager extends UndoManager {
	
	MyUndoManager() 
	{
	}

	public Vector getEdits()
	{
		return edits;
	}

	public UndoableEdit getEditToBeRedone()
	{
		return((UndoableEdit) editToBeRedone());
	} 
	
	public void massChange(UndoableEdit undoableEdit, boolean redo)
	{
		if (redo) {
			redoTo(undoableEdit);	// UndoManager method
		} else {
			undoTo(undoableEdit);	// UndoManager method
		}
	}
	
	public int	countEdits()
	{
		return(edits.size());
	} 
}

public class UndoableTa extends EditableTa implements UndoableEditListener  
{
	protected MyUndoManager			m_undoManager      = new MyUndoManager();
	protected String				m_compoundEditName = null;
	protected MyCompoundEdit		m_compoundEdit     = null;
	protected int					m_compoundEditCnt  = -1;
	protected boolean				m_useCompoundEdit  = true;
	protected UndoListener			m_undoListener     = null;

	// -----------------
	// Protected methods
	// -----------------

	protected void updateMenu()
	{
		if (m_undoListener != null) {
			m_undoListener.setEnabledRedo(m_undoManager.canRedo());
			m_undoListener.setEnabledUndo(m_undoManager.canUndo());
	}	}

	public UndoableTa(TaFeedback taFeedback)
	{
		super(taFeedback);
		setUndoEnabled(true);	// Remains false if this layer not invoked
	}

	public void setUndoListener(UndoListener undoListener)
	{
		m_undoListener = undoListener;
	}

	// If the user is invoking this then must also clearUndoCache()

	public void setUndoEnabled(boolean value)
	{
		m_undoEnabled = value;
	}

	public boolean useCompoundEdit()
	{
		return m_useCompoundEdit;
	}

	public void setUseCompoundEdit(boolean value)
	{
		m_useCompoundEdit = value;
	}

	public int getLimit()
	{
		return m_undoManager.getLimit();
	}

	public void setLimit(int limit)
	{
		m_undoManager.setLimit(limit);
	}

	public Vector getEdits()
	{
		return m_undoManager.getEdits();
	}

	public UndoableEdit getEditToBeRedone()
	{
		return m_undoManager.getEditToBeRedone();
	}

	public int countEdits()
	{
		return m_undoManager.countEdits();
	}

	public void massChange(UndoableEdit undoableEdit, boolean redo)
	{
		beginUpdates();
		m_undoManager.massChange(undoableEdit, redo);
		endUpdates();
	}

	public void discardAllEdits()
	{
		m_undoManager.discardAllEdits();
	}

 	public boolean addEditToManager(UndoableEdit lastEdit) 
	{	
		boolean		ret;

		ret = m_undoManager.addEdit(lastEdit);
		if (ret) {
			if (m_undoListener != null) {
				m_undoListener.setPreferredSizeUndo(getEdits(), lastEdit);
		}	}
		return(ret);
	}

	public void	beginUndoRedo(String name)
	{
		if (m_useCompoundEdit) {
			m_compoundEditName = name;
			m_compoundEdit     = null;
			m_compoundEditCnt  = 0;
		}
		beginUpdates();
	}

	public void endUndoRedo()
	{
		if (m_compoundEdit != null) {
			m_compoundEdit.end();
			m_compoundEdit = null;
		}
		m_compoundEditName = null;
		m_compoundEditCnt  = -1;

		endUpdates();
	}

/*
	private void testUpdate()
	{
		System.out.println("UndoableTa missed updateBegins");
		java.lang.Thread.dumpStack();
	}
*/

	public boolean logEdit(UndoableEdit anEdit) 
	{	
		boolean ret;

		m_changedFlag = true;
		if (m_compoundEditCnt < 0) {
//			testUpdate();
			ret = addEditToManager(anEdit);
		} else {
			switch (m_compoundEditCnt) {
			case 0:
				ret            = addEditToManager(anEdit);
				break;
			case 1:
				m_compoundEdit = new MyCompoundEdit(m_compoundEditName);
				ret            = m_compoundEdit.addEdit(anEdit);
				ret            = addEditToManager(m_compoundEdit);
				break;
			default:
				ret            = m_compoundEdit.addEdit(anEdit);
			}
			++m_compoundEditCnt;
		}
		updateMenu();
		if (!ret) {
			error("logEdit failed");
		}
		return(ret);
	}

	public void undo()
	{
		String s;

		if (!m_undoManager.canUndo()) {
			s = "Nothing to undo";
		} else {
			beginUpdates();
			m_undoManager.undo();
			endUpdates();
			updateMenu();
			s = "Undo done";
		}
		if (m_undoListener != null) {
			m_undoListener.undoHistoryChanged();
		}
		doFeedback(s);
	}

	public void redo()
	{
		String s;

		if (!m_undoManager.canRedo()) {
			s = "Nothing to redo";
		} else { 
			beginUpdates();
			m_undoManager.redo();
			endUpdates();
			doFeedback("Redo done");
			updateMenu();
			s = "Redo done";
		}
		if (m_undoListener != null) {
			m_undoListener.undoHistoryChanged();
		}
		doFeedback(s);
	}

	// Interface UndoableEditListener

	public void undoableEditHappened(UndoableEditEvent e)
	{
		m_undoManager.undoableEditHappened(e);
		updateMenu();
	}
}

