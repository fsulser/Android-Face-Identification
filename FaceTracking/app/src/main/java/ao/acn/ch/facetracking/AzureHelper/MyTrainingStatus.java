package ao.acn.ch.facetracking.AzureHelper;

import android.os.AsyncTask;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.PersonGroup;
import com.microsoft.projectoxford.face.contract.TrainingStatus;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.IOException;

import ao.acn.ch.facetracking.MainActivity;

/**
 * Created by fabio.sulser on 27.03.2018.
 */


public class MyTrainingStatus extends AsyncTask<String, String, TrainingStatus> {
    public interface AsyncResponse {
        void processFinish(TrainingStatus output);
    }
    private MyTrainingStatus.AsyncResponse delegate = null;

    public MyTrainingStatus(MyTrainingStatus.AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected TrainingStatus doInBackground(String... params) {
        // Get an instance of face service client.
        FaceServiceClient faceServiceClient = MainActivity.getFaceServiceClient();
        try{
            return faceServiceClient.getPersonGroupTrainingStatus(params[0]);
        } catch (Exception e) {
            MainActivity.showToast(e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(TrainingStatus result) {
        delegate.processFinish(result);

    }

}
