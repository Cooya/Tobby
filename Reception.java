import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;


public class Reception implements Runnable {

	private InputStream in;
	private String message = null;
	
	public Reception(InputStream in){
		
		this.in = in;
	}
	
	public void run() {
		
		while(true){/*
	        try {
	        	
			message = in.read(arg0);
		    } catch (IOException e) {
				
				e.printStackTrace();
			}*/
		}
	}

}
