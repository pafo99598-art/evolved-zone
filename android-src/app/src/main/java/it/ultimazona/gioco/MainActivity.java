// Ultima Zona · app per Android
// - il gioco gira dentro l'app (niente browser), a schermo intero, in orizzontale
// - aggiornamenti automatici come l'app per PC: scarica la nuova versione dal sito del gioco, controlla l'impronta
//   SHA-256 di ogni file e la usa al riavvio; se una versione non parte entro 2 minuti o si chiude da sola,
//   torna all'ultima che funzionava; dentro l'app c'è sempre una copia del gioco che funziona anche senza internet
// - i salvataggi (impostazioni, nome, preset, progressi) restano nell'app e non si toccano con gli aggiornamenti
// - i link d'invito (…/ultima-zona/#lobby=CODICE) aprono direttamente l'app nella lobby
package it.ultimazona.gioco;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    static final String UPDATE_URL = BuildConfig.UPDATE_URL;
    static final String HOST = "appassets.androidplatform.net";
    static final String BASE = "https://" + HOST + "/gioco/";
    static final long BOOT_TIMEOUT = BuildConfig.BOOT_MS; // se il gioco non dice "pronto" entro 2 minuti si torna alla versione precedente
    static final long CHECK_EVERY = 30 * 60 * 1000;

    WebView web;
    FrameLayout root;
    File vDir, stateFile;
    JSONObject state = new JSONObject();
    String running = "bundled", bundledVersion = "0.0.0", startHash = "";
    final Handler ui = new Handler(Looper.getMainLooper());
    final Runnable bootTimer = () -> rollback("non è partita entro 2 minuti");
    final Runnable periodic = new Runnable() { public void run() { checkUpdateAsync(); ui.postDelayed(this, CHECK_EVERY); } };
    volatile boolean checking = false;
    long lastBack = 0;

    // ---------------------------------------------------------------- avvio
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        vDir = new File(getFilesDir(), "versioni");
        stateFile = new File(getFilesDir(), "stato-versioni.json");
        try { bundledVersion = new JSONObject(new String(readAll(getAssets().open("game/version.json")), StandardCharsets.UTF_8)).optString("version", "0.0.0"); } catch (Exception e) { }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(11, 13, 24));
        setContentView(root);
        immersive();
        startHash = hashFromIntent(getIntent());
        pickVersion();
        createWeb();
        ui.postDelayed(periodic, CHECK_EVERY);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String h = hashFromIntent(intent);
        if (!h.isEmpty() && web != null) web.evaluateJavascript("location.hash=" + JSONObject.quote(h.substring(1)) + ";location.reload()", null);
    }

    static String hashFromIntent(Intent i) {
        try { Uri u = i == null ? null : i.getData(); if (u != null && u.getFragment() != null && u.getFragment().startsWith("lobby=")) return "#" + u.getFragment(); } catch (Exception e) { }
        return "";
    }

    @Override public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) immersive(); }

    void immersive() {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) { c.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars()); c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE); }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    // ---------------------------------------------------------------- versioni
    synchronized void readState() {
        try { if (stateFile.exists()) state = new JSONObject(new String(readAll(new FileInputStream(stateFile)), StandardCharsets.UTF_8)); } catch (Exception e) { state = new JSONObject(); }
        try { if (!state.has("bad")) state.put("bad", new JSONArray()); if (!state.has("log")) state.put("log", new JSONArray()); } catch (Exception e) { }
    }
    synchronized void saveState() {
        try { File t = new File(stateFile.getPath() + ".tmp"); try (FileOutputStream o = new FileOutputStream(t)) { o.write(state.toString(1).getBytes(StandardCharsets.UTF_8)); } t.renameTo(stateFile); } catch (Exception e) { }
    }
    synchronized void log(String t) {
        android.util.Log.i("UltimaZona", t);
        try { JSONArray l = state.optJSONArray("log"); if (l == null) l = new JSONArray(); JSONArray n = new JSONArray(); for (int i = Math.max(0, l.length() - 40); i < l.length(); i++) n.put(l.get(i)); n.put(new java.util.Date().toString() + " " + t); state.put("log", n); } catch (Exception e) { }
        saveState();
    }
    boolean isBad(String v) { JSONArray b = state.optJSONArray("bad"); if (b == null || v == null) return false; for (int i = 0; i < b.length(); i++) if (v.equals(b.optString(i))) return true; return false; }
    File dirOf(String v) { return new File(vDir, v); }
    boolean usable(String v) { return v != null && !v.isEmpty() && !isBad(v) && (v.equals("bundled") || new File(dirOf(v), "index.html").exists()); }
    static int cmpVer(String a, String b) {
        String[] x = String.valueOf(a).split("\\."), y = String.valueOf(b).split("\\.");
        for (int i = 0; i < 3; i++) { int p = i < x.length ? parseInt(x[i]) : 0, q = i < y.length ? parseInt(y[i]) : 0; if (p != q) return p - q; }
        return 0;
    }
    static int parseInt(String s) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; } }

    void pickVersion() {
        readState();
        String cur = state.optString("current", null), good = state.optString("good", null);
        String v = usable(cur) ? cur : (usable(good) ? good : "bundled");
        if (!v.equals("bundled") && cmpVer(v, bundledVersion) < 0) v = "bundled"; // l'app appena installata è più nuova di quella scaricata
        running = v;
        log("avvio con " + v + " (nell'app: " + bundledVersion + ")");
    }

    void rollback(String why) {
        String good = state.optString("good", null);
        if (running.equals("bundled") && (good == null || good.isEmpty())) return;
        String bad = running;
        log("versione " + bad + " scartata: " + why);
        try {
            if (!bad.equals("bundled") && !isBad(bad)) state.getJSONArray("bad").put(bad);
            running = (usable(good) && !good.equals(bad)) ? good : "bundled";
            state.put("current", running);
            if (bad.equals(state.optString("pending", ""))) state.remove("pending");
        } catch (Exception e) { running = "bundled"; }
        saveState();
        reloadWeb();
    }

    // ---------------------------------------------------------------- il gioco
    void createWeb() {
        if (web != null) { root.removeView(web); web.destroy(); }
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(11, 13, 24));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setTextZoom(100);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        web.addJavascriptInterface(new Bridge(), "uzAppNative");
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onConsoleMessage(android.webkit.ConsoleMessage m) {
                if (m.messageLevel() == android.webkit.ConsoleMessage.MessageLevel.ERROR) android.util.Log.w("UltimaZona", "gioco: " + m.message() + " @" + m.lineNumber());
                return true;
            }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) { return serve(r.getUrl()); }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if (HOST.equals(u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception e) { }
                return true;
            }
            @Override public boolean onRenderProcessGone(WebView v, RenderProcessGoneDetail d) {
                ui.post(() -> { createWebAfterCrash(); });
                return true;
            }
        });
        root.addView(web, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        web.loadUrl(BASE + "index.html" + startHash);
        startHash = "";
        armBoot();
    }
    void createWebAfterCrash() {
        if (web != null) { root.removeView(web); try { web.destroy(); } catch (Exception e) { } web = null; }
        rollback("si è chiusa da sola");
        if (web == null) createWeb();
    }
    void reloadWeb() { if (web == null) createWeb(); else { web.loadUrl(BASE + "index.html"); armBoot(); } }
    void armBoot() { ui.removeCallbacks(bootTimer); ui.postDelayed(bootTimer, BOOT_TIMEOUT); }

    static final String SHIM = "<script>(function(){var N=window.uzAppNative;if(!N)return;var cbs=[];window.__uzOnUpdate=function(d){cbs.forEach(function(f){try{f(d)}catch(e){}})};" +
            "window.uzApp={app:true,android:true,ready:function(v){N.ready(String(v||''))},info:function(){return Promise.resolve(JSON.parse(N.info()))},restart:function(){N.restart()},onUpdate:function(cb){cbs.push(cb)}};})();</script>";

    WebResourceResponse serve(Uri u) {
        if (!HOST.equals(u.getHost())) return null;
        String p = u.getPath() == null ? "" : u.getPath();
        if (!p.startsWith("/gioco/")) return resp(404, "text/plain", "non trovato".getBytes(StandardCharsets.UTF_8));
        String rel = p.substring(7);
        if (rel.isEmpty()) rel = "index.html";
        if (rel.contains("..")) return resp(403, "text/plain", "no".getBytes(StandardCharsets.UTF_8));
        byte[] data = null;
        try {
            if (!running.equals("bundled")) { File f = new File(dirOf(running), rel); if (f.isFile()) data = readAll(new FileInputStream(f)); }
            if (data == null) data = readAll(getAssets().open("game/" + rel));
        } catch (Exception e) { data = null; }
        if (data == null) return resp(404, "text/plain", "non trovato".getBytes(StandardCharsets.UTF_8));
        String mime = mimeOf(rel);
        if (rel.equals("index.html")) {
            String h = new String(data, StandardCharsets.UTF_8);
            int i = h.indexOf("<head>");
            h = i >= 0 ? h.substring(0, i + 6) + SHIM + h.substring(i + 6) : SHIM + h;
            data = h.getBytes(StandardCharsets.UTF_8);
        }
        return resp(200, mime, data);
    }
    static WebResourceResponse resp(int code, String mime, byte[] data) {
        Map<String, String> h = new HashMap<>();
        h.put("Cache-Control", "no-cache");
        h.put("Access-Control-Allow-Origin", "*");
        String reason = code == 200 ? "OK" : code == 404 ? "Not Found" : "Forbidden";
        return new WebResourceResponse(mime, mime.startsWith("text/") || mime.contains("json") || mime.contains("javascript") ? "utf-8" : null, code, reason, h, new ByteArrayInputStream(data));
    }
    static String mimeOf(String f) {
        String x = f.toLowerCase();
        if (x.endsWith(".html")) return "text/html";
        if (x.endsWith(".js")) return "text/javascript";
        if (x.endsWith(".json")) return "application/json";
        if (x.endsWith(".png")) return "image/png";
        if (x.endsWith(".jpg") || x.endsWith(".jpeg")) return "image/jpeg";
        if (x.endsWith(".webp")) return "image/webp";
        if (x.endsWith(".css")) return "text/css";
        if (x.endsWith(".ktx2")) return "image/ktx2";
        return "application/octet-stream";
    }

    class Bridge {
        @JavascriptInterface public void ready(String ver) { ui.post(() -> onReady(ver)); }
        @JavascriptInterface public String info() {
            try { JSONObject o = new JSONObject(); o.put("running", running); o.put("bundled", bundledVersion); o.put("good", state.optString("good", null)); o.put("pending", state.optString("pending", null)); o.put("android", true); return o.toString(); } catch (Exception e) { return "{}"; }
        }
        @JavascriptInterface public void restart() { ui.post(() -> { pickVersion(); reloadWeb(); }); }
    }

    void onReady(String ver) {
        ui.removeCallbacks(bootTimer);
        try {
            state.put("good", running);
            String pend = state.optString("pending", "");
            state.put("current", (!pend.isEmpty() && !pend.equals(running) && !isBad(pend)) ? pend : running);
        } catch (Exception e) { }
        log("versione " + running + " partita bene (" + ver + ")");
        ui.postDelayed(this::checkUpdateAsync, 8000);
    }

    // ---------------------------------------------------------------- aggiornamenti
    void checkUpdateAsync() { if (checking) return; checking = true; new Thread(() -> { try { checkUpdate(); } catch (Exception e) { log("aggiornamento non riuscito: " + e.getMessage()); } checking = false; }).start(); }

    void checkUpdate() throws Exception {
        JSONObject info = new JSONObject(new String(fetch(UPDATE_URL + "version.json?t=" + System.currentTimeMillis()), StandardCharsets.UTF_8));
        String nv = info.optString("version", "");
        String have = running.equals("bundled") ? bundledVersion : running;
        if (nv.isEmpty() || cmpVer(nv, have) <= 0 || isBad(nv)) return;
        File dir = dirOf(nv), tmp = new File(vDir, nv + ".scarico");
        deleteAll(tmp); tmp.mkdirs();
        JSONArray files = info.getJSONArray("files");
        for (int i = 0; i < files.length(); i++) {
            JSONObject f = files.getJSONObject(i);
            String path = f.getString("path");
            if (path.contains("..")) throw new Exception("percorso non valido");
            byte[] buf = fetch(UPDATE_URL + path + "?v=" + nv);
            if (!sha256(buf).equalsIgnoreCase(f.getString("sha256"))) throw new Exception("impronta diversa per " + path);
            File out = new File(tmp, path); out.getParentFile().mkdirs();
            try (FileOutputStream o = new FileOutputStream(out)) { o.write(buf); }
        }
        deleteAll(dir);
        if (!tmp.renameTo(dir)) throw new Exception("non riesco a salvare la versione");
        synchronized (this) { state.put("current", nv); state.put("pending", nv); }
        log("scaricata " + nv + " (si usa al prossimo avvio)");
        // si tengono solo le ultime 3 versioni scaricate
        List<String> all = new ArrayList<>();
        String[] names = vDir.list();
        if (names != null) for (String n : names) if (n.matches("\\d+\\.\\d+\\.\\d+")) all.add(n);
        Collections.sort(all, MainActivity::cmpVer);
        String good = state.optString("good", "");
        for (int i = 0; i < all.size() - 3; i++) if (!all.get(i).equals(good) && !all.get(i).equals(running)) deleteAll(dirOf(all.get(i)));
        JSONObject d = new JSONObject(); d.put("version", nv); d.put("notes", info.optString("notes", ""));
        ui.post(() -> { if (web != null) web.evaluateJavascript("window.__uzOnUpdate&&window.__uzOnUpdate(" + d + ")", null); });
    }

    static byte[] fetch(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(15000); c.setReadTimeout(60000); c.setUseCaches(false); c.setInstanceFollowRedirects(true);
        try {
            if (c.getResponseCode() != 200) throw new Exception("HTTP " + c.getResponseCode());
            return readAll(c.getInputStream());
        } finally { c.disconnect(); }
    }
    static byte[] readAll(InputStream in) throws Exception {
        try (InputStream i = in; ByteArrayOutputStream o = new ByteArrayOutputStream()) { byte[] b = new byte[65536]; int n; while ((n = i.read(b)) > 0) o.write(b, 0, n); return o.toByteArray(); }
    }
    static String sha256(byte[] b) throws Exception {
        byte[] h = MessageDigest.getInstance("SHA-256").digest(b); StringBuilder s = new StringBuilder(); for (byte x : h) s.append(String.format("%02x", x)); return s.toString();
    }
    static void deleteAll(File f) { if (f == null || !f.exists()) return; File[] l = f.listFiles(); if (l != null) for (File c : l) deleteAll(c); f.delete(); }

    // ---------------------------------------------------------------- tasto Indietro: pausa nel gioco, due volte per uscire
    @Override public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (now - lastBack < 1800) { moveTaskToBack(true); return; }
        lastBack = now;
        if (web != null) web.evaluateJavascript("window.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape',code:'Escape',bubbles:true}))", null);
        Toast.makeText(this, "Premi di nuovo Indietro per uscire", Toast.LENGTH_SHORT).show();
    }

    @Override protected void onDestroy() { ui.removeCallbacksAndMessages(null); if (web != null) web.destroy(); super.onDestroy(); }
}
