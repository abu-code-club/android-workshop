package com.codeclub.abu.chatcodeclub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.*;

import com.codeclub.abu.chatcodeclub.R;
import com.codeclub.abu.chatcodeclub.adapters.CustomPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class Dashboard extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDb;
    private ViewPager mViewPager;
    private CustomPagerAdapter mCustomPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        // tabs
        mViewPager = findViewById(R.id.main_viewPager);
        mCustomPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mCustomPagerAdapter);

        mTabLayout = findViewById(R.id.main_tabPager);
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_bubble_black_24dp);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_group_add);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_group_add);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.main_allUsers_btn:
                Intent allUsersIntent = new Intent(this, UsersActivity.class);
                startActivity(allUsersIntent);
                break;
            case R.id.main_logout_btn:
                FirebaseAuth.getInstance().signOut();
                gotToLoginActivity();
                break;
            default:
                return true;
        }
        return true;
    }

    private void gotToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
