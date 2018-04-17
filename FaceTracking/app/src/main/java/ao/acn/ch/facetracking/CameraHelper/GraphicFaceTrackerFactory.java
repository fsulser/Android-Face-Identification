/*
 * Copyright 2018 Sulser Fabio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ao.acn.ch.facetracking.CameraHelper;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

import ao.acn.ch.facetracking.camera.GraphicOverlay;


/**
 * Created by fabiosulser on 29.12.17.
 */

class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
    private final GraphicOverlay mGraphicOverlay;

    public GraphicFaceTrackerFactory(GraphicOverlay mGraphicOverlay){
        this.mGraphicOverlay = mGraphicOverlay;
    }

    @Override
    public Tracker<Face> create(Face face) {
        return new GraphicFaceTracker(mGraphicOverlay);
    }
}
