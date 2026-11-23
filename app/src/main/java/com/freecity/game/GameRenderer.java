package com.freecity.game;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {
    private static final String VERTEX_SHADER =
        "#version 300 es\n"+
        "layout(location=0) in vec3 aPosition;\n"+
        "layout(location=1) in vec3 aNormal;\n"+
        "layout(location=2) in vec4 iModel0;\n"+
        "layout(location=3) in vec4 iModel1;\n"+
        "layout(location=4) in vec4 iModel2;\n"+
        "layout(location=5) in vec4 iModel3;\n"+
        "layout(location=6) in vec4 iColor;\n"+
        "uniform mat4 uViewProjection;\n"+
        "out vec3 vNormal; out vec3 vColor; out vec3 vWorld;\n"+
        "void main(){ mat4 model=mat4(iModel0,iModel1,iModel2,iModel3); vec4 world=model*vec4(aPosition,1.0); vWorld=world.xyz; vNormal=normalize(mat3(model)*aNormal); vColor=iColor.rgb; gl_Position=uViewProjection*world; }";
    private static final String FRAGMENT_SHADER =
        "#version 300 es\nprecision highp float; in vec3 vNormal; in vec3 vColor; in vec3 vWorld; out vec4 fragColor;\n"+
        "uniform vec3 uSunDirection; uniform vec3 uCameraPosition;\n"+
        "void main(){ vec3 n=normalize(vNormal); float diffuse=max(dot(n,normalize(-uSunDirection)),0.0); float hemi=0.5+0.5*n.y; float light=0.30+diffuse*0.62+hemi*0.08; vec3 color=vColor*light; float fog=clamp((length(uCameraPosition-vWorld)-100.0)/360.0,0.0,0.55); color=mix(color,vec3(0.58,0.70,0.79),fog); fragColor=vec4(color,1.0); }";

    private final Camera3D camera=new Camera3D();
    private final CityWorld city=new CityWorld();
    private GpuMesh cube;
    private ShaderProgram shader;
    private int vpLocation,sunLocation,cameraLocation;
    private volatile float moveX,moveY,look;
    private float playerX,playerZ,yaw,lastTime;

    public void setMove(float x,float y){moveX=x;moveY=y;}
    public void addLook(float amount){look+=amount;}

    @Override public void onSurfaceCreated(GL10 gl,EGLConfig config) {
        shader=new ShaderProgram(VERTEX_SHADER,FRAGMENT_SHADER);
        cube=new GpuMesh(GpuMesh.unitCube());
        vpLocation=shader.uniform("uViewProjection");
        sunLocation=shader.uniform("uSunDirection");
        cameraLocation=shader.uniform("uCameraPosition");
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glCullFace(GLES30.GL_BACK);
        GLES30.glClearColor(0.52f,0.72f,0.88f,1f);
        lastTime=System.nanoTime()/1.0e9f;
    }
    @Override public void onSurfaceChanged(GL10 gl,int width,int height) {
        GLES30.glViewport(0,0,width,height);
        camera.resize(width,height);
    }
    @Override public void onDrawFrame(GL10 gl) {
        float now=System.nanoTime()/1.0e9f;
        float dt=Math.min(0.04f,Math.max(0f,now-lastTime));
        lastTime=now;
        yaw+=look;look=0;
        float speed=9.0f;
        playerX+=(float)Math.sin(yaw)*moveY*speed*dt+(float)Math.cos(yaw)*moveX*speed*dt;
        playerZ+=(float)Math.cos(yaw)*moveY*speed*dt-(float)Math.sin(yaw)*moveX*speed*dt;
        float[] vp=camera.update(playerX,playerZ,yaw);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT|GLES30.GL_DEPTH_BUFFER_BIT);
        shader.use();
        GLES30.glUniformMatrix4fv(vpLocation,1,false,vp,0);
        GLES30.glUniform3f(sunLocation,-0.45f,-1.0f,-0.28f);
        GLES30.glUniform3f(cameraLocation,playerX-(float)Math.sin(yaw)*10.5f,7.2f,playerZ+(float)Math.cos(yaw)*10.5f);
        cube.drawInstanced(city.createFrameInstances(playerX,playerZ,yaw),city.instanceCount());
    }
    @Override public void onSurfaceDestroyed() {
        if(cube!=null)cube.destroy();
        if(shader!=null)shader.destroy();
    }
}
