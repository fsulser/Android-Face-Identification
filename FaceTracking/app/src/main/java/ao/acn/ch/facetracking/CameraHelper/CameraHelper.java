package ao.acn.ch.facetracking.CameraHelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import ao.acn.ch.facetracking.camera.CameraSourcePreview;
import ao.acn.ch.facetracking.camera.GraphicOverlay;

/**
 * Created by fabiosulser on 29.12.17.
 */

@SuppressLint("Registered")
public class CameraHelper extends Application {
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private final GraphicOverlay mGraphicOverlay;
    private final Activity parentActivity;


    public CameraHelper(Activity mainActivity, GraphicOverlay mGraphicOverlay) {
        parentActivity = mainActivity;
        this.mGraphicOverlay = mGraphicOverlay;
    }

    public boolean checkCameraPermission() {
        int rc = ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.CAMERA);
        return rc == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a message of why the permission is needed then
     * sending the request.
     */
    public void requestCameraPermission() {
        Log.w("", "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(parentActivity,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(parentActivity, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(parentActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, "Camera access is required in this application",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     * @param cameraFacing camera facing value
     */
    public CameraSource createCameraSource(int cameraFacing) {

        Context context = parentActivity.getApplicationContext();

        // You can use your own settings for your detector
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .build();

        // This is how you merge myFaceDetector and google.vision detector
        MyFaceDetector myFaceDetector = new MyFaceDetector(detector);

        // You can use your own settings for CameraSource
        CameraSource mCameraSource = new CameraSource.Builder(context, myFaceDetector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(cameraFacing)
                .setRequestedFps(20.0f)
//                .setAutoFocusEnabled(true)
                .build();

        // You can use your own processor
        myFaceDetector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory(mGraphicOverlay))
                        .build());

        if (!myFaceDetector.isOperational()) {
            Log.w("", "Face detector dependencies are not yet available.");
        }

        return mCameraSource;
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     * @param mCameraSource camersource
     * @param mPreview preview
     */
    public void startCameraSource(CameraSource mCameraSource, CameraSourcePreview mPreview) {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(parentActivity);
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(parentActivity, code, RC_HANDLE_GMS);
            dlg.show();
        }

        updateCameraSource(mCameraSource, mPreview);
    }

    private void updateCameraSource(CameraSource mCameraSource, CameraSourcePreview mPreview){
        try {
            mPreview.stop();
            mPreview.start(mCameraSource, mGraphicOverlay);
        } catch (IOException e) {
            Log.e("", "Unable to start camera source.", e);
            mCameraSource.release();
        }
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d("", "Got unexpected permission result: " + requestCode);
            parentActivity.getParent().onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("", "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource(CameraSource.CAMERA_FACING_FRONT);
            return;
        }

        Log.e("", "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                parentActivity.finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
        builder.setTitle("Face Tracker sample")
                .setMessage("This application cannot run because it does not have the camera permission. The application will now exit.")
                .setPositiveButton("OK", listener)
                .show();

    }

}
