package com.example.android.animationsdemo;

/**
 * Created by lfredericks on 3/17/2015.
 */
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by IntelliJ IDEA.
 * User: Jim
 * Date: 3/21/13
 * Time: 1:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
    String LOG_TAG = "Camera Direct Preview" ;

    Camera _camera;
    SurfaceHolder _holder;

    public CameraPreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public CameraPreview(Context context) {
        super(context);
    }

    public void connectCamera(Camera camera, int cameraId) {
        _camera = camera;

        int previewOrientation = CameraHelper.getDisplayOrientationForCamera(getContext(), cameraId);
        _camera.setDisplayOrientation(previewOrientation);

        _holder = getHolder();
        _holder.addCallback(this);

        // Start Preview
        startPreview();
    }

    public void releaseCamera() {
        if (_camera != null) {
            // Stop Preview
            stopPreview();

            _camera = null;
        }
    }

    void startPreview() {
        if(_camera != null && _holder.getSurface() != null) {
            try {
                _camera.setPreviewDisplay(_holder);
                _camera.startPreview();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error setting preview display: " + e.getMessage());
            }
        }
    }

    void stopPreview() {
        if (_camera != null) {
            try {
                _camera.stopPreview();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error stopping preview: " + e.getMessage());
            }
        }
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        startPreview();
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        stopPreview();
        startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopPreview();
    }

}
