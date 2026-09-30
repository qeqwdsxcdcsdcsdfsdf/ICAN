package com.attendance.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.attendance.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");
        
        try {
            setContentView(R.layout.activity_main);
            
            viewPager = findViewById(R.id.view_pager);
            tabLayout = findViewById(R.id.tab_layout);
            
            viewPager.setAdapter(new FragmentPagerAdapter(this));
            
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText("签到");
                        break;
                    case 1:
                        tab.setText("记录");
                        break;
                    case 2:
                        tab.setText("我的");
                        break;
                }
            }).attach();
            
            Log.d(TAG, "onCreate completed");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            android.widget.Toast.makeText(this, "启动失败: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private class FragmentPagerAdapter extends FragmentStateAdapter {
        public FragmentPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new SignInFragment();
                case 1:
                    return new AttendanceRecordFragment();
                case 2:
                    return new ProfileFragment();
                default:
                    return new SignInFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
