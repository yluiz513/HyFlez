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
            {"TEXTURES", "▧  Texturas"}, {"WORLDS", "▦  Worlds"}, {"TOOLS", "⚙  Tools"}
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
