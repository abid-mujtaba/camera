package com.sandersnewmedia.camera;

// Most of this code is based on the git project: https://github.com/vanevery/Custom-Video-Capture-with-Preview.git


import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

import android.widget.Toast;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;


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
    }


    private void uploadVideo(File video)
    {
        AWSCredentials credential = new BasicAWSCredentials("access key", "secret key");
        TransferManager manager = new TransferManager(credential);

        Upload upload = manager.upload("bucket", "key", video);

        Toast.makeText(this, "Uploading Video", Toast.LENGTH_SHORT).show();

        try
        {
            UploadResult result = upload.waitForUploadResult();
            Toast.makeText(this, "Video Uploaded!", Toast.LENGTH_SHORT).show();
        }
        catch (InterruptedException e)
        {
            Log.e(LOGTAG, "Upload Interrupted");
            Toast.makeText(this, "Uploading Failed!", Toast.LENGTH_SHORT).show();
        }
    }


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
        }
        else
        {
            btnRecord.setText("Pause");
            recording = true;

            prepareRecorder();
            recorder.start();
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

        Time now = new Time();
        now.setToNow();
        String filename = now.format("Video-%Y-%m-%d-%H:%M:%S.mp4");

        File videoFile = new File(this.getExternalFilesDir(null), filename);        // We save the video file to the semi-public app folder in the External Storage

        recorder.setOutputFile(videoFile.getAbsolutePath());

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