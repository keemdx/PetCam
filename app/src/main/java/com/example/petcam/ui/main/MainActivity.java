package com.example.petcam.ui.main;

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
import com.example.petcam.databinding.ActivityMainBinding;
import com.example.petcam.ui.profile.ProfileFragment;
import com.example.petcam.streaming.StreamingActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * Class: MainActivity
 * <p>
 * Comment
 * 이 액티비티는 앱의 메인 화면입니다.
 **/

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Intent intent;
    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;
    private final int MY_PERMISSIONS_REQUEST = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

        binding.btnStreaming.setOnClickListener(v -> {
            startStreamingActivity();
        });

        BottomNavigationView bottomNavigation;
        bottomNavigation = binding.bottomNavigation;
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.action_live:
                    setFrag(0);
                    break;

                case R.id.action_profile:
                    setFrag(1);
                    break;
            }
            return true;
        });

        homeFragment = new HomeFragment();
        profileFragment = new ProfileFragment();

        setFrag(0); // 첫 프래그먼트 화면 지정 -> 홈 화면
    }

    // 프레그먼트 교체
    private void setFrag(int n) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (n) {
            case 0:
                fragmentTransaction.replace(R.id.container, homeFragment);
                fragmentTransaction.commit();
                break;

            case 1:
                fragmentTransaction.replace(R.id.container, profileFragment);
                fragmentTransaction.commit();
                break;
        }
    }

    private void startStreamingActivity() {
        intent = new Intent(getApplicationContext(), StreamingActivity.class);
        int cameraPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        int audioPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO);
        if (cameraPermission == PackageManager.PERMISSION_DENIED || audioPermission == PackageManager.PERMISSION_DENIED) { // 권한 없어서 요청
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, MY_PERMISSIONS_REQUEST);
        } else { // 권한 있음
            startActivity(intent);
        }
    }
}