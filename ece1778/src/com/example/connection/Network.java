package com.example.connection;

import com.android.ece1778.R;

public class Network {
	static final public String head = "http://";
	static final public String port = "8080";
	static final public String link = "/ref/android/Login";
	static final public String querylink = "/ref/android/QueryDatabase";
	static final public String datalink = "/ref/data/";
	/** The time it takes for our client to timeout */
    public static final int HTTP_TIMEOUT = 30 * 1000; // milliseconds

}
