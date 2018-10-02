package no.nav.brukerdialog.security.oidc;

import no.nav.brukerdialog.security.domain.IdTokenAndRefreshToken;
import no.nav.brukerdialog.security.domain.OidcCredential;
import no.nav.sbl.rest.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CACHE_CONTROL;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;

public class IdTokenAndRefreshTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(IdTokenAndRefreshTokenProvider.class);

    static final String ENCODING = "UTF-8";

    public static final String ID_TOKEN = "id_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    private final Client client = RestUtils.createClient();


    private final IdTokenAndRefreshTokenProviderConfig parameters;

    public IdTokenAndRefreshTokenProvider() {
        this(IdTokenAndRefreshTokenProviderConfig.resolveFromSystemProperties()
        );
    }

    public IdTokenAndRefreshTokenProvider(IdTokenAndRefreshTokenProviderConfig idTokenAndRefreshTokenProviderConfig) {
        this.parameters = idTokenAndRefreshTokenProviderConfig;
    }

    public IdTokenAndRefreshToken getToken(String authorizationCode, String redirectUri) {
        return TokenProviderUtil.getToken(() -> createTokenRequest(authorizationCode, redirectUri, client), this::extractToken);
    }

    Response createTokenRequest(String authorizationCode, String redirectUri, Client client) {
        String urlEncodedRedirectUri;

        try {
            urlEncodedRedirectUri = URLEncoder.encode(redirectUri, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Could not URL-encode the redirectUri: " + redirectUri);
        }

        String host = parameters.issoHostUrl;
        String data = "grant_type=authorization_code"
                + "&realm=/"
                + "&redirect_uri=" + urlEncodedRedirectUri
                + "&code=" + authorizationCode;
        log.debug("Requesting tokens by POST to " + host);
        return client.target(host + "/access_token")
                .request()
                .header(AUTHORIZATION, TokenProviderUtil.basicCredentials(parameters.issoRpUserUsername, parameters.issoRpUserPassword))
                .header(CACHE_CONTROL, "no-cache")
                .post(Entity.entity(data, APPLICATION_FORM_URLENCODED_TYPE))
                ;
    }

    private IdTokenAndRefreshToken extractToken(String responseString) {
        OidcCredential token = new OidcCredential(TokenProviderUtil.findToken(responseString, ID_TOKEN));
        String refreshToken = TokenProviderUtil.findToken(responseString, REFRESH_TOKEN);
        return new IdTokenAndRefreshToken(token, refreshToken);
    }

}
