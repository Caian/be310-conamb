package com.example.unilink1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {
	
	public final static String hostname = "caianbene.no-ip.org";
	public final static int hostport = 3136;
	public final static int hostportd = 3137;
	
	private final static int TIMEOUT = 10000;
	
	private Socket clientSocket = null;
	private OutputStream writeStream = null;
	private InputStream readStream = null;
	
	public Boolean open() {
		try {
			this.clientSocket = new Socket(hostname, hostport);
			this.writeStream = this.clientSocket.getOutputStream();
			this.readStream = this.clientSocket.getInputStream();
			this.clientSocket.setSoTimeout(TCPClient.TIMEOUT);
			return true;
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
	
	public void close() {
		if (this.clientSocket != null)
			try {this.clientSocket.close();} 
			catch (IOException e) { }
	}
	
	public Boolean sendMessage(String message) {
		int retry = 5;
		for (int i = 0; i < retry; i++) {
			try {
				//message = message + "\n";
				byte[] bmessage = message.getBytes("UTF8");
				this.writeStream.write(bmessage);
				Thread.sleep(100);
				return true;
			} catch (UnsupportedEncodingException e) {
				return false; 
			} catch (IOException e) {
				continue;
			} catch (InterruptedException e) {
				return false;
			}
		}
		return false;
	}
	
	public Boolean sendData(byte[] data, int count) {
		try {
			this.writeStream.write(data, 0, count);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public String readMessage(int length) {
		try {
			byte[] buffer = new byte[length];
			this.readStream.read(buffer, 0, length);
			return new String(buffer, "UTF-8");
		} catch (IOException e) {
			return null;
		}
	}
	
	public String readLine() {
		try {
			String line = "";
			byte[] buffer = new byte[1];
			while (true) {
				this.readStream.read(buffer, 0, 1);
				String c = new String(buffer, "UTF-8");
				if (c.compareTo("\n") == 0)
					return line;
				else
					line += c;
			}
		} catch (IOException e) {
			return null;
		}
	}
}
