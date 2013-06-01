package com.example.unilink1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Vector;
import com.google.android.gms.maps.model.Marker;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;

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
	
	private final String USERFILE = "user.info";
	
	private HashMap<Marker, BasePin> pins;
	
	private String username = "";
	private String password = "";
	private Boolean validated = false;
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
		this.validated = false;
	}
	
	
	// -----------------------------------------------------
	// Carrega o usuário do armazenamento interno
	// -----------------------------------------------------
	public void loadUserFromStorage(Context c) {
		try {
			FileInputStream fos = c.openFileInput(USERFILE);
			InputStreamReader s = new InputStreamReader(fos);
			BufferedReader b = new BufferedReader(s);
			String u = b.readLine();
			String p = b.readLine();
			if (u != null && !u.isEmpty() && p != null && !p.isEmpty()) {
				this.username = u;
				this.password = p;
			}
			b.close();
		} catch (FileNotFoundException e) {
			// Nada...
		} catch (IOException e) {
			// Hue?
		}
	}
	
	// -----------------------------------------------------
	// Salva o usuário no armazenamento interno
	// -----------------------------------------------------
	public void saveUserToStorage(Context c) {
		try {
			FileOutputStream fos = c.openFileOutput(USERFILE, Context.MODE_PRIVATE);
			PrintStream s = new PrintStream(fos);
			s.println(this.username);
			s.println(this.password);
			s.close();
		} catch (FileNotFoundException e) {
			// Nada...
		}
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
		updater.execute("NEAR" + lats + lons);
	}

	
	// -----------------------------------------------------
	// Converte a string do formato padrão para o sem 
	// espaços 
	// -----------------------------------------------------
	public String encodeString(String s) {
		try {
			s = Base64.encodeToString(s.getBytes("UTF-8"), 
				Base64.URL_SAFE | Base64.NO_WRAP);
		} catch (UnsupportedEncodingException e) {
			s = "";
		}
		return s;
	}
	
	
	// -----------------------------------------------------
	// Converte a string do formato sem espaços para o 
	// padrão
	// -----------------------------------------------------
	public String decodeString(String s) {
		try {
			s = new String(Base64.decode(s, Base64.URL_SAFE), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			s = "";
		}
		return s;
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
	// Valida o usuário e senha
	// -----------------------------------------------------
	public Boolean hasUser() {
		if (this.username.isEmpty() || this.password.isEmpty())
			return false;
		
		return true;
	}
	
	
	// -----------------------------------------------------
	// Valida o usuário e senha
	// -----------------------------------------------------
	public void validateUser() {
		
		if (!hasUser())
			return;
		
		new LongValidateUser().execute();
	}
	
	
	// -----------------------------------------------------
	// Valida o usuário e senha
	// -----------------------------------------------------
	public Integer validateUserB() {
		
		if (!hasUser())
			return -1;
		
		synchronized (client) {
		
			if (!client.open())
				return -2;
			
			// Monta a mensagem
			client.sendMessage("VALU" + encodeString(this.username) + 
					" " + encodeString(this.password));
			
			int code;
			
			while(true) {
				String response = client.readLine();
				if (response == null)
					return -1;
				if (response.startsWith("UUSID")) {
					this.validated = true;
					code = 0;
					break;
				} else if (response.compareTo("UFAIL") == 0) {
					code = -3;
					break;
				}
			}
			
			client.close();
			return code;
		}
	}
	
	
	// -----------------------------------------------------
	// Verifica se o usuário foi validado pelo servidor
	// -----------------------------------------------------
	public Boolean isValidated() {
		return this.validated;
	}
	
	
	// -----------------------------------------------------
	// Tarefa assíncrona de baixar novos pinos
	// -----------------------------------------------------
	public class LongUpdateNearPins extends AsyncTask<String, Integer, Boolean> {
		
		// Variáveis -------------------------------------------
		
		private Vector<BasePin> pins = new Vector<BasePin>();
		private PinListener listener = null;
		
		// Métodos ---------------------------------------------
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			
			synchronized (client) {
			
				if (!client.open())
					return false;
				
				// Monta a mensagem
				client.sendMessage(arg0[0]);
				
				String response;
				while (true) {
					response = client.readLine();
					if (response == null)
						break;
					if (response.compareTo("EORQ") == 0)
						break;
					BasePin p = parseNearResponse(response);
					if (p != null) pins.add(p);
				}
				
				// Filter
				
				for (int i = 0; i < pins.size(); i++) {
					client.sendMessage(String.format("GETD%d",pins.get(i).getUid()));
					response = client.readLine();
					BasePin p = null;
					if (response != null)
						p = parseGetResponse(response);
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
		}
		
		public void setListener(PinListener listener) {
			this.listener  = listener;
			
		}

		@Override
		protected void onPostExecute(Boolean result) {
			
			if (!result)
				return;
			
			if (this.listener != null) {
				for (int i = 0; i < this.pins.size(); i++)
					listener.OnNewPin(this.pins.get(i));
			}
		}
	}
	
	
	// -----------------------------------------------------
	// Tarefa assíncrona de validar usuário
	// -----------------------------------------------------
	public class LongValidateUser extends AsyncTask<Void, Void, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			return UnilinkDB.getDatabase().validateUserB();
		}

		@Override
		protected void onPostExecute(final Integer success) {
			if (success == 0) {
				validated = true;
			} else {
				validated = false;
			}
		}
	}
}
