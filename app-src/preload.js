// ponte tra il gioco e l'app: il gioco dice "sono partito", l'app avvisa quando un aggiornamento è pronto
const { contextBridge, ipcRenderer } = require('electron');
contextBridge.exposeInMainWorld('uzApp', {
  app: true,
  ready: (v) => ipcRenderer.send('uz-ready', String(v || '')),
  info: () => ipcRenderer.invoke('uz-info'),
  restart: () => ipcRenderer.send('uz-restart'),
  onUpdate: (cb) => ipcRenderer.on('uz-update', (e, d) => cb(d))
});
