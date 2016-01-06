import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class Main {

	public static void main(String[] args) {
				boolean isConnected=true;
				try {
					Socket s=new Socket(InetAddress.getByName("213.248.126.39"),5555);

					Emission e=new Emission(s.getOutputStream());
					Reception r=new Reception(s.getInputStream());
					Thread t1=new Thread(new Emission(s.getOutputStream()));
					t1.start();
					Thread t2=new Thread(new Reception(s.getInputStream()));
					t2.start();
					
					while(isConnected){
						
					}
					
					
					s.close();
					
					System.out.println(s.isClosed());
					
					
					
					
					
					
					
					
					
					} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			
	}

}
