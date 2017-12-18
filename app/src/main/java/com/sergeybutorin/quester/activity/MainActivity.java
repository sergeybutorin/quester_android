package com.sergeybutorin.quester.activity;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.sergeybutorin.quester.BuildConfig;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.fragment.AuthFragment;
import com.sergeybutorin.quester.fragment.ProfileFragment;
import com.sergeybutorin.quester.fragment.QFragment;
import com.sergeybutorin.quester.fragment.QMapFragment;
import com.sergeybutorin.quester.fragment.QuestAddFragment;
import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.model.UserProfile;
import com.sergeybutorin.quester.utils.SPHelper;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        QMapFragment.QuestAddListener,
        QuestAddFragment.QuestSavedListener {
    private static String FRAGMENT_TAG = "FRAGMENT_TAG";

    private TextView nameTextView;
    private TextView emailTextView;
    private MenuItem loginItem;
    private MenuItem profileItem;
    private SPHelper spHelper;

    private QFragment currentFragment;
    private QMapFragment qMapFragment = new QMapFragment();
    private AuthFragment authFragment = new AuthFragment();
    private ProfileFragment profileFragment = new ProfileFragment();
    private QuestAddFragment questAddFragment = new QuestAddFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureCrashReporting();
        setStrictMode();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        nameTextView = headerView.findViewById(R.id.user_name);
        emailTextView = headerView.findViewById(R.id.user_email);
        Menu menu = navigationView.getMenu();
        loginItem = menu.findItem(R.id.nav_login);
        profileItem = menu.findItem(R.id.nav_profile);

        spHelper = SPHelper.getInstance(getApplicationContext());

        setUserInformation();

        if (savedInstanceState != null) {
            currentFragment = (QFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, FRAGMENT_TAG);
        } else {
            changeFragment(qMapFragment, false);
        }
    }

    private void configureCrashReporting() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }

    private void setStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        currentFragment = (QFragment) getSupportFragmentManager().findFragmentById(R.id.content);
        currentFragment.setTitle();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.nav_login:
                changeFragment(authFragment, true);
                break;
            case R.id.nav_map:
                changeFragment(qMapFragment, false);
                break;
            case R.id.nav_profile:
                changeFragment(profileFragment, true);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeFragment(QFragment fragment, boolean addToBackStack) {
        if (currentFragment == fragment) { return; }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.content, fragment, FRAGMENT_TAG);
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        currentFragment = fragment;
        transaction.commit();
        fragment.setTitle();
    }

    public void setUserInformation() {
        UserProfile user = spHelper.getCurrentUser();
        if (user != null) {
            String name = user.getFirstName() + " " + user.getLastName();
            nameTextView.setText(name);

            emailTextView.setVisibility(View.VISIBLE);
            emailTextView.setText(user.getEmail());

            loginItem.setVisible(false);
            profileItem.setVisible(true);
        } else {
            nameTextView.setText(R.string.guest_name);

            emailTextView.setVisibility(View.GONE);

            loginItem.setVisible(true);
            profileItem.setVisible(false);
        }
    }

    @Override
    public void onPointsAdded(Quest quest) {
        Bundle args = new Bundle();
        args.putSerializable(QuestAddFragment.QUEST_ARG, quest);
        questAddFragment.setArguments(args);
        changeFragment(questAddFragment, true);
    }

    @Override
    public void onQuestSaved(Quest quest) {
        Bundle args = new Bundle();
        args.putSerializable(QMapFragment.QUEST_ARG, quest);
        qMapFragment.setArguments(args);
        changeFragment(qMapFragment, false);
    }

    public void hideSoftKeyboard() {
        View view = getCurrentFocus();
        if(view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
