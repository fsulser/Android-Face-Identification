package ao.acn.ch.facetracking.AzureHelper;

import android.os.*;

import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.PersonGroup;

import ao.acn.ch.facetracking.MainActivity;

/**
 * Created by fabiosulser on 31.12.17.
 */

public class GetPersonGroups extends AsyncTask<String, String, PersonGroup[]>{

    public interface AsyncResponse {
        void processFinish(PersonGroup[] output);
    }
    private AsyncResponse delegate = null;

    public GetPersonGroups(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected PersonGroup[] doInBackground(String... params) {
        // Get an instance of face service client.
        FaceServiceClient faceServiceClient = MainActivity.getFaceServiceClient();
        try{
            return faceServiceClient.getPersonGroups();
        } catch (Exception e) {
            MainActivity.showToast(e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(PersonGroup[] result) {
        delegate.processFinish(result);
    }


}