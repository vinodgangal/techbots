package com.androidexample.uploadtoserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStreamReader;
import org.apache.http.util.ByteArrayBuffer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

public class UploadToServer extends Activity {
	EditText et1, et2;
	TextView messageText;
	ImageButton uploadButton;
	ImageButton downloadButton;
	int serverResponseCode = 0;
	ProgressDialog dialog = null;
	String username;
	String backup_id = "";
	String upLoadServerUri = null;
    private static final String TAG = "android-drive-quickstart";
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

	// final String uploadFilePath =
	// Environment.getExternalStorageDirectory()+"/company/";
	// final String uploadFileName = "test.txt";
	String uploadFilePath = null;
    String downloadFilePath = null;
	final String uploadFileName = null;

    private static final String TAG1 = "RetrieveFileWithProgressDialogActivity";

    private static final int REQUEST_CODE_OPENER = 1;

    private ProgressBar mProgressBar;

    private DriveId mSelectedFileDriveId;





    @Override
	public void onCreate(Bundle savedInstanceState) {

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_upload_to_server);

		
		Intent in = getIntent();
		username = in.getStringExtra("username");
		
		downloadButton = (ImageButton) findViewById(R.id.restoreBtn);

		uploadButton = (ImageButton) findViewById(R.id.backupBtn);
		messageText = (TextView) findViewById(R.id.messageText);
		et1 = (EditText) findViewById(R.id.editText1);
		et2 = (EditText) findViewById(R.id.editText2);

		upLoadServerUri = "http://localhost:8100/backup/backup.php";

		//backup_id = findID();
		if (backup_id.length() == 1) {
			backup_id = "0" + backup_id;
		}
		Log.d("backup_id", backup_id);

		downloadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                BackupFromDrive();
			}
		});

		uploadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {


                saveFileToDrive();
			}
		});

	}
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
       // final Bitmap image = mBitmapToSave;
        Drive.DriveApi.newDriveContents(MainActivity.mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        // If the operation was not successful, we cannot do anything
                        // and must
                        // fail.
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        // Otherwise, we can write our data to the new contents.
                        Log.i(TAG, "New contents created.");
                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents().getOutputStream();
                        // Write the bitmap data from it.
                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        uploadFilePath = et1.getText().toString();

                        //System.out.println(file.exists() + "!!");
                        //InputStream in = resource.openStream();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try {
                            FileInputStream fis = new FileInputStream(uploadFilePath+"/one.txt");

                            byte[] buf = new byte[1024];
                            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                                bos.write(buf, 0, readNum); //no doubt here is 0
                                //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
                                System.out.println("read " + readNum + " bytes,");
                            }
                        } catch (IOException ex) {
                           // Logger.getLogger(genJpeg.class.getName()).log(Level.SEVERE, null, ex);
                        }


                        //image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                        try {
                            outputStream.write(bos.toByteArray());
                        } catch (IOException e1) {
                            Log.i(TAG, "Unable to write file contents.");
                        }

                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                               .setMimeType("image/jpeg").setTitle("one.txt").build();

                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(result.getDriveContents())
                                .build(MainActivity.mGoogleApiClient);
                        try {
                            startIntentSenderForResult(
                                    intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "Failed to launch file chooser.");
                        }
                    }
                });
    }

    private void BackupFromDrive() {

//        //Intent intent = new Intent(getBaseContext(),RetrieveContentsActivity.class);
//        //startActivity(intent);
//        new Thread() {
//            @Override
//            public void run() {
//
//                String contents = null;
//
//                DriveId id=mSelectedFileDriveId;
//
//                mSelectedFileDriveId = (DriveId) data.getParcelableExtra(
//                        OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
//
//                DriveFile file = Drive.DriveApi.getFile(MainActivity.mGoogleApiClient,);
//
//                DriveApi.DriveContentsResult driveContentsResult =
//                        file.open(MainActivity.mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).await();
//
//                if (!driveContentsResult.getStatus().isSuccess()) {
//                    //return null;
//                }
//                DriveContents driveContents = driveContentsResult.getDriveContents();
//                BufferedReader reader = new BufferedReader(
//                        new InputStreamReader(driveContents.getInputStream()));
//                StringBuilder builder = new StringBuilder();
//                String line;
//                try {
//                    while ((line = reader.readLine()) != null) {
//                        builder.append(line);
//                    }
//                    contents = builder.toString();
//                } catch (IOException e) {
//                    Log.e(TAG, "IOException while reading from the stream", e);
//                }
//
//               // driveContents.discard(getGoogleApiClient());
//
//            }
//        }.start();

        }



@Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CAPTURE_IMAGE:
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    // Store the image data as a bitmap for writing later.
                  //  mBitmapToSave = (Bitmap) data.getExtras().get("data");
                }
                break;
            case REQUEST_CODE_CREATOR:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");
                    //mBitmapToSave = null;
                    Context context = getApplicationContext();
                    CharSequence text = "Saved Successfully";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                break;
        }
    }
	public int downloadFile(String destinationFileUri) {

		SharedPreferences prefs = getSharedPreferences("fileNamePrefs",
				MODE_PRIVATE);
		String fileName = prefs.getString("fileName", null);
		if (fileName != null) {

			destinationFileUri += fileName;

			Log.d("file in download", destinationFileUri);

			File root = android.os.Environment.getExternalStorageDirectory();
			String folder = et2.getText().toString();
			File dir = new File(root + "/" + folder);

			if (dir.exists() == false) {
				dir.mkdirs();
			}

			File file = new File(dir, fileName);

			try {
				URL url = new URL(destinationFileUri);

				long startTime = System.currentTimeMillis();
				Log.d("DownloadManager", "download url:" + url);
				Log.d("DownloadManager", "download file name:" + fileName);

				URLConnection uconn = url.openConnection();

				InputStream is = uconn.getInputStream();
				BufferedInputStream bufferinstream = new BufferedInputStream(is);

				ByteArrayBuffer baf = new ByteArrayBuffer(5000);
				int current = 0;
				while ((current = bufferinstream.read()) != -1) {
					baf.append((byte) current);
				}

				FileOutputStream fos = new FileOutputStream(file);
				fos.write(baf.toByteArray());
				fos.flush();
				fos.close();
				Log.d("DownloadManager",
						"download ready in"
								+ ((System.currentTimeMillis() - startTime) / 1000)
								+ "sec");
				int dotindex = fileName.lastIndexOf('.');
				if (dotindex >= 0) {
					fileName = fileName.substring(0, dotindex);
				}

				String msg = "File Restore Completed\n\n See restored file here : \n\n"
						+ dir + "";

				messageText.setText(msg);

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return serverResponseCode;

	}

	public int uploadFile(String sourceFileUri) {

		File file = null;

		File fileList = new File(sourceFileUri);
		if (fileList != null) {
			File[] filenames = fileList.listFiles();

			file = filenames[0];
		}
		Log.d("filename123", file + "");

		String fileName = file + "";

		SharedPreferences.Editor editor = getSharedPreferences("fileNamePrefs",
				MODE_PRIVATE).edit();
		editor.putString(
				"fileName",
				fileName.substring(sourceFileUri.length() + 1,
						fileName.length()));
		Log.d("filenNAmedgnsjndo",
				fileName.substring(sourceFileUri.length() + 1,
						fileName.length()));
		editor.commit();

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(fileName);

		if (!sourceFile.isFile()) {

			dialog.dismiss();

			Log.e("uploadFile", "Source File not exist :" + uploadFilePath + ""
					+ uploadFileName);

			runOnUiThread(new Runnable() {
				public void run() {
					messageText.setText("Source File not exist :"
							+ uploadFilePath + "" + uploadFileName);
				}
			});

			return 0;

		} else {
			try {
				fileName += backup_id;

				FileInputStream fileInputStream = new FileInputStream(
						sourceFile);
				URL url = new URL(upLoadServerUri);

				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setUseCaches(false);
				conn.setRequestMethod("POST");

				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);
				Log.d("FIle Name", fileName + backup_id);

				conn.setRequestProperty("uploaded_file", fileName);

				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
						+ fileName + "\"" + lineEnd);

				dos.writeBytes(lineEnd);

				bytesAvailable = fileInputStream.available();

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {

					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				}

				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();

				Log.i("uploadFile", "HTTP Response is : "
						+ serverResponseMessage + ": " + serverResponseCode);

				if (serverResponseCode == 200) {

					runOnUiThread(new Runnable() {
						public void run() {

							String msg = "File Upload Completed.\n\nSee uploaded file here : "
									+ "localhost/backup/uploads/";

							messageText.setText(msg);
							Toast.makeText(UploadToServer.this,
									"File Upload Complete.", Toast.LENGTH_SHORT)
									.show();
						}
					});
				}

				fileInputStream.close();
				dos.flush();
				dos.close();

			} catch (MalformedURLException ex) {

				dialog.dismiss();
				ex.printStackTrace();

				runOnUiThread(new Runnable() {
					public void run() {
						messageText
								.setText("MalformedURLException Exception : check script url.");
						Toast.makeText(UploadToServer.this,
								"MalformedURLException", Toast.LENGTH_SHORT)
								.show();
					}
				});

				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
			} catch (Exception e) {

				dialog.dismiss();
				e.printStackTrace();

				runOnUiThread(new Runnable() {
					public void run() {
						messageText.setText("Got Exception : see logcat ");
						Toast.makeText(UploadToServer.this,
								"Got Exception : see logcat ",
								Toast.LENGTH_SHORT).show();
					}
				});
				Log.e("Upload file to server Exception",
						"Exception : " + e.getMessage(), e);
			}
			dialog.dismiss();
			return serverResponseCode;

		}
	}
}