package com.sandersnewmedia.camera;

// Most of this code is based on the git project: https://github.com/vanevery/Custom-Video-Capture-with-Preview.git

//import android.app.Activity;
//import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CaptureActivity extends Activity implements SurfaceHolder.Callback {

    public static final String LOGTAG = "CAPTURE";

    private MediaRecorder recorder;
    private SurfaceHolder holder;
    private CamcorderProfile camcorderProfile;
    private Camera camera;

    boolean recording = false;      // Flag used to determine whether the video is being recorded or not.


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

        setContentView(R.layout.capture);

        SurfaceView cameraView = (SurfaceView) findViewById(R.id.CameraView);
        holder = cameraView.getHolder();

        holder.addCallback(this);             // Note: This is the line which causes the emulator app to crash
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

//        cameraView.setClickable(true);
//        cameraView.setOnClickListener(this);
    }

//    public void onClick(View v) {
//        if (recording) {
//            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
//            recorder.stop();
//            if (usecamera) {
//                try {
//                    camera.reconnect();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            // recorder.release();
//            recording = false;
//            Log.v(LOGTAG, "Recording Stopped");
//            // Let's prepareRecorder so we can record again
//            prepareRecorder();
//        } else {
//            Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
//
//            recording = true;
//            recorder.start();
//            Log.v(LOGTAG, "Recording Started");
//        }
//    }

    public void onRecordClick(View v) {

        Button btnRecord = (Button) findViewById(R.id.RecordButton);

        if (recording)          // If already recording clicking the button is supposed to stop the recording.
        {
            btnRecord.setText("Record");
            recording = false;

            recorder.stop();

            try {
                camera.reconnect();
            }
            catch (IOException e)
            {
                Log.e(LOGTAG, "Failed to reconnect camera.");
            }

            prepareRecorder();      // Set up recorder fo the next recording

//            Toast.makeText(this, "Stop Recording!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            btnRecord.setText("Pause");
            recording = true;

            recorder.start();

//            Toast.makeText(this, "Start Recording!", Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareRecorder() {
        recorder = new MediaRecorder();
        recorder.setPreviewDisplay(holder.getSurface());

        camera.unlock();
        recorder.setCamera(camera);

        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        recorder.setProfile(camcorderProfile);

        try {
            File newFile = File.createTempFile("videocapture", ".mp4", Environment.getExternalStorageDirectory());
            recorder.setOutputFile(newFile.getAbsolutePath());
        } catch (IOException e) {
            Log.w(LOGTAG,"Couldn't create file");
            e.printStackTrace();
            finish();
        }

        try {
            recorder.prepare();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        }
        catch (IOException e) {
            e.printStackTrace();
            finish();
        }

        Log.e(LOGTAG, "prepareRecorder exitted successfully!");
    }


    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(LOGTAG, "surfaceCreated");

        camera = Camera.open();
        camera.setDisplayOrientation(90);       // Ensures that the camera is displaying correctly when the phone is in Portrait Mode.

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }
        catch (IOException e) {
            Log.e(LOGTAG,e.getMessage());
            e.printStackTrace();
        }
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(LOGTAG, "surfaceChanged");

        if (!recording) {
                camera.stopPreview();

            try {
                Camera.Parameters p = camera.getParameters();

                p.setPreviewSize(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight);
                p.setPreviewFrameRate(camcorderProfile.videoFrameRate);

                camera.setParameters(p);

                camera.setPreviewDisplay(holder);
                camera.startPreview();
            }
            catch (IOException e) {
                Log.e(LOGTAG,e.getMessage());
                e.printStackTrace();
            }

            prepareRecorder();
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder) {

        Log.v(LOGTAG, "surfaceDestroyed");

        if (recording) {
            recorder.stop();
            recording = false;
        }

        recorder.release();
        camera.release();

        finish();
    }
}