package com.pokebonk.inputprobe;
import android.app.Instrumentation;
import android.os.*;
import android.view.*;
/** Test APK only: inject genuine Android events into the foreground game Activity. */
public final class InputProbe extends Instrumentation {
    private Bundle args;
    @Override public void onCreate(Bundle bundle){args=bundle;start();}
    @Override public void onStart(){Bundle result=new Bundle();try{
        long now=SystemClock.uptimeMillis();MotionEvent.PointerProperties pp=new MotionEvent.PointerProperties();pp.id=0;
        MotionEvent.PointerCoords coords=new MotionEvent.PointerCoords();String[] values=args.getString("axes","0,0,0,0").split(",");
        int[] axes={MotionEvent.AXIS_X,MotionEvent.AXIS_Y,MotionEvent.AXIS_Z,MotionEvent.AXIS_RZ};for(int n=0;n<Math.min(4,values.length);n++)coords.setAxisValue(axes[n],Float.parseFloat(values[n]));
        MotionEvent e=MotionEvent.obtain(now,now,MotionEvent.ACTION_MOVE,1,new MotionEvent.PointerProperties[]{pp},new MotionEvent.PointerCoords[]{coords},0,0,1,1,-1,0,InputDevice.SOURCE_JOYSTICK,0);
        result.putBoolean("injected",getUiAutomation().injectInputEvent(e,true));e.recycle();finish(0,result);
    }catch(Exception e){result.putString("error",e.toString());finish(1,result);}}
}
