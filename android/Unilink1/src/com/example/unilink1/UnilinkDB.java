package com.example.unilink1;

import java.util.HashMap;
import java.util.Vector;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
	
	// Variáveis -------------------------------------------
	
	private HashMap<Marker, BasePin> pins;
	
	private String username = "";
	private String password = "";
	private TCPClient client;
	
	// Métodos ---------------------------------------------
	
	// -----------------------------------------------------
	// Construtor
	// -----------------------------------------------------
	private UnilinkDB()
	{
		client = new TCPClient();
		pins = new HashMap<Marker, BasePin>();
	}
	
	
	// -----------------------------------------------------
	// Define o usuário a ser usado nas requisições
	// -----------------------------------------------------
	public void setUser(String u, String p) {
		this.username = u;
		this.password = p;
	}

	
	// -----------------------------------------------------
	// Coloca o cabeçalho nas mensagens
	// -----------------------------------------------------
	private String messageHead() {
		return ""; //this.username + "\n" + this.password + "\n";
	}
	
	
	// -----------------------------------------------------
	// Atualiza os pinos próximos a uma latitude/longitude
	// -----------------------------------------------------
	public void updateNear(double latitude, double longitude, PinListener listener) {
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
		LongUpdateNearPins updater = new LongUpdateNearPins();
		updater.setListener(listener);
		updater.execute(messageHead() + "NEAR" + lats + lons);
	}

	
	// -----------------------------------------------------
	// Converte a string do formato sem espaços para o 
	// padrão
	// -----------------------------------------------------
	public String decodeString(String s) {
		return s.replace("\\s", " ").replace("\\\\", "\\");
	}

	
	// -----------------------------------------------------
	// Cria um pino base a partir de uma requisição de NEAR
	// -----------------------------------------------------
	public BasePin parseNearResponse(String response) {
		String[] tokens = response.split(" ");
		if (tokens.length != 4) 
			return null;
		
		if (tokens[0].compareTo("NEAR") != 0)
			return null;
		
		long uid = Long.parseLong(tokens[2]);
		long date = Long.parseLong(tokens[3]);
		
		return new BasePin(uid, date, 0, 0);
	}
	
	
	// -----------------------------------------------------
	// Cria um pino específico a partir de requisições
	// MARK - Pino de marcador
	// NEWS - Pino de notícia
	// -----------------------------------------------------
	public BasePin parseGetResponse(String response) {
		String[] tokens = response.split(" ");
		if (tokens.length == 0) 
			return null;
		
		if (tokens[0].compareTo("MARK") == 0) {
			if (tokens.length != 7) 
				return null;
			
			long uid = Long.parseLong(tokens[1]);
			long date = Long.parseLong(tokens[2]);
			long type = Long.parseLong(tokens[3]);
			long icon = Long.parseLong(tokens[4]);
			double lat = Double.parseDouble(tokens[5]);
			double lon = Double.parseDouble(tokens[6]);
			
			return new MarkerPin(uid, date, lat, lon, type, icon);
		}
		else if (tokens[0].compareTo("NEWS") == 0) {
			if (tokens.length != 9) 
				return null;
			
			long uid = Long.parseLong(tokens[1]);
			long date = Long.parseLong(tokens[2]);
			double lat = Double.parseDouble(tokens[3]);
			double lon = Double.parseDouble(tokens[4]);
			long upvt = Long.parseLong(tokens[5]);
			long dnvt = Long.parseLong(tokens[6]);
			String name = decodeString(tokens[7]);
			String text = decodeString(tokens[8]);
			
			return new NewsPin(uid, date, lat, lon, name, text, upvt, dnvt);
		}
		else {
			return null;
		}
	}
	
	// -----------------------------------------------------
	// Busca um pino a partir de um marcador
	// -----------------------------------------------------
	public BasePin getPin(Marker marker) {
		return this.pins.get(marker);
	}
	
	
	// -----------------------------------------------------
	// Tarefa assíncrona de baixar novos pinos
	// -----------------------------------------------------
	public class LongUpdateNearPins extends AsyncTask<String, Integer, Boolean> {
		
		// Variáveis -------------------------------------------
		
		private Vector<BasePin> pins = new Vector<BasePin>();
		private Vector<Marker> markers = new Vector<Marker>(); 
		private PinListener listener = null;
		
		// Métodos ---------------------------------------------
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			
			if (!client.open())
				return false;
			
			// Monta a mensagem
			client.sendMessage(arg0[0]);
			
			String response;
			while ((response = client.readLine()).compareTo("EORQ") != 0) {
				BasePin p = parseNearResponse(response);
				if (p != null) pins.add(p);
			}
			
			// Filter
			
			for (int i = 0; i < pins.size(); i++) {
				client.sendMessage(String.format("GETD%d",pins.get(i).getUid()));
				BasePin p = parseGetResponse(client.readLine());
				if (p == null) {
					pins.remove(i--);
				}
				else {
					pins.set(i, p);
				}
			}
			
			client.close();
			
			return true;
		}
		
		public void setListener(PinListener listener) {
			this.listener  = listener;
			
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (this.listener != null) {
				for (int i = 0; i < this.pins.size(); i++)
					listener.OnNewPin(this.pins.get(i));
			}
		}
	}
}
