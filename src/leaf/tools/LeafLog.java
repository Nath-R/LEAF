package leaf.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


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
	
	/**
	 * Enable or not the file logging.
	 * If active, a log file is written at root
	 */
	public static final boolean LOGFILEENABLE = true;
	
	/**
	 * File log writer
	 */
	private static FileWriter fw ;
	
	
	public static void e(String meta, String message)
	{
		if(LOGLEVEL >= 1)
		{
			Date today = new Date();
			String dateStr = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.FRANCE).format(today);
			String logStr = dateStr+" | ERROR | "+meta+" | "+message;
			System.out.println(logStr);
			writeToFile(logStr);
		}
	}
	
	public static void w(String meta, String message)
	{
		if(LOGLEVEL >= 2)
		{
			Date today = new Date();
			String dateStr = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.FRANCE).format(today);
			String logStr = dateStr+" | WARN  | "+meta+" | "+message;
			System.out.println(logStr);
			writeToFile(logStr);
		}
	}
	
	public static void i(String meta, String message)
	{
		if(LOGLEVEL >= 3)
		{
			Date today = new Date();
			String dateStr = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.FRANCE).format(today);
			String logStr = dateStr+" | INFO  | "+meta+" | "+message;
			System.out.println(logStr);
			writeToFile(logStr);
		}
	}
	
	public static void m(String meta, String message)
	{
		if(LOGLEVEL >= 4)
		{
			Date today = new Date();
			String dateStr = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.FRANCE).format(today);
			String logStr = dateStr+" | MILST | "+meta+" | "+message;
			System.out.println(logStr);
			writeToFile(logStr);
		}
	}
	
	public static void d(String meta, String message)
	{
		if(LOGLEVEL >= 5)
		{
			Date today = new Date();
			String dateStr = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.FRANCE).format(today);
			String logStr = dateStr+" | DEBUG | "+meta+" | "+message;
			System.out.println(logStr);
			writeToFile(logStr);
		}
	}
	
	private static void writeToFile(String message)
	{
		if(LOGFILEENABLE)
		{
			try {			
				if(fw==null)
				{
					fw = new FileWriter(new File("leaf.log") );							
				}
				
				BufferedWriter bw = new BufferedWriter(fw);
				
				bw.write(message+"\n");
				
				bw.flush();
				
			} catch (IOException e) {			
				e.printStackTrace();
				System.out.println("ERROR | LOG | "+" Unable to write logs into file !");
			}
		}
	}
}
