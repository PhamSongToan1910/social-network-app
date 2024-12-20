package com.tunghq.fsocialmobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.Adapter.PostsListAdapter;
import com.tunghq.fsocialmobileapp.Models.Posts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class OthersProfileActivity extends AppCompatActivity {


    Button btn_follow,btn_following, btn_chat, btn_sent;
    ImageView profile;
    ImageView bg;
    TextView username, featuredName,following_count,followers_count,pos_count, status;
    TextView followers,following;

    Toolbar toolbar;

    RecyclerView recyclerView;
    List<Posts> postsList;
    PostsListAdapter adapter;

    FirebaseAuth auth;
    FirebaseUser user;
    String id;
    String notificationId, affectedPersonId;
    String myUid;
    DatabaseReference reference;
    StorageReference storageReference,bgRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_profile);


        //init view
        init();

        //init firebase
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference= FirebaseStorage.getInstance().getReference().child("Profiles");
        bgRef= FirebaseStorage.getInstance().getReference().child("Backgrounds");
        myUid = user.getUid();

        //receive data from NotificationAdapter, PostAdapter, ShowUserAdapter
        id = getIntent().getStringExtra("uid");
        notificationId = getIntent().getStringExtra("notificationId");
        affectedPersonId = getIntent().getStringExtra("affectedPersonId");


        if (id.equals(user.getUid()))
        {
            btn_chat.setVisibility(View.GONE);
            btn_follow.setVisibility(View.GONE);
        }else
        {
            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference().child("checkSentFollow")
                    .child(user.getUid());
            reference2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(id).exists()) {
                        btn_follow.setVisibility(View.GONE);
                        btn_sent.setVisibility(View.VISIBLE);
                        btn_following.setVisibility(View.GONE);
                    } else {
                        checkFollow();
                        btn_sent.setVisibility(View.GONE);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            btn_chat.setVisibility(View.VISIBLE);
        }

        //init recyclerview
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(),3));

        postsList=new ArrayList<>();
        adapter=new PostsListAdapter(this,postsList);

        getPosts();
        recyclerView.setAdapter(adapter);


        getUserData();
        getFollowCount();

        //check user click from NotificationActivity
        if(notificationId != null){
            changeStatusNotify(notificationId, affectedPersonId);

        }


        clickListener();


    }

    private void clickListener() {
        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(OthersProfileActivity.this, ShowListActivity.class);
                intent.putExtra("pid",id);
                intent.putExtra("title",followers.getText().toString());
                startActivity(intent);
//                Toast.makeText(OthersProfile.this, "OK", Toast.LENGTH_SHORT).show();
            }
        });
        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(OthersProfileActivity.this, ShowListActivity.class);
                intent.putExtra("pid",id);
                intent.putExtra("title",following.getText().toString());
                startActivity(intent);
            }
        });
        //

        btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("checkSentFollow",true);
                FirebaseDatabase.getInstance().getReference().child("checkSentFollow")
                        .child(user.getUid())
                        .child(id).setValue(map);
                addNotifications();
                Toast.makeText(OthersProfileActivity.this, "Sent a follow request", Toast.LENGTH_SHORT).show();
            }
        });
        btn_following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_following.getText().toString().equals("Following"))
                {
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(user.getUid())
                            .child("following").child(id).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(id)
                            .child("followers").child(user.getUid()).removeValue();
                }
            }
        });

        btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(OthersProfileActivity.this, "Chat", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OthersProfileActivity.this, ChatActivity.class);
                intent.putExtra("chatuid", id);
                startActivity(intent);
            }
        });
    }

    private void changeStatusNotify(String notificationId, String affectedPersonId) {
        HashMap<String, Object> resultUpdate = new HashMap<>();
        resultUpdate.put("status", "seen");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(affectedPersonId).child(notificationId);
        reference.updateChildren(resultUpdate);
    }
    private void init()
    {
        btn_sent = findViewById(R.id.btn_sent);
        btn_follow=findViewById(R.id.btn_follow);
        btn_following=findViewById(R.id.btn_following);
        btn_chat = findViewById(R.id.btn_chat);
        profile=findViewById(R.id.profile_image);
        username=findViewById(R.id.username);
        featuredName =findViewById(R.id.memer);
        status = findViewById(R.id.status);
        bg=findViewById(R.id.background);
        following_count=findViewById(R.id.following_count);
        followers_count=findViewById(R.id.followers_count);
        pos_count=findViewById(R.id.posts);
        followers=findViewById(R.id.followers);
        following=findViewById(R.id.following);

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



    }

    private void getUserData()
    {

        reference.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String n=snapshot.child("userName").getValue().toString();
                String m=snapshot.child("featuredName").getValue().toString();
                String p=snapshot.child("profileImg").getValue().toString();
                String b=snapshot.child("backgroundImg").getValue().toString();
                String s=snapshot.child("status").getValue().toString();


                username.setText(n);
                featuredName.setText(m);
                Picasso.get().load(p).placeholder(R.drawable.profile_image).into(profile);
                Picasso.get().load(b).into(bg);
                status.setText(s);


//                Glide.with(getActivity()).load(p).centerCrop().placeholder(R.drawable.profile_image).into(profile);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OthersProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void getFollowCount()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(id).child("followers");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers_count.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OthersProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference databaseReference1=FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(id).child("following");
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following_count.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OthersProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference postCount=FirebaseDatabase.getInstance().getReference().child("Posts");
        postCount.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i=0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    String p=dataSnapshot.child("userId").getValue().toString();
                    if (p.equals(id))
                    {
                        i++;
                    }
                }
                pos_count.setText(""+i+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(OthersProfileActivity.this, "Error "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void checkFollow()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(user.getUid()).child("following");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(id).exists())
                {
                    btn_follow.setVisibility(View.GONE);
                    btn_following.setVisibility(View.VISIBLE);

                }else
                {
                    btn_following.setVisibility(View.GONE);
                    btn_follow.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getPosts()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Posts posts=dataSnapshot.getValue(Posts.class);
                    if (posts.getUserId().equals(id) && !posts.getType().equals("status"))
                    {
                        postsList.add(posts);
                    }
                }
                Collections.reverse(postsList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(OthersProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void addNotifications()
    {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Notifications").child(id);
        DatabaseReference reference2=FirebaseDatabase.getInstance().getReference().child("checkNotification").child(id);

        String notificationId = reference.push().getKey();

        HashMap<String,Object> map=new HashMap<>();
        HashMap<String,Object> map2=new HashMap<>();

        map.put("notificationId", notificationId);
        map.put("affectedPersonId", id);
        map.put("userId",user.getUid());
        map.put("groupId", "");
        map.put("textNotifications","want to follow you");
        map.put("postId","");
        map.put("check","user");
        map.put("status", "NoSeen");
        map2.put("seen", true);

        reference.child(notificationId).setValue(map);
        reference2.setValue(map2);


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




}