package com.example.petcam.streaming;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.petcam.R;
import com.example.petcam.ui.main.MainActivity;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.STREAMING_ROOM_ID;
import static com.example.petcam.function.App.makeStatusBarBlack;

public class StreamingResultActivity extends AppCompatActivity {

    private ServiceApi mServiceApi;
    private ImageView mBackground;
    private RecyclerView mViewersRV;
    private TextView mViewerCount;
    private String roomID;
    private List<ViewersItem> mDataList, mList;
    private ViewersAdapter adapter;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.btn_finish: // 확인 버튼 (종료)
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_result);

        // 서버와의 연결을 위한 ServiceApi 객체를 생성한다.
        mServiceApi = RetrofitClient.getClient().create(ServiceApi.class);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        // 리사이클러뷰에서 받은 룸 아이디 가져오기
        Intent intent = getIntent();
        roomID = intent.getStringExtra(STREAMING_ROOM_ID);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.btn_finish).setOnClickListener(onClickListener);

        // UI 선언
        mBackground = (ImageView) findViewById(R.id.iv_background);
        mViewerCount = (TextView) findViewById(R.id.tv_viewers);
        mViewersRV = (RecyclerView) findViewById(R.id.rv_viewers);

        mDataList = new ArrayList<>();
        mList = new ArrayList<>();


        getLiveResult(roomID);
    }

    // =========================================================================================================

    // 서버에서 종료된 라이브 스트리밍 마지막 이미지 가져오기 (종료시 저장된 thumbnail)
    @SuppressLint("SimpleDateFormat")
    private void getLiveResult(String roomID) {

        mServiceApi.getLiveResult(roomID).enqueue(new Callback<List<ViewersItem>>() {
            // 통신이 성공했을 경우 호출된다. Response 객체에 응답받은 데이터가 들어있다.
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<List<ViewersItem>> call, Response<List<ViewersItem>> response) {
                // 정상적으로 네트워크 통신 완료
                if (response.isSuccessful() && response.body() != null) {
                    mDataList = response.body();

                    Glide.with(getApplicationContext())
                            .load(mDataList.get(0).getThumbnail_image())
                            .into(mBackground);

                    if (mDataList.get(0).getViewer_image() == null) {
                        mViewerCount.setText("0 viewer");
                    } else {
                        mViewerCount.setText((mDataList.size()) + " viewers");
                        for (int i = 0; i < mDataList.size(); i++) {
                            String getViewerPhoto = mDataList.get(i).getViewer_image();
                            mList.add(new ViewersItem(getViewerPhoto));
                        }
                        mViewersRV.setLayoutManager(new GridLayoutManager(StreamingResultActivity.this, mDataList.size()));
                        adapter = new ViewersAdapter(mList, StreamingResultActivity.this);
                        mViewersRV.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            // 통신이 실패했을 경우 호출된다.
            @Override
            public void onFailure(Call<List<ViewersItem>> call, Throwable t) {
                Toast.makeText(StreamingResultActivity.this, "에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("에러 발생", t.getMessage());
            }
        });
    }

    // =========================================================================================================
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }
}