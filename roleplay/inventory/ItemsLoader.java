package game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

public class ItemsLoader {
	
	public static Hashtable<Integer,String> LoadMap(){
		Hashtable<Integer,String> tmp= new Hashtable<Integer,String>();
		try {
		File f=new File("Items.txt");
		FileReader fip = null;
		String line="";
		fip=new FileReader(f);
		BufferedReader br=new BufferedReader(fip);
			while ((line = br.readLine()) != null) {
				String[] parseline=line.split("	");
				tmp.put(Integer.valueOf(parseline[0]),parseline[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tmp;
	}
}