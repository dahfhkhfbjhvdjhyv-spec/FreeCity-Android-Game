package com.freecity.game;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {
    private int program, vbo, mvpLoc, colorLoc;
    private final float[] projection=new float[16], view=new float[16], model=new float[16], vp=new float[16], mvp=new float[16];
    private volatile float moveX, moveY, look;
    private float px=0, pz=0, yaw=0, lastTime;
    private static final float[] CUBE = {
        -1,-1,-1, 1,-1,-1, 1,1,-1,  -1,-1,-1, 1,1,-1, -1,1,-1,
        -1,-1,1, 1,1,1, 1,-1,1,  -1,-1,1, -1,1,1, 1,1,1,
        -1,-1,-1, -1,1,-1, -1,1,1,  -1,-1,-1, -1,1,1, -1,-1,1,
        1,-1,-1, 1,-1,1, 1,1,1,  1,-1,-1, 1,1,1, 1,1,-1,
        -1,1,-1, 1,1,-1, 1,1,1,  -1,1,-1, 1,1,1, -1,1,1,
        -1,-1,-1, 1,-1,1, 1,-1,-1,  -1,-1,-1, -1,-1,1, 1,-1,1
    };

    public void setMove(float x,float y){ moveX=x; moveY=y; }
    public void addLook(float v){ look+=v; }

    @Override public void onSurfaceCreated(GL10 gl,EGLConfig config) {
        String vs="#version 300 es\nlayout(location=0) in vec3 aPos; uniform mat4 uMVP; void main(){ gl_Position=uMVP*vec4(aPos,1.0); }";
        String fs="#version 300 es\nprecision mediump float; uniform vec4 uColor; out vec4 fragColor; void main(){ fragColor=uColor; }";
        int v=shader(GLES30.GL_VERTEX_SHADER,vs), f=shader(GLES30.GL_FRAGMENT_SHADER,fs);
        program=GLES30.glCreateProgram(); GLES30.glAttachShader(program,v); GLES30.glAttachShader(program,f); GLES30.glLinkProgram(program);
        int[] ok=new int[1]; GLES30.glGetProgramiv(program,GLES30.GL_LINK_STATUS,ok,0);
        if(ok[0]==0) throw new RuntimeException("Shader link failed: "+GLES30.glGetProgramInfoLog(program));
        mvpLoc=GLES30.glGetUniformLocation(program,"uMVP"); colorLoc=GLES30.glGetUniformLocation(program,"uColor");
        int[] b=new int[1]; GLES30.glGenBuffers(1,b,0); vbo=b[0];
        FloatBuffer fb=ByteBuffer.allocateDirect(CUBE.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer(); fb.put(CUBE).position(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vbo); GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,CUBE.length*4,fb,GLES30.GL_STATIC_DRAW);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST); GLES30.glClearColor(0.42f,0.68f,0.88f,1);
        lastTime=System.nanoTime()/1e9f;
    }
    private int shader(int type,String src){ int s=GLES30.glCreateShader(type); GLES30.glShaderSource(s,src); GLES30.glCompileShader(s); int[] ok=new int[1]; GLES30.glGetShaderiv(s,GLES30.GL_COMPILE_STATUS,ok,0); if(ok[0]==0) throw new RuntimeException(GLES30.glGetShaderInfoLog(s)); return s; }
    @Override public void onSurfaceChanged(GL10 gl,int w,int h){ GLES30.glViewport(0,0,w,h); Matrix4.perspective(projection,60f,(float)w/Math.max(1,h),0.1f,500f); }
    @Override public void onDrawFrame(GL10 gl) {
        float now=System.nanoTime()/1e9f, dt=Math.min(0.04f,now-lastTime); lastTime=now;
        yaw += look; look=0;
        float speed=10f;
        px += (float)Math.sin(yaw)*(-moveY)*speed*dt + (float)Math.cos(yaw)*moveX*speed*dt;
        pz += (float)Math.cos(yaw)*(-moveY)*speed*dt - (float)Math.sin(yaw)*moveX*speed*dt;
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT|GLES30.GL_DEPTH_BUFFER_BIT);
        Matrix4.lookAt(view,px-(float)Math.sin(yaw)*11f,8f,pz+(float)Math.cos(yaw)*11f,px,1.2f,pz,0,1,0);
        Matrix4.multiply(vp,projection,view);
        GLES30.glUseProgram(program); GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vbo);
        GLES30.glEnableVertexAttribArray(0); GLES30.glVertexAttribPointer(0,3,GLES30.GL_FLOAT,false,12,0);
        box(0,-0.35f,0,180,0.3f,180,0.20f,0.43f,0.20f);
        // City blocks: a connected road grid and varied-height buildings.
        for(int i=-4;i<=4;i++){
            box(i*42f,0.01f,0,7f,0.04f,190f,0.13f,0.14f,0.17f);
            box(0,0.02f,i*42f,190f,0.04f,7f,0.13f,0.14f,0.17f);
            for(int j=-4;j<=4;j++){
                float bx=i*42f+((i+j)%2==0?13f:-13f), bz=j*42f+((i-j)%2==0?13f:-13f);
                float height=9f+Math.abs((i*17+j*11)%23);
                float shade=0.32f+((i*7+j*3)&7)*0.045f;
                box(bx,height*0.5f,bz,7f,height*0.5f,7f,shade,shade+0.08f,shade+0.14f);
                box(bx,height+0.18f,bz,7.2f,0.18f,7.2f,0.18f,0.20f,0.23f);
            }
        }
        // Player avatar and a parked car.
        box(px,1.05f,pz,0.48f,0.85f,0.34f,0.10f,0.34f,0.92f);
        box(px,2.05f,pz,0.34f,0.30f,0.32f,0.82f,0.64f,0.48f);
        float cx=px+5f, cz=pz+3f;
        box(cx,0.65f,cz,1.25f,0.45f,2.1f,0.78f,0.12f,0.08f);
        box(cx,1.2f,cz,0.92f,0.38f,1.0f,0.16f,0.22f,0.28f);
        GLES30.glDisableVertexAttribArray(0);
    }
    private void box(float x,float y,float z,float sx,float sy,float sz,float r,float g,float b){
        Matrix4.identity(model); Matrix4.translate(model,x,y,z); Matrix4.scale(model,sx,sy,sz); Matrix4.multiply(mvp,vp,model);
        GLES30.glUniformMatrix4fv(mvpLoc,1,false,mvp,0); GLES30.glUniform4f(colorLoc,r,g,b,1); GLES30.glDrawArrays(GLES30.GL_TRIANGLES,0,36);
    }
}
