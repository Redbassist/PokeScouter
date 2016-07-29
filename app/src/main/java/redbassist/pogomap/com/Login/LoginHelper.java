package redbassist.pogomap.com.Login;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.pokegoapi.auth.GoogleAuthTokenJson;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import okhttp3.OkHttpClient;
import redbassist.pogomap.com.pokemongomap.R;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

import redbassist.pogomap.com.Utilities.Util;

/**
 * Created by Matt on 27-Jul-16.
 */

public class LoginHelper {

    public boolean PTCLogin = false;

    Util util;
    public Activity activity;
    Snackbar snackbar;

    private String password;
    private String username;
    boolean loggedIn;

    EditText et_username;
    EditText et_password;
    GoogleAuthToken googleToken;

    RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo auth;

    SharedPreferences localSharedPrefs;

    public LoginHelper(Activity _activity, SharedPreferences sp, Snackbar sb) {
        this.activity = _activity;
        localSharedPrefs = sp;
        snackbar = sb;

        util = new Util(this.activity);

        et_username = (EditText) this.activity.findViewById(R.id.et_username);
        et_password = (EditText) this.activity.findViewById(R.id.et_password);
    }

    public String GetUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo GetPTCAuth() {
        return auth;
    }

    public void HandleNewLogin() {
        StartLogin();
        username = et_username.getText().toString();
        password = et_password.getText().toString();

        CheckPTCAuth();
    }

    public void LoginUsingPTC() {
        PTCLogin = true;
        loggedIn = localSharedPrefs.getBoolean("loggedIn", false);

        if (loggedIn) {
            username = localSharedPrefs.getString("username", "null");
            password = localSharedPrefs.getString("password", "null");

            //password = localSharedPrefs.getString("password", "null");

            et_username.setText(username);
            et_password.setText(password);

            util.SpinImage((ImageView)this.activity.findViewById(R.id.titleball));
            CheckPTCAuth();
        }
        else {
            util.fadeInAndShow(this.activity.findViewById(R.id.cnt_login));
        }
    }

    private void CheckPTCAuth() {
        CheckPTCLogin().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        FailedLogin();
                    }

                    @Override
                    public void onNext(RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
                        if (authInfo != null) {
                            if (authInfo.hasToken()) {
                                auth = authInfo;
                                SharedPreferences.Editor editPrefs = localSharedPrefs.edit();
                                if (!loggedIn) {
                                    editPrefs.putBoolean("loggedIn", true);
                                    editPrefs.putString("username", username);
                                    editPrefs.putString("password", password);
                                    editPrefs.apply();
                                }

                                snackbar.setText("Login Successful");
                                snackbar.setDuration(1000);
                                snackbar.show();
                                ShowMap();
                            }
                        }
                    }
                });
    }

    //used for handling the request for auth token by the user using PTC
    private Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo> CheckPTCLogin() {
        return Observable.defer(new Func0<Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo>>() {
            @Override public Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo> call() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    PTCLogin ptcLogin = new PTCLogin(client);
                    RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo auth = ptcLogin.login(username,password);
                    return Observable.just(auth);
                } catch (LoginFailedException e) {
                    snackbar.setText("Login Unsuccessful; PoGo Servers May Be Down");
                    snackbar.setDuration(2500);
                    snackbar.show();
                    e.printStackTrace();
                    return null;
                }
            }
        });
    }

    private void StartLogin() {
        //hide the login screen and start spinning the pokeball
        util.fadeOutAndHide(this.activity.findViewById(R.id.cnt_login));
        util.SpinImage((ImageView)this.activity.findViewById(R.id.titleball));
    }

    public void FailedLogin() {
        snackbar.setText("Login Unsuccessful; PoGo Servers May Be Down");
        snackbar.setDuration(2500);
        snackbar.show();
        util.fadeInAndShow(this.activity.findViewById(R.id.cnt_login));
        this.activity.findViewById(R.id.titleball).clearAnimation();
    }

    public void ShowMap() {
        util.fadeOutAndHide(this.activity.findViewById(R.id.splashscreen));
        util.fadeOutAndHide(this.activity.findViewById(R.id.titlemark));
        util.fadeOutAndHide(this.activity.findViewById(R.id.titleball));
        //show scan button
        util.fadeInAndShow(this.activity.findViewById(R.id.scanButton));
        util.fadeInAndShow(this.activity.findViewById(R.id.btn_settings));
    }

    public GoogleAuthToken getGoogleToken() {
        return googleToken;
    }

    public void setGoogleToken(GoogleAuthTokenJson googleToken) {
        this.googleToken = new GoogleAuthToken(googleToken);
    }
}
