package com.freecity.game;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class GameSurface extends GLSurfaceView {
    public GameSurface(Context context, GameRenderer renderer) {
        super(context);
        setEGLContextClientVersion(3);
        setPreserveEGLContextOnPause(true);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
}
