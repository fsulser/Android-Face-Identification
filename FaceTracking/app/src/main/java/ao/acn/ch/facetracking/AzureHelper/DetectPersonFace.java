package ao.acn.ch.facetracking.AzureHelper;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

import ao.acn.ch.facetracking.MainActivity;

/**
 * Created by fabiosulser on 31.12.17.
 */

public class DetectPersonFace extends AsyncTask<String, String, UUID> {
    private final InputStream imageInputStream;

    public interface AsyncResponse {
        void processFinish(UUID output);
    }
    private DetectPersonFace.AsyncResponse delegate = null;

    public DetectPersonFace(Bitmap bitmap, DetectPersonFace.AsyncResponse delegate){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        imageInputStream = new ByteArrayInputStream(stream.toByteArray());

        this.delegate = delegate;
    }

    @Override
    protected UUID doInBackground(String... strings) {

        // Get an instance of face service client.
        FaceServiceClient faceServiceClient = MainActivity.getFaceServiceClient();
        try{
            Face[] faces = faceServiceClient.detect(imageInputStream,true,false, null);
            if(faces.length == 0){
                return null;
            }
            return faces[0].faceId;
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
