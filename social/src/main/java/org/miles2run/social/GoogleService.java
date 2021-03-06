package org.miles2run.social;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.gson.Gson;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class GoogleService {

    private static final String CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET");
    private static final String CALLBACK_URI = "/google/callback";
    private static final List<String> SCOPE = Arrays.asList("https://www.googleapis.com/auth/userinfo.profile;https://www.googleapis.com/auth/userinfo.email".split(";"));
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private GoogleAuthorizationCodeFlow flow;

    public GoogleService() {
        if (CLIENT_ID != null && CLIENT_SECRET != null) {
            flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, SCOPE).build();
        }
    }

    public String buildLoginUrl(String baseUrl) {
        checkFlowIsNotNull();
        final GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        return url.setRedirectUri(baseUrl + CALLBACK_URI).setState(generateStateToken()).build();
    }

    private void checkFlowIsNotNull() {
        if (flow == null) {
            throw new RuntimeException("Application is not configured to use Google services. Please set Google Client ID and Secret");
        }
    }

    private String generateStateToken() {
        SecureRandom sr1 = new SecureRandom();
        return "google;" + sr1.nextInt();
    }

    public GoogleTokenResponse getOauthToken(String baseUrl, final String authCode) throws IOException {
        checkFlowIsNotNull();
        final GoogleTokenResponse response = flow.newTokenRequest(authCode).setRedirectUri(baseUrl + CALLBACK_URI).execute();
        return response;
    }

    public Google getUser(final GoogleTokenResponse token) throws IOException {
        checkFlowIsNotNull();
        final Credential credential = flow.createAndStoreCredential(token, null);
        final HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
        final GenericUrl url = new GenericUrl(USER_INFO_URL);
        final HttpRequest request = requestFactory.buildGetRequest(url);
        request.getHeaders().setContentType("application/json");
        final String jsonIdentity = request.execute().parseAsString();
        Google google = new Gson().fromJson(jsonIdentity, Google.class);
        return google;

    }

}