package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import main.Main;

public class Processes {
	private static String INJECTOR_PATH = System.getProperty("user.dir") + "/Ressources/DLLInjector/Injector.exe";
	
	public synchronized static boolean inProcess(String processName) {
		try {
		    String line;
		    Process p = Runtime.getRuntime().exec("tasklist");
		    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    while ((line = input.readLine()) != null)
		        if(line.split(" ")[0].equals(processName))
		        	return true;
		    input.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return false;
	}
	
	public synchronized static void injectDLL(String dllName, String exeName) {
		try {
			Process p = Runtime.getRuntime().exec(INJECTOR_PATH + " " + dllName + " " + exeName);
			InputStream in = p.getInputStream();
			byte[] bytes = new byte[Main.BUFFER_DEFAULT_SIZE];
			in.read(bytes);
			System.out.println("DLL Injection.\n" + new String(bytes));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static boolean fileExists(String filePath) {
		File f = new File(filePath);
		if(f.exists() && !f.isDirectory())
		    return true;
		return false;
	}
}