package com.pokebonk.thor;
import java.util.Arrays;
/** Fixed Android layout; centers come from declared ranges, never the first/held sample. */
public final class ControllerState {
    private final boolean[] digital=new boolean[17];
    private final float[] axes=new float[4];
    private float lt,rt,hx,hy;
    private static final int[] CODES={96,97,99,100,102,103,104,105,109,108,106,107,19,20,21,22,110};
    public boolean key(int code,boolean down){for(int n=0;n<CODES.length;n++)if(CODES[n]==code){digital[n]=down;return true;}return false;}
    private static float finite(float n){return Float.isFinite(n)?n:0;}
    private static float clamp(float n,float low,float high){return Math.max(low,Math.min(high,finite(n)));}
    private static float stick(float n){n=clamp(n,-1,1);return Math.abs(n)<=.22f?0:n;}
    public void motion(float x,float y,float rx,float ry,float left,float right,float hatX,float hatY){axes[0]=stick(x);axes[1]=stick(y);axes[2]=stick(rx);axes[3]=stick(ry);lt=clamp(left,0,1);rt=clamp(right,0,1);hx=clamp(hatX,-1,1);hy=clamp(hatY,-1,1);}
    public float axis(int n){return axes[n];}
    public float buttonValue(int n){float analog=switch(n){case 6->lt;case 7->rt;case 12->hy<-.5f?1:0;case 13->hy>.5f?1:0;case 14->hx<-.5f?1:0;case 15->hx>.5f?1:0;default->0;};return Math.max(digital[n]?1:0,analog);}
    public void clear(){Arrays.fill(digital,false);Arrays.fill(axes,0);lt=rt=hx=hy=0;}
    public static float centered(float value,float min,float max,float flat){if(!Float.isFinite(value)||!Float.isFinite(min)||!Float.isFinite(max)||max<=min)return 0;float mid=(min+max)/2,half=(max-min)/2;return Math.abs(value-mid)<=Math.max(0,flat)?0:clamp((value-mid)/half,-1,1);}
    public static float trigger(float value,float min,float max){return max>min&&Float.isFinite(value)?clamp((value-min)/(max-min),0,1):0;}
    public static int rightAxisStart(boolean rx,boolean ry,boolean z,boolean rz){return rx&&ry?12:z&&rz?11:-1;}
}
