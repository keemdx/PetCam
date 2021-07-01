package com.example.petcam.profile.notice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcam.R;
import com.example.petcam.widget.TimeString;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import static com.example.petcam.function.App.NOTICE_CONTENTS;
import static com.example.petcam.function.App.NOTICE_CREATE_AT;
import static com.example.petcam.function.App.NOTICE_ID;
import static com.example.petcam.function.App.NOTICE_PIN;
import static com.example.petcam.function.App.NOTICE_TITLE;

public class FixTopNoticeAdapter extends RecyclerView.Adapter<FixTopNoticeAdapter.ViewHolder> {

    private List<FixTopNoticeItem> mList;
    private Context mContext;
    private Activity mActivity;

    public FixTopNoticeAdapter(List<FixTopNoticeItem> mList, Context mContext, Activity mActivity) {
        this.mList = mList;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_fix_top_notice, parent, false);

        // 뷰를 가져온다
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        FixTopNoticeItem item = mList.get(position);

        // 글 제목
        holder.tv_title.setText(item.getNotice_title());

        // 글 작성 시간 (create at)
        TimeString timeString = new TimeString();
        long getTime = Long.parseLong(item.getCreate_at());
        holder.tv_time.setText(TimeString.formatTimeString(getTime));

        int commentCount = item.getComment_count();
        // 코멘트가 없을 경우 0으로 표시한다.
        if (commentCount  > 0) {
            holder.tv_comment_count.setText(String.valueOf(commentCount));
        } else {
            holder.tv_comment_count.setText("0");
        }

        holder.cv_fix_top_notice_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NoticeDetailActivity.class);
                intent.putExtra(NOTICE_ID, mList.get(position).getNotice_id());
                mContext.startActivity(intent);
                mActivity.overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    // 리사이클러뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cv_fix_top_notice_item;
        public TextView tv_title, tv_time, tv_comment_count;

        public ViewHolder(View itemView) {
            super(itemView);
            this.cv_fix_top_notice_item = itemView.findViewById(R.id.cv_fix_top_notice_item);
            this.tv_title = itemView.findViewById(R.id.tv_title);
            this.tv_time = itemView.findViewById(R.id.tv_createAt);
            this.tv_comment_count = itemView.findViewById(R.id.tv_comment_count);
        }
    }
}