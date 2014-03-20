package com.example.modelloading;

import java.nio.FloatBuffer;

import android.opengl.GLES20;


public class Ground {
	

	
	
	public Ground() {
		
	}
	
	public void draw(FloatBuffer vertexBuffer, FloatBuffer texBuffer, int mTextureDataHandle, float[] model, float[] view, float[] projection,float[] modelViewMatrix,int modelviewHandle, int mhorsecolorHandler, int positionHandle, int mTextureCoordinateHandle, int projectionHandle, int viewmatHandle, int modelHandle, int mTextureUniformHandle, int length,int horseColor) {
		//position
		GLES20.glEnableVertexAttribArray(positionHandle);
		GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false,	3 * 4, vertexBuffer);
		
		//texture
		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
		GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false,2 * 4, texBuffer);
		
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        
        
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);
        
		GLES20.glUniformMatrix4fv(projectionHandle, 1, false, projection, 0);
		GLES20.glUniformMatrix4fv(viewmatHandle, 1, false, view, 0);
		GLES20.glUniformMatrix4fv(modelHandle, 1, false, model, 0);
		GLES20.glUniformMatrix4fv(modelviewHandle, 1, false, modelViewMatrix, 0);
		GLES20.glUniform1i(mhorsecolorHandler,horseColor);
        
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, length/3);
		//GLES20.glDrawElements(GLES20.GL_TRIANGLES, faceVerticesLength,GLES20.GL_UNSIGNED_SHORT, vertexFaceOrder);
		
		GLES20.glDisableVertexAttribArray(positionHandle);
		GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
	}
}
