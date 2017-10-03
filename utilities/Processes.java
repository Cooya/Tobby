package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import main.Log;
import main.Main;

public class Processes {
	private static final String WIN_TASKLIST = "tasklist";
	private static final String LINUX_PIDOF = "pidof";
	private static final String KILL;
	
	static {
		if(Main.IS_WINDOWS)
			KILL = "taskkill /F /IM";
		else
			KILL = "pkill";
	}
	
	public static boolean inProcess(String processName) {
		if(Main.IS_WINDOWS)
			try {
			    String line;
			    Process tasklist = Runtime.getRuntime().exec(WIN_TASKLIST);
			    BufferedReader input = new BufferedReader(new InputStreamReader(tasklist.getInputStream()));
			    while((line = input.readLine()) != null)
			        if(line.split(" ")[0].equals(processName))
			        	return true;
			    input.close();
			} catch(Exception e) {
			    e.printStackTrace();
			}
		else
			try {
				Process pidof = Runtime.getRuntime().exec(LINUX_PIDOF + " " + processName);
				BufferedReader input = new BufferedReader(new InputStreamReader(pidof.getInputStream()));
				if(input.readLine() != null)
					return true;
			} catch(Exception e) {
				e.printStackTrace();
			}
		return false;
	}
	
	public static void killProcess(String processName) {
		try {
			Runtime.getRuntime().exec(KILL + " " + processName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void injectDLL(String libName, String exeName) {
		try {
			Runtime.getRuntime().exec("A COMPLETER" + " " + libName + " " + exeName);
			//InputStream in = process.getInputStream();
			//byte[] bytes = new byte[ByteArray.BUFFER_DEFAULT_SIZE];
			//in.read(bytes);
			Log.info("Library injected in \"" + exeName + "\" process.");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean fileExists(String filePath) {
		File f = new File(filePath);
		if(f.exists() && !f.isDirectory())
		    return true;
		return false;
	}
	
	public static boolean dirExists(String dirPath) {
		File f = new File(dirPath);
		if(f.exists() && f.isDirectory())
			return true;
		return false;
	}
}