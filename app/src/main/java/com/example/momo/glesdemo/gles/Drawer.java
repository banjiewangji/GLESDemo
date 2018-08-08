package com.example.momo.glesdemo.gles;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author shidefeng
 * @since 2018/1/30.
 */

public class Drawer {

    private static final String sPointVertexShaderCode =
            "attribute vec4 vPosition;" +
            "attribute vec4 aColor;" +
            "varying vec4 vColor;" +
                    "void main() {" +
//                    "gl_PointSize = 15.0;" +
                    "vColor = aColor;" +
                    "gl_Position = vPosition;" +
                    "}";

    // 注意 "#" 预处理符前不能有字符，所以最好加上 "\n"
    private static final String sFragmentVertexShaderCode =
            "#ifdef GL_FRAGMENT_PRECISION_HIGH \n" +
                    "precision highp float;\n" +
                    "#else \n" +
                    "precision mediump float;\n" +
                    "#endif \n" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "gl_FragColor = vColor;" +
                    "}";

//    private static final float[] pointCoords = {
//            -0.5f, 0.5f, 0,
//            -0.5f, -0.5f, 0,
//
//            0.5f, -0.5f, 0,
//            0.5f, 0.5f, 0,
//    };

    private static final float[] pointCoords = {
            -0.5f, 0.5f, 0,
            -0.5f, -0.5f, 0,
            0.5f, 0.5f, 0,
            0.5f, 0.5f, 0,
            0.5f, -0.5f, 0,
            -0.5f, -0.5f, 0,

//            0.5f, 0.5f, 0,
    };

    private static final float[] lineColors = {
            0, 1, 0, 1,
            1, 0, 0, 1,
            0, 0, 1, 1,
            0, 0, 1, 1,
            1, 1, 1, 1,
            1, 0, 0, 1,
    };

    private final int mProgram;
    private final int mPositionHandle;
    private final FloatBuffer mVertexBuffer;
    private final FloatBuffer mColorBuffer;
    private final int mColorHandle;

    public Drawer() {
        mVertexBuffer = ByteBuffer.allocateDirect(pointCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(pointCoords);
        mVertexBuffer.position(0);

        mColorBuffer = ByteBuffer.allocateDirect(lineColors.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(lineColors);
        mColorBuffer.position(0);

        final int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, sPointVertexShaderCode);
        final int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, sFragmentVertexShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 0.0f);

        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 12, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 16, mColorBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);

//        GLES20.glLineWidth(15);
    }

    public void draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    private int loadShader(int type, String source) {
        final int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
