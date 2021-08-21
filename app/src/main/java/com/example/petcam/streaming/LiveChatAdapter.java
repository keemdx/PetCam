package com.example.petcam.streaming;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.streaming.LiveChatItem;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class LiveChatAdapter extends RecyclerView.Adapter<LiveChatAdapter.ViewHolder> {


    private final List<LiveChatItem> mList;
    private final Context mContext;


    public LiveChatAdapter(List<LiveChatItem> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_live_chat, parent, false);

        // 뷰를 가져온다
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        LiveChatItem item = mList.get(position);

        // 유저 닉네임
        holder.tv_name.setText(item.getUserName());

        // 메시지
        holder.tv_message.setText(item.getMessage());

        // 프로필 이미지가 등록되어 있는지 여부 확인
        if (item.getUserProfileImage() == null) {

            // 프로필 이미지가 없다면 기본 이미지 보여주기
            Glide.with(mContext).load(R.drawable.ic_user).centerCrop().into(holder.civ_profileImage);

        } else {

            // 현재 유저 프로필 이미지 보여주기
            Glide.with(mContext).load(item.getUserProfileImage()).centerCrop().into(holder.civ_profileImage);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    // 리사이클러뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView civ_profileImage;
        public TextView tv_name, tv_message;

        public ViewHolder(View itemView) {
            super(itemView);
            this.civ_profileImage = itemView.findViewById(R.id.civ_user_profile);
            this.tv_name = itemView.findViewById(R.id.tv_user_name);
            this.tv_message = itemView.findViewById(R.id.tv_message);
        }
    }
}
