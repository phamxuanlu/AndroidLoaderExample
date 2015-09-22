package snow.com.testloader;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLoaderManager().initLoader(R.id.loader_id_media_store_data, null, loaderCallbacks);
    }

    private LoaderManager.LoaderCallbacks<List<AudioItem>> loaderCallbacks = new LoaderManager
        .LoaderCallbacks<List<AudioItem>>
        () {
        @Override
        public Loader onCreateLoader(int i, Bundle bundle) {
            return new MediaStoreAudioLoader(MainActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<AudioItem>> loader, List<AudioItem> audioItems) {
            for (AudioItem ai : audioItems) {
                Log.v("ITEM", ai.title);
            }
        }

        @Override
        public void onLoaderReset(Loader loader) {
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
