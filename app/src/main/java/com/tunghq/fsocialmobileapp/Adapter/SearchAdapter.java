package com.tunghq.fsocialmobileapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private final Context context;
    List<User> dataList;


    public SearchAdapter(Context context, List<User> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    FirebaseUser user;
    DatabaseReference reference;


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_user, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        //init
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        user = FirebaseAuth.getInstance().getCurrentUser();
        User data = dataList.get(position);

        holder.username.setText(data.getUserName());
        holder.memer.setText(data.getFeaturedName());
        Glide.with(context).load(data.getProfileImg()).centerCrop().placeholder(R.drawable.profile_image).into(holder.profile);

        //image status
        if(data.getOnlineStatus().equals("online")){
            //online
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);

        }else{
            //offline
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);

        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send id
                Intent intent = new Intent(context, OthersProfileActivity.class);
                intent.putExtra("uid", data.getUserId());
                context.startActivity(intent);


            }
        });


        //change status button
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference().child("checkSentFollow")
                .child(user.getUid());
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(data.getUserId()).exists()) {
                    holder.btn_follow.setVisibility(View.GONE);
                    holder.btn_sent.setVisibility(View.VISIBLE);
                    holder.btn_following.setVisibility(View.GONE);
                } else {
                    isFollowing(data.getUserId(), holder.btn_follow, holder.btn_following);
                    holder.btn_sent.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap map = new HashMap();
                map.put("checkSentFollow",true);
                FirebaseDatabase.getInstance().getReference().child("checkSentFollow")
                        .child(user.getUid())
                        .child(data.getUserId()).setValue(map);
                addNotifications(data.getUserId());
                Toast.makeText(context, "Sent a follow request", Toast.LENGTH_SHORT).show();
            }
        });
        holder.btn_following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btn_following.getText().toString().equals("Following")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(user.getUid())
                            .child("following").child(data.getUserId()).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(data.getUserId())
                            .child("followers").child(user.getUid()).removeValue();
                    holder.btn_sent.setVisibility(View.GONE);

                }
            }
        });


    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile;
        ImageView onlineStatusIv;
        TextView username, memer;
        Button btn_follow, btn_following, btn_sent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            memer = itemView.findViewById(R.id.memer);
            btn_follow = itemView.findViewById(R.id.btn_follow);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            btn_following = itemView.findViewById(R.id.btn_following);
            btn_sent = itemView.findViewById(R.id.btn_sent);


        }
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
    private void addNotifications(String userid)
    {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child("Notifications").child(userid);
        DatabaseReference reference2=FirebaseDatabase.getInstance().getReference().child("checkNotification").child(userid);

        String notificationId = reference.push().getKey();

        HashMap<String,Object> map=new HashMap<>();
        HashMap<String,Object> map2=new HashMap<>();

        map.put("notificationId", notificationId);
        map.put("affectedPersonId", userid);
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
}
