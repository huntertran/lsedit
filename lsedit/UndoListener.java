package lsedit;

import java.util.Vector;
import javax.swing.undo.UndoableEdit;


/* This interface decouples UndoableTa.java and TemporalTa.java from LandscapeEditorCore
 * That allows it to be used in other products more easily
 */

public interface UndoListener {

	abstract void setEnabledRedo(boolean value);
	abstract void setEnabledUndo(boolean value);
	abstract void setPreferredSizeUndo(Vector edits, UndoableEdit lastEdit);
	abstract void undoHistoryChanged();
}

