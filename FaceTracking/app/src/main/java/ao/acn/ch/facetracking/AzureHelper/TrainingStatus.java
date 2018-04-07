package ao.acn.ch.facetracking.AzureHelper;

import android.os.AsyncTask;

import com.microsoft.projectoxford.face.FaceServiceClient;

import java.util.Objects;

import ao.acn.ch.facetracking.MainActivity;

/**
 * Created by fabio.sulser on 27.03.2018.
 */


public class TrainingStatus extends AsyncTask<String, String, String> {
    public interface AsyncResponse {
        void processFinish(String output);
    }
    private TrainPersonGroup.AsyncResponse delegate = null;

    public TrainingStatus(TrainPersonGroup.AsyncResponse delegate){
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
            faceServiceClient.getPersonGroupTrainingStatus(params[0]);
            return params[0];
        } catch (Exception e) {
            MainActivity.showToast(e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        //TODO add training status
        MainActivity.showToast("Trainingstatus is : ");
        delegate.processFinish(result);
    }
}
