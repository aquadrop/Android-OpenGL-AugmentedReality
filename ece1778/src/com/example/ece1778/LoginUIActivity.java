package com.example.ece1778;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.android.constants.FileSystem;
import com.android.ece1778.R;
import com.example.connection.ConnectServer;
import com.example.connection.Network;

public class LoginUIActivity extends Activity {
	String TAG = "info";
	private TextView loginView;
	private TextView passwordView;
	private TextView IPView;
	private EditText loginText;
	private EditText passwordText;
	private EditText IPText;
	private Button loginButton;
	private Button guestButton;
	private static CheckBox remember;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		// loadSQLite();
		addItemsOnAll();
	}

	private void loadSQLite() {
		DatabaseHelper myDbHelper = new DatabaseHelper(this);

		SQLiteDatabase db = myDbHelper.getReadableDatabase();
		db.execSQL("CREATE TABLE if not exists defaultuser (_id INTEGER PRIMARY KEY AUTOINCREMENT,login TEXT, password TEXT, ip TEXT);");
		SQLiteDatabase write = myDbHelper.getWritableDatabase();
		write.execSQL("insert into defaultuser (login, password, ip) values"
				+ "('super'," + "'super'," + "'142.1.210.178');");
		Cursor cursor = db.rawQuery("select * from defaultuser", null);
		cursor.moveToNext();
		Log.w(TAG, cursor.getString(1));
		myDbHelper.close();

	}

	private void replaceDefaultUser(String username, String password, String IP) {

	}

	private void addItemsOnAll() {

		loginView = (TextView) findViewById(R.id.loginView);
		passwordView = (TextView) findViewById(R.id.passwordView);
		IPView = (TextView) findViewById(R.id.IPView);

		loginText = (EditText) findViewById(R.id.loginText);
		passwordText = (EditText) findViewById(R.id.passwordText);
		IPText = (EditText) findViewById(R.id.passwordText);

		loginButton = (Button) findViewById(R.id.loginButton);
		guestButton = (Button) findViewById(R.id.guestButton);

		remember = (CheckBox) findViewById(R.id.rmbcheckBox);

		addItemsOnEditText();

		guestButtonListener();
		loginButtonListener();

	}

	private void guestButtonListener() {
		guestButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//				File file = new File(Environment.getExternalStorageDirectory()
//						.toString() + "/3D/");
				String path = FileSystem.dir;
				DatabaseHelper myDbHelper = new DatabaseHelper(
						LoginUIActivity.this);
				// SQLiteDatabase db = myDbHelper.getReadableDatabase();
				// db.execSQL("delete from images");
				// Cursor cursor = db.rawQuery("select key from images",null);

//				String[] fullfilelist = file.list();
//				List<String> filelist = new ArrayList<String>();
//				for (String a : fullfilelist) {
//					Log.w(TAG, a);
//					if (!a.endsWith(".mtl"))
//						filelist.add(a);
//				}
//				Log.w(TAG, "Finished");
				Intent i = new Intent(LoginUIActivity.this,
						GuestListActivity.class);
				//i.putExtra("filenamelist", filelist.toArray(new String[0]));
				i.putExtra("path", path);
				startActivity(i);

			}
		});
	}

	private void addItemsOnEditText() {
		loginText = (EditText) findViewById(R.id.loginText);
		passwordText = (EditText) findViewById(R.id.passwordText);
		IPText = (EditText) findViewById(R.id.IPText);

		DatabaseHelper myDbHelper = new DatabaseHelper(LoginUIActivity.this);

		SQLiteDatabase db = myDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from defaultuser", null);
		cursor.moveToNext();

		Log.w(TAG,
				cursor.getString(0) + cursor.getString(1) + cursor.getString(2)
						+ cursor.getString(3));

		loginText.setText(cursor.getString(1));
		passwordText.setText(cursor.getString(2));
		IPText.setText(cursor.getString(3));
		myDbHelper.close();
	}

	private void loginButtonListener() {
		this.loginButton.setOnClickListener(new View.OnClickListener() {

			private String username = null;
			private String password = null;
			private String ip = null;

			@Override
			public void onClick(View v) {
				retriveInputs();
				ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				postParameters
						.add(new BasicNameValuePair("username", username));
				postParameters
						.add(new BasicNameValuePair("password", password));

				new ConnectServer(LoginUIActivity.this, ip, username ,postParameters)
						.execute();

			}

			private void retriveInputs() {
				ip = IPText.getText().toString();
				username = loginText.getText().toString();
				password = passwordText.getText().toString();
//				url = Network.head + ip + ":" + Network.port + Network.link;

				if (LoginUIActivity.remember.isChecked()) {
					Log.w(TAG, "update");
					DatabaseHelper myDbHelper = new DatabaseHelper(
							LoginUIActivity.this);
					SQLiteDatabase write = myDbHelper.getWritableDatabase();

					write.execSQL("Update defaultuser " + "set login =" + "'"
							+ username + "'," + "password =" + "'"
							+ password + "'," + "ip =" + "'" + ip + "'"
							+ "where _id = '1';");
					myDbHelper.close();

				}
				// username = "super";
				// password = "super";
				// url = "http://142.1.210.178:8080/ref/android/Login";
			}

		});

	}
}
