package com.example.unilink1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.maps.model.LatLng;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;

public class ShareActivity extends Activity {
	
	// Variáveis -------------------------------------------
	
	public final int PICTURE_WIDTH = 800;
	public final int PICTURE_HEIGHT = 600;
	public final float PICTURE_ASPECT = 800.0f/600.0f;
	
	private final int CAMERA_CODE = 1;
	//private final int LOGIN_CODE = 2;
	
	private Bitmap sharePicture = null;
	
	// Métodos -------------------------------------------

	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		setupActionBar();
		
		((EditText)findViewById(R.id.editTextNews)).
			addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					((TextView) findViewById(R.id.textViewChars)).
						setText((140-arg0.length())+"/140");
				}
				
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
						int arg3) {
				}
				
				@Override
				public void afterTextChanged(Editable arg0) {
				}
			});
		
		((Button)findViewById(R.id.buttonCapture)).
			setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				takePicture();
			}
		});
		
		((Button)findViewById(R.id.buttonShare)).
			setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doShare();
			}
		});
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------	
	public void doShare() {
		File f = new File(getExternalFilesDir(null), "share.jpg");
		if (!f.exists()) f = new File(getExternalFilesDir(null), 
				MainActivity.DUMMYFILE);
		
		LatLng l = MainActivity.getLng();
		
		UnilinkDB.getDatabase().share(
				((EditText)findViewById(R.id.editTextName)).getText().toString(),
				((EditText)findViewById(R.id.editTextNews)).getText().toString(),
				l.latitude, l.longitude, f.getPath());
		
		finish();
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	public void takePicture() {
		File f = new File(getExternalFilesDir(null), "share.tmp");
		if (f.exists()) f.delete();
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
	    startActivityForResult(takePictureIntent, CAMERA_CODE);
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------	
	private void processImage(Intent data) {
		new LongProcessImage().execute();
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.share, menu);
		return true;
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CAMERA_CODE:
			if (resultCode == RESULT_OK)
				processImage(data);
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	// -----------------------------------------------------
	// Tarefa assíncrona compartilhar noticia
	// -----------------------------------------------------
	public class LongProcessImage extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			File f = new File(getExternalFilesDir(null), "share.tmp");
			if (!f.exists())
				return false; // Erro...
			
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
		    BitmapFactory.decodeFile(f.getPath(), bmOptions);
		    
		    int w, h;
		    float a = (float)bmOptions.outWidth / (float)bmOptions.outHeight;
		    if (a < PICTURE_ASPECT) {
		    	w = PICTURE_WIDTH;
		    	h = (int)(w / PICTURE_ASPECT);
		    } else {
		    	h = PICTURE_HEIGHT;
		    	w = (int)(h * PICTURE_ASPECT);
		    }
		    
		    int scale=1;
	        while(bmOptions.outWidth/scale/2>=w && bmOptions.outHeight/scale/2>=h)
	            scale*=2;
	        
	        bmOptions = new BitmapFactory.Options();
			bmOptions.inSampleSize = scale;
	        Bitmap photo = BitmapFactory.decodeFile(f.getPath(), bmOptions);

		    try {
			    sharePicture = Bitmap.createScaledBitmap(photo, 
			    		PICTURE_WIDTH, PICTURE_HEIGHT, true);
		    }
		    catch (RuntimeException e) {
		    	e.printStackTrace();
		    }
		    
		    try {
		    	f = new File(getExternalFilesDir(null), "share.jpg");
				FileOutputStream out = new FileOutputStream(f.getPath());
				sharePicture.compress(Bitmap.CompressFormat.JPEG, 50, out);
			} catch (Exception e) {
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			if (success) {
				((ImageView) findViewById(R.id.imageViewPicture)).
				setImageBitmap(sharePicture);
			}
			else {
				((ImageView) findViewById(R.id.imageViewPicture)).
				setImageBitmap(null);
			}
		}
	}
}
