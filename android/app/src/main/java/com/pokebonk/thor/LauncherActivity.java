package com.pokebonk.thor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/** Keeps the existing app icon while opening or resuming native play immediately. */
public final class LauncherActivity extends Activity {
    @Override public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        startActivity(new Intent(this, GameActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        finish();
    }
}
