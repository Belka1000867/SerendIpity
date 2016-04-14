package com.example.bel.softwarefactory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Bel on 22.02.2016.
 */
public class MenuActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView listView;
    //Layout for user information
    private RelativeLayout rlMainMenu;

    private String[] aTitles;
    private String[] aDescriptions;
    private int mPrevItem = 0;
    private ImageView ivUserPhoto;

    private UserLocalStore userLocalStore;

    private FragmentManager menuFragmentManager;

    //facebook call back manager
    private CallbackManager facebookCallbackManager;

    private static final String TAG = "Debug_Menu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        FacebookSdk.sdkInitialize(getApplicationContext());
        facebookCallbackManager = new CallbackManager.Factory().create();
        setContentView(R.layout.menu);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayoutMenu);
        listView = (ListView) findViewById(R.id.listViewMenu);
        rlMainMenu = (RelativeLayout) findViewById(R.id.rlMainMenu);
        //getting arrays
        aTitles = getResources().getStringArray(R.array.itemTitles);
        aDescriptions = getResources().getStringArray(R.array.itemDescriptions);

        //get reference to local store
        userLocalStore = new UserLocalStore(this);

        if(savedInstanceState == null){
            menuFragmentManager = getSupportFragmentManager();
            menuFragmentManager.beginTransaction()
                    .add(R.id.frameLayoutMainContent, new Map())
                    .addToBackStack(null)
                    .commit();
        }

        fillMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu()");
        MenuItem logOut = menu.findItem(R.id.overflowItemLogOut);

        if(!userLocalStore.isUserLoggedIn()){
            logOut.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_overflow, menu);

        if(userLocalStore.isUserLoggedIn()){
            MenuItem item = menu.findItem(R.id.overflowItemLogOut);
            item.setVisible(true);
        }

        if(userLocalStore.isUserLoggedIn()) {
            createUserProfileLayout();
        }

        return true;
    }

    public void fillMenu(){
        Log.d(TAG, "fillMenu()");
        ArrayList<NavItem> aNavItems = new ArrayList<>();
        aNavItems.add(new NavItem(aTitles[0], aDescriptions[0], R.mipmap.ic_map_marker, 0));
        aNavItems.add(new NavItem(aTitles[1], aDescriptions[1], R.mipmap.ic_microphone, 1));
        aNavItems.add(new NavItem(aTitles[2], aDescriptions[2], R.mipmap.ic_note, 2));

        listView.setOnItemClickListener(this);
        DrawerListAdapter adapter = new DrawerListAdapter(this, aNavItems);
        listView.setAdapter(adapter);

        Log.d(TAG, "Before Menu Header initialization isLoggedIn: " + userLocalStore.isUserLoggedIn());

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close){
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
        drawerLayout.addDrawerListener(drawerToggle);


        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setIcon(R.mipmap.ic_logo_circle);
        }

    }

    public void createUserProfileLayout(){
        RelativeLayout relativeLayout = new RelativeLayout(this);
        ivUserPhoto = new ImageView(this);
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
        if(!userLocalStore.isFacebookLoggedIn()) {
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(intent);
                }
            });
        }

        //PUSH List View below created layout
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        p.addRule(RelativeLayout.BELOW, R.id.layoutProfileHeader);
        listView.setLayoutParams(p);

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

        if(userLocalStore.isFacebookLoggedIn()){
            new GetProfilePicture().execute();
        }
        else {
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
        if(drawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()){
/*            case R.id.overflowItemSettings:
                //setting item in overflow menu
                break;*/
/*            case R.id.overflowItemAbout:
                //about item in overflow menu
                break;*/
            case R.id.overflowItemLogOut:
                if(userLocalStore.isUserLoggedIn()) {
                    //if logged in with facebook - log out
                    if(userLocalStore.isFacebookLoggedIn())
                    {
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
                    if(recordFragment != null && recordFragment.isVisible()){
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

    public void selectItem(int position){
        Fragment menuFragment;
        int mCurCheckPosition = position;
        NavItem currentItem;
        menuFragmentManager = getSupportFragmentManager();

        listView.setItemChecked(mCurCheckPosition, true);
        currentItem = (NavItem) listView.getItemAtPosition(mCurCheckPosition);

        if(mPrevItem != currentItem.id) {
            switch (currentItem.id) {
                case 0:
                    menuFragment = new Map();
                    menuFragmentManager.beginTransaction()
                            .replace(R.id.frameLayoutMainContent, menuFragment, "Map")
                            .addToBackStack(null)
                            .commit();
                    try {
                        getSupportActionBar().setTitle(currentItem.title);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mPrevItem = 0;
                    break;
                case 1:
                    menuFragment = new RecordFragment();

                    if (userLocalStore.isUserLoggedIn()) {
                        menuFragmentManager.beginTransaction()
                                .replace(R.id.frameLayoutMainContent, menuFragment, "Record")
                                .addToBackStack(null)
                                .commit();
                        try {
                            getSupportActionBar().setTitle(currentItem.title);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mPrevItem = 1;
                    } else {
                        showLoginRequestDialog();
                    }

                    break;
                case 2:
                    menuFragment = new RecordingListFragment();
                    menuFragmentManager.beginTransaction()
                            .replace(R.id.frameLayoutMainContent, menuFragment, "RecordList")
                            .addToBackStack(null)
                            .commit();
                    try {
                        getSupportActionBar().setTitle(currentItem.title);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mPrevItem = 2;

                    break;
            }
        }
        drawerLayout.closeDrawers();
    }

    private void showLoginRequestDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("To use this function you need to Login");

        alertDialog.setPositiveButton("Go to Login", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
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

    /*
    * Asynchronous class to get profile picture from the internet and set to the header in menu
    * */
    private class GetProfilePicture extends AsyncTask <Void, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap profilePicture;
            try {
                URL imageURL = new URL(userLocalStore.getProfilePictureUrl());
                profilePicture = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                return profilePicture;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap!=null)
                ivUserPhoto.setImageBitmap(bitmap);
            else
                ivUserPhoto.setImageResource(R.mipmap.ic_user);
        }
    }
}
