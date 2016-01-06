import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;


public class Reception implements Runnable {

	private InputStream in;
	private int message;
	private byte[] DepositBuffer;
	
	public Reception(InputStream in){
		this.in = in;
	}
	
	public void run() {
		
		while(true){
	        try {
			message = in.read();
			System.out.println(message);
		    } catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void createDepositBuffer(byte[] buffer)
	{
	    DepositBuffer=buffer.clone();
	}

	public void addToDepositBuffer(byte[] buffer)
	{
		byte[] tmp= new byte[DepositBuffer.length+buffer.length];
		for(int i=0;i<DepositBuffer.length;i++){
			tmp[i]=DepositBuffer[i];
		}
		for(int i=0;i<buffer.length;i++){
			tmp[i+DepositBuffer.length]=buffer[i];
		}
		DepositBuffer=tmp;
	}
	
	
	
	
	
}
	



