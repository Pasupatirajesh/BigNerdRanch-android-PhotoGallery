package secondapp.bignerdranch.com.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by SSubra27 on 4/19/16.
 */
public class ThumbnailDownloader<T> extends HandlerThread {

    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_PRELOAD=1;
    private static final String TAG = "ThumbnailDownloader";

    private Handler mRequestHandler;

    private Handler mResponseHandler;

    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    private ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private LruCache<String, Bitmap> mMemoryCache;


    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
        final int maxMemory =(int) Runtime.getRuntime().maxMemory()/1024;
        final int cacheSize = maxMemory/8;
        mMemoryCache = new LruCache<>(cacheSize);
    }

    @Override
    protected void onLooperPrepared() // good place to implement Handler.handleMessage() because onLooperPrepared() is called
    // before looper object checks for messages in its queue for the first time.
    {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what)
                {
                    case MESSAGE_DOWNLOAD:
                        T target =(T) msg.obj;
                        Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                        handleRequest(target);
                        break;
                    case MESSAGE_PRELOAD:
                        String url = (String) msg.obj;
                        downloadImage(url);
                        break;
                }
            }
        };
    }
    public void queueThumbnail(T target, String url)
    {
        if(url == null)
        {
            mRequestMap.remove(target);
        } else
        {
            mRequestMap.put(target,url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();
        }
        Log.i(TAG, "Got a url : " + url);
    }

    public void preloadImage(String url)
    {
        if(url != null)
        {
            mRequestHandler.obtainMessage(MESSAGE_PRELOAD, url).sendToTarget();
        }
    }

    public void handleRequest(final T target) {

            final String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }
            final Bitmap bitmap = downloadImage(url);

            mResponseHandler.post(new Runnable() {
                public void run() {
                    if (mRequestMap.get(target) != url) {
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    private Bitmap downloadImage(String url)
    {
        Bitmap bitmap;
        if(url==null)
        {
            return null;
        }
        bitmap=mMemoryCache.get(url);
        if(bitmap!=null)
        {
            return bitmap;
        }
        try
        {
            byte[] bitmapBytes = new FlickrFetcher().getUrlBytes(url);
            bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            mMemoryCache.put(url, bitmap);
            Log.i(TAG, "Bitmaps were added to Cache");
            return bitmap;
        } catch (IOException ioe)
        {
            Log.i(TAG, "error downloading bitmap", ioe);
            return null;
        }
    }

    public Bitmap getBitmapFromMemoryCache(String url)
    {
        return mMemoryCache.get(url);
    }
    public void clearCache()
    {
        mMemoryCache.evictAll();
    }

}
