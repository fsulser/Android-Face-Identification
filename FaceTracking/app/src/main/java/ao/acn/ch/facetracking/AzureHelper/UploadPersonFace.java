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
        void processFinish(AddPersistedFaceResult output);
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
