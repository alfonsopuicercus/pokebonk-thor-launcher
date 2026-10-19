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

/** Opens the official game in a real browser, retaining that browser's account and storage. */
public final class LauncherActivity extends Activity {
    private static final String GAME_URL = "https://pokebonk.com/";
    private static final String GUIDE = "Use Thor's normal Android gamepad mode. Disable touch-mapping overlays for this game. Open the game on the top screen, tap once for sound, then press a face button with both sticks released.\n\n"
        + "Default Xbox-style layout (positions may differ from the printed letters):\n"
        + "Left stick: move\nRight stick: camera\nBottom face / A: jump, hold to glide; confirm in menus\nRight face / B: dash; back in menus\nLeft face / X: interact\nTop face / Y: Ultimate\nLB: auto/manual aim\nRB: target lock\nLT: dash\nRT: Ultimate\nStart: pause\nSelect: map\nD-pad: menu navigation; up/down zoom during play\nLB/RB: menu tabs\n\n"
        + "Settings → Controls lets you remap buttons and adjust the stick deadzone. For performance, lower graphics quality in the game's settings.\n\n"
        + "Accounts and saves stay on pokebonk.com in your browser. Use Enter for an existing username/password account, or Create account. As checked on 19 October 2026, the site does not offer Google sign-in. This launcher preserves the website's own sign-in options.\n\n"
        + "Keep using the same browser and its normal profile. A different browser or private/incognito mode has separate guest progress and sessions. Do not clear browser site data if you rely on a guest save. Internet is required.\n\n"
        + "This is an unofficial launcher, not a game emulator or an offline copy. Pokébonk already supplies the controller support. Physical AYN Thor acceptance is still pending.";

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
        box.addView(text("Official online game · physical controls · browser accounts and saves", 17));
        Button play = button("Play in Chrome", () -> openGame(true));
        box.addView(play);
        box.addView(button("Choose another browser", () -> openGame(false)));
        box.addView(button("Controls & sign-in", () -> new AlertDialog.Builder(this)
            .setTitle("Playing on your Thor").setMessage(GUIDE).setPositiveButton("OK", null).show()));
        box.addView(text("No scripts or companion service needed. Use the same browser as your existing save. Google login is not currently offered by Pokébonk.", 15));
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
