# Sito Evolved Zone (Claude B, 8/11/2026) · versione 7

Cartella pronta da pubblicare così com'è in `/evolvedzone/` (nessun altro file del sito/gioco viene toccato).

## Come funziona
- **Prima delle 15:00 del 17/11**: pagina countdown (logo con 13 suoni, cristallo da colpire, musica, Instagram).
- **Ultimo minuto**: se l'audio non è attivo compare l'invito «Tocca lo schermo».
- **Alle 15:00 in punto (chi è sulla pagina)**: evento di circa 17 s a tempo di musica (128 battiti/min) → il cristallo va al centro e accelera, 6 fasci di luce con dentro le 6 immagini del gioco, energia al massimo, esplosione in frammenti, DROP musicale e entrata del sito ufficiale (circa 5 s, con pulsazioni a tempo, lame di luce, braci). La colonna sonora dell'evento è suonata dal browser (nessun file).
- **Chi apre la pagina dopo le 15:00**: sito ufficiale subito; in fondo «↺ Rivedi l'esplosione».

## Da fare prima del 17/11
In `index.html` cerca `var LINKS` e incolla i 4 link veri (browser, crazygames, pc, android). Finché un link è vuoto il pulsante mostra «Link in arrivo».

## Prove
- `prova.html` → pagina di prova di Michele: tasto ▶ che avvia l'evento e ↺ che torna al countdown (non indicizzata, ma chi ha il link la apre).
- `?prova=10` → countdown di 10 secondi e poi l'evento.
- `?sito=1` → sito ufficiale subito.

| File | Cosa |
|---|---|
| `index.html` | Tutta la pagina (countdown + evento + sito ufficiale). |
| `assets/logo.webp` | Logo ufficiale (da `cgassets/logo_t.png`) con contorno neon. |
| `assets/img/*.webp` | Le 6 immagini di Michele (fasci dell'evento + galleria Stagione 1). |
| `assets/snd/m_neon.mp3` | Musica «Ora neon» del gioco (originale, ElevenLabs, account di Michele). |
| `assets/snd/*.mp3` | Effetti del gioco, tutti CC0 (vedi `snd/FONTI.md` del gioco). |
