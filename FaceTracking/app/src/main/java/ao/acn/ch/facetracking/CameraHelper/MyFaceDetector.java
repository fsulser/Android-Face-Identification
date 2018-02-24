package ao.acn.ch.facetracking.CameraHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;

/**
 * Created by fabiosulser on 24.02.18.
 */

class MyFaceDetector extends Detector<Face> {
    private Detector<Face> mDelegate;

    MyFaceDetector(Detector<Face> delegate) {
        mDelegate = delegate;
    }

    public SparseArray<Face> detect(final Frame frame) {
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
             }
            }.start();
        return mDelegate.detect(frame);
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}