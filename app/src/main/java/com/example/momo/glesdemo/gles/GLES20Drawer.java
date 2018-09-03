package com.example.momo.glesdemo.gles;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Size;

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
            -1f, -1f, 0f,   // 0
            1f, -1f, 0f,    // 1
            -1f, 1f, 0f,    // 2
            1f, 1f, 0f,     // 3
    };

    /**
     * 定义纹理坐标
     */
    private float[] texturePoints = {
            0f, 0f,   // 0
            1f, 0f,   // 1
            0f, 1f,   // 2
            1f, 1f    // 3
    };

    /**
     * 定义纹理坐标
     */
    private float[] texturePoints_90 = {
            0f, 1f,   // 0
            1f, 1f,   // 1
            0f, 0f,   // 2
            1f, 0f    // 3
    };

    /**
     * 定义顶点索引
     */
    private short[] vertexIndexs = {
            0, 1, 2, 3
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
     * 定义纹理坐标缓冲
     */
    private Buffer mtextureBuffer = ByteBuffer
            .allocateDirect(vertexPoints.length * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(texturePoints_90)
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
            "attribute vec4 vPosition;\n" +
                    "attribute vec2 a_textureCoord;\n" +
                    "varying vec2 v_textureCoord;\n" +
                    "void main() {\n" +
                    "v_textureCoord = a_textureCoord;\n" +
                    "gl_Position = vPosition;\n" +
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
                    "varying vec2 v_textureCoord;\n" +
                    "uniform sampler2D texture1;\n" +
                    "uniform sampler2D texture2;\n" +
                    "uniform vec4 vColor;\n" +

                    "void main() {" +
                        "gl_FragColor = mix(texture2D(texture1, v_textureCoord), texture2D(texture2, v_textureCoord), vColor.z);" +
//                        "gl_FragColor = texture2D(texture2, v_textureCoord);" +
                    "}";

    private boolean initialized = false;
    private boolean isSizeChanged = false;
    private int mVertexShaderHandle;
    private int mFragmentShaderHandle;
    private int mProgramHandle;

    private int mPositionHandle;
    private int mTexCoordHandle;

    private int mColorHandle;
    private int mTexture1Handle;
    private int mTexture2Handle;

    private Bitmap mBitmap1;
    private Bitmap mBitmap2;
    private boolean isSettedImage1;
    private boolean isSettedImage2;
    private int[] mTextureIds;
    private Size mScreenSize;

    public void setTexture1(Bitmap texture) {
        mBitmap1 = texture;
    }

    public void setTexture2(Bitmap texture) {
        mBitmap2 = texture;
    }

    public void draw() {
        if (!initialized) {
            initGLES();
            initTexture();
            GLES20.glViewport(0, 0, 720, 1280);
            initialized = true;
        }
        if (isSizeChanged && mScreenSize != null) {
            isSizeChanged = false;
            final int offsetW = (mScreenSize.getWidth() - 720) / 2;
            final int offsetH = (mScreenSize.getHeight() - 1280) / 2;
            GLES20.glViewport(offsetW, offsetH, 720, 1280);
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

        GLES20.glClearColor(1f, 0f, 0f, 1f);
    }

    private void initTexture() {
        mTextureIds = new int[2];
        GLES20.glGenTextures(2, mTextureIds, 0);

        for (int i = 0; i < mTextureIds.length; i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[i]);

            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, GLES20.GL_TRUE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
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
        GLES20.glBindAttribLocation(mVertexShaderHandle, 0, "a_textureCoord");
    }

    private void initShaderHandles() {
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "vPosition");
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_textureCoord");
        mTexture1Handle = GLES20.glGetUniformLocation(mProgramHandle, "texture1");
        mTexture2Handle = GLES20.glGetUniformLocation(mProgramHandle, "texture2");
        mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "vColor");
    }

    private void onDraw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        passShaderValues();
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, 4, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);
    }

    private float delta = 0.01f;
    private float mRed = 0f;

//    private float[] color = {0.0f, 0.0f, 0.0f, 1.0f};

    private void passShaderValues() {
        GLES20.glUseProgram(mProgramHandle);

        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 12, mPointBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 8, mtextureBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);

        mRed = (mRed + delta) % 1f;

//        color[0] = mRed;

        GLES20.glUniform4f(mColorHandle, 0.0f, 0.0f, mRed, 1.0f);
        GLES20.glUniform1i(mTexture1Handle, 0);
        GLES20.glUniform1i(mTexture2Handle, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[0]);
        if (!isSettedImage1 && mBitmap1 != null && !mBitmap1.isRecycled()) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap1, 0);
            isSettedImage1 = true;
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[1]);
        if (!isSettedImage2 && mBitmap2 != null && !mBitmap2.isRecycled()) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap2, 0);
            isSettedImage2 = true;
        }
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
        if (mTextureIds != null && mTextureIds.length > 0) {
            GLES20.glDeleteTextures(mTextureIds.length, mTextureIds, 0);
        }
    }

    public void setScreenSize(Size size) {
        mScreenSize = size;
        isSizeChanged = true;
    }
}
