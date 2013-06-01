package com.example.unilink1;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.MediaStore;

public class ShareActivity extends Activity {
	
	// Variáveis -------------------------------------------
	
	public final int PICTURE_WIDTH = 800;
	public final int PICTURE_HEIGHT = 600;
	
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
	public void takePicture() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    startActivityForResult(takePictureIntent, 0);
	}
	
	
	// -----------------------------------------------------
	// 
	// -----------------------------------------------------	
	private void processImage(Intent data) {
	    Bitmap photo = (Bitmap)data.getExtras().get("data");
	    this.sharePicture = Bitmap.createScaledBitmap(photo, 
	    		PICTURE_WIDTH, PICTURE_HEIGHT, true);
	    
	    ((ImageView) findViewById(R.id.imageViewPicture)).
	    	setImageBitmap(this.sharePicture);
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
			if (requestCode == RESULT_OK)
				processImage(data);
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
