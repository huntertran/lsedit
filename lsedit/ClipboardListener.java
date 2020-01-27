package lsedit;

/* This interface decouples clipboard operations from the clipboard box
 * That allows it to be used in other products more easily
 */

public interface ClipboardListener {

	abstract void clipboardChanged();
}

