## 19/11/2026 (Claude B) · mette DENTRO l'app Android il gioco Godot EVOLVEDZONEZ (quello di app/ del sito).
## Prima l'app conteneva il vecchio gioco HTML 0.67.2: chi la installava giocava a quello finché l'aggiornamento non arrivava.
## Uso (dalla cartella android-src, sul PC di Michele, prima di «gradle assembleRelease»):
##   python prepara_gioco.py            (prende ../app/ del sito)
##   python prepara_gioco.py <cartella> (per esempio la build/prova-z del gioco + un version.json)
## Copia tutti i file del gioco (non EvolvedZone.apk, non tex/ del vecchio HTML) in app/src/main/assets/game/ e controlla gli sha256.
import hashlib, json, os, shutil, sys

here = os.path.dirname(os.path.abspath(__file__))
src = sys.argv[1] if len(sys.argv) > 1 else os.path.join(here, "..", "app")
dst = os.path.join(here, "app", "src", "main", "assets", "game")
info = json.load(open(os.path.join(src, "version.json"), encoding="utf-8"))
if info.get("motore") != "godot":
    sys.exit("ERRORE: %s non contiene il gioco Godot (manca \"motore\":\"godot\" in version.json)" % src)
if os.path.isdir(dst):
    for n in os.listdir(dst):   # solo la cartella dei file copiati dell'app (si rifà ogni volta)
        p = os.path.join(dst, n)
        shutil.rmtree(p) if os.path.isdir(p) else os.remove(p)
os.makedirs(dst, exist_ok=True)
tot = 0
for f in info["files"]:
    a = os.path.join(src, f["path"])
    b = os.path.join(dst, f["path"])
    data = open(a, "rb").read()
    if hashlib.sha256(data).hexdigest() != f["sha256"]:
        sys.exit("ERRORE: impronta diversa per " + f["path"])
    os.makedirs(os.path.dirname(b), exist_ok=True)
    open(b, "wb").write(data)
    tot += len(data)
json.dump({"version": info["version"], "motore": "godot"}, open(os.path.join(dst, "version.json"), "w", encoding="utf-8"))
print("assets/game pronta: EVOLVEDZONEZ %s, %d file, %.1f MB" % (info["version"], len(info["files"]), tot / 1e6))
