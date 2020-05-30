package com.example.waterpollutant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

public class OnBoardingActivity extends AppCompatActivity implements View.OnClickListener {
    private ViewPager viewPager;
    private int[] layouts = {R.layout.on_boarding_1, R.layout.on_boarding_2, R.layout.on_boarding_3};
    private LinearLayout dotslayout;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button nextBtn = findViewById(R.id.Next);
        Button skipBtn = findViewById(R.id.skip);
        skipBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);

        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         editor = mPreferences.edit();

        boolean islogged = mPreferences.getBoolean("isLogged", false);
        boolean firstInstall = mPreferences.getBoolean("firstInstall", false);
        if (islogged) {
            loadMainActivity();
        }else if (firstInstall){
            loadLoginActivity();
        }

        viewPager = findViewById(R.id.viewPager);
        OnBoardingAdapter onBoardingAdapter = new OnBoardingAdapter(layouts, this);
        viewPager.setAdapter(onBoardingAdapter);

        dotslayout = findViewById(R.id.dotsLayout);

        // createdots(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                RelativeLayout layout = findViewById(R.id.hide);
                if (i == 3) {

                    layout.setVisibility(View.GONE);
                    //  createdots(i);

                } else {
                    //     createdots(i);
                    layout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    private void createdots(int current_position) {
        if (dotslayout != null)
            dotslayout.removeAllViews();

        ImageView[] dots = new ImageView[layouts.length];
        for (int i = 0; i < layouts.length; i++) {
            dots[i] = new ImageView(this);
            if (i == current_position) {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_dots));
            } else {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.default_dots));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 4, 0);
            dotslayout.addView(dots[i], params);

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Next:
                load_nxt();
                break;
            case R.id.skip:
                loadLoginActivity();
                break;
        }

    }


    private void loadLoginActivity() {
        // finishOnboarding();
        Intent intent = new Intent(OnBoardingActivity.this, LoginActivity.class);
        startActivity(intent);
        editor.putBoolean("firstInstall",true);
        editor.commit();
        OnBoardingActivity.this.finish();
    }
private void loadMainActivity() {
        // finishOnboarding();
        Intent intent = new Intent(OnBoardingActivity.this, MainActivity.class);
        startActivity(intent);
        OnBoardingActivity.this.finish();
    }

    public void load_nxt() {
        int nextslide = viewPager.getCurrentItem() + 1;
        if (nextslide < layouts.length) {
            viewPager.setCurrentItem(nextslide);
        } else {
            loadLoginActivity();
        }
    }
}
