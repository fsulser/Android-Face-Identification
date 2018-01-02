package ao.acn.ch.facetracking.CameraHelper;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

import ao.acn.ch.facetracking.camera.GraphicOverlay;


/**
 * Created by fabiosulser on 29.12.17.
 */

class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
    private final GraphicOverlay mGraphicOverlay;
    private final CameraSource cameraSource;

    public GraphicFaceTrackerFactory(GraphicOverlay mGraphicOverlay, CameraSource cameraSource){
        this.cameraSource = cameraSource;
        this.mGraphicOverlay = mGraphicOverlay;
    }

    @Override
    public Tracker<Face> create(Face face) {
        return new GraphicFaceTracker(mGraphicOverlay, cameraSource);
    }
}
