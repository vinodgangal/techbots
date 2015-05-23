package com.androidexample.uploadtoserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class SignInActivity extends Activity {

	EditText etU, etP;
	String username, password;
	ImageButton btnSign, btnCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_sign_in);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		etU = (EditText) findViewById(R.id.userName);
		etP = (EditText) findViewById(R.id.password);
		btnSign = (ImageButton) findViewById(R.id.login);
		btnCancel = (ImageButton) findViewById(R.id.cancel);

		btnSign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				signIn(username, password);

			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

	private void signIn(String username, String password) {
		HttpURLConnection con = null;
		try {

			username = etU.getText().toString();
			password = etP.getText().toString();

			String req = "http://localhost:8100/backup/signIn.php?username="
					+ username + "&password=" + password;
			Log.d("url", req);

			URL url = new URL(req);

			con = (HttpURLConnection) url.openConnection();
			String res = readStream(con.getInputStream());

			if (res.equals("success")) {

				
				Intent intent = new Intent(SignInActivity.this,
						UploadToServer.class);
				intent.putExtra("username", username);
				startActivity(intent);

			} else {
				Toast.makeText(getApplicationContext(),
						"Incorrect UserName/Password! Please Try Again!",
						Toast.LENGTH_LONG).show();

			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("exception", "0");
		}

	}

	private String readStream(InputStream in) {
		String line = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			line = reader.readLine();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		Log.d("msg", line);
		return line;

	}

}
