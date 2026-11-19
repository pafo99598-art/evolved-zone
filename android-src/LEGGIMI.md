# Evolved Zone · app Android (sorgente)

**Dal 19/11/2026 (Claude B) dentro l'app c'è il gioco Godot EVOLVEDZONEZ**, preso dalla cartella `app/` del sito
(la stessa da cui le app installate si aggiornano). Prima c'era il vecchio gioco HTML 0.67.2: chi installava l'app giocava a quello.
L'app si aggiorna da sola da `https://pafo99598-art.github.io/evolved-zone/app/` (controllo SHA-256 di ogni file, ritorno automatico all'ultima versione buona).

Costruzione (serve la chiave di firma `evolvedzone-release.jks` + `keystore.properties`, conservate da Michele, MAI nel repository):

    git pull                      (nella cartella del sito evolved-zone)
    python prepara_gioco.py       (copia app/ → app/src/main/assets/game/, controlla gli sha256, scrive version.json)
    gradle assembleRelease
    → app/build/outputs/apk/release/app-release.apk  da copiare come ../app/EvolvedZone.apk

I file del gioco copiati in assets/game NON vanno nel repository (.gitignore): si rifanno ogni volta con prepara_gioco.py.
Il numero di versione dell'app (versionCode) viene da version.json: maggiore×10000 + minore×100 + patch (0.93.0 → 9300).

Cambi del 19/11 in MainActivity (per il gioco Godot): i file grandi (index.wasm 39 MB, index.pck 16 MB) passano al WebView
a flusso invece che tutti in memoria; `.wasm` col tipo giusto (application/wasm); anche le richieste del service worker
del gioco si servono dai file dell'app; in build.gradle wasm e pck non compressi dentro l'apk (si aprono più in fretta).

Attenzione: con una chiave diversa Android non accetta l'aggiornamento dell'app già installata.
