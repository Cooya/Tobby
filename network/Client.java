package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import main.FatalError;

class Client {
	private Socket client;
	private InputStream inputStream;
	private OutputStream outputStream;
	
	protected Client(String serverIP, int port) throws IOException {
		this.client = new Socket(serverIP, port);
		this.inputStream = this.client.getInputStream();
		this.outputStream = this.client.getOutputStream();
	}
	
	protected Client(Socket client) {
		try {
			this.client = client;
			this.inputStream = this.client.getInputStream();
			this.outputStream = this.client.getOutputStream();
		} catch(Exception e) {
			throw new FatalError(e);
		}
	}
	
	protected Client(String proxyIP, int proxyPort, String serverIP, int serverPort) {
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
	
	protected void send(byte[] bytes) throws IOException {
		this.outputStream.write(bytes);
	}
	
	protected int receive(byte[] buffer) throws IOException {
		return this.inputStream.read(buffer);
	}
	
	protected int receive(byte[] buffer, int timeout) throws IOException {
		this.client.setSoTimeout(timeout);
		int bytes = receive(buffer);
		this.client.setSoTimeout(0);
		return bytes;
	}
	
	protected void close() {
		try {
			this.inputStream.close();
			this.outputStream.close();
			this.client.close();
		} catch(Exception e) {
			throw new FatalError(e);
		}
	}
	
	protected boolean isClosed() {
		return this.client.isClosed();
	}
}