package com.example.unilink1;

import java.lang.reflect.Array;
import java.util.Vector;

import android.os.AsyncTask;

public class UnilinkDB {
	
	// Singleton -------------------------------------------
	
	private static UnilinkDB singleton = null;
	public static UnilinkDB getDatabase() {
		if (singleton == null) {
			singleton = new UnilinkDB();
		}
		return singleton;
	}
	
	// Variaveis -------------------------------------------
	
	private String username = "";
	private String password = "";
	private TCPClient client;
	
	// Metodos ---------------------------------------------
	
	private UnilinkDB()
	{
		client = new TCPClient();
	}
	
	public void setUser(String u, String p) {
		this.username = u;
		this.password = p;
	}
	
	private String messageHead() {
		return ""; //this.username + "\n" + this.password + "\n";
	}
	
	public void updateNear(double latitude, double longitude) {
		// Comprime lat e long para angulo + minuto
		latitude += 90;
		longitude += 180;
		
		Integer[] latq = new Integer[2];
		Integer[] lonq = new Integer[2];
		
		latq[0] = (int)latitude;
		latq[1] = (int)(60.0 * (latitude - latq[0]));
		
		lonq[0] = (int)longitude;
		lonq[1] = (int)(60.0 * (longitude - lonq[0]));
		
		String lats = String.format("%03d", latq[0]) + 
				String.format("%02d", latq[1]);
		String lons = String.format("%03d", lonq[0]) + 
				String.format("%02d", lonq[1]);
		
		// Up we go...
		new LongSendMessage().execute(messageHead() + "NEAR" + lats + lons);
	}
	
	public class LongSendMessage extends AsyncTask<String, Integer, Boolean> {
		Vector<String> responses = new Vector<String>(); 

		@Override
		protected Boolean doInBackground(String... arg0) {
			if (!client.open())
				return false;
			
			// Monta a mensagem
			client.sendMessage(arg0[0]);
			
			String response;
			while ((response = client.readLine()).compareTo("EORQ") != 0) {
				this.responses.add(response);
			}
			
			client.close();
			return true;
		}
	}
	
	//public class LongDownloadMessages extends LongSendMessage
}
