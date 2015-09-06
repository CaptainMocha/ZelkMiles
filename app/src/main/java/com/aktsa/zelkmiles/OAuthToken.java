package com.aktsa.zelkmiles;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OAuthToken extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_token);

        final WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("https://api.dailymile.com/oauth/authorize?response_type=token&client_id=" + Constants.DM_CLIENT_ID + "&redirect_uri=" + Constants.CALLBACK_URL);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                String fragment = "#access_token=";
                int start = url.indexOf(fragment);
                if (start > -1) {
                    webView.stopLoading();
                    String token = url.substring(start + fragment.length(), url.length());
                    SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).edit();
                    editor.putString("oauth_token", token);
                    editor.apply();
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_oauth_token, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
