package samples.printer.balloon.com.googlerest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadUrl(OAuthUtil.buildRequestCodeUri());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String name = view.getTitle();
                Log.v("Title", name);
                if (name.startsWith("Success")) {
                    OAuthUtil.code = name.substring(13);
                    Log.i("code", OAuthUtil.code);
                    if (OAuthUtil.code != null) {
                        OAuthUtil.executeFirst();
                        OAuthUtil.authorize();
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                }
            }
        });
    }




}
