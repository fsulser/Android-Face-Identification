package ao.acn.ch.facetracking;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.microsoft.projectoxford.face.contract.AddPersistedFaceResult;
import com.microsoft.projectoxford.face.contract.Person;
import com.microsoft.projectoxford.face.contract.PersonGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ao.acn.ch.facetracking.AzureHelper.CreatePersonForGroup;
import ao.acn.ch.facetracking.AzureHelper.CreatePersonGroup;
import ao.acn.ch.facetracking.AzureHelper.GetPersonGroups;
import ao.acn.ch.facetracking.AzureHelper.GetPersonsForGroup;
import ao.acn.ch.facetracking.AzureHelper.TrainPersonGroup;
import ao.acn.ch.facetracking.AzureHelper.UploadPersonFace;

/**
 * Created by fabiosulser on 29.12.17.
 */

public class Train_Fragment extends Fragment{
    private static final int CAMERA_REQUEST = 1888;
    private Activity activity;
    private int runningTasks = 0;
    private HashMap<String, String> personIDs = new HashMap<>();

    private View rootView;
    private Spinner personGroups, persons;
    private Button capture, upload, train;
    private ImageView preview;
    private Bitmap capturedImage;
    private ProgressDialog progressDialog;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.train_fragment, container, false);

        personGroups = rootView.findViewById(R.id.groupSpinner);
        persons = rootView.findViewById(R.id.personSpinner);
        capture = rootView.findViewById(R.id.capture);
        preview = rootView.findViewById(R.id.preview);
        upload = rootView.findViewById(R.id.uploadImage);
        train = rootView.findViewById(R.id.train);

        //add listener on group change
        personGroupItemSelect();

        //listen on add person group and add person button
        createGroupButtonListener();
        createPersonButtonListener();

        //listen to capture image button
        captureImageButtonListener();
        //listen to upload image button
        uploadButtonListener();
        //listen to train button
        trainButtonListener();


        return rootView;
    }

    private void addProgressbar(){
        progressDialog = new ProgressDialog(activity);
        progressDialog.show();
    }

    private void removeProgressbar(){
        if (progressDialog.isShowing() && runningTasks <= 0) {
            progressDialog.dismiss();
        }
    }

    private void trainButtonListener(){
        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            addProgressbar();
            runningTasks += 1;
            String groupName = personGroups.getSelectedItem().toString();
            new TrainPersonGroup(new TrainPersonGroup.AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    runningTasks -=1;
                    removeProgressbar();
                }
            }).execute(groupName);
            }
        });
    }

    private void uploadButtonListener(){
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProgressbar();
                runningTasks += 1;
                String groupName = personGroups.getSelectedItem().toString();
                String personID = personIDs.get(persons.getSelectedItem().toString());

                new UploadPersonFace(capturedImage, new UploadPersonFace.AsyncResponse() {
                    @Override
                    public void processFinish(AddPersistedFaceResult output) {
                        runningTasks -= 1;
                        removeProgressbar();
                    }
                }).execute(groupName, personID);
            }
        });
    }

    private void captureImageButtonListener(){
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            capturedImage = (Bitmap) data.getExtras().get("data");
            preview.setImageBitmap(capturedImage);
        }
    }

    private void personGroupItemSelect(){
        personGroups.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                final String personGroupValue = (String) personGroups.getItemAtPosition(position);
                getPersons(personGroupValue);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                final String personGroupValue = "";
                getPersons(personGroupValue);
            }
        });

    }

    private void getPersons(String personGroupValue){
        addProgressbar();
        runningTasks += 1;
        personIDs = new HashMap<>();
        new GetPersonsForGroup(new GetPersonsForGroup.AsyncResponse() {
            @Override
            public void processFinish(Person[] result) {
                if (result != null) {
                    List<String> personNames = new ArrayList<>();
                    for (Person person : result){
                        personIDs.put(person.name, person.personId.toString());
                        personNames.add(person.name);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, personNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    persons.setAdapter(adapter);
                }
                runningTasks -=1;
                removeProgressbar();
            }
        }).execute(personGroupValue);
    }

    private void getPersonGroups(){
        addProgressbar();
        runningTasks += 1;
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
                    personGroups.setAdapter(adapter);
                }
                runningTasks -=1;
                removeProgressbar();
            }
        }).execute();
    }

    private void createGroupButtonListener(){
        Button createNewGroup = rootView.findViewById(R.id.newGroup);
        createNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Title");

                // Set up the input
                final EditText input = new EditText(activity);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String groupName = input.getText().toString();
                        createPersonGroup(groupName);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    private void createPersonButtonListener(){
        Button createNewPerson = rootView.findViewById(R.id.newPerson);
        createNewPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Title");

                // Set up the input
                final EditText input = new EditText(activity);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String personName = input.getText().toString();
                        String groupName = personGroups.getSelectedItem().toString();
                        createPerson(groupName, personName);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }


    private void createPersonGroup(String name){
        addProgressbar();
        runningTasks += 1;
        new CreatePersonGroup(new CreatePersonGroup.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                if(output != null){
                    getPersonGroups();
                }
                runningTasks -=1;
                removeProgressbar();
            }
        }).execute(name);
    }

    private void createPerson(final String personGroupName, String personName){
        addProgressbar();
        runningTasks += 1;
        new CreatePersonForGroup(new CreatePersonForGroup.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                if(output != null){
                    getPersons(personGroupName);
                }
                runningTasks -=1;
                removeProgressbar();
            }
        }).execute(personGroupName, personName);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            //get all existing persongroups
            getPersonGroups();
        }
    }
}
