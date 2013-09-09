package com.sandersnewmedia.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class IntroActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
    }

    public void onClickCapture(View view)
    {
        startActivity(new Intent("com.sandersnewmedia.camera.CaptureActivity"));
    }
}
