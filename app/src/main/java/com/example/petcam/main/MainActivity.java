package com.example.petcam.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import com.example.petcam.R;
import com.example.petcam.profile.ProfileFragment;
import com.example.petcam.streaming.StreamingActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * Class: MainActivity
 *
 * Comment
 * 이 액티비티는 앱의 메인 화면입니다.
 **/

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST = 1001;
    private Intent intent;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private BottomNavigationView bottomNavigation;

    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                // 스트리밍 시작 버튼을 클릭했을 경우,
                case R.id.btn_streaming:
                    // 방송 시작 액티비티로 이동한다.
                    intent = new Intent(getApplicationContext(), StreamingActivity.class);
                    int cameraPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
                    int audioPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO);
                    if(cameraPermission == PackageManager.PERMISSION_DENIED || audioPermission == PackageManager.PERMISSION_DENIED) { // 권한 없어서 요청
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                        }, MY_PERMISSIONS_REQUEST);
                    } else { // 권한 있음
                        startActivity(intent);
                    }
                    break;
            }
        }
    };

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //동의 했을 경우 .....
                    startActivity(intent);
                } else { //거부했을 경우
                    Toast.makeText(this, "기능 사용을 위한 권한 동의가 필요합니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        findViewById(R.id.btn_streaming).setOnClickListener(onClickListener);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_live:
                        setFrag(0);
                        break;

                    case R.id.action_profile:
                        setFrag(1);
                        break;
                }
                return true;
            }
        });

        homeFragment = new HomeFragment();
        profileFragment = new ProfileFragment();

        setFrag(0); // 첫 프래그먼트 화면 지정 -> 홈 화면
    }

    // 프레그먼트 교체
    private void setFrag(int n) {

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction= fragmentManager.beginTransaction();

        switch(n) {
            case 0:
                fragmentTransaction.replace(R.id.container,homeFragment);
                fragmentTransaction.commit();
                break;

            case 1:
                fragmentTransaction.replace(R.id.container,profileFragment);
                fragmentTransaction.commit();
                break;
        }
    }
}