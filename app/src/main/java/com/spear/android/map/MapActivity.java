package com.spear.android.map;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.spear.android.R;
import com.spear.android.album.AlbumActivity;
import com.spear.android.custom.CustomTypeFace;
import com.spear.android.login.LoginActivity;
import com.spear.android.map.map.MapFragment;
import com.spear.android.map.menu.MapMenuFragment;
import com.spear.android.map.poi.PoiFragment;
import com.spear.android.news.NewsActivity;
import com.spear.android.pojo.PoiInfo;
import com.spear.android.profile.ProfileActivity;
import com.spear.android.weather.WeatherActivity;

import java.util.Map;

public class MapActivity extends AppCompatActivity implements MapView {


    private MapFragment mapFragment;
    private MapMenuFragment mapMenuFragment;
    private PoiFragment poiFragment;
    private FragmentManager fm;
    private Menu menu;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    private MapPresenter mapPresenter;
    private boolean mapMenuOpen;
    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private static final int map = 1;
    private static final int mapmenu = 3;
    private static final int poi = 4;
    private static final int hideFragments = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        init();
        cambiarFragment(hideFragments);

    }

    private void init() {
        fm = getSupportFragmentManager();
        mapFragment = (MapFragment) fm.findFragmentById(R.id.mapFragment);
        mapMenuFragment = (MapMenuFragment) fm.findFragmentById(R.id.mapMenuFragment);
        poiFragment = (PoiFragment) fm.findFragmentById(R.id.poiFragment);
        actionBar = getSupportActionBar();
        mapPresenter = new MapPresenter(this);
        Typeface typeLibel = Typeface.createFromAsset(getAssets(), "Libel_Suit.ttf");
        SpannableStringBuilder typeFaceAction = new SpannableStringBuilder("Map");
        typeFaceAction.setSpan(new CustomTypeFace("", typeLibel), 0, typeFaceAction.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        actionBar.setTitle(typeFaceAction);

        firebaseAuth = FirebaseAuth.getInstance();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        this.menu = m;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_map, menu);
        for (int i=0;i<menu.size();i++) {
            MenuItem mi = menu.getItem(i);
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu!=null && subMenu.size() >0 ) {
                for (int j=0; j <subMenu.size();j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.signOutItemmap:
                signOut();
                return true;
            case R.id.profilemap:
                startActivity( new Intent(this, ProfileActivity.class));
                return true;
            case android.R.id.home:

                return true;
            case R.id.newsmap:
                startActivity(new Intent(this, NewsActivity
                        .class));
                return true;
            case R.id.weathermap:
                startActivity(new Intent(this, WeatherActivity
                        .class));
                return true;
            case R.id.gallerymap:
                startActivity(new Intent(this, AlbumActivity
                        .class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void cambiarFragment(int ifrg) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if (ifrg == map) {
            transaction.show(mapFragment);
        }  else if (ifrg == hideFragments) {
            mapMenuOpen = false;
            mapFragment.disableLocationMenuChange = false;
            transaction.hide(poiFragment);
            transaction.hide(mapMenuFragment);
        } else if (ifrg == mapmenu) {
            mapMenuOpen = true;
            transaction.show(mapMenuFragment);
        }else if (ifrg == poi){
            transaction.show(poiFragment);
        }
        transaction.commit();
    }

    @Override
    public void pushGeoPoint(String latitude, String longitude, String description) {
        mapPresenter.pushGeoPoint(latitude,longitude,description);
    }


    @Override
    public void setGeoCordsMenu(String lat, String lon) {
        mapMenuFragment.setGeoCoords(lat,lon);
    }

    @Override
    public void loadGeoPoints() {
        mapPresenter.loadGeoCords();
    }

    @Override
    public void setGeoCoords(Map<String,Object> poiList) {
        mapFragment.addPoiListMap(poiList);
    }

    @Override
    public void setDataPoi(PoiInfo poi) {
       poiFragment.setDataPoi(poi);
    }

    @Override
    public void deletePoi(String timestamp) {
        mapPresenter.deletePoi(timestamp);
    }



    @Override
    public void closeMenuDialog() {
        mapMenuFragment.closeDialog();
    }

    @Override
    public void showError(String s) {
        Toast.makeText(this, "Error: "+ s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notChangeCoords() {
        if (mapFragment != null){
            mapFragment.disableLocationMenuChange = true;
        }

    }

    @Override
    public void changeCoords() {
        if (mapFragment != null){
            mapFragment.disableLocationMenuChange = false;
        }

    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface typeLibel = Typeface.createFromAsset(getAssets(), "Libel_Suit.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypeFace("" , typeLibel), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }
    @Override
    public boolean checkIfMapMenuisOpen() {
        return mapMenuOpen;
    }

    private void initLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void signOut() {
        firebaseAuth.signOut();
        initLogin();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PETICION_PERMISO_LOCALIZACION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                mapFragment.permissionGranted();



            } else {
                mapFragment.permissionDenied();
            }
        }
    }


}
