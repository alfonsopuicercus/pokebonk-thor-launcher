package com.pokebonk.thor;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Native play entry and browser fallback for the official online game. */
public final class LauncherActivity extends Activity {
    private static final String GAME_URL = "https://pokebonk.com/";
    private static final String GUIDE = "Choose Play with native controls. The app reads Android stick/button events directly, with a fixed 22% deadzone. No touch mapping or screen gestures are used. Start + Select opens controller diagnostics and Release all inputs.\n\n"
        + "Default Xbox-style layout (positions may differ from the printed letters):\n"
        + "Left stick: move\nRight stick: camera\nBottom face / A: jump, hold to glide; confirm in menus\nRight face / B: dash; back in menus\nLeft face / X: interact\nTop face / Y: Ultimate\nLB: auto/manual aim\nRB: target lock\nLT: dash\nRT: Ultimate\nStart: pause\nSelect: map\nD-pad: menu navigation; up/down zoom during play\nLB/RB: menu tabs\n\n"
        + "Settings → Controls lets you remap buttons and adjust the stick deadzone. For performance, lower graphics quality in the game's settings.\n\n"
        + "Accounts and saves stay on the official pokebonk.com server. Native play has its own persistent app session, separate from Chrome. Sign in once using your existing Pokébonk username/password under Enter, or use Create account. As checked on 19 October 2026, the site does not offer Google sign-in. This launcher preserves the website's own sign-in options.\n\n"
        + "Chrome guest progress is not automatically imported into native play. Keep the browser fallback if you rely on a Chrome guest save. Uninstalling or clearing app data can remove the native guest session. Internet is required.\n\n"
        + "This is an unofficial launcher, not a game emulator or an offline copy. The game runs online in the app with normalized native controls. Physical AYN Thor acceptance is still pending.";

    @Override public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(22 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad, pad, pad);
        box.setBackgroundColor(0xff182338);
        scroll.addView(box);
        setContentView(scroll);
        TextView title = text("Pokébonk for Thor", 28);
        box.addView(title);
        box.addView(text("Official online game · native controls · official accounts and saves", 17));
        Button play = button("Play with native controls", () -> startActivity(new Intent(this, GameActivity.class)));
        box.addView(play);
        box.addView(button("Open in Chrome (browser fallback)", () -> openGame(true)));
        box.addView(button("Choose another browser", () -> openGame(false)));
        box.addView(button("Controls & sign-in", () -> new AlertDialog.Builder(this)
            .setTitle("Playing on your Thor").setMessage(GUIDE).setPositiveButton("OK", null).show()));
        box.addView(text("Native controls read the Thor directly. Sign in once inside the app for your existing account; Chrome guest data stays in Chrome. Google login is not currently offered by Pokébonk.", 15));
        play.requestFocus();
    }

    private TextView text(String value, int size) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(0xfff2f4f8);
        view.setPadding(0, 8, 0, 16);
        return view;
    }

    private Button button(String label, Runnable action) {
        Button view = new Button(this);
        view.setText(label);
        view.setAllCaps(false);
        view.setTextSize(18);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.setOnClickListener(v -> action.run());
        return view;
    }

    private void openGame(boolean preferChrome) {
        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(GAME_URL));
        browser.addCategory(Intent.CATEGORY_BROWSABLE);
        if (preferChrome) browser.setPackage("com.android.chrome");
        if (!preferChrome) browser = Intent.createChooser(browser, "Use the browser with your Pokébonk save");
        try {
            // On a Thor, keep the browser on the primary/top display.
            startActivity(browser, ActivityOptions.makeBasic().setLaunchDisplayId(Display.DEFAULT_DISPLAY).toBundle());
        } catch (ActivityNotFoundException missing) {
            if (preferChrome) openGame(false);
            else new AlertDialog.Builder(this).setMessage("Install Chrome or Firefox, then reopen this launcher.").setPositiveButton("OK", null).show();
        } catch (RuntimeException placementUnavailable) {
            // Some Android builds restrict moving tasks between screens.
            try { startActivity(browser); }
            catch (ActivityNotFoundException missing) {
                if (preferChrome) openGame(false);
                else new AlertDialog.Builder(this).setMessage("Install Chrome or Firefox, then reopen this launcher.").setPositiveButton("OK", null).show();
            }
        }
    }
}
