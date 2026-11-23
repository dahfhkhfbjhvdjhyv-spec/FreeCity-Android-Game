package com.freecity.game;

public final class Camera3D {
    private final float[] view=new float[16];
    private final float[] projection=new float[16];
    private final float[] viewProjection=new float[16];
    public void resize(int width,int height) {
        Matrix4.perspective(projection,62f,(float)width/Math.max(1,height),0.1f,650f);
    }
    public float[] update(float x,float z,float yaw) {
        float distance=10.5f;
        float eyeX=x-(float)Math.sin(yaw)*distance;
        float eyeZ=z+(float)Math.cos(yaw)*distance;
        Matrix4.lookAt(view,eyeX,7.2f,eyeZ,x,1.3f,z,0,1,0);
        Matrix4.multiply(viewProjection,projection,view);
        return viewProjection;
    }
}
