package pdfprinterlistener.balloon.com.pdfprinterlistener;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends Activity {

    private static final String RECEIVE_MESSAGE_SERVLET = "http://localhost:8080/TestOAuthServer/servlet/ReceiveMessageServlet";
    Thread thread=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         thread  = new Thread(){
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(3000);
                        URL url = new URL(RECEIVE_MESSAGE_SERVLET);
                        HttpURLConnection httpConn = null;

                        httpConn = (HttpURLConnection) url.openConnection();

                        httpConn.setUseCaches(false);
                        httpConn.setDoOutput(true);
                        httpConn.setRequestMethod("POST");
                        httpConn.setDoInput(true);

                        InputStream inputStream = httpConn.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

                        String json = br.readLine();
                        Log.i("json",json);
                        OutputStream outputStream = httpConn.getOutputStream();
                        outputStream.close();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
    };



}

    @Override
    protected void onResume() {
        super.onResume();
        thread.start();
    }
}


