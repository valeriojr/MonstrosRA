package com.example.myapplication.rendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.example.myapplication.R;

import java.io.InputStream;
import java.util.Scanner;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class BasicRenderer implements GLSurfaceView.Renderer {
    protected final float[] projectionMatrix = new float[16];
    protected final float[] viewMatrix = new float[16];
    protected final float[] modelMatrix = new float[16];
    protected final float[] mvpMatrix = new float[16];
    protected Context context;
    protected int program;
    protected int vertexShaderId;
    protected int fragmentShaderId;
    protected int textureID;
    protected Mesh mesh;
    public float yaw;
    public float pitch;
    public float roll;
    private float meshPositionX, meshPositionY, meshPositionZ;
    protected float cameraX, cameraY, cameraZ;

    public BasicRenderer(Context context) {
        this.context = context;
        cameraX = cameraY = cameraZ = 0.0f;
    }

    private int loadShader(int type, int id) {
        int shader = GLES20.glCreateShader(type);
        InputStream fileInputStream = context.getResources().openRawResource(id);
        Scanner scanner = new Scanner(fileInputStream).useDelimiter("\\A");

        GLES20.glShaderSource(shader, scanner.hasNext() ? scanner.next() : "");
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Could not compile program: "
                    + GLES20.glGetShaderInfoLog(shader));
        }

        return shader;
    }

    protected int loadTexture(final Context context, final int resourceId)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,  0);
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public int getProgram() {
        return program;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        InputStream inputStream = context.getResources().openRawResource(R.raw.sapo);

        mesh = new Mesh(inputStream);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderId);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderId);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        textureID = loadTexture(context, R.raw.sapo_textura);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float ratio = (float) width / height;
        Matrix.perspectiveM(projectionMatrix, 0, 45, ratio, 0.1f, 1000.0f);
    }

    @Override public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(program);

        Matrix.setLookAtM(viewMatrix, 0, cameraX, cameraY, cameraZ,
                cameraX, cameraY, cameraZ - 1.0f,
                0.0f, 1.0f, 0.0f);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, meshPositionX, meshPositionY, meshPositionZ);

        Matrix.rotateM(modelMatrix, 0, yaw, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(modelMatrix, 0, pitch, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(modelMatrix, 0, roll, 0.0f, 0.0f, 1.0f);


        Matrix.scaleM(modelMatrix, 0, 1.0f, 1.0f, 1.0f);

        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "uTexture"), 0);
    }

    public void setMeshPosition(float x, float y, float z) {
        meshPositionX = x;
        meshPositionY = y;
        meshPositionZ = z;
    }
}
