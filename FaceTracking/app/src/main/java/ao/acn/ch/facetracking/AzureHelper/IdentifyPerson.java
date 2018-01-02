package ao.acn.ch.facetracking.AzureHelper;

import android.os.AsyncTask;

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
            return faces[0].candidates.get(0).personId;
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
