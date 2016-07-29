package redbassist.pogomap.com.Pokemon;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.joda.time.DateTime;

/**
 * Created by Matt on 24/07/2016.
 */
public class Pokemon {
    int Number;
    String Name;
    int encounterid;
    long expires;
    Marker marker;
    DateTime expiryTime;
    String ID;

    public Pokemon() {
    }

    public Pokemon(int number, String name, int encounterid, DateTime et, long expires, String i) {
        ID = i;
        expiryTime = et;
        Number = number;
        Name = name;
        this.encounterid = encounterid;
        this.expires = expires;
    }

    public void setMarker(Marker m) { marker = m; }

    public Marker getMarker() { return marker; }

    public void RemoveMarker() {
        if (marker != null)
            marker.remove();
    }

    public int getNumber() {
        return Number;
    }

    public void setNumber(int number) {
        Number = number;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getEncounterid() {
        return encounterid;
    }

    public void setEncounterid(int encounterid) {
        this.encounterid = encounterid;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public DateTime getExpiryTime() {return expiryTime; }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
