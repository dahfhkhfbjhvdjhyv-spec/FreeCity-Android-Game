package com.freecity.game;

public final class Matrix4 {
    private Matrix4(){}
    public static void identity(float[] m){ for(int i=0;i<16;i++)m[i]=0; m[0]=m[5]=m[10]=m[15]=1; }
    public static void translate(float[] m,float x,float y,float z){ m[12]+=m[0]*x+m[4]*y+m[8]*z; m[13]+=m[1]*x+m[5]*y+m[9]*z; m[14]+=m[2]*x+m[6]*y+m[10]*z; m[15]+=m[3]*x+m[7]*y+m[11]*z; }
    public static void scale(float[] m,float x,float y,float z){ for(int i=0;i<4;i++){m[i]*=x;m[4+i]*=y;m[8+i]*=z;} }
    public static void multiply(float[] out,float[] a,float[] b){
        float[] t=new float[16];
        for(int c=0;c<4;c++) for(int r=0;r<4;r++) t[c*4+r]=a[r]*b[c*4]+a[4+r]*b[c*4+1]+a[8+r]*b[c*4+2]+a[12+r]*b[c*4+3];
        System.arraycopy(t,0,out,0,16);
    }
    public static void perspective(float[] m,float fovy,float aspect,float near,float far){
        for(int i=0;i<16;i++)m[i]=0;
        float f=1f/(float)Math.tan(Math.toRadians(fovy)/2);
        m[0]=f/aspect;m[5]=f;m[10]=(far+near)/(near-far);m[11]=-1;m[14]=(2*far*near)/(near-far);
    }
    public static void lookAt(float[] m,float ex,float ey,float ez,float cx,float cy,float cz,float ux,float uy,float uz){
        float fx=cx-ex,fy=cy-ey,fz=cz-ez; float fl=(float)Math.sqrt(fx*fx+fy*fy+fz*fz); fx/=fl;fy/=fl;fz/=fl;
        float sx=fy*uz-fz*uy,sy=fz*ux-fx*uz,sz=fx*uy-fy*ux; float sl=(float)Math.sqrt(sx*sx+sy*sy+sz*sz); sx/=sl;sy/=sl;sz/=sl;
        float vx=sy*fz-sz*fy,vy=sz*fx-sx*fz,vz=sx*fy-sy*fx;
        m[0]=sx;m[1]=vx;m[2]=-fx;m[3]=0;
        m[4]=sy;m[5]=vy;m[6]=-fy;m[7]=0;
        m[8]=sz;m[9]=vz;m[10]=-fz;m[11]=0;
        m[12]=-(sx*ex+sy*ey+sz*ez);m[13]=-(vx*ex+vy*ey+vz*ez);m[14]=(fx*ex+fy*ey+fz*ez);m[15]=1;
    }
}
