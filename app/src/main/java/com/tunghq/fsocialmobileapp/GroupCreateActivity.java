package com.tunghq.fsocialmobileapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class GroupCreateActivity extends AppCompatActivity {

    Toolbar toolbar;
    FirebaseAuth mAuth;

    FirebaseUser user;

    String myUid;

    ImageView groupImgIv;
    EditText groupTitleEt, groupDescEt;
    FloatingActionButton createGroupBtn;
    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constant
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //permission array
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //pick image uri
    Uri image_uri = null;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);

        init();
        clickListener();
    }

    private void clickListener() {
        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreatingGroup();
            }
        });
        groupImgIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });
    }

    private void startCreatingGroup() {
        pd = new ProgressDialog(this);
        pd.setMessage("Creating Group");

        //input title, desc
        String groupTitle = groupTitleEt.getText().toString();
        String groupDesc = groupDescEt.getText().toString();
        //validation
        if (TextUtils.isEmpty(groupTitle)) {
            Toast.makeText(this, "Please enter group title", Toast.LENGTH_SHORT).show();
            return;//dont procede further
        }
        pd.show();
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm a");
        String gTimeStamp = format.format(date);
        if (image_uri == null) {
            //creating group without icon image
            createGroup("" + gTimeStamp, "" + groupTitle, "" + groupDesc, "");
        } else {
            //creating group with icon image
            //upload image
            //image name and path
            String fileNameAndPath = "Group_Imgs/" + "image" + gTimeStamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded, get url
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());

                            Uri downloadUri = uriTask.getResult();
                            if(uriTask.isSuccessful()){
                                createGroup("" + gTimeStamp, "" + groupTitle, "" + groupDesc, ""+downloadUri);
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(GroupCreateActivity.this, "Fail..", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void createGroup(String gTimeStamp, String groupTitle, String groupDesc, String groupImg) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups Chat");
        String groupId = databaseReference.push().getKey();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("groupId", "" + groupId);
        hashMap.put("groupTitle", "" + groupTitle);
        hashMap.put("groupDesc", "" + groupDesc);
        hashMap.put("groupImg", "" + groupImg);
        hashMap.put("timeStamp", "" + gTimeStamp);
        hashMap.put("creatorId", "" + mAuth.getUid());
        databaseReference.child(groupId).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //set up member inf (add current user in group's participants list)
                HashMap<String,String> hashMap1 = new HashMap<>();
                hashMap1.put("uid", mAuth.getUid());
                hashMap1.put("role", "creator");
                hashMap1.put("timeStamp", gTimeStamp);

                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups Chat");
                ref1.child(groupId).child("Participants").child(mAuth.getUid())
                        .setValue(hashMap1)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //participants added
                                pd.dismiss();
                                Toast.makeText(GroupCreateActivity.this, "Group created successfully...", Toast.LENGTH_SHORT).show();
                                groupDescEt.setText("");
                                groupTitleEt.setText("");
                                groupImgIv.setImageResource(R.drawable.baseline_groups_100);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //failed
                                pd.dismiss();
                                Toast.makeText(GroupCreateActivity.this, "Fail..", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(GroupCreateActivity.this, "Fail..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImagePickDialog() {
        String options[] = {"Camera", "Gallery"};
        //alerDialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Pick Image From...");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //handle dialog item clicks
                if (which == 0) {
                    //camera clicked
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                } else if (which == 1) {
                    //gallery clicked
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }

                }
            }
        });
        builder.create().show();
    }


    private void init() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupImgIv = findViewById(R.id.groupImg);
        groupTitleEt = findViewById(R.id.groupTitleEt);
        groupDescEt = findViewById(R.id.groupDescEt);
        createGroupBtn = findViewById(R.id.createGroupBtn);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        myUid = user.getUid();

        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void pickFromCamera() {
        //intent of pikcing image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Group Image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Description");
        //put image uri
        image_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);

    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission() {
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //This method will be called after picking image from camera or gallery
        if (requestCode != RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //IMAGE Is pick from gallery, get uri of image

                try {
                    image_uri = data.getData();
                    groupImgIv.setImageURI(image_uri);
                } catch (Exception e) {

                }

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //IMAGE Is pick from camera, get uri of image
                try {
                    groupImgIv.setImageURI(image_uri);

                } catch (Exception e) {

                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                //picking from camera, first check if camera and storage permission allowed or not
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //permission enabled
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Please enable camera and storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                //picking from gallery, first check if storage permission allowed or not
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //permission enalbed
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        //checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //get time stamp
        Calendar ccDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveDatee = currentDate.format(ccDate.getTime());

        Calendar ccTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        final String saveTimee = currentTime.format(ccTime.getTime());

        String timeStamp = saveDatee + ":" + saveTimee;
        //set offline with last seen time stamp
        checkOnlineStatus(timeStamp);

    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();

    }
}