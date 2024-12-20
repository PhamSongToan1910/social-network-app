package com.tunghq.fsocialmobileapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.tunghq.fsocialmobileapp.Adapter.GroupChatAdapter;
import com.tunghq.fsocialmobileapp.Models.GroupChat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    String groupId, myGroupRole = "";
    Toolbar toolbar;
    ImageView groupImgIv, addParticipant, groupInfo;
    ImageView attachBtn, sendBtn;
    TextView groupTitleTv;
    EditText messageEt;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    String myUid;
    RecyclerView chatRv;

    ArrayList<GroupChat> groupChatList;
    GroupChatAdapter adapterGroupChat;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    String cameraPermissions[];
    String storagePermissions[];
    Uri image_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        //init
        init();
        //get data
        groupId = getIntent().getStringExtra("groupId");

        //set view

        //load data
        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();


        //click
        clickListener();
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
        ref.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            myGroupRole = "" + ds.child("role").getValue();

                        }
                        if (myGroupRole.equals("creator") || myGroupRole.equals("admin")) {
                            addParticipant.setVisibility(View.VISIBLE);
                        } else {
                            addParticipant.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadGroupMessages() {

        groupChatList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
        ref.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    GroupChat model = ds.getValue(GroupChat.class);
                    groupChatList.add(model);
                }
                //adapter
                adapterGroupChat = new GroupChatAdapter(GroupChatActivity.this, groupChatList);
                adapterGroupChat.notifyItemInserted(groupChatList.size() - 1);
                chatRv.setAdapter(adapterGroupChat);
                //Setted recyclerView to scroll to the last item of the List
                chatRv.scrollToPosition(groupChatList.size() - 1);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void clickListener() {
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEt.getText().toString().trim();
                //validate
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(GroupChatActivity.this, "Can't send empty message...", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);
                }
            }
        });
        addParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupChatActivity.this, GroupAddParticipantsActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePicDialog();
            }
        });
        groupInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });
    }
    private void showImagePicDialog() {
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
    private void pickFromCamera() {
        //intent of pikcing image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //This method will be called after picking image from camera or gallery
        try {
            if (requestCode != RESULT_OK) {

                if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                    //IMAGE Is pick from gallery, get uri of image
                    image_uri = data.getData();

                    try {

                        sendImageMessage(image_uri);
                    } catch (IOException e) {
                        e.printStackTrace();

                    }

                }
                if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                    //IMAGE Is pick from camera, get uri of image
                    try {
                        sendImageMessage(image_uri);

                    } catch (IOException e) {
                        e.printStackTrace();

                    }

                }
            }
        }catch (Exception e){

        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                //picking from camera, first check if camera and storage permission allowed or not
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //permisstion enalbed
                        pickFromCamera();
                    } else {
                        //permisstion denied

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
                        //permisstion enalbed
                        pickFromGallery();
                    } else {
                        //permisstion denied
                        Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
    }
    private void sendImageMessage(Uri image_uri) throws IOException {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image....");
        progressDialog.show();

        Calendar ccDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveDate = currentDate.format(ccDate.getTime());

        Calendar ccTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        final String saveTime = currentTime.format(ccTime.getTime());

        String timeStamp1 = saveDate +":"+ saveTime;

        String fileNameAndPath = "ChatImages/"+"group_chat_"+timeStamp1;

        //chat not will be created that will contain all images sent via chat

        //get bitmap form image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //image updated
                        progressDialog.dismiss();

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();

                        if(uriTask.isSuccessful()){
                            //add image uri and other info to dtb
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
                            String messageId = ref.push().getKey();
                            //set up message data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("groupId", ""+groupId);
                            hashMap.put("messageId", "" + messageId);
                            hashMap.put("sender", "" + myUid);
                            hashMap.put("message", "" + downloadUri);
                            hashMap.put("timeStamp", "" + timeStamp1);
                            hashMap.put("date",""+saveDate);
                            hashMap.put("time",""+saveTime);
                            hashMap.put("type", "" + "image");

                            //add in db
                            ref.child(groupId).child("Messages").child(messageId)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //sent
                                            messageEt.setText("");
                                            progressDialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //fail
                                            progressDialog.dismiss();
                                            Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                            //send noti
                            //String img = "Sent you a photo...";
                            //addNotifications(img);




                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        progressDialog.dismiss();
                    }
                });



    }

    private void sendMessage(String message) {
        //time stamp
        Calendar cDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveDate = currentDate.format(cDate.getTime());

        Calendar cTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        final String saveTime = currentTime.format(cTime.getTime());

        String time = saveDate + ":" + saveTime;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
        String messageId = ref.push().getKey();
        //set up message data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("groupId", ""+groupId);
        hashMap.put("messageId", "" + messageId);
        hashMap.put("sender", "" + myUid);
        hashMap.put("message", "" + message);
        hashMap.put("timeStamp", "" + time);
        hashMap.put("date",""+saveDate);
        hashMap.put("time",""+saveTime);
        hashMap.put("type", "" + "text");

        //add in db
        ref.child(groupId).child("Messages").child(messageId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //sent
                        messageEt.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //fail
                        Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
        Query query = ref.orderByChild("groupId").equalTo(groupId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String groupTitle = "" + ds.child("groupTitle").getValue();
                    String groupDesc = "" + ds.child("groupDesc").getValue();
                    String groupImg = "" + ds.child("groupImg").getValue();
                    String timeStamp = "" + ds.child("timeStamp").getValue();
                    String creatorId = "" + ds.child("creatorId").getValue();

                    groupTitleTv.setText(groupTitle);
                    try {
                        Picasso.get().load(groupImg).placeholder(R.drawable.baseline_groups_white_24).into(groupImgIv);
                    } catch (Exception e) {
                        groupImgIv.setImageResource(R.drawable.baseline_groups_white_24);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void init() {
        toolbar = findViewById(R.id.toolbarGroupChat);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupImgIv = findViewById(R.id.groupImgIv);
        groupTitleTv = findViewById(R.id.groupTitleTv);
        attachBtn = findViewById(R.id.attachBtnGroupChat);
        messageEt = findViewById(R.id.messageEtGroupChat);
        sendBtn = findViewById(R.id.sendBtnGroupChat);
        chatRv = findViewById(R.id.groupChat_RecyclerView);
        addParticipant = findViewById(R.id.addParticipant);
        groupInfo = findViewById(R.id.groupInfo);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);


        chatRv.setHasFixedSize(true);
        chatRv.setLayoutManager(linearLayoutManager);

        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        myUid = user.getUid();
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