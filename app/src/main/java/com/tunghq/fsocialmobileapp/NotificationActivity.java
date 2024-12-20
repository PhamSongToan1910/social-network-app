package com.tunghq.fsocialmobileapp;

import static com.tunghq.fsocialmobileapp.Fragments.ChatListFragment.note4;
import static com.tunghq.fsocialmobileapp.Fragments.FavouriteFragment.note3;
import static com.tunghq.fsocialmobileapp.Fragments.FeedFragment.note2;
import static com.tunghq.fsocialmobileapp.Fragments.FollowingFragment.note;
import static com.tunghq.fsocialmobileapp.Fragments.GroupListFragment.note5;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.Adapter.NotificationAdapter;
import com.tunghq.fsocialmobileapp.Models.Notifications;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;



public class NotificationActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    NotificationAdapter adapter;
    TextView clearAll;
    List<Notifications> list;

    Toolbar toolbar;
    FirebaseUser user;
    String myUid;
    FirebaseAuth auth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        myUid = user.getUid();

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        clearAll = findViewById(R.id.clearAll);
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        list=new ArrayList<>();
        adapter=new NotificationAdapter(this,list);

        readNotifications();
        recyclerView.setAdapter(adapter);

        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllNotifications();
            }
        });

    }

    private void clearAllNotifications() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Notifications").child(user.getUid());
        myRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(NotificationActivity.this, "Clear All", Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void readNotifications() {
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Notifications")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {

                    Notifications notifications=dataSnapshot.getValue(Notifications.class);

                    if(!notifications.getUserId().equals(firebaseUser.getUid())){
                        list.add(notifications);
                    }

                }
                Collections.reverse(list);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("checkNotification").child(firebaseUser.getUid());
        reference.removeValue();
        try {
            note.setImageResource(R.drawable.ic_notifications);
            note2.setImageResource(R.drawable.ic_notifications);
            note3.setImageResource(R.drawable.ic_notifications);
            note4.setImageResource(R.drawable.ic_notifications);
            note5.setImageResource(R.drawable.ic_notifications);
        }catch (Exception e){
        }

    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();

    }

}