package com.hyflez.overlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(10, 12, 20);
    private final int PANEL = Color.rgb(22, 25, 39);
    private final int PURPLE = Color.rgb(160, 90, 255);
    private final int CYAN = Color.rgb(35, 220, 235);
    private LinearLayout root;
    private LinearLayout content;
    private String currentTab = "HOME";

    private int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildLauncher();
    }

    private GradientDrawable shape(int color, int strokeColor, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        if (strokeColor != Color.TRANSPARENT) d.setStroke(dp(1), strokeColor);
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void buildLauncher() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(28));
        root.setBackgroundColor(BG);
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout brand = new LinearLayout(this);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        TextView logo = text("H", 27, Color.WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(shape(PURPLE, CYAN, 15));
        brand.addView(logo, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout names = new LinearLayout(this);
        names.setOrientation(LinearLayout.VERTICAL);
        names.setPadding(dp(12), 0, 0, 0);
        names.addView(text("HYFLEZ", 22, Color.WHITE, true));
        names.addView(text("BEDROCK LAUNCHER", 10, CYAN, true));
        brand.addView(names, new LinearLayout.LayoutParams(0, -2, 1));
        TextView version = text("V4 • ALPHA", 10, CYAN, true);
        version.setPadding(dp(10), dp(7), dp(10), dp(7));
        version.setBackground(shape(PANEL, PURPLE, 20));
        brand.addView(version);
        root.addView(brand);

        TextView welcome = text("SEU MINECRAFT.\nDO SEU JEITO.", 29, Color.WHITE, true);
        welcome.setPadding(0, dp(30), 0, dp(7));
        root.addView(welcome);
        root.addView(text("Personalize sua experiência Bedrock.", 14, Color.LTGRAY, false));

        LinearLayout playCard = new LinearLayout(this);
        playCard.setOrientation(LinearLayout.VERTICAL);
        playCard.setPadding(dp(18), dp(18), dp(18), dp(18));
        playCard.setBackground(shape(PANEL, PURPLE, 22));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, dp(24), 0, dp(18));
        root.addView(playCard, cp);
        playCard.addView(text("PRONTO PARA JOGAR?", 12, CYAN, true));
        TextView playDesc = text("Abra o Minecraft Bedrock instalado no seu Android.", 13, Color.LTGRAY, false);
        playDesc.setPadding(0, dp(7), 0, dp(14));
        playCard.addView(playDesc);
        Button play = button("▶   PLAY • ABRIR MINECRAFT", PURPLE);
        play.setOnClickListener(v -> openMinecraft());
        playCard.addView(play, new LinearLayout.LayoutParams(-1, dp(54)));

        TextView section = text("CENTRAL HYFLEZ", 12, CYAN, true);
        section.setPadding(0, dp(5), 0, dp(12));
        root.addView(section);

        String[][] tabs = {
            {"HOME", "⌂  Home"}, {"MODS", "◇  Mods"}, {"SKINS", "♙  Skins"},
            {"TEXTURES", "▧  Texturas"}, {"WORLDS", "▦  Worlds"}, {"TOOLS", "⚙  Tools"},
            {"COMBAT", "⚔  Combat"}
        };
        for (int i = 0; i < tabs.length; i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = i; j < Math.min(i + 2, tabs.length); j++) {
                final String key = tabs[j][0];
                Button tab = button(tabs[j][1], currentTab.equals(key) ? PURPLE : PANEL);
                tab.setTextSize(13);
                tab.setOnClickListener(v -> showTab(key));
                LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, dp(48), 1);
                tp.setMargins(0, 0, dp(8), dp(8));
                row.addView(tab, tp);
            }
            root.addView(row);
        }

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content, new LinearLayout.LayoutParams(-1, -2));
        showTab("HOME");
        TextView footer = text("HYFLEZ • FEITO PARA BEDROCK", 10, Color.GRAY, true);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(24), 0, 0);
        root.addView(footer);
    }

    private Button button(String label, int color) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(shape(color, color == PANEL ? Color.rgb(55, 60, 82) : CYAN, 14));
        return b;
    }

    private void addCard(String title, String description, String action, Runnable callback) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(15), dp(16), dp(15));
        card.setBackground(shape(PANEL, Color.rgb(48, 52, 75), 18));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 0, 0, dp(12));
        content.addView(card, p);
        card.addView(text(title, 16, Color.WHITE, true));
        TextView desc = text(description, 13, Color.LTGRAY, false);
        desc.setPadding(0, dp(6), 0, dp(12));
        card.addView(desc);
        if (action != null) {
            Button b = button(action, PURPLE);
            b.setOnClickListener(v -> callback.run());
            card.addView(b, new LinearLayout.LayoutParams(-1, dp(46)));
        }
    }

    private void showTab(String tab) {
        currentTab = tab;
        if (content == null) return;
        content.removeAllViews();
        switch (tab) {
            case "MODS":
                addCard("ADD-ONS & MODS", "Importe arquivos .mcpack e .mcaddon. O Minecraft precisa reconhecer o formato para concluir a instalação.", "IMPORTAR PACOTE", () -> importToMinecraft());
                addCard("DICA", "Use arquivos de fontes confiáveis. O HyFlez não instala modificações escondidas nem altera arquivos internos do jogo.", null, null);
                break;
            case "SKINS":
                addCard("SKINS", "A área de skins está preparada para receber importação e organização de visuais.", null, null);
                addCard("PRÓXIMA ETAPA", "Vamos adicionar seleção e importação de arquivos de skin compatíveis.", null, null);
                break;
            case "TEXTURES":
                addCard("TEXTURE PACKS", "Gerencie pacotes visuais compatíveis com Minecraft Bedrock.", "IMPORTAR PACOTE", () -> importToMinecraft());
                break;
            case "WORLDS":
                addCard("MUNDOS", "Importe mundos Bedrock no formato .mcworld. A abertura será encaminhada ao Minecraft quando houver suporte no aparelho.", "IMPORTAR MUNDO", () -> importToMinecraft());
                break;
            case "TOOLS":
                addCard("OVERLAY HYFLEZ", "Ative a bolinha flutuante e o HUD visual do HyFlez. Esses recursos não modificam a memória do Minecraft.", "ATIVAR OVERLAY", () -> startOverlay());
                addCard("STATUS", "Launcher Android inicial. Algumas áreas ainda serão desenvolvidas nas próximas versões.", null, null);
                break;
            case "COMBAT":
                addCard("REACTION TRAINER", "Teste seus reflexos. Aguarde a tela ficar verde e toque o mais rápido possível.", "INICIAR TESTE", () -> startReactionTest());
                addCard("CPS TEST", "Meça sua velocidade de cliques durante cinco segundos.", "TESTAR CPS", () -> startCpsTest());
                addCard("AIM TRAINER", "Treino de precisão com alvos dentro do HyFlez. Não controla a mira do Minecraft.", "ABRIR TREINO", () -> showAimTrainer());
                addCard("FAIR PLAY", "Os módulos de treino funcionam fora das partidas e não leem jogadores, entidades ou dados de servidores públicos.", null, null);
                break;
            default:
                addCard("BEM-VINDO AO HYFLEZ", "Seu ponto de partida para add-ons, skins, texturas e mundos do Minecraft Bedrock.", null, null);
                addCard("ATALHO RÁPIDO", "Toque em PLAY para abrir o Minecraft instalado no aparelho.", "ABRIR MINECRAFT", () -> openMinecraft());
                addCard("PERSONALIZE", "Entre em Mods, Skins, Texturas ou Worlds para importar conteúdo compatível.", null, null);
        }
    }

    private void openMinecraft() {
        Intent launch = getPackageManager().getLaunchIntentForPackage("com.mojang.minecraftpe");
        if (launch == null) {
            new AlertDialog.Builder(this)
                .setTitle("Minecraft não encontrado")
                .setMessage("Instale o Minecraft Bedrock oficial neste aparelho e tente novamente.")
                .setPositiveButton("OK", null).show();
            return;
        }
        try {
            startActivity(launch);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Não foi possível abrir o Minecraft.", Toast.LENGTH_LONG).show();
        }
    }

    private void importToMinecraft() {
        Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pick.addCategory(Intent.CATEGORY_OPENABLE);
        pick.setType("*/*");
        pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(pick, 41);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Gerenciador de arquivos indisponível.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 41 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String mime = getContentResolver().getType(uri);
            if (mime == null) mime = "application/octet-stream";
            Intent open = new Intent(Intent.ACTION_VIEW);
            open.setDataAndType(uri, mime);
            open.setPackage("com.mojang.minecraftpe");
            open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(open);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "O Minecraft não aceitou esse arquivo. Confira se é um .mcpack, .mcaddon ou .mcworld.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void startReactionTest() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(20), dp(20), dp(20));
        box.setBackground(shape(PANEL, PURPLE, 18));
        TextView status = text("Toque em INICIAR e espere a tela ficar verde.", 16, Color.WHITE, true);
        status.setGravity(Gravity.CENTER);
        box.addView(status, new LinearLayout.LayoutParams(-1, dp(90)));
        Button action = button("INICIAR", PURPLE);
        box.addView(action, new LinearLayout.LayoutParams(-1, dp(54)));
        final boolean[] waiting = {false};
        final boolean[] ready = {false};
        final long[] greenAt = {0L};
        final Runnable[] goGreen = new Runnable[1];
        action.setOnClickListener(v -> {
            if (ready[0]) {
                long elapsed = SystemClock.elapsedRealtime() - greenAt[0];
                ready[0] = false;
                status.setText("Seu tempo: " + elapsed + " ms");
                action.setText("TENTAR NOVAMENTE");
                action.setBackground(shape(PURPLE, CYAN, 14));
                return;
            }
            if (waiting[0]) {
                handler.removeCallbacks(goGreen[0]);
                waiting[0] = false;
                status.setText("Você foi rápido demais! Tente novamente.");
                action.setText("TENTAR NOVAMENTE");
                return;
            }
            waiting[0] = true;
            status.setText("Aguarde... não toque ainda!");
            action.setText("AGUARDANDO");
            action.setBackground(shape(Color.rgb(170, 45, 55), CYAN, 14));
            int delay = 1500 + (int)(Math.random() * 2500);
            goGreen[0] = () -> {
                if (!dialog.isShowing() || !waiting[0]) return;
                waiting[0] = false;
                ready[0] = true;
                greenAt[0] = SystemClock.elapsedRealtime();
                status.setText("AGORA! TOQUE!");
                action.setText("TOCAR!");
                action.setBackground(shape(Color.rgb(30, 170, 90), CYAN, 14));
            };
            handler.postDelayed(goGreen[0], delay);
        });
        dialog.setView(box);
        dialog.setOnDismissListener(d -> handler.removeCallbacksAndMessages(null));
        dialog.show();
    }

    private void startCpsTest() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(18), dp(18), dp(18));
        box.setBackground(shape(PANEL, PURPLE, 18));
        TextView status = text("Toque em COMEÇAR para iniciar a contagem de 5 segundos.", 15, Color.WHITE, true);
        status.setGravity(Gravity.CENTER);
        box.addView(status, new LinearLayout.LayoutParams(-1, dp(82)));
        Button tap = button("COMEÇAR", PURPLE);
        box.addView(tap, new LinearLayout.LayoutParams(-1, dp(64)));
        final boolean[] running = {false};
        final int[] clicks = {0};
        final long[] endAt = {0L};
        final Runnable[] ticker = new Runnable[1];
        tap.setOnClickListener(v -> {
            if (!running[0]) {
                clicks[0] = 0;
                running[0] = true;
                endAt[0] = SystemClock.elapsedRealtime() + 5000L;
                tap.setText("TOQUE!  •  0");
                status.setText("5.0 segundos restantes");
                ticker[0] = new Runnable() {
                    @Override public void run() {
                        if (!running[0] || !dialog.isShowing()) return;
                        long left = endAt[0] - SystemClock.elapsedRealtime();
                        if (left <= 0) {
                            running[0] = false;
                            double cps = clicks[0] / 5.0;
                            status.setText("Resultado: " + clicks[0] + " cliques • " + String.format(java.util.Locale.US, "%.1f", cps) + " CPS");
                            tap.setText("TENTAR NOVAMENTE");
                            return;
                        }
                        status.setText(String.format(java.util.Locale.US, "%.1f segundos restantes", left / 1000.0));
                        handler.postDelayed(this, 80);
                    }
                };
                handler.post(ticker[0]);
            } else {
                clicks[0]++;
                tap.setText("TOQUE!  •  " + clicks[0]);
            }
        });
        dialog.setView(box);
        dialog.setOnDismissListener(d -> handler.removeCallbacksAndMessages(null));
        dialog.show();
    }

    private void showAimTrainer() {
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(18), dp(18), dp(18));
        box.setBackground(shape(PANEL, PURPLE, 18));
        TextView score = text("ALVOS ACERTADOS: 0", 17, Color.WHITE, true);
        score.setGravity(Gravity.CENTER);
        box.addView(score, new LinearLayout.LayoutParams(-1, dp(60)));
        Button target = button("●", PURPLE);
        box.addView(target, new LinearLayout.LayoutParams(-1, dp(170)));
        final int[] hits = {0};
        target.setOnClickListener(v -> {
            hits[0]++;
            score.setText("ALVOS ACERTADOS: " + hits[0]);
            target.setText(hits[0] % 2 == 0 ? "●" : "◎");
        });
        dialog.setView(box);
        dialog.show();
    }

    private void startOverlay() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Intent permission = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivity(permission);
            Toast.makeText(this, "Permita a sobreposição e volte ao HyFlez.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent);
        else startService(intent);
        Toast.makeText(this, "Overlay HyFlez ativado.", Toast.LENGTH_SHORT).show();
    }
}
