/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ao.acn.ch.facetracking.CameraHelper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.vision.face.Face;

import ao.acn.ch.facetracking.camera.GraphicOverlay;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float ID_TEXT_SIZE = 50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;


    private final Paint mIdPaint;
    private final Paint mBoxPaint;

    private volatile Face mFace;
    private String mFaceName = "Unknown";
    private int tries = 0;
    private boolean isRecognized = false, isRecognizing = false;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mIdPaint = new Paint();
        mIdPaint.setTextSize(ID_TEXT_SIZE);
        mIdPaint.setTextAlign(Paint.Align.CENTER);

        mBoxPaint = new Paint();
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setName(String FaceId) {
        mFaceName = FaceId;
    }

    void increaseTries(){tries++; }
    int getTries (){ return tries; }

    void setRecognized(boolean recog){ this.isRecognized = recog; }
    boolean isRecognized(){ return this.isRecognized;}

    void setRecognizing(boolean recog){ this.isRecognizing = recog; }
    boolean isRecognizing(){ return this.isRecognizing;}


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        int selectedColor = Color.RED;

        if(!mFaceName.startsWith("Unknown")){
            selectedColor = Color.GREEN;
        }

        mIdPaint.setColor(selectedColor);
        mBoxPaint.setColor(selectedColor);


        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);

        canvas.drawText(mFaceName, x, top-4, mIdPaint);
    }
}
