package com.example.petcam.profile.vod;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.petcam.R;
import com.example.petcam.main.PopularAdapter;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.example.petcam.profile.ChannelActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.USER_UID;

public class VODFragment extends Fragment {

    private ServiceApi mServiceApi;
    private SharedPreferences sharedPreferences;
    private RecyclerView mVodRV;
    private VODAdapter vodAdapter;
    private List<VODItem> mDataList;
    private String userID;
    private LinearLayout layout_none;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vod, container, false);
        // Inflate the layout for this fragment

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 쉐어드 프리퍼런스에 저장된 로그인된 사용자의 uid를 가져온다.
        sharedPreferences = getActivity().getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);

        if(getActivity().getClass().getSimpleName().equals("ChannelActivity")) {
            ChannelActivity channelActivity = (ChannelActivity) getActivity();
            userID = channelActivity.sendData();
        } else {
            userID = sharedPreferences.getString(USER_UID, "");
        }

        mDataList = new ArrayList<>();

        layout_none = view.findViewById(R.id.layout_none);
        mVodRV = view.findViewById(R.id.rv_vod);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mVodRV.setLayoutManager(linearLayoutManager); // LayoutManager 등록

        getVODList(userID);

        return view;
    }

    // =========================================================================================================

    // VOD 리스트 가져오기
    private void getVODList(String uid){

        Call<List<VODItem>> call = mServiceApi.getVODList(uid);
        call.enqueue(new Callback<List<VODItem>>() {
            @Override
            public void onResponse(Call<List<VODItem>> call, Response<List<VODItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mDataList = response.body();

                    if(!mDataList.isEmpty()) {
                        layout_none.setVisibility(View.GONE);
                    }
                    vodAdapter = new VODAdapter(mDataList, getActivity());
                    mVodRV.setAdapter(vodAdapter);
                    vodAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<VODItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }
}