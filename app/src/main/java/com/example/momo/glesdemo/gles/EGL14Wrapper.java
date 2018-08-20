package com.example.momo.glesdemo.gles;

import android.opengl.EGL14;
import android.opengl.EGLDisplay;
import android.opengl.GLUtils;

/**
 * @author feng
 * @since 2018/8/20.
 */

public class EGL14Wrapper {

    public void createScreenEgl() {
        // 第一步：
        final EGLDisplay mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY || EGL14.eglGetError() != EGL14.EGL_SUCCESS){
            throw new RuntimeException("eglGetDisplay, failed " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }

        // 第二步：
        final int[] versions = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, versions, 0, versions, 1)) {
            throw new RuntimeException("eglInitialize, failed " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }

        // 第三步：


    }

}
