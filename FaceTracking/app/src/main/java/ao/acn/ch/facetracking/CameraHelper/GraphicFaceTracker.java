package ao.acn.ch.facetracking.CameraHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.microsoft.projectoxford.face.contract.Person;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import ao.acn.ch.facetracking.AzureHelper.GetPersonsForGroup;
import ao.acn.ch.facetracking.AzureHelper.DetectPersonFace;
import ao.acn.ch.facetracking.AzureHelper.IdentifyPerson;
import ao.acn.ch.facetracking.MainActivity;
import ao.acn.ch.facetracking.R;
import ao.acn.ch.facetracking.Train_Fragment;
import ao.acn.ch.facetracking.camera.GraphicOverlay;

/**
 * Created by fabiosulser on 29.12.17.
 */

class GraphicFaceTracker extends Tracker<Face> {
    private final GraphicOverlay mOverlay;
    private final FaceGraphic mFaceGraphic;
    private CameraSource mCameraSource = null;
    private final HashMap<UUID, String> personIDs = new HashMap<>();
    private String personGroup;

    GraphicFaceTracker(GraphicOverlay overlay, CameraSource cameraSource) {
        mOverlay = overlay;
        mFaceGraphic = new FaceGraphic(overlay);
        mCameraSource = cameraSource;
        setPersonGroup();

        getPersonIDsForGroup();

    }

    private void setPersonGroup(){
        personGroup = getPersonGroupFromPref();
    }

    private String getPersonGroupFromPref(){
        SharedPreferences sharedPref = MainActivity.activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(MainActivity.activity.getString(R.string.active_group), "");
    }

    private void getPersonIDsForGroup(){
        new GetPersonsForGroup(new GetPersonsForGroup.AsyncResponse() {
            @Override
            public void processFinish(Person[] result) {
                if (result != null) {
                    for (Person person : result){
                        personIDs.put(person.personId, person.name);
                    }
                }
            }
        }).execute(personGroup);
    }


    /**
     * Start tracking the detected face instance within the face overlay.
     */
    @Override
    public void onNewItem(final int faceId, final Face face) {
        final Frame frame = GraphicHolder.frame;
        Log.i("", "neues gesicht");
        if(!Objects.equals(personGroup, getPersonGroupFromPref())){
            setPersonGroup();
            getPersonIDsForGroup();
        }
        new Thread() {
            @Override
            public void run() {
                int rotationAngle = 0;
                int width = frame.getMetadata().getWidth();
                int height = frame.getMetadata().getHeight();

                switch (frame.getMetadata().getRotation()) {
                    case 0:
                        break;
                    case 1:
                        rotationAngle = 90;
                        break;
                    case 2:
                        rotationAngle = 180;
                        break;
                    case 3:
                        rotationAngle = 270;
                        break;
                    default:
                        rotationAngle = 0;
                }


                YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, width, height, null);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream);
                byte[] jpegArray = byteArrayOutputStream.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationAngle);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                GraphicHolder.faceImage = rotatedBitmap;
                Bitmap resizedBitmap = crop(rotatedBitmap, face);
//                      Train_Fragment.setPreview(resizedBitmap);
                detectFace(resizedBitmap);
            }
        }.start();
    }

    private void detectFace(Bitmap bitmap){
        new DetectPersonFace(bitmap, new DetectPersonFace.AsyncResponse() {
            @Override
            public void processFinish(UUID faceUUID) {
                if (faceUUID != null) {
                    UUID[] faceArray = new UUID[]{faceUUID};
                    new IdentifyPerson(faceArray, personGroup, new IdentifyPerson.AsyncResponse() {
                        @Override
                        public void processFinish(UUID personUUID) {
                            if (personUUID != null) {
                                mFaceGraphic.setName(personIDs.get(personUUID));
                            }
                        }
                    }).execute();
                }else{
                    //if no face is detetcted by azure, set recognized to false
                    mFaceGraphic.setRecognized(false);
                }
            }
        }).execute();
    }



    /**
     * Update the position/characteristics of the face within the overlay.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, final Face face) {
        mOverlay.add(mFaceGraphic);
        mFaceGraphic.updateFace(face);
        //if face was not detected on add (due tue bad image), retry to detect a face
        if(!mFaceGraphic.getRecognized()){
            mFaceGraphic.setRecognized(true);
            if (mCameraSource != null){
                mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
                    @Override
                    public void onPictureTaken(final byte[] bytes) {
                    new Thread() {
                        @Override
                        public void run() {
                            Bitmap resizedBitmap = crop(BitmapFactory.decodeByteArray(bytes, 0, bytes.length), face);
                            detectFace(resizedBitmap);
                        }
                    }.start();
                    }
                });
            }
        }
    }

    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        mOverlay.remove(mFaceGraphic);
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    private Bitmap crop(Bitmap bitmap, Face face) {
        try {
            if (face == null) {
                return bitmap;
            }

            float scaleX = bitmap.getWidth() / mCameraSource.getPreviewSize().getWidth();
            float scaleY = bitmap.getHeight() / mCameraSource.getPreviewSize().getHeight();
            float scaleWidthAndHeight = Math.max(scaleX, scaleY);
            float scaleLeftAndTop = Math.max(scaleX, scaleY);


            int offsetX = 0;
            int offsetY = 0;
            if (mCameraSource.getCameraFacing() == CameraSource.CAMERA_FACING_BACK) {
                if (bitmap.getWidth() >= bitmap.getHeight()) {
                    scaleWidthAndHeight += 2;
                    offsetX = -100;
                    offsetY = -100;
                } else {
                    offsetX = -300;
                    offsetY = -300;
                }
            }

            int left = (int) Math.max((face.getPosition().x * scaleLeftAndTop) + offsetX, 0);
            int top = (int) Math.max((face.getPosition().y * scaleLeftAndTop) + offsetY, 0);
            int width = (int) Math.max((face.getWidth() * scaleWidthAndHeight), 0);
            int height = (int) Math.max((face.getHeight() * scaleWidthAndHeight), 0);


            if (left + width >= bitmap.getWidth()) {
                width = Math.max(bitmap.getWidth() - left, 1);
            }
            if (height + top >= bitmap.getHeight()) {
                height = Math.max(bitmap.getHeight() - top, 1);
            }

            return Bitmap.createBitmap(bitmap, left, top, width, height);
        }catch (Exception e){
            return null;
        }
    }
}
