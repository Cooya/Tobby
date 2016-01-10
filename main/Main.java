package main;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {
	public static void main(String[] args) {
		try {
			Socket socket = new Socket("213.248.126.39", 5555);
			Sender.create(socket.getOutputStream());
			InputStream is = socket.getInputStream();
			byte[] buffer = new byte[8192];
			
			System.out.println("Connecting to server, waiting response...");
			int bytesReceived = 0;
			while(true) {
				bytesReceived = is.read(buffer);
				if(bytesReceived == -1)
					break;
				System.out.println();
				System.out.println(bytesReceived + " bytes received.");
				Reader.processBuffer(new ByteArray(buffer, bytesReceived));
			}
			System.out.println("Deconnected from authentification server.");

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
