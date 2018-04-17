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

package ao.acn.ch.facetracking;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.vision.CameraSource;

import ao.acn.ch.facetracking.CameraHelper.CameraHelper;
import ao.acn.ch.facetracking.camera.CameraSourcePreview;
import ao.acn.ch.facetracking.camera.GraphicOverlay;

/**
 * Created by fabiosulser on 29.12.17.
 */

public class Identify_Fragment extends Fragment {
    private CameraSourcePreview mPreview;
    private CameraSource cameraSource = null;
    private CameraHelper cameraHelper;

    private Activity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.identify_fragment, container, false);

        mPreview = rootView.findViewById(R.id.preview);
        GraphicOverlay mGraphicOverlay = rootView.findViewById(R.id.faceOverlay);
        cameraHelper = new CameraHelper(activity, mGraphicOverlay);

        if(cameraHelper.checkCameraPermission()){
            cameraSource = cameraHelper.createCameraSource(CameraSource.CAMERA_FACING_FRONT);
        } else {
            cameraHelper.requestCameraPermission();
            cameraSource = cameraHelper.createCameraSource(CameraSource.CAMERA_FACING_FRONT);
            cameraHelper.startCameraSource(cameraSource, mPreview);
        }

        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch front to back camera
                if(cameraSource.getCameraFacing() == CameraSource.CAMERA_FACING_FRONT){
                    cameraSource = cameraHelper.createCameraSource(CameraSource.CAMERA_FACING_BACK);
                    cameraHelper.startCameraSource(cameraSource, mPreview);
                }else{
                    cameraSource = cameraHelper.createCameraSource(CameraSource.CAMERA_FACING_FRONT);
                    cameraHelper.startCameraSource(cameraSource, mPreview);
                }
            }
        });

        FloatingActionButton settings = rootView.findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelperDialog mydialog = new HelperDialog(activity);
                mydialog.show();
            }
        });


        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            if(cameraHelper!= null){
                cameraHelper.startCameraSource(cameraSource, mPreview);
            }
        }else{
            if(mPreview!= null){
                mPreview.stop();
            }
        }
    }

    /**
     * Restarts the camera.
     */
    @Override
    public void onResume() {
        super.onResume();

        cameraHelper.startCameraSource(cameraSource, mPreview);
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }


    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        cameraHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
