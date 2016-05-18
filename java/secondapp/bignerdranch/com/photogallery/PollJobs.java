package secondapp.bignerdranch.com.photogallery;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

/**
 * Created by SSubra27 on 5/3/16.
 */
public class PollJobs extends JobService {
    private PollTask mCurrentTask;
    @Override
    public boolean onStartJob(JobParameters jp)
    {

        mCurrentTask = new PollTask();
        mCurrentTask.execute(jp);
        Toast.makeText(getApplicationContext(), "JobService task running", Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jp)
    {

        if(mCurrentTask!=null)
        {
            mCurrentTask.cancel(true);
        }
        Toast.makeText(this, "Job Stopped: criteria not met", Toast.LENGTH_LONG).show();
            return true;
    }
    private class PollTask extends AsyncTask<JobParameters, Void, Void>
    {
        @Override
        protected Void doInBackground(JobParameters... params)
        {
           JobParameters jobParameters = params[0];

           List<GalleryItem> items = PollServiceHelper.fetchNewItems(PollJobs.this);

            if(items.size()==0)
            {
                jobFinished(jobParameters, false);
                return null;
            }

            String resultId = items.get(0).getId();

            PollServiceHelper.sendNotificationIfNewDataFetched(PollJobs.this,resultId);
            jobFinished(jobParameters,false);
            return null;

        }


    }
}





