package com.example.petcam.chatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petcam.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatMemberAdapter extends RecyclerView.Adapter<ChatMemberAdapter.ViewHolder> {

    private List<ChatMemberItem> mList;
    private Context mContext;
    private String userID;

    public ChatMemberAdapter(List<ChatMemberItem> mList, Context mContext, String userID) {
        this.mList = mList;
        this.mContext = mContext;
        this.userID = userID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_chat_users, parent, false);

        // 뷰를 가져온다
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        ChatMemberItem item = mList.get(position);
        // 유저 id 가 일치하면 '나' 표시해 주기
        if(userID.equals(item.getUserId())) {
            holder.iv_me.setVisibility(View.VISIBLE);
        }
        // 유저 닉네임
        holder.tv_name.setText(item.getUserName());

        // 프로필 이미지가 등록되어 있는지 여부 확인
        if (item.getUserProfileImage() != null) {
            // 현재 유저 프로필 이미지 보여주기
            Glide.with(mContext).load(item.getUserProfileImage()).centerCrop().into(holder.civ_profileImage);

        } else {
            // 프로필 이미지가 없다면 기본 이미지 보여주기
            Glide.with(mContext).load(R.drawable.ic_user).centerCrop().into(holder.civ_profileImage);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    // 리사이클러뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cv_user_item;
        public CircleImageView civ_profileImage;
        public TextView tv_name;
        public ImageView iv_me;

        public ViewHolder(View itemView) {
            super(itemView);
            this.civ_profileImage = itemView.findViewById(R.id.civ_user_profile);
            this.tv_name = itemView.findViewById(R.id.tv_user_name);
            this.cv_user_item = itemView.findViewById(R.id.cv_user_item);
            this.iv_me = itemView.findViewById(R.id.iv_me);

        }
    }
}
