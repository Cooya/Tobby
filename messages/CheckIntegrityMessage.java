package messages;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import utilities.ByteArray;
import utilities.Encryption;
import utilities.Log;

public class CheckIntegrityMessage extends Message {
	public static final int ID = 6372;

	public CheckIntegrityMessage(byte[] publicKey, String gameServerTicket, byte[] RDM) {
		super(ID, 0, 0, null);
		
		createServerEmulation(publicKey, gameServerTicket, RDM);
	}
	
	private void createServerEmulation(byte[] publicKey, String gameServerTicket, byte[] RDM) {
		String publicKeyPEM = Encryption.makePEM(publicKey);
		ByteArray buffer = new ByteArray();
		buffer.writeInt(publicKeyPEM.length() + gameServerTicket.length() + 4); // + 2 shorts
		buffer.writeUTF(publicKeyPEM);
		buffer.writeUTF(gameServerTicket);
		
		try {
			Log.p("Running emulation server.");
			ServerSocket server = new ServerSocket(5555);
			
			Socket client = server.accept(); // script AS
			Log.p("AS script connected.");
			OutputStream os = client.getOutputStream();
			os.write(buffer.bytes());
			Log.p(buffer.getSize() + " bytes send to AS script.");
			
			client = server.accept(); // client Dofus
			Log.p("Dofus client connected.");
			InputStream in = client.getInputStream();
			byte[] buffer2 = new byte[8192];
			
			int bytesReceived = 0;
			while(true) {
				bytesReceived = in.read(buffer2);
				if(bytesReceived == -1)
					break;
				Log.p(bytesReceived + " bytes received from Dofus client.");
			}
			
			in.close();
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
