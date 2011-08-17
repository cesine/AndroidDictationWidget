/*
 * Copyright (C) 2007 The Android Open Source Project
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

package ca.ilanguage.aublog.service;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import ca.ilanguage.aublog.R;


/**
 * Controller to start and stop a service. 

Demonstrates how to pass information to the service via extras

Clicking on the notification brings user here, this is where user can do extra actions, like schedule uplaods for later, import transcriptions into aublog?

add buttons
Turn on wifi
Open aublog settings
Retry xxx audio file (add files to cue)
 */
public class NotifyingController extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notifying_controller);

        Button button = (Button) findViewById(R.id.notifyStart);
        button.setOnClickListener(mStartListener);
        button = (Button) findViewById(R.id.notifyStop);
        button.setOnClickListener(mStopListener);
    }

    private OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {
        	Intent intent = new Intent(NotifyingController.this, NotifyingTranscriptionService.class);
        	intent.putExtra(NotifyingTranscriptionService.EXTRA_AUDIOFILE_FULL_PATH, "/sdcard/AuBlog/test.mp3");
            startService(intent); 
        }
    };

    private OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {
        	Intent intent = new Intent(NotifyingController.this, NotifyingTranscriptionService.class);
        	intent.putExtra("error", "user clicked cancel");
            stopService(intent);
        }
    };
}

