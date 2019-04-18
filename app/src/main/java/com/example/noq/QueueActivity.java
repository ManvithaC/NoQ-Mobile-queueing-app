package com.example.noq;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;

public class QueueActivity extends AppCompatActivity {

    private TextView cafeAddress, cafeName;

    private TextView waitTime, queueLength;

    private Button joinQueue;

    private String userId;

    private GetQueueTask mGetQueueTask = null;

    private JoinQueueTask mJoinQueueTask = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        cafeName = (TextView)findViewById(R.id.tvCafeName);
        cafeAddress = (TextView)findViewById(R.id.tvCafeAddress);
        waitTime = (TextView) findViewById(R.id.tvWaitTimeValue);
        queueLength = (TextView) findViewById(R.id.tvQueueSizeValue);
        joinQueue = (Button) findViewById(R.id.btnJoinQueue);

        final String placeId = getIntent().getStringExtra("placeId");
        String placeName = getIntent().getStringExtra("placeName");
        userId = getIntent().getStringExtra("email");

        cafeAddress.setText(placeId);
        cafeName.setText(placeName);

        mGetQueueTask = new GetQueueTask(placeId);
        mGetQueueTask.execute((Void) null);

        joinQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mJoinQueueTask = new JoinQueueTask(userId, placeId);
                mJoinQueueTask.execute((Void) null);
            }
        });
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class GetQueueTask extends AsyncTask<Object, String, String> {

        private final String mPlaceId;

        public GetQueueTask(String placeId) {
            mPlaceId = placeId;
        }

        @Override
        protected String doInBackground(Object... objects) {
            String returnData ="";

            String url = "https://noqueue-app.herokuapp.com/queues?placeId="+mPlaceId;
            DownloadUrl downloadUrl = new DownloadUrl();
            try {
                returnData = downloadUrl.readUrl(url);
                Log.d("return data", returnData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("data from GET Queue", returnData);
            return returnData;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("Result", result);
            DownloadUrl downloadUrl = new DownloadUrl();

            int get_places_responseCode = downloadUrl.getResponseCode();
            if(get_places_responseCode == 200){
                Log.d("Positive - Result", result);
                Map<String, String> queueData= null;

                try {
                    queueData = DownloadUrl.jsonToMap(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("Map - Result", queueData.get("waitTime"));
                waitTime.setText(queueData.get("waitTime"));
                queueLength.setText(queueData.get("queueLength"));
            }
            if(get_places_responseCode == 401){
                Toast toast =  Toast.makeText(getApplicationContext(), "Error occured",
                        Toast.LENGTH_LONG);
                toast.show();
            }

        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class JoinQueueTask extends AsyncTask<Object, String, String> {

        private final String mEmail;
        private final String mPlaceId;

        JoinQueueTask(String email, String placeId) {
            mEmail = email;
            mPlaceId = placeId;
        }

        @Override
        protected String doInBackground(Object... objects) {
            HashMap<String, String> newUser = new HashMap<String, String>();
            newUser.put("userId", mEmail);
            newUser.put("placeId", mPlaceId);

            String returnData ="";

            String url = "https://noqueue-app.herokuapp.com/queues/addUser";
            PostUrl postUrl = new PostUrl();
            try {
                returnData = postUrl.postData(newUser, url);
                Log.d("Add to Queue success", returnData);

                Toast toast =  Toast.makeText(getApplicationContext(), "You're successfully added to the queue.",
                        Toast.LENGTH_LONG);
                toast.show();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return returnData;
        }

        @Override
        protected void onPostExecute(String result) {
            mJoinQueueTask = null;
            if(result == "Successful"){
                Log.d("Add User success", result);
            }
        }

    }
}