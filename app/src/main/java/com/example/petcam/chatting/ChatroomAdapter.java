package com.example.petcam.chatting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

import org.jetbrains.annotations.NotNull;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.example.petcam.function.App.ROOM_ID;
import static com.example.petcam.function.App.ROOM_NAME;
import static com.example.petcam.function.App.ROOM_USER_COUNT;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatroomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatroomItem> mList;
    private Context mContext;

    public static final int SINGLE = 0;
    public static final int MULTI = 1;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm aa", Locale.KOREAN);
    Date today, chatDate, dateCheck;
    Date newDate = new Date();
    String date, time;
    int result;
    String current = dateFormat.format(newDate);

    {
        try {
            today = dateFormat.parse(current);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public ChatroomAdapter(List<ChatroomItem> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position).getView_type() == 0) {
            return SINGLE;
        } else {
            return MULTI;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == SINGLE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatroom_single, parent, false);
            return new SingleViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatroom_multi, parent, false);
            return new MultiViewHolder(view);
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        ChatroomItem item = mList.get(position);

        switch (holder.getItemViewType()) {
            case SINGLE: // 대화 상대가 한명일 경우,
                ((SingleViewHolder) holder).tv_single_name.setText(item.getChatroom_name());
                if (item.getUnread_message_num().equals("0")) {
                    ((SingleViewHolder) holder).tv_single_num.setVisibility(View.INVISIBLE);
                } else {
                    ((SingleViewHolder) holder).tv_single_num.setText(item.getUnread_message_num());
                }
                if (item.getLast_message() != null) {
                    ((SingleViewHolder) holder).tv_single_text.setText(item.getLast_message());
                    try {
                        chatDate = format.parse(item.getDay_check());
                        dateCheck = dateFormat.parse(item.getDay_check());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.e("타임", String.valueOf(chatDate));
                    date = dateFormat.format(chatDate);
                    time = timeFormat.format(chatDate);
                    result = today.compareTo(dateCheck);
                    Log.e("날짜", date);
                    Log.e("시간", time);
                    Log.e("결과값", String.valueOf(result));
                    if (result == 1) {
                        ((SingleViewHolder) holder).tv_single_time.setText(date);
                    } else {
                        ((SingleViewHolder) holder).tv_single_time.setText(time);
                    }
                }
                // 프로필 이미지가 등록되어 있는지 여부 확인
                if (item.getChatroom_user_profile() != null) {
                    // 현재 유저 프로필 이미지 보여주기
                    Glide.with(mContext).load(item.getChatroom_user_profile()).centerCrop().into(((SingleViewHolder) holder).civ_single_profile);

                } else {
                    // 프로필 이미지가 없다면 기본 이미지 보여주기
                    Glide.with(mContext).load(R.drawable.ic_user).centerCrop().into(((SingleViewHolder) holder).civ_single_profile);
                }

                ((SingleViewHolder) holder).cv_chat_single_item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ChattingActivity.class);
                        intent.putExtra(ROOM_ID, item.getChatroom_id());
                        intent.putExtra(ROOM_NAME, item.getChatroom_name());
                        mContext.startActivity(intent);
                    }
                });
                break;
            case MULTI: // 대화 상대가 여러명일 경우,


                if (item.getUnread_message_num().equals("0")) {
                    ((MultiViewHolder) holder).tv_multi_num.setVisibility(View.INVISIBLE);
                } else {
                    ((MultiViewHolder) holder).tv_multi_num.setText(item.getUnread_message_num());
                }
                String[] profileList = item.getChatroom_user_profile().split(", ");

                if (profileList[0].isEmpty()) {
                    Glide.with(mContext).load(R.drawable.ic_user).centerCrop().into(((MultiViewHolder) holder).civ_multi_profile);
                } else {
                    // 현재 유저 프로필 이미지 보여주기
                    Glide.with(mContext).load(profileList[0]).centerCrop().into(((MultiViewHolder) holder).civ_multi_profile);
                }

                if (profileList[1].isEmpty()) {
                    Glide.with(mContext).load(R.drawable.ic_user).centerCrop().into(((MultiViewHolder) holder).civ_multi_profile2);
                } else {
                    // 현재 유저 프로필 이미지 보여주기
                    Glide.with(mContext).load(profileList[1]).centerCrop().into(((MultiViewHolder) holder).civ_multi_profile2);
                }

                    if (item.getLast_message() != null) {
                        ((MultiViewHolder) holder).tv_multi_text.setText(item.getLast_message());

                        try {
                            chatDate = format.parse(item.getDay_check());
                            dateCheck = dateFormat.parse(item.getDay_check());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        date = dateFormat.format(chatDate);
                        time = timeFormat.format(chatDate);
                        result = today.compareTo(dateCheck);
                        if (result == 1) {
                            ((MultiViewHolder) holder).tv_multi_time.setText(date);
                        } else {
                            ((MultiViewHolder) holder).tv_multi_time.setText(time);
                        }
                    }
                    ((MultiViewHolder) holder).tv_multi_name.setText(item.getChatroom_name());
                    ((MultiViewHolder) holder).tv_multi_user_num.setText(item.getChatroom_user_num());

                    ((MultiViewHolder) holder).cv_chat_multi_item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ChattingActivity.class);
                            intent.putExtra(ROOM_ID, item.getChatroom_id());
                            intent.putExtra(ROOM_NAME, item.getChatroom_name());
                            intent.putExtra(ROOM_USER_COUNT, item.getChatroom_user_num());
                            mContext.startActivity(intent);
                        }
                    });
                    break;
                }
    }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class SingleViewHolder extends RecyclerView.ViewHolder {
            TextView tv_single_name, tv_single_text, tv_single_time, tv_single_num;
            CircleImageView civ_single_profile;
            CardView cv_chat_single_item;

            SingleViewHolder(View itemView) {
                super(itemView);
                civ_single_profile = itemView.findViewById(R.id.civ_chat_single_profile);
                tv_single_name = itemView.findViewById(R.id.tv_chat_single_name);
                tv_single_text = itemView.findViewById(R.id.tv_chat_single_text);
                tv_single_time = itemView.findViewById(R.id.tv_chat_single_time);
                cv_chat_single_item = itemView.findViewById(R.id.cv_chat_single_item);
                tv_single_num = itemView.findViewById(R.id.tv_chat_single_num);
            }
        }

            public class MultiViewHolder extends RecyclerView.ViewHolder {
                TextView tv_multi_name, tv_multi_text, tv_multi_time, tv_multi_user_num, tv_multi_num;
                CircleImageView civ_multi_profile, civ_multi_profile2;
                CardView cv_chat_multi_item;

                MultiViewHolder(View itemView) {
                    super(itemView);
                    civ_multi_profile = itemView.findViewById(R.id.civ_chat_multi_profile);
                    civ_multi_profile2 = itemView.findViewById(R.id.civ_chat_multi_profile2);
                    tv_multi_name = itemView.findViewById(R.id.tv_chat_multi_name);
                    tv_multi_text = itemView.findViewById(R.id.tv_chat_multi_text);
                    tv_multi_time = itemView.findViewById(R.id.tv_chat_multi_time);
                    tv_multi_user_num = itemView.findViewById(R.id.tv_chat_multi_user_num);
                    cv_chat_multi_item = itemView.findViewById(R.id.cv_chat_multi_item);
                    tv_multi_num = itemView.findViewById(R.id.tv_chat_multi_num);
                }
            }

        }
