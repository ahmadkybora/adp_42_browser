    package com.example.adp_42_browser;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;

    import android.app.AlertDialog;
    import android.app.ProgressDialog;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.net.ConnectivityManager;
    import android.net.NetworkCapabilities;
    import android.net.NetworkInfo;
    import android.net.Uri;
    import android.os.Build;
    import android.os.Bundle;
    import android.provider.Settings;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.WindowManager;
    import android.webkit.DownloadListener;
    import android.webkit.WebChromeClient;
    import android.webkit.WebSettings;
    import android.webkit.WebView;
    import android.webkit.WebViewClient;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ProgressBar;

    public class MainActivity extends AppCompatActivity {

        EditText etUrl;
        Button btnGo;
        WebView webView;
        ProgressBar progressBar;
        ProgressDialog progressDialog;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_main);
            etUrl = findViewById(R.id.etUrl);
            btnGo = findViewById(R.id.btnGo);
            webView = findViewById(R.id.webview);
            progressBar = findViewById(R.id.progressBar);
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading Please Wait....");
            WebSettings webSettings = webView.getSettings();
            webSettings.setBuiltInZoomControls(true);
            webSettings.setJavaScriptEnabled(true);
            webView.setWebViewClient(new MyWebViewClient());
            if(!checkConnection()){
                showDialog();
            }
            btnGo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = etUrl.getText().toString().trim();
                    if(!url.startsWith("http://") || !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    webView.loadUrl(url);
                }
            });
            webView.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    setTitle("Loading...");
                    progressBar.setProgress(newProgress);
                    progressDialog.show();
                    if(newProgress == 100) {
                        setTitle(webView.getTitle());
                        progressDialog.dismiss();
                    }
                    super.onProgressChanged(view, newProgress);
                }
            });
            webView.setDownloadListener(new MyDownloadListener());
        }

        @Override
        public void onBackPressed() {
            if(webView.canGoBack()) {
                webView.goBack();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
            return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navPrev:
                    onBackPressed();
                    break;

                case R.id.navNext:
                    if(webView.canGoForward()) {
                        webView.goForward();
                    }
                    break;

                case R.id.navReload:
                    checkConnection();
            }
            return super.onOptionsItemSelected(item);
        }

        private boolean checkConnection() {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager != null) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                    if(capabilities != null) {
                        if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return true;
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return true;
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            return true;
                        }
                    }
                } else {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if(activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void showDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Connect to wifi or exist")
            .setCancelable(false)
            .setPositiveButton("Connect to Wifi", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            })
            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        private class MyWebViewClient extends WebViewClient {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
        }

        private class MyDownloadListener implements DownloadListener {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                if(s != null) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(s));
                    startActivity(i);
                }
            }
        }
    }