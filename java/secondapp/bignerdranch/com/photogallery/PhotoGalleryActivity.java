package secondapp.bignerdranch.com.photogallery;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class PhotoGalleryActivity extends SingleFragmentActivity{

    private JobScheduler mJobScheduler;
    public static Intent newIntent(Context context)
    {
        return new Intent(context, PhotoGalleryActivity.class);
    }
   @Override
   protected Fragment createFragment()
   {
       return PhotoGalleryFragment.newInstance();
   }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

    }

}


