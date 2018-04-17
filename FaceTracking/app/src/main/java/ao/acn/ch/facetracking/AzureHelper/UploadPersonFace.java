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

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.AddPersistedFaceResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import ao.acn.ch.facetracking.MainActivity;

/**
 * Created by fabiosulser on 31.12.17.
 */

public class UploadPersonFace extends AsyncTask<String, String, AddPersistedFaceResult> {
    private final InputStream imageInputStream;

    public interface AsyncResponse {
        void processFinish(@SuppressWarnings("unused") AddPersistedFaceResult output);
    }
    private UploadPersonFace.AsyncResponse delegate = null;

    public UploadPersonFace(Bitmap imageBitmap, UploadPersonFace.AsyncResponse delegate){
        this.delegate = delegate;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        imageInputStream = new ByteArrayInputStream(stream.toByteArray());
    }


    @Override
    protected AddPersistedFaceResult doInBackground(String... strings) {
        try {
            if(Objects.equals(strings[0], "") || strings[0] == null){
                return null;
            }
            if(Objects.equals(strings[1], "") || strings[1] == null){
                return  null;
            }

            String personGroupId = strings[0];
            UUID personId = UUID.fromString(strings[1]);

            // Get an instance of face service client.
            FaceServiceClient faceServiceClient = MainActivity.getFaceServiceClient();

            AddPersistedFaceResult res = faceServiceClient.addPersonFace(personGroupId, personId, imageInputStream, null, null);
            MainActivity.showToast("Image added");
            return res;
        } catch (Exception e) {

            MainActivity.showToast(e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(AddPersistedFaceResult result) {
        delegate.processFinish(result);
    }

}
