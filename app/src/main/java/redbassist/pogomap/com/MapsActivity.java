package redbassist.pogomap.com;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.Manifest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.text.Text;
import com.google.gson.Gson;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;
import com.pokegoapi.auth.GoogleAuthTokenJson;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.auth.GoogleLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import io.realm.Realm;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import redbassist.pogomap.com.Login.GoogleAuthToken;
import redbassist.pogomap.com.Login.GoogleLoginActivity;
import redbassist.pogomap.com.Utilities.Settings;
import redbassist.pogomap.com.pokemongomap.R;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

import redbassist.pogomap.com.Pokemon.*;
import redbassist.pogomap.com.Utilities.Util;
import redbassist.pogomap.com.Login.LoginHelper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnConnectionFailedListener {

    LoginHelper loginHelper;
    Settings settings;
    String code;
    Util utils;

    private DrawerLayout drawerLayout;

    boolean startZoomedToUser = false;

    //start screen ui pieces
    LinearLayout loginContainer;
    LinearLayout login_select;
    ImageView titlemark;
    ImageView titleball;
    ImageView sscreen;

    Button btnLogin;
    Button btnRegister;

    //used for the logging in of the user
    EditText et_username;
    EditText et_password;
    String username;
    String password;
    boolean loggedIn;
    boolean googleLoggedIn;
    GoogleApiClient mGoogleApiClient;
    SignInButton btn_googleLogin;
    private final static int RC_SIGN_IN = 1;
    private Intent signInIntent;
    private String server_client_id = "178957806894-dm4lq4vv4c3dpp9gak6veqo6nqs7u87f.apps.googleusercontent.com";
    private String token;

    Button btn_ptc_login;

    //current location
    private LatLng loc;
    private GoogleMap mMap;
    SharedPreferences localSharedPrefs;

    //map target info
    int scanRadius = 400;
    boolean target = false;
    Marker targetMarker;
    Circle targetCircle;
    Button scanBtn;
    ImageButton btn_settings;
    boolean firstScan = false;
    boolean scanning = false;
    AsyncTask loader;

    int highlightedID = 0;

    final ArrayList<String> options = new ArrayList<>();

    Snackbar snackbar;

    //container for seen pokemon
    List<Pokemon> seenPokemon = Collections.synchronizedList(new ArrayList<Pokemon>());
    ArrayList<LatLng> scanPoints = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        settings = new Settings(this);
        settings.LoadPreferences();
        scanRadius = settings.distance;

        localSharedPrefs = getSharedPreferences(getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
        snackbar = Snackbar.make(findViewById(R.id.mainLayout), "Created Snackbar", Snackbar.LENGTH_SHORT);

        loginHelper = new LoginHelper(this, localSharedPrefs, snackbar);
        utils = new Util(this);

        SetupUI();
        GoogleLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings.LoadPreferences();
        scanRadius = settings.distance;

        if (settings.clearPokemon) {
            ClearPokemonMarkers();
            settings.clearPokemon = false;
            settings.SavePreferences();
        }
    }

    private void GoogleLogin() {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken(server_client_id)
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // ...
    }

    private void signIn() {
        googleLoggedIn = localSharedPrefs.getBoolean("googleLoggedIn", false);

        Intent intent = new Intent(this, GoogleLoginActivity.class);
        startActivityForResult(intent, 1300);

       /* if (!googleLoggedIn) {
            Intent intent = new Intent(this, GoogleLoginActivity.class);
            startActivityForResult(intent, 1300);
        }
        else {
            token = localSharedPrefs.getString("userToken", "null");
            snackbar.setText("Login Successful");
            snackbar.setDuration(1000);
            snackbar.show();
            loginHelper.ShowMap();
            utils.fadeOutAndHide(login_select);
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        code = data.getStringExtra(GoogleLoginActivity.EXTRA_CODE);
        if (code != null) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
            CheckGoogleAuth();
    }

    private void SetupUI() {
        ViewGroup group = (ViewGroup) snackbar.getView();
        group.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.notificationColor));

        loginContainer = (LinearLayout)findViewById(R.id.cnt_login);
        login_select = (LinearLayout)findViewById(R.id.login_select);
        sscreen = (ImageView)findViewById(R.id.splashscreen);
        titleball = (ImageView)findViewById(R.id.titleball);
        titlemark = (ImageView)findViewById(R.id.titlemark);

        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loginHelper.HandleNewLogin();
            }
        });

        btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.parse("https://club.pokemon.com/us/pokemon-trainer-club/sign-up/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);

        btn_settings = (ImageButton) findViewById(R.id.btn_settings);
        btn_settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent settingsIntent = new Intent(MapsActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        scanBtn = (Button) findViewById(R.id.scanButton);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!target) {
                    targetMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.targeticon))
                            .position(loc)
                            .alpha(0.8f));

                    targetCircle = mMap.addCircle(new CircleOptions()
                            .center(loc)   //set center
                            .radius(scanRadius)   //set radius in meters
                            .fillColor(Color.argb(50, 57, 234, 254))  //default
                            .strokeColor(0x10000000)
                            .strokeWidth(5)
                    );
                    target = true;
                }

                if (!firstScan) {
                    Observable.interval(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Long>() {
                                @Override
                                public void call(Long aLong) {
                                    UpdatePokemonMarkers();
                                }
                            });
                }

                createScanMap(targetMarker.getPosition(), 20, scanRadius);
                StartScan();
                loader = new loadPokemon().execute();
            }
        });

        btn_ptc_login = (Button) findViewById(R.id.btn_ptc_login);
        btn_ptc_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loginHelper.LoginUsingPTC();
                utils.fadeOutAndHide(findViewById(R.id.login_select));
            }
        });

        btn_googleLogin = (SignInButton) findViewById(R.id.btn_google_login);
        btn_googleLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signIn();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, 1400);
        }
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);

        SetupMapInteractions();
    }

    //map clicks etc.
    private void SetupMapInteractions() {
        //dropping the target
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng)
            {
                findViewById(R.id.highlight).setVisibility(View.GONE);
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (!scanning) {
                    if (targetMarker != null)
                        targetMarker.remove();
                    if (targetCircle != null)
                        targetCircle.remove();

                    targetMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.targeticon))
                            .position(latLng)
                            .alpha(0.8f));

                    targetCircle = mMap.addCircle(new CircleOptions()
                            .center(latLng)   //set center
                            .radius(scanRadius)   //set radius in meters
                            .fillColor(Color.argb(50, 57, 234, 254))  //default
                            .strokeColor(0x10000000)
                            .strokeWidth(5)
                    );
                    target = true;
                }
                //fadeInAndShowButton(scanBtn);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                marker.hideInfoWindow();

                int id = Integer.parseInt(marker.getTitle());

                synchronized (seenPokemon) {
                    for (Pokemon markerPoke : seenPokemon) {
                        if (markerPoke.getEncounterid() == id) {

                            String uri = "p" + markerPoke.getNumber();
                            int resourceID = getResources().getIdentifier(uri, "drawable", getPackageName());

                            ImageView icon = (ImageView)findViewById(R.id.highlighticon);
                            icon.setImageResource(resourceID);

                            String name = markerPoke.getName();
                            TextView txtname = (TextView)findViewById(R.id.highlightname);
                            txtname.setText(name);

                            highlightedID = id;

                            TextView txttime = (TextView)findViewById(R.id.highlighttime);
                            txttime.setText(marker.getSnippet());

                            findViewById(R.id.highlight).setVisibility(View.VISIBLE);
                        }
                    }
                }

                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1400: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
            }
        }
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            loc = new LatLng(location.getLatitude(), location.getLongitude());

            if (!startZoomedToUser) {
                startZoomedToUser = true;
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(loc, 15);
                mMap.animateCamera(cameraUpdate);

            }
        }
    };

    private void CheckGoogleAuth() {
        CheckGoogleLogin().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loginHelper.FailedLogin();
                    }

                    @Override
                    public void onNext(RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
                        if (authInfo != null) {
                            if (authInfo.hasToken()) {

                                SharedPreferences.Editor editPrefs = localSharedPrefs.edit();
                                if (!googleLoggedIn) {
                                    editPrefs.putBoolean("googleLoggedIn", true);
                                    editPrefs.putString("userToken", loginHelper.getGoogleToken().getId_token());
                                    editPrefs.apply();
                                }
                                snackbar.setText("Login Successful");
                                snackbar.setDuration(1000);
                                snackbar.show();
                                loginHelper.ShowMap();
                                utils.fadeOutAndHide(login_select);
                            }
                        }
                    }
                });
    }

    //used for handling the request for auth token by the user using PTC
    public Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo> CheckGoogleLogin() {
        return Observable.defer(new Func0<Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo>>() {
            @Override public Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo> call() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    GoogleLogin googleLogin = new GoogleLogin(client);

                    RequestBody body = new FormBody.Builder()
                            .add("code", code)
                            .add("client_id", GoogleLogin.CLIENT_ID)
                            .add("client_secret", GoogleLogin.SECRET)
                            .add("redirect_uri", "http://127.0.0.1:9004")
                            .add("grant_type", "authorization_code")
                            .build();
                    Request req = new Request.Builder()
                            .url(GoogleLogin.OAUTH_TOKEN_ENDPOINT)
                            .method("POST", body)
                            .build();
                    Response response = client.newCall(req).execute();

                    GoogleAuthTokenJson token = new Gson().fromJson(response.body().string(), GoogleAuthTokenJson.class);
                    GoogleAuthToken token2 = new GoogleAuthToken(token);
                    RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo auth = googleLogin.login(token2.getId_token());

                    loginHelper.setGoogleToken(token);

                    return Observable.just(auth);

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
    }

    public void createScanMap(LatLng loc, int n, int radius) {

        int numberLoops = (int)((radius / 150) + 1);

        double distance1 = (double)radius / 100000;

        double circleDistance;

        scanPoints.clear();
        scanPoints.add(loc);

        for (int i = 0; i < numberLoops; i++) {
            circleDistance = (distance1 * (double)(i+1)) / (double)numberLoops;
            for (int j = 0; j < n; j++) {
                double t = 2 * Math.PI * j / n;
                double x = (loc.latitude + circleDistance * Math.cos(t));
                double y = (loc.longitude + circleDistance * Math.sin(t));

                LatLng scanPoint = new LatLng(x, y);

                scanPoints.add(scanPoint);
            }
        }
    }

    private class loadPokemon extends AsyncTask<String, List<CatchablePokemon>, String> {
        int pos = 1;

        @Override
        protected String doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient();
            PokemonGo go;
            RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo;
            if (loginHelper.PTCLogin)
                go = new PokemonGo(loginHelper.GetPTCAuth(), client);
            else {
                GoogleLogin googleLogin = new GoogleLogin(client);
                authInfo = googleLogin.login(loginHelper.getGoogleToken().getId_token());
                go = new PokemonGo(authInfo, client);
            }
            for (LatLng loc : scanPoints) {
                try {
                    go.setLongitude(loc.longitude);
                    go.setLatitude(loc.latitude);
                    Map map = new Map(go);
                    List<CatchablePokemon> catchablePokemon = map.getCatchablePokemon();
                    publishProgress(catchablePokemon);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (RemoteServerException e) {
                    //Toast.makeText(MapsActivity.this, "Remote Server Error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (LoginFailedException e) {
                    //Toast.makeText(MapsActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            ScanComplete();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(List<CatchablePokemon>... objects) {
            if (objects != null) {
                if (objects.length < 1) return;
                List<CatchablePokemon> object = objects[0];
                AddPokemon(object);
            }else {
                //Toast.makeText(MapsActivity.this, "Connection Error (Servers might be down)", Toast.LENGTH_SHORT).show();
            }
            pos++;
        }
    }

    private void StartScan() {
        utils.fadeOutAndHide(scanBtn);
        utils.fadeOutAndHide(btn_settings);
        btn_settings.setEnabled(false);
        scanBtn.setEnabled(false);
        snackbar.setText("NOW SCANNING AREA...");
        snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        scanning = true;
    }

    private void ScanComplete() {
        scanning = false;
        utils.fadeInAndShow(scanBtn);
        utils.fadeInAndShow(btn_settings);
        scanBtn.setEnabled(true);
        btn_settings.setEnabled(true);
        target = false;
        targetMarker.remove();
        targetCircle.remove();
        snackbar.dismiss();
    }

    private void AddPokemon(List<CatchablePokemon> seenPokes) {
        for (CatchablePokemon p : seenPokes) {

            synchronized (seenPokemon) {
                if (!CheckPokemonExists((int)p.getEncounterId())) {
                    String name = p.getPokemonId().toString();
                    name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

                    DateTime expires = new DateTime(p.getExpirationTimestampMs());

                    Pokemon temp = new Pokemon(p.getPokemonId().getNumber(), name, (int) p.getEncounterId(), expires, p.getExpirationTimestampMs(), Integer.toString(p.getPokemonId().getNumber()));
                    createMarker(p, temp);
                        seenPokemon.add(temp);
                }
                else {
                    int poop = 0;
                }
            }
        }
    }

    private boolean CheckPokemonExists(int encID) {
        for (Pokemon p: seenPokemon) {
            if (p.getEncounterid() == encID) {
                return true;
            }
        }
        return false;
    }

    private void createMarker(CatchablePokemon p, Pokemon poke) {
        //seeing when the pokemon will expire
        DateTime oldDate = new DateTime(p.getExpirationTimestampMs());
        Interval interval;
        if (oldDate.isAfter(new Instant())) {
            interval = new Interval(new Instant(), oldDate);
            //turn our interval into MM:SS
            DateTime dt = new DateTime(interval.toDurationMillis());
            DateTimeFormatter fmt = DateTimeFormat.forPattern("mm:ss");

            String timeOut = fmt.print(dt);
            //set our location
            LatLng position = new LatLng(p.getLatitude(), p.getLongitude());

            String uri = "p" + p.getPokemonId().getNumber();
            int resourceID = getResources().getIdentifier(uri, "drawable", getPackageName());

            Bitmap out = createMapIcon(resourceID, 2);

            int tempID = (int)p.getEncounterId();

            String encounterID = Integer.toString(tempID);

            //name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

            MarkerOptions pokeIcon = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(out))
                    .position(position)
                    .title(encounterID)
                    .snippet(timeOut);

            poke.setMarker(mMap.addMarker(pokeIcon));
        }
    }

    private Bitmap createMapIcon(int drawableId, int scale) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);
        bm = Bitmap.createScaledBitmap(bm,bm.getWidth()/scale + 1,bm.getHeight()/scale + 1,false);

        //background marker for pokemon
        int resourceID = getResources().getIdentifier("pokemonloc", "drawable", getPackageName());
        Bitmap bm2 = BitmapFactory.decodeResource(getResources(), resourceID)
                .copy(Bitmap.Config.ARGB_8888, true);
        bm2 = Bitmap.createScaledBitmap(bm2,bm2.getWidth()/scale,bm2.getHeight()/scale,false);

        Bitmap result = Bitmap.createBitmap(bm2.getWidth(), bm2.getHeight(), bm2.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bm2, new Matrix(), null);
        canvas.drawBitmap(bm, 28, 25, null);

        return result;
    }

    private void UpdatePokemonMarkers() {
        synchronized (seenPokemon) {

            for(Iterator<Pokemon> iterator = seenPokemon.iterator(); iterator.hasNext();) {
                Pokemon p = iterator.next();

                DateTimeFormatter formatter = DateTimeFormat.forPattern("mm:ss");
                Marker temp = p.getMarker();
                if (temp != null) {
                    if (p.getExpiryTime().isAfter(new Instant())) {
                        Interval interval = new Interval(new Instant(), p.getExpiryTime());
                        DateTime dt = new DateTime(interval.toDurationMillis());
                        String timeLeft = formatter.print(dt);

                        temp.setSnippet(timeLeft);

                        int id = Integer.parseInt(temp.getTitle());

                        if(p.getEncounterid() == highlightedID) {
                            TextView timeView = (TextView)findViewById(R.id.highlighttime);
                            timeView.setText(timeLeft);
                        }

                        DateTime dt2 = formatter.parseDateTime("00:00");

                        if (timeLeft.equals("00:00")) {
                            p.RemoveMarker();
                            iterator.remove();
                            findViewById(R.id.highlight).setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }

    private void ClearPokemonMarkers() {
        synchronized (seenPokemon) {
            for (Pokemon p: seenPokemon) {
                p.RemoveMarker();
            }
            seenPokemon.clear();
            findViewById(R.id.highlight).setVisibility(View.GONE);
        }
    }
}
