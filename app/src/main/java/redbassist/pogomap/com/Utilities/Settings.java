package redbassist.pogomap.com.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import redbassist.pogomap.com.pokemongomap.R;

/**
 * Created by Matt on 28-Jul-16.
 */
public class Settings {
    Activity activity;
    SharedPreferences sharedPreferences;

    //options that are set and loaded
    public int distance;
    public boolean clearPokemon;

    public Settings(Activity a) {
        activity = a;
        sharedPreferences = this.activity.getSharedPreferences(this.activity.getString(R.string.sharedPrefName), Context.MODE_PRIVATE);

        LoadPreferences();
    }

    public void LoadPreferences() {
        distance = sharedPreferences.getInt("distance", 400);
        clearPokemon = sharedPreferences.getBoolean("clearPokemon", false);
    }

    public void SavePreferences() {
        SharedPreferences.Editor editPrefs = sharedPreferences.edit();
        editPrefs.putInt("distance", distance);
        editPrefs.putBoolean("clearPokemon", clearPokemon);

        editPrefs.commit();
    }

    public void SetDistance(int d) {
        distance = d;
    }
}
