package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public interface Connection {
	void send(byte[] bytes);
	int receive(byte[] bytes) throws Exception;
	void close();

	public class Client implements Connection {
		private Socket client;
		private InputStream inputStream;
		private OutputStream outputStream;
		
		public Client(String serverIP, int port) {
			while(true)
				try {
					this.client = new Socket(serverIP, port);
					this.inputStream = this.client.getInputStream();
					this.outputStream = this.client.getOutputStream();
					break;
				} catch(Exception e) {
					Log.err(e.getClass().getSimpleName() + " : " + e.getMessage());
				}
		}
		
		public Client(Socket client) {
			try {
				this.client = client;
				this.inputStream = this.client.getInputStream();
				this.outputStream = this.client.getOutputStream();
			} catch(Exception e) {
				throw new FatalError(e);
			}
		}
		
		public Client(String proxyIP, int proxyPort, String serverIP, int serverPort) {
			try {
				Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyIP, proxyPort));
				this.client = new Socket(proxy);
				this.client.connect(new InetSocketAddress(serverIP, serverPort));
				this.inputStream = this.client.getInputStream();
				this.outputStream = this.client.getOutputStream();
			} catch(Exception e) {
				throw new FatalError(e);
			}
		}
		
		public void send(byte[] bytes) {
			try {
				this.outputStream.write(bytes);
				this.outputStream.flush();
			} catch(Exception e) {
				throw new FatalError(e);
			}
		}
		
		public int receive(byte[] buffer) {
			try {
				return this.inputStream.read(buffer);
			} catch(IOException e) {
				Log.err(e.getClass().getSimpleName() + " : " + e.getMessage());
				return -1;
			}
		}
		
		public int receive(byte[] buffer, int timeout) throws SocketException {
			this.client.setSoTimeout(timeout);
			int bytes = receive(buffer);
			this.client.setSoTimeout(0);
			return bytes;
		}
		
		public void close() {
			try {
				this.inputStream.close();
				this.outputStream.close();
				this.client.close();
			} catch(Exception e) {
				throw new FatalError(e);
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
			} catch(Exception e) {
				throw new FatalError(e);
			}
		}
		
		public void send(byte[] bytes) {
			this.client.send(bytes);
		}

		public int receive(byte[] bytes) {
			if(this.client.isClosed())
				throw new FatalError("Connection closed.");
			return this.client.receive(bytes);
		}

		public void close() {
			try {
				this.client.close();
				this.server.close();
			} catch(Exception e) {
				throw new FatalError(e);
			}
		}
		
		public void closeClient() {
			try {
				this.client.close();
			} catch(Exception e) {
				throw new FatalError(e);
			}
		}
		
		public void waitClient() {
			try {
				this.client = new Client(server.accept());
			} catch (Exception e) {
				throw new FatalError(e);
			}
		}
		
		public boolean haveClient() {
			return this.client != null || !this.client.isClosed();
		}
	}
}