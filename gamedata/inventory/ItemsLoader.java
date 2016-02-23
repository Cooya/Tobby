package gamedata.inventory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tmp;
	}
}
