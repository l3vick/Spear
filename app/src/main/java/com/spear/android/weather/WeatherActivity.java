package com.spear.android.weather;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.spear.android.R;
import com.spear.android.album.AlbumActivity;
import com.spear.android.custom.CustomTypeFace;
import com.spear.android.login.LoginActivity;
import com.spear.android.managers.SQLliteManager;
import com.spear.android.map.MapActivity;
import com.spear.android.news.NewsActivity;
import com.spear.android.pojo.WeatherResponse;
import com.spear.android.profile.ProfileActivity;
import com.spear.android.weather.search.SearchFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static com.spear.android.R.id.profile;

public class WeatherActivity extends AppCompatActivity implements WeatherView, View.OnClickListener {


    private ImageView imgWeather, imgCardinal;
    private TextView txtHumidity, txtPressure, txtTemperature, txtDate,
            txtDescription, txtWindVel, txtSunrise, txtSunset, txtCity;
    private FloatingActionButton fabOpenSearchView;
    private WeatherPresenter weatherPresenter;
    private SearchFragment searchFragment;
    private FragmentManager fm;
    private Menu menu;
    private SQLliteManager dataManager;
    private SQLiteDatabase db;
    private WeatherResponse data;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    final int hideFragment = 0;
    final int search = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather);


        weatherPresenter = new WeatherPresenter(this);

        init();

        data = checkIfDataExist();


        if (data != null) {
            setDataResponse(data, data.getDate());
            cambiarFragment(hideFragment);
        } else {
            fabOpenSearchView.hide();
            cambiarFragment(1);
        }


    }


    private WeatherResponse checkIfDataExist() {
        WeatherResponse dataAux = null;
        db = dataManager.getWritableDatabase();
        if (db != null) {
            String selectQuery = "SELECT  * FROM " + "weather_response";
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                dataAux = new WeatherResponse();
                do {
                    Log.v("Cursor", "" + cursor.getString(0) + cursor.getString(1) + cursor.getString(2) + cursor.getString(3) + cursor.getString(4) + cursor.getString(5) + " sun: " + cursor.getString(6) + " sun: " + cursor.getString(7) + " deg: " + cursor.getString(8) + " icon: " + cursor.getString(9));
                    dataAux.setDate(cursor.getString(0));
                    dataAux.setDescription(cursor.getString(1));
                    dataAux.setPressure(Long.parseLong(cursor.getString(2)));
                    dataAux.setHumidity(Double.parseDouble(cursor.getString(3)));
                    dataAux.setTemperature(Double.parseDouble(cursor.getString(4)));
                    dataAux.setSpeed(Float.parseFloat(cursor.getString(5)));
                    dataAux.setSunrise(Long.parseLong(cursor.getString(6)));
                    dataAux.setSunset(Long.parseLong(cursor.getString(7)));
                    dataAux.setDeg(Float.parseFloat(cursor.getString(8)));
                    dataAux.setIcon(cursor.getString(9));
                    dataAux.setName(cursor.getString(10));

                } while (cursor.moveToNext());
            }
        }

        return dataAux;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        this.menu = m;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_weather, menu);
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
            case R.id.signOutItem:
                signOut();
                return true;
            case profile:
                startActivity( new Intent(this, ProfileActivity.class));
                return true;
            case R.id.newsmenu:
                startActivity(new Intent(this, NewsActivity
                        .class));
                return true;
            case R.id.mapmenu:
                startActivity(new Intent(this, MapActivity
                        .class));
                return true;
            case R.id.gallerymenu:
                startActivity(new Intent(this, AlbumActivity
                        .class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void init() {
        Typeface typeLibel = Typeface.createFromAsset(getAssets(), "Libel_Suit.ttf");
        dataManager = new SQLliteManager(this, "spear", null, 1);
        db = dataManager.getWritableDatabase();
        fm = getSupportFragmentManager();
        searchFragment = (SearchFragment) fm.findFragmentById(R.id.weatherSearchFrag);
        txtCity = (TextView) findViewById(R.id.txtCity);
        txtDate = (TextView) findViewById(R.id.txtDate);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        txtHumidity = (TextView) findViewById(R.id.txtHumidity);
        txtPressure = (TextView) findViewById(R.id.txtPressure);
        txtTemperature = (TextView) findViewById(R.id.txtTemperature);
        txtSunrise = (TextView) findViewById(R.id.txtSunrise);
        txtSunset = (TextView) findViewById(R.id.txtSunset);
        txtWindVel = (TextView) findViewById(R.id.txtWindVel);
        imgCardinal = (ImageView) findViewById(R.id.imgCardinal);
        imgWeather = (ImageView) findViewById(R.id.imgWeather);
        fabOpenSearchView = (FloatingActionButton) findViewById(R.id.fabSearchView);
        txtCity.setTypeface(typeLibel);
        txtDate.setTypeface(typeLibel);
        txtDescription.setTypeface(typeLibel);
        txtHumidity.setTypeface(typeLibel);
        txtPressure.setTypeface(typeLibel);
        txtTemperature.setTypeface(typeLibel);
        txtSunrise.setTypeface(typeLibel);
        txtSunset.setTypeface(typeLibel);
        txtWindVel.setTypeface(typeLibel);
        fabOpenSearchView.setOnClickListener(this);
        actionBar = getSupportActionBar();
        SpannableStringBuilder typeFaceAction = new SpannableStringBuilder("Weather");
        typeFaceAction.setSpan(new CustomTypeFace("", typeLibel), 0, typeFaceAction.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        actionBar.setTitle(typeFaceAction);
        firebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void setWeatherResult(WeatherResponse weatherResult) {

        cambiarFragment(hideFragment);
        WeatherResponse result = weatherResult;
        if (result != null) {
            Date date = new Date();
            SimpleDateFormat fmtOut = new SimpleDateFormat("dd-MM-yyyy");
            String dateStr = fmtOut.format(date);
            saveWeatherResultToDatabase(result, dateStr);
            setDataResponse(result, dateStr);
        }
    }

    private void saveWeatherResultToDatabase(WeatherResponse weatherResult, String dateStr) {
        db = dataManager.getWritableDatabase();
        if (db != null) {
            db.execSQL("INSERT INTO weather_response (date, description,pressure,humidity,temperature ,windvel ,sunrise,sunset,deg,icon,city) " +
                    "VALUES ('" + dateStr + "', '" + weatherResult.getDescription() + "' , '" + weatherResult.getPressure() + "','" + weatherResult.getHumidity() + "','" + weatherResult.getTemperature() + "','" + weatherResult.getSpeed() + "','" + weatherResult.getSunrise() + "','" + weatherResult.getSunset() + "','" + weatherResult.getDeg() + "','" + weatherResult.getIcon() + "','" + weatherResult.getName() + "')");


        }
    }

    private void setDataResponse(WeatherResponse response, String dateStr) {

        if (response.getIcon() != null){
            imgWeather.setImageResource(weatherPresenter.convertIconToImage(response.getIcon()));
        }else{
            imgWeather.setImageResource(R.mipmap.error);
        }

        if (!Float.isNaN(response.getDeg()) || response.getDeg() != 0){
            imgCardinal.setImageResource(weatherPresenter.convertDegToImage(response.getDeg()));
        }else{
            imgCardinal.setImageResource(R.mipmap.error);
        }

        if (dateStr != null) {
            txtDate.setText(dateStr);
        } else {
            txtDate.setText("N/A");
        }

        if (response.getDescription() != null) {
            txtDescription.setText(response.getDescription());
        } else {
            txtDescription.setText("N/A");
        }

        if (response.getPressure() != 0) {
            txtPressure.setText(String.valueOf(response.getPressure()) + "hPa");
        } else {
            txtPressure.setText("N/A");
        }


        if (response.getHumidity() != 0) {
            txtHumidity.setText(String.valueOf(response.getHumidity()) + "%");
        } else {
            txtHumidity.setText("N/A");
        }

        if (response.getTemperature() != 0) {
            int temp = (int) response.getTemperature();
            txtTemperature.setText(String.valueOf(temp) + "ºC");
        } else {
            txtTemperature.setText("N/A");
        }


        if (response.getSpeed() != 0) {
            txtWindVel.setText(convertToKMH(response.getSpeed()));
        } else {
            txtWindVel.setText("N/A");
        }

        if (response.getSunrise() != 0) {
            String sunrise = timeStampToDate(response.getSunrise());

            sunrise = sunrise.substring(10);
            txtSunrise.setText(sunrise);

        } else {
            txtSunrise.setText("N/A");
        }

        if (response.getSunset() != 0) {

            String sunset = timeStampToDate(response.getSunset());

            sunset = sunset.substring(10);
            txtSunset.setText(sunset);

        } else {
            txtSunset.setText("N/A");
        }

        if (response.getName() != null) {
            txtCity.setText(response.getName());
        } else {
            txtCity.setText("N/A");
        }
        data = checkIfDataExist();
    }

    private String convertToKMH(float speed) {
        String kmh = String.format("%.2f", speed * 1.609344f) + " KM/H";
        return kmh;
    }

    public String timeStampToDate(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();

            calendar.setTimeInMillis(timestamp * 1000);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        } catch (Exception e) {
        }
        return "";
    }

    public String getCurrentTimezoneOffset() {

        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

        String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000),
                Math.abs((offsetInMillis / 60000) % 60));
        offset = "GMT" + (offsetInMillis >= 0 ? "+" : "-") + offset;

        return offset;
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
    public void showError(String error) {
        Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
    }

    public void cambiarFragment(int ifrg) {
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.hide(searchFragment);
        if (ifrg == search) {
            transaction.show(searchFragment);
        } else if (ifrg == hideFragment) {
            if (!fabOpenSearchView.isShown()) {
                fabOpenSearchView.show();
            }

        }
        transaction.commit();
    }

    @Override
    public void getWeatherResponse(String cityZip) {
        weatherPresenter.getWeatherResponse(cityZip);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fabSearchView) {
            cambiarFragment(1);
            fabOpenSearchView.hide();
        }

    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface typeLibel = Typeface.createFromAsset(getAssets(), "Libel_Suit.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypeFace("" , typeLibel), 0 , mNewTitle.length(),  Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }
}