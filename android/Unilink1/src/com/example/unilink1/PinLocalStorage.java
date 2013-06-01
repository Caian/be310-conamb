package com.example.unilink1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.SparseArray;
import android.util.SparseIntArray;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

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
	private SparseIntArray icons;
	
	
	// -----------------------------------------------------
	// Construtor
	// -----------------------------------------------------
	private PinLocalStorage() {
		this.markers = new ArrayList<MarkerPin>();
		this.news = new ArrayList<NewsPin>();
		this.icons = new SparseIntArray();
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
			case 2: r = R.drawable.ic_trash; break;
			case 4: r = R.drawable.ic_recl; break;
			case 7: r = R.drawable.ic_recl_batt; break;
			case 9: r = R.drawable.ic_recl_light; break;
		}
		
		return BitmapDescriptorFactory.fromResource(r);
	}
	
	// -----------------------------------------------------
	// Carrega os pinos do cache local
	// -----------------------------------------------------
	public void loadFromLocalStorage() {
		
	}
}
