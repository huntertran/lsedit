package lsedit;

public interface ToolBarEventHandler {

	abstract boolean	processMetaKeyEvent(String name);
	abstract void		processKeyEvent(int key, int modifiers, Object object);
	abstract void		showInfo(String msg);
}

