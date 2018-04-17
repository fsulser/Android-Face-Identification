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

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.IdentifyResult;

import java.util.UUID;

import ao.acn.ch.facetracking.MainActivity;

/**
 * Created by fabiosulser on 31.12.17.
 */

public class IdentifyPerson extends AsyncTask<String, String, UUID>{
    private final UUID[] faceIDs;
    private final String personGroup;

    public interface AsyncResponse {
        void processFinish(UUID output);
    }
    private IdentifyPerson.AsyncResponse delegate = null;

    public IdentifyPerson(UUID[] faceIDs, String personGroup, IdentifyPerson.AsyncResponse delegate){
        this.personGroup = personGroup;
        this.faceIDs = faceIDs;
        this.delegate = delegate;
    }

    @Override
    protected UUID doInBackground(String... strings) {
        // Get an instance of face service client.
        FaceServiceClient faceServiceClient = MainActivity.getFaceServiceClient();
        try{
            IdentifyResult[] faces = faceServiceClient.identity(personGroup, faceIDs, 1);
            if(faces.length > 0){
                if(faces[0].candidates.size() > 0) {
                    return faces[0].candidates.get(0).personId;
                }
            }
            return null;
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
