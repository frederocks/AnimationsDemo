package com.example.android.animationsdemo;

/**
 * Created by lfredericks on 3/17/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;


public class CameraActivity extends Activity
{
    final String LOG_TAG = "Camera Direct Access" ;

    public final static String ACTIVITY_TITLE_EXTRA = "com.example.android.animationsdemo.ACTIVITY_TITLE";
    public final static String SELECTIONS_EXTRA = "com.example.android.animationsdemo.SELECTIONS";
    public final static String SELECTED_INDEX_EXTRA = "com.example.android.animationsdemo.SELECTED_INDEX";

    final String CAMERA_SIZE_DISPLAY_FORMAT = "%d x %d";
    final String SELECTED_CAMERA_ID_KEY = "_selectedCameraId";
    final int CAMERA_ID_NOT_SET = -1;
    final int NOT_SET = -1;

    final int PICTURE_SIZE_SELECTION_REQUEST_CODE = 2100;

    int _frontFacingCameraId = CAMERA_ID_NOT_SET;
    int _backFacingCameraId = CAMERA_ID_NOT_SET;

    boolean _hasCamera = false;
    boolean _hasFrontCamera = false;

    int _selectedCameraId  = CAMERA_ID_NOT_SET;
    Camera _selectedCamera;

    Camera.Parameters _cameraParameters = null;
    List<Camera.Size> _supportedPictureSizes = null;
    Camera.Size _selectedPictureSize = null;

    int _currentZoom = NOT_SET;
    int _maxZoom = NOT_SET;
    boolean _isSmoothZoomSupported = false;
    boolean _isZoomSupported = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        PackageManager pm = getPackageManager();
        _hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
        _hasFrontCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);

        if(!_hasCamera)
            showNoCameraDialog();

        if (savedInstanceState != null)
            _selectedCameraId = savedInstanceState.getInt(SELECTED_CAMERA_ID_KEY, CAMERA_ID_NOT_SET);

        setupCameraControlsEventHandlers();
        manageCameraControls(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SELECTED_CAMERA_ID_KEY, _selectedCameraId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater() ;
        inflater.inflate(R.menu.camera_menu, menu) ;

        if(!_hasCamera)
            disableCameraMenuItems(menu);
        else if(!_hasFrontCamera)
            disableFrontCameraMenuItems(menu);

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseSelectedCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openSelectedCamera();
    }

    public void onMenuOpenBackCamera(MenuItem item) {
        logMenuChoice(item);

        _selectedCameraId = getBackFacingCameraId();
        openSelectedCamera();

    }

    public void onMenuOpenFrontCamera(MenuItem item) {
        logMenuChoice(item);

        _selectedCameraId = getFrontFacingCameraId();
        openSelectedCamera();
    }

    public void onMenuCloseCamera(MenuItem item) {
        logMenuChoice(item);

        releaseSelectedCamera();
        _selectedCameraId = CAMERA_ID_NOT_SET;

    }

    public void onExit(MenuItem item) {
        logMenuChoice(item);

        releaseSelectedCamera();

        finish();
    }

    private void logMenuChoice(MenuItem item) {
        CharSequence menuTitle = item.getTitle();
        Log.d(LOG_TAG, "Menu item selected:" + menuTitle);
    }

    void showNoCameraDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Camera");
        builder.setMessage(
                "Device does not have required camera support. " +
                        "Some features will not be available.");
        builder.setPositiveButton("Continue", null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    void disableCameraMenuItems(Menu menu) {
        menu.findItem(R.id.menuOpenBackCamera).setEnabled(false);
        menu.findItem(R.id.menuOpenFrontCamera).setEnabled(false);
        menu.findItem(R.id.menuCloseCamera).setEnabled(false);
    }

    void disableFrontCameraMenuItems(Menu menu) {
        menu.findItem(R.id.menuOpenFrontCamera).setEnabled(false);
    }

    int getFacingCameraId(int facing) {
        int cameraId = CAMERA_ID_NOT_SET;

        int nCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        for(int cameraInfoId=0; cameraInfoId < nCameras; cameraInfoId++) {
            Camera.getCameraInfo(cameraInfoId, cameraInfo);
            if(cameraInfo.facing == facing) {
                cameraId = cameraInfoId;
                break;
            }

        }
        return cameraId;
    }

    int getFrontFacingCameraId() {
        if(_frontFacingCameraId == CAMERA_ID_NOT_SET)
            _frontFacingCameraId = getFacingCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);

        return _frontFacingCameraId;
    }

    int getBackFacingCameraId() {
        if(_backFacingCameraId == CAMERA_ID_NOT_SET)
            _backFacingCameraId = getFacingCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);

        return _backFacingCameraId;
    }

    void openSelectedCamera() {
        String message = null;

        releaseSelectedCamera();
        if(_selectedCameraId != CAMERA_ID_NOT_SET) {
            try {
                _selectedCamera = Camera.open(_selectedCameraId);
                //message = String.format("Opened Camera ID: %d", _selectedCameraId);

                CameraPreview cameraPreview =
                        (CameraPreview) findViewById(R.id.cameraPreview);
                cameraPreview.connectCamera(_selectedCamera, _selectedCameraId);

                _cameraParameters = _selectedCamera.getParameters();

                _supportedPictureSizes = _cameraParameters.getSupportedPictureSizes();

                if(_selectedPictureSize == null)
                    _selectedPictureSize = _cameraParameters.getPictureSize();

                _isZoomSupported = _cameraParameters.isZoomSupported();
                if (_isZoomSupported) {
                    _currentZoom = _cameraParameters.getZoom();
                    _maxZoom = _cameraParameters.getMaxZoom();
                }

                _isSmoothZoomSupported = _cameraParameters.isSmoothZoomSupported();
                if (_isSmoothZoomSupported)
                    _selectedCamera.setZoomChangeListener((Camera.OnZoomChangeListener) this);

            } catch (Exception ex) {
                message = "Unable to open camera: " + ex.getMessage();
                Log.e(LOG_TAG, message);

            }

            manageCameraControls(true);
        }

        if(message != null)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    void releaseSelectedCamera() {
        if(_selectedCamera != null) {
            CameraPreview cameraPreview =
                    (CameraPreview) findViewById(R.id.cameraPreview);
            cameraPreview.releaseCamera();

            manageCameraControls(false);

            _selectedCamera.release();
            _selectedCamera = null;
        }
    }

    private void manageCameraControls(boolean enable) {

        Button takePictureButton = (Button) findViewById(R.id.takePictureButton);
        takePictureButton.setEnabled(enable);
        Button selectPictureSizeButton = (Button) findViewById(R.id.selectPictureSizeButton);
        selectPictureSizeButton.setEnabled(enable);

        displaySelectedPictureSize(enable ? _selectedPictureSize : null);
        manageCameraZoomControls(enable);
    }

    void manageCameraZoomControls(boolean enable) {
        // Only enable zoom controls if the selected camera supports zooming
        boolean zoomEnable = enable && _isZoomSupported;
        ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoomControls);
        zoomControls.setIsZoomInEnabled(zoomEnable);
        zoomControls.setIsZoomOutEnabled(zoomEnable);

        //Only enable zoom min/max controls if selected camera supports smooth zoom
        boolean smoothZoomEnable = enable && _isSmoothZoomSupported;
        Button zoomMinButton = (Button) findViewById(R.id.zoomMinButton);
        Button zoomMaxButton = (Button) findViewById(R.id.zoomMaxButton);
        zoomMinButton.setEnabled(smoothZoomEnable);
        zoomMaxButton.setEnabled(smoothZoomEnable);
    }

    private void displaySelectedPictureSize(Camera.Size size) {
        String display = size == null ? "" :
                String.format(CAMERA_SIZE_DISPLAY_FORMAT, size.width,  size.height);

        TextView textView = (TextView) findViewById(R.id.selectedPictureSizeTextView);
        textView.setText(display);
    }

    private void setupCameraControlsEventHandlers() {
        // **** Take Picture Button ****
        Button takePictureButton = (Button) findViewById(R.id.takePictureButton);
        takePictureButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View view) {
                // Take Picture Button Click Handler
                takePicture();
            }
        });

        // **** Select Picture Size Button ****
        Button selectPictureSizeButton = (Button) findViewById(R.id.selectPictureSizeButton);
        selectPictureSizeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                selectPictureSizeButtonClicked();
            }
        });

        ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoomControls);
        // **** Zoom In Button ****
        zoomControls.setOnZoomInClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        // Zoom In Button Click Handler
                        zoomIn();
                    }
                }
        );
        // **** Zoom Out Button ****
        zoomControls.setOnZoomOutClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        // Zoom Out Button Click Handler
                        zoomOut();
                    }
                }
        );

        // **** Smooth Zoom to Minimum Button ****
        Button zoomMinButton = (Button) findViewById(R.id.zoomMinButton);
        zoomMinButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Smooth Zoom to Minimum Click Handler
                zoomTo(0);
            }
        });
        // **** Smooth Zoom to Maximum Button ****
        Button zoomMaxButton = (Button) findViewById(R.id.zoomMaxButton);
        zoomMaxButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Smooth Zoom to Maximum Click Handler
                zoomTo(_maxZoom);
            }
        });
    }

    void zoomIn() {
        if(_currentZoom < _maxZoom) {
            _currentZoom++;
            _cameraParameters.setZoom(_currentZoom);
            _selectedCamera.setParameters(_cameraParameters);
        }
    }

    void zoomOut() {
        if(_currentZoom > 0) {
            _currentZoom--;
            _cameraParameters.setZoom(_currentZoom);
            _selectedCamera.setParameters(_cameraParameters);
        }
    }

    void zoomTo(int value) {
        if (_currentZoom != value) {
            manageCameraZoomControls(false);
            _selectedCamera.startSmoothZoom(value);
        }

    }

    public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
        if(stopped)
            manageCameraZoomControls(true);

        _currentZoom = zoomValue;
    }

    void takePicture() {
        if(_cameraParameters != null) {
            _cameraParameters.setPictureSize(_selectedPictureSize.width, _selectedPictureSize.height);

            int rotation = CameraHelper.getDisplayOrientationForCamera(this, _selectedCameraId);
            _cameraParameters.setRotation(rotation);

            configureCameraGpsParameters(_cameraParameters);

            _selectedCamera.setParameters(_cameraParameters);
        }

        _selectedCamera.takePicture(null, null, new Camera.PictureCallback() {
            public void onPictureTaken(byte[] bytes, Camera camera) {
                onPictureJpeg(bytes, camera);
            }
        });
    }

    void onPictureJpeg(byte[] bytes, Camera camera) {
        String userMessage = null;
        int i = bytes.length;
        Log.d(LOG_TAG, String.format("bytes = %d", i));

        File f = CameraHelper.generateTimeStampPhotoFile();

        try {
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(f));
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
            userMessage = "Picture saved as " + f.getName();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error accessing photo output file:" + e.getMessage());
            userMessage = "Error saving photo";
        }

        if (userMessage != null)
            Toast.makeText(this, userMessage, Toast.LENGTH_LONG).show();

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                Uri.parse("file://" + Environment.getExternalStorageDirectory())));

        _selectedCamera.startPreview();

    }

    private void configureCameraGpsParameters(Camera.Parameters cameraParameters) {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            double altitude = location.hasAltitude() ? location.getAltitude() : 0;
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            long gpsTime = location.getTime();

            // Set GPS values into parameters
            cameraParameters.setGpsAltitude(altitude);
            cameraParameters.setGpsLatitude(latitude);
            cameraParameters.setGpsLongitude(longitude);
            cameraParameters.setGpsTimestamp(gpsTime);

        }
        else {
            // Clear GPS parameters
            cameraParameters.removeGpsData();

        }

    }

    void selectPictureSizeButtonClicked() {
        // Create as array of strings of the form 320x240
        String[] pictureSizesAsString = new String[_supportedPictureSizes.size()];
        int index = 0;
        for(Camera.Size pictureSize:_supportedPictureSizes)
            pictureSizesAsString[index++] =
                    String.format(CAMERA_SIZE_DISPLAY_FORMAT, pictureSize.width, pictureSize.height);

        // Show list in selection activity
        Intent intent = new Intent(this, SelectionActivity.class);
        intent.putExtra(ACTIVITY_TITLE_EXTRA, "Select picture size");
        intent.putExtra(SELECTIONS_EXTRA, pictureSizesAsString);
        startActivityForResult(intent, PICTURE_SIZE_SELECTION_REQUEST_CODE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int sizeIndex = -1;
        if (resultCode == RESULT_OK) {
            switch(requestCode) {
                case PICTURE_SIZE_SELECTION_REQUEST_CODE:
                    sizeIndex = data.getIntExtra(SELECTED_INDEX_EXTRA, NOT_SET);
                    if(sizeIndex != NOT_SET)
                        _selectedPictureSize = _supportedPictureSizes.get(sizeIndex);
                    break;
                default:
                    break;
            }
        }

    }

}
