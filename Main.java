import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class Main {
	public static void main(String[] args) {
		try {
			Socket socket = new Socket("213.248.126.39", 5555);
			InputStream is = socket.getInputStream();
			byte[] buffer = new byte[8192];
			
			int read = 0;
			while(read != - 1) {
				read = is.read(buffer);
				System.out.println(read + " bytes received.");
				
			}

			is.close();
			socket.close();		
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}

/*
Emission e=new Emission(s.getOutputStream());
Reception r=new Reception(s.getInputStream());
Thread t1=new Thread(new Emission(s.getOutputStream()));
t1.start();
Thread t2=new Thread(new Reception(s.getInputStream()));
t2.start();
*/
