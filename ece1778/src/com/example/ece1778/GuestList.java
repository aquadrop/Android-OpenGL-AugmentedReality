package com.example.ece1778;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.ece1778.R;

public class GuestList extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
	private File[] files;

	public GuestList(Context context, File[] files, String[] values) {
		super(context, R.layout.guestlist, values);
		this.context = context;
		this.values = values;
		this.files = files;

	}

	public GuestList(Context context, String[] values) {
		super(context, R.layout.guestlist, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.guestlist, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
		textView.setText(values[position]);

		// Change icon based on name
		String s = values[position];

		System.out.println(s);

		if (!this.files[position].isDirectory())
		{
			if (s.endsWith(".obj"))
			{
				imageView.setImageResource(R.drawable.gear);
				s = s.replace(".obj", "");
			}
			if (s.endsWith(".x3d"))
			{
				imageView.setImageResource(R.drawable.xml);
				s = s.replace(".x3d", "");
			}
		}
		else
			imageView.setImageResource(R.drawable.folder);
		textView.setText(s);

		return rowView;
	}
}
