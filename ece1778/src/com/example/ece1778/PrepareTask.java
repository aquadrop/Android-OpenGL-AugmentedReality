package com.example.ece1778;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import com.android.ece1778.R;

public class PrepareTask extends AsyncTask<Void, Void, Boolean>
{
	String TAG = "info";
	private Dialog mSplashDialog;
	Context context = null;
	public PrepareTask(Context context)
	{
		this.context = context;
		mSplashDialog = new Dialog(context, android.R.style.Theme_Light);
		mSplashDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSplashDialog.setContentView(R.layout.splashscreen);
		//mSplashDialog.setCancelable(false);
		
		
	}
	

	@Override
	protected void onPreExecute()
	{
		//mSplashDialog.show();
	}
	@Override
	protected Boolean doInBackground(Void... NULL) 
	{
		Boolean connected = false;
		connected = checkInternet();
		if (connected)
			Log.w(TAG, "network connected");
		else
			Log.w(TAG, "netword failure");
		try
		{
			Thread.sleep(1000);
		}
		catch(Exception e){}
		
		return connected;
		
		
	}
	
	@Override
	protected void onPostExecute(Boolean success)
	{
		if (mSplashDialog != null)
		{
			mSplashDialog.dismiss();
			mSplashDialog = null;
		}
		
	}
	
	protected boolean checkInternet() 
	{
	    ConnectivityManager connect = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    android.net.NetworkInfo wifi = connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    //android.net.NetworkInfo mobile = connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    

	    // Here if condition check for wifi and mobile network is available or not.
	    // If anyone of them is available or connected then it will return true, otherwise false;

//	    if (wifi.isConnected()) {
//	        return true;
	    if (wifi.isConnected()) {
	        return true;
	    }
	    return false;
	}
	
}
