package com.tunghq.fsocialmobileapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.Adapter.PostAdapter;
import com.tunghq.fsocialmobileapp.Models.Posts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class PostDetailsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Posts> postsList;
    PostAdapter adapter;

    String postid;
    Toolbar toolbar;
    FirebaseAuth auth;

    String myUid;
    String notificationId, affectedPersonId;

    FirebaseUser user;
    TextView noPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        //init view
        noPost = findViewById(R.id.noPost);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        postsList=new ArrayList<>();
        adapter=new PostAdapter(this,postsList);

        //receive data from postAdapter, NotificationAdapter
        postid=getIntent().getStringExtra("postId");
        notificationId = getIntent().getStringExtra("notificationId");
        affectedPersonId = getIntent().getStringExtra("affectedPersonId");

        recyclerView.setAdapter(adapter);


        //init firebase
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        myUid = user.getUid();

        readPosts();

        //check user click from NotificationActivity.
        if(notificationId != null){
            //change seen notification
            changeStatusNotify(notificationId, affectedPersonId);
        }

    }

    private void changeStatusNotify(String notificationId, String affectedPersonId) {
        HashMap<String, Object> resultUpdate = new HashMap<>();
        resultUpdate.put("status", "seen");


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(affectedPersonId).child(notificationId);
        reference.updateChildren(resultUpdate);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void readPosts() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Posts")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    postsList.clear();
                    Posts posts=snapshot.getValue(Posts.class);

                    postsList.add(posts);

                    adapter.notifyDataSetChanged();
                    noPost.setVisibility(View.GONE);

                }else{
                    noPost.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(PostDetailsActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });

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
}