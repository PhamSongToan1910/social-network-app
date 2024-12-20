package com.tunghq.fsocialmobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.Adapter.CommentAdapter;
import com.tunghq.fsocialmobileapp.Models.Comment;
import com.tunghq.fsocialmobileapp.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    CircleImageView profile;
    EditText edit_comment;
    ImageView send;
    Toolbar toolbar;
    TextView no;

    RecyclerView recyclerView;
    List<Comment> commentList;
    CommentAdapter adapter;

    String myUid;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference, commentRef;

    public static String postid;
    public static String publisher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        //init view
        profile=findViewById(R.id.profile_image);
        edit_comment=findViewById(R.id.comment_edit);
        send=findViewById(R.id.send);
        no=findViewById(R.id.no);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init firebase
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        reference=FirebaseDatabase.getInstance().getReference().child("Users");

        myUid = user.getUid();


        //receive data from postAdapter
        Intent intent=getIntent();
        postid=intent.getStringExtra("postId");
        publisher=intent.getStringExtra("userId");

        //init recyclerview
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        //init lists
        commentList=new ArrayList<>();
        adapter=new CommentAdapter(this,commentList);

        getComments();
        commentRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        recyclerView.setAdapter(adapter);
        getImage();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_comment.getText().toString().isEmpty())
                {
                    Toast.makeText(CommentActivity.this, "Comment can't be empty", Toast.LENGTH_SHORT).show();
                }else
                {
                    addComment();
                }
            }
        });



    }

    private void getComments() {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Comments")
                .child(postid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    commentList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren())
                    {
                        Comment comment=dataSnapshot.getValue(Comment.class);
                        commentList.add(comment);
                    }
                    adapter.notifyDataSetChanged();
                    no.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }else
                {
                    no.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(CommentActivity.this, "Error while load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addComment() {
        Date date=new Date();
        SimpleDateFormat format=new SimpleDateFormat("dd-M-yyyy hh:mm a");
        String currentDate=format.format(date);
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);
        String commentid = databaseReference.push().getKey();

        HashMap<String,Object> map=new HashMap<>();
        map.put("commentId", commentid);
        map.put("comment",edit_comment.getText().toString());
        map.put("userId",user.getUid());
        map.put("time",currentDate);

        databaseReference.child(commentid).setValue(map);
        addNotifications();
        edit_comment.setText("");
    }
    private void addNotifications()
    {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Notifications").child(publisher);
        DatabaseReference reference2=FirebaseDatabase.getInstance().getReference().child("checkNotification").child(publisher);

        String notificationId = reference.push().getKey();
        HashMap<String,Object> map=new HashMap<>();
        HashMap<String,Object> map2=new HashMap<>();

        if(!user.getUid().equals(publisher)){
            map.put("notificationId", notificationId);
            map.put("affectedPersonId", publisher);
            map.put("userId",user.getUid());
            map.put("groupId", "");
            map.put("textNotifications","Commented: "+edit_comment.getText().toString());
            map.put("postId",postid);
            map.put("check","post");
            map.put("status", "NoSeen");
            map2.put("seen", true);
            reference.child(notificationId).setValue(map);
            reference2.setValue(map2);
        }


    }
    private void getImage()
    {
        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User data=snapshot.getValue(User.class);

                Picasso.get().load(data.getProfileImg()).into(profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
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

//