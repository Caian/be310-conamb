package com.example.unilink1;

import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements LocationListener, OnInfoWindowClickListener, PinListener {

	// Variáveis -------------------------------------------
	
	//private Menu menu;
	private GoogleMap map;
	private LocationManager locationManager;
	private Location location;
	private Boolean autoMove = true;
	
	// Métodos ---------------------------------------------
	
	// -----------------------------------------------------
	// Construtor
	// -----------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.map = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		
	    this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50000, 10, this);
	    
	    //Criteria criteria = new Criteria();
	    //String provider = locationManager.getBestProvider(criteria, false);
	    //this.location = locationManager.getLastKnownLocation(provider);
    
	    if (this.map != null) {
	    	
	    	//map.setOnMarkerClickListener(MarkerContentManager.getManager());
	    	this.map.setInfoWindowAdapter(new PinContentManager(getLayoutInflater()));
	    	this.map.setOnInfoWindowClickListener(this);
	    	
	    	moveToLocation();
	    }
	    
	    UnilinkDB.getDatabase().updateNear(
	    		-23.19653095677495, -46.88130693510175, this);
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public void onLocationChanged(Location arg0) {
		this.location = arg0;
		if (this.autoMove) moveToLocation();
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	public void moveToLocation() {
		if (location != null) {
			LatLng p = new LatLng(this.location.getLatitude(), this.location.getLongitude());
			this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 15));
		}
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	public void takePicture() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    startActivityForResult(takePictureIntent, 0);
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.action_share:
	    	takePicture();
	        return true;
	    case R.id.action_settings:
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public void onInfoWindowClick(Marker arg0) {
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public Marker OnNewPin(BasePin p) {
		return this.map.addMarker(new MarkerOptions()
		.position(new LatLng(p.getLat(), p.getLon()))
		.icon(p.getResourceIcon()));
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public Marker OnUpdatePin(BasePin p) {
		return null;
	}
}
