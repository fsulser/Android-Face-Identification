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
