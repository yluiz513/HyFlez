package com.hyflez.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager wm;
    private View bubble, panel, trainingHud;
    private WindowManager.LayoutParams bubbleParams, panelParams, hudParams;
    private boolean panelOpen = false, rgbEnabled = true, compact = false, trainingEnabled = false;
    private boolean clickCounterEnabled = false, movementTimerEnabled = false, playerInfoEnabled = false;
    private int clickCount = 0, rgbIndex = 0;
    private String currentTab = "Combat";
    private TextView clickCountView, timerView;
    private long timerStartedAt = 0L, timerElapsed = 0L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final int[] rgbColors = new int[] {
        Color.rgb(255, 55, 120), Color.rgb(255, 145, 45), Color.rgb(220, 235, 55),
        Color.rgb(45, 230, 150), Color.rgb(40, 190, 255), Color.rgb(145, 75, 255)
    };
    private final Runnable rgbRunnable = new Runnable() {
        @Override public void run() {
            if (rgbEnabled) {
                rgbIndex = (rgbIndex + 1) % rgbColors.length;
                refreshBorders();
                handler.postDelayed(this, 350);
            }
        }
    };
    private final Runnable timerRunnable = new Runnable() {
        @Override public void run() {
            if (movementTimerEnabled) {
                long total = timerElapsed + (System.currentTimeMillis() - timerStartedAt);
                if (timerView != null) timerView.setText("TEMPO: " + formatTime(total));
                handler.postDelayed(this, 500);
            }
        }
    };

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onCreate() {
        super.onCreate();
        startForegroundNotification();
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        createBubble();
        handler.post(rgbRunnable);
    }

    private void startForegroundNotification() {
        String channelId = "hyflez_overlay";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelId, "HyFlez Overlay", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, channelId) : new Notification.Builder(this);
        startForeground(7, builder.setContentTitle("HyFlez está ativo").setContentText("Toque na bolinha para abrir os controles.")
            .setSmallIcon(android.R.drawable.ic_dialog_info).setOngoing(true).build());
    }

    private GradientDrawable shape(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(18)); d.setStroke(dp(2), stroke);
        return d;
    }

    private TextView text(String value, int size) {
        TextView t = new TextView(this); t.setText(value); t.setTextColor(Color.WHITE);
        t.setTextSize(size); t.setGravity(Gravity.CENTER); return t;
    }

    private int accent() { return rgbEnabled ? rgbColors[rgbIndex] : Color.rgb(115, 115, 135); }

    private int windowType() {
        return Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
    }

    private void createBubble() {
        TextView b = text("H", 21); b.setTypeface(null, 1); bubble = b; applyBubbleStyle();
        bubbleParams = new WindowManager.LayoutParams(dp(58), dp(58), windowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        bubbleParams.gravity = Gravity.TOP | Gravity.LEFT; bubbleParams.x = dp(12); bubbleParams.y = dp(220);
        b.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY; int startX, startY; boolean moved;
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    downX=e.getRawX(); downY=e.getRawY(); startX=bubbleParams.x; startY=bubbleParams.y; moved=false; return true;
                }
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    int dx=(int)(e.getRawX()-downX), dy=(int)(e.getRawY()-downY);
                    if (Math.abs(dx)>dp(5) || Math.abs(dy)>dp(5)) moved=true;
                    bubbleParams.x=startX+dx; bubbleParams.y=startY+dy; wm.updateViewLayout(bubble,bubbleParams); return true;
                }
                if (e.getAction() == MotionEvent.ACTION_UP) { if (!moved) togglePanel(); return true; }
                return true;
            }
        });
        wm.addView(bubble, bubbleParams);
    }

    private void applyBubbleStyle() {
        if (bubble != null) bubble.setBackground(shape(Color.rgb(25,18,38), accent()));
    }

    private void refreshBorders() {
        applyBubbleStyle();
        if (panel != null) panel.setBackground(shape(Color.rgb(16,16,27), accent()));
        if (trainingHud != null) trainingHud.setBackground(shape(Color.rgb(20,20,32), accent()));
    }

    private void togglePanel() { if (panelOpen) closePanel(); else buildPanel(); }

    private void buildPanel() {
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setPadding(dp(12), dp(10), dp(12), dp(10));
        outer.setBackground(shape(Color.rgb(16,16,27), accent()));

        TextView heading = text("HYFLEZ  •  CONTROL CENTER", 16);
        heading.setTextColor(accent());
        outer.addView(heading, new LinearLayout.LayoutParams(-1, dp(40)));

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        String[] names = {"Combat", "HUD", "Visual", "World", "Utility", "Settings"};
        for (String name : names) {
            String tabName = name;
            TextView tab = text(name, 10);
            tab.setBackground(shape(Color.rgb(38,32,52), currentTab.equals(tabName) ? accent() : Color.rgb(75,70,85)));
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, dp(38), 1);
            tp.setMargins(dp(1), dp(2), dp(1), dp(5));
            tabs.addView(tab, tp);
            tab.setOnClickListener(v -> { currentTab = tabName; rebuildPanel(); });
        }
        outer.addView(tabs);

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        addCategoryContent(content);
        scroll.addView(content);
        outer.addView(scroll, new LinearLayout.LayoutParams(-1, dp(compact ? 230 : 310), 1));

        TextView close = text("MINIMIZAR", 12);
        close.setBackground(shape(Color.rgb(48,25,65), accent()));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(40));
        cp.topMargin = dp(6); outer.addView(close, cp);
        close.setOnClickListener(v -> closePanel());

        panel = outer;
        panelParams = new WindowManager.LayoutParams(dp(310), -2, windowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        panelParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL; panelParams.x = dp(8);
        wm.addView(panel, panelParams); panelOpen = true;
    }

    private void addCategoryContent(LinearLayout content) {
        if ("Combat".equals(currentTab)) {
            section(content, "COMBAT • TREINO MANUAL");
            addToggle(content, "Contador de golpes", clickCounterEnabled, () -> { clickCounterEnabled=!clickCounterEnabled; rebuildPanel(); });
            addButton(content, "REGISTRAR GOLPE", "Toques registrados: " + clickCount, () -> { clickCount++; if(clickCountView!=null) clickCountView.setText("Toques registrados: " + clickCount); });
            TextView count = text("Toques registrados: " + clickCount, 12); count.setTextColor(Color.LTGRAY);
            content.addView(count, new LinearLayout.LayoutParams(-1, dp(34))); clickCountView = count;
            note(content, "Ferramentas de treino manual. Não atacam automaticamente nem alteram o jogo.");
        } else if ("HUD".equals(currentTab)) {
            section(content, "HUD • INFORMAÇÕES LOCAIS");
            addToggle(content, "HUD de treino", trainingEnabled, () -> { trainingEnabled=!trainingEnabled; updateTrainingHud(); rebuildPanel(); });
            addToggle(content, "Status do painel", playerInfoEnabled, () -> { playerInfoEnabled=!playerInfoEnabled; rebuildPanel(); });
            note(content, "O overlay não consegue ler FPS, coordenadas ou vida do Minecraft sem uma integração suportada pelo próprio jogo.");
            if (playerInfoEnabled) note(content, "HyFlez ativo • Sessão local conectada");
        } else if ("World".equals(currentTab)) {
            section(content, "WORLD • BRIDGE");
            note(content, "O HyFlez Bridge funciona somente em mundos onde o pacote de comportamento foi instalado e ativado.");
            note(content, "Status atual: o APK não consegue confirmar sozinho se o Bridge está ativo dentro do mundo.");
            addButton(content, "ABRIR MINECRAFT", "Iniciar o jogo", () -> {
                Intent launch = getPackageManager().getLaunchIntentForPackage("com.mojang.minecraftpe");
                if (launch != null) { launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(launch); }
                else note(content, "Minecraft não encontrado neste aparelho.");
            });
        } else if ("Utility".equals(currentTab)) {
            section(content, "UTILITY • FERRAMENTAS");
            addToggle(content, "Cronômetro de treino", movementTimerEnabled, () -> {
                if (!movementTimerEnabled) { movementTimerEnabled=true; timerStartedAt=System.currentTimeMillis(); handler.post(timerRunnable); }
                else { timerElapsed += System.currentTimeMillis()-timerStartedAt; movementTimerEnabled=false; handler.removeCallbacks(timerRunnable); }
                rebuildPanel();
            });
            timerView = text("TEMPO: " + formatTime(timerElapsed + (movementTimerEnabled ? System.currentTimeMillis()-timerStartedAt : 0)), 16);
            timerView.setTextColor(accent()); content.addView(timerView, new LinearLayout.LayoutParams(-1, dp(42)));
            addButton(content, "ZERAR CRONÔMETRO", "00:00", () -> { timerElapsed=0; if(movementTimerEnabled) timerStartedAt=System.currentTimeMillis(); if(timerView!=null) timerView.setText("TEMPO: 00:00"); });
            note(content, "Cronômetro independente para sessões de treino.");
        } else if ("Visual".equals(currentTab)) {
            section(content, "VISUAL • PERSONALIZAÇÃO");
            addToggle(content, "Efeito RGB animado", rgbEnabled, () -> {
                rgbEnabled=!rgbEnabled;
                if(rgbEnabled) handler.post(rgbRunnable); else handler.removeCallbacks(rgbRunnable);
                refreshBorders(); rebuildPanel();
            });
            addToggle(content, "Modo compacto", compact, () -> { compact=!compact; rebuildPanel(); });
            addToggle(content, "HUD de treino", trainingEnabled, () -> { trainingEnabled=!trainingEnabled; updateTrainingHud(); rebuildPanel(); });
            note(content, "Personaliza somente a aparência do overlay.");
        } else {
            section(content, "SETTINGS • CONFIGURAÇÕES");
            addToggle(content, "Efeito RGB", rgbEnabled, () -> {
                rgbEnabled=!rgbEnabled;
                if(rgbEnabled) handler.post(rgbRunnable); else handler.removeCallbacks(rgbRunnable);
                refreshBorders(); rebuildPanel();
            });
            addToggle(content, "Modo compacto", compact, () -> { compact=!compact; rebuildPanel(); });
            addButton(content, "REINICIAR CONTADOR", "Zerar", () -> { clickCount=0; rebuildPanel(); });
            note(content, "O HyFlez funciona como overlay visual. Não injeta código no Minecraft.");
        }
    }

    private void section(LinearLayout parent, String title) {
        TextView t=text(title,11); t.setTextColor(accent()); t.setGravity(Gravity.CENTER_VERTICAL);
        parent.addView(t,new LinearLayout.LayoutParams(-1,dp(34)));
    }

    private void note(LinearLayout parent, String value) {
        TextView t=text(value,10); t.setTextColor(Color.LTGRAY); t.setPadding(dp(4),dp(5),dp(4),dp(5));
        parent.addView(t,new LinearLayout.LayoutParams(-1,-2));
    }

    private void addToggle(LinearLayout parent, String label, boolean enabled, Runnable action) {
        TextView row=text(label + "     " + (enabled ? "● ON" : "○ OFF"),12);
        row.setBackground(shape(Color.rgb(35,30,48),enabled ? accent() : Color.rgb(75,75,90)));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(compact?40:46)); p.topMargin=dp(5);
        parent.addView(row,p); row.setOnClickListener(v -> action.run());
    }

    private void addButton(LinearLayout parent, String label, String detail, Runnable action) {
        TextView row=text(label + "\n" + detail,11);
        row.setBackground(shape(Color.rgb(43,30,58),accent()));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52)); p.topMargin=dp(5);
        parent.addView(row,p); row.setOnClickListener(v -> action.run());
    }

    private String formatTime(long ms) {
        long sec=ms/1000; return String.format(java.util.Locale.US,"%02d:%02d",sec/60,sec%60);
    }

    private void rebuildPanel() {
        if(panel!=null) { try { wm.removeView(panel); } catch(Exception ignored) {} panel=null; }
        panelOpen=false; buildPanel();
    }

    private void updateTrainingHud() {
        if(trainingEnabled) {
            if(trainingHud==null) {
                TextView hud=text("HYFLEZ  •  TREINO ATIVO",12); hud.setPadding(dp(12),dp(6),dp(12),dp(6));
                hud.setBackground(shape(Color.rgb(20,20,32),accent())); trainingHud=hud;
                hudParams=new WindowManager.LayoutParams(-2,-2,windowType(),
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,PixelFormat.TRANSLUCENT);
                hudParams.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL; hudParams.y=dp(40); wm.addView(trainingHud,hudParams);
            }
        } else if(trainingHud!=null) { try { wm.removeView(trainingHud); } catch(Exception ignored) {} trainingHud=null; }
    }

    private void closePanel() {
        if(panel!=null) { try { wm.removeView(panel); } catch(Exception ignored) {} panel=null; }
        panelOpen=false;
    }

    @Override public int onStartCommand(Intent intent,int flags,int startId) { return START_STICKY; }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        closePanel();
        if(trainingHud!=null) { try { wm.removeView(trainingHud); } catch(Exception ignored) {} trainingHud=null; }
        if(bubble!=null) { try { wm.removeView(bubble); } catch(Exception ignored) {} bubble=null; }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}