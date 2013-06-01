package com.example.unilink1;

import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
	
	private final int LOGIN_CODE = 2;
	private final int LOGIN_REDIRECT = 2 << 8;
	private final int SHARE_CODE = 3;
	private final int SHARE_REDIRECT = 3 << 8;
	private final int SETTINGS_CODE = 4;
	private final int SETTINGS_REDIRECT = 4 << 8;
	
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
	    
	    UnilinkDB db = UnilinkDB.getDatabase();
	    db.updateNear(-23.19653095677495, -46.88130693510175, this);
	    db.loadUserFromStorage(this);
	    db.validateUser();
	   
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
	public void doLogin(int redirect) {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivityForResult(intent, LOGIN_CODE | redirect);
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	public void doSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	public void doShare() {
		Intent intent = new Intent(this, ShareActivity.class);
		startActivity(intent);
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	public void doRedirect(int redirectCode, int newRedirect) {
		switch (redirectCode & (0xFF << 8)) {
		case LOGIN_REDIRECT:
			doLogin(newRedirect);
			break;
		case SHARE_REDIRECT:
			doShare();
			break;
		case SETTINGS_REDIRECT:
			doSettings();
			break;
		}
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.action_share:
	    	UnilinkDB db = UnilinkDB.getDatabase();
	    	
	    	if (!db.isValidated()) {
	    		doLogin(SHARE_REDIRECT);
	    	} else {
	    		doShare();
	    	}
	        return true;
	    case R.id.action_settings:
	    	doSettings();
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


	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode & 0xFF) {
		case LOGIN_CODE:
			if (UnilinkDB.getDatabase().isValidated()) {
				UnilinkDB.getDatabase().saveUserToStorage(this);
				doRedirect(requestCode, 0);
			}
			
			/*{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.dialog_flogin_message1)
					.setTitle(R.string.dialog_flogin_title);
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			           }
			       });
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			else {
				
			}*/
			
			break;
		case SHARE_CODE:
			break;
		case SETTINGS_CODE:
			break;
		}
	}
}
