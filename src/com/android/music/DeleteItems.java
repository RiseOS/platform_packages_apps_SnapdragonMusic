/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.music;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;
import android.net.Uri;
import android.widget.Toast;

public class DeleteItems extends Activity
{
    private TextView mPrompt;
    private Button mButton;
    private long [] mItemList;
    private Uri mPlaylistUri;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.confirm_delete);
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT);

        mPrompt = (TextView)findViewById(R.id.prompt);
        mButton = (Button) findViewById(R.id.delete);
        mButton.setOnClickListener(mButtonClicked);

        ((Button)findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        Bundle b = getIntent().getExtras();
        String desc = b.getString("description");
        mPlaylistUri = b.getParcelable("Playlist");
        if (mPlaylistUri == null) {
            mItemList = b.getLongArray("items");
        }
        mPrompt.setText(desc);

        // Register broadcast receiver can monitor system language change.
        IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(mLanguageChangeReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver can monitor system language change.
        unregisterReceiver(mLanguageChangeReceiver);
    }

    @Override
    public void onUserLeaveHint() {
        finish();
        super.onUserLeaveHint();
    }

    // Broadcast receiver monitor system language change, if language changed
    // will finsh current activity, same as system alter dialog.
    private BroadcastReceiver mLanguageChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_LOCALE_CHANGED)) {
                finish();
            }
        }
    };
    
    private View.OnClickListener mButtonClicked = new View.OnClickListener() {
        public void onClick(View v) {
            // delete the selected Playlist
            if (mPlaylistUri != null) {
                getContentResolver().delete(mPlaylistUri, null, null);
                Toast.makeText(DeleteItems.this,
                        R.string.playlist_deleted_message,
                        Toast.LENGTH_SHORT).show();
            } else {
                // delete the selected item(s)
                MusicUtils.deleteTracks(DeleteItems.this, mItemList);
            }
            finish();
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP &&
                event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.dispatchKeyEvent(event);
    };
}
