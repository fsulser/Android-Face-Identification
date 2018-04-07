package ao.acn.ch.facetracking.AzureHelper;

import android.os.AsyncTask;

import com.microsoft.projectoxford.face.FaceServiceClient;

import java.util.Objects;

import ao.acn.ch.facetracking.MainActivity;

/**
 * Created by fabiosulser on 31.12.17.
 */

public class TrainPersonGroup extends AsyncTask<String, String, String>{
    public interface AsyncResponse {
        void processFinish(String output);
    }
    private TrainPersonGroup.AsyncResponse delegate = null;

    public TrainPersonGroup(TrainPersonGroup.AsyncResponse delegate){
        this.delegate = delegate;
    }


    @Override
    protected String doInBackground(String... params) {
        if(Objects.equals(params[0], "") || params[0] == null){
            return null;
        }
        // Get an instance of face service client.
        FaceServiceClient faceServiceClient = MainActivity.getFaceServiceClient();
        try{
            faceServiceClient.trainPersonGroup(params[0]);
            MainActivity.showToast("Persongroup " + params[0] + " is training");
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