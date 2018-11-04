package com.example.sleepydragon.thirdtexture;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
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
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.GLUtils.texImage2D;

public class TexturedRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "FirstRenderer";

    private Context mContext;

    private static final int BYTES_PER_FLOAT = 4;
    private final FloatBuffer mVertexData;
    private final FloatBuffer mTextureData;
    private int mShaderProgram;

    private int aPositionLocation;
    private int uTextureUnitLocation;
    private int aTextureCoordinatesLocation;

    private float[] mTrianglePoints = {0.5f, -0.5f, -0.5f, -0.5f, 0.5f, 0.5f,-0.5f,0.5f};
    private float[] mTexturePoints = {1.0f, 1.0f, 0f, 1.0f, 1.0f, 0f,0f,0f};
    private int mTextureId;
    private String mVertexShaderCode =
                    "attribute vec4 a_Position;     \n" +
                    "attribute vec2 a_TextureCoordinates;\n" +
                    "varying vec2 v_TextureCoordinates;"+
                    "void main()                    \n" +
                    "{                              \n" +
                    "    v_TextureCoordinates = a_TextureCoordinates;\n"+
                    "    gl_Position = a_Position;  \n" +

                    "}   \n";
    private String mFragmentShaderCode =
            "precision mediump float; \n" +
                    "uniform sampler2D u_TextureUnit;      \n" +
                    "varying vec2 v_TextureCoordinates; \n " +
                    "void main()                    \n" +
                    "{                              \n" +
                    "    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates);    \n" +
                    "}";

    TexturedRenderer(@NonNull Context context) {
        mContext = context;
        mVertexData = ByteBuffer
                .allocateDirect(mTrianglePoints.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexData.put(mTrianglePoints);

        mTextureData = ByteBuffer
                .allocateDirect(mTexturePoints.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureData.put(mTexturePoints);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        //create vertex shader
        int vertexShader = compileShader(GL_VERTEX_SHADER, mVertexShaderCode);
        //create fragment shader
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, mFragmentShaderCode);
        //create program and link two shader
        mShaderProgram = linkProgram(vertexShader, fragmentShader);
        //use this program
        glUseProgram(mShaderProgram);


        aPositionLocation = glGetAttribLocation(mShaderProgram, "a_Position");
        aTextureCoordinatesLocation = glGetAttribLocation(mShaderProgram, "a_TextureCoordinates");
        uTextureUnitLocation = glGetUniformLocation(mShaderProgram, "u_TextureUnit");

        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_POSITION_LOCATION.
        mVertexData.position(0);
        glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT,
                false, 0, mVertexData);
        glEnableVertexAttribArray(aPositionLocation);

        mTextureData.position(0);
        glVertexAttribPointer(aTextureCoordinatesLocation,2, GL_FLOAT,
                false, 0, mTextureData);
        glEnableVertexAttribArray(aTextureCoordinatesLocation);

        mTextureId = loadTexture(mContext,R.drawable.golden_triangle);
        // Set the active texture unit to texture unit 0.
        glActiveTexture(GL_TEXTURE0);

        // Bind the texture to this unit.
        glBindTexture(GL_TEXTURE_2D, mTextureId);

        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        glUniform1i(uTextureUnitLocation, 0);

    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {

        glClear(GL_COLOR_BUFFER_BIT);
        // Draw the triangle.
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
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

    /**
     * Loads a texture from a resource ID, returning the OpenGL ID for that
     * texture. Returns 0 if the load failed.
     *
     * @param context
     * @param resourceId
     * @return
     */
    public static int loadTexture(Context context, int resourceId) {
        final int[] textureObjectIds = new int[1];
        glGenTextures(1, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            Log.w(TAG, "Could not generate a new OpenGL texture object.");
            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        // Read in the resource
        final Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(), resourceId, options);

        if (bitmap == null) {
            Log.w(TAG, "Resource ID " + resourceId + " could not be decoded.");

            glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }
        // Bind to the texture in OpenGL
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

        // Set filtering: a default must be set, or the texture will be
        // black.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        // Load the bitmap into the bound texture.
        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        // Note: Following code may cause an error to be reported in the
        // ADB log as follows: E/IMGSRV(20095): :0: HardwareMipGen:
        // Failed to generate texture mipmap levels (error=3)
        // No OpenGL error will be encountered (glGetError() will return
        // 0). If this happens, just squash the source image to be
        // square. It will look the same because of texture coordinates,
        // and mipmap generation will work.

        glGenerateMipmap(GL_TEXTURE_2D);

        // Recycle the bitmap, since its data has been loaded into
        // OpenGL.
        bitmap.recycle();

        // Unbind from the texture.
        glBindTexture(GL_TEXTURE_2D, 0);

        return textureObjectIds[0];
    }
}

