package com.tunghq.fsocialmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tunghq.fsocialmobileapp.Adapter.ParticipantAddAdapter;
import com.tunghq.fsocialmobileapp.Models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GroupInfoActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView groupImgIv;
    TextView descTv, createdByTv, editGroupTv, addParticipantTv, leaveGroupTv, participantsTv, actionBarTv;
    RecyclerView participantRv;

    ArrayList<User> userList;
    List<String> participantList;
    ParticipantAddAdapter participantAddAdapter;


    String myUid, groupId, myGroupRole = "";
    FirebaseAuth firebaseAuth;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        init();

        groupId = getIntent().getStringExtra("groupId");

        loadGroupInfo();
        loadMyGroupRole();
        getUser();

        clickListener();
    }

    private void clickListener() {
        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupAddParticipantsActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });
        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dialogTitle = "";
                String dialogDesc = "";
                String positiveButtonTitle = "";
                if(myGroupRole.equals("creator")){
                    dialogTitle = "Delete Group";
                    dialogDesc = "Are you sure wan to Delete group permanently?";
                    positiveButtonTitle = "Delete";

                }else{
                    dialogTitle = "Leave Group";
                    dialogDesc = "Are you sure wan to Leave group permanently?";
                    positiveButtonTitle = "Leave";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDesc)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(myGroupRole.equals("creator")){
                                    deleteGroup();
                                }else{
                                    leaveGroup();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                builder.create().show();
            }
        });
        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupEditActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });
    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
        ref.child(groupId).child("Participants").child(myUid)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(GroupInfoActivity.this, "Left Successfully...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, HomeActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
        ref.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(GroupInfoActivity.this, "Group Successfully Deleted...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, HomeActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, "Failed...", Toast.LENGTH_SHORT).show();

                    }
                });

    }


    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String groupId = "" + ds.child("groupId").getValue();
                    String groupTitle = "" + ds.child("groupTitle").getValue();
                    String groupDesc = "" + ds.child("groupDesc").getValue();

                    String groupImg = "" + ds.child("groupImg").getValue();
                    String creatorId = "" + ds.child("creatorId").getValue();
                    String timeStamp = "" + ds.child("timeStamp").getValue();

                    loadCreatorInfo(timeStamp, creatorId);

                    actionBarTv.setText(groupTitle);
                    descTv.setText(groupDesc);
                    try {
                        Picasso.get().load(groupImg).placeholder(R.drawable.baseline_groups_100).into(groupImgIv);
                    }catch (Exception e){
                        groupImgIv.setImageResource(R.drawable.baseline_groups_100);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadCreatorInfo(String timeStamp, String creatorId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("userId").equalTo(creatorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("userName").getValue();
                    String featuredName = "" + ds.child("featuredName").getValue();
                    createdByTv.setText("Created by: " + name + " (" + featuredName + " ) on " + timeStamp);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                        if (myGroupRole.equals("creator")) {
                            addParticipantTv.setVisibility(View.VISIBLE);
                            editGroupTv.setVisibility(View.VISIBLE);
                            leaveGroupTv.setText("Delete Group");

                        } else if (myGroupRole.equals("admin")) {
                            addParticipantTv.setVisibility(View.VISIBLE);
                            editGroupTv.setVisibility(View.GONE);
                            leaveGroupTv.setText("Leave Group");

                        } else if (myGroupRole.equals("participant")) {
                            editGroupTv.setVisibility(View.GONE);
                            addParticipantTv.setVisibility(View.GONE);
                            leaveGroupTv.setText("Leave Group");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void getUser()
    {
        participantList=new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat").child(groupId).child("Participants");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        participantList.add(dataSnapshot.getKey());
                    }
                    getAllParticipants();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAllParticipants() {
        userList = new ArrayList<>();
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
        //Query query=postRef.orderByChild("counterPost");
        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    User model = ds.getValue(User.class);
                    for (String id : participantList)
                    {
                        if (model.getUserId().equals(id))
                        {

                            userList.add(model);
                        }
                    }

                }
                Collections.reverse(userList);
                participantAddAdapter = new ParticipantAddAdapter(GroupInfoActivity.this, userList, "" + groupId, "" + myGroupRole);
                participantAddAdapter.notifyDataSetChanged();
                participantRv.setAdapter(participantAddAdapter);
                participantsTv.setText("Participants (" + participantList.size() + ")");


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
    }

//    private void getAllParticipants() {
//        //init List
//        userList = new ArrayList<>();
//        //load users from db
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
//        ref.child(groupId).child("Participants")
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        userList.clear();
//                        for (DataSnapshot ds : snapshot.getChildren()){
//                            //get uid from group > participants
//                            String uid = "" + ds.child("uid").getValue();
//
//                            //get info od user using uid we got above
//                            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
//                            ref1.orderByChild("userId").equalTo(uid)
//                                    .addValueEventListener(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                            for (DataSnapshot ds : snapshot.getChildren()) {
//                                                User model = ds.getValue(User.class);
//                                                userList.add(model);
//
//                                            }
//                                            //setup adapter
//                                            participantAddAdapter = new ParticipantAddAdapter(GroupInfoActivity.this, userList, "" + groupId, "" + myGroupRole);
//                                            participantRv.setAdapter(participantAddAdapter);
//
//
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError error) {
//
//                                        }
//                                    });
//                        }
//
//
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }

    private void init() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupImgIv = findViewById(R.id.groupImgIv);
        descTv = findViewById(R.id.descriptionTv);
        createdByTv = findViewById(R.id.createdByTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        participantRv = findViewById(R.id.participantsRv);
        participantsTv = findViewById(R.id.participantsTv);

        actionBarTv = findViewById(R.id.actionBarTv);


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
        //getAllParticipants();

        super.onResume();

    }
}