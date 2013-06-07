package com.example.ece1778;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.util.Log;
import com.android.ece1778.R;

class Cube {
	String TAG = "info";

	private VertexPackage mVertexPackage = null;

	private List<FloatBuffer> mVertexBuffer;
	private List<FloatBuffer> mColorBuffer;
	private List<FloatBuffer> mHighlightColorBuffer;
	private List<FloatBuffer> mNormalBuffer;

	public Cube(VertexPackage mVertexPackage) {
		this.mVertexPackage = mVertexPackage;
		mVertexBuffer = new ArrayList<FloatBuffer>();
		mColorBuffer = new ArrayList<FloatBuffer>();
		mHighlightColorBuffer = new ArrayList<FloatBuffer>();
		mNormalBuffer = new ArrayList<FloatBuffer>();

		/****************/
		for (short i = 0; i < this.mVertexPackage.list_vertex_cor_tri.size(); i++) {
			load_Buffer(i);
		}
		/***************/
	}

	public static FloatBuffer generate_Float_Buffer(float[] input) {
		FloatBuffer output = null;
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(input.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		output = byteBuf.asFloatBuffer();
		output.put(input);
		output.position(0);
		return output;

	}

	public static ShortBuffer generate_Short_Buffer(short[] input) {
		ShortBuffer output = null;
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(input.length * 2);
		byteBuf.order(ByteOrder.nativeOrder());
		output = byteBuf.asShortBuffer();
		output.put(input);
		output.position(0);
		return output;
	}

	private void load_Buffer(int which_component) {
		try {
			mVertexBuffer.add(Cube
					.generate_Float_Buffer(mVertexPackage.list_vertex_cor_tri
							.get(which_component)));

			
			mColorBuffer.add(Cube
					.generate_Float_Buffer(mVertexPackage.list_colors
							.get(which_component)));
			
			mHighlightColorBuffer.add(Cube
					.generate_Float_Buffer(mVertexPackage.list_highlight_colors
							.get(which_component)));

			// mLightPositionBuffer = Cube.generate_Float_Buffer(pos);
			//
			// mDiffuseLightBuffer = Cube.generate_Float_Buffer(diffuse);
			try {
				if (mVertexPackage.normal_mode) {
					mNormalBuffer
							.add(Cube
									.generate_Float_Buffer(mVertexPackage.list_normal_cor_tri
											.get(which_component)));
				}
			} catch (Exception e) {
			}
			// try {
			// if (mVertexPackage.texture_mode) {
			//
			// mTextureBuffer = Cube.generate_Float_Buffer(textures);
			// }
			// } catch (Exception e) {
			// }

			// mIndexBuffer = Cube.generate_Short_Buffer(indices);

		} catch (Exception e) {
		}
	}

	public void draw_mode(GL10 gl, boolean tutorial_mode, short which) {
		// Log.w(TAG,
		// "components"+" "+Integer.toString(this.mVertexBuffer.size()));
		if (!tutorial_mode) {
			for (short i = 0; i < this.mVertexBuffer.size(); i++) {
				draw(gl, i);
			}

		} else {
			 //Log.w(TAG, "drawing"+" "+which+
			 //"components"+" "+this.mVertexBuffer.size());
			for (short i = 0; i < which+1; i++)
				draw(gl, i);
		}
	}

	public void draw(GL10 gl, short which) {

		boolean normal_mode = mVertexPackage.normal_mode;
		// boolean texture_mode = mVertexPackage.texture_mode;
		// boolean texture_3d_mode = mVertexPackage.texture_3d_mode;

		gl.glFrontFace(GL10.GL_CW);

//		gl.glColorPointer(4, GL10.GL_FLOAT, 0, this.mColorBuffer.get(which));
//		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer.get(which));
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		try {
			if (normal_mode) {
				gl.glNormalPointer(GL10.GL_FLOAT, 0, mNormalBuffer.get(which));
				gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
				gl.glEnable(GL10.GL_NORMALIZE);
			}
		} catch (Exception e) {
		}

		gl.glDrawArrays(GLES20.GL_TRIANGLES, 0,
				mVertexPackage.list_vertex_cor_tri.get(which).length / 3);
		// gl.glDrawElements(GL10.GL_TRIANGLES, indices.length,
		// GL10.GL_UNSIGNED_SHORT, mIndexBuffer);

		if (normal_mode) {
			gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		}

		
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	}

}
