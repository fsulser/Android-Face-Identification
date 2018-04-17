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

package ao.acn.ch.facetracking.AzureHelper;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

import ao.acn.ch.facetracking.MainActivity;

/**
 * Created by fabiosulser on 31.12.17.
 */

public class DetectPersonFace extends AsyncTask<String, String, UUID> {
    private final InputStream imageInputStream;

    public interface AsyncResponse {
        void processFinish(UUID output);
    }
    private DetectPersonFace.AsyncResponse delegate = null;

    public DetectPersonFace(Bitmap bitmap, DetectPersonFace.AsyncResponse delegate){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        imageInputStream = new ByteArrayInputStream(stream.toByteArray());
        this.delegate = delegate;
    }

    @Override
    protected UUID doInBackground(String... strings) {
        // Get an instance of face service client.
        FaceServiceClient faceServiceClient = MainActivity.getFaceServiceClient();
        try{
            Face[] faces = faceServiceClient.detect(imageInputStream,true,false, null);
            if(faces.length == 0){
                return null;
            }
            return faces[0].faceId;
        } catch (Exception e) {
            MainActivity.showToast(e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(UUID result) {
        delegate.processFinish(result);
    }
}
