package com.appmarket.guardian;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, float size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(0xFF172033);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private void card(LinearLayout root, String heading, String detail) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(16), dp(14), dp(16), dp(14));
        item.setBackgroundColor(0xFFF0F4FA);
        TextView h = text(heading, 17, true);
        TextView d = text(detail, 13, false);
        d.setPadding(0, dp(6), 0, 0);
        item.addView(h);
        item.addView(d);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        root.addView(item, lp);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFFF7F9FC);
        getWindow().setNavigationBarColor(0xFFF7F9FC);
        getWindow().getDecorView().setSystemUiVisibility(0x00002000 | 0x00000010);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(28), dp(20), dp(20));
        root.setBackgroundColor(0xFFF7F9FC);

        TextView brand = text("AppMarket Guardian", 25, true);
        root.addView(brand);
        TextView subtitle = text("Telegram Managed Bots workspace • Preview 0.1.0", 13, false);
        subtitle.setPadding(0, dp(6), 0, dp(22));
        root.addView(subtitle);

        card(root, "Guardian Monitor", "Monitoring status: Not connected");
        card(root, "Backend heartbeat", "Waiting for the cloud backend URL and monitoring credentials.");
        card(root, "Telegram webhook", "Webhook health will appear after the backend is deployed.");
        card(root, "Recovery", "This app can alert and guide recovery. The cloud host, not Android, keeps the bot service online.");

        TextView footer = text("Prototype UI only — cloud connection and production controls are not enabled yet.", 12, false);
        footer.setPadding(0, dp(8), 0, 0);
        root.addView(footer);
        setContentView(root);
    }
}
