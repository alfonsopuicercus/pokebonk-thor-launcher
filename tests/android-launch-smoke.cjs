// Run explicitly on a test emulator after installing the debug APK. No account changes.
// Catches a launcher returning to the old browser chooser and help replacing/reloading play.
const assert = require("node:assert/strict");
const { execFileSync } = require("node:child_process");
const fs = require("node:fs");
const adb = process.env.ADB || "adb";
const serial = process.env.ANDROID_SERIAL;
assert(serial?.startsWith("emulator-"), "Set ANDROID_SERIAL to a test emulator");
const pkg = process.env.APP_PACKAGE || "com.pokebonk.thor.debug";
const call = (...args) =>
  execFileSync(adb, ["-s", serial, ...args], { encoding: "utf8", timeout: 20000 });
const shell = (...args) => call("shell", ...args);
const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
function ui() {
  const result = shell(
    "am",
    "instrument",
    "-w",
    "-e",
    "mode",
    "ui",
    "com.pokebonk.inputprobe/.InputProbe",
  );
  const json = result.match(/^INSTRUMENTATION_RESULT: ui=(.*)$/m)?.[1];
  assert(
    json,
    "Install the separate inputprobe test APK before running this smoke test: " + result,
  );
  return JSON.parse(json);
}
async function expectUI(predicate, message) {
  for (let attempt = 0; attempt < 8; attempt++) {
    const nodes = ui();
    if (predicate(nodes)) return nodes;
    await delay(150);
  }
  assert.fail(message);
}
const gameVisible = (message) =>
  expectUI((nodes) => nodes.some((node) => node.kind === "android.webkit.WebView"), message);
async function openMenu() {
  shell("input", "keyevent", "4");
  await expectUI(
    (nodes) => nodes.some((node) => node.text === "Continue playing"),
    "App menu appears",
  );
}
async function tapText(label) {
  const nodes = await expectUI(
    (nodes) => nodes.some((node) => node.text === label),
    "Visible native option: " + label,
  );
  const node = nodes.find((n) => n.text === label);
  assert(node, `Visible native option: ${label}`);
  const [x1, y1, x2, y2] = node.bounds;
  shell("input", "tap", String(Math.round((x1 + x2) / 2)), String(Math.round((y1 + y2) / 2)));
}
(async () => {
  shell("am", "force-stop", pkg);
  const component = shell(
    "cmd",
    "package",
    "resolve-activity",
    "--brief",
    "-a",
    "android.intent.action.MAIN",
    "-c",
    "android.intent.category.LAUNCHER",
    pkg,
  )
    .trim()
    .split("\n")
    .at(-1);
  assert(component.startsWith(pkg + "/"), "Installed app has a launcher entry");
  shell("am", "start", "-W", "-n", component);
  await delay(1500);
  const activities = shell("dumpsys", "activity", "activities");
  assert.match(
    activities,
    new RegExp(
      `(?:topResumedActivity|mResumedActivity)[^\n]*${pkg.replaceAll(".", "\\.")}\/(?:com\\.pokebonk\\.thor)?\\.GameActivity`,
    ),
    "The app icon must open the game directly",
  );
  await gameVisible("Game view is visible without choosing a browser");
  console.log("PASS: app icon opens the game directly");
  await openMenu();
  await tapText("Controls & sign-in");
  await expectUI(
    (nodes) => nodes.some((node) => node.text.includes("Jump / confirm")),
    "Controls help is available inside the game",
  );
  shell("input", "gamepad", "keyevent", "96");
  await gameVisible("Help returns to the game");
  console.log("PASS: help opens and returns");
  await openMenu();
  await tapText("Controller diagnostics");
  await expectUI(
    (nodes) => nodes.some((node) => node.text.includes("Deadzone: 22%")),
    "Diagnostics remain available",
  );
  await tapText("RELEASE ALL INPUTS");
  await gameVisible("Diagnostics returns to the game");
  console.log("PASS: diagnostics releases and returns");
  await openMenu();
  shell("input", "gamepad", "keyevent", "96");
  await gameVisible("Controller confirm resumes from the app menu");
  console.log("PASS: native A resumes after touch");
  await openMenu();
  shell("input", "dpad", "keyevent", "20");
  shell("input", "gamepad", "keyevent", "96");
  await expectUI(
    (nodes) => nodes.some((node) => node.text.includes("Jump / confirm")),
    "D-pad and native A open the selected help row",
  );
  shell("input", "gamepad", "keyevent", "97");
  await gameVisible("Controller Back closes help");
  await openMenu();
  await tapText("Open in browser…");
  shell("input", "gamepad", "keyevent", "97");
  await gameVisible("Controller Back cancels browser options without leaving the game");
  const report = {
    passed: true,
    package: pkg,
    checks: [
      "app icon opens game directly",
      "native WebView visible without browser chooser",
      "in-game controls help and native A return",
      "controller diagnostics and release",
      "native A resumes after touch interaction",
      "D-pad and A select help",
      "native B cancels browser options",
    ],
  };
  if (process.env.QA_REPORT)
    fs.writeFileSync(process.env.QA_REPORT, JSON.stringify(report, null, 2) + "\n");
  console.log(JSON.stringify(report, null, 2));
})().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
