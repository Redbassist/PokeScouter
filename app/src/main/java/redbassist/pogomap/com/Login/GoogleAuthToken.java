package redbassist.pogomap.com.Login;

import com.pokegoapi.auth.GoogleAuthTokenJson;

import io.realm.RealmObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class GoogleAuthToken extends RealmObject {
    private String error;
    private String access_token;
    private String token_type;
    private int expires_in;
    private String refresh_token;
    private String id_token;

    public GoogleAuthToken(){}

    public GoogleAuthToken(GoogleAuthTokenJson token) {
        this.error = token.getError();
        this.access_token = token.getAccess_token();
        this.token_type = token.getToken_type();
        this.expires_in = token.getExpires_in();
        this.refresh_token = token.getRefresh_token();
        this.id_token = token.getId_token();
    }

    public GoogleAuthTokenJson toGoogleJson() {
        GoogleAuthTokenJson token = new GoogleAuthTokenJson();
        token.setAccess_token(getAccess_token());
        token.setError(getError());
        token.setToken_type(getToken_type());
        token.setExpires_in(getExpires_in());
        token.setRefresh_token(getRefresh_token());
        token.setId_token(getId_token());
        return token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getError() {
        return error;
    }

    public String getToken_type() {
        return token_type;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public String getId_token() {
        return id_token;
    }
}

