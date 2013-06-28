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
import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

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
	
	private final String serveraddr = "http://atum.caco.ic.unicamp.br/be310-conamb/servidor/php/server.php";
	private final String USERFILE = "user.info";
	
	private HashMap<Marker, BasePin> hpins;
	private SparseArray<BasePin> vpins;
	
	private String username = "";
	private String password = "";
	private Boolean validated = false;
	
	// Métodos ---------------------------------------------
	
	// -----------------------------------------------------
	// Construtor
	// -----------------------------------------------------
	private UnilinkDB()
	{
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
	// Atualiza os pinos próximos a uma latitude/longitude
	// -----------------------------------------------------
	public void updateNear(double latfrom, double lonfrom, double latto, double lonto) {
		
		// Up we go...
		LongUpdateNearPins updater = new LongUpdateNearPins();
		updater.execute(
				new BasicNameValuePair("latfrom", ((Double)latfrom).toString()),
				new BasicNameValuePair("lonfrom", ((Double)lonfrom).toString()),
				new BasicNameValuePair("latto", ((Double)latto).toString()),
				new BasicNameValuePair("lonto", ((Double)lonto).toString())
				);
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
		
		int code = -1;
		
		HttpClient httpclient = null;
		HttpPost httppost = null;
		HttpResponse response = null;
		HttpEntity entity = null;
		InputStreamReader stream = null;
		BufferedReader reader = null;
		
		try {
			httpclient = new DefaultHttpClient();
			httppost = new HttpPost(serveraddr);
			
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("cmd","VALU"));
			parameters.add(new BasicNameValuePair("uus", this.username));
			parameters.add(new BasicNameValuePair("passw", this.password));
			httppost.setEntity(new UrlEncodedFormEntity(parameters));
			
			response = httpclient.execute(httppost);
			entity = response.getEntity();
			stream = new InputStreamReader(entity.getContent());
			reader = new BufferedReader(stream);
			
			StringBuilder total = new StringBuilder();
			String line;
			
			while ((line = reader.readLine()) != null) {
			    total.append(line);
			}
			line = total.toString();
			
			if (line == null)
				return -1;
			if (line.startsWith("UUSID")) {
				this.validated = true;
				code = 0;
			} else if (line.compareTo("UFAIL") == 0) {
				code = -3;
			}
			
		} catch (ClientProtocolException ex) {
			// Problema com a requisição
			ex.printStackTrace();
		} catch (IOException ex) {
			// Problema com a conexão
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception ex) {
					// Não importa
				}
				reader = null;
			}
		}
		
		return code;
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
		
		s.execute(
				new BasicNameValuePair("image", sharePicture),
				new BasicNameValuePair("name", name.toString()),
				new BasicNameValuePair("text", text.toString()),
				new BasicNameValuePair("lat", ((Double)latitude).toString()),
				new BasicNameValuePair("lon", ((Double)longitude).toString())
				);
	}
	
	
	// -----------------------------------------------------
	// Compartilha uma noticia
	// -----------------------------------------------------
	public void mark(int type, int icon, double latitude, 
			double longitude) {
		LongMark m = new LongMark();
		m.execute(
				new BasicNameValuePair("type", ((Integer)type).toString()),
				new BasicNameValuePair("icon", ((Integer)icon).toString()),
				new BasicNameValuePair("lat", ((Double)latitude).toString()),
				new BasicNameValuePair("lon", ((Double)longitude).toString())
				);
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
	public class LongUpdateNearPins extends AsyncTask<NameValuePair, Integer, Boolean> {
		
		// Variáveis -------------------------------------------
		
		private Vector<BasePin> pins = new Vector<BasePin>();
		
		// Métodos ---------------------------------------------
		
		@Override
		protected Boolean doInBackground(NameValuePair... arg0) {
			try {
				HttpClient httpclient = null;
				HttpPost httppost = null;
				HttpResponse response = null;
				HttpEntity entity = null;
				InputStreamReader stream = null;
				BufferedReader reader = null;
				BasePin pin;
				
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(serveraddr);
				
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				parameters.add(new BasicNameValuePair("cmd","NEAR"));
				for (NameValuePair p : arg0) parameters.add(p);
				httppost.setEntity(new UrlEncodedFormEntity(parameters));
				
				try {
					response = httpclient.execute(httppost);
					entity = response.getEntity();
					stream = new InputStreamReader(entity.getContent());
					reader = new BufferedReader(stream);
					
					while (true) {
						String line = reader.readLine();
						if (line == null)
							break;
						if (line.compareTo("EORQ") == 0)
							break;
						pin = parseNearResponse(line);
						if (pin != null) pins.add(pin);
					}
				} catch (ClientProtocolException ex) {
					// Problema com a requisição
				} catch (IOException ex) {
					// Problema com a conexão
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (Exception ex) {
							// Não importa
						}
						reader = null;
					}
				}
				
				// Filter
				
				for (int i = 0; i < pins.size(); i++) {
					
					httpclient = new DefaultHttpClient();
					httppost = new HttpPost(serveraddr);
					String suid = String.format("%d", pins.get(i).getUid());
					parameters.clear();
					parameters.add(new BasicNameValuePair("cmd","GETD"));
					parameters.add(new BasicNameValuePair("uid",suid));
					httppost.setEntity(new UrlEncodedFormEntity(parameters));
					
					try {
						pin = null;
						response = httpclient.execute(httppost);
						entity = response.getEntity();
						stream = new InputStreamReader(entity.getContent());
						reader = new BufferedReader(stream);
						StringBuilder total = new StringBuilder();
						String line;
						
						while ((line = reader.readLine()) != null) {
						    total.append(line);
						}
						line = total.toString();
						
						if (line != null)
							pin = parseGetResponse(line);
						
						if (pin == null) {
							pins.remove(i--);
						}
						else {
							pins.set(i, pin);
						}
						
					} catch (ClientProtocolException ex) {
						// Problema com a requisição
						// Remove pino incompleto
						pins.remove(i--);
					} catch (IOException ex) {
						// Problema com a conexão
						// Remove pino incompleto
						pins.remove(i--);
					} finally {
						if (reader != null) {
							try {
								reader.close();
							} catch (Exception ex) {
								// Não importa
							}
							reader = null;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return true;
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
			
			String cmd;
			HttpClient httpclient = null;
			HttpPost httppost = null;
			HttpResponse response = null;
			HttpEntity entity = null;
			InputStreamReader stream = null;
			BufferedReader reader = null;
			
			if (params[0] == 1) {
				cmd = "UPVT";
			}else if (params[0] == -1) {
				cmd = "DNVT";
			}else {
				return false;
			}
			
			try {
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(serveraddr);
				
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				parameters.add(new BasicNameValuePair("cmd",cmd));
				parameters.add(new BasicNameValuePair("uus", username));
				parameters.add(new BasicNameValuePair("passw", password));
				
				response = httpclient.execute(httppost);
				entity = response.getEntity();
				stream = new InputStreamReader(entity.getContent());
				reader = new BufferedReader(stream);
				StringBuilder total = new StringBuilder();
				String line;
				
				while ((line = reader.readLine()) != null) {
				    total.append(line);
				}
				line = total.toString();

				if (line == null)
					return false;
				
				NewsPin pin = (NewsPin) parseGetResponse(line);
				if (pin == null)
					return false;

				this.old = (NewsPin) getPin(params[1]);
				this.old.setUpVotes(pin.getUpVotes());
				this.old.setDownVotes(pin.getDownVotes());
				
			} catch (ClientProtocolException ex) {
				// Problema com a requisição
				ex.printStackTrace();
			} catch (IOException ex) {
				// Problema com a conexão
				ex.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Exception ex) {
						// Não importa
					}
					reader = null;
				}
			}
			
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			updatePin(this.old);
		}
	}


	// -----------------------------------------------------
	// Tarefa assíncrona compartilhar noticia
	// -----------------------------------------------------
	public class LongShare extends AsyncTask<NameValuePair, Void, Boolean> {

		private NewsPin pin;
		private String sharep;

		@Override
		protected Boolean doInBackground(NameValuePair... params) {
			
			HttpClient httpclient = null;
			HttpPost httppost = null;
			HttpResponse response = null;
			MultipartEntity entity = null;
			InputStreamReader stream = null;
			BufferedReader reader = null;
			
			try {
				
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(serveraddr);
				
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				parameters.add(new BasicNameValuePair("cmd","POSN"));
				parameters.add(new BasicNameValuePair("uus", username));
				parameters.add(new BasicNameValuePair("passw", password));
				for (int i = 1; i < params.length; i++) parameters.add(params[i]);
				
			    entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			    File file = new File(params[0].getValue());
			    ContentBody encFile = new FileBody(file,"image/jpg");
			    entity.addPart("images", encFile);
			    
			    for(NameValuePair p: parameters) {
			    	entity.addPart(p.getName(), new StringBody(p.getValue()));
			    }
			    
			    httppost.setEntity(entity);

			    ResponseHandler<String> responsehandler = new BasicResponseHandler();
			    String line = httpclient.execute(httppost, responsehandler);
				
				if (line == null)
					return false;
				
				this.pin = (NewsPin) parseGetResponse(line);
				if (this.pin == null)
					return false;
				
				return true;
				
			} catch (ClientProtocolException ex) {
				// Problema com a requisição
			} catch (IOException ex) {
				// Problema com a conexão
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Exception ex) {
						// Não importa
					}
					reader = null;
				}
			}
			
			return false;
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
	public class LongMark extends AsyncTask<NameValuePair, Void, Boolean> {

		private MarkerPin pin;

		@Override
		protected Boolean doInBackground(NameValuePair... params) {
			
			HttpClient httpclient = null;
			HttpPost httppost = null;
			HttpResponse response = null;
			HttpEntity entity = null;
			InputStreamReader stream = null;
			BufferedReader reader = null;
			
			try {
				
				httpclient = new DefaultHttpClient();
				httppost = new HttpPost(serveraddr);
				
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				parameters.add(new BasicNameValuePair("cmd","POSM"));
				parameters.add(new BasicNameValuePair("uus", username));
				parameters.add(new BasicNameValuePair("passw", password));
				for (NameValuePair p : params) parameters.add(p);
				httppost.setEntity(new UrlEncodedFormEntity(parameters));
				
				response = httpclient.execute(httppost);
				entity = response.getEntity();
				stream = new InputStreamReader(entity.getContent());
				reader = new BufferedReader(stream);
				StringBuilder total = new StringBuilder();
				String line;
				
				while ((line = reader.readLine()) != null) {
				    total.append(line);
				}
				line = total.toString();
				
				if (line == null)
					return false;
				
				this.pin = (MarkerPin) parseGetResponse(line);
				if (this.pin == null)
					return false;

				return true;
				
			} catch (ClientProtocolException ex) {
				// Problema com a requisição
			} catch (IOException ex) {
				// Problema com a conexão
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Exception ex) {
						// Não importa
					}
					reader = null;
				}
			}
			
			return false;
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
