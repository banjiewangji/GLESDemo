package com.example.momo.glesdemo.gles;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;

import com.example.momo.glesdemo.gles.Drawer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author shidefeng
 * @since 2018/1/30.
 */

public class GLES20Renderer implements GLSurfaceView.Renderer {

    private Drawer mDrawer;
    private FpsListener mFpsListener;

    private int mFps;
    private long mLastTime;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mDrawer = new Drawer();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        final int delta = height - width;
        GLES20.glViewport(0, delta / 2, width, width);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mDrawer.draw();

        fps();
    }

    private void fps() {
        mFps++;
        final long now = SystemClock.uptimeMillis();
        if (now - mLastTime < 1000) {
            return;
        }
        if (mFpsListener != null) {
            mFpsListener.fps(mFps);
        }
        mLastTime = now;
        mFps = 0;
    }

    public void setFPSListener(FpsListener listener) {
        mFpsListener = listener;
    }

    public interface FpsListener {
        void fps(int fps);
    }
}
