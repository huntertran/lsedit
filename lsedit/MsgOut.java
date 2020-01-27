package lsedit;

public class MsgOut {

	protected static boolean dbflag = false;
	protected static boolean vflag  = false;

	static public void setDebugFlag(boolean state) 
	{
		dbflag = state;
	}

	static public void setVerboseFlag(boolean state) 
	{
		vflag = state;
	}

	static public void dprintln(String msg) 
	{
		if (dbflag) {
			System.out.println(msg);
			System.out.flush();
		}
	}

	static public void dprint(String msg) 
	{
		if (dbflag) {
			System.out.print(msg);
			System.out.flush();
		}
	}

	static public void vprintln(String msg) 
	{
		if (vflag) {
			System.out.println(msg);
			System.out.flush();
		}
	}

	static public void vprint(String msg) 
	{
		if (vflag) {
			System.out.print(msg);
			System.out.flush();
		}
	}

	static public void println(String msg) 
	{ 
		System.out.println(msg);
		System.out.flush();
	}

	static public void print(String msg) 
	{
		System.out.print(msg);
		System.out.flush();
	}
}

