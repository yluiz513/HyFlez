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
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OverlayService extends Service {
    private WindowManager wm;
    private View bubble, panel, trainingHud;
    private WindowManager.LayoutParams bubbleParams, panelParams, hudParams;
    private boolean panelOpen = false;
    private boolean rgbEnabled = true;
    private boolean compact = false;
    private boolean trainingEnabled = false;

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onCreate() {
        super.onCreate();
        startForegroundNotification();
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        createBubble();
    }

    private void startForegroundNotification() {
        String channelId = "hyflez_overlay";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelId, "HyFlez Overlay", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, channelId) : new Notification.Builder(this);
        startForeground(7, builder.setContentTitle("HyFlez está ativo")
            .setContentText("Toque na bolinha para abrir os controles.")
            .setSmallIcon(android.R.drawable.ic_dialog_info).setOngoing(true).build());
    }

    private GradientDrawable shape(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(20));
        d.setStroke(dp(2), stroke);
        return d;
    }

    private TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(Color.WHITE);
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private int windowType() {
        return Build.VERSION.SDK_INT >= 26
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;
    }

    private void createBubble() {
        TextView b = text("H", 21);
        b.setTypeface(null, 1);
        bubble = b;
        applyBubbleStyle();
        bubbleParams = new WindowManager.LayoutParams(dp(58), dp(58), windowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        bubbleParams.gravity = Gravity.TOP | Gravity.LEFT;
        bubbleParams.x = dp(12);
        bubbleParams.y = dp(220);
        b.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY; int startX, startY; long downTime;
            boolean moved;
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    downX=e.getRawX(); downY=e.getRawY();
                    startX=bubbleParams.x; startY=bubbleParams.y;
                    downTime=System.currentTimeMillis(); moved=false;
                    return true;
                }
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    int dx=(int)(e.getRawX()-downX), dy=(int)(e.getRawY()-downY);
                    if (Math.abs(dx)>dp(5) || Math.abs(dy)>dp(5)) moved=true;
                    bubbleParams.x=startX+dx; bubbleParams.y=startY+dy;
                    wm.updateViewLayout(bubble,bubbleParams);
                    return true;
                }
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    if (!moved && System.currentTimeMillis()-downTime < 600) togglePanel();
                    return true;
                }
                return true;
            }
        });
        wm.addView(bubble, bubbleParams);
    }

    private void applyBubbleStyle() {
        if (bubble != null) {
            bubble.setBackground(shape(Color.rgb(35,18,58),
                rgbEnabled ? Color.rgb(190,80,255) : Color.GRAY));
        }
    }

    private void togglePanel() {
        if (panelOpen) { closePanel(); return; }
        buildPanel();
    }

    private void buildPanel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(12));
        box.setBackground(shape(Color.rgb(17,17,29),
            rgbEnabled ? Color.rgb(160,70,255) : Color.GRAY));

        TextView heading = text("HYFLEZ  •  RGB", 18);
        heading.setTextColor(Color.rgb(205,130,255));
        box.addView(heading, new LinearLayout.LayoutParams(-1, dp(44)));

        TextView subtitle = text("CONTROLES DO OVERLAY", 10);
        subtitle.setTextColor(Color.LTGRAY);
        box.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(24)));

        addToggle(box, "Efeito RGB", rgbEnabled, () -> {
            rgbEnabled = !rgbEnabled;
            applyBubbleStyle();
            rebuildPanel();
        });
        addToggle(box, "Modo compacto", compact, () -> {
            compact = !compact;
            rebuildPanel();
        });
        addToggle(box, "HUD de treino", trainingEnabled, () -> {
            trainingEnabled = !trainingEnabled;
            updateTrainingHud();
            rebuildPanel();
        });

        TextView info = text("O HUD é apenas visual e não altera o Minecraft.", 10);
        info.setTextColor(Color.LTGRAY);
        info.setPadding(dp(4), dp(8), dp(4), dp(8));
        box.addView(info, new LinearLayout.LayoutParams(-1, compact ? dp(36) : dp(48)));

        TextView close = text("MINIMIZAR", 13);
        close.setBackground(shape(Color.rgb(55,25,80), Color.rgb(210,90,255)));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(42));
        cp.topMargin = dp(6);
        box.addView(close, cp);
        close.setOnClickListener(v -> closePanel());

        panel = box;
        panelParams = new WindowManager.LayoutParams(dp(280), -2, windowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        panelParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        panelParams.x = dp(8);
        wm.addView(panel, panelParams);
        panelOpen = true;
    }

    private void addToggle(LinearLayout parent, String label, boolean enabled, Runnable action) {
        TextView row = text(label + "     " + (enabled ? "● ON" : "○ OFF"), 14);
        row.setBackground(shape(Color.rgb(34,30,48),
            enabled ? Color.rgb(150,75,220) : Color.rgb(75,75,90)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(compact ? 42 : 50));
        p.topMargin = dp(6);
        parent.addView(row, p);
        row.setOnClickListener(v -> action.run());
    }

    private void rebuildPanel() {
        if (panel != null) {
            try { wm.removeView(panel); } catch (Exception ignored) {}
            panel = null;
        }
        panelOpen = false;
        buildPanel();
    }

    private void updateTrainingHud() {
        if (trainingEnabled) {
            if (trainingHud == null) {
                TextView hud = text("HYFLEZ  •  TREINO ATIVO", 12);
                hud.setPadding(dp(12), dp(6), dp(12), dp(6));
                hud.setBackground(shape(Color.rgb(20,20,32), Color.rgb(190,80,255)));
                trainingHud = hud;
                hudParams = new WindowManager.LayoutParams(-2, -2, windowType(),
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT);
                hudParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                hudParams.y = dp(40);
                wm.addView(trainingHud, hudParams);
            }
        } else if (trainingHud != null) {
            try { wm.removeView(trainingHud); } catch (Exception ignored) {}
            trainingHud = null;
        }
    }

    private void closePanel() {
        if (panel != null) {
            try { wm.removeView(panel); } catch (Exception ignored) {}
            panel = null;
        }
        panelOpen = false;
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override public void onDestroy() {
        closePanel();
        if (trainingHud != null) {
            try { wm.removeView(trainingHud); } catch (Exception ignored) {}
            trainingHud = null;
        }
        if (bubble != null) {
            try { wm.removeView(bubble); } catch (Exception ignored) {}
            bubble = null;
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}