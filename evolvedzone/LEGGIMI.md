# Sito Evolved Zone (Claude B, 8/11/2026) · versione 13

Cartella pronta da pubblicare così com'è in `/evolvedzone/` (nessun altro file del sito/gioco viene toccato).

## Come funziona
- **Prima delle 15:00 del 17/11**: pagina countdown (logo con 13 suoni, cristallo da colpire, musica, Instagram).
- **Ultimo minuto**: se l'audio non è attivo compare l'invito «Tocca lo schermo».
- **Alle 15:00 in punto (chi è sulla pagina)**: evento di circa 20 s a tempo di musica (128 battiti/min) → il countdown svanisce piano (3,75 s), il cristallo sale al centro e cresce lentamente, 6 fasci di luce riempiti da 6 immagini scelte a caso tra le 11 del gioco (ogni visitatore vede un evento diverso), energia al massimo, esplosione in frammenti, DROP musicale e entrata del sito ufficiale (circa 5 s, con pulsazioni a tempo, lame di luce, braci). La colonna sonora dell'evento è suonata dal browser (nessun file).
- **Chi apre la pagina dopo le 15:00**: sito ufficiale subito; in fondo «↺ Rivedi l'esplosione».

## Partenza esatta alle 15:00
- L'ora non viene dal telefono/PC: la pagina legge l'ora del server (intestazione «Date» di GitHub) e corregge l'orologio sbagliato del dispositivo.
- Controllo ogni 0,25 s. Fuso orario del visitatore indifferente (15:00 Italia = 14:00 UTC).
- Schermo spento/scheda nascosta alle 15:00 → l'evento parte quando torna (entro 30 min).
- Pagina aperta tra 15:00 e 15:03 → evento subito; dopo → sito ufficiale diretto.

## Da fare prima del 17/11
In `index.html` cerca `var LINKS` e incolla i 4 link veri (browser, crazygames, pc, android). Finché un link è vuoto il pulsante mostra «Link in arrivo».

## Testi de «Il gioco» e «Le isole»
In `index.html` cerca `var GAME` (7 capitoli: `img` immagine, `clip` video breve che parte col mouse sopra o al centro dello schermo sul telefono, `text`, `foto`, `video`) e `var ISLES` (isole: `model` = modellino 3D .glb in arrivo).

## Sezioni del sito ufficiale
Il gioco (7 capitoli) · Le isole · Armadietto 3D (tocca «IL GIOCO»: `assets/modelli.json`: 8 personaggi + 8 zaini veri del gioco, estratti da char52.js; Three.js r128 da cdnjs, caricati solo quando si arriva alla sezione) · In arrivo · Domande · Chi siamo.

## QR
Icona QR in alto a sinistra (countdown e sito): apre il QR del sito, con «copia link» e «condividi». Glitch neon ogni 8 s (icona e immagine del QR).

## Prove
- `prova.html` → pagina di prova di Michele: tasto ▶ che avvia l'evento e ↺ che torna al countdown (non indicizzata, ma chi ha il link la apre).
- `?prova=10` → countdown di 10 secondi e poi l'evento.
- `?sito=1` → sito ufficiale subito.

| File | Cosa |
|---|---|
| `index.html` | Tutta la pagina (countdown + evento + sito ufficiale). |
| `assets/logo.webp` | Logo ufficiale (da `cgassets/logo_t.png`) con contorno neon. |
| `assets/img/*.webp` | Le 11 immagini di Michele (fasci dell'evento + capitoli de «Il gioco»). |
| `assets/snd/m_neon.mp3` | Musica «Ora neon» del gioco (originale, ElevenLabs, account di Michele). |
| `assets/snd/*.mp3` | Effetti del gioco, tutti CC0 (vedi `snd/FONTI.md` del gioco). |
