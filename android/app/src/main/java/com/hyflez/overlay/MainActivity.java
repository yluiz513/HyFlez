package com.hyflez.overlay;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;

public class MainActivity extends Activity {
    private int dp(float value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(Color.rgb(12, 12, 20));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));

        TextView title = new TextView(this);
        title.setText("HYFLEZ");
        title.setTextColor(Color.rgb(190, 120, 255));
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);
        title.setLetterSpacing(0.12f);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(60)));

        TextView info = new TextView(this);
        info.setText("RGB OVERLAY • ANDROID\n\nAtive a permissão de sobreposição para mostrar a bolinha por cima do Minecraft.\n\nO painel inicial oferece controles visuais do HyFlez; ele não altera a memória nem automatiza ações do jogo.");
        info.setTextColor(Color.WHITE);
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, -2);
        ip.setMargins(0, dp(16), 0, dp(24));
        root.addView(info, ip);

        Button start = new Button(this);
        start.setText("ATIVAR BOLINHA FLUTUANTE");
        start.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                Intent permission = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                startActivity(permission);
            } else {
                startOverlay();
            }
        });
        root.addView(start, new LinearLayout.LayoutParams(-1, dp(56)));

        TextView note = new TextView(this);
        note.setText("Depois de permitir, volte ao HyFlez e toque no botão novamente.");
        note.setTextColor(Color.LTGRAY);
        note.setTextSize(13);
        note.setGravity(Gravity.CENTER);
        root.addView(note, new LinearLayout.LayoutParams(-1, dp(60)));

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startOverlay() {
        Intent intent = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent);
        else startService(intent);
        finish();
    }
}
