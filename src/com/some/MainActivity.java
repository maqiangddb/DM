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
import android.os.Handler;

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
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.addJavascriptInterface(new Html5VideoInterface(), "videoInterface");

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
        Log.d("DM", "onBackPressed---canGoBack:"+webView.canGoBack());
        if(webView == null ){
            super.onBackPressed();
        }
        else{
            if(!webView.canGoBack())
            {
                super.onBackPressed();
            } else {
                chromeClient.onHideCustomView();
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
        public WebResourceResponse shouldInterceptRequest(WebView view,
                String url) {
            WebResourceResponse response = null;
            response = super.shouldInterceptRequest(view, url);
            return response;
        }
    }

    public class MyChromeClient extends WebChromeClient{
        
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            Log.d("DM", "onShowCustomView");
            if(myView != null){
                callback.onCustomViewHidden();
                return;
            }

            ViewGroup parent = (ViewGroup) webView.getParent();
            parent.removeView(webView);
            parent.addView(view);
            myView = view;

            myCallBack = callback;

            new Handler().postDelayed(new Runnable() {
                public void run() 
                {
                    String js = "javascript: var v=document.getElementsByTagName('video')[0]; "
                        + "v.play(); ";
                    webView.loadUrl(js);

                    js = "javascript: var v=document.getElementsByTagName('video')[0]; "
                        + "v.addEventListener('playing', function() { window.demo.clickonAndroid(); }, true); ";
                    webView.loadUrl(js);
                }
            }, 1);
            
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
        Html5VideoInterface()
        {

        }

        public void videoStart()
        {
            Log.d("DM", "video interface videoStart");
            String js = "javascript: var v=document.getElementsByTagName('video')[0]; "
                + "v.webkitEnterFullscreen(); ";
            webView.loadUrl(js);
        }

        public void videoFinish()
        {
            Log.d("DM", "video interface videoFinish");
        }
    }

}
