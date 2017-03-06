package leaf.tools;

/**
 * Displays log according to level of awareness
 * @author ramol_na
 *
 */
public class LeafLog {

	/**
	 * Awareness level
	 * 0 = no log
	 * 1 = error (of Leaf)
	 * 2 = warning + error
	 * 3 = info + warning + error
	 * 4 = milestone + info + warning + error 
	 * 5 = debug + the rest (all)
	 */
	public static final int LOGLEVEL = 5;
	
	
	public static void e(String meta, String message)
	{
		if(LOGLEVEL >= 1)
		{
			System.out.println("ERROR | "+meta+" | "+message);
		}
	}
	
	public static void w(String meta, String message)
	{
		if(LOGLEVEL >= 2)
		{
			System.out.println("WARN  | "+meta+" | "+message);
		}
	}
	
	public static void i(String meta, String message)
	{
		if(LOGLEVEL >= 3)
		{
			System.out.println("INFO  | "+meta+" | "+message);
		}
	}
	
	public static void m(String meta, String message)
	{
		if(LOGLEVEL >= 4)
		{
			System.out.println("MILST | "+meta+" | "+message);
		}
	}
	
	public static void d(String meta, String message)
	{
		if(LOGLEVEL >= 5)
		{
			System.out.println("DEBUG | "+meta+" | "+message);
		}
	}
}
