package com.tunghq.fsocialmobileapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tunghq.fsocialmobileapp.Models.Posts;
import com.tunghq.fsocialmobileapp.PostDetailsActivity;
import com.tunghq.fsocialmobileapp.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;


public class PostsListAdapter extends RecyclerView.Adapter<PostsListAdapter.ViewHolder> {
    Context context;
    List<Posts> postsList;

    public PostsListAdapter(Context context, List<Posts> postsList) {
        this.context = context;
        this.postsList = postsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.posts_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Posts posts=postsList.get(position);

        if(postsList.get(position).getType().equals("image")){
            Picasso.get().load(posts.getPostUrl()).into(holder.postImage);
            holder.postVideo.setVisibility(View.GONE);
        }
        else if (postsList.get(position).getType().equals("video")) {
            try {

                SimpleExoPlayer simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();
                holder.postVideo.setPlayer(simpleExoPlayer);
                MediaItem mediaItem = MediaItem.fromUri(posts.getPostUrl());
                simpleExoPlayer.addMediaItems(Collections.singletonList(mediaItem));
                simpleExoPlayer.prepare();
                simpleExoPlayer.setPlayWhenReady(false);


            } catch (Exception e) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }
        } else if (postsList.get(position).getType().equals("status")) {

            holder.postVideo.setVisibility(View.GONE);
            holder.postImage.setVisibility(View.GONE);

        }


        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PostDetailsActivity.class);
                intent.putExtra("postId",posts.getPostId());
                context.startActivity(intent);

            }
        });
        holder.postVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, PostDetailsActivity.class);
                intent.putExtra("postId",posts.getPostId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;
        PlayerView postVideo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postImage=itemView.findViewById(R.id.my_photos);
            postVideo=itemView.findViewById(R.id.my_videos);

        }
    }
}
