package com.example.petcam.profile.fanboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.petcam.R;
import com.example.petcam.widget.TimeString;

import java.util.List;

public class FanboardAdapter extends RecyclerView.Adapter<FanboardAdapter.ViewHolder> {

    private List<FanboardItem> mList;
    private Context mContext;

    public FanboardAdapter(List<FanboardItem> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_fanboard, parent, false);

        // 뷰를 가져온다
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        FanboardItem item = mList.get(position);

        // 팬보드 닉네임
        holder.tv_name.setText(item.getName());

        // 팬보드 텍스트
        holder.tv_text.setText(item.getText());

        // 글 작성 시간 (create at)
        TimeString timeString = new TimeString();
        long getTime = Long.parseLong(item.getTime());
        holder.tv_time.setText(TimeString.formatTimeString(getTime));

        // 코멘트 이미지
        holder.iv_comment.setImageResource(item.getCommentImage());

        // 코멘트가 없을 경우 0으로 표시한다.
        if(item.getCommentCount() == null) {
            holder.tv_comment_count.setText("0");
        } else {
            holder.tv_comment_count.setText(item.getCommentCount());
        }

        // 팬 이미지
        try {
            Glide.with(mContext)
                    .load(item.getImageUrl())
                    .transform(new CenterCrop(), new RoundedCorners(20))
                    .override(160)
                    .into(holder.iv_thumbnail);
        } catch (Exception e){

        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    // 리사이클러뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_name, tv_text, tv_time, tv_comment_count;
        public ImageView iv_thumbnail, iv_comment;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_comment_name);
            tv_text = itemView.findViewById(R.id.tv_comment_text);
            tv_time = itemView.findViewById(R.id.tv_comment_createAt);
            iv_comment = itemView.findViewById(R.id.iv_comment);
            tv_comment_count = itemView.findViewById(R.id.tv_comment_count);
            iv_thumbnail = itemView.findViewById(R.id.iv_thumbnail);
        }
    }
}