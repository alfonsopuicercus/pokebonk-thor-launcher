/* Native Android controller frames only. No touch or pointer emulation, no account access. */
function installNativeGamepad(env=window){
  if(env.top!==env.self||env.location.protocol!=='https:'||!['pokebonk.com','www.pokebonk.com'].includes(env.location.hostname))return null;
  if(env.__PB_NATIVE__)return env.__PB_NATIVE__;
  let latest=null,frames=[],seen=-Infinity,suspended=false,overflow=false,audioWoken=false,warmup=true;
  const now=()=>env.performance.now();
  const finite=n=>Number.isFinite(n)?n:0;
  const clamp=(n,lo,hi)=>Math.max(lo,Math.min(hi,finite(n)));
  const clear=()=>{latest=null;frames=[];seen=-Infinity;warmup=true;};
  function normalize(p){if(!p||!Array.isArray(p.axes)||!Array.isArray(p.buttons))return null;return{id:'Pokebonk native Android controller',index:0,connected:true,mapping:'standard',timestamp:now(),axes:Array.from({length:4},(_,n)=>clamp(p.axes[n],-1,1)),buttons:Array.from({length:17},(_,n)=>{let value=clamp(p.buttons[n]?.value,0,1);return{value,pressed:value>=.5,touched:value>0}})};}
  function getGamepads(){if(suspended||env.document.hidden||now()-seen>500){clear();return [];}if(latest&&warmup){warmup=false;return[normalize({axes:[0,0,0,0],buttons:[]})];}return frames.length?[frames.shift()]:latest?[latest]:[];}
  Object.defineProperty(env.navigator,'getGamepads',{value:getGamepads,configurable:true});
  const bridge={update(p,discrete){if(suspended||env.document.hidden)return;const next=normalize(p);if(!next){clear();return;}const neutral=next.axes.every(n=>n===0)&&next.buttons.every(b=>!b.pressed);if(overflow){if(!neutral)return;overflow=false;clear();}
      seen=now();latest=next;if(discrete){frames.push(next);if(frames.length>32){overflow=true;clear();}}
      if(!audioWoken&&next.buttons.some(b=>b.pressed)){audioWoken=true;/* Initialize the game's audio listener; F24 has no default game action. */env.dispatchEvent(new env.KeyboardEvent('keydown',{key:'F24',code:'F24',bubbles:true}));env.dispatchEvent(new env.KeyboardEvent('keyup',{key:'F24',code:'F24',bubbles:true}));}
    },suspend(){if(suspended)return;suspended=true;overflow=false;clear();env.dispatchEvent(new env.Event('blur'));},resume(){suspended=false;overflow=false;clear();},reset(){overflow=false;clear();},replaceDevices(p){overflow=false;clear();bridge.update(p,false);},getGamepads};
  env.addEventListener('blur',()=>bridge.suspend());
  env.addEventListener('focus',()=>bridge.resume());
  env.addEventListener('pagehide',()=>bridge.suspend());
  env.addEventListener('visibilitychange',()=>env.document.hidden?bridge.suspend():bridge.resume());
  env.__PB_NATIVE__=bridge;return bridge;
}
if(typeof module==='object'&&module.exports)module.exports=installNativeGamepad;
else installNativeGamepad();
