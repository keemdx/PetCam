package com.example.petcam.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petcam.R;
import com.example.petcam.chatting.ChatroomActivity;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.USER_UID;


public class HomeFragment extends Fragment {

    private ServiceApi mServiceApi;
    private SharedPreferences pref;
    private String userID;
    private RecyclerView mFollowingRV, mPopularRV, mChartsRV;
    private List<PopularItem> mPopularList;
    private List<FollowingItem> mFollowingList;
    private List<ChartVideosItem> mVideosList;
    private List<ChartChannelsItem> mChannelsList;
    private RadioGroup radioGroup;
    private RadioButton mVideosRB, mChannelsRB;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 다이렉트 메시지 아이콘을 클릭했을 경우, 채팅 리스트 액티비티로 넘어간다.
                case R.id.iv_message:
                    Intent intent = new Intent(getContext(), ChatroomActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 저장된 유저 정보 가져오기
        pref = getActivity().getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        userID = pref.getString(USER_UID, ""); // 유저 프로필 이미지

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        view.findViewById(R.id.iv_message).setOnClickListener(onClickListener);

        radioGroup = view.findViewById(R.id.rc_group);
        mVideosRB = view.findViewById(R.id.rb_videos);
        mChannelsRB = view.findViewById(R.id.rb_channels);
        mVideosRB.setChecked(true);

        // 라이브 방송 중인 팔로잉 유저 (친구) 리스트
        mFollowingRV = view.findViewById(R.id.rv_following);
        LinearLayoutManager fManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mFollowingRV.setLayoutManager(fManager); // LayoutManager 등록

        getLiveNowFriends(userID); // 라이브 중인 친구 리스트 가져오기

        // 현재 인기 실시간 방송 영상 리스트
        mPopularRV = view.findViewById(R.id.rv_popular);
        LinearLayoutManager pManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mPopularRV.setLayoutManager(pManager); // LayoutManager 등록

        getHotLiveRooms(); // 실시간 방송 뷰어(시청자) 순으로 가져오기


        mVideosList = new ArrayList<>();
        ChartVideosItem chartVideosItem = new ChartVideosItem("쿠마랑 나들이",
                String.valueOf(System.currentTimeMillis()),
                "333",
                "",
                "제니",
                "",
                R.drawable.ic_profile);
        mVideosList.add(chartVideosItem);

        ChartVideosItem chartVideosItem1 = new ChartVideosItem("차오츄르먹방!!!",
                String.valueOf(System.currentTimeMillis()),
                "2",
                "",
                "코로나",
                "",
                R.drawable.ic_profile);
        mVideosList.add(chartVideosItem1);

        mChartsRV = view.findViewById(R.id.rv_charts);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mChartsRV.setLayoutManager(manager); // LayoutManager 등록
        mChartsRV.setAdapter(new ChartVideosAdapter(mVideosList, getContext()));

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    // 채널 2개 버튼
                    case R.id.rb_videos: // 비디오(vod) 차트

                    mVideosList = new ArrayList<>();
                    ChartVideosItem chartVideosItem = new ChartVideosItem("쿠마랑 나들이",
                            String.valueOf(System.currentTimeMillis()),
                            "333",
                            "",
                            "제니",
                            "",
                            R.drawable.ic_profile);
                    mVideosList.add(chartVideosItem);

                    ChartVideosItem chartVideosItem1 = new ChartVideosItem("차오츄르먹방!!!",
                            String.valueOf(System.currentTimeMillis()),
                            "2",
                            "",
                            "코로나",
                            "",
                            R.drawable.ic_profile);
                    mVideosList.add(chartVideosItem1);

                    mChartsRV = view.findViewById(R.id.rv_charts);
                    LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                    mChartsRV.setLayoutManager(manager); // LayoutManager 등록
                    mChartsRV.setAdapter(new ChartVideosAdapter(mVideosList, getContext()));

                        break;

                    case R.id.rb_channels: // 채널 차트

                        mChannelsList = new ArrayList<>();
                        ChartChannelsItem chartChannelsItem = new ChartChannelsItem("33","제니","");
                        mChannelsList.add(chartChannelsItem);

                        ChartChannelsItem chartChannelsItem2 = new ChartChannelsItem("33","쿠쿠파파잉","");
                        mChannelsList.add(chartChannelsItem2);
                        LinearLayoutManager manager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                        mChartsRV.setLayoutManager(manager2); // LayoutManager 등록
                        mChartsRV.setAdapter(new ChartChannelsAdapter(mChannelsList, getContext()));

                        break;

                    default:
                        break;
                }

            }
        });
        return view;
    }

    // =========================================================================================================

    // 라이브 중인 친구 (팔로잉) 리스트 가져오기
    private void getLiveNowFriends(String uid){

        Call<List<FollowingItem>> call = mServiceApi.getLiveNowFriends(uid);
        call.enqueue(new Callback<List<FollowingItem>>() {
            @Override
            public void onResponse(Call<List<FollowingItem>> call, Response<List<FollowingItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mFollowingList = response.body();
                    mFollowingRV.setVisibility(View.VISIBLE);
                    FollowingAdapter mFollowingAdapter = new FollowingAdapter(mFollowingList, getContext());
                    mFollowingRV.setAdapter(mFollowingAdapter);
                    mFollowingAdapter.notifyDataSetChanged();

                } else {
                    if(mFollowingRV.getVisibility() == View.VISIBLE) {
                        mFollowingRV.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<FollowingItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }

    // =========================================================================================================

    // HOT LIVE : 현재 실시간 방송 중인 룸 조회 수 순으로 정렬해서 보여주기
    private void getHotLiveRooms() {

        Call<List<PopularItem>> call = mServiceApi.getHotLiveRooms(userID);
        call.enqueue(new Callback<List<PopularItem>>() {
            @Override
            public void onResponse(Call<List<PopularItem>> call, Response<List<PopularItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mPopularList = response.body();
                    mPopularRV.setVisibility(View.VISIBLE);
                    PopularAdapter mPopularAdapter = new PopularAdapter(mPopularList, getContext());
                    mPopularRV.setAdapter(mPopularAdapter);
                    mPopularAdapter.notifyDataSetChanged();

                } else {
                    if(mPopularRV.getVisibility() == View.VISIBLE) {
                        mPopularRV.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<PopularItem>> call, Throwable t) {
                Log.e("오류태그", t.getMessage());
            }
        });
    }
}