package com.example.momo.glesdemo.gles;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * @author shidefeng
 * @since 2018/8/20.
 */
public class EGL14Wrapper {

    private EGLDisplay mEglDisplay;
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;
    private EGLSurface mEglSurface;

    public void createScreenEgl(Object surface) {
        // 第一步：关联显示设备，mEglDisplay相当于显示器的句柄
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        Log.i("forTest", "eglDisplay:" + mEglDisplay);

        // 判断系统中是否有可用的 native display ID 与给定的 display 参数匹配
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY || EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
            throw new RuntimeException("eglGetDisplay, failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }

        // 第二步：每个 EGLDisplay 在使用前都需要初始化。初始化当前的Display
        // 初始化 EGLDisplay 的同时，你可以得到系统中 EGL 的实现版本号。
        final int[] version = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("eglInitialize, failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
        Log.i("forTest", "version:" + version[0] + "," + version[1]);

        // 第三步：获取、配置config
        final int configsCount[] = new int[1];
        final EGLConfig configs[] = new EGLConfig[1];
        int configSpec[] = /*isAlpha ? new int[]{
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 16,
                EGL14.EGL_STENCIL_SIZE, 0,
                EGL14.EGL_NONE
        } : */{
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 0,
                EGL14.EGL_STENCIL_SIZE, 0,
                EGL14.EGL_NONE
        };
        // attrib_list：选择配置时需要参照的属性
        // configs：返回一个按照 attrib_list 排序的平台有效的所有 EGL framebuffer 配置列表
        // config_size：手动设置应返回的配置个数
        // num_config：实际匹配的配置总数
        if (!EGL14.eglChooseConfig(mEglDisplay, configSpec, 0, configs, 0, 1, configsCount, 0)
                || configsCount[0] <= 0) {
            throw new RuntimeException("eglChooseConfig, failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }

        mEglConfig = configs[0];
        Log.i("forTest", "eglChooseConfig:" + mEglConfig);

        // 第四步：构建surface
        final int[] surface_attr = {
                EGL14.EGL_NONE
        };
        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, surface_attr, 0);
        if (mEglSurface == null || mEglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("eglCreateWindowSurface, failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }

        Log.i("forTest", "mEglSurface:" + mEglSurface);

        // 第五步：创建Context
        final int[] context_attr = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, EGL14.EGL_NO_CONTEXT, context_attr, 0);
        if (mEglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("eglCreateContext, failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
    }

    public boolean makeCurrent() {
        if (mEglDisplay != null && mEglSurface != null && mEglContext != null) {
            if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                throw new RuntimeException("eglMakeCurrent, failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean makeCurrent(EGLSurface readSurface) {
        if (mEglDisplay != null && mEglSurface != null && mEglContext != null) {
            if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, readSurface, mEglContext)) {
                throw new RuntimeException("eglMakeCurrent, failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            }
            return true;
        } else {
            return false;
        }
    }

    public void swapBuffer() {
        if (mEglDisplay != null && mEglSurface != null) {
            if (!EGL14.eglSwapBuffers(mEglDisplay, mEglSurface)) {
                throw new RuntimeException("eglSwapBuffers,failed!");
            }
        }
    }

    public void release() {
        // null everything out so future attempts to use this object will cause an NPE
        if (mEglDisplay != null && mEglSurface != null && mEglContext != null) {
            EGL14.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext);
            EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
            EGL14.eglDestroyContext(mEglDisplay, mEglContext);
            EGL14.eglTerminate(mEglDisplay);
        }
        EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
    }

}
