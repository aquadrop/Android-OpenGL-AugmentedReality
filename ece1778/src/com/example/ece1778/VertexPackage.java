package com.example.ece1778;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import com.android.constants.FileSystem;
import com.android.ece1778.R;

import java.lang.Math;

public class VertexPackage implements Serializable {
	/**
	 * 
	 * 
	 * 
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String TAG = "info";
	protected List<String> component_name;
	
	/***********object origin, translation, rotation************/
	float[] translation = new float[3];
	float[] scale = new float[3];
	float[] rotation = new float[4];
	
	
/**	/***** Recognizable by OpenGL *****/
	protected float[] vertex_cor_tri;
	protected float[] colors;
	protected float[] highlight_colors;
	protected float[] normal_cor_tri;
	protected float[] texture_cor_tri;
	
	/***special for XML as image consisting with different components***/
	protected List<float[]> list_vertex_cor_tri;
	protected List<float[]> list_colors;
	protected List<float[]> list_highlight_colors;
	protected List<float[]> list_normal_cor_tri;
	protected List<float[]> list_texture_cor_tri;
/**	/***********/
/**	/***********/

	private boolean already_triangle_mode = false;
	private boolean xml_mode = false;
	protected float critical = 0.001f;
	private int n_tri = 0;
	private List<float[]> vertex_cor;
	private List<short[]> vertex_index;
	private List<short[]> normal_index;
	private List<short[]> texture_index;
	private List<float[]> normals;
	private List<float[]> textures;

	protected boolean normal_mode = false;
	protected boolean texture_mode = false;
	protected boolean texture_3d_mode = false;

	// material part
	protected boolean material_mode = false;
	protected String mtl_name = null;
	protected float[] ambient_color = { 0.2f, 0.2f, 0.2f, 1f }; // ka
	protected float[] diffuse_color = { 0.8f, 0.8f, 0.8f, 1f }; // kd
	protected float[] specular_color = { 1f, 1f, 1f, 1f }; // ks
	protected float[] emissive_color = {0f, 0f, 0f, 1f};
	protected float transparency = 1.0f;// d or tr
	protected float shininess = 0.0f; // s
	protected short illum = 1;// illum = 1 indicates a flat material with no
								// specular highlights, so the value of Ks is
								// not
								// used. illum = 2 denotes the presence of
								// specular
								// highlights, and so a specification for Ks is
								// required
	protected String map_Ka = null;

	public List<float[]> get_list_vertex_cor_tri()
	{
		return this.list_vertex_cor_tri;
	}
	
	public List<float[]> get_list_normal_cor_tri()
	{
		return this.list_normal_cor_tri;
	}
	
	public List<float[]> get_list_color()
	{
		return this.list_colors;
	}
	public VertexPackage(List<float[]> vertex_cor, List<short[]> vertex_index,
			List<float[]> normals, List<float[]> textures, int n_tri) {
		// Log.w(TAG, "size "+ vertex_cor.size()+" "+vertex_index.size());
		this.n_tri = n_tri;
		this.vertex_cor = vertex_cor;
		this.vertex_index = vertex_index;
		this.normals = normals;
		this.textures = textures;
		create_Tri();

	}

	public VertexPackage() {
		
		
		list_vertex_cor_tri = new ArrayList<float[]>();
		list_colors = new ArrayList<float[]>();
		list_normal_cor_tri = new ArrayList<float[]>();
		component_name = new ArrayList<String>();
		list_highlight_colors = new ArrayList<float[]>();
		
	}

	public boolean parse_Data(String filename) {
		try {
			Log.w(TAG, filename);
			if (filename.endsWith(".obj")) {
				File read = new File(FileSystem.dir, filename);
				BufferedReader br = new BufferedReader(new FileReader(read));
				vertex_index = new ArrayList<short[]>();
				normal_index = new ArrayList<short[]>();
				texture_index = new ArrayList<short[]>();

				vertex_cor = new ArrayList<float[]>();
				normals = new ArrayList<float[]>();
				textures = new ArrayList<float[]>();
				parse_Data(br);
			}
			else if (filename.endsWith(".x3d")) {
				xml_mode = true;
				InputStream in = new FileInputStream(FileSystem.dir + filename);
				xml_parse_data(in);
			} else
				return false;
			
			vertex_index.clear();
			normal_index.clear();
			texture_index.clear();

			vertex_cor.clear();
			normals.clear();
			textures.clear();
			
			
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	private static final String ns = null;

	public boolean xml_parse_data(InputStream in) throws XmlPullParserException,
			IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			readX3D(parser);
			return true;
		} finally {
			in.close();
		}

	}
	/**
	 * 
	 * serials of xml tree parser
	 * 
	 * */
	private boolean readX3D(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		parser.require(XmlPullParser.START_TAG, ns, "X3D");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("Scene")) {
				Log.w("info", "tag into" + " " +name);
				readScene(parser);
			} else {
				skip(parser);
			}
		}
		return true;
	}

	private boolean readScene(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "Scene");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("Transform")) {
				Log.w("info", "tag high into" + " " +name);
				readTransformHigh(parser);
			} else {
				skip(parser);
			}
		}
		return true;
	}
	
	private boolean readTransformHigh(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "Transform");
		
		String single_line = parser.getAttributeValue(null, "translation");
		String[] parts = single_line.split("\\s");
		
		for (byte i = 0; i<3; i++)
			translation[i] = Float.valueOf(parts[i]).floatValue();
		float x = translation[0];
		float y = translation[1];
		float z = translation[2];
//		Log.w("info2", "Translation "+Float.toString(x)+" "+Float.toString(y)+" "+Float.toString(z));
		
		
		
		single_line = parser.getAttributeValue(null, "scale");
		parts = single_line.split("\\s");
		
		for (byte i = 0; i<3; i++)
			scale[i] = Float.valueOf(parts[i]).floatValue();
		
		single_line = parser.getAttributeValue(null, "rotation");
		parts = single_line.split("\\s");
		
		for (byte i = 0; i<4; i++)
			rotation[i] = Float.valueOf(parts[i]).floatValue();
		
		
		
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("Transform")) {
				Log.w("info", "tag into" + " " +name);
				readTransformLow(parser);
			} else {
				skip(parser);
			}
		}
		return true;

	}
	
	private boolean readTransformLow(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "Transform");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("Group")) {
				Log.w("info", "tag into" + " " +name);
				readGroup(parser);
			} else {
				skip(parser);
			}
		}
		return true;

	}
	
	private boolean readGroup(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "Group");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("Shape")) {
				Log.w("info", "tag into" + " " +name);
				readShape(parser);
			} else {
				skip(parser);
			}
		}
		return true;

	}
	
	private boolean readShape(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "Shape");
		boolean success = true;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the appearance and indexedtriangleset tag
//			if (name.equals("Appearance")) {
//				//Log.w("info", "tag into" + " " + name);
//				//success = success && readAppearance(parser);
//			} 
			if (name.equals("IndexedTriangleSet")) {
				vertex_index = new ArrayList<short[]>();
				normal_index = new ArrayList<short[]>();
				texture_index = new ArrayList<short[]>();

				vertex_cor = new ArrayList<float[]>();
				normals = new ArrayList<float[]>();
				textures = new ArrayList<float[]>();
				Log.w("info", "tag into" + " " + name);
				this.already_triangle_mode = true;
				success = success && readIndexedTriangleSet(parser);
				
				create_Tri();
				
			} else {
				skip(parser);
			}
		}
		return success;

	}
	
	private boolean readAppearance(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "Appearance");
		boolean success = true;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			Log.w("info", "tag" + " " + name);
			// Starts by looking for the entry tag
			if (name.equals("Material")) {
				//success = success&&readMaterial(parser);
			} else {
				skip(parser);
			}
		}
		return success;

	}
	
	
	

	// Processes material tags in the x3d.
	private boolean readMaterial(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "Material");
		String diffuseColor = parser.getAttributeValue(null, "diffuseColor");
		String specularColor = parser.getAttributeValue(null, "specularColor");
		String emissiveColor = parser.getAttributeValue(null, "emissiveColor");
		String ambientIntensity = parser.getAttributeValue(null,
				"ambientIntensity");
		String shininess = parser.getAttributeValue(null, "shininess");
		String transparency = parser.getAttributeValue(null, "transparency");
		parser.require(XmlPullParser.END_TAG, ns, "Material");

		// convert string to arrays
		String[] parts = diffuseColor.split("\\s");
		for (byte i = 1; i < 3; i++)
			this.diffuse_color[i] = Float.valueOf(parts[i]).floatValue();
		this.diffuse_color[3] = 1.0f;
		
		parts = specularColor.split("\\s");
		for (byte i = 1; i < 3; i++)
			this.specular_color[i] = Float.valueOf(parts[i]).floatValue();
		this.specular_color[3] = 1.0f;
		
		parts = emissiveColor.split("\\s");
		for (byte i = 1; i < 3; i++)
			this.emissive_color[i] = Float.valueOf(parts[i]).floatValue();
		this.emissive_color[3] = 1.0f;
		
		this.ambient_color[3] = Float.valueOf(ambientIntensity).floatValue();
		this.shininess = Float.valueOf(shininess).floatValue();
		this.transparency = Float.valueOf(transparency).floatValue();
		
		return true;
	}

	// Processes link tags in the feed.
	private boolean readIndexedTriangleSet(XmlPullParser parser)
			throws IOException, XmlPullParserException {
		String _index = parser.getAttributeValue(null, "index");
		//because it is triangle
		String[] parts = _index.split("\\s");
		for (short i = 0; i<parts.length; i+=3)
		{
			short[] single_line = new short[3];
			for (byte j = 0; j<3; j++)
				single_line[j] = (short)Short.valueOf(parts[i+j]);
			this.vertex_index.add(single_line);
			this.normal_index.add(single_line);
			
			//Log.w("info", Integer.toString(single_line[0])+" " +Integer.toString(single_line[1])+" "+Integer.toString(single_line[2]));
		}
		parser.require(XmlPullParser.START_TAG, ns, "IndexedTriangleSet");
		while(parser.next()!=XmlPullParser.END_TAG)
		{
			//Log.w("info", "Hello"+ " " + parser.getName());
			if (parser.getEventType()!=XmlPullParser.START_TAG)
				continue;
			String name = parser.getName();
			Log.w("info", name);
			if (name.equals("Coordinate")){
				readCoordinate(parser);
				Log.w("info", "tag into" + " " +name);
			}
			else if(name.equals("Normal")){
				Log.w("info", "tag into" + " " +name);
				readNormal(parser);
			}
			else
				skip(parser);
			//Log.w("info", "Hello2"+ " " + parser.getName());
		}
		
		return true;
		//parser.require(XmlPullParser.END_TAG, ns, "IndexedTriangleSet");
	}

	// Processes summary tags in the feed.
	private boolean readCoordinate(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "Coordinate");
		String coordinate = parser.getAttributeValue(null, "point");
		String[] parts = coordinate.split("\\s");
		for (short i = 0; i<parts.length; i+=3)
		{
			float[] single_line = new float[3];
			for (byte j = 0; j<3; j++)
				single_line[j] = Float.valueOf(parts[i+j]).floatValue();
			this.vertex_cor.add(single_line);
			//Log.w("info", Float.toString(single_line[0])+" " +Float.toString(single_line[1])+" "+Float.toString(single_line[2]));
		}
		parser.nextTag();
		parser.require(XmlPullParser.END_TAG, ns, "Coordinate");
		Log.w("info", "readcoordinate");
		return true;
	}

	private boolean readNormal(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "Normal");
		String normal = parser.getAttributeValue(null, "vector");
		String[] parts = normal.split("\\s");
		for (short i = 0; i<parts.length; i+=3)
		{
			float[] single_line = new float[3];
			for (byte j = 0; j<3; j++)
				single_line[j] = Float.valueOf(parts[i+j]).floatValue();
			this.normals.add(single_line);
		}
		parser.nextTag();
		parser.require(XmlPullParser.END_TAG, ns, "Normal");
		
		return true;
	}

	// For the tags title and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

	/*********/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/
	/*** XML parser ***/

	public boolean parse_Data(BufferedReader br) {

		String line;
		try {
			while ((line = br.readLine()) != null) {
				parse_Line_Data(line);
				// Log.w(TAG, line);
			}
			create_Tri();
			return true;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private void parse_Line_Data(String line) {
		String[] parts = line.split("\\s");
		// vertices
		String mark = parts[0];

		// if (mark.equals("mtllib")) {
		// this.parse_Material(parts[1]);
		// }

		if (mark.equals("v")) {
			float[] single_vertex_cor = new float[parts.length - 1];
			for (byte i = 1; i <= parts.length - 1; i++)
				single_vertex_cor[i - 1] = Float.valueOf(parts[i]).floatValue();
			vertex_cor.add(single_vertex_cor);
			return;
			// Log.w(TAG, single_vertex_cor[0]+" "+
			// single_vertex_cor[1]+" "+single_vertex_cor[2]);
		}

		// textures
		if (mark.equals("vt")) {
			float[] single_texture = new float[2];

			for (byte i = 1; i <= 2; i++)
				single_texture[i - 1] = Float.valueOf(parts[i]).floatValue();
			textures.add(single_texture);
			return;
		}

		// normals
		if (mark.equals("vn")) {
			float[] single_normal = new float[3];

			for (byte i = 1; i <= 3; i++)
				single_normal[i - 1] = Float.valueOf(parts[i]).floatValue();
			normals.add(single_normal);
			return;
		}

		if (mark.equals("f")) {
			parse_Face(line);
			return;
		}
	}

	private void parse_Material(String filename) {
		this.material_mode = true;
		try {
			File read = new File(Environment.getExternalStorageDirectory()
					+ "/3D/", filename);
			BufferedReader br = new BufferedReader(new FileReader(read));
			Log.w(TAG, "material name" + " " + filename);
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\\s");
				String mark = parts[0];
				if (mark.equals("newmtl"))
					this.mtl_name = parts[1];
				if (mark.equals("Ka")) {
					for (byte i = 1; i <= 3; i++)
						this.ambient_color[i - 1] = Float.valueOf(parts[i])
								.floatValue();
				}
				if (mark.equals("Kd")) {
					for (byte i = 1; i <= 3; i++)
						this.diffuse_color[i - 1] = Float.valueOf(parts[i])
								.floatValue();
				}

				if (mark.equals("Ks")) {
					for (byte i = 1; i <= 3; i++)
						this.specular_color[i - 1] = Float.valueOf(parts[i])
								.floatValue();
				}

				if (mark.equals("d") || mark.equals("Tr")) {
					this.transparency = Float.valueOf(parts[1]).floatValue();
					this.ambient_color[3] = this.transparency;
					this.diffuse_color[3] = this.transparency;
					this.specular_color[3] = this.transparency;
				}

				if (mark.equals("Ns")) {
					this.shininess = Float.valueOf(parts[1]).floatValue();
				}

				if (mark.equals("illum")) {
					this.illum = Integer.valueOf(parts[1]).shortValue();
					// Log.w(TAG, "illum"+" "+Integer.toString(illum));
				}

				if (mark.equals("map_Ka")) {
					this.mtl_name = parts[1];
					parse_Texture_Image(this.mtl_name);
				}

			}

		} catch (Exception e) {
			Log.w("info", "read material file failure");
		}

	}

	private void parse_Texture_Image(String filename) {

	}

	private void parse_Face(String line) {
		String[] tokens = line.split("[ ]+");
		int c = tokens.length;

		if (tokens[1].matches("[0-9]+//[0-9]+")) {// f: v//vn
			// Log.w(TAG, "normal" + " " + Integer.toString(c));
			short[] single_vertex_index = new short[c - 1];
			short[] single_normal_index = new short[c - 1];
			for (int i = 1; i <= c - 1; i++) {
				short s = (short) Short.valueOf(tokens[i].split("//")[0]);
				s--;
				single_vertex_index[i - 1] = s;
				// Log.w(TAG, "vertex index" + Integer.toString(s));
				s = (short) Short.valueOf(tokens[i].split("//")[1]);

				s--;
				// Log.w(TAG, "normal index" + Integer.toString(s));
				single_normal_index[i - 1] = s;
			}
			vertex_index.add(single_vertex_index);
			normal_index.add(single_normal_index);

			return;
			// for (int i = 0; i <=c-2; i++)
			// Log.w(TAG,
			// "normal" + " "
			// + Integer.toString(single_normal_index[i]));

		}

		if (tokens[1].matches("[0-9]+")) {// f: v
			short[] single_vertex_index = new short[c - 1];

			for (byte i = 1; i <= c - 1; i++)
				single_vertex_index[i - 1] = (short) (Short.valueOf(tokens[i])
						.shortValue() - 1);
			vertex_index.add(single_vertex_index);
			n_tri = n_tri + tokens.length - 3;
			return;
		}

		if (tokens[1].matches("[0-9]+/[0-9]+")) {// if: v/vt
			Log.w("info", "load texture");
			short[] single_vertex_index = new short[c - 1];
			short[] single_texture_index = new short[c - 1];
			for (int i = 1; i <= c - 1; i++) {
				short s = (short) Short.valueOf(tokens[i].split("/")[0]);
				s--;
				single_vertex_index[i - 1] = s;
				s = (short) Short.valueOf(tokens[i].split("/")[1]);
				s--;
				single_texture_index[i - 1] = s;

			}
			n_tri = n_tri + tokens.length - 3;
			vertex_index.add(single_vertex_index);
			texture_index.add(single_texture_index);
			return;

		}

		if (tokens[1].matches("[0-9]+/[0-9]+/[0-9]+")) {// f: v/vt/vn

			short[] single_vertex_index = new short[c - 1];
			short[] single_normal_index = new short[c - 1];
			short[] single_texture_index = new short[c - 1];
			for (int i = 1; i <= c - 1; i++) {
				short s = (short) Short.valueOf(tokens[i].split("/")[0]);
				s--;
				single_vertex_index[i - 1] = s;
				s = (short) Short.valueOf(tokens[i].split("/")[1]);
				s--;
				single_texture_index[i - 1] = s;
				s = (short) Short.valueOf(tokens[i].split("/")[2]);
				s--;
				single_normal_index[i - 1] = s;
			}
			vertex_index.add(single_vertex_index);
			texture_index.add(single_texture_index);
			normal_index.add(single_normal_index);
			return;

		}

	}

	// original data is of rectangle, now parse to triangle
	private void create_Tri() {

		ArrayList<Short> _vertex_index_tri = new ArrayList<Short>();
		ArrayList<Short> _normal_index_tri = new ArrayList<Short>();
		// ArrayList<Short> _texture_index_tri = new ArrayList<Short>();

		_vertex_index_tri = (ArrayList<Short>) this.triangulate(vertex_index);
//		for (int i = 0; i < vertex_index.size(); i++) {
//			Log.w(TAG, Integer.toString(vertex_index.get(i)[0]));
//		}
		// vertex_index_tri = _vertex_index_tri.toArray(new Short[0]);

		try {

			_normal_index_tri = (ArrayList<Short>) this
					.triangulate(normal_index);
			// normal_index_tri = _normal_index_tri.toArray(new Short[0]);
		} catch (NullPointerException e) {
		}

		// try {
		// _texture_index_tri = (ArrayList<Short>) this
		// .triangulate(texture_index);
		// //texture_index_tri = _texture_index_tri.toArray(new Short[0]);
		// } catch (NullPointerException e) {
		// }

		// vertex

		vertex_cor_tri = new float[_vertex_index_tri.size() * 3];
		colors = new float[_vertex_index_tri.size() * 4];
		highlight_colors = new float[_vertex_index_tri.size()*4];
		int index = 0;
		// arrange vertex coordinates by indices
		for (int i = 0; i < _vertex_index_tri.size(); i++) {
			short where = _vertex_index_tri.get(i);

			for (byte j = 0; j < 3; j++) {
				// one triangle
				vertex_cor_tri[index] = vertex_cor.get(where)[j];
				// Log.w(TAG, Float.toString(vertex_cor_tri[index]) + " "
				// + Integer.toString(_vertex_index_tri.size()) + " "
				// + Integer.toString(index));
				index++;
			}
		}

		// Random generator = new Random( 19580427 );

		for (int i = 0; i < _vertex_index_tri.size() * 4; i += 4) {
			// for (byte j = 0; j<3; j++)
			// colors[i+j] = (float) (generator.nextInt(3)/2.0);
			// colors[i+3] = 0.0f;
			colors[i] = 0.2f;// * (1 + i / vertex_cor.size());
			colors[i + 1] = 0.5f;// * (1 + i / vertex_cor.size());
			colors[i + 2] = 0.6f;// * (1 - i / vertex_cor.size());
			colors[i + 3] = 1.0f;
			
//			highlight_colors[i] = 1.0f;// * (1 + i / vertex_cor.size());
//			highlight_colors[i + 1] = 1.0f;// * (1 + i / vertex_cor.size());
//			highlight_colors[i + 2] = 0.0f;// * (1 - i / vertex_cor.size());
//			highlight_colors[i + 3] = 1.0f;
			// grey color 0.6f,0.6f,0.6f,1.0f

		}

		// for (int i = 0; i<normals.size(); i++)
		// {
		// Log.w(TAG, Float.toString(normals.get(i)[0]));
		// }
		try {
			// normals
			normal_cor_tri = new float[_normal_index_tri.size() * 3];

			index = 0;
			for (int i = 0; i < _normal_index_tri.size(); i++) {
				short where = _normal_index_tri.get(i);
				// Log.w(TAG,Short.toString(where));
				for (byte j = 0; j < 3; j++) {
					normal_cor_tri[index] = normals.get(where)[j];
					// Log.w(TAG, Float.toString(normals.get(where)[j]) + " "
					// + Integer.toString(_normal_index_tri.size()) + " "
					// + Integer.toString(index));
					index++;
				}
				normal_mode = true;
			}
		} catch (NullPointerException e) {
			normal_mode = false;
		}

		
		if (xml_mode){
			
			//xml rotatation is according to an angle http://en.wikipedia.org/wiki/Rotation_matrix
			
			
			rotate(vertex_cor_tri, vertex_cor_tri, rotation);
			rotate(normal_cor_tri, normal_cor_tri, rotation);
			translate(vertex_cor_tri, vertex_cor_tri, translation);
			scale(vertex_cor_tri, vertex_cor_tri, scale);
			
		}
		
		list_vertex_cor_tri.add(vertex_cor_tri);
		list_colors.add(colors);
		list_highlight_colors.add(highlight_colors);
		list_normal_cor_tri.add(normal_cor_tri);

	}

	private void translate(float[] tm, float[] sm, float[] translation){
		for (int i = 0; i<sm.length; i+=3){
			for (byte j = 0; j<3; j++){
				tm[i+j] = sm[i+j] + translation[j];
			}
		}
	}
	
	private void scale(float[] tm, float[] sm, float[] scale){
		for (int i = 0; i<sm.length; i+=3){
			for (byte j = 0; j<3; j++){
				if (scale[j] == 1) continue;
					tm[i+j] = sm[i+j] * scale[j];
			}
		}
	}
	
	private void rotate(float[] tm, float[] sm, float[] rotation){
		//generate the rotation matrix http://en.wikipedia.org/wiki/Rotation_matrix
		float[][] matrix = new float[3][3];
		float a = rotation[3];
		float x = rotation[0];
		float y = rotation[1];
		float z = rotation[2];
		
		float cosa = (float) Math.cos(a);
		float sina = (float) Math.sin(a);
		matrix[0][0] = cosa + x*x*(1-cosa);
		matrix[0][1] = x*y*(1-cosa) - z*sina;
		matrix[0][2] = x*z*(1-cosa) + y*sina;
		matrix[1][0] = y*x*(1-cosa) + z*sina;
		matrix[1][1] = cosa + y*y*(1-cosa);
		matrix[1][2] = y*z*(1-cosa) - x*sina;
		matrix[2][0] = z*x*(1-cosa) - y*sina;
		matrix[2][1] = z*y*(1-cosa) + x*sina;
		matrix[2][2] = cosa + z*z*(1-cosa);
		
		float[] mm = new float[sm.length];
		for (short i = 0; i<sm.length; i+=3){
			for (byte j = 0; j<3; j++){
				for (byte k = 0; k<3; k++){
					mm[i+j]+=matrix[j][k]*sm[i+k];
				}
			}
		}
		if (tm.length != sm.length)
			tm = new float[sm.length];
		for (short i = 0; i<sm.length; i++)
			tm[i] = mm[i];
	}
	
	private List<Short> triangulate(List<short[]> input) {
		List<Short> output = new ArrayList<Short>();

		for (int i = 0; i < input.size(); i++) {
			// number of triangles from polygons
			int n = input.get(i).length - 2;
			for (int j = 1; j <= n; j++) {
				// Log.w(TAG, "length" + " " + Integer.toString(n + 2));
				output.add((short) input.get(i)[0]);
				output.add((short) input.get(i)[j]);
				output.add((short) input.get(i)[j + 1]);

			}

		}

		return output;
	}

}
