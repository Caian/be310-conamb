package com.example.unilink1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
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
	
	public final static String LAST_LOCATION = "location.info";
	public final static String DUMMYFILE = "dummy";
	
	//private Menu menu;
	private GoogleMap map;
	private LocationManager locationManager;
	private static LatLng location;
	private static PinListener pinListener = null;
	private static Context context = null;
	private Boolean autoMove = true;
	
	private final int LOGIN_CODE = 2;
	private final int LOGIN_REDIRECT = 2 << 8;
	private final int SHARE_CODE = 3;
	private final int SHARE_REDIRECT = 3 << 8;
	private final int SETTINGS_CODE = 4;
	private final int SETTINGS_REDIRECT = 4 << 8;
	private final int MARK_CODE = 5;
	private final int MARK_REDIRECT = 5 << 8;
	
	// Métodos ---------------------------------------------
	
	// -----------------------------------------------------
	// Construtor
	// -----------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		MainActivity.pinListener = this;
		MainActivity.context = this;

		this.map = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		
	    this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
	    
	    if (this.map != null) {
	    	this.map.setInfoWindowAdapter(new PinContentManager(getLayoutInflater()));
	    	this.map.setOnInfoWindowClickListener(this);
	    }
	    
	    PinLocalStorage.getStorage().setContext(this);
	    
	    UnilinkDB db = UnilinkDB.getDatabase();
	    db.loadUserFromStorage(this);
	    db.validateUser();
	    
	    try {
			FileInputStream fos = openFileInput(LAST_LOCATION);
			InputStreamReader s = new InputStreamReader(fos);
			BufferedReader b = new BufferedReader(s);
			double lat = Double.parseDouble(b.readLine());
			double lon = Double.parseDouble(b.readLine());
			b.close();
			MainActivity.location = new LatLng(lat, lon);
			changeLocation(MainActivity.location);
			moveToLocation();
		} catch (FileNotFoundException e) {
			// Nada...
		} catch (IOException e) {
			// Nada...
		}
	    
	    try {
			FileOutputStream fos = openFileOutput(DUMMYFILE, 
					Context.MODE_PRIVATE);
			PrintStream s = new PrintStream(fos);
			s.close();
		} catch (FileNotFoundException e) {
			// Nada...
		}
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	public static LatLng getLng() {
		return new LatLng(MainActivity.location.latitude, 
				MainActivity.location.longitude);
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	public static PinListener getPinListener() {
		return MainActivity.pinListener;
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	public static Context getContext() {
		return MainActivity.context;
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
	public void changeLocation(LatLng arg0) {
		if (arg0 != null) {
			
			MainActivity.location = arg0;
			
			VisibleRegion vr = this.map.getProjection().getVisibleRegion();
			double left = vr.latLngBounds.southwest.longitude;
			double top = vr.latLngBounds.northeast.latitude;
			double right = vr.latLngBounds.northeast.longitude;
			double bottom = vr.latLngBounds.southwest.latitude;
			
			UnilinkDB db = UnilinkDB.getDatabase();
			db.updateNear(top, left, bottom, right);
			
			try {
				FileOutputStream fos = openFileOutput(LAST_LOCATION, 
						Context.MODE_PRIVATE);
				PrintStream s = new PrintStream(fos);
				s.println(MainActivity.location.latitude);
				s.println(MainActivity.location.longitude);
				s.close();
			} catch (FileNotFoundException e) {
				// Nada...
			}
			
			if (this.autoMove) 
				moveToLocation();
		}
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public void onLocationChanged(Location arg0) {
		changeLocation(new LatLng(arg0.getLatitude(), 
				arg0.getLongitude()));
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
			LatLng p = MainActivity.location;
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
	public void doMark() {
		Intent intent = new Intent(this, MarkersActivity.class);
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
		case MARK_REDIRECT:
			doMark();
			break;
		}
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		UnilinkDB db;
	    switch (item.getItemId()) {
	    case R.id.action_share:
	    	db = UnilinkDB.getDatabase();
	    	
	    	if (!db.isValidated()) {
	    		doLogin(SHARE_REDIRECT);
	    	} else {
	    		doShare();
	    	}
	        return true;
	    case R.id.action_mark:
	    	db = UnilinkDB.getDatabase();
	    	
	    	if (!db.isValidated()) {
	    		doLogin(MARK_REDIRECT);
	    	} else {
	    		doMark();
	    	}
	        return true;
	    case R.id.action_settings:
	    	//doSettings();
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
		BasePin p = UnilinkDB.getDatabase().getPin(arg0);
		long uid = p.getUid();
		Intent intent = new Intent(this, NewsActivity.class);
		intent.putExtra(NewsActivity.REF_PIN, uid);
		startActivity(intent);
		
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
		p.getMarker().remove();
		return this.map.addMarker(new MarkerOptions()
		.position(new LatLng(p.getLat(), p.getLon()))
		.icon(p.getResourceIcon()));
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
