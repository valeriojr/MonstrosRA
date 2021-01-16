package com.example.myapplication.rendering;

import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

public class Mesh {
    private final int vbo; // vertex data
    private final int nbo; // normal data
    private final int ibo; // indices
    private int indicesLength;

    public Mesh(InputStream inputStream) {
        int nBuffers = 3;
        int[] buffers = new int[nBuffers];
        GLES20.glGenBuffers(nBuffers, buffers, 0);
        vbo = buffers[0];
        nbo = buffers[1];
        ibo = buffers[2];

        Obj obj = null;
        try {
            obj = ObjUtils.convertToRenderable(ObjReader.read(inputStream));
            ShortBuffer indices = ObjData.convertToShortBuffer(ObjData.getFaceVertexIndices(obj));
            FloatBuffer vertices = ObjData.getVertices(obj);
            FloatBuffer texCoords = ObjData.getTexCoords(obj, 2);
            FloatBuffer normals = ObjData.getNormals(obj);

            indicesLength = indices.remaining();

            // Positions
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 4 * vertices.remaining(), vertices,
                    GLES20.GL_STATIC_DRAW);
            // Normals
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, nbo);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 4 * normals.remaining(), normals,
                    GLES20.GL_STATIC_DRAW);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, 2 * indices.remaining(), indices,
                    GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

            inputStream.close();
        } catch (IOException e) {
            Log.d("MeshLoading", "Error: " + e.getMessage());
        }
    }

    public void draw(BasicRenderer renderer) {
        int positionHandle = GLES20.glGetAttribLocation(renderer.getProgram(), "aPosition");
        int normalHandle = GLES20.glGetAttribLocation(renderer.getProgram(), "aNormal");

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, nbo);
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesLength, GLES20.GL_UNSIGNED_SHORT, 0);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
