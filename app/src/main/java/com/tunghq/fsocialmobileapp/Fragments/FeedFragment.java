package com.tunghq.fsocialmobileapp.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.Adapter.PostAdapter;
import com.tunghq.fsocialmobileapp.AddPostActivity;
import com.tunghq.fsocialmobileapp.Models.Posts;
import com.tunghq.fsocialmobileapp.NotificationActivity;
import com.tunghq.fsocialmobileapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tunghq.fsocialmobileapp.SearchUsersActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class FeedFragment extends Fragment {

    RecyclerView recyclerView;
    List<Posts> postsList;
    PostAdapter adapter;
    public static ImageView note2;
    ImageView search;
    FloatingActionButton floatingActionButton;

    FirebaseAuth auth;
    FirebaseUser user;

    DatabaseReference reference,postRef;



    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_feed, container, false);



        recyclerView=view.findViewById(R.id.recyclerView);
        floatingActionButton = view.findViewById(R.id.addPost);
        note2 = view.findViewById(R.id.note2);
        search = view.findViewById(R.id.search);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        postsList=new ArrayList<>();
        adapter=new PostAdapter(getActivity(),postsList);
        recyclerView.setAdapter(adapter);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddPostActivity.class));
            }
        });
        note2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seenNotifications();
                note2.setImageResource(R.drawable.ic_notifications);
                Intent intent=new Intent(getActivity(), NotificationActivity.class);
                startActivity(intent);
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), SearchUsersActivity.class);
                startActivity(intent);
            }
        });

        checkNotifications();
        getPosts();




        return view;
    }
    private void checkNotifications(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("checkNotification")
                .child(firebaseUser.getUid());
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // If any child is added to the database reference
                note2.setImageResource(R.drawable.ic_baseline_notifications_active);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // If any child is updated/changed to the database reference
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // If any child is removed to the database reference
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // If any child is moved to the database reference
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

    }
    private void seenNotifications(){
//        HashMap<String, Object> resultUpdate = new HashMap<>();
//        resultUpdate.put("seen", "yes");
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("checkNotification").child(firebaseUser.getUid());
        reference.removeValue();

    }
    private void getPosts()
    {
        //Query query= postRef.orderByChild("counterPost");
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Posts posts=dataSnapshot.getValue(Posts.class);
                    postsList.add(posts);
                }
                Collections.reverse(postsList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                //Toast.makeText(getContext(), "Error"+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}



