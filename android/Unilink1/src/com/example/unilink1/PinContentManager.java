package com.example.unilink1;

import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class PinContentManager implements InfoWindowAdapter {

	// Vari�veis -------------------------------------------
	
	private LayoutInflater inflater;
		
	// M�todos ---------------------------------------------
	
	// -----------------------------------------------------
	// Construtor
	// -----------------------------------------------------
	public PinContentManager(LayoutInflater inflater) {
		this.inflater = inflater;
	}
	
	
	// -----------------------------------------------------
	// Gera o bal�o para um marcador
	// -----------------------------------------------------
	@Override
	public View getInfoContents(Marker arg0) {
		return UnilinkDB.getDatabase().getPin(arg0).getView(this.inflater);
	}

	
	// -----------------------------------------------------
	// N�o lembro o que isso faz
	// -----------------------------------------------------
	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}
}
