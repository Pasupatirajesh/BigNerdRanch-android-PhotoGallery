package secondapp.bignerdranch.com.photogallery;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SSubra27 on 4/6/16.
 */
public class PhotoGalleryFragment extends VisibleFragment
{
    private static final String TAG = "PhotoGalleryFragment";

    private JobScheduler mJobScheduler;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private PhotoAdapter mPhotoAdapter;
    private  int lastFetchedPage=1;
    private boolean loading = false;
    private ProgressBar mPb;
    private List<GalleryItem> mItems = new ArrayList<>();
    private boolean mPollingIsOn;

    public static Fragment newInstance()
    {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();
    }
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState)
    {
        final int standardColumns=3;
        layoutInflater = LayoutInflater.from(getActivity());
        View v =  layoutInflater.inflate(R.layout.fragment_photo_gallery, container,false);
        mRecyclerView =(RecyclerView) v.findViewById(R.id.fragment_photogallery);
        mLayoutManager = new GridLayoutManager(getActivity(), standardColumns);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                // check for scroll
                if(dy >0)
                {
                    int visibleItemCount =  layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItemCount = layoutManager.findFirstVisibleItemPosition();

                    if(loading)
                    {
                        if(visibleItemCount+pastVisibleItemCount >=totalItemCount)
                        {
                            loading=true;
                            ++lastFetchedPage;
                            updateItems();
                        }
                    }
                }
            }
        });
            mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Point size = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getSize(size);
                int newColumns = (int) Math.floor(size.x * 3 / 1440);
                if (newColumns != standardColumns) {
                    GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
                    layoutManager.setSpanCount(newColumns);
                }
            }
        });

            mPb = (ProgressBar)v.findViewById(R.id.progress_bar);
            setupAdapter();
            showProgressBar(true);
        return v;
    }
    public void showProgressBar(boolean isShown)
    {
        if(isShown)
        {
            mPb.setIndeterminate(true);
            mPb.setVisibility(ProgressBar.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        } else
        {
            mPb.setIndeterminate(false);
            mPb.setVisibility(ProgressBar.INVISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    private void setupAdapter()
    {
        if (isAdded()) {
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
    {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final android.support.v7.widget.SearchView searchView =(android.support.v7.widget.SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "QueryTextSubmit: " + s);
                InputMethodManager inm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inm.hideSoftInputFromWindow(mRecyclerView.getWindowToken(), 0);
                QueryPreferences.setStoredQuery(getActivity(), s);
                updateItems();
//                searchView.onActionViewCollapsed();
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                Log.d(TAG, "QueryTextChange: " + query);
                return true;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);


            }
        });
        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if(PollService.isServiceAlarmOn(getActivity()))
        {
            toggleItem.setTitle(R.string.stop_polling);
        } else
        {
            toggleItem.setTitle(R.string.start_polling);
        }
    }
    private void updateItems()
    {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query,this).execute();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                mPollingIsOn = !mPollingIsOn;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Log.i(TAG, "JobScheduler Toggled!!!");
                    final int JOB_ID = 1;
                    mJobScheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);

                    if (!mPollingIsOn) {
                        mJobScheduler.cancelAll();
                        Log.i(TAG, "JobScheduler Canceled");
                    } else {
                        boolean hasBeenScheduled = false;

                        for (JobInfo jobInfo : mJobScheduler.getAllPendingJobs()) {
                            if (jobInfo.getId() == JOB_ID) {
                                hasBeenScheduled = true;
                            }
                        }
                        if (hasBeenScheduled == false) {
                            JobInfo builder = new JobInfo.Builder(JOB_ID, new ComponentName(getActivity(), PollJobs.class))
                                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED).setPeriodic(1000 * 6).build();
                            mJobScheduler.schedule(builder);
                        }
                    }

                } else
                {
                    boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                    PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                }
                getActivity().invalidateOptionsMenu();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }
    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private GalleryItem mGalleryItem;
        private ImageView mTitleImageView;
        public PhotoHolder(View itemView)
        {
            super(itemView);
            mTitleImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            itemView.setOnClickListener(this);
        }
        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }
            @Override
            public void onClick(View v)
            {
                Intent i = PhotoPageActivity.newIntent(getActivity(),mGalleryItem.getPhotoPageUri());
                startActivity(i);
            }
        public void bindDrawable(Drawable drawable)
        {
            mTitleImageView.setImageDrawable(drawable);
        }
    }
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>
    {
        private List<GalleryItem> mGalleryItems;
        private int lastBoundPosition;

        public int getLastBoundPosition()
        {
            return lastBoundPosition;
        }

        public PhotoAdapter(List<GalleryItem> galleryItems)
        {
            mGalleryItems = galleryItems;
        }
        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
        {

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
            return new PhotoHolder(view);
        }
        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            photoHolder.bindGalleryItem(galleryItem);
            Picasso.with(getActivity()).load(galleryItem.getUrl()).placeholder(getResources().getDrawable(R.drawable.kangchenjunga)).into(photoHolder.mTitleImageView);
        }
        @Override
        public int getItemCount()
        {
            return mGalleryItems.size();
        }
    }


    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>>
    {
        private String mQuery = null;
        private PhotoGalleryFragment mPhotoGalleryFragment;

        public FetchItemsTask(String query, PhotoGalleryFragment photoGalleryFragment)
        {
            mPhotoGalleryFragment=photoGalleryFragment;
            mQuery = query;
        }
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            if(mPhotoGalleryFragment.isResumed())
            {
                showProgressBar(true);
            }
        }
        @Override
        protected List<GalleryItem> doInBackground(Void... params)
        {
            if(mQuery==null)
            {
                return new FlickrFetcher().fetchRecentPhotos();
            } else
            {
                return new FlickrFetcher().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items)
        {
            mPhotoGalleryFragment.showProgressBar(false);
            if(lastFetchedPage>1)
            {
                mItems.addAll(items);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            } else
            {
                mItems = items;
                setupAdapter();
            }
           lastFetchedPage++;
        }

    }
}
