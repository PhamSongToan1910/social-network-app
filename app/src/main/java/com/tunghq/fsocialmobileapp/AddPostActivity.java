package com.tunghq.fsocialmobileapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class AddPostActivity extends AppCompatActivity {
    ImageView pick_image;
    TextView post;
    EditText description;
    Button addVideo, addImage;
    private static final int PICK_FILE = 1;
    VideoView videoView;
    MediaController mediaController;
    UploadTask uploadTask;
    String type;
    CardView post_card;
    String profileUrl, username, featuredName;
    Uri postUri;
    ProgressDialog pd;
    FirebaseUser user;
    FirebaseAuth auth;
    DatabaseReference userRef, postRef;
    StorageReference storageReference;
    String editDescription, editPostUrl, editType;
    String myUid;
    String isUpdateKey,editPostId;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        //init view
        init();

        //init firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        myUid = user.getUid();
        storageReference = FirebaseStorage.getInstance().getReference().child("Posts");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        //get data update
        Intent intent = getIntent();
        isUpdateKey = "" + intent.getStringExtra("key");
        editPostId = "" + intent.getStringExtra("editPostId");

        //post edit
        if (isUpdateKey.equals("editPost")) {
            //update
            post.setText("Update");
            loadPostDataEdit(editPostId);
        } else {
            //add
            post.setText("Upload");
        }


        //check permission
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddPostActivity.this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        //get some user value to display when updating
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userRef.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    username = "" + ds.child("userName").getValue();
                    featuredName = "" + ds.child("featuredName").getValue();
                    profileUrl = "" + ds.child("profileImg").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        clickListener();


    }

    private void clickListener() {
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String desc = description.getText().toString().trim();
                if (isUpdateKey.equals("editPost")) {
                    beginUpdate(desc, editPostId);
                } else {
                    uploadData();
                }

            }
        });
        addVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseVideo();
            }
        });
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
    }

    private void beginUpdate(String desc, String editPostId) {
        pd.setMessage("Updating Post...");
        pd.show();

        if (editType.equals("image")) {
            //with image
            updateWithImage(desc, editPostId);
        } else if (editType.equals("video")) {
            //with video
            updateWithVideo(desc, editPostId);
        } else if(editType.equals("status")){
            //with status
            updateStatusPost(desc,editPostId);
        }
    }

    private void updateStatusPost(String desc, String editPostId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        //put post info
        hashMap.put("postId", editPostId);
        hashMap.put("postUrl", "StatusPost");
        hashMap.put("description", desc);
        hashMap.put("userId", user.getUid());
        hashMap.put("profileImg", profileUrl);
        hashMap.put("featuredName", featuredName);
        hashMap.put("userName", username);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        description.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWithVideo(String desc, String editPostId) {
        StorageReference mVideoRef = FirebaseStorage.getInstance().getReferenceFromUrl(editPostUrl);
        mVideoRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        //get time stamp
                        Date date = new Date();
                        SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm a");
                        String currentDate = format.format(date);

                        String filePathAndName = "Post/" + "post_" + currentDate;

                        if (postUri != null) {

                            final StorageReference reference = storageReference.child(filePathAndName);
                            uploadTask = reference.putFile(postUri);

                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();

                                    }
                                    return reference.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();

                                        //upload data
                                            //post with video
                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("postId", editPostId);
                                            map.put("postUrl", downloadUri.toString());
                                            map.put("description", desc);
                                            map.put("userId", user.getUid());
                                            map.put("profileImg", profileUrl);
                                            map.put("featuredName", featuredName);
                                            map.put("userName", username);

                                            //path to store postdata
                                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Posts");
                                            //put data in ref
                                            reference1.child(editPostId).updateChildren(map);

                                            Toast.makeText(AddPostActivity.this, "Post Updated", Toast.LENGTH_SHORT).show();
                                            pd.dismiss();

                                        }  else {
                                            Toast.makeText(AddPostActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                            pd.dismiss();
                                        }



                                }
                            });


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWithImage(String desc, String editPostId) {
        StorageReference mPicRef = FirebaseStorage.getInstance().getReferenceFromUrl(editPostUrl);
        mPicRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //image deleted, upload new image
                        //for post image name, postid, publishtime
                        //get time stamp
                        Date date = new Date();
                        SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm a");
                        String currentDate = format.format(date);

                        String filePathAndName = "Post/" + "post_" + currentDate;

                        //get image from imageview
                        Bitmap bitmap = ((BitmapDrawable) pick_image.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        //image compress
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        //image upload get its url
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful()) ;

                                        String downloadUri = uriTask.getResult().toString();
                                        if (uriTask.isSuccessful()) {
                                            //url is received, upload to firebase database
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            //put post info
                                            hashMap.put("postId", editPostId);
                                            hashMap.put("postUrl", downloadUri.toString());
                                            hashMap.put("description", desc);
                                            hashMap.put("userId", user.getUid());
                                            hashMap.put("profileImg", profileUrl);
                                            hashMap.put("featuredName", featuredName);
                                            hashMap.put("userName", username);


                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                            ref.child(editPostId)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                                            description.setText("");
                                                            pick_image.setImageResource(android.R.color.black);
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //image not uploaded
                                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPostDataEdit(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //get detail of post using id of post
        Query fquery = reference.orderByChild("postId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    editDescription = "" + ds.child("description").getValue();
                    editPostUrl = "" + ds.child("postUrl").getValue();
                    editType = "" + ds.child("type").getValue();
                    //set
                    description.setText(editDescription);

                    if (editType.equals("image")) {
                        addVideo.setVisibility(View.GONE);
                        try {
                            Picasso.get().load(editPostUrl).into(pick_image);
                            videoView.setVisibility(View.GONE);
                        } catch (Exception e) {

                        }
                    } else if (editType.equals("video")) {
                        addImage.setVisibility(View.GONE);
                        videoView.setMediaController(mediaController);
                        videoView.setVisibility(View.VISIBLE);
                        videoView.setVideoURI(Uri.parse(editPostUrl));
                        videoView.start();
                    } else if (editType.equals("status")) {
                        videoView.setVisibility(View.GONE);
                        pick_image.setVisibility(View.GONE);
                        
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == PICK_FILE || requestCode == RESULT_OK
                    || data != null || data.getData() != null) {

                postUri = data.getData();
                if (postUri.toString().contains("image")) {

                    Picasso.get().load(postUri).into(pick_image);
                    pick_image.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.INVISIBLE);
                    type = "image";

                } else if (postUri.toString().contains("video")) {

                    videoView.setMediaController(mediaController);
                    videoView.setVisibility(View.VISIBLE);
                    pick_image.setVisibility(View.INVISIBLE);
                    videoView.setVideoURI(postUri);
                    videoView.start();
                    type = "video";

                } else {
                    Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
                }

            }
        } catch (Exception e) {
        }
        try {
            if (data.getData() == null) {
                videoView.setVisibility(View.GONE);
                pick_image.setVisibility(View.GONE);
                type = "status";
            }
        }catch (Exception e){

        }


    }

    private void uploadData() {
        pd.setMessage("Publishing post..");
        pd.show();

        String postid = postRef.push().getKey();
        //get time stamp
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm a");
        String currentDate = format.format(date);

        String filePathAndName = "Post/" + "post_" + currentDate;

        if (postUri != null) {

            final StorageReference reference = storageReference.child(filePathAndName);
            uploadTask = reference.putFile(postUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();

                    }
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        //upload data
                        if (type.equals("image")) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("time", currentDate);
                            map.put("postId", postid);
                            map.put("postUrl", downloadUri.toString());
                            map.put("description", description.getText().toString());
                            map.put("userId", user.getUid());
                            map.put("profileImg", profileUrl);
                            map.put("featuredName", featuredName);
                            map.put("userName", username);
                            map.put("type", "image");

                            pd.dismiss();
                            //path to store postdata
                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Posts");
                            //put data in ref
                            reference1.child(postid).setValue(map);

                            Toast.makeText(AddPostActivity.this, "Post Uploaded", Toast.LENGTH_SHORT).show();
                            description.setText("");
                            pick_image.setImageResource(android.R.color.transparent);

                        } else if (type.equals("video")) {
                            //post with video
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("time", currentDate);
                            map.put("postId", postid);
                            map.put("postUrl", downloadUri.toString());
                            map.put("description", description.getText().toString());
                            map.put("userId", user.getUid());
                            map.put("profileImg", profileUrl);
                            map.put("featuredName", featuredName);
                            map.put("userName", username);
                            map.put("type", "video");

                            pd.dismiss();
                            //path to store postdata
                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Posts");
                            //put data in ref
                            reference1.child(postid).setValue(map);

                            Toast.makeText(AddPostActivity.this, "Post Uploaded", Toast.LENGTH_SHORT).show();
                            description.setText("");
                            videoView.setVisibility(View.GONE);

                        }  else {
                            Toast.makeText(AddPostActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }

                    }

                }
            });


        } else {
            //post status
            HashMap<String, Object> map = new HashMap<>();
            map.put("time", currentDate);
            map.put("postId", postid);
            map.put("postUrl", "Status Post");
            map.put("description", description.getText().toString());
            map.put("userId", user.getUid());
            map.put("profileImg", profileUrl);
            map.put("featuredName", featuredName);
            map.put("userName", username);
            map.put("type", "status");

            pd.dismiss();
            //path to store postdata
            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Posts");
            //put data in ref
            reference1.child(postid).setValue(map);

            Toast.makeText(AddPostActivity.this, "Post Uploaded", Toast.LENGTH_SHORT).show();
            description.setText("");
        }

    }

    private void init() {

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pick_image = findViewById(R.id.pick);
        post = findViewById(R.id.post_upload);
        pick_image = findViewById(R.id.pick);
        post_card = findViewById(R.id.post_card);
        description = findViewById(R.id.post_description);
        pd = new ProgressDialog(this);
        addVideo = findViewById(R.id.btnAddvideo);
        addImage = findViewById(R.id.btnAddImage);
        videoView = findViewById(R.id.vv_post);

    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        //intent.setType("video/*");

        startActivityForResult(intent, PICK_FILE);
    }

    private void chooseVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_FILE);
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus",status);
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

        String timeStamp = saveDatee +":"+ saveTimee;
        //set offline with last seen time stamp
        checkOnlineStatus(timeStamp);

    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}