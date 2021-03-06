package com.spear.android.album;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spear.android.R;
import com.spear.android.album.detail.DetailFragment;
import com.spear.android.album.galleryoption.GalleryOptionFragment;
import com.spear.android.album.result.ResultFragment;
import com.spear.android.custom.CustomTypeFace;
import com.spear.android.login.LoginActivity;
import com.spear.android.managers.CameraManager;
import com.spear.android.map.MapActivity;
import com.spear.android.news.NewsActivity;
import com.spear.android.pojo.GalleryCard;
import com.spear.android.pojo.ImageInfo;
import com.spear.android.profile.ProfileActivity;
import com.spear.android.weather.WeatherActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.activeandroid.Cache.getContext;
import static com.google.android.gms.internal.zzt.TAG;
import static com.spear.android.R.id.profile;

public class AlbumActivity extends AppCompatActivity implements View.OnClickListener, AlbumView {

    private VideoView mVV;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 111;
    private AlbumPresenter albumPresenter;
    private RecyclerView recyclerView;
    private AlbumAdapter adapter;
    private List<GalleryCard> cardList;
    private FloatingActionButton fabOpenCamera;
    private static final int appImageCard = 946684800;
    private ImageButton btnOrderByRating, btnOrderByDate;
    private ProgressDialog dialog;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private Menu menu;
    private FragmentManager fm;
    private ArrayList<ImageInfo> imageArray;
    private ActionBar actionBar;
    private GalleryOptionFragment galleryOptionFragment;
    private DetailFragment detailFragment;
    private ResultFragment resultFragment;
    private CameraManager cameraManager;
    private boolean isGalleryOptionShown, isDetailShown;
    private Intent galleryIntent;
    final int hideFragment = 0;
    final int galleryOption = 1;
    final int result = 3;
    final int detail = 4;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        albumPresenter = new AlbumPresenter(this);
        init();
        FullScreencall();
        loadGalleryFirebase();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreencall();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        this.menu = m;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_album, m);

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

    public void FullScreencall() {
        if (Build.VERSION.SDK_INT < 19) {
            //19 or above api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else {
            //for lower api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.signOutItem:
                signOut();
                return true;
            case profile:
                startActivity( new Intent(this, ProfileActivity.class));
                return true;
            case R.id.weathermenu:
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
                return true;
            case R.id.mapmenu:
                startActivity(new Intent(this, MapActivity.class));
                return true;
            case R.id.newsmenu:
                startActivity(new Intent(this, NewsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        if (!isGalleryOptionShown && !isDetailShown) {
            if (view.getId() == R.id.fabOpenCamera) {
                albumPresenter.askForPermissions();
                fabOpenCamera.hide();
                cambiarFragment(galleryOption);

            } else if (view.getId() == R.id.btnOrderByDate) {
                if (imageArray != null) {
                    imageArray = albumPresenter.orderByDate(imageArray);
                    render(imageArray);
                }
            } else if (view.getId() == R.id.btnOrderRating) {
                if (imageArray != null) {
                    imageArray = albumPresenter.orderByRating(imageArray);
                    render(imageArray);
                }
            }

        }
    }


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            galleryIntent = data;
            cameraManager.proccessImage(requestCode, resultCode, data);
        }
    }

    final AlbumView.OnCameraCapture onCameraCapture = new AlbumView.OnCameraCapture() {
        @Override
        public void onSuccess(Bitmap imageBitmap, int requestCode, int resultCode) {
            openResultFragment(imageBitmap, requestCode, resultCode);
        }

        @Override
        public void onError() {

        }

        @Override
        public void hideLoading() {
            dialog.dismiss();
            cambiarFragment(hideFragment);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void loadGalleryFirebase() {

        imageArray = new ArrayList<>();

        databaseReference.getRoot().child("images").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                imageArray.clear();
                cardList.clear();
                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: " + imageSnapshot.getKey());
                    ImageInfo image = imageSnapshot.getValue(ImageInfo.class);
                    imageArray.add(image);
                }
                while (imageArray.size() % 3 != 0) {
                    ImageInfo image2 = new ImageInfo("Spear", 0, appImageCard, "Spear", "https://firebasestorage.googleapis.com/v0/b/spear-e5a6a.appspot.com/o/imagenews%2FCaptura%20de%20pantalla%202017-06-13%20a%20las%200.42.53.png?alt=media&token=5d6d5226-ba3c-4098-9cf0-dc15f3c73600", 0, "Spear");
                    imageArray.add(image2);

                }
                Collections.reverse(imageArray);
                render(imageArray);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    final OnImageClick onImageClick = new OnImageClick() {
        @Override
        public void onSuccess(GalleryCard card) {
            if (!isGalleryOptionShown) {
                fabOpenCamera.hide();
                actionBar.hide();
                cambiarFragment(detail);
                detailFragment.setModel(card);
            }
        }

        @Override
        public void onError() {

        }
    };

    private void render(ArrayList<ImageInfo> imgInfo) {
        cardList.clear();
        GalleryCard card;
        for (ImageInfo imageInfo : imgInfo) {
            card = new GalleryCard(imageInfo.getName(), imageInfo.getRating(), imageInfo.getUrl(), imageInfo.getTimeStamp(), imageInfo.getVoted());
            cardList.add(card);
        }
        adapter.notifyDataSetChanged();
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface typeLibel = Typeface.createFromAsset(getAssets(), "Libel_Suit.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypeFace("" , typeLibel), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    private void init() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        actionBar = getSupportActionBar();
        cardList = new ArrayList<>();
        adapter = new AlbumAdapter(this, getContext(), cardList, onImageClick);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 3);  //displays number of cards per row
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        fabOpenCamera = (FloatingActionButton) findViewById(R.id.fabOpenCamera);
        fabOpenCamera.setOnClickListener(this);
        btnOrderByDate = (ImageButton) findViewById(R.id.btnOrderByDate);
        btnOrderByRating = (ImageButton) findViewById(R.id.btnOrderRating);
        btnOrderByDate.setOnClickListener(this);
        btnOrderByRating.setOnClickListener(this);
        Typeface typeLibel = Typeface.createFromAsset(getAssets(), "Libel_Suit.ttf");
        SpannableStringBuilder typeFaceAction = new SpannableStringBuilder("Gallery");
        typeFaceAction.setSpan(new CustomTypeFace("", typeLibel), 0, typeFaceAction.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        actionBar.setTitle(typeFaceAction);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Subiendo información");
        fm = getSupportFragmentManager();
        galleryOptionFragment = (GalleryOptionFragment) fm.findFragmentById(R.id.galleryOptionFragment);
        resultFragment = (ResultFragment) fm.findFragmentById(R.id.resultFragment);
        detailFragment = (DetailFragment) fm.findFragmentById(R.id.detailFragment);

        cameraManager = new CameraManager(this, onCameraCapture);

        cambiarFragment(hideFragment);

    }

    @Override
    public void openResultFragment(Bitmap imageBitmap, int requestCode, int resultCode) {
        cambiarFragment(result);
        resultFragment.setImageBitmap(imageBitmap, requestCode, resultCode);
    }


    @Override
    public void showLoading() {
        dialog.show();
    }

    @Override
    public void hideLoading() {
        dialog.hide();
    }

    @Override
    public void showError(String s) {
        Toast.makeText(this, "Error exception:" + s, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void openCamera() {
        cameraManager.openCameraIntent();
    }

    @Override
    public void openGallery() {
        cameraManager.openGalleryIntent();
    }

    @Override
    public void pushTofirebase(ImageView image, int requestCode, int resultCode, String comentary) {
        albumPresenter.pushToFirebase(galleryIntent, image, requestCode, resultCode, comentary);
    }

    @Override
    public void pushRatingToFirebase(long timeStamp, float rating) {
        albumPresenter.pushRatingFirebase(timeStamp, rating);
    }

    @Override
    public void setNewDetailRating(float currentRating) {
        detailFragment.setDetailRating(currentRating);
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
    public void cambiarFragment(int ifrg) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        transaction.hide(galleryOptionFragment);
        transaction.hide(resultFragment);
        transaction.hide(detailFragment);

        if (ifrg == galleryOption) {
            isGalleryOptionShown = true;
            transaction.show(galleryOptionFragment);
        } else if (ifrg == hideFragment) {
            isGalleryOptionShown = false;
            isDetailShown = false;
            if (!fabOpenCamera.isShown()) {
                fabOpenCamera.show();
            }
            actionBar.show();
        } else if (ifrg == result) {
            transaction.show(resultFragment);
        } else if (ifrg == detail) {
            isDetailShown = true;
            transaction.show(detailFragment);
        }
        transaction.commit();
    }

}
