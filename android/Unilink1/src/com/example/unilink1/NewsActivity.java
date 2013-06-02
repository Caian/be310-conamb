package com.example.unilink1;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

public class NewsActivity extends Activity {
	
	public static final String REF_PIN = "com.example.android.news.pin";
	private Context context = this;
	private NewsPin np;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);
		// Show the Up button in the action bar.
		setupActionBar();
		
		long uid = getIntent().getLongExtra(REF_PIN, -1);
		BasePin pin = UnilinkDB.getDatabase().getPin(uid);
		
		if (!(pin instanceof NewsPin))
			return; // Não é pra acontecer...
			
		this.np = (NewsPin) pin;
		((TextView) findViewById(R.id.textViewNews)).setText(this.np.getText());
		
		File f = new File(this.getExternalFilesDir(null), uid + ".jpg");
		if (f.exists()) {
			((ImageView) findViewById(R.id.imageViewPicture)).
			setImageBitmap(BitmapFactory.decodeFile(f.getPath()));
		 }
		
		((ImageView) findViewById(R.id.imageViewLike)).
			setOnClickListener(new OnClickListener()  {
				@Override
				public void onClick(View arg0) {
					if (!UnilinkDB.getDatabase().isValidated()) {
						Intent intent = new Intent(context, LoginActivity.class);
						startActivityForResult(intent, 0);
					}
					else {
						doLike();
					}
				}
		});
		
		((ImageView) findViewById(R.id.ImageViewDislike)).
		setOnClickListener(new OnClickListener()  {
			@Override
			public void onClick(View arg0) {
				if (!UnilinkDB.getDatabase().isValidated()) {
					Intent intent = new Intent(context, LoginActivity.class);
					startActivityForResult(intent, 0);
				}
				else {
					doDislike();
				}
			}
	});
	}
	
	public void doLike() {
		((ImageView) findViewById(R.id.imageViewLike)).
		setImageResource(R.drawable.ic_mark_upvtg);
		((ImageView) findViewById(R.id.ImageViewDislike)).
		setImageResource(R.drawable.ic_mark_dnvt);
		this.np.like();
	}
	
	public void doDislike() {
		((ImageView) findViewById(R.id.imageViewLike)).
		setImageResource(R.drawable.ic_mark_upvt);
		((ImageView) findViewById(R.id.ImageViewDislike)).
		setImageResource(R.drawable.ic_mark_dnvtr);
		this.np.dislike();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.news, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
