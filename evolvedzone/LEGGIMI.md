# Sito Evolved Zone (Claude B, 10/11/2026) · versione 19

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

## Mostro neon e caccia (Blocco S13a · v17)
- **Mostro neon 3D** (low poly traslucido, occhi azzurri, spigoli rosa/azzurri) in 4 versioni su 7 (1, 4, 6, 7 · `mon:1` in `VERSIONS`). Tempi (orologio dell'evento, quindi uguali anche su telefoni lenti): sale dalla foschia sotto al sole da 8,8 s · occhi a 9,7 s · alza le braccia da 11,4 s · ruggisce a 12,6 s · sfera di energia dalla bocca · da 13,2 s fulmini spessi e fiumi di energia dalle mani al cristallo · arrivo esatto a 15 s con l'esplosione, il mostro va in pezzi.
- Telefono: col mostro la versione perde il 5° momento (fuga finale), per restare fluida.
- **Caccia** (modello vero del gioco `e4caccia73`, aggiunto in `assets/modelli.json` alla voce `VE.jet`): passaggio di 3 caccia dal sole sopra di te con 2 missili (versioni 2, 3, 6) e **duello aereo** (versione 3): azzurri inseguiti dai rosa con raffiche, uno abbattuto esplode in aria e precipita.
- Nuovi valori del SITO (non del gioco), scelti da Claude B: dimensione mostro 2,1 (PC) / 1,95 (telefono), distanza 95, caccia ×0,62 (passaggio) / ×0,5 (duello), passaggio 1,9 s, duello circa 2,4 s da bordo a bordo.

## v19 (Blocco S14) · veicoli nell'armadietto e zaino di prova
- **Armadietto 3D**: nuova sezione VEICOLI con CACCIA ed ELICOTTERO (modelli veri del gioco, `e4caccia73` ed `e2heli70`, in `assets/modelli.json` alla voce `VE`; l'elicottero ha le pale che girano).
- **Zaino di prova** nella scheda «Sopravvivenza» (pulsante «APRI LO ZAINO»): telo e icone veri del gioco (`assets/zaino/`), oggetti da trascinare uno sull'altro (sul telefono: tocco + tocco), 13 combinazioni e ruota del CRAFTING con 20 ricette. Nomi, costi, quantità e testi sono copiati dal gioco (ricette base, `craft39`, `zaino89`) senza modifiche. Senza «Sangue», tasto zaino, «Esc chiudi» e crediti OpenStreetMap. Nota «È solo un assaggio» nella scheda e scritta «ESEMPIO» nello zaino.
- Valore del SITO scelto da Claude B: il contenuto dello zaino di partenza (`ZN_START`), inventato per far provare tutto. Non comprese: armi sul telo e relative modifiche, pezzi neon, uso/lancio degli oggetti, ricette di barricate/ponti/rampino (non erano nei file che ho).

## v18 (Blocco S13b + rifiniture)
- **Passaggio esplosione → sito più fluido**: dopo l'esplosione i due livelli a tutto schermo dei fasci e delle schegge si spengono appena vuoti (prima venivano ridisegnati per 5 s); l'impulso a tempo di musica tocca solo titolo e linea d'orizzonte (non tutta la pagina); sul telefono metà schegge/scintille e niente tremolio del sito. Prova telefono rallentato ×4: fotogramma medio dell'entrata 250 → 49 ms, fotogramma dell'esplosione 349 → 166 ms.
- **Bug corretto (causa vera degli «oggetti rimasti»)**: un effetto 3D creato da un altro effetto (esplosione del missile/meteorite, fumo) veniva perso dalla lista e restava congelato in scena. Ora la lista (`s3Run`) tiene anche quelli nati durante l'aggiornamento.
- **Mostro rifatto sull'immagine di Michele**: incappucciato con punta, senza corna, spalle enormi, corpo viola scuro, braccia lunghe con avambracci azzurri trasparenti e artigli, occhi azzurri obliqui, gambe dietro l'orizzonte. Alza gli artigli verso di te e scarica i fulmini sul cristallo a 15 s.
- **Animazioni dei personaggi** (`pose`): gomiti, busto che ruota e si piega, testa, rimbalzo del corpo; nuova posa di salto (stacco, raccolta, distensione) e doppie pistole.
- **Momenti nuovi**: `meteoriti` (versione 5: cadono, esplodono a terra con fiamme che si spengono in 1,2 s, tre colpiscono chi corre), `auto` (versione 2: auto volante a mattoncini inseguita da una moto neon che spara, poi ti passa sopra), `corsa` (versione 5: tre piloti in sella ai cinghiali con doppie pistole, uno aggancia col rampino quello davanti).
- Versioni: 1 volpi·cinghiali·rampino·guerra·fuga +mostro | 2 guerra·auto·caccia·salto·fuga | 3 paracadute·duello·rampino·caccia·fuga | 4 bus·inseguimento·volpi·guerra·fuga +mostro | 5 volpi·corsa·meteoriti·paracadute·fuga | 6 salto·caccia·cinghiali·inseguimento·fuga +mostro | 7 paracadute·bus·guerra·volpi·fuga +mostro.

## Apertura più veloce (v16.1)
Logo leggero `assets/logo_s.webp` (87 KB) scaricato per primo, poi sostituito da `logo.webp` a piena qualità; caratteri Google non bloccano più il primo disegno; immagini dei fasci scaricate dopo il logo; immagini del sito ufficiale preparate durante l'evento. Prova 4G lenta su telefono: logo visibile 2,8 s → 1,5 s, primo disegno 0,84 → 0,45 s.

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
