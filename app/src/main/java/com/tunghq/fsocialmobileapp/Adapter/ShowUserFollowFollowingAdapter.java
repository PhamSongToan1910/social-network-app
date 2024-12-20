package com.tunghq.fsocialmobileapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.Models.User;
import com.tunghq.fsocialmobileapp.OthersProfileActivity;
import com.tunghq.fsocialmobileapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowUserFollowFollowingAdapter extends RecyclerView.Adapter<ShowUserFollowFollowingAdapter.ViewHolder> {

    Context context;
    List<User> dataList;
    FirebaseUser user;


    public ShowUserFollowFollowingAdapter(Context context, List<User> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.show_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        User data=dataList.get(position);
        user= FirebaseAuth.getInstance().getCurrentUser();
        holder.username.setText(data.getUserName());
        holder.memer.setText(data.getFeaturedName());
        Picasso.get().load(data.getProfileImg()).into(holder.profile);


        //set display all button
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference().child("checkSentFollow")
                .child(user.getUid());
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(dataList.get(position).getUserId()).exists()) {
                    holder.btn_follow.setVisibility(View.GONE);
                    holder.btn_sent.setVisibility(View.VISIBLE);
                    holder.btn_following.setVisibility(View.GONE);
                } else if (data.getUserId().equals(user.getUid())) {
                    holder.btn_follow.setVisibility(View.GONE);
                    holder.btn_sent.setVisibility(View.GONE);
                    holder.btn_following.setVisibility(View.GONE);
                }else{
                    isFollowing(dataList.get(position).getUserId(), holder.btn_follow, holder.btn_following);
                    holder.btn_sent.setVisibility(View.GONE);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //isFollowing(data.getUserId(),holder.btn_follow,holder.btn_following);


        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btn_follow.getText().toString().equals("Follow")) {
                    HashMap map = new HashMap();
                    map.put("checkSentFollow",true);
                    FirebaseDatabase.getInstance().getReference().child("checkSentFollow")
                            .child(user.getUid())
                            .child(dataList.get(position).getUserId()).setValue(map);
                    addNotifications(dataList.get(position).getUserId());
                    Toast.makeText(context, "Sent a follow request", Toast.LENGTH_SHORT).show();
//                    FirebaseDatabase.getInstance().getReference().child("Follow")
//                            .child(user.getUid())
//                            .child("following").child(dataList.get(position).getUserId()).setValue(true);
//
//                    FirebaseDatabase.getInstance().getReference().child("Follow")
//                            .child(dataList.get(position).getUserId())
//                            .child("followers").child(user.getUid()).setValue(true);

                }
            }
        });
        holder.btn_following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btn_following.getText().toString().equals("Following")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(user.getUid())
                            .child("following").child(dataList.get(position).getUserId()).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(dataList.get(position).getUserId())
                            .child("followers").child(user.getUid()).removeValue();
                }
            }
        });


        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, OthersProfileActivity.class);
                intent.putExtra("uid",data.getUserId());
                context.startActivity(intent);
            }
        });





    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile;
        TextView username,memer;
        Button btn_follow,btn_following, btn_sent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            memer = itemView.findViewById(R.id.memer);
            btn_follow = itemView.findViewById(R.id.btn_follow);
            btn_following = itemView.findViewById(R.id.btn_following);
            btn_sent = itemView.findViewById(R.id.btn_sent);

        }
    }
    private void addNotifications(String id)
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
        map.put("textNotifications","want to follow you");
        map.put("postId","");
        map.put("check","user");
        map.put("status", "NoSeen");
        map2.put("seen", true);

        reference.child(notificationId).setValue(map);
        reference2.setValue(map2);


    }
    private void isFollowing(final String userid, final Button follow, final Button following) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(user.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userid).exists()) {

                    follow.setVisibility(View.GONE);
                    following.setVisibility(View.VISIBLE);
                } else {
                    follow.setVisibility(View.VISIBLE);
                    following.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
