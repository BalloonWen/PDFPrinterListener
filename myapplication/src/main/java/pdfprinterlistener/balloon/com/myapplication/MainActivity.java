package pdfprinterlistener.balloon.com.myapplication;

import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String RECEIVE_MESSAGE_SERVLET = "http://135.23.64.27:8080/TestOAuthServer/servlet/ReceiveMessageServlet";
    private List<String> list;
    private GoogleApiClient mGoogleApiClient;
    String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";
    protected static final int REQUEST_CODE_RESOLUTION = 1;
    Thread getFileThread = null;
    String fileName;
    String resourceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();

        final Thread queueThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {

                        URL url = new URL(RECEIVE_MESSAGE_SERVLET);
                        HttpURLConnection httpConn = null;

                        httpConn = (HttpURLConnection) url.openConnection();

                        httpConn.setUseCaches(false);
                        httpConn.setDoOutput(true);
                        httpConn.setRequestMethod("POST");
                        httpConn.setDoInput(true);

                        InputStream inputStream = httpConn.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

                        String jsonString = br.readLine();
                        Log.i("json", jsonString);
                        Log.i("list", list.size() + "");
                        JSONArray jsonArray = new JSONArray(jsonString);


                        for (int i = 0; i < jsonArray.length(); i++) {
                            list.add(jsonArray.getString(i));
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

            ;
        };

        getFileThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        if (!list.isEmpty()) {

                            JSONObject json = new JSONObject(list.get(0));


                            resourceId = json.getString("DriveId");
                            fileName = json.getString("fileName");
//                        String resourceId = "0B7aKAvWVJQKtSXJVTUxBVFliXzg";

                            DriveFile driveFile = DriveId.decodeFromString(resourceId).asDriveFile();


//                        DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient,
//                                DriveId.decodeFromString(resourceId));
                            driveFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                                @Override
                                public void onResult(DriveApi.DriveContentsResult result) {
                                    try {
                                        if (!result.getStatus().isSuccess()) {
                                            // Handle error
                                            return;
                                        }

                                        DriveContents contents = result.getDriveContents();
                                        InputStream inputStream = contents.getInputStream();
                                        File file = new File(filePath + fileName);
                                        FileOutputStream fileOut = null;

                                        fileOut = new FileOutputStream(file);

                                        byte[] buffer = new byte[1024];
                                        int len = -1;
                                        while ((len = inputStream.read(buffer)) != -1) {
                                            fileOut.write(buffer, 0, len);
                                        }

                                        inputStream.close();
                                        fileOut.flush();
                                        fileOut.close();

                                        Log.i("print", "printed " + fileName);
                                        if(!list.isEmpty())
                                        list.remove(0);


                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return;
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
        queueThread.start();

    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i("connection", "connection success");
        getFileThread.start();

    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i("connection", "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
            return;
        }
        try {
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e("connection", "Exception while starting resolution activity", e);
        }
    }

    private class print extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.currentThread();

                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
