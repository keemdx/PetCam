package com.example.petcam.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcam.R;

import java.util.List;

public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.ViewHolder> {

    private List<FollowingItem> mList;
    private Context mContext;

    public FollowingAdapter(List<FollowingItem> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_following, parent, false);

        // 뷰를 가져온다
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        FollowingItem item = mList.get(position);
        holder.tv_name.setText(item.getName());
        holder.iv_profile.setImageResource(item.getImage());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    // 리사이클러뷰 홀더
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_name;
        public ImageView iv_profile;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tv_name = itemView.findViewById(R.id.tv_name);
            this.iv_profile = itemView.findViewById(R.id.iv_profile);
        }
    }
}