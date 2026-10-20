/* Native Android controller frames only. No touch or pointer emulation, no account access. */
function installNativeGamepad(env = window) {
  if (
    env.top !== env.self ||
    env.location.protocol !== "https:" ||
    !["pokebonk.com", "www.pokebonk.com"].includes(env.location.hostname)
  )
    return null;
  if (env.__PB_NATIVE__) return env.__PB_NATIVE__;
  // Sizes follow the W3C standard Gamepad layout, also used by ControllerState.java.
  const STANDARD_AXIS_COUNT = 4;
  const STANDARD_BUTTON_COUNT = 17;
  const BUTTON_PRESSED_THRESHOLD = 0.5;
  const INPUT_TIMEOUT_MS = 500;
  const MAX_QUEUED_FRAMES = 32;

  let latestFrame = null;
  let pendingFrames = [];
  let lastUpdateTime = -Infinity;
  let suspended = false;
  let awaitingNeutralFrame = false;
  let audioInitialized = false;
  let needsNeutralPoll = true;
  const now = () => env.performance.now();
  const finite = (n) => (Number.isFinite(n) ? n : 0);
  const clamp = (n, lo, hi) => Math.max(lo, Math.min(hi, finite(n)));
  const clear = () => {
    latestFrame = null;
    pendingFrames = [];
    lastUpdateTime = -Infinity;
    needsNeutralPoll = true;
  };
  function normalize(frame) {
    if (!frame || !Array.isArray(frame.axes) || !Array.isArray(frame.buttons)) return null;
    return {
      id: "Pokebonk native Android controller",
      index: 0,
      connected: true,
      mapping: "standard",
      timestamp: now(),
      axes: Array.from({ length: STANDARD_AXIS_COUNT }, (_, n) => clamp(frame.axes[n], -1, 1)),
      buttons: Array.from({ length: STANDARD_BUTTON_COUNT }, (_, n) => {
        let value = clamp(frame.buttons[n]?.value, 0, 1);
        return { value, pressed: value >= BUTTON_PRESSED_THRESHOLD, touched: value > 0 };
      }),
    };
  }
  function getGamepads() {
    if (suspended || env.document.hidden || now() - lastUpdateTime > INPUT_TIMEOUT_MS) {
      clear();
      return [];
    }
    if (latestFrame && needsNeutralPoll) {
      // The game calibrates on first contact, so it must see a neutral sample first.
      needsNeutralPoll = false;
      return [normalize({ axes: [0, 0, 0, 0], buttons: [] })];
    }
    return pendingFrames.length ? [pendingFrames.shift()] : latestFrame ? [latestFrame] : [];
  }
  Object.defineProperty(env.navigator, "getGamepads", { value: getGamepads, configurable: true });
  const bridge = {
    update(frame, discrete) {
      if (suspended || env.document.hidden) return;
      const next = normalize(frame);
      if (!next) {
        clear();
        return;
      }
      const neutral = next.axes.every((n) => n === 0) && next.buttons.every((b) => !b.pressed);
      if (awaitingNeutralFrame) {
        if (!neutral) return;
        awaitingNeutralFrame = false;
        clear();
      }
      lastUpdateTime = now();
      latestFrame = next;
      if (discrete) {
        // Preserve quick press/release edges that arrive between game render frames.
        pendingFrames.push(next);
        if (pendingFrames.length > MAX_QUEUED_FRAMES) {
          awaitingNeutralFrame = true;
          clear();
        }
      }
      if (!audioInitialized && next.buttons.some((b) => b.pressed)) {
        audioInitialized = true;
        // Initialize the game's audio listener; F24 has no default game action.
        env.dispatchEvent(
          new env.KeyboardEvent("keydown", { key: "F24", code: "F24", bubbles: true }),
        );
        env.dispatchEvent(
          new env.KeyboardEvent("keyup", { key: "F24", code: "F24", bubbles: true }),
        );
      }
    },
    suspend() {
      if (suspended) return;
      suspended = true;
      awaitingNeutralFrame = false;
      clear();
      // The game may stop polling while hidden; blur releases its own held actions immediately.
      env.dispatchEvent(new env.Event("blur"));
    },
    resume() {
      suspended = false;
      awaitingNeutralFrame = false;
      clear();
    },
    reset() {
      awaitingNeutralFrame = false;
      clear();
    },
    replaceDevices(frame) {
      awaitingNeutralFrame = false;
      clear();
      bridge.update(frame, false);
    },
    getGamepads,
  };
  env.addEventListener("blur", () => bridge.suspend());
  env.addEventListener("focus", () => bridge.resume());
  env.addEventListener("pagehide", () => bridge.suspend());
  env.addEventListener("visibilitychange", () =>
    env.document.hidden ? bridge.suspend() : bridge.resume(),
  );
  env.__PB_NATIVE__ = bridge;
  return bridge;
}
if (typeof module === "object" && module.exports) module.exports = installNativeGamepad;
else installNativeGamepad();
