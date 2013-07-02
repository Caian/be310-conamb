package com.example.unilink1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
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
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements LocationListener, OnInfoWindowClickListener, OnCameraChangeListener, PinListener {

	// Variáveis -------------------------------------------
	
	public final static String LAST_LOCATION = "location.info";
	public final static String DUMMYFILE = "dummy";
	
	//private Menu menu;
	private GoogleMap map;
	private LocationManager locationManager;
	private static LatLng location;
	private static LatLng locfrom;
	private static LatLng locto;
	private static PinListener pinListener = null;
	private static Context context = null;
	private Boolean autoMove = true;
	private Handler updateDispatcher;
	private Runnable lastUpdate;
	
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
		
		this.updateDispatcher = new Handler();
		this.lastUpdate = null;

		this.map = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		
	    this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
	    
	    if (this.map != null) {
	    	this.map.setInfoWindowAdapter(new PinContentManager(getLayoutInflater()));
	    	this.map.setOnInfoWindowClickListener(this);
	    	this.map.setOnCameraChangeListener(this);
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
			double latf = Double.parseDouble(b.readLine());
			double lonf = Double.parseDouble(b.readLine());
			double latt = Double.parseDouble(b.readLine());
			double lont = Double.parseDouble(b.readLine());
			b.close();
			MainActivity.location = new LatLng(lat, lon);
			MainActivity.locfrom = new LatLng(latf, lonf);
			MainActivity.locto = new LatLng(latt, lont);
			changeLocation(MainActivity.location);
			moveToLocation();
		} catch (FileNotFoundException e) {
			// Nada...
		} catch (IOException e) {
			// Nada...
		} catch (Exception e) {
			// Nay
		}
	    
	    File f = new File(getExternalFilesDir(null), DUMMYFILE);
	    try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public void updateLocation() {
		VisibleRegion vr = this.map.getProjection().getVisibleRegion();
		double latf = vr.latLngBounds.southwest.latitude;
		double lonf = vr.latLngBounds.northeast.longitude;
		double latt = vr.latLngBounds.northeast.latitude;
		double lont = vr.latLngBounds.southwest.longitude;
		
		if (latf == 0.0 && lonf == 0.0 && latt == 0.0 && lont == 0.0) {
			latf = MainActivity.locfrom.latitude;
			lonf = MainActivity.locfrom.longitude;
			latt = MainActivity.locto.latitude;
			lont = MainActivity.locto.longitude;
		}
		
		if (latf > latt) {
			double t = latt;
			latt = latf;
			latf = t;
		}
		
		if (lonf > lont) {
			double t = lont;
			lont = lonf;
			lonf = t;
		}
			
		MainActivity.locfrom = new LatLng(latf, lonf);
		MainActivity.locto = new LatLng(latt, lont);
		
		UnilinkDB db = UnilinkDB.getDatabase();
		db.updateNear(
				MainActivity.locfrom.latitude, 
				MainActivity.locfrom.longitude, 
				MainActivity.locto.latitude,
				MainActivity.locto.longitude);
		
		try {
			FileOutputStream fos = openFileOutput(LAST_LOCATION, 
					Context.MODE_PRIVATE);
			PrintStream s = new PrintStream(fos);
			s.println(MainActivity.location.latitude);
			s.println(MainActivity.location.longitude);
			s.println(MainActivity.locfrom.latitude);
			s.println(MainActivity.locfrom.longitude);
			s.println(MainActivity.locto.latitude);
			s.println(MainActivity.locto.longitude);
			s.close();
		} catch (FileNotFoundException e) {
			// Nada...
		}
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	public void changeLocation(LatLng arg0) {
		if (arg0 != null) {
			
			MainActivity.location = arg0;
			//updateLocation();
			if (this.autoMove) 
				moveToLocation();
		}
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public void onCameraChange(CameraPosition arg0) {
		
		// Cancela atualização pendente
		if (this.lastUpdate != null)
			this.updateDispatcher.removeCallbacks(this.lastUpdate);
		
		this.lastUpdate = new Runnable(){

			@Override
			public void run() {
				updateLocation();
			}
		};
		
		this.updateDispatcher.postDelayed(this.lastUpdate, 5000);
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
