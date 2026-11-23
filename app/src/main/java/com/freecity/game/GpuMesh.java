package com.freecity.game;

import android.opengl.GLES30;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class GpuMesh {
    private static final int FLOATS_PER_VERTEX=6;
    private static final int INSTANCE_FLOATS=20;
    private final int vertexBuffer;
    private final int instanceBuffer;
    private final int vertexCount;

    public GpuMesh(float[] vertices) {
        vertexCount=vertices.length/FLOATS_PER_VERTEX;
        int[] ids=new int[2];
        GLES30.glGenBuffers(2,ids,0);
        vertexBuffer=ids[0];
        instanceBuffer=ids[1];
        FloatBuffer data=direct(vertices.length);
        data.put(vertices).position(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vertexBuffer);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,vertices.length*4,data,GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);
    }

    public void drawInstanced(float[] instances,int count) {
        if(count<=0)return;
        FloatBuffer data=direct(instances.length);
        data.put(instances).position(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vertexBuffer);
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0,3,GLES30.GL_FLOAT,false,FLOATS_PER_VERTEX*4,0);
        GLES30.glEnableVertexAttribArray(1);
        GLES30.glVertexAttribPointer(1,3,GLES30.GL_FLOAT,false,FLOATS_PER_VERTEX*4,3*4);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,instanceBuffer);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,instances.length*4,data,GLES30.GL_DYNAMIC_DRAW);
        int stride=INSTANCE_FLOATS*4;
        for(int column=0;column<4;column++) {
            int location=2+column;
            GLES30.glEnableVertexAttribArray(location);
            GLES30.glVertexAttribPointer(location,4,GLES30.GL_FLOAT,false,stride,column*16);
            GLES30.glVertexAttribDivisor(location,1);
        }
        GLES30.glEnableVertexAttribArray(6);
        GLES30.glVertexAttribPointer(6,4,GLES30.GL_FLOAT,false,stride,64);
        GLES30.glVertexAttribDivisor(6,1);
        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES,0,vertexCount,count);
        for(int location=2;location<=6;location++) {
            GLES30.glVertexAttribDivisor(location,0);
            GLES30.glDisableVertexAttribArray(location);
        }
        GLES30.glDisableVertexAttribArray(0);
        GLES30.glDisableVertexAttribArray(1);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);
    }

    public void destroy() {
        GLES30.glDeleteBuffers(2,new int[]{vertexBuffer,instanceBuffer},0);
    }
    private static FloatBuffer direct(int count) {
        return ByteBuffer.allocateDirect(count*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public static float[] unitCube() {
        float[] out=new float[36*6];
        int p=0;
        float[][] faces={
            {-1,-1,-1, 1,-1,-1, 1,1,-1, -1,1,-1, 0,0,-1},
            {-1,-1,1, 1,1,1, 1,-1,1, -1,1,1, 0,0,1},
            {-1,-1,-1, -1,1,-1, -1,1,1, -1,-1,1, -1,0,0},
            {1,-1,-1, 1,-1,1, 1,1,1, 1,1,-1, 1,0,0},
            {-1,1,-1, 1,1,-1, 1,1,1, -1,1,1, 0,1,0},
            {-1,-1,-1, -1,-1,1, 1,-1,1, 1,-1,-1, 0,-1,0}
        };
        int[] order={0,2,1,0,3,2};
        for(float[] face:faces) {
            for(int index:order) {
                int base=index*3;
                out[p++]=face[base]; out[p++]=face[base+1]; out[p++]=face[base+2];
                out[p++]=face[12]; out[p++]=face[13]; out[p++]=face[14];
            }
        }
        return out;
    }
}
