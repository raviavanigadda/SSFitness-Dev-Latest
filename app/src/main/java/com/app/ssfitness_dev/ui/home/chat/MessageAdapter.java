package com.app.ssfitness_dev.ui.home.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.app.ssfitness_dev.R;
import com.app.ssfitness_dev.data.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private boolean isGroup = false;
    private Context context;
    private String chatUserId;

    public MessageAdapter(List<Messages> mMessageList,Context context,String chatUserId) {
        this.mMessageList = mMessageList;
        this.context = context;
        this.chatUserId = chatUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        /*------ Hiding Previous Date and Message Received User name for group and bg For Keeping data Updated when new data is loaded ------*/
        holder.received_from_text_view.setVisibility(View.GONE);
        holder.received_from_bg_view.setVisibility(View.GONE);
        holder.messages_date_layout.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();

        Timestamp ts = new Timestamp(c.getTime());
        Date message_dt = ts;
        String date = String.valueOf(message_dt);

        Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        String current_date = "";
        if (Character.charCount(mm) == 1) {
            current_date = yy + "-0" + (mm + 1) + "-" + dd;
        } else {
            current_date = yy + "-" + (mm + 1) + "-" + dd;
        }

        /*--------------- Displaying Date of Chat and the first username of received message is before than sent------------*/
        if (position - 1 != -1) {
            Messages messages = mMessageList.get(position - 1);
            Timestamp ts1 = new Timestamp(messages.getTime());
            Date prev_message_dt = ts1;
            String prevDate = String.valueOf(prev_message_dt).split(" ")[0];
            if (!date.split(" ")[0].equals(prevDate)) {
                holder.messages_date_layout.setVisibility(View.VISIBLE);
                if (date.split(" ")[0].equals(current_date)) {
                    holder.messages_date_txt_view.setText("TODAY");
                    if (isGroup) {
                        if (!from_user.equals(current_user_id)) {
                            holder.received_from_text_view.setVisibility(View.VISIBLE);
                            holder.received_from_bg_view.setVisibility(View.VISIBLE);
                            holder.received_from_text_view.setText(c.getUserName());
                        }
                    }
                } else {
                    holder.messages_date_txt_view.setText(date.split(" ")[0]);
                    if (isGroup) {
                        if (!from_user.equals(current_user_id)) {
                            holder.received_from_text_view.setVisibility(View.VISIBLE);
                            holder.received_from_bg_view.setVisibility(View.VISIBLE);
                            holder.received_from_text_view.setText(c.getUserName());
                        }
                    }
                }
            }
        }
        else {
            holder.messages_date_layout.setVisibility(View.VISIBLE);
            if (date.split(" ")[0].equals(current_date)) {
                holder.messages_date_txt_view.setText("TODAY");
                if (isGroup) {
                    if (!from_user.equals(current_user_id)) {
                        holder.received_from_text_view.setVisibility(View.VISIBLE);
                        holder.received_from_bg_view.setVisibility(View.VISIBLE);
                        holder.received_from_text_view.setText(c.getUserName());
                    }
                }
            } else {
                holder.messages_date_txt_view.setText(date.split(" ")[0]);
                if (isGroup) {
                    if (!from_user.equals(current_user_id)) {
                        holder.received_from_text_view.setVisibility(View.VISIBLE);
                        holder.received_from_bg_view.setVisibility(View.VISIBLE);
                        holder.received_from_text_view.setText(c.getUserName());
                    }
                }
            }
        }
        /*------------ Displaying Date Of Chat Finishes ---------------*/


        /*---------- Displaying message based on sender and receiver -----------*/
        if (from_user.equals(current_user_id)) {
            holder.sent_msg_text_view.setText(c.getMessage());
            holder.sender_message_cv.setVisibility(View.VISIBLE);
            holder.sender_sent_time.setVisibility(View.VISIBLE);
            holder.receiver_message_cv.setVisibility(View.GONE);
            holder.receiver_sent_time.setVisibility(View.GONE);
            holder.sender_sent_time.setText(date.split(" ")[1].substring(0, 5));

        } else {

            if (isGroup) {
                String current_user_name = c.getUserName();
                if (position - 1 != -1) {
                    Messages message = mMessageList.get(position - 1);
                    if (current_user_name.equals(message.getUserName())) {

                    } else {
                        holder.received_from_text_view.setText(c.getUserName());
                        holder.received_from_text_view.setVisibility(View.VISIBLE);
                        holder.received_from_bg_view.setVisibility(View.VISIBLE);
                    }
                } else {
                    holder.received_from_text_view.setText(c.getUserName());
                    holder.received_from_text_view.setVisibility(View.VISIBLE);
                    holder.received_from_bg_view.setVisibility(View.VISIBLE);
                }


            }

            holder.received_msg_text_view.setText(c.getMessage());
            holder.receiver_message_cv.setVisibility(View.VISIBLE);
            holder.receiver_sent_time.setVisibility(View.VISIBLE);
            holder.sender_message_cv.setVisibility(View.GONE);
            holder.sender_sent_time.setVisibility(View.GONE);
            holder.receiver_sent_time.setText(date.split(" ")[1].substring(0, 5));
        }
        /*---------- Displaying message based on sender and receiver Finishes-----------*/

        /*------------ Set On Clicks Starts------------*/
        holder.sender_message_cv.setOnLongClickListener(view -> {
            showDeleteDialog(view,c);
            return false;
        });

        holder.receiver_message_cv.setOnLongClickListener(view -> {
            showDeleteDialog(view,c);
            return false;
        });

    }

    private void showDeleteDialog(View view,Messages message){
        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(view.getContext())
                .setView(R.layout.delete_msg_dialog);
        final AlertDialog Delete_Msg_Dialog = dialog_builder.create();
        Delete_Msg_Dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Delete_Msg_Dialog.show();
        final MaterialButton dialog_cancel_msg_btn = Delete_Msg_Dialog.findViewById(R.id.dialog_cancel_msg_btn);
        final MaterialButton dialog_delete_msg_btn = Delete_Msg_Dialog.findViewById(R.id.dialog_delete_msg_btn);

        /*------------- Creating DB References ---------------*/
        DatabaseReference mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages");
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        dialog_cancel_msg_btn.setOnClickListener(view1 -> Delete_Msg_Dialog.dismiss());

        dialog_delete_msg_btn.setOnClickListener(view1 -> {
            dialog_delete_msg_btn.setEnabled(false);
            dialog_cancel_msg_btn.setEnabled(false);
            Delete_Msg_Dialog.setCanceledOnTouchOutside(false);
            mMessageDatabase.child(mCurrentUser.getUid()).child(chatUserId).child(message.getMessageId()).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        mMessageList.remove(message);
                        notifyDataSetChanged();
                        dialog_delete_msg_btn.setEnabled(true);
                        dialog_cancel_msg_btn.setEnabled(true);
                        Delete_Msg_Dialog.setCanceledOnTouchOutside(true);
                        Delete_Msg_Dialog.dismiss();
                        Toast.makeText(context,"Message Deleted!",Toast.LENGTH_LONG).show();

                    }
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public void checkForGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView sent_msg_text_view, received_msg_text_view, received_from_text_view, receiver_sent_time, sender_sent_time, messages_date_txt_view;
        public MaterialCardView sender_message_cv, receiver_message_cv;
        public ConstraintLayout messages_date_layout;
        public View received_from_bg_view;
        //public CircleImageView profileImage;


        public MessageViewHolder(View view) {
            super(view);

            view.setTag(this);
            //messageText = view.findViewById(R.id.message_text_layout);
            //profileImage = view.findViewById(R.id.message_profile_layout);
            sent_msg_text_view = view.findViewById(R.id.sent_msg_text_view);
            sender_message_cv = view.findViewById(R.id.sender_message_cv);
            received_msg_text_view = view.findViewById(R.id.received_msg_text_view);
            receiver_message_cv = view.findViewById(R.id.receiver_message_cv);
            received_from_bg_view = view.findViewById(R.id.received_from_bg_view);
            received_from_text_view = view.findViewById(R.id.received_from_text_view);
            sender_sent_time = view.findViewById(R.id.sender_sent_time);
            receiver_sent_time = view.findViewById(R.id.receiver_sent_time);
            messages_date_layout = view.findViewById(R.id.messages_date_layout);
            messages_date_txt_view = view.findViewById(R.id.messages_date_txt_view);


        }

    }
}




