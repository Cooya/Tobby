package utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import messages.Message;

public class WpeLogsHandler {
	
	public static void handler(String[] args) {
		String inputfile= "Log.txt";
		String outputfile= "test.txt";
		try {
			InputStream ips;
			OutputStream ops;
			ips = new FileInputStream(inputfile);
			ops = new FileOutputStream(outputfile);
			InputStreamReader ipsr=new InputStreamReader(ips);
			OutputStreamWriter opsw=new OutputStreamWriter(ops);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne;
			while ((ligne=br.readLine()) != null){
				String[] res=ligne.split("  ");
				int nb_byte=Integer.parseInt(res[3]);
				int nbLineBytes=nb_byte/16+2;
				String write=res[4]+" message ";
				int i=0;
				while(i<nbLineBytes){
					String content= br.readLine();
					String[] res0=content.split("  ");
					if(res0.length==1)
						break;
					res0=res0[1].split(" ");
					if(i==0){
						int id=(int) ((int) (Character.getNumericValue(res0[0].charAt(0))*Math.pow(16, 3))+(Character.getNumericValue(res0[0].charAt(1))*Math.pow(16, 2))+(Character.getNumericValue(res0[1].charAt(0))*16))+(Character.getNumericValue(res0[1].charAt(1)));
						id=id >> 2;
						write=write.concat(id+" ");
						String tmp;
						if((tmp=Message.get(id))!=null){
							write=write.concat("("+tmp+")\n");
						}
						write=write.concat("Size: "+nb_byte+" bytes\r\n");
						System.out.println(write);
						opsw.write(write);
						opsw.flush();
						
					}
					i++;
				}
				
			}
			opsw.close();
			br.close(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
