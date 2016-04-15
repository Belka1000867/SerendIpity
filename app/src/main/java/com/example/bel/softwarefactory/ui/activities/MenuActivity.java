package com.example.bel.softwarefactory.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.bel.softwarefactory.NavItem;
import com.example.bel.softwarefactory.R;
import com.example.bel.softwarefactory.preferences.UserLocalStore;
import com.example.bel.softwarefactory.ui.adapters.DrawerListAdapter;
import com.example.bel.softwarefactory.ui.fragments.MapFragment;
import com.example.bel.softwarefactory.ui.fragments.RecordFragment;
import com.example.bel.softwarefactory.ui.fragments.RecordingListFragment_;
import com.facebook.CallbackManager;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

@EActivity(R.layout.activity_menu)
public class MenuActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MenuActivity";

    @ViewById
    protected DrawerLayout drawerLayoutMenu;

    @ViewById
    protected ListView listViewMenu;

    @ViewById
    protected RelativeLayout rlMainMenu;

    private ActionBar actionBar;
    private ActionBarDrawerToggle drawerToggle;

    //Layout for user information
    private String[] aTitles;
    private String[] aDescriptions;
    private int mPrevItem = 0;

    private UserLocalStore userLocalStore;

    //facebook call back manager
    private CallbackManager facebookCallbackManager;

    @AfterViews
    protected void afterViews() {
        userLocalStore = new UserLocalStore(MenuActivity.this);

        facebookCallbackManager = new CallbackManager.Factory().create();

        //getting arrays
        aTitles = getResources().getStringArray(R.array.itemTitles);
        aDescriptions = getResources().getStringArray(R.array.itemDescriptions);

        switchFragment(new MapFragment());

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setIcon(R.mipmap.ic_logo_circle);
        }

        fillMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu()");
        MenuItem logOut = menu.findItem(R.id.overflowItemLogOut);

        if (!userLocalStore.isUserLoggedIn()) {
            logOut.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_overflow, menu);

        if (userLocalStore.isUserLoggedIn()) {
            MenuItem item = menu.findItem(R.id.overflowItemLogOut);
            item.setVisible(true);
        }

        if (userLocalStore.isUserLoggedIn()) {
            createUserProfileLayout();
        }

        return true;
    }

    public void fillMenu() {
        Log.d(TAG, "fillMenu()");
        ArrayList<NavItem> aNavItems = new ArrayList<>();
        aNavItems.add(new NavItem(aTitles[0], aDescriptions[0], R.mipmap.ic_map_marker, 0));
        aNavItems.add(new NavItem(aTitles[1], aDescriptions[1], R.mipmap.ic_microphone, 1));
        aNavItems.add(new NavItem(aTitles[2], aDescriptions[2], R.mipmap.ic_note, 2));

        listViewMenu.setOnItemClickListener(this);
        DrawerListAdapter adapter = new DrawerListAdapter(this, aNavItems);
        listViewMenu.setAdapter(adapter);

        Log.d(TAG, "Before Menu Header initialization isLoggedIn: " + userLocalStore.isUserLoggedIn());

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayoutMenu, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                //Toast.makeText(MainActivity.this, "Drawer closed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //Toast.makeText(MainActivity.this, "Drawer opened", Toast.LENGTH_SHORT).show();
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.setHomeAsUpIndicator(R.mipmap.ic_menu);
        drawerLayoutMenu.addDrawerListener(drawerToggle);
    }

    public void createUserProfileLayout() {
        RelativeLayout relativeLayout = new RelativeLayout(this);
        ImageView ivUserPhoto = new ImageView(this);
        TextView tvUserName = new TextView(this);

        //SET PARAMS TO LinearLayout WHERE USER PROFILE INFO is situated
        //set width and height
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //set id to Header in the menu
        relativeLayout.setId(R.id.layoutProfileHeader);
        relativeLayout.setBackgroundColor(Color.BLACK);
        relativeLayout.setGravity(Gravity.CENTER_VERTICAL);
        relativeLayout.setLayoutParams(layoutParams);

        //add profile activity where user can change data if user is not logged in from facebook
        if (!userLocalStore.isFacebookLoggedIn()) {
            relativeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
            });
        }

        //PUSH List View below created layout
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        p.addRule(RelativeLayout.BELOW, R.id.layoutProfileHeader);
        listViewMenu.setLayoutParams(p);

        //SET PARAMS FOR TEXT VIEW with USER PROFILE INFO
        tvUserName.setText(userLocalStore.getUsername());
        tvUserName.setEms(10);
        tvUserName.setTextSize(20);
        tvUserName.setTextColor(Color.WHITE);
        tvUserName.setHeight(150);
        tvUserName.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams paramsUserText = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //paramsUserText.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        paramsUserText.setMarginStart(50);
        tvUserName.setLayoutParams(paramsUserText);
        relativeLayout.addView(tvUserName);

        //SET PARAMS for IMAGE VIEW with USER PROFILE INFO

        if (userLocalStore.isFacebookLoggedIn()) {
            Picasso.with(MenuActivity.this)
                    .load(userLocalStore.getProfilePictureUrl())
                    .error(R.mipmap.ic_user)
                    .into(ivUserPhoto);
        } else {
            ivUserPhoto.setImageResource(R.mipmap.ic_user);
        }

        ivUserPhoto.setContentDescription("Profile photo");
        ivUserPhoto.setBackgroundColor(Color.TRANSPARENT);
        RelativeLayout.LayoutParams paramsUserPhoto = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsUserPhoto.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        paramsUserPhoto.addRule(RelativeLayout.CENTER_VERTICAL);
        paramsUserPhoto.setMarginEnd(20);
        ivUserPhoto.setLayoutParams(paramsUserPhoto);
        relativeLayout.addView(ivUserPhoto);

        //setting params finished

        //Add layout to the header
        rlMainMenu.addView(relativeLayout);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onPostCreate()");
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected()");
        if (drawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
/*            case R.id.overflowItemSettings:
                //setting item in overflow menu
                break;*/
/*            case R.id.overflowItemAbout:
                //about item in overflow menu
                break;*/
            case R.id.overflowItemLogOut:
                if (userLocalStore.isUserLoggedIn()) {
                    //if logged in with facebook - log out
                    if (userLocalStore.isFacebookLoggedIn()) {
                        LoginManager.getInstance().logOut();
                        Log.d("DEBUG", "Log OUT FROM FACEBOOK" + LoginManager.getInstance());
                    }
                    //clean local store with user information
                    userLocalStore.clearUserData();
                    //delete profile header with user name from left side menu
                    rlMainMenu.removeView(findViewById(R.id.layoutProfileHeader));

                    /*
                    * Check if user logout in record fragment, then go to map fragment
                    * */
                    RecordFragment recordFragment = (RecordFragment) getSupportFragmentManager().findFragmentByTag("Record");
                    if (recordFragment != null && recordFragment.isVisible()) {
                        selectItem(0);
                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //change HEADER name to the Item name
        selectItem(position);
    }

    public void selectItem(int position) {
        Fragment menuFragment;
        NavItem currentItem;

        listViewMenu.setItemChecked(position, true);
        currentItem = (NavItem) listViewMenu.getItemAtPosition(position);

        if (mPrevItem != currentItem.getId()) {
            switch (currentItem.getId()) {
                case 0:
                    menuFragment = new MapFragment();
                    switchFragment(menuFragment);
                    if (actionBar != null) {
                        actionBar.setTitle(currentItem.getTitle());
                    }
                    mPrevItem = 0;
                    break;
                case 1:
                    menuFragment = new RecordFragment();

                    if (userLocalStore.isUserLoggedIn()) {
                        switchFragment(menuFragment);
                        if (actionBar != null) {
                            actionBar.setTitle(currentItem.getTitle());
                        }

                        mPrevItem = 1;
                    } else {
                        showLoginRequestDialog();
                    }

                    break;
                case 2:
                    switchFragment(RecordingListFragment_.builder().build());
                    if (actionBar != null) {
                        actionBar.setTitle(currentItem.getTitle());
                    }

                    mPrevItem = 2;

                    break;
            }
        }
        drawerLayoutMenu.closeDrawers();
    }

    private void showLoginRequestDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("To use this function you need to Login");

        alertDialog.setPositiveButton("Go to Login", (dialog, which) -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
        });

        alertDialog.show();
    }

    //facebook methods
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onOptionsItemSelected()");
        //measure installs on your mobile app ads
        //log an app activation event for Facebook
        //Logs 'install' and 'app activate' App Events
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onOptionsItemSelected()");
        //logs 'app deactivate' app event for facebook
        AppEventsLogger.deactivateApp(this);
    }

}
