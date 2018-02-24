package ao.acn.ch.facetracking.CameraHelper;

import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

/**
 * Created by fabiosulser on 24.02.18.
 */

class MyFaceDetector extends Detector<Face> {
    private final Detector<Face> mDelegate;

    MyFaceDetector(Detector<Face> delegate) {
        mDelegate = delegate;
    }

    public SparseArray<Face> detect(final Frame frame) {
        GraphicHolder.frame = frame;

        return mDelegate.detect(frame);
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}