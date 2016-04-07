package samples.printer.balloon.com.googlerest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.api.services.drive.Drive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    private static final int GET_PERMIT_REQUEST = 1;
    private static final String DRIVE_ID = "DriveId:CAESHDBCN2FLQXZXVkpRS3Rja2w2U1hGd0xWQjJTV3MYECCcjsvkzFMoAA==";
    //    private static final String RESOURCE_ID="0B7aKAvWVJQKtckl6SXFwLVB2SWs";
    private static String FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";
//    private static final String RECEIVE_MESSAGE_SERVLET = "http://135.23.64.27:8080/TestOAuthServer/servlet/ReceiveMessageServlet";
//    private static final String RECEIVE_MESSAGE_SERVLET = "http://192.168.139.128:8080/TestOAuthServer/servlet/ReceiveMessageServlet";
private static final String RECEIVE_MESSAGE_SERVLET = "http://samprinter.cloudapp.net/TestOAuthServer/servlet/ReceiveMessageServlet";
    private List<String> files2StoreList=null;
    private Queue<String> files2Print=null;
    private static final String TAG = "PrinterListener";
    Button btnGetPermission;
    Button btnDownload;
    TextView txtGetPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        files2StoreList = new ArrayList<>();
        files2Print = new LinkedList<>();

        btnGetPermission = (Button) findViewById(R.id.btnGetPermit);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        txtGetPermission = (TextView) findViewById(R.id.txtGetPermission);
        btnDownload.setVisibility(View.INVISIBLE);

        btnGetPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                startActivityForResult(intent, GET_PERMIT_REQUEST);
            }
        });
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

//        Uri uri = Uri.parse(buildRequestCodeUri());//
//        Intent intent = new Intent(Intent.ACTION_VIEW,uri);//
//        startActivity(intent);

        try {
            OAuthUtil.in = this.getResources().openRawResource(
                    getResources().getIdentifier("client_secret",
                            "raw", getPackageName()));
        } catch (Exception e) {
            Log.e(OAuthUtil.TAG, e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GET_PERMIT_REQUEST:
                if (resultCode == RESULT_OK) {

                    txtGetPermission.setText("You have already get refresh token:" + OAuthUtil.REFRESH_TOKEN);
                    btnDownload.setVisibility(View.VISIBLE);
                    new ReceiveMessageThread().start();
                    new getFileFromGD().start();
                }
        }
    }

    private class getFileFromGD extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (true) {
                    if (!files2StoreList.isEmpty()) {
                        JSONObject json = new JSONObject(files2StoreList.get(0));
                        String resourceId = json.getString("resourceId");
                        String fileName = json.getString("fileName");
                        Drive drive = OAuthUtil.getDrive();
                        java.io.File file = new java.io.File(FILE_PATH + fileName);
                        java.io.OutputStream out = new java.io.FileOutputStream(file);
                        drive.files().get(resourceId).executeMediaAndDownloadTo(out);
                        out.flush();
                        out.close();
                        files2Print.add(fileName);
                        files2StoreList.remove(0);
                        Thread.sleep(2000);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private class ReceiveMessageThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {

                    URL url = new URL(RECEIVE_MESSAGE_SERVLET);
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

                    httpConn.setUseCaches(false);
                    httpConn.setDoOutput(true);
                    httpConn.setRequestMethod("POST");
                    httpConn.setDoInput(true);

                    InputStream inputStream = httpConn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

                    String jsonString = br.readLine();
                    Log.i(TAG, jsonString);
                    Log.i(TAG, files2StoreList.size() + "");
                    JSONArray jsonArray = new JSONArray(jsonString);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        files2StoreList.add(jsonArray.getString(i));
                    }
                    sleep(5000);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}



