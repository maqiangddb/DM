package com.some;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebResourceResponse;
import android.widget.FrameLayout;
import android.widget.VideoView;
import android.os.Handler;
import android.content.Context;
import android.graphics.Bitmap;

//import android.media.MediaFormat;
//import com.android.org.chromium.content.browser.ContentVideoView;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;

public class MainActivity extends Activity
{
    /*
    private WebView webview = null;
    private WebChromeClient chromeClient = null;
    private WebChromeClient.CustomViewCallback myCallBack = null;
    */

    private FrameLayout frameLayout = null;
    private WebView webView = null;
    private WebChromeClient chromeClient = null;
    private View myView = null;
    private Handler mHandler = new Handler();
    private WebChromeClient.CustomViewCallback myCallBack = null;

    static final String web_url = "http://avdanmu.duapp.com/";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d("DM", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        frameLayout = (FrameLayout)findViewById(R.id.framelayout);
        webView = (WebView)findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();

        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false); 

        //load web in this app
        webView.setWebViewClient(new MyWebviewClient());

        chromeClient = new MyChromeClient();
        webView.setWebChromeClient(chromeClient);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);


        final String USER_AGENT_STRING = webView.getSettings().getUserAgentString() + " Rong/2.0";
        webView.getSettings().setUserAgentString( USER_AGENT_STRING );
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setLoadWithOverviewMode(true);

        //enable javascript 
        
        webSettings.setJavaScriptEnabled(true);
        webSettings.setPluginsEnabled(true);
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.addJavascriptInterface(new Html5VideoInterface(this), "videoInterface");

        webView.loadUrl(web_url);

        if(savedInstanceState != null){
            webView.restoreState(savedInstanceState);
        }
    }

    @Override
    protected void onPause()
    {
        Log.d("DM", "onPause");
        super.onPause();
        webView.onPause();
    }
/*
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event)
    {
         //do something
    }
    */

    @Override
    public void onBackPressed() {
        Log.d("DM", "onBackPressed---canGoBack:"+webView.canGoBack()+ " myView:"+myView);
        if (myView != null)
        {
            chromeClient.onHideCustomView();
        }
        
        if(webView == null ){
            super.onBackPressed();
        }
        else{
            if(!webView.canGoBack())
            {
                super.onBackPressed();
            } else {
                
                webView.goBack();
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        webView.saveState(outState);
    }

    public class MyWebviewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            Log.d("DM", "shouldOverrideUrlLoading url:"+url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {
            Log.d("DM", "onPageStarted  url:"+url);
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            Log.d("DM", "onPageFinished url:" + url );
            super.onPageFinished(view, url);
            String js = "javascript: var v=document.getElementsByTagName('video')[0];"
                + "window.videoInterface.log(v.videoHeight);"
                + "window.videoInterface.log(v.videoWidth);"
                + "var title=document.getElementsByTagName('title')[0];"
                + "window.videoInterface.log(title.innerHTML);"
                + "title.innerHTML='Hello World!';";
            webView.loadUrl(js);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                String url) {
            Log.d("DM", "shouldInterceptRequest  url:" + url);
            WebResourceResponse response = null;
            response = super.shouldInterceptRequest(view, url);
            return response;
        }
    }

    public class MyChromeClient extends WebChromeClient{
        
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            String name = view.getClass().getName();
            Log.d("DM", "onShowCustomView--view:"+view+" name:"+ name);
            if(myView != null){
                callback.onCustomViewHidden();
                return;
            }

            //videoview.addSubtitleSource();

            ViewGroup parent = (ViewGroup) webView.getParent();
            parent.removeView(webView);
            parent.addView(view);
            try {
                myView = view;
            } catch (Exception e) {
                Log.e("DM", "myView error :"+ e);
                e.printStackTrace();
            }
            //myView = view;

            myCallBack = callback;

            mHandler.postDelayed(new Runnable() {
                public void run() 
                {
                    String js = "javascript: var v=document.getElementsByTagName('video')[0]; "
                        + "v.play(); ";
                    webView.loadUrl(js);

                    //js = "javascript: var v=document.getElementsByTagName('video')[0]; "
                      //  + "v.addEventListener('playing', function() { window.videoInterface.videoStart(); }, true); ";
                    //webView.loadUrl(js);

                }
            }, 1);

            String js = "javascript: var v=document.getElementsByTagName('video')[0]; "
              + "v.addEventListener('playing', function() { window.videoInterface.videoStart(); }, true); ";
            webView.loadUrl(js);

            js = "javascript: var track = document.getElementsByTagName('track')[0];"
                + "window.videoInterface.onSubtitleUrl(track.src); "
                + "window.videoInterface.log(track.src);";
            webView.loadUrl(js);
            
        }
        
        @Override
        public void onHideCustomView() {
            if(myView == null){
                return;
            }
            ViewGroup parent = (ViewGroup) myView.getParent();
            parent.removeView(myView);
            parent.addView(webView);
            myView = null;

            if (myCallBack != null)
            {
                myCallBack.onCustomViewHidden();
                myCallBack = null;
            }
            
        }
        
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            // TODO Auto-generated method stub
            Log.d("DM", consoleMessage.message()+" at "+consoleMessage.sourceId()+":"+consoleMessage.lineNumber());
            return super.onConsoleMessage(consoleMessage);
        }
    }

    private final class Html5VideoInterface
    {
        MainActivity mContext;
        Html5VideoInterface(Context c)
        {
            mContext = (MainActivity) c;
        }

        public void videoStart()
        {
            Log.d("DM", "video interface videoStart");
            mHandler.post( new Runnable() {
                public void run()
                {
                    String js = "javascript: var v=document.getElementsByTagName('video')[0]; "
                        + "v.webkitEnterFullscreen(); ";
                    mContext.webView.loadUrl(js);  
                }

            });

        }

        public void videoFinish()
        {
            Log.d("DM", "video interface videoFinish");
        }

        public void onSubtitleUrl(String url)
        {
            Log.d("DM", "getSubtitleUrl url:" + url);
            //InputStream is = getStream(url);
            //myView.addSubtitleSource(is, 
             //   MediaFormat.createSubtitleFormat("text/vtt",Locale.ENGLISH.getLanguage()));
        }

        public void log(String msg)
        {
            final String log = msg;
            mHandler.post( new Runnable() {
                public void run ()
                {
                    Log.d("DM", "log ["+log+ "]");
                }
            });
        }

        private InputStream getStream(String path)
        {
            InputStream is = null;
            URL url = null;

            try {
                url = new URL(path);
            } catch (MalformedURLException e) {
                e.printStackTrace(); 
            }

            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return is;
        }
    }

}
