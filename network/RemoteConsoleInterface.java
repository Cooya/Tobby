package network;

import java.io.IOException;
import java.util.Queue;
import java.util.Scanner;

import main.Log;
import main.Main;
import messages.NetworkMessage;
import messages.UserCommandMessage;
import messages.connection.HelloConnectMessage;
import utilities.ByteArray;

public class RemoteConsoleInterface {
	
	public static void start() {
		Client client = null;
		try {
			client = new Client(Main.LOCALHOST, Main.SERVER_PORT);
			Log.info("Connected to server.");
		} catch(IOException e) {
			Log.err("Server offline.");
			return;
		}
		
		// récupération automatique des logs
		UserCommandMessage cmdMsg = new UserCommandMessage();
		/*
		cmdMsg.command = "log";
		try {
			client.send(cmdMsg.pack(0));
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		*/
		
		ByteArray array = new ByteArray(0);
		byte[] buffer = new byte[ByteArray.BUFFER_DEFAULT_SIZE];
		int bytesReceived;
		Reader reader = new Reader();
		NetworkMessage msg;
		Queue<NetworkMessage> msgStack;
		Scanner sc = new Scanner(System.in);
		Log.info("Command line interface ready.");
		while(sc.hasNext()) {
			cmdMsg.command = sc.nextLine();
			try {
				client.send(cmdMsg.pack(0));
				receivingLoop: // label
				while(true) {
					if((bytesReceived = client.receive(buffer)) == -1)
						break;
					array.setArray(buffer, bytesReceived);
					msgStack = reader.processBuffer(array);
					while((msg = msgStack.poll()) != null) {
						if(msg instanceof UserCommandMessage) {
							msg.deserialize();
							System.out.println(((UserCommandMessage) msg).command);
							break receivingLoop;
						}
						else if(msg instanceof HelloConnectMessage)
							;
						else
							Log.err("Invalid message received : " + msg.getId() + ".");
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
				break;
			}
		}
		sc.close();
		client.close();
	}
}