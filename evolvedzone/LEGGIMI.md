# Sito Evolved Zone (Claude B, 9/11/2026) · versione 16

Cartella pronta da pubblicare così com'è in `/evolvedzone/` (nessun altro file del sito/gioco viene toccato).

## Come funziona
- **Prima delle 15:00 del 17/11**: pagina countdown (logo con 13 suoni, cristallo da colpire, musica, Instagram).
- **Ultimo minuto**: se l'audio non è attivo compare l'invito «Tocca lo schermo».
- **Alle 15:00 in punto (chi è sulla pagina)**: evento di circa 20 s a tempo di musica (128 battiti/min) → il countdown svanisce piano (3,75 s), il cristallo sale al centro e cresce lentamente, 6 fasci di luce riempiti da 6 immagini scelte a caso tra le 11 del gioco (ogni visitatore vede un evento diverso), energia al massimo, esplosione in frammenti, DROP musicale e entrata del sito ufficiale (circa 5 s, con pulsazioni a tempo, lame di luce, braci). La colonna sonora dell'evento è suonata dal browser (nessun file).
- **Chi apre la pagina dopo le 15:00**: sito ufficiale subito; in fondo «↺ Rivedi l'esplosione».

## Avviso «Countdown di inizio dell'evento su questa pagina» (Blocco S12)
Compare **una sola volta per dispositivo** (memoria del browser `ez_disc`), a destra (sul telefono in basso a destra, il countdown resta libero), con accensione al neon a 1,3 s; a 6,9 s si frantuma in 42 schegge low-poly risucchiate dal cristallo che gira. Su `prova.html` compare sempre.

## Versioni dell'evento (Blocco S12)
7 versioni complete, una scelta a caso per ogni visitatore. Ogni versione = 5 momenti 3D (a 3,9 · 5,8 · 7,7 · 9,6 · 11,5 s):
1 Carica selvaggia · 2 Guerra totale · 3 Cielo neon · 4 Strada neon · 5 Valdarno selvaggio · 6 Assalto · 7 Rissa (lista in `var VERSIONS`).
Tolti: coccodrillo, pioggia, vecchio mostro a sagoma (il mostro nuovo arriva nel blocco S13). Negli ultimi 1,6 s fulmini leggeri escono dal cristallo e le stelle si accendono.
Corretto: oggetti 3D che restavano a terra dopo un riavvio (contati: 0 oggetti a inizio di ogni nuovo avvio, 4 avvii di fila).
Telefono: evento alleggerito (meno particelle e detriti, risoluzione 3D 1×) → circa +60% fotogrammi al secondo nella prova di confronto.
Su `prova.html` si può forzare una versione con `?v=1` … `?v=7`.

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
