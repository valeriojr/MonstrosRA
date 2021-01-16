package com.example.myapplication.rendering;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.myapplication.R;

import javax.microedition.khronos.opengles.GL10;

public class PhongRenderer extends BasicRenderer {
    private float[] modelViewMatrix = new float[16];

    public PhongRenderer(Context context) {
        super(context);

        vertexShaderId = R.raw.phong_shading_v;
        fragmentShaderId = R.raw.phong_shading_f;

        //cameraZ = 1.0f;
    }

    @Override public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);

        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);

        int projectionHandle = GLES20.glGetUniformLocation(program, "uProjection");
        int viewHandle = GLES20.glGetUniformLocation(program, "uView");
        int modelHandle = GLES20.glGetUniformLocation(program, "uModel");
        int lightPosHandle = GLES20.glGetUniformLocation(program, "lightPos");
        int cameraPosHandle = GLES20.glGetUniformLocation(program, "cameraPosition");
        int objectColor = GLES20.glGetUniformLocation(program, "objectColor");

        GLES20.glUniformMatrix4fv(projectionHandle, 1, false, projectionMatrix, 0);
        GLES20.glUniformMatrix4fv(viewHandle, 1, false, viewMatrix, 0);
        GLES20.glUniformMatrix4fv(modelHandle, 1, false, modelMatrix, 0);
        GLES20.glUniform3f(lightPosHandle, 0.0f, 2.0f, 0.0f);
        GLES20.glUniform3f(cameraPosHandle, cameraX, cameraY, cameraZ);
        GLES20.glUniform4f(objectColor, 0.1f, 0.1f, 0.8f, 1.0f);

        mesh.draw(this);
    }
}
