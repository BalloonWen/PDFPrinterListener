package samples.printer.balloon.com.googlerest;

import android.os.AsyncTask;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by whenb on 4/6/2016.
 */
public class OAuthUtil {

    private static GoogleAuthorizationCodeFlow flow;
    private static HttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY = JacksonFactory
            .getDefaultInstance();
    public static final String TAG="PrinterListener";
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);
    private static Credential credential = null;
    public static String REFRESH_TOKEN = "";
    private static final String CLIENT_ID = "830410059030-ta425ms7b2ud4mjl3k5njdb6f9ogu1rk.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "1NNS_U_KfhyAKydCzW36Rn2d";
    public static String code = "";
    public static InputStream in ;
    public static void executeFirst(){
        if (flow==null) {
            try {
            HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

            GoogleClientSecrets clientSecrets = null;

                clientSecrets = GoogleClientSecrets.load(
                        JSON_FACTORY, new InputStreamReader(in));
                flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
    //					.setDataStoreFactory(DATA_STORE_FACTORY)
                        .setApprovalPrompt("force").setAccessType("offline")
                        .build();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private static String generateUserId() {
        SecureRandom sr1 = new SecureRandom();
        String userId = "google;" + sr1.nextInt();
        return userId;
    }

    public static void authorize(){
        new GetAccessToken().execute();
    }
    public static Drive getDrive() {
        try {
            credential = new GetRefreshedToken().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("PDFListenerPure").build();
    }
    public static String buildRequestCodeUri() {
        StringBuffer sb = new StringBuffer();
        sb.append("https://");
        sb.append("accounts.google.com/o/oauth2/v2/auth");
        sb.append("?scope=");
        sb.append("https://www.googleapis.com/auth/drive");
        sb.append("&redirect_uri=");
        sb.append("urn:ietf:wg:oauth:2.0:oob:auto");
        sb.append("&response_type=code");
        sb.append("&client_id=");
        sb.append(CLIENT_ID);
        return sb.toString();
    }

    private static class GetAccessToken extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            final GoogleTokenResponse response;
            try {
                response = flow.newTokenRequest(code)
                        .setRedirectUri("urn:ietf:wg:oauth:2.0:oob:auto").execute();
                credential = flow.createAndStoreCredential(response,
                        generateUserId());
                REFRESH_TOKEN = credential.getRefreshToken();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class GetRefreshedToken extends AsyncTask<Void, Void, Credential> {

        @Override
        protected Credential doInBackground(Void... params) {
            try {
                if (REFRESH_TOKEN.equals("") || credential.getExpiresInSeconds() < 3500 || credential != null) {
                    System.out.println("get permission first");
                    return credential;
                }
                GoogleCredential refreshCredential = new GoogleCredential.Builder()
                        .setClientSecrets(
                                CLIENT_ID,
                                CLIENT_SECRET)
                        .setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT)
                        .build().setRefreshToken(REFRESH_TOKEN);
                refreshCredential.refreshToken();
                return refreshCredential;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return credential;

        }
    }
    //    private class RequestToken extends AsyncTask<String, Void, Void> {
//
//
//        @Override
//        protected Void doInBackground(String... params) {
//            try {
//                String code = params[0];
//                String urlParameters = "code=" + code +
//                        "client_id=" + CLIENT_ID +
//                        "client_secret=" + CLIENT_SECRET +
//                        "redirect_uri=urn:ietf:wg:oauth:2.0:oob:auto" +
//                        "grant_type=authorization_code";
//                byte[] postData = new byte[0];
//
//                postData = urlParameters.getBytes();
//
//                int postDataLength = postData.length;
//
//                URL getTokenURL = new URL(TOKEN_URL);
//                HttpURLConnection conn = (HttpURLConnection) getTokenURL.openConnection();
//                conn.setDoOutput(true);
//                conn.setInstanceFollowRedirects(false);
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                conn.setRequestProperty("charset", "utf-8");
//                conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
//                conn.setUseCaches(false);
//
//                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
//                    wr.write(postData);
//                }
//                int responseCode = conn.getResponseCode();
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    // reads server's response
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                    String response = reader.readLine();
//                    System.out.println("Server's response: " + response);
//                } else {
//                    System.out.println("Server returned non-OK code: " + responseCode);
//                }
//
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//    }

}
