package com.example.unilink1;

import com.google.android.gms.internal.af.c;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;

public class MainActivity extends FragmentActivity implements LocationListener, OnInfoWindowClickListener {

	static final LatLng HAMBURG = new LatLng(53.558, 9.927);
	static final LatLng KIEL = new LatLng(53.551, 9.993);
	private Menu menu;
	private GoogleMap map;
	private LocationManager locationManager;
	private Location location;
	private Boolean autoMove = true;
	private View infowindow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		map = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50000, 10, this);
	    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    
	    /*Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
	    Location location = locationManager.getLastKnownLocation(provider);*/
    
	    if (map != null) {
	    	
	    	//map.setOnMarkerClickListener(MarkerContentManager.getManager());
	    	map.setInfoWindowAdapter(new PinContentManager(getLayoutInflater()));
	    	map.setOnInfoWindowClickListener(this);
	    	
	    	Marker hamburg = map.addMarker(new MarkerOptions().position(HAMBURG)
	    			.title("Hamburg"));
	    	Marker kiel = map.addMarker(new MarkerOptions().position(KIEL).
	    			title("Kiel").snippet("Kiel is cool").icon(BitmapDescriptorFactory.
	    					fromResource(R.drawable.ic_launcher)));
	    	
	    	moveToLocation();
	      
	    	/*map.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 15));
			map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);*/
	    }
	    
	    UnilinkDB.getDatabase().updateNear(-23.19653095677495, -46.88130693510175);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onLocationChanged(Location arg0) {
		location = arg0;
		if (autoMove) moveToLocation();
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	
	public void moveToLocation() {
		if (location != null) {
			LatLng p = new LatLng(location.getLatitude(), location.getLongitude());
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 15));
		}
	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		getMenuInflater().inflate(R.layout.menu, );
	}
}
