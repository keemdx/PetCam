package com.example.petcam.ui.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.ui.profile.ChannelActivity;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.petcam.function.App.CHANNEL_ID;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.USER_UID;


public class ChartChannelsAdapter extends RecyclerView.Adapter<ChartChannelsAdapter.ViewHolder> {

    private final List<ChartChannelsItem> mList;
    private final Context mContext;
    private final Activity mActivity;
    private SharedPreferences pref;


    public ChartChannelsAdapter(List<ChartChannelsItem> mList, Context mContext, Activity mActivity) {
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_chart_channels, parent, false);

        // 뷰를 가져온다
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        ChartChannelsItem item = mList.get(position);

        holder.tv_rank.setText(String.valueOf(item.getNo()));
        if (item.getNo() > 3) {
            holder.tv_rank.setTextColor(Color.GRAY);
        }

        // 유저 닉네임
        holder.tv_name.setText(item.getUserName());

        if (item.getCnt() <= 1) {
            holder.tv_fans.setText(item.getCnt() + " fan");
        } else {
            holder.tv_fans.setText(item.getCnt() + " fans");
        }

        // 프로필 이미지가 등록되어 있는지 여부 확인
        if (item.getUserProfileImage() == null) {
            // 프로필 이미지가 없다면 기본 이미지 보여주기
            Glide.with(holder.civ_profileImage)
                    .load(R.drawable.ic_user)
                    .centerCrop()
                    .into(holder.civ_profileImage);

        } else {
            // 현재 유저 프로필 이미지 보여주기
            Glide.with(holder.civ_profileImage)
                    .load(item.getUserProfileImage())
                    .centerCrop()
                    .into(holder.civ_profileImage);
        }

        holder.cv_user_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 저장된 유저 정보 가져오기
                pref = mActivity.getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
                String userID = pref.getString(USER_UID, ""); // 유저 프로필 이미지

                if (!item.getUserId().equals(userID)) {
                    Intent intent = new Intent(mContext, ChannelActivity.class);
                    intent.putExtra(CHANNEL_ID, item.getUserId());
                    mContext.startActivity(intent);
                    mActivity.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    // 리사이클러뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cv_user_item;
        public CircleImageView civ_profileImage;
        public TextView tv_name, tv_rank, tv_fans;

        public ViewHolder(View itemView) {
            super(itemView);
            this.civ_profileImage = itemView.findViewById(R.id.civ_user_profile);
            this.tv_name = itemView.findViewById(R.id.tv_user_name);
            this.tv_rank = itemView.findViewById(R.id.tv_rank);
            this.tv_fans = itemView.findViewById(R.id.tv_fans);
            this.cv_user_item = itemView.findViewById(R.id.cv_user_item);


        }
    }
}
