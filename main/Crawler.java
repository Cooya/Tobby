package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
	private static final String SOURCES_PATH = "C:\\Users\\Nicolas\\Documents\\Programmation\\Java\\Tobby\\Workshop\\Client décompilé 2.34.4";
	private static final String MSG_FILE = "Workshop/messagesList.txt";
	private static final Pattern regexp = Pattern.compile("protocolId:uint = ([0-9]+|0x[0-9A-F]+);");
	
	public static void buildMessagesFile() {
		StringBuilder str = new StringBuilder();
		String[] splitLine;
		for(String line : retrieveAllNetworkMessages()) {
			splitLine = line.split(" ");
			str.append("messages.put(");
			str.append(Integer.valueOf(splitLine[1]));
			str.append(", \"" + splitLine[0] + "\");\n");
		}
		
		try {
			FileWriter fileWriter = new FileWriter(MSG_FILE, false);
			fileWriter.write(str.toString());
			fileWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Vector<String> retrieveAllNetworkMessages() {
		Vector<File> files = new Vector<File>();
		listAllFilesInDir(new File(SOURCES_PATH), files);
		
		Vector<String> messages = new Vector<String>();
		int protocolId;
		String filename;
		for(File file : files) {
			filename = file.getName();
			if(filename.endsWith("Message.as")) {
				protocolId = getProtocolId(file);
				if(protocolId > -1)
					messages.add(filename.replaceFirst("[.][^.]+$", "") + " " + protocolId);
			}
		}
		java.util.Collections.sort(messages);
		return messages;
	}
	
	private static int getProtocolId(File file) {
		BufferedReader buffer;
		String str;
		Matcher match;
		try {
			buffer = new BufferedReader(new FileReader(file));
			str = buffer.readLine();
			while(str != null) {
				match = regexp.matcher(str);
				if(match.find()) {
					buffer.close();
					str = match.group(1);
					if(str.startsWith("0x"))
						return Integer.parseInt(str.replace("0x", ""), 16); 
					return Integer.valueOf(str);
				}
				str = buffer.readLine();
			}
			buffer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private static void listAllFilesInDir(File dir, Vector<File> files) {
		for(File file : dir.listFiles())
			if(file.isDirectory())
				listAllFilesInDir(file, files);
			else
				files.add(file);
	}
}