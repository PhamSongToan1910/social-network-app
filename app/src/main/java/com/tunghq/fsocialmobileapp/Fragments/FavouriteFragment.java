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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.Adapter.PostsListAdapter;
import com.tunghq.fsocialmobileapp.Models.Posts;
import com.tunghq.fsocialmobileapp.NotificationActivity;
import com.tunghq.fsocialmobileapp.R;
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
import java.util.List;



public class FavouriteFragment extends Fragment {

    private List<String> mySaves;
    RecyclerView recyclerView_save;
    List<Posts> mPost_save;
    PostsListAdapter adapter;

    public static ImageView note3;
    ImageView search;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_favourite, container, false);

        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        reference=FirebaseDatabase.getInstance().getReference().child("Users");


        recyclerView_save=view.findViewById ( R.id.recyclerView );
        note3 = view.findViewById(R.id.note3);
        search = view.findViewById(R.id.search);

        recyclerView_save.setHasFixedSize ( true );
        recyclerView_save.setLayoutManager(new GridLayoutManager(getContext(),3));

        mPost_save=new ArrayList<>(  );
        adapter=new PostsListAdapter( getContext (),mPost_save );
        recyclerView_save.setAdapter ( adapter );

        checkNotifications();
        Saved();

        note3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seenNotifications();
                note3.setImageResource(R.drawable.ic_notifications);
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

        return view;
    }

    private void Saved(){
        mySaves=new ArrayList<> (  );
        DatabaseReference reference=FirebaseDatabase.getInstance ().getReference ().child ( "Favourites" )
                .child ( user.getUid () );

        reference.addValueEventListener ( new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren ()){
                    mySaves.add ( snapshot.getKey () );
                }
                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }

    private void readSaves() {
        DatabaseReference reference= FirebaseDatabase.getInstance ().getReference ().child ( "Posts" );

        reference.addValueEventListener ( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPost_save.clear ();
                for (DataSnapshot snapshot : dataSnapshot.getChildren ()){
                    Posts post=snapshot.getValue (Posts.class);

                    for (String id : mySaves){
                        if (post.getPostId().equals(id)){
                            mPost_save.add ( post );
                        }
                    }
                }
                adapter.notifyDataSetChanged ();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }
    private void checkNotifications(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("checkNotification")
                .child(firebaseUser.getUid());
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // If any child is added to the database reference
                note3.setImageResource(R.drawable.ic_baseline_notifications_active);
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



}