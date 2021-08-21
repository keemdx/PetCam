package com.example.petcam.ui.chatting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petcam.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChattingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChattingItem> mList;
    private final Context mContext;
    private final String userID;

    // 채팅방 상황 (처음 생긴 방인지, 기존 존재하던 방인지 등)
    private static final String START_ROOM = "START_ROOM";
    private static final String CHATTING = "CHATTING";
    private static final String EXIT_ROOM = "EXIT_ROOM"; // 멤버 나간 경우 ("님이 퇴장하셨습니다",  db 삭제)
    private static final String DATE = "DATE"; // 접속 (날짜, 시간)


    public static final int START_CENTER = 0;
    public static final int MINE_RIGHT = 1;
    public static final int OTHERS_LEFT = 2;
    public static final int EXIT_CENTER = 3;
    public static final int DATE_CENTER = 4;

    public ChattingAdapter(List<ChattingItem> mList, Context mContext, String userID) {
        this.mList = mList;
        this.mContext = mContext;
        this.userID = userID;
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position).getStatus().equals(START_ROOM)) { // 최초 접속 ("님이 입장하셨습니다", 날짜, 시간)
            return START_CENTER;
        } else if (mList.get(position).getStatus().equals(EXIT_ROOM)) {
            return EXIT_CENTER;
        } else if (mList.get(position).getStatus().equals(DATE)) {
            return DATE_CENTER;
        } else if (mList.get(position).getMessage() != null && mList.get(position).getStatus().equals(CHATTING) && userID.equals(mList.get(position).getUserID())) { // 내 채팅
            return MINE_RIGHT;
        } else { // 다른 사람의 채팅 메시지
            return OTHERS_LEFT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == START_CENTER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_center, parent, false);
            return new CenterViewHolder(view);
        } else if (viewType == EXIT_CENTER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_center, parent, false);
            return new CenterViewHolder(view);
        } else if (viewType == DATE_CENTER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_center, parent, false);
            return new CenterViewHolder(view);
        } else if (viewType == MINE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right, parent, false);
            return new RightViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left, parent, false);
            return new LeftViewHolder(view);
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        ChattingItem item = mList.get(position);

        switch (holder.getItemViewType()) {
            case START_CENTER:
                ((CenterViewHolder) holder).tv_chatting_notice.setText(item.getMessage());
                break;
            case EXIT_CENTER:
                ((CenterViewHolder) holder).tv_chatting_notice.setText(item.getMessage());
                break;
            case DATE_CENTER:
                ((CenterViewHolder) holder).tv_chatting_notice.setText(item.getTime());
                break;
            case MINE_RIGHT:
                ((RightViewHolder) holder).tv_message_mine.setText(item.getMessage());
                ((RightViewHolder) holder).tv_time_mine.setText(item.getTime());
                break;
            case OTHERS_LEFT:
                // 프로필 이미지가 등록되어 있는지 여부 확인
                if (!item.getUserPhoto().isEmpty()) {
                    // 현재 유저 프로필 이미지 보여주기
                    Glide.with(mContext).load(item.getUserPhoto()).centerCrop().into(((LeftViewHolder) holder).iv_profile_others);

                } else {
                    // 프로필 이미지가 없다면 기본 이미지 보여주기
                    Glide.with(mContext).load(R.drawable.ic_user).centerCrop().into(((LeftViewHolder) holder).iv_profile_others);
                }
                ((LeftViewHolder) holder).tv_message_others.setText(item.getMessage());
                ((LeftViewHolder) holder).tv_time_others.setText(item.getTime());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class CenterViewHolder extends RecyclerView.ViewHolder {
        TextView tv_chatting_notice;

        CenterViewHolder(View itemView) {
            super(itemView);
            tv_chatting_notice = itemView.findViewById(R.id.tv_chatting_notice);
        }
    }

    public class LeftViewHolder extends RecyclerView.ViewHolder {
        TextView tv_message_others, tv_time_others;
        ImageView iv_profile_others;

        LeftViewHolder(View itemView) {
            super(itemView);
            tv_time_others = itemView.findViewById(R.id.tv_time_others);
            tv_message_others = itemView.findViewById(R.id.tv_message_others);
            iv_profile_others = itemView.findViewById(R.id.iv_profile_others);
        }
    }

    public class RightViewHolder extends RecyclerView.ViewHolder {
        TextView tv_message_mine, tv_time_mine;

        RightViewHolder(View itemView) {
            super(itemView);
            tv_time_mine = itemView.findViewById(R.id.tv_time_mine);
            tv_message_mine = itemView.findViewById(R.id.tv_message_mine);
        }
    }


}
