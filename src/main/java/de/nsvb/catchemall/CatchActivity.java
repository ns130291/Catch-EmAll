package de.nsvb.catchemall;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

public class CatchActivity extends Activity {

	private static final int WRITE_REQUEST_CODE = 42;

	private Intent file;
	private boolean txt = false;
	//private static final String fileIntent = "fileIntent";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*if (savedInstanceState != null) {
			file = (Intent) savedInstanceState.getParcelable(fileIntent);
		} else {*/
			file = getIntent();
		//}

		if (file != null) {
			// TODO: mimeType evtl. überprüfen/einschränken
			String type = file.getType();
			// Toast.makeText(this, "Mime Type " + type,
			// Toast.LENGTH_LONG).show();
			Log.d("", "Type " + type);
			Uri uri = (Uri) file.getExtras().get(Intent.EXTRA_STREAM);
			if (uri != null) {
				String data = uri.getLastPathSegment();
				// Toast.makeText(this, "Data " + data,
				// Toast.LENGTH_LONG).show();
				createFile(type, data);
			} else {

				if (type.equals("text/plain") && file.getExtras().get(Intent.EXTRA_TEXT) != null) {
					txt = true;
					String name = (String) file.getExtras().get(Intent.EXTRA_SUBJECT);
					if(name != null){
						createFile(type, name + ".txt");
					}else{
						createFile(type, "");
					}
				} else {
					Log.d("", "Kein Bekannter Intent-Typ");
					Log.d("", file.toString());

					Set<String> keys = file.getExtras().keySet();
					for (String key : keys) {
						Log.d("", key + " = "
								+ file.getExtras().get(key).toString());
					}

					saveFailed();
					finish();
				}
			}
		} else {
			Log.d("", "Intent null");
			saveFailed();
			finish();
		}

	}

	private void createFile(String mimeType, String fileName) {
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

		// Filter to only show results that can be "opened", such as
		// a file (as opposed to a list of contacts or timezones).
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		// Create a file with the requested MIME type.
		intent.setType(mimeType);
		intent.putExtra(Intent.EXTRA_TITLE, fileName);
		startActivityForResult(intent, WRITE_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent resultData) {
		if (requestCode == WRITE_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				if (resultData != null) {
					Uri out = resultData.getData();

					Log.d("", out.toString());
					if(txt){
						Log.d("", "saving TXT");
						
						save((String) file.getExtras().get(Intent.EXTRA_TEXT), out);
					}else{
						Log.d("", ((Uri) file.getExtras().get(Intent.EXTRA_STREAM))
								.toString());
						
						save((Uri) file.getExtras().get(Intent.EXTRA_STREAM), out);
					}

				} else {
					saveFailed();
				}
			} else {
				saveFailed();
			}
		}
		finish();
	}

	private void save(String text, Uri out) {
		// TODO: Async Task
		try {

			ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(
					out, "w");
			FileOutputStream fileOutputStream = new FileOutputStream(
					pfd.getFileDescriptor());

			
			fileOutputStream.write(text.getBytes());

			// Let the document provider know you're done by closing the stream.
			fileOutputStream.close();
			pfd.close();

		} catch (Exception e) {
			e.printStackTrace();
			saveFailed();
		}
	}
	
	private void save(Uri in, Uri out) {
		// TODO: Async Task
		try {
			InputStream inputStream = getContentResolver().openInputStream(in);

			ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(
					out, "w");
			FileOutputStream fileOutputStream = new FileOutputStream(
					pfd.getFileDescriptor());

			byte buffer[] = new byte[1024];
			int length = 0;

			while ((length = inputStream.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, length);
			}

			// Let the document provider know you're done by closing the stream.
			fileOutputStream.close();
			inputStream.close();
			pfd.close();

		} catch (Exception e) {
			e.printStackTrace();
			saveFailed();
		}
	}

	private void saveFailed() {
		Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
	}
}
