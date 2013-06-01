package com.example.unilink1;

import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class PinContentManager implements InfoWindowAdapter {

	// Variáveis -------------------------------------------
	
	private LayoutInflater inflater;
		
	// Métodos ---------------------------------------------
	
	// -----------------------------------------------------
	// Construtor
	// -----------------------------------------------------
	public PinContentManager(LayoutInflater inflater) {
		this.inflater = inflater;
	}
	
	
	// -----------------------------------------------------
	// Gera o balão para um marcador
	// -----------------------------------------------------
	@Override
	public View getInfoContents(Marker arg0) {
		//return UnilinkDB.getDatabase().getPin(arg0).getView(this.inflater);
		return null;
		//View v = inflater.inflate(R.layout.news_marker, null);
		//return v;
	}

	
	// -----------------------------------------------------
	// Não lembro o que isso faz
	// -----------------------------------------------------
	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
