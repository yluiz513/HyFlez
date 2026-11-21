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

public class OverlayService extends Service {
    private WindowManager wm;
    private View bubble;
    private View panel;
    private WindowManager.LayoutParams bubbleParams;
    private WindowManager.LayoutParams panelParams;
    private boolean panelOpen = false;

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
        Notification notification = builder.setContentTitle("HyFlez está ativo")
            .setContentText("Toque na bolinha para abrir o painel.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true).build();
        startForeground(7, notification);
    }

    private GradientDrawable shape(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(24));
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
        b.setBackground(shape(Color.rgb(35, 18, 58), Color.rgb(190, 80, 255)));
        bubble = b;
        bubbleParams = new WindowManager.LayoutParams(dp(58), dp(58), windowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        bubbleParams.gravity = Gravity.TOP | Gravity.LEFT;
        bubbleParams.x = dp(12);
        bubbleParams.y = dp(220);
        b.setOnClickListener(v -> togglePanel());
        b.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY; int startX, startY; long downTime;
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    downX=e.getRawX(); downY=e.getRawY();
                    startX=bubbleParams.x; startY=bubbleParams.y; downTime=System.currentTimeMillis();
                    return true;
                }
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    bubbleParams.x=startX+(int)(e.getRawX()-downX);
                    bubbleParams.y=startY+(int)(e.getRawY()-downY);
                    wm.updateViewLayout(bubble,bubbleParams);
                    return true;
                }
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    if (System.currentTimeMillis()-downTime < 220 &&
                        Math.abs(e.getRawX()-downX)<12 && Math.abs(e.getRawY()-downY)<12) togglePanel();
                    return true;
                }
                return false;
            }
        });
        wm.addView(bubble, bubbleParams);
    }

    private void togglePanel() {
        if (panelOpen) {
            closePanel();
            return;
        }
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(12));
        box.setBackground(shape(Color.rgb(17, 17, 29), Color.rgb(160, 70, 255)));

        TextView heading = text("HYFLEZ  •  RGB", 18);
        heading.setTextColor(Color.rgb(205, 130, 255));
        box.addView(heading, new LinearLayout.LayoutParams(-1, dp(48)));

        TextView subtitle = text("OVERLAY ATIVO", 11);
        subtitle.setTextColor(Color.LTGRAY);
        box.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(28)));

        addAction(box, "Combat", "Área de treino / sem automação");
        addAction(box, "Movement", "Atalhos visuais");
        addAction(box, "Visual", "Efeitos do painel");
        addAction(box, "Player", "Perfil local");
        addAction(box, "Settings", "Fechar ou minimizar");
        TextView close = text("MINIMIZAR", 13);
        close.setBackground(shape(Color.rgb(55, 25, 80), Color.rgb(210, 90, 255)));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(42));
        cp.topMargin = dp(8);
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

    private void addAction(LinearLayout parent, String title, String detail) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(8), dp(5), dp(8), dp(5));
        TextView name = text(title, 15);
        name.setTextColor(Color.WHITE);
        TextView desc = text(detail, 10);
        desc.setTextColor(Color.LTGRAY);
        item.addView(name, new LinearLayout.LayoutParams(-1, dp(28)));
        item.addView(desc, new LinearLayout.LayoutParams(-1, dp(24)));
        parent.addView(item, new LinearLayout.LayoutParams(-1, dp(56)));
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
        if (bubble != null) {
            try { wm.removeView(bubble); } catch (Exception ignored) {}
            bubble = null;
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
