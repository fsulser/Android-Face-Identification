package ao.acn.ch.facetracking;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.PersonGroup;

import java.util.ArrayList;
import java.util.List;

import ao.acn.ch.facetracking.AzureHelper.GetPersonGroups;

/**
 * Created by fabiosulser on 02.01.18.
 */

class HelperDialog extends Dialog {
    private Spinner selectGroup;
    private Button save, cancel;
    private EditText apiKey, endPoint;
    private final Activity activity;
    private final SharedPreferences sharedPref;

    public HelperDialog(@NonNull Activity activity) {
        super(activity);
        this.activity = activity;
        sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings_dialog);

        selectGroup = findViewById(R.id.groupSpinner);
        save = findViewById(R.id.save);
        cancel = findViewById(R.id.cancel);
        apiKey = findViewById(R.id.apiKey);
        endPoint = findViewById(R.id.endpoint);

        setAPIKey();
        setEndpoint();
        getAllGroups();

        saveButtonListener();
        cancelButtonListener();
    }

    private void setAPIKey(){
        //e0019a85c9c04be1ab69ae9bdd09a002
        apiKey.setText(sharedPref.getString(activity.getString(R.string.azure_subscription_key), ""));
    }

    private void setEndpoint(){
        //"https://westcentralus.api.cognitive.microsoft.com/face/v1.0"
        endPoint.setText(sharedPref.getString(activity.getString(R.string.azure_endpoint), "https://westcentralus.api.cognitive.microsoft.com/face/v1.0"));

    }

    private void getAllGroups(){
        new GetPersonGroups(new GetPersonGroups.AsyncResponse() {
            @Override
            public void processFinish(PersonGroup[] result) {
                if (result != null) {
                    List<String> personGroupNames = new ArrayList<>();
                    for (PersonGroup personGroup : result){
                        personGroupNames.add(personGroup.name);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, personGroupNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    selectGroup.setAdapter(adapter);

                    String activeGroup = sharedPref.getString(activity.getString(R.string.active_group), "---");

                    selectGroup.setSelection(adapter.getPosition(activeGroup));
                }
            }
        }).execute();
    }


    private void saveButtonListener(){
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPref.edit();
                if(selectGroup.getSelectedItem() != null ){
                    if(!selectGroup.getSelectedItem().toString().equals("")) {
                        editor.putString(activity.getString(R.string.active_group), selectGroup.getSelectedItem().toString());
                    }
                }
                if(apiKey.getText() != null && !apiKey.getText().equals("")){
                    editor.putString(activity.getString(R.string.azure_subscription_key), String.valueOf(apiKey.getText()));
                }
                if(endPoint.getText() != null && !endPoint.getText().equals("")){
                    editor.putString(activity.getString(R.string.azure_endpoint), String.valueOf(endPoint.getText()));
                }
                MainActivity.createFaceServiceClient();

                editor.apply();
                dismiss();
            }
        });

    }

    private void cancelButtonListener(){
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }
}