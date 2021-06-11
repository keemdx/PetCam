package com.example.petcam.access;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petcam.main.MainActivity;
import com.example.petcam.R;
import com.example.petcam.network.ResultModel;
import com.example.petcam.network.RetrofitClient;
import com.example.petcam.network.ServiceApi;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.petcam.function.App.TAG;
import static com.example.petcam.function.App.AUTO_LOGIN_KEY;
import static com.example.petcam.function.App.LOGIN_STATUS;
import static com.example.petcam.function.App.USER_EMAIL;
import static com.example.petcam.function.App.USER_NAME;
import static com.example.petcam.function.App.USER_IMAGE;
import static com.example.petcam.function.App.USER_STATUS;
import static com.example.petcam.function.App.USER_UID;
import static com.example.petcam.function.App.USER_ID;


/**
 * Class: IntroActivity
 *
 * Comment
 * 이 액티비티는 신규 사용자들이 처음 접하게 되는 인트로 화면입니다.
 **/

public class IntroActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private ServiceApi service;
    private VideoView videoBG;
    private MediaPlayer mMediaPlayer;
    int mCurrentVideoPosition;
    int RC_SIGN_IN = 0;
    GoogleSignInClient mGoogleSignInClient;

    // 클릭 리스너
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                // 회원가입 버튼
                case R.id.btn_register:
                // 회원가입 텍스트뷰를 누르면 회원가입 페이지로 넘어간다.
                Intent intent_signUp = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent_signUp);
                break;

                // 로그인 버튼
                case R.id.btn_login:
                Intent intent_login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent_login);
                break;

                // 구글 로그인 버튼
                case R.id.btn_googleLogin:
                signIn();
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // =========================================================================================================
        // 자동 로그인 확인을 위한 부분
        SharedPreferences autoLogin = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
        String status = autoLogin.getString(AUTO_LOGIN_KEY, ""); // 저장된 자동로그인 정보가 있으면 "success" 없을 경우 "fail"

        // 자동 로그인 정보가 저장되어 있다면 "success" 아닌 경우 "fail"
        if (status.equals("success")) {

            USER_ID = autoLogin.getString(USER_UID, ""); // USER_ID 전역 변수에 고유 ID 값 담아서 어디서든 사용하기 편하게 해준다.
            Log.d(TAG, "USER_UID 값은? : " + USER_ID);

            // 로그인 한 경험이 있는 회원은 자동으로 MainActivity로 넘어간다.
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        // =========================================================================================================

        // 상단 상태바(Status bar)를 투명하게 만든다.
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        // ServiceApi 객체를 생성한다.
        service = RetrofitClient.getClient().create(ServiceApi.class);

        // 클릭 이벤트를 위해 버튼에 클릭 리스너 달아주기
        findViewById(R.id.btn_register).setOnClickListener(onClickListener); // 회원가입 버튼
        findViewById(R.id.btn_login).setOnClickListener(onClickListener); // 로그인 버튼
        findViewById(R.id.btn_googleLogin).setOnClickListener(onClickListener); // 구글 로그인 버튼

        // 배경 비디오 뷰 화면
        videoBG = (VideoView) findViewById(R.id.videoView);

        Uri uri = Uri.parse("android.resource://"
                + getPackageName()
                + "/"
                + R.raw.intro);

        videoBG.setVideoURI(uri);
        videoBG.start();

        videoBG.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mMediaPlayer = mediaPlayer;
                mMediaPlayer.setLooping(true);
                if (mCurrentVideoPosition != 0) {
                    mMediaPlayer.seekTo(mCurrentVideoPosition);
                    mMediaPlayer.start();
                }
            }
        });

        /**
         * 구글 연동 로그인
         **/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        // gso에서 지정한 옵션으로 GoogleSignInClient를 빌드
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCurrentVideoPosition = mMediaPlayer.getCurrentPosition();
        videoBG.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoBG.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // 구글 로그인 인증을 요청했을 때 결과 값을 되돌려 받는다.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // account => 구글 로그인 정보를 담고 있다.
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // 구글 토큰, 현재 시간 정보
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(IntroActivity.this);
            if (acct != null) {
                String userPhoto = acct.getPhotoUrl().toString();
                String userName = acct.getDisplayName();
                String userEmail = acct.getEmail();
                // 서버로 구글 로그인 정보를 보내고 응답을 받는다.
                checkGoogleId(userPhoto, userName, userEmail);
            }
        } catch (ApiException e) {
            Log.e("Error", "signInResult:failed code=" + e.getStatusCode());
        }
    }


    // 구글 로그인 요청
    private void checkGoogleId(String userPhoto, String userName, String userEmail) {
        service.userGoogleLogin(userPhoto, userName, userEmail).enqueue(new Callback<ResultModel>() {
            @Override
            public void onResponse(Call<ResultModel> call, Response<ResultModel> response) {
                ResultModel result = response.body();
                // [success] 홈(메인) 화면으로 넘어간다.
                if (result.getResult().equals("success")) {

                    // 로그인 정보를 저장한다.
                    pref = getSharedPreferences(LOGIN_STATUS, Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(AUTO_LOGIN_KEY, "success"); // 자동 로그인 상태 저장
                    editor.putString(USER_UID, result.getUid());
                    editor.putString(USER_IMAGE, result.getUserPhoto());
                    editor.putString(USER_EMAIL, userEmail);
                    editor.putString(USER_NAME, result.getUserName());
                    editor.putString(USER_STATUS, result.getUserStatus());
                    editor.commit();

                    // 로그인을 완료하고 홈(메인) 화면으로 넘어간다.
                    Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(IntroActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResultModel> call, Throwable t) {
                Toast.makeText(IntroActivity.this, "로그인 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("로그인 에러 발생", t.getMessage());
                t.printStackTrace(); // 에러 발생시 에러 발생 원인 단계별로 출력해 준다.
            }
        });
    }



}