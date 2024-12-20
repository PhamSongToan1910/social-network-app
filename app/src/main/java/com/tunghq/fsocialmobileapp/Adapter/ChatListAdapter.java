package com.tunghq.fsocialmobileapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.ChatActivity;
import com.tunghq.fsocialmobileapp.Models.User;
import com.tunghq.fsocialmobileapp.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyHolder> {

    Context context;
    List<User> userList;
    private HashMap<String, String> lastMessageMap;

    public ChatListAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String hisUid = userList.get(position).getUserId();
        String userImage = userList.get(position).getProfileImg();
        String username = userList.get(position).getUserName();
        String lastMessage = lastMessageMap.get(hisUid);

        //set data
        holder.nameTv.setText(username);

        if(lastMessage==null || lastMessage.equals("default")){
            holder.lastMessTv.setVisibility(View.GONE);

        }else{
            holder.lastMessTv.setVisibility(View.VISIBLE);
            holder.lastMessTv.setText(lastMessage);
        }
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileivIv);
        }catch (Exception e){
            Picasso.get().load(R.drawable.profile_image).into(holder.profileivIv);
        }
        if(userList.get(position).getOnlineStatus().equals("online")){
            //online
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);

        }else{
            //offline
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);

        }

        //handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start chat activity with that user
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chatuid", hisUid);
                context.startActivity(intent);

            }
        });
    }

    public void setLastMessageMap(String userId, String lastMess){
        lastMessageMap.put(userId,lastMess);
    }
    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView profileivIv, onlineStatusIv ;
        TextView nameTv, lastMessTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileivIv = itemView.findViewById(R.id.profileivIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
