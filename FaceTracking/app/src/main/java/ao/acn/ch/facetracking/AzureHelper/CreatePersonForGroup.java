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

import com.microsoft.projectoxford.face.FaceServiceClient;

import java.util.Objects;

import ao.acn.ch.facetracking.MainActivity;

/**
 * Created by fabiosulser on 31.12.17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class CreatePersonForGroup extends AsyncTask<String, String, String> {

    public interface AsyncResponse {
        void processFinish(String output);
    }
    private CreatePersonForGroup.AsyncResponse delegate = null;

    public CreatePersonForGroup(CreatePersonForGroup.AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... params) {
        if(Objects.equals(params[0], "") || params[0] == null){
            return null;
        }
        if(Objects.equals(params[1], "") || params[1] == null){
            return null;
        }
        String personGroupName = params[0];
        String personName = params[1];
        // Get an instance of face service client.
        FaceServiceClient faceServiceClient = MainActivity.getFaceServiceClient();
        try{
            faceServiceClient.createPerson(personGroupName, personName, null);
            MainActivity.showToast("Person " + personName + " created for group "+ personGroupName);
            return params[0];
        } catch (Exception e) {
            MainActivity.showToast(e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }
}