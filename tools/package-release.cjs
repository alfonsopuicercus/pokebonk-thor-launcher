// Package only the signed launcher and human-readable guide. No keys or game assets.
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const root = path.resolve(__dirname, '..');
const out = path.join(root, 'dist');
fs.mkdirSync(out, { recursive: true });
const name = 'PokeBonk-Thor-Launcher-0.1.0.apk';
fs.copyFileSync(path.join(root, 'android/app/build/outputs/apk/release/app-release.apk'), path.join(out, name));
fs.copyFileSync(path.join(root, 'INSTALL.md'), path.join(out, 'INSTALL.md'));
fs.writeFileSync(path.join(out, 'SHA256SUMS.txt'), [name, 'INSTALL.md'].map(file =>
  crypto.createHash('sha256').update(fs.readFileSync(path.join(out, file))).digest('hex') + '  ' + file
).join('\n') + '\n');
console.log('Packaged signed launcher, INSTALL.md, SHA256SUMS.txt');
