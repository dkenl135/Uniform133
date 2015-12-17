package com.ironfactory.donation.controllers.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.ironfactory.donation.Global;
import com.ironfactory.donation.R;
import com.ironfactory.donation.controllers.fragments.BuyFragment;
import com.ironfactory.donation.controllers.fragments.CommunityFragment;
import com.ironfactory.donation.controllers.fragments.MyPageFragment;
import com.ironfactory.donation.controllers.fragments.SettingFragment;
import com.ironfactory.donation.controllers.fragments.SupportFragment;
import com.ironfactory.donation.entities.AlarmEntity;
import com.ironfactory.donation.slidingtab.SlidingBaseFragment;
import com.ironfactory.donation.slidingtab.SlidingTabsBasicFragment;

import java.util.ArrayList;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    public static Context context;

    private MenuItem alarmMenuItem;

    private CommunityFragment communityFragment;
    private BuyFragment buyFragment;
    private MyPageFragment myPageFragment;
    private SupportFragment supportFragment;
    private SettingFragment settingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews(R.layout.activity_main);
        Log.d(TAG, "액티비티 시작");
        context = this;
        if (savedInstanceState == null) {
            if (getIntent().getParcelableExtra(Global.USER) == null) {
                Toast.makeText(getApplicationContext(), "로그인 중 에러가 발생했습니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                finish();
            }

            Global.userEntity = getIntent().getParcelableExtra(Global.USER);
            Log.d(TAG, "id = " + Global.userEntity.id);
            ArrayList<SlidingBaseFragment> tabFragments = new ArrayList<>();

            communityFragment = new CommunityFragment();
            buyFragment = new BuyFragment();
            myPageFragment = new MyPageFragment();
            supportFragment = new SupportFragment();
            settingFragment = new SettingFragment();

            tabFragments.add(communityFragment);
            tabFragments.add(buyFragment);
            tabFragments.add(myPageFragment);
            tabFragments.add(supportFragment);
            tabFragments.add(settingFragment);
            SettingFragment.context = getApplicationContext();

            ArrayList<String> tabTitles = new ArrayList<>();
            tabTitles.add("커뮤니티");
            tabTitles.add("마켓");
            tabTitles.add("마이페이지");
            tabTitles.add("서포트");
            tabTitles.add("설정");

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();
            fragment.setTabFragments(tabFragments);
            fragment.setTabTitles(tabTitles);
            transaction.replace(R.id.content_fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BillingProcessor billingProcessor = SupportFragment.billingProcessor;
        if (billingProcessor != null) {
            if (billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
                return;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        alarmMenuItem = menu.findItem(R.id.action_alram);
        updateAlarmStatus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_alram) {
            BaseActivity.startActivityUsingStack(AlarmActivity.class);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateAlarmStatus();
    }


    private void updateAlarmStatus() {
        if (alarmMenuItem == null) {
            return;
        }

        alarmMenuItem.setIcon(hasNotSyncedAlarm() ? R.drawable.ic_alram : R.drawable.ic_no_alarm);
    }

    private boolean hasNotSyncedAlarm() {
//        ArrayList<BaasioPaylolad> receivedPushMessages = SettingFragment.getReceivedPushMessages();
        ArrayList<AlarmEntity> receivedPushMessages = SettingFragment.getReceivedPushMessages();
        if (receivedPushMessages.isEmpty()) {
            return false;
        }

        long lastSyncedTimestamp = SettingFragment.getLastAlarmSyncedTimestamp();
//        AlarmEntity latestAlarmEntity = new AlarmEntity(receivedPushMessages.get(0));
        AlarmEntity latestAlarmEntity = receivedPushMessages.get(0);
        return (lastSyncedTimestamp < latestAlarmEntity.timestamp);
    }

    @Override
    protected void initViews(int layoutResID) {
        setContentView(layoutResID);
    }
}