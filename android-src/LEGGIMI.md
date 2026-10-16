# Ultima Zona · app Android (sorgente)

Il gioco dentro l'app arriva da `app/index.html` del sito (copiato in `app/src/main/assets/game/index.html` prima di costruire).
L'app si aggiorna da sola da `https://pafo99598-art.github.io/ultima-zona/app/` (controllo SHA-256 di ogni file, ritorno automatico all'ultima versione buona).

Costruzione (serve la chiave di firma `ultimazona-release.jks` + `keystore.properties`, conservate da Michele, MAI nel repository):

    cp ../app/index.html app/src/main/assets/game/index.html
    echo '{"version":"X.Y.Z"}' > app/src/main/assets/game/version.json
    gradle assembleRelease

Attenzione: con una chiave diversa Android non accetta l'aggiornamento dell'app già installata.
