package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import main.Log;
import main.Main;

public class Processes {
	private static String INJECTOR_PATH = System.getProperty("user.dir") + "/Ressources/DLLInjector/Injector.exe";
	private static String WIN_PROC_LIST = "tasklist";
	private static String WIN_KILL = "taskkill /F /IM ";
	
	public static boolean inProcess(String processName) {
		try {
		    String line;
		    Process tasklist = Runtime.getRuntime().exec(WIN_PROC_LIST);
		    BufferedReader input = new BufferedReader(new InputStreamReader(tasklist.getInputStream()));
		    while ((line = input.readLine()) != null)
		        if(line.split(" ")[0].equals(processName))
		        	return true;
		    input.close();
		} catch(Exception e) {
		    e.printStackTrace();
		}
		return false;
	}
	
	public static void killProcess(String processName) {
		try {
			Runtime.getRuntime().exec(WIN_KILL + processName);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void injectDLL(String dllName, String exeName) {
		try {
			Process p = Runtime.getRuntime().exec(INJECTOR_PATH + " " + dllName + " " + exeName);
			InputStream in = p.getInputStream();
			byte[] bytes = new byte[Main.BUFFER_DEFAULT_SIZE];
			in.read(bytes);
			Log.info("DLL injected in AS launcher process.");
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
}