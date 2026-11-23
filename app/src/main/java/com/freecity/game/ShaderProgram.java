package com.freecity.game;

import android.opengl.GLES30;

public final class ShaderProgram {
    private final int handle;
    public ShaderProgram(String vertexSource,String fragmentSource) {
        int vertex=compile(GLES30.GL_VERTEX_SHADER,vertexSource);
        int fragment=compile(GLES30.GL_FRAGMENT_SHADER,fragmentSource);
        handle=GLES30.glCreateProgram();
        GLES30.glAttachShader(handle,vertex);
        GLES30.glAttachShader(handle,fragment);
        GLES30.glLinkProgram(handle);
        int[] status=new int[1];
        GLES30.glGetProgramiv(handle,GLES30.GL_LINK_STATUS,status,0);
        GLES30.glDeleteShader(vertex);
        GLES30.glDeleteShader(fragment);
        if(status[0]==0) {
            String log=GLES30.glGetProgramInfoLog(handle);
            GLES30.glDeleteProgram(handle);
            throw new IllegalStateException("FreeCity shader link failed: "+log);
        }
    }
    private static int compile(int type,String source) {
        int shader=GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader,source);
        GLES30.glCompileShader(shader);
        int[] status=new int[1];
        GLES30.glGetShaderiv(shader,GLES30.GL_COMPILE_STATUS,status,0);
        if(status[0]==0) {
            String log=GLES30.glGetShaderInfoLog(shader);
            GLES30.glDeleteShader(shader);
            throw new IllegalStateException("FreeCity shader compile failed: "+log);
        }
        return shader;
    }
    public void use(){ GLES30.glUseProgram(handle); }
    public int uniform(String name){ return GLES30.glGetUniformLocation(handle,name); }
    public void destroy(){ GLES30.glDeleteProgram(handle); }
}
