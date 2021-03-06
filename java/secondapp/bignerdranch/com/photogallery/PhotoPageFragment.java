package secondapp.bignerdranch.com.photogallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by SSubra27 on 5/16/16.
 */
public class PhotoPageFragment extends VisibleFragment {
    private static final String ARG_URI= "photo_page_url";

    private Uri mUri;
    private WebView mWebView;
    private ProgressBar mProgressBar;

    public static PhotoPageFragment newInstance(Uri uri)
    {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mUri = getArguments().getParcelable(ARG_URI);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = layoutInflater.inflate(R.layout.fragment_photo_page, container,false);
        mProgressBar = (ProgressBar)v.findViewById(R.id.fragment_photo_page_progress_bar);
        mProgressBar.setMax(1000); // WebChromeClient reports in range 0-100
        mWebView = (WebView)v.findViewById(R.id.fragment_photo_page_web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient()
        {
            public void onProgressChanged(WebView webView, int newProgress)
            {
                if(newProgress == 100)
                {
                    mProgressBar.setVisibility(View.GONE);
                } else
                {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }
            public void onReceivedTitle(WebView webView, String title)
            {
                AppCompatActivity appCompatActivity =(AppCompatActivity) getActivity();
                appCompatActivity.getSupportActionBar().setSubtitle(title);
            }
        });
        mWebView.setWebViewClient(new WebViewClient()
        {
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if(!url.startsWith("http"))
                {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;

                }
                return false;
            }
        });
        mWebView.loadUrl(mUri.toString());
        return v;
    }
    public boolean onBackPressed()
    {
        if(mWebView.canGoBack())
        {
            mWebView.goBack();
            return true;
        } else
        {
            return false;
        }
    }
}
