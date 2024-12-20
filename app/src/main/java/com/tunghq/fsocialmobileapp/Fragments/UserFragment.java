package com.tunghq.fsocialmobileapp.Fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.tunghq.fsocialmobileapp.Adapter.PostsListAdapter;
import com.tunghq.fsocialmobileapp.Models.Posts;
import com.tunghq.fsocialmobileapp.R;
import com.tunghq.fsocialmobileapp.ShowListActivity;
import com.tunghq.fsocialmobileapp.SignInActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class UserFragment extends Fragment {
    String storagePath = "Users_Profile_Background_Imgs/";
    TextView followers, following;
    Button btn_update, u_bg;
    ImageView profile;
    ImageView bg;
    TextView username, featuredName, following_count, followers_count, pos_count, status;
    ProgressDialog pd;
    RecyclerView recyclerView;
    List<Posts> postsList;
    PostsListAdapter adapter;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    String cameraPermissions[];
    String storagePermissions[];
    FirebaseAuth auth;
    FirebaseUser user;
    String profileid;
    String uid;
    DatabaseReference reference;
    DatabaseReference ref;
    StorageReference storageReference, bgRef;
    Uri image_uri;
    String profileOrBackgroundPhoto;
    FloatingActionButton fab;
    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);


        SharedPreferences preferences = getContext().getSharedPreferences("USERS", Context.MODE_PRIVATE);
        profileid = preferences.getString("profileid", "none");

       // init view
        init(view);

        //init array of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference().child("Profiles");
        bgRef = FirebaseStorage.getInstance().getReference().child("Backgrounds");
        ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        uid = user.getUid();

        //init recyclerview
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        //init list
        postsList = new ArrayList<>();
        adapter = new PostsListAdapter(getContext(), postsList);


        recyclerView.setAdapter(adapter);
        getImages();


        //clicks();
        getUserData();
        getCount();

        clickListener();

        return view;

    }

    private void clickListener() {
        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShowListActivity.class);
                intent.putExtra("pid", uid);
                intent.putExtra("title", followers.getText().toString());
                startActivity(intent);
            }
        });
        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShowListActivity.class);
                intent.putExtra("pid", uid);
                intent.putExtra("title", following.getText().toString());
                startActivity(intent);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setItems(new String[]{"Edit Profile", "Log Out"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            showEditProfileDialog();
                        }
                        if (i == 1) {
                            Toast.makeText(getContext(), "Log Out", Toast.LENGTH_SHORT).show();
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

                            auth.signOut();
                            startActivity(new Intent(getActivity(), SignInActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                        }
                    }
                });
                builder.create().show();
            }
        });
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus",status);
        //update
        dbRef.updateChildren(hashMap);
    }
    private void showEditProfileDialog() {
        String options[] = {"Edit Name", "Edit Featured Name", "Edit Status", "Edit Profile Photo", "Edit Profile Background Photo", "Change Password"};
        //alerDialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //handle dialog item clicks
                if (which == 0) {

                    pd.setMessage("Updating Name");
                    showUpdateDialog("userName");
                } else if (which == 1) {

                    pd.setMessage("Updating Featured Name");
                    showUpdateDialog("featuredName");
                } else if (which == 2) {
                    pd.setMessage("Updating Status");
                    showUpdateDialog("status");

                } else if (which == 3) {
                    pd.setMessage("Updating Profile Photo");
                    profileOrBackgroundPhoto = "profileImg";
                    showImagePicDialog();
                } else if (which == 4) {
                    pd.setMessage("Updating Background Photo");
                    profileOrBackgroundPhoto = "backgroundImg";
                    showImagePicDialog();
                }else if (which == 5) {
                    pd.setMessage("Changing Password");
                    showChangePasswordDialog();
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showChangePasswordDialog() {

        //dialog password view
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password, null);

        EditText prePasswordEt = view.findViewById(R.id.prePasswordEt);
        EditText newPasswordEt = view.findViewById(R.id.newPasswordEt);
        Button updatePassBtn = view.findViewById(R.id.updatePassBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        updatePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //validate password data
                String oldPassword = prePasswordEt.getText().toString();
                String newPassword = newPasswordEt.getText().toString();
                if(TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(getActivity(), "Enter your current password...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(newPassword.length()<6){
                    Toast.makeText(getActivity(), "Password length must at least 6 character...", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldPassword, newPassword);
            }
        });
    }

    private void updatePassword(String oldPassword, String newPassword) {
        pd.show();
        //get current user
        FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();

        //before changing pass re-authenticate the user
        AuthCredential authCredential = EmailAuthProvider.getCredential(user1.getEmail(), oldPassword);
        user1.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //success authenticated, begin update
                        user1.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Updated.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Update fail", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //fail
                        pd.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showImagePicDialog() {
        String options[] = {"Camera", "Gallery"};
        //alerDialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
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
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

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
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission() {
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showUpdateDialog(String key) {
        //custom dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(linearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        //add edit text
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);//hint e.g. edit name or edit phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add button
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //input
                String value = editText.getText().toString().trim();

                if (!TextUtils.isEmpty(value)) {
                    pd.show();
                    switch (key) {
                        case "featuredName":
                            pd.show();
                            DatabaseReference referencee = FirebaseDatabase.getInstance().getReference();
                            Query querye = referencee.child("Users").orderByChild("featuredName").equalTo("@"+value);
                            querye.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // dataSnapshot is the "issue" node with all children with id 0
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "Featured name already exists, please enter another featured name", Toast.LENGTH_SHORT).show();

                                    }else{
                                        HashMap<String, Object> result1 = new HashMap<>();
                                        result1.put(key, "@" + value);
                                        reference.child(user.getUid()).updateChildren(result1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //update, dismiss progress
                                                pd.dismiss();
                                                Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        Query query2 = ref.orderByChild("userId").equalTo(uid);
                                        query2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()) {
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("featuredName").setValue("@" + value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            });


                            break;
                        case "userName":
                            HashMap<String, Object> result2 = new HashMap<>();
                            result2.put(key, value);
                            reference.child(user.getUid()).updateChildren(result2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //update, dismiss progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            Query query = ref.orderByChild("userId").equalTo(uid);
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String child = ds.getKey();
                                        snapshot.getRef().child(child).child("userName").setValue(value);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            break;
                        case "status":
                            HashMap<String, Object> result3 = new HashMap<>();
                            result3.put(key, value);
                            reference.child(user.getUid()).updateChildren(result3).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //update, dismiss progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                    }
                } else {
                    Toast.makeText(getActivity(), "Please Enter " + key, Toast.LENGTH_SHORT).show();

                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //This method will be called after picking image from camera or gallery
        if (requestCode != RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //IMAGE Is pick from gallery, get uri of image

                try {
                    image_uri = data.getData();
                    uploadProfileBackgroundPhoto(image_uri);
                } catch (Exception e) {

                }

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //IMAGE Is pick from camera, get uri of image
                try {
                    uploadProfileBackgroundPhoto(image_uri);
                } catch (Exception e) {

                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileBackgroundPhoto(final Uri uri) {
        /*
        Instead of creating separate function for profile picture and cover photo
        * i am doing word for both in same function
        * To add check ill add a string variable and assign it value "image" when user clicks "Edit profile pic", and assign it
        value "cover when user click "Edit cover photo"
        Here: image is the key in each user containing url of user's profile picture
              cover is the key in each user containing url of user's profile picture
        *The parameter "image_uri" contains the uri of image picked either from camera or gallery
         I will user UID of the currently signed in user as name of the image so there Will be only
         one image profile and one image for cover for each user*/
        //show progress
        pd.show();
        //path and name of image to be stored in firebase storage
        String filePathAndName = storagePath + "" + profileOrBackgroundPhoto + "_" + user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage, now get it's url and store in user's dtb
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();

                        //check if image is upload or not and is received
                        if (uriTask.isSuccessful()) {
                            //image uploaded
                            //add/upload url in user's database
                            HashMap<String, Object> results = new HashMap<>();

                            /*First parameter is profileOrCoverPhoto that has value "image" or "background"
                             * which are keys in user's database where url of image will be saved in one of
                             * them
                             * Second parameter contains the url of the image stored in firebase storage, this url will be saved as value
                             * against key "image" or "background"*/
                            results.put(profileOrBackgroundPhoto, downloadUri.toString());
                            reference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated", Toast.LENGTH_SHORT).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error Updating....", Toast.LENGTH_SHORT).show();

                                        }
                                    });
                            if (profileOrBackgroundPhoto.equals("profileImg")) {
                                Query query = ref.orderByChild("userId").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            String child = ds.getKey();
                                            snapshot.getRef().child(child).child("profileImg").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        } else {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some error occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were some errors, get and show error message
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                        //permisstion enalbed
                        pickFromCamera();
                    } else {
                        //permisstion denied
                        Toast.makeText(getActivity(), "Please enable camera and storage permission", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
    }

    private void getCount() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(user.getUid()).child("followers");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers_count.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(user.getUid()).child("following");
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following_count.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference postCount = FirebaseDatabase.getInstance().getReference().child("Posts");
        postCount.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String p = dataSnapshot.child("userId").getValue().toString();
                    if (p.equals(user.getUid())) {
                        i++;
                    }
                }
                pos_count.setText("" + i + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getActivity(), "Error " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void getUserData() {

        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String n = snapshot.child("userName").getValue().toString();
                String m = snapshot.child("featuredName").getValue().toString();
                String p = snapshot.child("profileImg").getValue().toString();
                String b = snapshot.child("backgroundImg").getValue().toString();
                String s = snapshot.child("status").getValue().toString();


                username.setText(n);
                featuredName.setText(m);
                Picasso.get().load(p).placeholder(R.drawable.profile_image).into(profile);
                Picasso.get().load(b).into(bg);
                status.setText(s);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();

            }
        });

    }


    private void init(View view) {

        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        featuredName = view.findViewById(R.id.memer);
        status = view.findViewById(R.id.status);
        bg = view.findViewById(R.id.background);
        following_count = view.findViewById(R.id.following_count);
        followers_count = view.findViewById(R.id.followers_count);
        pos_count = view.findViewById(R.id.posts);
        pd = new ProgressDialog(getContext());
        fab = view.findViewById(R.id.settingProfile);


    }
    private void getImages() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Posts posts = dataSnapshot.getValue(Posts.class);
                    if (posts.getUserId().equals(user.getUid()) && !posts.getType().equals("status")) {
                        postsList.add(posts);
                    }
                }
                Collections.reverse(postsList);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


