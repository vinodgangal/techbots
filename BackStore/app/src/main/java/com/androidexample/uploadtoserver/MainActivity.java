package com.androidexample.uploadtoserver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				Intent mainIntent = new Intent(MainActivity.this,
						SignInActivity.class);
				startActivity(mainIntent);
				finish();

			}

		}, 4000);

	}

}
