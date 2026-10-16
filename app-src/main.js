// Ultima Zona · app per PC (Electron)
// - il gioco gira dentro l'app (niente browser), a schermo intero, con la memoria del PC
// - aggiornamenti automatici: l'app scarica da sola la nuova versione dal sito del gioco e la usa al prossimo avvio
// - sicurezza: ogni versione scaricata viene controllata (impronta SHA-256); se non parte, l'app torna da sola
//   all'ultima versione che funzionava; dentro l'app c'è sempre una copia del gioco che funziona anche senza internet
// - i salvataggi (impostazioni, nome, preset, progressi) restano nel profilo dell'app e non si toccano con gli aggiornamenti
const { app, BrowserWindow, protocol, ipcMain, net, shell, dialog } = require('electron');
const path = require('path'), fs = require('fs'), crypto = require('crypto');
const UPDATE_URL = process.env.UZ_UPDATE_URL || 'https://pafo99598-art.github.io/ultima-zona/app/';
const BOOT_TIMEOUT = +process.env.UZ_BOOT_MS || 120000; // se il gioco non dice "pronto" entro 2 minuti, si torna alla versione precedente
protocol.registerSchemesAsPrivileged([{ scheme: 'uz', privileges: { standard: true, secure: true, supportFetchAPI: true, stream: true, corsEnabled: true } }]);
app.commandLine.appendSwitch('enable-gpu-rasterization');
app.commandLine.appendSwitch('ignore-gpu-blocklist');
app.commandLine.appendSwitch('force_high_performance_gpu');
app.commandLine.appendSwitch('js-flags', '--max-old-space-size=8192');
const DATA = () => app.getPath('userData');
const VDIR = () => path.join(DATA(), 'versioni');
const STATE = () => path.join(DATA(), 'stato-versioni.json');
const BUNDLED = path.join(__dirname, 'game');
function readJSON(f, d) { try { return JSON.parse(fs.readFileSync(f, 'utf8')); } catch (e) { return d; } }
function writeJSON(f, o) { fs.mkdirSync(path.dirname(f), { recursive: true }); fs.writeFileSync(f + '.tmp', JSON.stringify(o, null, 1)); fs.renameSync(f + '.tmp', f); }
function cmpVer(a, b) { const x = String(a).split('.').map(Number), y = String(b).split('.').map(Number); for (let i = 0; i < 3; i++) { if ((x[i] || 0) !== (y[i] || 0)) return (x[i] || 0) - (y[i] || 0); } return 0; }
const bundledVersion = readJSON(path.join(BUNDLED, 'version.json'), { version: '0.0.0' }).version;
let state = { current: null, good: null, bad: [], log: [] };
function log(t) { state.log = (state.log || []).slice(-40); state.log.push(new Date().toISOString() + ' ' + t); try { writeJSON(STATE(), state); } catch (e) {} }
function dirOf(v) { return v && v !== 'bundled' ? path.join(VDIR(), v) : BUNDLED; }
function usable(v) { return v && (v === 'bundled' || fs.existsSync(path.join(dirOf(v), 'index.html'))) && !(state.bad || []).includes(v); }
let running = 'bundled', win = null, bootTimer = null;
function pickVersion() {
  state = readJSON(STATE(), state); state.bad = state.bad || [];
  // la versione più nuova tra: quella scaricata (se buona o mai provata) e quella dentro l'app
  let v = usable(state.current) ? state.current : (usable(state.good) ? state.good : 'bundled');
  if (v !== 'bundled' && cmpVer(v, bundledVersion) < 0) v = 'bundled';
  running = v; log('avvio con ' + v + ' (nell\'app: ' + bundledVersion + ')');
}
function serve() {
  protocol.handle('uz', async (req) => {
    const u = new URL(req.url); let rel = decodeURIComponent(u.pathname).replace(/^\/+/, '') || 'index.html';
    if (rel.includes('..')) return new Response('no', { status: 403 });
    let f = path.join(dirOf(running), rel);
    if (!fs.existsSync(f)) f = path.join(BUNDLED, rel);
    if (!fs.existsSync(f)) return new Response('non trovato', { status: 404 });
    const ext = path.extname(f).toLowerCase();
    const type = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript', '.json': 'application/json', '.png': 'image/png', '.jpg': 'image/jpeg', '.webp': 'image/webp', '.ktx2': 'image/ktx2', '.bin': 'application/octet-stream', '.css': 'text/css' }[ext] || 'application/octet-stream';
    return new Response(fs.readFileSync(f), { headers: { 'content-type': type } });
  });
}
// link d'invito: ultimazona://lobby/CODICE (dalla pagina del gioco: "Apri nell'app") → si apre l'app direttamente nella lobby
function lobbyHash(argv) { const u = (argv || []).find(a => /^ultimazona:\/\//i.test(a)); if (!u) return ''; const m = /lobby[\/=]([A-Za-z0-9]{4,12})/.exec(u); return m ? '#lobby=' + m[1].toUpperCase() : ''; }
let startHash = lobbyHash(process.argv);
const gotLock = app.requestSingleInstanceLock(); if (!gotLock) app.quit();
app.on('second-instance', (e, argv) => { const h = lobbyHash(argv); if (win && !win.isDestroyed()) { if (win.isMinimized()) win.restore(); win.focus(); if (h) win.loadURL('uz://gioco/index.html' + h); } });
try { app.setAsDefaultProtocolClient('ultimazona'); } catch (e) {}
function createWindow() {
  win = new BrowserWindow({ width: 1600, height: 900, backgroundColor: '#0b0d18', title: 'Ultima Zona', icon: path.join(__dirname, 'icon.png'), autoHideMenuBar: true,
    webPreferences: { preload: path.join(__dirname, 'preload.js'), contextIsolation: true, nodeIntegration: false, backgroundThrottling: false } });
  win.setMenuBarVisibility(false);
  if (startHash) log('apro la lobby ' + startHash.slice(7) + ' dal link');
  win.loadURL('uz://gioco/index.html' + startHash); startHash = '';
  win.webContents.on('before-input-event', (e, i) => { if (i.type === 'keyDown' && i.key === 'F11') { win.setFullScreen(!win.isFullScreen()); e.preventDefault(); } });
  win.webContents.setWindowOpenHandler(({ url }) => { if (/^https:\/\//.test(url)) shell.openExternal(url); return { action: 'deny' }; });
  clearTimeout(bootTimer);
  bootTimer = setTimeout(() => rollback('non è partita entro 2 minuti'), BOOT_TIMEOUT);
  win.webContents.on('render-process-gone', () => rollback('si è chiusa da sola'));
}
function rollback(why) {
  if (running === 'bundled' && !state.good) return;
  const bad = running; log('versione ' + bad + ' scartata: ' + why);
  if (bad !== 'bundled') { state.bad = [...new Set([...(state.bad || []), bad])]; }
  running = usable(state.good) && state.good !== bad ? state.good : 'bundled';
  state.current = running; if (state.pending === bad) state.pending = null; writeJSON(STATE(), state);
  if (win && !win.isDestroyed()) { win.loadURL('uz://gioco/index.html'); clearTimeout(bootTimer); bootTimer = setTimeout(() => {}, 1); }
}
ipcMain.on('uz-ready', (e, ver) => { clearTimeout(bootTimer); state.good = running; state.current = (state.pending && state.pending !== running && !(state.bad || []).includes(state.pending)) ? state.pending : running; log('versione ' + running + ' partita bene (' + ver + ')'); setTimeout(checkUpdate, +process.env.UZ_CHECK_MS || 8000); });
ipcMain.handle('uz-info', () => ({ running, bundled: bundledVersion, good: state.good, pending: state.pending || null, data: DATA() }));
ipcMain.on('uz-restart', () => { app.relaunch(); app.exit(0); });
function fetchBuf(url) { return new Promise((res, rej) => { const r = net.request({ url, cache: 'no-cache' }); const chunks = []; r.on('response', (resp) => { if (resp.statusCode !== 200) { rej(new Error('HTTP ' + resp.statusCode)); return; } resp.on('data', c => chunks.push(c)); resp.on('end', () => res(Buffer.concat(chunks))); resp.on('error', rej); }); r.on('error', rej); r.end(); }); }
let checking = false;
async function checkUpdate() {
  if (checking) return; checking = true;
  try {
    const info = JSON.parse((await fetchBuf(UPDATE_URL + 'version.json?t=' + Date.now())).toString('utf8'));
    const have = running === 'bundled' ? bundledVersion : running;
    if (!info.version || cmpVer(info.version, have) <= 0 || (state.bad || []).includes(info.version)) { checking = false; return; }
    const dir = path.join(VDIR(), info.version), tmp = dir + '.scarico';
    fs.rmSync(tmp, { recursive: true, force: true }); fs.mkdirSync(tmp, { recursive: true });
    for (const f of info.files) {
      const buf = await fetchBuf(UPDATE_URL + f.path + '?v=' + info.version);
      const h = crypto.createHash('sha256').update(buf).digest('hex');
      if (h !== f.sha256) throw new Error('impronta diversa per ' + f.path);
      fs.mkdirSync(path.dirname(path.join(tmp, f.path)), { recursive: true }); fs.writeFileSync(path.join(tmp, f.path), buf);
    }
    fs.rmSync(dir, { recursive: true, force: true }); fs.renameSync(tmp, dir);
    state.current = info.version; state.pending = info.version; log('scaricata ' + info.version + ' (si usa al prossimo avvio)');
    // si tengono solo le ultime 3 versioni scaricate
    const all = fs.readdirSync(VDIR()).filter(n => /^\d+\.\d+\.\d+$/.test(n)).sort(cmpVer);
    for (const n of all.slice(0, -3)) if (n !== state.good) fs.rmSync(path.join(VDIR(), n), { recursive: true, force: true });
    if (win && !win.isDestroyed()) win.webContents.send('uz-update', { version: info.version, notes: info.notes || '' });
  } catch (e) { log('aggiornamento non riuscito: ' + e.message); }
  checking = false;
}
app.whenReady().then(() => { if (!gotLock) return; pickVersion(); serve(); createWindow(); setInterval(checkUpdate, 30 * 60 * 1000); });
app.on('window-all-closed', () => app.quit());
