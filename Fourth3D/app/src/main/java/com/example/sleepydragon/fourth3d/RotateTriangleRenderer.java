package com.example.sleepydragon.fourth3d;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public class RotateTriangleRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "FirstRenderer";
    private static final int BYTES_PER_FLOAT = 4;
    private final FloatBuffer mVertexData;
    private final FloatBuffer mColorData;
    private int mShaderProgram;

    private int aColorLocation;
    private int aPositionLocation;
    private int uMatrixLocation;

    private final float[] modelMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];


    private float[] mTrianglePoints =
            {0.0f, 0.5f, 0.0f,
                    0.0f, 0.0f, 0.5f,
                    0.5f, 0.0f, 0.0f,
                    -0.25f, 0.0f, -0.25f,
                    0.0f, 0.5f, 0.0f,
                    0.0f, 0.0f, 0.5f};
    private float[] mColorPoints =
            {1.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 0.0f, 1.0f,
                    0.5f, 0.5f, 0.5f,
                    1.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,};
    private String mVertexShaderCode =
            "uniform mat4 u_Matrix;        \n" +
                    "attribute vec4 a_Position;     \n" +
                    "attribute vec4 a_Color;     \n" +
                    "varying vec4 v_Color;     \n" +
                    "void main()                    \n" +
                    "{                              \n" +
                    "    v_Color =  a_Color;  \n" +
                    "    gl_Position =  u_Matrix * a_Position;  \n" +
                    "}   \n";
    private String mFragmentShaderCode =
            "precision mediump float; \n" +
                    "varying vec4 v_Color;     \n" +
                    "void main()                    \n" +
                    "{                              \n" +
                    "    gl_FragColor = v_Color;    \n" +
                    "}";

    RotateTriangleRenderer() {
        mVertexData = ByteBuffer
                .allocateDirect(mTrianglePoints.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mVertexData.put(mTrianglePoints);

        mColorData = ByteBuffer
                .allocateDirect(mColorPoints.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mColorData.put(mColorPoints);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        //create vertex shader
        int vertexShader = compileShader(GL_VERTEX_SHADER, mVertexShaderCode);
        //create fragment shader
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, mFragmentShaderCode);
        //create program and link two shader
        mShaderProgram = linkProgram(vertexShader, fragmentShader);
        //use this program
        glUseProgram(mShaderProgram);

        aColorLocation = glGetAttribLocation(mShaderProgram, "a_Color");

        aPositionLocation = glGetAttribLocation(mShaderProgram, "a_Position");

        uMatrixLocation = glGetUniformLocation(mShaderProgram, "u_Matrix");

        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_POSITION_LOCATION.
        mVertexData.position(0);
        glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT,
                false, 0, mVertexData);
        glEnableVertexAttribArray(aPositionLocation);

        mColorData.position(0);
        glVertexAttribPointer(aColorLocation, 3, GL_FLOAT,
                false, 0, mColorData);
        glEnableVertexAttribArray(aColorLocation);

    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);
        Matrix.perspectiveM(projectionMatrix, 0, 45, (float) width
                / (float) height, 1f, 10f);

        setIdentityM(modelMatrix, 0);

        translateM(modelMatrix, 0, 0f, 0f, -2.5f);
        rotateM(modelMatrix, 0, 20f, 1f, 0f, 0f);
//        rotateM(modelMatrix, 0, -10f, 0f, 1f, 0f);
//        rotateM(modelMatrix, 0, -60f, 0f, 0f, 1f);

        multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, modelMatrix, 0);
    }

    private void updateAngle() {
        rotateM(modelMatrix, 0, (float) 1.0, 0f, 1f, 0f);
        multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, modelMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        updateAngle();
        glClear(GL_COLOR_BUFFER_BIT);

        // Assign the matrix
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);
        // Draw the table.
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 6);
    }


    /**
     * Compiles a shader, returning the OpenGL object ID.
     */
    private static int compileShader(int type, String shaderCode) {

        // Create a new shader object.
        final int shaderObjectId = glCreateShader(type);

        if (shaderObjectId == 0) {
            Log.w(TAG, "Could not create new shader.");
            return 0;
        }

        // Pass in the shader source.
        glShaderSource(shaderObjectId, shaderCode);

        // Compile the shader.
        glCompileShader(shaderObjectId);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

        // Print the shader info log to the Android log output.
        Log.v(TAG, "Results of compiling source:" + "\n" + shaderCode + "\n:"
                + glGetShaderInfoLog(shaderObjectId));

        // Verify the compile status.
        if (compileStatus[0] == 0) {
            // If it failed, delete the shader object.
            glDeleteShader(shaderObjectId);
            Log.w(TAG, "Compilation of shader failed.");
            return 0;
        }

        // Return the shader object ID.
        return shaderObjectId;
    }

    /**
     * Links a vertex shader and a fragment shader together into an OpenGL
     * program. Returns the OpenGL program object ID, or 0 if linking failed.
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {

        // Create a new program object.
        final int programObjectId = glCreateProgram();

        if (programObjectId == 0) {
            Log.w(TAG, "Could not create new program");
            return 0;
        }

        // Attach the vertex shader to the program.
        glAttachShader(programObjectId, vertexShaderId);
        // Attach the fragment shader to the program.
        glAttachShader(programObjectId, fragmentShaderId);

        // Link the two shaders together into a program.
        glLinkProgram(programObjectId);

        // Get the link status.
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);

        // Print the program info log to the Android log output.
        Log.v(TAG, "Results of linking program:\n"
                + glGetProgramInfoLog(programObjectId));

        // Verify the link status.
        if (linkStatus[0] == 0) {
            // If it failed, delete the program object.
            glDeleteProgram(programObjectId);
            Log.w(TAG, "Linking of program failed.");
            return 0;
        }

        // Return the program object ID.
        return programObjectId;
    }
}

