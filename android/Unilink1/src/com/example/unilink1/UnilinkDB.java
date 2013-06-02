package com.example.unilink1;

import java.io.BufferedReader;
import java.io.File;
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
import android.text.Editable;
import android.util.Base64;
import android.util.SparseArray;

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
	
	private HashMap<Marker, BasePin> hpins;
	private SparseArray<BasePin> vpins;
	
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
		this.client = new TCPClient();
		this.hpins = new HashMap<Marker, BasePin>();
		this.vpins = new SparseArray<BasePin>();
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
	// Comprime a posição em quadrante
	// -----------------------------------------------------
	public void compressQuadrant(double latitude, double longitude, 
			int latq[], int lonq[]) {
		latitude += 90;
		longitude += 180;
		
		latq[0] = (int)latitude;
		latq[1] = (int)(60.0 * (latitude - latq[0]));
		
		lonq[0] = (int)longitude;
		lonq[1] = (int)(60.0 * (longitude - lonq[0]));
	}
	
	
	// -----------------------------------------------------
	// Atualiza os pinos próximos a uma latitude/longitude
	// -----------------------------------------------------
	public void updateNear(int latq[], int lonq[]) {
		String lats = String.format("%03d", latq[0]) + 
				String.format("%02d", latq[1]);
		String lons = String.format("%03d", lonq[0]) + 
				String.format("%02d", lonq[1]);
		
		// Up we go...
		LongUpdateNearPins updater = new LongUpdateNearPins();
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
		return this.hpins.get(marker);
	}
	

	// -----------------------------------------------------
	// Busca um pino a partir de seu uid
	// -----------------------------------------------------
	public BasePin getPin(long uid) {
		return this.vpins.get((int)uid);
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
	// Curte uma noticia
	// -----------------------------------------------------
	public void like(long uid) {
		LongVote v = new LongVote();
		v.execute(1L, uid);
	}


	// -----------------------------------------------------
	// Descurte uma noticia
	// -----------------------------------------------------
	public void dislike(long uid) {
		LongVote v = new LongVote();
		v.execute(-1L, uid);
	}
	
	
	// -----------------------------------------------------
	// Compartilha uma noticia
	// -----------------------------------------------------
	public void share(Editable name, Editable text, double latitude, 
			double longitude, String sharePicture) {
		LongShare s = new LongShare();
		s.execute(name.toString(), text.toString(), sharePicture, 
		((Double)latitude).toString(), ((Double)longitude).toString());
	}
	
	
	// -----------------------------------------------------
	// Compartilha uma noticia
	// -----------------------------------------------------
	public void mark(int type, int icon, double latitude, 
			double longitude) {
		LongMark m = new LongMark();
		m.execute(((Integer)type).toString(), ((Integer)icon).toString(), 
		((Double)latitude).toString(), ((Double)longitude).toString());
	}
	

	// -----------------------------------------------------
	// Atualiza um pino
	// -----------------------------------------------------	
	public void updatePin(BasePin p) {
		Marker old = p.getMarker();
		Marker m = MainActivity.getPinListener().OnUpdatePin(p);
		p.setMarker(m);
		hpins.remove(old);
		hpins.put(m, p);
		vpins.put((int)p.getUid(), p);
	}
	
	
	// -----------------------------------------------------
	// Tarefa assíncrona de baixar novos pinos
	// -----------------------------------------------------
	public class LongUpdateNearPins extends AsyncTask<String, Integer, Boolean> {
		
		// Variáveis -------------------------------------------
		
		private Vector<BasePin> pins = new Vector<BasePin>();
		
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

		@Override
		protected void onPostExecute(Boolean result) {
			
			if (!result)
				return;
			
			PinLocalStorage.getStorage().downloadImages(this.pins);
			
			PinListener listener = MainActivity.getPinListener();
			
			if (listener != null) {
				for (int i = 0; i < this.pins.size(); i++) {
					BasePin p = this.pins.get(i);
					Marker m = listener.OnNewPin(p);
					p.setMarker(m);
					hpins.put(m, p);
					vpins.put((int)p.getUid(), p);
				}
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
	
	
	// -----------------------------------------------------
	// Tarefa assíncrona de validar usuário
	// -----------------------------------------------------
	public class LongVote extends AsyncTask<Long, Void, Boolean> {
	
		private NewsPin old;
	
		@Override
		protected Boolean doInBackground(Long... params) {
			synchronized (client) {
				
				if (!client.open())
					return false;
				
				String s;
				
				if (params[0] == 1) {
					s = "UPVT";
				}else if (params[0] == -1) {
					s = "DNVT";
				}else {
					return false;
				}
				
				 s = s + encodeString(username) + 
					" " + encodeString(password) + " " + params[1];
				
				// Monta a mensagem
				client.sendMessage(s);

				// Não me importo com o resultado por enquanto
				String response = client.readLine();
				if (response == null)
					return false;
				
				NewsPin pin = (NewsPin) parseGetResponse(response);
				if (pin == null)
					return false;

				this.old = (NewsPin) getPin(params[1]);
				this.old.setUpVotes(pin.getUpVotes());
				this.old.setDownVotes(pin.getDownVotes());
				
				client.close();
				return true;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			updatePin(this.old);
		}
	}


	// -----------------------------------------------------
	// Tarefa assíncrona compartilhar noticia
	// -----------------------------------------------------
	public class LongShare extends AsyncTask<String, Void, Boolean> {

		private NewsPin pin;
		private String sharep;

		@Override
		protected Boolean doInBackground(String... params) {
			synchronized (client) {
				
				if (!client.open())
					return false;
				
				this.sharep = params[2];
				File f = new File(this.sharep);
				
				String s = "POSN" + 
					encodeString(username) + " " + 
					encodeString(password) + " " + 
					encodeString(params[0]) + " " + 
					encodeString(params[1]) + " " + 
					params[3] + " " + params[4] + " " + f.length();
					
				client.sendMessage(s);
					
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				
				FileInputStream fs;
				try {
					fs = new FileInputStream(this.sharep);
				
					byte data[] = new byte[1024];
		            int count;
		            while ((count = fs.read(data)) != -1) {
		                client.sendData(data, count);
		            }
				} catch (FileNotFoundException e) {
					
				} catch (IOException e) {

				}
				
				String response = client.readLine();
				if (response == null)
					return false;
				
				this.pin = (NewsPin) parseGetResponse(response);
				if (this.pin == null)
					return false;

				client.close();
				return true;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			
			if (this.pin != null) {
				PinListener listener = MainActivity.getPinListener();
				
				if (listener != null) {
					BasePin p = this.pin;
					Marker m = listener.OnNewPin(p);
					p.setMarker(m);
					hpins.put(m, p);
					vpins.put((int)p.getUid(), p);
				}
				
				File f = new File(this.sharep);
				File nf = new File(MainActivity.getContext().
					getExternalFilesDir(null), this.pin.getUid() + ".jpg");
				f.renameTo(nf);
			}
		}
	}
	
	
	// -----------------------------------------------------
	// Tarefa assíncrona compartilhar noticia
	// -----------------------------------------------------
	public class LongMark extends AsyncTask<String, Void, Boolean> {

		private MarkerPin pin;

		@Override
		protected Boolean doInBackground(String... params) {
			synchronized (client) {
				
				if (!client.open())
					return false;
				
				String s = "POSM" + 
					encodeString(username) + " " + 
					encodeString(password) + " " + 
					params[0] + " " + params[1] + " " + 
					params[2] + " " + params[3];
					
				client.sendMessage(s);
				
				String response = client.readLine();
				if (response == null)
					return false;
				
				this.pin = (MarkerPin) parseGetResponse(response);
				if (this.pin == null)
					return false;

				client.close();
				return true;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			
			if (this.pin != null) {
				PinListener listener = MainActivity.getPinListener();
				
				if (listener != null) {
					BasePin p = this.pin;
					Marker m = listener.OnNewPin(p);
					p.setMarker(m);
					hpins.put(m, p);
					vpins.put((int)p.getUid(), p);
				}
			}
		}
	}
}
