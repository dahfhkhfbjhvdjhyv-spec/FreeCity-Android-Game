package com.freecity.game;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.view.MotionEvent;

public class MainActivity extends Activity {
    private GameRenderer renderer;
    private GameSurface surface;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        renderer = new GameRenderer();
        surface = new GameSurface(this, renderer);
        FrameLayout root = new FrameLayout(this);
        root.addView(surface);
        root.addView(new TouchHud());
        setContentView(root);
    }

    @Override protected void onPause() { super.onPause(); if (surface != null) surface.onPause(); }
    @Override protected void onResume() { super.onResume(); if (surface != null) surface.onResume(); }

    private class TouchHud extends View {
        private final Paint p = new Paint(3);
        private float joyX, joyY, camX;
        private int joyPointer = -1, camPointer = -1;
        TouchHud() { super(MainActivity.this); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w=getWidth(), h=getHeight(), baseX=105, baseY=h-105;
            p.setColor(0x55333333); c.drawCircle(baseX,baseY,72,p);
            p.setColor(0x99ffffff); c.drawCircle(baseX+joyX*42,baseY+joyY*42,27,p);
            p.setColor(0x55333333); c.drawCircle(w-105,h-105,58,p);
            p.setColor(0x99ffffff); c.drawCircle(w-105,h-105,25,p);
            p.setColor(Color.WHITE); p.setTextSize(17); c.drawText("FREECITY  •  OPEN WORLD PROTOTYPE",24,32,p);
            p.setTextSize(13); c.drawText("LEFT: MOVE     RIGHT: LOOK",24,54,p);
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            float x=e.getX(), y=e.getY(), w=getWidth(), h=getHeight();
            int action=e.getActionMasked(), index=e.getActionIndex(), id=e.getPointerId(index);
            if(action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_POINTER_DOWN) {
                if(x < w*0.48f) joyPointer=id; else camPointer=id;
            }
            if(action==MotionEvent.ACTION_MOVE) {
                for(int i=0;i<e.getPointerCount();i++) {
                    int pid=e.getPointerId(i); float px=e.getX(i), py=e.getY(i);
                    if(pid==joyPointer) {
                        joyX=Math.max(-1,Math.min(1,(px-105)/72));
                        joyY=Math.max(-1,Math.min(1,(py-(h-105))/72));
                        renderer.setMove(joyX,joyY);
                    } else if(pid==camPointer) {
                        camX=px; renderer.addLook((px-camX)*0.004f);
                    }
                }
                invalidate(); return true;
            }
            if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_POINTER_UP || action==MotionEvent.ACTION_CANCEL) {
                if(id==joyPointer) { joyPointer=-1; joyX=joyY=0; renderer.setMove(0,0); }
                if(id==camPointer) camPointer=-1;
                invalidate(); return true;
            }
            return true;
        }
    }
}
