package com.example.momo.glesdemo;

import android.opengl.GLES20;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.momo.glesdemo.gles.EGL14Wrapper;
import com.example.momo.glesdemo.gles.GLES20Drawer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView() {
        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        final SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback2() {

            long startTime = 0L;

            @Override
            public void surfaceRedrawNeeded(SurfaceHolder holder) {
                final long current = System.currentTimeMillis();
                Log.e("forTest", "surfaceRedrawNeeded time:" + (current - startTime));
                startTime = current;
            }

            private RenderThread mRenderThread;

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mRenderThread = new RenderThread(holder.getSurface());
                mRenderThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mRenderThread.exit();
            }
        });
    }

    private static class RenderThread extends Thread {

        private EGL14Wrapper mEglScreen;
        private Surface mSurface;

        private volatile boolean mRunning = true;
        private volatile long mLastTime = 0L;

        private GLES20Drawer drawer = null;

        private RenderThread(Surface surface) {
            mSurface = surface;
        }

        @Override
        public void run() {
            ensureEgl(mSurface);

            while (true) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mRunning) {
                    mEglScreen.makeCurrent();
                    render();
                    mEglScreen.swapBuffer();
                } else {
                    break;
                }
            }

            releaseEgl();

            if (drawer != null) {
                drawer.release();
                drawer = null;
            }
        }

        public void exit() {
            mRunning = false;
        }

        private void ensureEgl(Surface surface) {
            if (mEglScreen == null) {
                mEglScreen = new EGL14Wrapper();
                mEglScreen.createScreenEgl(surface);
            }
        }

        private void render() {
            final long start = SystemClock.uptimeMillis();
            Log.e("forTest", "render used time:" + (start - mLastTime));
            mLastTime = start;

            if (drawer == null) {
                drawer = new GLES20Drawer();
            }
            drawer.draw();
        }

        private void releaseEgl() {
            if (mEglScreen != null) {
                mEglScreen.release();
            }
        }
    }

}
