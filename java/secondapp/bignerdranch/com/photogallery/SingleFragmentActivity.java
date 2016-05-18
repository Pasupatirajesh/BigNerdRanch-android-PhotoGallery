package secondapp.bignerdranch.com.photogallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by SSubra27 on 4/6/16.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity
{

    protected abstract Fragment createFragment();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.activity_fragment_container);
        if(fragment==null)
        {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.activity_fragment_container,fragment).commit();

        }
    }

}
