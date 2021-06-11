package com.example.petcam.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.petcam.R;
import com.example.petcam.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.petcam.function.App.makeStatusBarBlack;

/**
 * Class: MainActivity
 *
 * Comment
 * 이 액티비티는 앱의 메인 화면입니다.
 **/

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private BottomNavigationView bottomNavigation;

    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 상단 상태바(Status bar) 컬러를 블랙으로 바꾼다.
        makeStatusBarBlack(this);

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