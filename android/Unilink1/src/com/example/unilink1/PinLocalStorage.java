package com.example.unilink1;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class PinLocalStorage {
	
	// Singleton -------------------------------------------
	
	private static PinLocalStorage singleton = null;
	public static PinLocalStorage getStorage() {
		if (singleton == null) {
			singleton = new PinLocalStorage();
		}
		return singleton;
	}
	
	// Variáveis -------------------------------------------
	
	private List<MarkerPin> markers;
	private List<NewsPin> news;
	//private SparseIntArray icons;
	private Context context;
	
	
	// -----------------------------------------------------
	// Construtor
	// -----------------------------------------------------
	private PinLocalStorage() {
		this.markers = new ArrayList<MarkerPin>();
		this.news = new ArrayList<NewsPin>();
		//this.icons = new SparseIntArray();
	}
	
	// -----------------------------------------------------
	// Seta o contexto
	// -----------------------------------------------------
	public void setContext(Context c) {
		this.context = c;
	}
	
	// -----------------------------------------------------
	// Remove da lista os pinos não desatualizados
	// -----------------------------------------------------
	public void removeOld(List<BasePin> pins) {
		for (int j = 0; j < this.markers.size(); j++) {
			BasePin pin = this.markers.get(j);
			for (int i = pins.size()-1; i >= 0; i--) {
				if (pins.get(i).equalsTo(pin) && pins.get(i).getDate() > pin.getDate())
					pins.remove(i);
			}
		}
		for (int j = 0; j < this.news.size(); j++) {
			BasePin pin = this.news.get(j);
			for (int i = pins.size()-1; i >= 0; i--) {
				if (pins.get(i).equalsTo(pin) && pins.get(i).getDate() > pin.getDate())
					pins.remove(i);
			}
		}
	}
	
	// -----------------------------------------------------
	// Retorna um ícone para um pino
	// -----------------------------------------------------
	public BitmapDescriptor getResourceIcon(long pinIcon) {
		int r = R.drawable.ic_unknown;
		
		switch ((int)pinIcon) {
			case 1: r = R.drawable.ic_trash; break;
			case 2: r = R.drawable.ic_recl; break;
			case 3: r = R.drawable.ic_recl_batt; break;
			case 4: r = R.drawable.ic_recl_light; break;
		}
		
		return BitmapDescriptorFactory.fromResource(r);
	}
	
	// -----------------------------------------------------
	// Carrega os pinos do cache local
	// -----------------------------------------------------
	public void loadFromLocalStorage() {
		
	}

	
	// -----------------------------------------------------
	// Baixa as imagens para o cache local
	// -----------------------------------------------------
	public void downloadImages(Vector<BasePin> pins) {
		Vector<Long> uids = new Vector<Long>();
		Boolean b;
		for (int i = 0; i < pins.size(); i++) {
			BasePin p = pins.get(i);
			long uid = p.getUid();
			
			if (pins.get(i).getType() != BasePin.CATEGORY_NEWS) {
				continue;
			}
			
			File f = new File(context.getExternalFilesDir(null), uid + ".jpg");
			if (f.exists()) {
				continue;
			}
			
			uids.add(p.getUid());
		}
		
		if (uids.size() > 0) {
			LongDownloadNewsImages d = new LongDownloadNewsImages();
			d.execute(uids);
		}
	}
	
	
	// -----------------------------------------------------
	// Tarefa assíncrona de download
	// -----------------------------------------------------
	public class LongDownloadNewsImages extends AsyncTask<Vector<Long>, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Vector<Long>... params) {
			for (int i = 0; i < params[0].size(); i++) {
				try {
					Long uid = params[0].get(i);
		            URL url = new URL(UnilinkDB.serverimg + uid + ".jpg");
		            URLConnection connection = url.openConnection();
		            connection.connect();

		            File f = new File(context.getExternalFilesDir(null), uid + ".jpg");
		            
		            InputStream input = new BufferedInputStream(url.openStream());
		            OutputStream output = new FileOutputStream(f);
	
		            byte data[] = new byte[1024];
		            int count;
		            while ((count = input.read(data)) != -1) {
		                output.write(data, 0, count);
		            }
	
		            output.flush();
		            output.close();
		            input.close();
		        } catch (Exception e) {
		        }
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
		}
	}
}
