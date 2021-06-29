package com.example.petcam.profile.notice;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.petcam.R;
import com.example.petcam.main.MainActivity;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.example.petcam.profile.ChannelActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.USER_UID;

public class NoticeFragment extends Fragment {

    private ServiceApi mServiceApi;
    private SharedPreferences sharedPreferences;
    private LinearLayout layout_none;
    private RecyclerView rv_fix_top_notice, rv_notice;
    private FloatingActionButton mUploadButton;
    private List<FixTopNoticeItem> mFixTopDataList;
    private List<NoticeItem> mDataList;
    private String uid, myID;

     View.OnClickListener onClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View view) {

             switch (view.getId()) {

                 // 버튼을 클릭했을 경우,
                 case R.id.btn_update_notice:
                     Intent intent = new Intent(getContext(), NoticeActivity.class);
                     startActivity(intent);
                     getActivity().overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
                     break;
             }
         }
     };

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice, container, false);
        // Inflate the layout for this fragments

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        view.findViewById(R.id.btn_update_notice).setOnClickListener(onClickListener);

        // 선언
        layout_none = view.findViewById(R.id.layout_none);
        mUploadButton = view.findViewById(R.id.btn_update_notice);

        // 상단 고정 공지사항 리사이클러뷰
        mFixTopDataList = new ArrayList<>();
        rv_fix_top_notice = view.findViewById(R.id.rv_fix_top_notice);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_fix_top_notice.setLayoutManager(linearLayoutManager); // LayoutManager 등록

        // 일반 공지사항 리사이클러뷰
        mDataList = new ArrayList<>();
        rv_notice = view.findViewById(R.id.rv_notice);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_notice.setLayoutManager(linearLayoutManager2); // LayoutManager 등록

        // 쉐어드 프리퍼런스에 저장된 로그인된 사용자의 uid를 가져온다.
        sharedPreferences = getActivity().getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        myID = sharedPreferences.getString(USER_UID, "");

        if(getActivity().getClass().getSimpleName().equals("ChannelActivity")) {
            ChannelActivity channelActivity = (ChannelActivity) getActivity();
            uid = channelActivity.sendData();
        } else {
            uid = sharedPreferences.getString(USER_UID, "");
        }

        if(uid.equals(myID)) {
            mUploadButton.setVisibility(View.VISIBLE);
        } else {
            mUploadButton.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 공지사항 리스트를 가져와서 보여준다.
        getFixTopNotice(uid);
        getNotice(uid);
    }

    // =========================================================================================================
    // 상단 고정 공지사항 가져오기
    private void getFixTopNotice(String uid){

        Call<List<FixTopNoticeItem>> call = mServiceApi.getFixTopNotice(uid);
        call.enqueue(new Callback<List<FixTopNoticeItem>>() {
            @Override
            public void onResponse(Call<List<FixTopNoticeItem>> call, Response<List<FixTopNoticeItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mFixTopDataList = response.body();
                    // 공지사항이 있다면 '등록된 글이 없습니다.' 안내문을 삭제한다.
                    if(!mFixTopDataList.isEmpty()) {
                        layout_none.setVisibility(View.GONE);
                    }
                    FixTopNoticeAdapter fixTopNoticeAdapter = new FixTopNoticeAdapter(mFixTopDataList, getActivity());
                    rv_fix_top_notice.setAdapter(fixTopNoticeAdapter);
                    fixTopNoticeAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<FixTopNoticeItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    // 일반 공지사항 가져오기
    private void getNotice(String uid){

        Call<List<NoticeItem>> call = mServiceApi.getNotice(uid);
        call.enqueue(new Callback<List<NoticeItem>>() {
            @Override
            public void onResponse(Call<List<NoticeItem>> call, Response<List<NoticeItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mDataList = response.body();
                    // 공지사항이 있다면 '등록된 글이 없습니다.' 안내문을 삭제한다.
                    if(!mDataList.isEmpty()) {
                        layout_none.setVisibility(View.GONE);
                    }
                    NoticeAdapter noticeAdapter = new NoticeAdapter(mDataList, getActivity());
                    rv_notice.setAdapter(noticeAdapter);
                    noticeAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<NoticeItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }
}