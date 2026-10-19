package com.pokebonk.thor;

import android.app.*;
import android.content.*;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.webkit.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Official online game with direct native Android gamepad input. */
public final class GameActivity extends Activity implements InputManager.InputDeviceListener {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Map<Integer,ControllerState> controllers=new LinkedHashMap<>();
    private WebView web;
    private InputManager input;
    private boolean ready,foreground,focused;
    private long lastDebug;
    private String script,raw="No motion received yet",axisMapping="Waiting for controller motion";
    private final Runnable heartbeat=new Runnable(){public void run(){emit(false);handler.postDelayed(this,50);}};
    private static boolean official(String url){try{Uri u=Uri.parse(url);return "https".equals(u.getScheme())&&("pokebonk.com".equals(u.getHost())||"www.pokebonk.com".equals(u.getHost()));}catch(RuntimeException e){return false;}}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try(InputStream in=getAssets().open("native-gamepad.js")){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] bytes=new byte[4096];int count;while((count=in.read(bytes))!=-1)out.write(bytes,0,count);script=out.toString(StandardCharsets.UTF_8.name());}catch(IOException e){finish();return;}
        web=new WebView(this);web.setBackgroundColor(0xff182338);
        WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setSafeBrowsingEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){if(!request.isForMainFrame())return false;String url=request.getUrl().toString();if(official(url))return false;openExternal(request.getUrl());return true;}
            @Override public void onPageStarted(WebView view,String url,android.graphics.Bitmap icon){ready=false;clear();}
            @Override public void onPageFinished(WebView view,String url){if(official(url))web.evaluateJavascript(script,result->{if(official(web.getUrl())){ready=true;if(BuildConfig.DEBUG)android.util.Log.d("PokeBonkNative","page input installed");emit(false);}});}
            @Override public void onReceivedError(WebView view,WebResourceRequest request,WebResourceError error){if(request.isForMainFrame())new AlertDialog.Builder(GameActivity.this).setMessage("Could not load the official game. Check the internet connection.").setPositiveButton("Retry",(d,w)->web.loadUrl("https://pokebonk.com/?touch=0")).setNegativeButton("Close",(d,w)->finish()).show();}
        });
        web.setWebChromeClient(new WebChromeClient(){
            private View custom;private CustomViewCallback customCallback;
            @Override public void onShowCustomView(View view,CustomViewCallback callback){if(custom!=null){callback.onCustomViewHidden();return;}custom=view;customCallback=callback;setContentView(view);}
            @Override public void onHideCustomView(){if(custom==null)return;setContentView(web);custom=null;customCallback.onCustomViewHidden();immersive();}
            @Override public boolean onConsoleMessage(ConsoleMessage message){if(BuildConfig.DEBUG)android.util.Log.d("PokeBonk",message.messageLevel()+": "+message.message());return true;}
        });
        setContentView(web);web.requestFocus();immersive();input=getSystemService(InputManager.class);input.registerInputDeviceListener(this,handler);
        for(int id:input.getInputDeviceIds())onInputDeviceAdded(id);
        web.loadUrl("https://pokebonk.com/?touch=0");handler.post(heartbeat);
    }
    private void immersive(){if(web!=null)web.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);}
    private void openExternal(Uri uri){if(!Arrays.asList("https","http","mailto").contains(uri.getScheme()))return;try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(ActivityNotFoundException ignored){}}
    private static boolean source(int value){return(value&InputDevice.SOURCE_GAMEPAD)==InputDevice.SOURCE_GAMEPAD||(value&InputDevice.SOURCE_JOYSTICK)==InputDevice.SOURCE_JOYSTICK||(value&InputDevice.SOURCE_DPAD)==InputDevice.SOURCE_DPAD;}
    private ControllerState state(int id){return controllers.computeIfAbsent(id,k->new ControllerState());}
    @Override public boolean dispatchKeyEvent(KeyEvent event){
        if(source(event.getSource())&&ready&&foreground&&focused){
            if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){if(event.getAction()==KeyEvent.ACTION_UP)showMenu();return true;}
            if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()>0)return true;
            if(event.getAction()!=KeyEvent.ACTION_DOWN&&event.getAction()!=KeyEvent.ACTION_UP)return super.dispatchKeyEvent(event);
            if(state(event.getDeviceId()).key(event.getKeyCode(),event.getAction()==KeyEvent.ACTION_DOWN)){
                if(event.getAction()==KeyEvent.ACTION_DOWN&&state(event.getDeviceId()).buttonValue(8)>.5&&state(event.getDeviceId()).buttonValue(9)>.5){clear();showMenu();}
                else emit(true);return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    private static InputDevice.MotionRange range(InputDevice device,int axis){return device==null?null:device.getMotionRange(axis,InputDevice.SOURCE_JOYSTICK);}
    private static boolean signed(InputDevice d,int axis){InputDevice.MotionRange r=range(d,axis);return r!=null&&r.getMin()<0&&r.getMax()>0;}
    private static float axis(MotionEvent event,int axis){if(axis<0)return 0;InputDevice.MotionRange r=range(event.getDevice(),axis);float v=event.getAxisValue(axis);if(r!=null)return ControllerState.centered(v,r.getMin(),r.getMax(),r.getFlat());return event.getDeviceId()<0?ControllerState.centered(v,-1,1,0):0;}
    private static float trigger(MotionEvent event,int primary,int fallback){InputDevice.MotionRange r=range(event.getDevice(),primary);int axis=primary;if(r==null){r=range(event.getDevice(),fallback);axis=fallback;}return r==null?0:ControllerState.trigger(event.getAxisValue(axis),r.getMin(),r.getMax());}
    @Override public boolean dispatchGenericMotionEvent(MotionEvent event){
        if((event.getSource()&InputDevice.SOURCE_JOYSTICK)==InputDevice.SOURCE_JOYSTICK&&event.getAction()==MotionEvent.ACTION_MOVE&&ready&&foreground&&focused){
            InputDevice d=event.getDevice();int right=ControllerState.rightAxisStart(signed(d,MotionEvent.AXIS_RX),signed(d,MotionEvent.AXIS_RY),signed(d,MotionEvent.AXIS_Z),signed(d,MotionEvent.AXIS_RZ));
            if(event.getDeviceId()<0&&right<0)right=MotionEvent.AXIS_Z;
            int ry=right==MotionEvent.AXIS_RX?MotionEvent.AXIS_RY:right==MotionEvent.AXIS_Z?MotionEvent.AXIS_RZ:-1;
            ControllerState s=state(event.getDeviceId());float[] before=new float[17];for(int n=0;n<17;n++)before[n]=s.buttonValue(n);
            s.motion(axis(event,MotionEvent.AXIS_X),axis(event,MotionEvent.AXIS_Y),axis(event,right),axis(event,ry),trigger(event,MotionEvent.AXIS_LTRIGGER,MotionEvent.AXIS_BRAKE),trigger(event,MotionEvent.AXIS_RTRIGGER,MotionEvent.AXIS_GAS),event.getAxisValue(MotionEvent.AXIS_HAT_X),event.getAxisValue(MotionEvent.AXIS_HAT_Y));
            raw=String.format(Locale.ROOT,"Raw X %.3f · Y %.3f · Z %.3f · RZ %.3f · RX %.3f · RY %.3f",event.getAxisValue(0),event.getAxisValue(1),event.getAxisValue(11),event.getAxisValue(14),event.getAxisValue(12),event.getAxisValue(13));
            axisMapping=right==12?"Right stick: RX/RY":right==11?"Right stick: Z/RZ":"No signed right-stick pair found; camera axes stay neutral";
            boolean edge=false;for(int n=0;n<17;n++)if((before[n]>=.5)!=(s.buttonValue(n)>=.5))edge=true;emit(edge);return true;
        }
        return super.dispatchGenericMotionEvent(event);
    }
    private JSONObject frame(){JSONObject p=new JSONObject();JSONArray axes=new JSONArray(),buttons=new JSONArray();try{
        for(int n=0;n<4;n++){float value=0;for(ControllerState s:controllers.values())if(Math.abs(s.axis(n))>Math.abs(value))value=s.axis(n);axes.put(value);}
        for(int n=0;n<17;n++){float value=0;for(ControllerState s:controllers.values())value=Math.max(value,s.buttonValue(n));buttons.put(new JSONObject().put("value",value));}
        p.put("axes",axes).put("buttons",buttons);
    }catch(JSONException ignored){}return p;}
    private void emit(boolean discrete){if(BuildConfig.DEBUG&&SystemClock.uptimeMillis()-lastDebug>2000){lastDebug=SystemClock.uptimeMillis();android.util.Log.d("PokeBonkNative","ready="+ready+" foreground="+foreground+" focused="+focused+" official="+(web!=null&&official(web.getUrl())));}
        if(!ready||web==null||!foreground||!focused||!official(web.getUrl()))return;web.evaluateJavascript("window.__PB_NATIVE__?.update("+frame()+","+discrete+")",null);}
    private void clear(){for(ControllerState s:controllers.values())s.clear();}
    private void suspend(){clear();if(ready&&web!=null&&official(web.getUrl()))web.evaluateJavascript("window.__PB_NATIVE__?.suspend()",null);}
    private void resumeInput(){if(ready&&web!=null&&official(web.getUrl()))web.evaluateJavascript("window.__PB_NATIVE__?.resume()",r->emit(false));}
    private void showMenu(){suspend();new AlertDialog.Builder(this).setTitle("Pokébonk native controls").setMessage("Direct sticks and buttons. No touch mapping. Start + Select opens this menu.\n\n"+axisMapping+"\n"+raw+"\n\nCentered axes should read near zero. Deadzone: 22%. Return to the game with sticks released.").setPositiveButton("Continue",(d,w)->resumeInput()).setNeutralButton("Release all inputs",(d,w)->{clear();resumeInput();}).setNegativeButton("Close game",(d,w)->finish()).setOnCancelListener(d->resumeInput()).show();}
    @Override public void onBackPressed(){showMenu();}
    @Override protected void onPause(){foreground=false;suspend();if(web!=null)web.onPause();CookieManager.getInstance().flush();super.onPause();}
    @Override protected void onResume(){super.onResume();foreground=true;if(web!=null){web.onResume();immersive();resumeInput();}}
    @Override public void onWindowFocusChanged(boolean value){super.onWindowFocusChanged(value);focused=value;if(web==null)return;if(value){immersive();resumeInput();}else suspend();}
    @Override public void onInputDeviceAdded(int id){InputDevice d=InputDevice.getDevice(id);if(d!=null&&source(d.getSources())){state(id);emit(false);}}
    @Override public void onInputDeviceRemoved(int id){controllers.remove(id);if(ready&&web!=null&&official(web.getUrl()))web.evaluateJavascript("window.__PB_NATIVE__?.replaceDevices("+frame()+")",null);}
    @Override public void onInputDeviceChanged(int id){onInputDeviceRemoved(id);onInputDeviceAdded(id);}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);if(input!=null)input.unregisterInputDeviceListener(this);ready=false;if(web!=null){web.destroy();web=null;}super.onDestroy();}
}
