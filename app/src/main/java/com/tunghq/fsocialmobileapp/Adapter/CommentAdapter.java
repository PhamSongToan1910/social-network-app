package com.tunghq.fsocialmobileapp.Adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import com.tunghq.fsocialmobileapp.CommentActivity;
import com.tunghq.fsocialmobileapp.Models.Comment;
import com.tunghq.fsocialmobileapp.Models.User;
import com.tunghq.fsocialmobileapp.OthersProfileActivity;
import com.tunghq.fsocialmobileapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    Context context;
    List<Comment> commentList;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ProgressDialog pd;


    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);

        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Comment comment = commentList.get(position);

        holder.comment.setText(comment.getComment());
        holder.time.setText(comment.getTime());
        getUserInfo(holder.profile, holder.username, comment.getUserId());


        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OthersProfileActivity.class);
                intent.putExtra("uid", comment.getUserId());
                context.startActivity(intent);
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OthersProfileActivity.class);
                intent.putExtra("uid", comment.getUserId());
                context.startActivity(intent);
            }
        });
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.getUid().equals(comment.getUserId())) {//delete own comment

                    showOptionsCmtDialog(comment.getCommentId());

                } else if (user.getUid().equals(CommentActivity.publisher)) {//own post can delete all comment

                    showOptionsCmtDialogNoEdit(comment.getCommentId());

                }
            }
        });
    }

    private void showOptionsCmtDialogNoEdit(String commentId) {
        pd = new ProgressDialog(context);

        String options[] = {"Delete Comment"};
        //alerDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //handle dialog item clicks
                if (which == 0) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Comments").child(CommentActivity.postid);
                    databaseReference.child(commentId).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Unable to delete", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showOptionsCmtDialog(String commentId) {
        pd = new ProgressDialog(context);

        String options[] = {"Edit Comment", "Delete Comment"};
        //alerDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Action");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //handle dialog item clicks
                if (which == 0) {
                    editCommentDialog(commentId);
                } else if (which == 1) {

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Comments").child(CommentActivity.postid);
                    databaseReference.child(commentId).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Unable to delete", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void editCommentDialog(String commentId) {

        //custom dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Edit Comment ");
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(linearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        //add edit text
        EditText editTextComment = new EditText(context);
        editTextComment.setHint("Enter comment");//hint e.g. edit name or edit phone
        linearLayout.addView(editTextComment);

        builder.setView(linearLayout);

        //add button
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //input
                String value = editTextComment.getText().toString().trim();

                if (!TextUtils.isEmpty(value)) {
                    pd.show();

                    HashMap<String, Object> resultUpdate = new HashMap<>();
                    resultUpdate.put("comment", value);


                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(CommentActivity.postid);

                    reference.child(commentId)
                            .updateChildren(resultUpdate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //update, dismiss progress
                                    pd.dismiss();
                                    Toast.makeText(context, "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                } else {
                    Toast.makeText(context, "Please enter comment", Toast.LENGTH_SHORT).show();

                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();


    }


    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView comment, username, time;
        CircleImageView profile;
        ImageView options;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            comment = itemView.findViewById(R.id.comment);
            username = itemView.findViewById(R.id.username);
            profile = itemView.findViewById(R.id.profile_image);
            time = itemView.findViewById(R.id.date);
            options = itemView.findViewById(R.id.optionsComment);


        }
    }

    private void getUserInfo(final ImageView imageView, final TextView username, final String publisher) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(publisher);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User data = snapshot.getValue(User.class);

                username.setText(data.getUserName());
                Picasso.get().load(data.getProfileImg()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
