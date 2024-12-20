package com.tunghq.fsocialmobileapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.tunghq.fsocialmobileapp.GroupChatActivity;
import com.tunghq.fsocialmobileapp.Models.GroupList;
import com.tunghq.fsocialmobileapp.R;

import java.util.ArrayList;

public class GroupListAdapter extends  RecyclerView.Adapter<GroupListAdapter.HolderGroupsChat>{


    Context context;
    ArrayList<GroupList> groupChatList;

    public GroupListAdapter(Context context, ArrayList<GroupList> groupChatList) {
        this.context = context;
        this.groupChatList = groupChatList;
    }

    @NonNull
    @Override
    public HolderGroupsChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat, parent, false);

        return new HolderGroupsChat(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupsChat holder, @SuppressLint("RecyclerView") int position) {
        //get data
        GroupList model = groupChatList.get(position);
        String groupId = model.getGroupId();
        String creatorId = model.getCreatorId();
        String time = model.getTimeStamp();
        String groupImg = model.getGroupImg();
        String groupTitle = model.getGroupTitle();

        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");

        //load last message
        loadLastMessage(model, holder);

        //set data
        holder.groupTitleTv.setText(groupTitle);
        try {
            Picasso.get().load(groupImg).placeholder(R.drawable.baseline_groups_100).into(holder.groupImgIv);
        }catch (Exception e){
            holder.groupImgIv.setImageResource(R.drawable.baseline_groups_100);

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId", groupChatList.get(position).getGroupId());
                intent.putExtra("creatorId", groupChatList.get(position).getCreatorId());

                context.startActivity(intent);
            }
        });

    }

    private void loadLastMessage(GroupList model, HolderGroupsChat holder) {
        //get last message from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups Chat");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            String message = ""+ds.child("message").getValue();
                            String timeStamp = ""+ds.child("timeStamp").getValue();
                            String sender = ""+ds.child("sender").getValue();
                            String messageType = ""+ds.child("type").getValue();

                            if(messageType.equals("image")){
                                holder.messageTv.setText("Sent Photo");
                            }else{
                                holder.messageTv.setText(message);

                            }
                            holder.timeTv.setText(timeStamp);

                            //get info of sender of last message
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("userId").equalTo(sender).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        String name = ""+ds.child("userName").getValue();
                                        holder.nameTv.setText(name);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatList.size();
    }


    //view holder class
    class HolderGroupsChat extends RecyclerView.ViewHolder{
        //view
        ImageView groupImgIv;
        TextView groupTitleTv, nameTv, messageTv, timeTv;


        public HolderGroupsChat(@NonNull View itemView) {
            super(itemView);
            groupImgIv = itemView.findViewById(R.id.rowGroupImgIv);
            groupTitleTv = itemView.findViewById(R.id.rowGroupTitleTv);
            nameTv = itemView.findViewById(R.id.rowGroupNameTv);
            messageTv = itemView.findViewById(R.id.rowGroupMessageTv);
            timeTv = itemView.findViewById(R.id.rowGroupTimeTv);
        }
    }
}
