package com.example.bel.softwarefactory.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;

import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.bel.softwarefactory.entities.LeftMenuItem;
import com.example.bel.softwarefactory.R;
import com.example.bel.softwarefactory.preferences.UserLocalStore;
import com.example.bel.softwarefactory.ui.adapters.DrawerListAdapter;
import com.example.bel.softwarefactory.ui.fragments.MapFragment_;
import com.example.bel.softwarefactory.ui.fragments.RecordFragment;
import com.example.bel.softwarefactory.ui.fragments.RecordFragment_;
import com.example.bel.softwarefactory.ui.fragments.RecordingListFragment_;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Arrays;

@EActivity(R.layout.activity_menu)
public class MenuActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MenuActivity";

    @ViewById
    protected DrawerLayout drawerLayoutMenu;

    @ViewById
    protected ListView listViewMenu;

    @ViewById
    protected LinearLayout leftMenu_layout;

    @ViewById
    protected View profile_layout;
    @ViewById
    protected TextView userName_textView;
    @ViewById
    protected ImageView userPhoto_imageView;

    private ActionBar actionBar;
    private ActionBarDrawerToggle drawerToggle;

    @InstanceState
    protected ArrayList<String> titles;
    @InstanceState
    protected ArrayList<String> descriptions;
    @InstanceState
    protected int prevItem = 0;

    @Bean
    protected UserLocalStore userLocalStore;

    //facebook call back manager
    private CallbackManager facebookCallbackManager;

    @AfterViews
    protected void afterViews() {
        facebookCallbackManager = CallbackManager.Factory.create();

        switchFragment(MapFragment_.builder().build());

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (userLocalStore.isUserLoggedIn()) {
            profile_layout.setVisibility(View.VISIBLE);
            userName_textView.setText(userLocalStore.getUser().getUsername());
            Picasso.with(MenuActivity.this)
                    .load(userLocalStore.getProfilePictureUrl())
                    .error(R.mipmap.ic_user)
                    .into(userPhoto_imageView);
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
        return true;
    }

    public void fillMenu() {
        Log.d(TAG, "fillMenu()");
        titles = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.itemTitles)));
        descriptions = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.itemDescriptions)));

        ArrayList<LeftMenuItem> leftMenuItems = new ArrayList<>();
        leftMenuItems.add(new LeftMenuItem(titles.get(0), descriptions.get(0), R.mipmap.ic_map_marker, 0));
        leftMenuItems.add(new LeftMenuItem(titles.get(1), descriptions.get(1), R.mipmap.ic_microphone, 1));
        leftMenuItems.add(new LeftMenuItem(titles.get(2), descriptions.get(2), R.mipmap.ic_note, 2));

        listViewMenu.setOnItemClickListener(this);
        DrawerListAdapter adapter = new DrawerListAdapter(this, leftMenuItems);
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

    @Click(R.id.profile_layout)
    protected void profile_layout_click() {
        if (!userLocalStore.isFacebookLoggedIn()) {
            ProfileActivity_.intent(MenuActivity.this).start();
        }
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
                    FirstActivity_.intent(MenuActivity.this).start();
                    finish();
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
        if (actionBar != null) {
            actionBar.setTitle(titles.get(position));
        }
        selectItem(position);
    }

    public void selectItem(int position) {
        LeftMenuItem currentItem;
        listViewMenu.setItemChecked(position, true);
        currentItem = (LeftMenuItem) listViewMenu.getItemAtPosition(position);

        if (prevItem != currentItem.getId()) {
            switch (currentItem.getId()) {
                case 0:
                    switchFragment(MapFragment_.builder().build());
                    prevItem = 0;
                    break;
                case 1:
                    if (userLocalStore.isUserLoggedIn()) {
                        switchFragment(RecordFragment_.builder().build());
                        prevItem = 1;
                    } else {
                        showLoginRequestDialog();
                    }

                    break;
                case 2:
                    switchFragment(RecordingListFragment_.builder().build());
                    prevItem = 2;
                    break;
            }
        }
        drawerLayoutMenu.closeDrawers();
    }

    private void showLoginRequestDialog() {
        new MaterialDialog.Builder(this)
                .title("To use this function you need to Login")
                .positiveText("Go to Login")
                .negativeText("Cancel")
                .onPositive((dialog, which) -> LoginActivity_.intent(MenuActivity.this).start())
                .show();
    }

    //facebook methods
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
