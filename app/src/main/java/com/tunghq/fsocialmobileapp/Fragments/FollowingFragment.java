package com.tunghq.fsocialmobileapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.Adapter.PostAdapter;
import com.tunghq.fsocialmobileapp.Models.Posts;
import com.tunghq.fsocialmobileapp.NotificationActivity;
import com.tunghq.fsocialmobileapp.OthersProfileActivity;
import com.tunghq.fsocialmobileapp.R;
import com.tunghq.fsocialmobileapp.SearchUsersActivity;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class FollowingFragment extends Fragment {


    RecyclerView recyclerView;
    List<Posts> postsList;
    PostAdapter adapter;

    CircleImageView profile;
    ImageView search;
    public static ImageView note;
    TextView no;
    Button discover;


    FirebaseAuth auth;
    FirebaseUser user;

    DatabaseReference reference;

    List<String> followingList;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_following, container, false);



        profile = view.findViewById(R.id.profile_image);
        search = view.findViewById(R.id.search);
        no = view.findViewById(R.id.no);
        discover = view.findViewById(R.id.discover);
        note = view.findViewById(R.id.note);

        recyclerView=view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        postsList=new ArrayList<>();
        adapter=new PostAdapter(getActivity(),postsList);

        checkFollowing();
        checkNotifications();

        recyclerView.setAdapter(adapter);

        clicks();
        //get data
        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               if (snapshot.exists())
               {
                   try{
                       String p = snapshot.child("profileImg").getValue().toString();
                       Picasso.get().load(p).placeholder(R.drawable.profile_image).into(profile);
                   }catch (Exception e){
                   }
               }else
               {

               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }


    private void clicks()
    {
        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchUsersActivity.class));
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), OthersProfileActivity.class);
                intent.putExtra("uid",user.getUid());
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
        note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seenNotifications();
                note.setImageResource(R.drawable.ic_notifications);
                Intent intent=new Intent(getActivity(), NotificationActivity.class);
                startActivity(intent);
            }
        });


    }
    private void checkNotifications(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("checkNotification")
                .child(firebaseUser.getUid());
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // If any child is added to the database reference
                note.setImageResource(R.drawable.ic_baseline_notifications_active);
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
    private void checkFollowing()
    {
        followingList=new ArrayList<>();
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(user.getUid()).child("following");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        followingList.add(dataSnapshot.getKey());
                    }
                    getPosts();

                    no.setVisibility(View.GONE);
                    discover.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }else
                {
                    no.setVisibility(View.VISIBLE);
                    discover.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void getPosts()
    {
        DatabaseReference postRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        //Query query=postRef.orderByChild("counterPost");
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Posts posts=dataSnapshot.getValue(Posts.class);

                    for (String id : followingList)
                    {
                        if (posts.getUserId().equals(id))
                        {

                            postsList.add(posts);
                        }
                    }
                }
                Collections.reverse(postsList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
    }
}