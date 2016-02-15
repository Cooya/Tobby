package main;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public interface Connection {
	void send(byte[] bytes);
	int receive(byte[] bytes);
	void close();

	public class Client implements Connection {
		private Socket client;
		private InputStream inputStream;
		private OutputStream outputStream;
		
		public Client(String serverIP, int port) {
			try {
				this.client = new Socket(serverIP, port);
				this.inputStream = this.client.getInputStream();
				this.outputStream = this.client.getOutputStream();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public Client(Socket client) {
			try {
				this.client = client;
				this.inputStream = this.client.getInputStream();
				this.outputStream = this.client.getOutputStream();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void send(byte[] bytes) {
			try {
				this.outputStream.write(bytes);
				this.outputStream.flush();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		public int receive(byte[] buffer) {
			int bytesReceived = -1;
			try {
				bytesReceived = this.inputStream.read(buffer);
			} catch (Exception e) {
				//e.printStackTrace();
				return bytesReceived;
			}
			return bytesReceived;
		}
		
		public void close() {
			try {
				this.inputStream.close();
				this.outputStream.close();
				this.client.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public boolean isClosed() {
			return this.client.isClosed();
		}
	}
	
	public class Server implements Connection {
		private ServerSocket server;
		private Client client = null;
		
		public Server(int port) {
			try {
				this.server = new ServerSocket(port);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void send(byte[] bytes) {
			this.client.send(bytes);
		}

		public int receive(byte[] bytes) {
			return this.client.receive(bytes);
		}

		public void close() {
			try {
				this.client.close();
				this.server.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void closeClient() {
			try {
				this.client.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void waitClient() {
			try {
				this.client = new Client(server.accept());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public boolean haveClient() {
			return this.client != null || !this.client.isClosed();
		}
	}
}
