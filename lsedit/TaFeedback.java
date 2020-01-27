package lsedit;

/* This interface decouples Ta.java from LandscapeEditorCore
 * That allows it to be used in other products more easily
 */

public interface TaFeedback {

	// Simple messages
	abstract void showProgress(String message);
	abstract void doFeedback(String message);
	abstract void showInfo(String message);
	abstract void error(String message);

	// Specific errors in loading TA worth reporting in detail
	 abstract void showCycle(RelationInstance ri);
	 abstract void noContainRelation(String taPath);
	 abstract void hasMultipleParents(RelationClass rc, EntityInstance e);
}

