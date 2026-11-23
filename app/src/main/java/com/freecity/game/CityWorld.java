package com.freecity.game;

import java.util.ArrayList;
import java.util.List;

public final class CityWorld {
    private final float[] staticInstances;
    private final int staticCount;
    public CityWorld() {
        List<Float> data=new ArrayList<>();
        add(data,0,-0.42f,0,260,0.35f,260,0.20f,0.39f,0.20f);
        for(int road=-5;road<=5;road++) {
            add(data,road*38f,-0.035f,0,6.8f,0.035f,250,0.105f,0.115f,0.13f);
            add(data,0,-0.032f,road*38f,250,0.035f,6.8f,0.105f,0.115f,0.13f);
            add(data,road*38f,-0.012f,0,0.12f,0.012f,250,0.82f,0.72f,0.35f);
            add(data,0,-0.010f,road*38f,250,0.012f,0.12f,0.82f,0.72f,0.35f);
        }
        for(int x=-5;x<=5;x++) for(int z=-5;z<=5;z++) {
            float bx=x*38f+((x+z)%2==0?12f:-12f);
            float bz=z*38f+((x-z)%2==0?12f:-12f);
            float height=10f+Math.abs((x*17+z*11)%27);
            float shade=0.28f+((x*7+z*3)&7)*0.045f;
            add(data,bx,height*0.5f,bz,7.8f,height*0.5f,7.8f,shade,shade+0.07f,shade+0.13f);
            add(data,bx,height+0.16f,bz,8.0f,0.16f,8.0f,0.13f,0.15f,0.18f);
            add(data,bx,height*0.52f,bz-3.94f,2.1f,height*0.22f,0.035f,0.12f,0.34f,0.48f);
        }
        staticCount=data.size()/20;
        staticInstances=new float[data.size()];
        for(int i=0;i<data.size();i++)staticInstances[i]=data.get(i);
    }
    public float[] createFrameInstances(float playerX,float playerZ,float yaw) {
        float[] frame=new float[staticInstances.length+5*20];
        System.arraycopy(staticInstances,0,frame,0,staticInstances.length);
        int p=staticInstances.length;
        p=put(frame,p,playerX,1.0f,playerZ,0.48f,0.82f,0.34f,0.08f,0.30f,0.92f);
        p=put(frame,p,playerX,1.9f,playerZ,0.32f,0.28f,0.30f,0.82f,0.63f,0.44f);
        float carX=playerX+(float)Math.cos(yaw)*5f;
        float carZ=playerZ-(float)Math.sin(yaw)*5f;
        p=put(frame,p,carX,0.62f,carZ,1.25f,0.42f,2.05f,0.78f,0.10f,0.07f);
        p=put(frame,p,carX,1.12f,carZ,0.88f,0.34f,0.95f,0.13f,0.22f,0.29f);
        p=put(frame,p,carX,0.28f,carZ,1.31f,0.12f,2.1f,0.08f,0.08f,0.09f);
        return frame;
    }
    public int instanceCount(){return staticCount+5;}
    private static void add(List<Float> out,float x,float y,float z,float sx,float sy,float sz,float r,float g,float b) {
        float[] one=new float[20]; put(one,0,x,y,z,sx,sy,sz,r,g,b);
        for(float value:one)out.add(value);
    }
    private static int put(float[] out,int offset,float x,float y,float z,float sx,float sy,float sz,float r,float g,float b) {
        float[] m=new float[16]; Matrix4.identity(m); Matrix4.translate(m,x,y,z); Matrix4.scale(m,sx,sy,sz);
        System.arraycopy(m,0,out,offset,16);
        out[offset+16]=r;out[offset+17]=g;out[offset+18]=b;out[offset+19]=1;
        return offset+20;
    }
}
