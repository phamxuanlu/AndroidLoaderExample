package snow.com.testloader;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by FRAMGIA\pham.xuan.lu on 07/09/2015.
 */
public class MediaStoreAudioLoader extends AsyncTaskLoader<List<AudioItem>> {
    private static final String[] AUDIO_PROJECTION =
        new String[]{
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME
        };
    private List<AudioItem> cached;
    private boolean observerRegistered = false;
    private final ForceLoadContentObserver forceLoadContentObserver = new ForceLoadContentObserver();
    private HashMap<String, Integer> colIndexCached;

    public MediaStoreAudioLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(List<AudioItem> data) {
        if (!isReset() && isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (cached != null) {
            deliverResult(cached);
        }
        if (takeContentChanged() || cached == null) {
            forceLoad();
        }
        registerContentObserver();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        cached = null;
        unregisterContentObserver();
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
        unregisterContentObserver();
    }

    @Override
    public List<AudioItem> loadInBackground() {
        List<AudioItem> data = queryAudios();
        Collections.sort(data, new Comparator<AudioItem>() {
            @Override
            public int compare(AudioItem lhs, AudioItem rhs) {
                return lhs.title.compareTo(rhs.title);
            }
        });
        return data;
    }

    private List<AudioItem> queryAudios() {
        List<AudioItem> data = new ArrayList<>();
        Cursor cursor = getContext().getContentResolver()
            .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                AUDIO_PROJECTION,
                MediaStore.Audio.Media.IS_MUSIC +" !=0",
                null,
                MediaStore.Audio.Media.TITLE + " ASC ");
        if (cursor == null) {
            return data;
        }
        try {
            if (colIndexCached == null) {
                colIndexCached = new HashMap<>();
                for (String col : AUDIO_PROJECTION) {
                    int index = cursor.getColumnIndex(col);
                    colIndexCached.put(col, index);
                }
            }
            while (cursor.moveToNext()) {
                AudioItem item = new AudioItem();
                item.title = cursor.getString(colIndexCached.get(MediaStore.Audio.Media.TITLE));
                data.add(item);
            }
        } finally {
            cursor.close();
        }
        return data;
    }

    private void registerContentObserver() {
        if (!observerRegistered) {
            ContentResolver cr = getContext().getContentResolver();
            cr.registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false, forceLoadContentObserver);
            observerRegistered = true;
        }
    }

    private void unregisterContentObserver() {
        if (observerRegistered) {
            observerRegistered = false;
            getContext().getContentResolver().unregisterContentObserver(forceLoadContentObserver);
        }
    }
}