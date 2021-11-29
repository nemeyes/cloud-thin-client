package ai.sibylla.egp.client;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.util.Log;

public class ServiceLauncher extends AsyncTask<String, String, String> {
    private static final String TAG = "ServiceLauncher";

    private MainActivity mFront = null;

    public ServiceLauncher(MainActivity front) {
        mFront = front;
    }

    @Override
    protected void onPreExecute() {
        Log.i(TAG, String.format("Start Game Controller Service"));
        Intent intent = new Intent(mFront.getApplicationContext(), ai.sibylla.egp.client.game.ControllerService.class);
        mFront.startService(intent);
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.i(TAG, String.format("Bind Game Controller Service"));
        if(!mFront.isServiceBind()) {
            Intent intent = new Intent(mFront.getApplicationContext(), ai.sibylla.egp.client.game.ControllerService.class);
            mFront.bindService(intent, mFront.getServiceConnection(), Context.BIND_AUTO_CREATE);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... progress) {

    }

    @Override
    protected void onPostExecute(String string) {

    }
}
