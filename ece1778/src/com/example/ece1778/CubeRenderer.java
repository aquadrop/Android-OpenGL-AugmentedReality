package com.example.ece1778;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.android.constants.Light;
import com.android.ece1778.R;

public class CubeRenderer implements GLSurfaceView.Renderer {
	
	
	String TAG = "info";
	
	private boolean mTranslucentBackground;
	private Cube mCube;
	public float mAngleX;
	public float mAngleY;
	public float scale;

	protected boolean tutorial_mode = false;
	protected short which_component = 0;
	
	private VertexPackage mVertexPackage = null;
	
	public CubeRenderer(boolean useTranslucentBackground,
			VertexPackage mVertexPackage) {
		this.mVertexPackage = mVertexPackage;
		mTranslucentBackground = useTranslucentBackground;
		mCube = new Cube(mVertexPackage);
		tutorial_mode = false;
		scale = 1;
	}

	public void onDrawFrame(GL10 gl) {
		/*
		 * Usually, the first thing one might want to do is to clear the screen.
		 * The most efficient way of doing this is to use glClear().
		 */
		// Log.w("info", "2");
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		/*
		 * Now we're ready to draw some 3D objects
		 */

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		// gl.glPushMatrix();
		if (mVertexPackage.normal_mode) {
			this.inti_Light(gl);
			gl.glPushMatrix();
		}
		this.camera_Setup(gl);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		// float factor = (float) 1.001;
		//mCube.scale(scale);

		mCube.draw_mode(gl, tutorial_mode, this.which_component);
		
		if (mVertexPackage.normal_mode) {
			gl.glPopMatrix();
		}
		// gl.glRotatef(mAngleX*2.0f, 0, 1, 1);
		// gl.glTranslatef(0.5f, 0.5f, 0.5f);

		// mCube.draw(gl);

		// mAngleX += 1.2f;
		// scale =1;
	}

	private void init_Material(GL10 gl) {
//		if (mVertexPackage.material_mode) {
//			
//			gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, mVertexPackage.shininess);
//			if (mVertexPackage.illum == 1) {
//				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Cube
//						.generate_Float_Buffer(mVertexPackage.ambient_color));
//
//				
//
//				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Cube
//						.generate_Float_Buffer(mVertexPackage.diffuse_color));
//			}
//			else if (mVertexPackage.illum == 2) {
//				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Cube
//						.generate_Float_Buffer(mVertexPackage.ambient_color));
//				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Cube
//						.generate_Float_Buffer(mVertexPackage.diffuse_color));
//				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, Cube
//						.generate_Float_Buffer(mVertexPackage.specular_color));
//				
//				for (int i = 0; i < 4; i++) {
//					Log.w("info", Float.toString(mVertexPackage.ambient_color[i]));
//				}
//			}
//			
//			else
//			{
//				gl.glColorPointer(4, GL10.GL_FLOAT, 0, Cube.generate_Float_Buffer(mVertexPackage.colors));
//				gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
//			}
//		}
//		else{
//			gl.glColorPointer(4, GL10.GL_FLOAT, 0, Cube.generate_Float_Buffer(mVertexPackage.colors));
//			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
//		}
	}

	private void inti_Light(GL10 gl) {

		gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT,
				Cube.generate_Float_Buffer(mVertexPackage.ambient_color));

		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION,
				Cube.generate_Float_Buffer(Light.pos0));
		
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE,
				Cube.generate_Float_Buffer(mVertexPackage.diffuse_color));
		gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 60.0f);

		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION,
				Cube.generate_Float_Buffer(Light.pos1));
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR,
				Cube.generate_Float_Buffer(mVertexPackage.specular_color));
		
		gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT,
				Cube.generate_Float_Buffer(mVertexPackage.ambient_color));

		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_POSITION,
				Cube.generate_Float_Buffer(Light.pos2));

		gl.glEnable(GL10.GL_LIGHTING);

		gl.glShadeModel(GL10.GL_SMOOTH);

		gl.glEnable(GL10.GL_LIGHT0);
		gl.glEnable(GL10.GL_LIGHT1);
		gl.glEnable(GL10.GL_LIGHT2);

	}

	private void camera_Setup(GL10 gl) {
		gl.glTranslatef(0, 0, -3.0f);
		gl.glRotatef(mAngleX, 0, 1, 0);
		gl.glRotatef(mAngleY, 1, 0, 0);
		gl.glScalef(scale, scale, scale);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);

		/*
		 * Set our projection matrix. This doesn't have to be done each time we
		 * draw, but usually a new projection needs to be set when the viewport
		 * is resized.
		 */
		// Log.w("info", "1");
		float ratio = (float) width / height;
		gl.glMatrixMode(GL10.GL_PROJECTION);

		gl.glLoadIdentity();
		gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		/*
		 * By default, OpenGL enables features that improve quality but reduce
		 * performance. One might want to tweak that especially on software
		 * renderer.
		 */
		gl.glDisable(GL10.GL_DITHER);

		/*
		 * Some one-time OpenGL initialization can be made here probably based
		 * on features of this particular context
		 */
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		if (mTranslucentBackground) {
			gl.glClearColor(0, 0, 0, 0);
		} else {
			gl.glClearColor(1, 1, 1, 1);
		}
		if (mVertexPackage.material_mode) {
			this.init_Material(gl);
		}
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glCullFace(GL10.GL_BACK);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}

	
	
	
	protected String getVertexShader()
	{
		// TODO: Explain why we normalize the vectors, explain some of the vector math behind it all. Explain what is eye space.
		final String vertexShader =
			"uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
		  + "uniform mat4 u_MVMatrix;       \n"		// A constant representing the combined model/view matrix.	
		  + "uniform vec3 u_LightPos;       \n"	    // The position of the light in eye space.
			
		  + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
		  + "attribute vec4 a_Color;        \n"		// Per-vertex color information we will pass in.
		  + "attribute vec3 a_Normal;       \n"		// Per-vertex normal information we will pass in.
		  
		  + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
		  
		  + "void main()                    \n" 	// The entry point for our vertex shader.
		  + "{                              \n"		
		// Transform the vertex into eye space.
		  + "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              \n"
		// Transform the normal's orientation into eye space.
		  + "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n"
		// Will be used for attenuation.
		  + "   float distance = length(u_LightPos - modelViewVertex);             \n"
		// Get a lighting direction vector from the light to the vertex.
		  + "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);        \n"
		// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
		// pointing in the same direction then it will get max illumination.
		  + "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);       \n" 	  		  													  
		// Attenuate the light based on distance.
		  + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));  \n"
		// Multiply the color by the illumination level. It will be interpolated across the triangle.
		  + "   v_Color = a_Color * diffuse;                                       \n" 	 
		// gl_Position is a special variable used to store the final position.
		// Multiply the vertex by the matrix to get the final point in normalized screen coordinates.		
		  + "   gl_Position = u_MVPMatrix * a_Position;                            \n"     
		  + "}                                                                     \n"; 
		
		return vertexShader;
	}
	
	protected String getFragmentShader()
	{
		final String fragmentShader =
			"precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a 
													// precision in the fragment shader.				
		  + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the 
		  											// triangle per fragment.			  
		  + "void main()                    \n"		// The entry point for our fragment shader.
		  + "{                              \n"
		  + "   gl_FragColor = v_Color;     \n"		// Pass the color directly through the pipeline.		  
		  + "}                              \n";
		
		return fragmentShader;
	}
	
	/** 
	 * Helper function to compile a shader.
	 * 
	 * @param shaderType The shader type.
	 * @param shaderSource The shader source code.
	 * @return An OpenGL handle to the shader.
	 */
	private int compileShader(final int shaderType, final String shaderSource) 
	{
		int shaderHandle = GLES20.glCreateShader(shaderType);

		if (shaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(shaderHandle, shaderSource);

			// Compile the shader.
			GLES20.glCompileShader(shaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{
				Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}

		if (shaderHandle == 0)
		{			
			throw new RuntimeException("Error creating shader.");
		}
		
		return shaderHandle;
	}	
	
	/**
	 * Helper function to compile and link a program.
	 * 
	 * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
	 * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
	 * @param attributes Attributes that need to be bound to the program.
	 * @return An OpenGL handle to the program.
	 */
	private int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes) 
	{
		int programHandle = GLES20.glCreateProgram();
		
		if (programHandle != 0) 
		{
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);			

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
			
			// Bind attributes
			if (attributes != null)
			{
				final int size = attributes.length;
				for (int i = 0; i < size; i++)
				{
					GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
				}						
			}
			
			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) 
			{				
				Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}
		
		if (programHandle == 0)
		{
			throw new RuntimeException("Error creating program.");
		}
		
		return programHandle;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}






