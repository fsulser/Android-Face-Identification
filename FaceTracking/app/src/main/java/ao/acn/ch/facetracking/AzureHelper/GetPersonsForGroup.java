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
import com.microsoft.projectoxford.face.contract.Person;

import java.util.Objects;

import ao.acn.ch.facetracking.MainActivity;

/**
 * Created by fabiosulser on 31.12.17.
 */

public class GetPersonsForGroup extends AsyncTask<String, String, Person[]>{

    public interface AsyncResponse {
        void processFinish(Person[] output);
    }
    private GetPersonsForGroup.AsyncResponse delegate = null;

    public GetPersonsForGroup(GetPersonsForGroup.AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected Person[] doInBackground(String... params) {
        if(params[0] == null || Objects.equals(params[0], "")){
            return null;
        }
        // Get an instance of face service client.
        FaceServiceClient faceServiceClient = MainActivity.getFaceServiceClient();
        try{
            return faceServiceClient.getPersons(params[0]);
        } catch (Exception e) {
            MainActivity.showToast(e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Person[] result) {
        delegate.processFinish(result);
    }
}
