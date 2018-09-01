package com.example.momo.glesdemo.gles;

import android.opengl.GLES20;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author shidefeng
 * @since 2018/8/28.
 */
public class GLES20Drawer {

    /**
     * 定义顶点数据
     */
    private float[] vertexPoints = {
            -1f, 1f, 0f,    // 0
            -1f, -1f, 0f,   // 1
            1f, -1f, 0f,    // 2
            1f, 1f, 0f      // 3
    };

    /**
     * 定义顶点索引
     */
    private short[] vertexIndexs = {
            0, 1, 2,    // 第一个三角形
            0, 2, 3     // 第二个三角形
    };

    /**
     * 定义顶点数据缓冲
     */
    private Buffer mPointBuffer = ByteBuffer
            .allocateDirect(vertexPoints.length * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexPoints)
            .position(0);

    /**
     * 定义索引缓冲
     */
    private Buffer mIndexBuffer = ByteBuffer
            .allocateDirect(vertexIndexs.length * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(vertexIndexs)
            .position(0);

    /**
     * 定义顶点着色器语言
     */
    private static final String vertexShader =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "gl_Position = vPosition;" +
                    "}";

    /**
     * 定义片段着色器语言
     */
    // 注意 "#" 预处理符前不能有字符，所以最好加上 "\n"
    private static final String fragmentShdader =
            "#ifdef GL_FRAGMENT_PRECISION_HIGH \n" +
                    "precision highp float;\n" +
                    "#else \n" +
                    "precision mediump float;\n" +
                    "#endif \n" +
                    "uniform vec4 vColor;\n" +
                    "void main() {" +
                    "gl_FragColor = vColor;" +
                    "}";

    private boolean initialized = false;
    private int mVertexShaderHandle;
    private int mFragmentShaderHandle;
    private int mProgramHandle;

    private int mPositionHandle;
    private int mColorHandle;

    public void draw() {
        if (!initialized) {
            initGLES();
            GLES20.glViewport(0, 0, 720, 1280);
            initialized = true;
        }
        onDraw();
    }

    private void initGLES() {
        // 加载、编译着色器
        mVertexShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        mFragmentShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShdader);

        // 加载、链接着色器程序
        mProgramHandle = GLES20.glCreateProgram();
        String errorLog = "";
        if (mProgramHandle != 0) {
            GLES20.glAttachShader(mProgramHandle, mVertexShaderHandle);
            GLES20.glAttachShader(mProgramHandle, mFragmentShaderHandle);

            // 绑定attribute变量位置索引
            bindShaderAttributes();

            GLES20.glLinkProgram(mProgramHandle);

            // 检测是否链接成功
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(mProgramHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
            // 查看失败原因
            if (linkStatus[0] == 0) {
                errorLog = GLES20.glGetProgramInfoLog(mProgramHandle);
                GLES20.glDeleteProgram(mProgramHandle);
                mProgramHandle = 0;
            }
        }
        if (mProgramHandle == 0) {
            throw new RuntimeException(this + "Could not create program " + errorLog);
        }

        initShaderHandles();

        GLES20.glUseProgram(mProgramHandle);

        GLES20.glClearColor(1f, 0f, 0f, 1f);
    }

    private int loadShader(int type, String code) {
        final int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // 检测是否编译成功
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // 如果失败，查看失败的原因是什么
        if (compileStatus[0] == 0) {
            final String log = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException(this + ": Could not create shader. Reason: " + log);
        }

        return shader;
    }

    private void bindShaderAttributes() {
        GLES20.glBindAttribLocation(mVertexShaderHandle, 0, "vPosition");
    }

    private void initShaderHandles() {
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "vPosition");
        mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "vColor");
    }

    private void onDraw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        passShaderValues();
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);
    }

    private float delta = 0.01f;
    private float mRed = 0f;

//    private float[] color = {0.0f, 0.0f, 0.0f, 1.0f};

    private void passShaderValues() {
        GLES20.glUseProgram(mProgramHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 12, mPointBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        mRed = (mRed + delta) % 1f;

//        color[0] = mRed;

        GLES20.glUniform4f(mColorHandle, mRed, 0.0f, 0.0f, 1.0f);
//        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
    }

    public void release() {
        initialized = false;
        if (mProgramHandle != 0) {
            GLES20.glDeleteProgram(mProgramHandle);
            mProgramHandle = 0;
        }
        if (mVertexShaderHandle != 0) {
            GLES20.glDeleteShader(mVertexShaderHandle);
            mVertexShaderHandle = 0;
        }
        if (mFragmentShaderHandle != 0) {
            GLES20.glDeleteShader(mFragmentShaderHandle);
            mFragmentShaderHandle = 0;
        }
    }
}
