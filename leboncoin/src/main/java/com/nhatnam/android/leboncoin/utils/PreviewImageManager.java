/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nhatnam.android.leboncoin.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.nhatnam.android.leboncoin.R;

/**
 * Asynchronously loads preview image of picture and videos and maintains a
 * cache of it.
 */
public abstract class PreviewImageManager
{
    public static DefaultImageProvider DEFAULT_IMAGE;

    final static BitmapFactory.Options BITMAP_OPTIONS        = new BitmapFactory.Options();

    public static final String         PREVIEW_IMAGE_SERVICE = "PreviewImageService";

    public static abstract class DefaultImageProvider
    {
        protected PreviewImageManager mManager;

        public abstract void applyDefaultImage(ImageView view, boolean hires,
                boolean darkTheme);

        public abstract boolean applyDefaultImageOverlay(ImageView view, boolean hires,
                boolean darkTheme, Bitmap bitmap);

        /**
         * 
         * @since 29 août 2012 09:37:30
         * @author cte
         */
        public DefaultImageProvider(final PreviewImageManager manager)
        {
            this.mManager = manager;
        }
    }

    static float                             width_height;
    static int                               width_height_ps;
    protected static Drawable sBlank;


    /**
     * Requests the singleton instance of {@link AccountTypeManager} with data
     * bound from the available authenticators. This method can safely be called
     * from the UI thread.
     */
    public static PreviewImageManager getInstance(final Context context)
    {
        final Context applicationContext = context.getApplicationContext();
        PreviewImageManager service = (PreviewImageManager) applicationContext.getSystemService(PREVIEW_IMAGE_SERVICE);
        if (service == null)
        {
            service = createPreviewImageManager(applicationContext);
            Log.i("lbc", "No preview image service in context: ");
        }
        return service;
    }

    public static synchronized PreviewImageManager createPreviewImageManager(
            final Context context)
    {

        width_height = context.getResources().getDimension(R.dimen.media_width);
        width_height_ps = context.getResources().getDimensionPixelSize(R.dimen.media_width);

        BITMAP_OPTIONS.inDither = false;
        BITMAP_OPTIONS.inPurgeable = true;

        sBlank = context.getResources().getDrawable(android.R.color.transparent);
        
        final PreviewImageManagerImpl previewImageManagerImpl = new PreviewImageManagerImpl(context);
        context.registerReceiver(previewImageManagerImpl.getReceiver(), new IntentFilter(Intent.ACTION_MEDIA_MOUNTED));

        return previewImageManagerImpl;
    }

    abstract void applyBitmap(final ImageView view, final Bitmap bitmap);


    /**
     * Load photo into the supplied image view. If the photo is already cached,
     * it is displayed immediately. Otherwise a request is sent to load the
     * photo from the database.
     */
    public abstract void loadPhoto(ImageView view, long photoId, boolean hires,
            boolean darkTheme, boolean keepRatio, DefaultImageProvider defaultProvider);

    /**
     * Calls {@link #loadPhoto(ImageView, long, boolean, boolean, DefaultImageProvider)} with {@link PreviewImageManagerImpl#DEFAULT_IMAGE}.
     */
    public final void loadPhoto(final ImageView view, final long photoId, final boolean hires,
            final boolean darkTheme, final boolean keepRatio)
    {
        loadPhoto(view, photoId, hires, darkTheme, keepRatio, PreviewImageManager.DEFAULT_IMAGE);
    }

    /**
     * Load photo into the supplied image view. If the photo is already cached,
     * it is displayed immediately. Otherwise a request is sent to load the
     * photo from the location specified by the URI.
     */
    public abstract void loadPhoto(ImageView view, Uri photoUri, boolean hires,
            boolean darkTheme, boolean keepRatio, DefaultImageProvider defaultProvider);

    /**
     * Calls {@link #loadPhoto(ImageView, Uri, boolean, boolean, DefaultImageProvider)} with {@link PreviewImageManagerImpl#DEFAULT_IMAGE}.
     */
    public final void loadPhoto(final ImageView view, final Uri photoUri, final boolean hires,
            final boolean darkTheme, boolean keepRatio)
    {
        loadPhoto(view, photoUri, hires, darkTheme, keepRatio, PreviewImageManager.DEFAULT_IMAGE);
    }

    /**
     * Remove photo from the supplied image view. This also cancels current
     * pending load request inside this photo manager.
     */
    public abstract void removePhoto(ImageView view);

    /**
     * Temporarily stops loading photos from the database.
     */
    public abstract void pause();

    /**
     * Resumes loading photos from the database.
     */
    public abstract void resume();

    /**
     * Marks all cached photos for reloading. We can continue using cache but
     * should also make sure the photos haven't changed in the background and
     * notify the views if so.
     */
    public abstract void refreshCache();

    /**
     * Initiates a background process that over time will fill up cache with
     * preload photos.
     */
    // public abstract void preloadPhotosInBackground();
}


class PreviewImageManagerImpl extends PreviewImageManager implements Callback
{
    private static final String LOADER_THREAD_NAME      = "ContactPhotoLoader";

    /**
     * Type of message sent by the UI thread to itself to indicate that some
     * photos need to be loaded.
     */
    private static final int    MESSAGE_REQUEST_LOADING = 1;

    /**
     * Type of message sent by the loader thread to indicate that some photos
     * have been loaded.
     */
    private static final int    MESSAGE_PHOTOS_LOADED   = 2;

    // private static final String[] EMPTY_STRING_ARRAY = new String[0];
    //
    // private static final String[] COLUMNS = new String[] { Photo._ID,
    // Photo.PHOTO };

    /**
     * Maintains the state of a particular photo.
     */
    private static class BitmapHolder
    {
        final byte[]      bytes;

        volatile boolean  fresh;
        Bitmap            bitmap;
        Reference<Bitmap> bitmapRef;

        public BitmapHolder(final byte[] bytes)
        {
            this.bytes = bytes;
            this.fresh = true;
        }
    }

    public static class PictureDefaultImageProvider extends DefaultImageProvider
    {
        private final Bitmap mDefaultImage;
        /**
         * @param manager
         * @since 29 août 2012 09:38:11
         * @author cte
         * @param defaultImage
         */
        public PictureDefaultImageProvider(final PreviewImageManager manager, final Bitmap defaultImage)
        {
            super(manager);
            this.mDefaultImage = defaultImage;
        }

        @Override
        public void applyDefaultImage(final ImageView view, final boolean hires,
                final boolean darkTheme)
        {
            // view.setImageResource(getDefaultAvatarResId(hires, darkTheme));
            this.mManager.applyBitmap(view, this.mDefaultImage);
        }

        @Override
        public boolean applyDefaultImageOverlay(final ImageView view, final boolean hires, final boolean darkTheme, final Bitmap bitmap)
        {
            // Do nothing
            return false;
        }
    }

    private final Context                               mContext;

    /**
     * An LRU cache for bitmap holders. The cache contains bytes for photos just
     * as they come from the database. Each holder has a soft reference to the
     * actual bitmap.
     */
    private final LruCache<Object, BitmapHolder>        mBitmapHolderCache;

    /**
     * Cache size threshold at which bitmaps will not be preloaded.
     */
    // private final int mBitmapHolderCacheRedZoneBytes;

    /**
     * Level 2 LRU cache for bitmaps. This is a smaller cache that holds the
     * most recently used bitmaps to save time on decoding them from bytes (the
     * bytes are stored in {@link #mBitmapHolderCache}.
     */
    private final LruCache<Object, Bitmap>              mBitmapCache;

    /**
     * A map from ImageView to the corresponding photo ID or uri, encapsulated
     * in a request. The request may swapped out before the photo loading
     * request is started.
     */
    private final ConcurrentHashMap<ImageView, Request> mPendingRequests   = new ConcurrentHashMap<ImageView, Request>();

    /**
     * Handler for messages sent to the UI thread.
     */
    private final Handler                               mMainThreadHandler = new Handler(this);

    /**
     * Thread responsible for loading photos from the database. Created upon the
     * first request.
     */
    private LoaderThread                                mLoaderThread;

    /**
     * A gate to make sure we only send one instance of MESSAGE_PHOTOS_NEEDED at
     * a time.
     */
    private boolean                                     mLoadingRequested;

    /**
     * Flag indicating if the image loading is paused.
     */
    private boolean                                     mPaused;
    
    /**
     * 
     */
    private boolean                              		mKeepRatio;

    private final BroadcastReceiver                     mUnmountReceiver   = new BroadcastReceiver()
    {
        @Override
        public void onReceive(final Context context, final Intent intent)
        {
            PreviewImageManagerImpl.this.mBitmapHolderCache.evictAll();
        }
    };

    PreviewImageManagerImpl(final Context context)
    {
        this.mContext = context;
        final Resources resources = context.getResources();

        final int maxSize = resources.getInteger(R.integer.config_photo_cache_max_bitmaps);
        this.mBitmapCache = new LruCache<Object, Bitmap>(maxSize);

        final int maxBytes = resources.getInteger(R.integer.config_photo_cache_max_bytes);
        this.mBitmapHolderCache = new LruCache<Object, BitmapHolder>(maxBytes) {
            @Override
            protected int sizeOf(final Object key, final BitmapHolder value)
            {
                return value.bytes != null ? value.bytes.length : 0;
            }
        };

        Bitmap defaultPictureScaleImage = BitmapFactory.decodeResource(resources, R.drawable.preview_image_default);
        defaultPictureScaleImage = Bitmap.createScaledBitmap(defaultPictureScaleImage, width_height_ps, width_height_ps, false);
        PreviewImageManager.DEFAULT_IMAGE = new PictureDefaultImageProvider(this, defaultPictureScaleImage);
    }

    // @Override
    // public void preloadPhotosInBackground() {
    // ensureLoaderThread();
    // mLoaderThread.requestPreloading();
    // }

    /**
     * @return
     * @since 19 juil. 2012 16:08:07
     * @author cte
     */
    public BroadcastReceiver getReceiver()
    {
        return this.mUnmountReceiver;
    }

    @Override
    public void loadPhoto(final ImageView view, final long photoId, final boolean hires,
            final boolean darkTheme, final boolean keepRatio, final DefaultImageProvider defaultProvider)
    {
    	this.mKeepRatio = keepRatio;
        if (photoId == 0)
        {
            // No photo is needed
            defaultProvider.applyDefaultImage(view, hires, darkTheme);
            this.mPendingRequests.remove(view);
        }
        else
        {
            loadPhotoByIdOrUri(view, Request.createFromId(photoId, hires,
                    darkTheme, defaultProvider));
        }
    }

    @Override
    public void loadPhoto(final ImageView view, final Uri photoUri, final boolean hires,
            final boolean darkTheme, final boolean keepRatio, final DefaultImageProvider defaultProvider)
    {
    	this.mKeepRatio = keepRatio;
        if (photoUri == null)
        {
            // No photo is needed
        	defaultProvider.applyDefaultImage(view, hires, darkTheme);
            this.mPendingRequests.remove(view);
        }
        else
        {
    		loadPhotoByIdOrUri(view, Request.createFromUri(photoUri, hires,
    				darkTheme, defaultProvider));
        }
    }

    private void loadPhotoByIdOrUri(final ImageView view, final Request request)
    {
        final boolean loaded = loadCachedPhoto(view, request);
        if (loaded)
        {
            this.mPendingRequests.remove(view);
        }
        else
        {
            this.mPendingRequests.put(view, request);
            if (!this.mPaused)
            {
                // Send a request to start loading photos
                requestLoading();
            }
        }
    }

    @Override
    public void removePhoto(final ImageView view)
    {
        view.setImageDrawable(null);
        this.mPendingRequests.remove(view);
    }

    @Override
    public void refreshCache()
    {
        for (final BitmapHolder holder : this.mBitmapHolderCache.snapshot().values())
        {
            holder.fresh = false;
        }
    }

    /**
     * Checks if the photo is present in cache. If so, sets the photo on the
     * view.
     * 
     * @return false if the photo needs to be (re)loaded from the provider.
     */
    private boolean loadCachedPhoto(final ImageView view, final Request request)
    {
        final BitmapHolder holder = this.mBitmapHolderCache.get(request.getKey());
        if (holder == null)
        {
            // The bitmap has not been loaded - should display the placeholder
            // image.
            request.applyDefaultImage(view);
            return false;
        }

        if (holder.bytes == null)
        {
            request.applyDefaultImage(view);
            return holder.fresh;
        }

        // Optionally decode bytes into a bitmap
        inflateBitmap(holder);

        // Append Video Play Overlay if necessary
        if (!request.applyDefaultImageOverlay(view, holder.bitmap))
        {
            // Else just put the image
            applyBitmap(view, holder.bitmap);
        }

        if (holder.bitmap != null)
        {
            // Put the bitmap in the LRU cache
            this.mBitmapCache.put(request, holder.bitmap);
        }

        // Soften the reference
        holder.bitmap = null;

        return holder.fresh;
    }

    /**
     * If necessary, decodes bytes stored in the holder to Bitmap. As long as
     * the bitmap is held either by {@link #mBitmapCache} or by a soft reference
     * in the holder, it will not be necessary to decode the bitmap.
     */
    private void inflateBitmap(final BitmapHolder holder)
    {
        final byte[] bytes = holder.bytes;
        if (bytes == null || bytes.length == 0)
        {
            return;
        }

        // Check the soft reference. If will be retained if the bitmap is also
        // in the LRU cache, so we don't need to check the LRU cache explicitly.
        if (holder.bitmapRef != null)
        {
            holder.bitmap = holder.bitmapRef.get();
            if (holder.bitmap != null)
            {
                return;
            }
        }

        try
        {

            final Bitmap bitmap = decodeThumbnail(bytes);
            holder.bitmap = bitmap;
            holder.bitmapRef = new SoftReference<Bitmap>(bitmap);
        }
        catch (final OutOfMemoryError e)
        {
            // Do nothing - the photo will appear to be missing
        }
    }

    /**
     * @param bytes
     * @return
     * @since 18 juil. 2012 15:52:55
     * @author cte
     */
    private Bitmap decodeThumbnail(final byte[] bytes)
    {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, BITMAP_OPTIONS);
        final int h = bitmap.getHeight();
        final int w = bitmap.getWidth();

        // if thumbnail is smaller than the targetted size, just return it
        if (!this.mKeepRatio || (h < width_height_ps && w < width_height_ps))
        {
            return bitmap;
        }

        // larger thumbnails need to be scaled down (with aspect-ratio
        // preserved)
        int offX, offY;
        if (h > w)
        {
            final int newH = (int) (width_height * h / w);
            bitmap = Bitmap.createScaledBitmap(bitmap, width_height_ps, newH, false);
            offX = 0;
            offY = (newH - width_height_ps) / 2;
        }
        else
        {
            final int newW = (int) (width_height * w / h);
            bitmap = Bitmap.createScaledBitmap(bitmap, newW, width_height_ps, false);
            offX = (newW - width_height_ps) / 2;
            offY = 0;
            
        }
        // make it a square, and return
        return Bitmap.createBitmap(bitmap, offX, offY, width_height_ps, width_height_ps);
    }

    public void clear()
    {
        this.mPendingRequests.clear();
        this.mBitmapHolderCache.evictAll();
    }

    @Override
    public void pause()
    {
        this.mPaused = true;
    }

    @Override
    public void resume()
    {
        this.mPaused = false;
        if (!this.mPendingRequests.isEmpty())
        {
            requestLoading();
        }
    }

    /**
     * Sends a message to this thread itself to start loading images. If the
     * current view contains multiple image views, all of those image views will
     * get a chance to request their respective photos before any of those
     * requests are executed. This allows us to load images in bulk.
     */
    private void requestLoading()
    {
        if (!this.mLoadingRequested)
        {
            this.mLoadingRequested = true;
            this.mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
        }
    }

    /**
     * Processes requests on the main thread.
     */
    @Override
    public boolean handleMessage(final Message msg)
    {
        switch (msg.what)
        {
            case MESSAGE_REQUEST_LOADING:
            {
                this.mLoadingRequested = false;
                if (!this.mPaused)
                {
                    ensureLoaderThread();
                    this.mLoaderThread.requestLoading();
                }
                return true;
            }

            case MESSAGE_PHOTOS_LOADED:
            {
                if (!this.mPaused)
                {
                    processLoadedImages();
                }
                return true;
            }
        }
        return false;
    }

    public void ensureLoaderThread()
    {
        if (this.mLoaderThread == null)
        {
            this.mLoaderThread = new LoaderThread(this.mContext.getContentResolver());
            this.mLoaderThread.start();
        }
    }

    /**
     * Goes over pending loading requests and displays loaded photos. If some of
     * the photos still haven't been loaded, sends another request for image
     * loading.
     */
    private void processLoadedImages()
    {
        final Iterator<ImageView> iterator = this.mPendingRequests.keySet().iterator();
        while (iterator.hasNext())
        {
            final ImageView view = iterator.next();
            final Request key = this.mPendingRequests.get(view);
            final boolean loaded = loadCachedPhoto(view, key);
            if (loaded)
            {
                iterator.remove();
            }
        }

        softenCache();

        if (!this.mPendingRequests.isEmpty())
        {
            requestLoading();
        }
    }

    /**
     * Removes strong references to loaded bitmaps to allow them to be garbage
     * collected if needed. Some of the bitmaps will still be retained by {@link #mBitmapCache}.
     */
    private void softenCache()
    {
        for (final BitmapHolder holder : this.mBitmapHolderCache.snapshot().values())
        {
            holder.bitmap = null;
        }
    }

    /**
     * Stores the supplied bitmap in cache.
     */
    private void cacheBitmap(final Object key, final byte[] bytes, final boolean preloading)
    {
        final BitmapHolder holder = new BitmapHolder(bytes);
        holder.fresh = true;

        // Unless this image is being preloaded, decode it right away while
        // we are still on the background thread.
        if (!preloading)
        {
            inflateBitmap(holder);
        }

        this.mBitmapHolderCache.put(key, holder);
    }

    /**
     * Populates an array of photo IDs that need to be loaded.
     */
    private void obtainPhotoIdsAndUrisToLoad(final Set<Long> photoIds,
            final Set<String> photoIdsAsStrings, final Set<Uri> uris)
    {
        photoIds.clear();
        photoIdsAsStrings.clear();
        uris.clear();

        /*
         * Since the call is made from the loader thread, the map could be
         * changing during the iteration. That's not really a problem:
         * ConcurrentHashMap will allow those changes to happen without throwing
         * exceptions. Since we may miss some requests in the situation of
         * concurrent change, we will need to check the map again once loading
         * is complete.
         */
        final Iterator<Request> iterator = this.mPendingRequests.values().iterator();
        while (iterator.hasNext())
        {
            final Request request = iterator.next();
            final BitmapHolder holder = this.mBitmapHolderCache.get(request);
            if (holder == null || !holder.fresh)
            {
                if (request.isUriRequest())
                {
                    uris.add(request.mUri);
                }
                else
                {
                    photoIds.add(request.mId);
                    photoIdsAsStrings.add(String.valueOf(request.mId));
                }
            }
        }
    }

    /**
     * The thread that performs loading of photos from the database.
     */
    private class LoaderThread extends HandlerThread implements Callback
    {
        private static final int      BUFFER_SIZE         = 1024 * 16;
        // private static final int MESSAGE_PRELOAD_PHOTOS = 0;
        private static final int      MESSAGE_LOAD_PHOTOS = 1;

        /**
         * A pause between preload batches that yields to the UI thread.
         */
        // private static final int PHOTO_PRELOAD_DELAY = 1000;

        /**
         * Number of photos to preload per batch.
         */
        // private static final int PRELOAD_BATCH = 25;

        /**
         * Maximum number of photos to preload. If the cache size is 2Mb and the
         * expected average size of a photo is 4kb, then this number should be
         * 2Mb/4kb = 500.
         */
        // private static final int MAX_PHOTOS_TO_PRELOAD = 100;

        // private final StringBuilder mStringBuilder = new StringBuilder();
        private final Set<Long>       mPhotoIds           = new HashSet<Long>();
        private final Set<String>     mPhotoIdsAsStrings  = new HashSet<String>();
        private final Set<Uri>        mPhotoUris          = new HashSet<Uri>();
        // private final List<Long> mPreloadPhotoIds = new ArrayList<Long>();

        private Handler               mLoaderThreadHandler;
        private byte                  mBuffer[];

        // private static final int PRELOAD_STATUS_NOT_STARTED = 0;
        // private static final int PRELOAD_STATUS_IN_PROGRESS = 1;
        // private static final int PRELOAD_STATUS_DONE = 2;
        //
        // private int mPreloadStatus = PRELOAD_STATUS_NOT_STARTED;

        public LoaderThread(final ContentResolver resolver)
        {
            super(LOADER_THREAD_NAME);
        }

        public void ensureHandler()
        {
            if (this.mLoaderThreadHandler == null)
            {
                this.mLoaderThreadHandler = new Handler(getLooper(), this);
            }
        }

        /**
         * Kicks off preloading of the next batch of photos on the background
         * thread. Preloading will happen after a delay: we want to yield to the
         * UI thread as much as possible.
         * <p>
         * If preloading is already complete, does nothing.
         */
        // public void requestPreloading() {
        // if (mPreloadStatus == PRELOAD_STATUS_DONE) {
        // return;
        // }
        //
        // ensureHandler();
        // if (mLoaderThreadHandler.hasMessages(MESSAGE_LOAD_PHOTOS)) {
        // return;
        // }
        //
        // mLoaderThreadHandler.sendEmptyMessageDelayed(
        // MESSAGE_PRELOAD_PHOTOS, PHOTO_PRELOAD_DELAY);
        // }

        /**
         * Sends a message to this thread to load requested photos. Cancels a
         * preloading request, if any: we don't want preloading to impede
         * loading of the photos we need to display now.
         */
        public void requestLoading()
        {
            ensureHandler();
            // mLoaderThreadHandler.removeMessages(MESSAGE_PRELOAD_PHOTOS);
            this.mLoaderThreadHandler.sendEmptyMessage(MESSAGE_LOAD_PHOTOS);
        }

        /**
         * Receives the above message, loads photos and then sends a message to
         * the main thread to process them.
         */
        @Override
        public boolean handleMessage(final Message msg)
        {
            switch (msg.what)
            {
                // case MESSAGE_PRELOAD_PHOTOS:
                // preloadPhotosInBackground();
                // break;
                case MESSAGE_LOAD_PHOTOS:
                    loadPhotosInBackground();
                    break;
            }
            return true;
        }

        /**
         * The first time it is called, figures out which photos need to be
         * preloaded. Each subsequent call preloads the next batch of photos and
         * requests another cycle of preloading after a delay. The whole process
         * ends when we either run out of photos to preload or fill up cache.
         */
        // private void preloadPhotosInBackground() {
        // if (mPreloadStatus == PRELOAD_STATUS_DONE) {
        // return;
        // }
        //
        // if (mPreloadStatus == PRELOAD_STATUS_NOT_STARTED) {
        // queryPhotosForPreload();
        // if (mPreloadPhotoIds.isEmpty()) {
        // mPreloadStatus = PRELOAD_STATUS_DONE;
        // } else {
        // mPreloadStatus = PRELOAD_STATUS_IN_PROGRESS;
        // }
        // requestPreloading();
        // return;
        // }
        //
        // if (mBitmapHolderCache.size() > mBitmapHolderCacheRedZoneBytes) {
        // mPreloadStatus = PRELOAD_STATUS_DONE;
        // return;
        // }
        //
        // mPhotoIds.clear();
        // mPhotoIdsAsStrings.clear();
        //
        // int count = 0;
        // int preloadSize = mPreloadPhotoIds.size();
        // while (preloadSize > 0 && mPhotoIds.size() < PRELOAD_BATCH) {
        // preloadSize--;
        // count++;
        // Long photoId = mPreloadPhotoIds.get(preloadSize);
        // mPhotoIds.add(photoId);
        // mPhotoIdsAsStrings.add(photoId.toString());
        // mPreloadPhotoIds.remove(preloadSize);
        // }
        //
        // // loadPhotosFromDatabase(true);
        //
        // if (preloadSize == 0) {
        // mPreloadStatus = PRELOAD_STATUS_DONE;
        // }
        //
        // Log.v(TAG, "Preloaded " + count + " photos.  Cached bytes: "
        // + mBitmapHolderCache.size());
        //
        // requestPreloading();
        // }

        // private void queryPhotosForPreload() {
        // Cursor cursor = null;
        // try {
        // Uri uri = Contacts.CONTENT_URI
        // .buildUpon()
        // .appendQueryParameter(
        // ContactsContract.DIRECTORY_PARAM_KEY,
        // String.valueOf(Directory.DEFAULT))
        // .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY,
        // String.valueOf(MAX_PHOTOS_TO_PRELOAD)).build();
        // cursor = mResolver.query(uri,
        // new String[] { Contacts.PHOTO_ID }, Contacts.PHOTO_ID
        // + " NOT NULL AND " + Contacts.PHOTO_ID + "!=0",
        // null, Contacts.STARRED + " DESC, "
        // + Contacts.LAST_TIME_CONTACTED + " DESC");
        //
        // if (cursor != null) {
        // while (cursor.moveToNext()) {
        // // Insert them in reverse order, because we will be
        // // taking
        // // them from the end of the list for loading.
        // mPreloadPhotoIds.add(0, cursor.getLong(0));
        // }
        // }
        // } finally {
        // if (cursor != null) {
        // cursor.close();
        // }
        // }
        // }

        private void loadPhotosInBackground()
        {
            obtainPhotoIdsAndUrisToLoad(this.mPhotoIds, this.mPhotoIdsAsStrings,
                    this.mPhotoUris);
            // loadPhotosFromDatabase(false);
            loadRemotePhotos();
            // requestPreloading();
        }

        // private void loadPhotosFromDatabase(boolean preloading) {
        // if (mPhotoIds.isEmpty()) {
        // return;
        // }
        //
        // // Remove loaded photos from the preload queue: we don't want
        // // the preloading process to load them again.
        // if (!preloading && mPreloadStatus == PRELOAD_STATUS_IN_PROGRESS) {
        // for (Long id : mPhotoIds) {
        // mPreloadPhotoIds.remove(id);
        // }
        // if (mPreloadPhotoIds.isEmpty()) {
        // mPreloadStatus = PRELOAD_STATUS_DONE;
        // }
        // }
        //
        // mStringBuilder.setLength(0);
        // mStringBuilder.append(Photo._ID + " IN(");
        // for (int i = 0; i < mPhotoIds.size(); i++) {
        // if (i != 0) {
        // mStringBuilder.append(',');
        // }
        // mStringBuilder.append('?');
        // }
        // mStringBuilder.append(')');
        //
        // Cursor cursor = null;
        // try {
        // cursor = mResolver.query(Data.CONTENT_URI, COLUMNS,
        // mStringBuilder.toString(),
        // mPhotoIdsAsStrings.toArray(EMPTY_STRING_ARRAY), null);
        //
        // if (cursor != null) {
        // while (cursor.moveToNext()) {
        // Long id = cursor.getLong(0);
        // byte[] bytes = cursor.getBlob(1);
        // cacheBitmap(id, bytes, preloading);
        // mPhotoIds.remove(id);
        // }
        // }
        // } finally {
        // if (cursor != null) {
        // cursor.close();
        // }
        // }
        //
        // // Remaining photos were not found in the contacts database (but
        // // might be in profile).
        // for (Long id : mPhotoIds) {
        // if (ContactsContract.isProfileId(id)) {
        // Cursor profileCursor = null;
        // try {
        // profileCursor = mResolver.query(ContentUris
        // .withAppendedId(Data.CONTENT_URI, id), COLUMNS,
        // null, null, null);
        // if (profileCursor != null
        // && profileCursor.moveToFirst()) {
        // cacheBitmap(profileCursor.getLong(0),
        // profileCursor.getBlob(1), preloading);
        // } else {
        // // Couldn't load a photo this way either.
        // cacheBitmap(id, null, preloading);
        // }
        // } finally {
        // if (profileCursor != null) {
        // profileCursor.close();
        // }
        // }
        // } else {
        // // Not a profile photo and not found - mark the cache
        // // accordingly
        // cacheBitmap(id, null, preloading);
        // }
        // }
        //
        // mMainThreadHandler.sendEmptyMessage(MESSAGE_PHOTOS_LOADED);
        // }

        private void loadRemotePhotos()
        {
            for (final Uri uri : this.mPhotoUris)
            {
                if (this.mBuffer == null)
                {
                    this.mBuffer = new byte[BUFFER_SIZE];
                }
                try
                {
                    final InputStream is; 
                    //= this.mResolver.openInputStream(uri);
                    
                    
                    URL url = new URL(uri.toString());
                    is  = (InputStream)url.getContent();
                    
                    
                    if (is != null)
                    {
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try
                        {
                            int size;
                            while ((size = is.read(this.mBuffer)) != -1)
                            {
                                baos.write(this.mBuffer, 0, size);
                            }
                        }
                        finally
                        {
                            is.close();
                        }
                        cacheBitmap(uri, baos.toByteArray(), false);
                        PreviewImageManagerImpl.this.mMainThreadHandler
                        .sendEmptyMessage(MESSAGE_PHOTOS_LOADED);
                    }
                    else
                    {
                        Log.i("lbc", "Cannot load photo: " +  uri);
                        cacheBitmap(uri, null, false);
                    }
                }
                catch (final Exception ex)
                {
                    Log.i("lbc", "Cannot load photo " +  uri  + " : "+ ex.getMessage());
                    cacheBitmap(uri, null, false);
                }
            }
        }
    }

    /**
     * A holder for either a Uri or an id and a flag whether this was requested
     * for the dark or light theme
     */
    private static final class Request
    {
        private final long                 mId;
        private final Uri                  mUri;
        private final boolean              mDarkTheme;
        private final boolean              mHires;
        private final DefaultImageProvider mDefaultProvider;

        private Request(final long id, final Uri uri, final boolean hires, final boolean darkTheme,
                final DefaultImageProvider defaultProvider)
        {
            this.mId = id;
            this.mUri = uri;
            this.mDarkTheme = darkTheme;
            this.mHires = hires;
            this.mDefaultProvider = defaultProvider;
        }

        public static Request createFromId(final long id, final boolean hires,
                final boolean darkTheme, final DefaultImageProvider defaultProvider)
        {
            return new Request(id, null /* no URI */, hires, darkTheme,
                    defaultProvider);
        }

        public static Request createFromUri(final Uri uri, final boolean hires,
                final boolean darkTheme, final DefaultImageProvider defaultProvider)
        {
            return new Request(0 /* no ID */, uri, hires, darkTheme,
                    defaultProvider);
        }

        // public boolean isDarkTheme() {
        // return mDarkTheme;
        // }
        //
        // public boolean isHires() {
        // return mHires;
        // }

        public boolean isUriRequest()
        {
            return this.mUri != null;
        }

        @Override
        public int hashCode()
        {
            if (this.mUri != null)
            {
                return this.mUri.hashCode();
            }

            // copied over from Long.hashCode()
            return (int) (this.mId ^ (this.mId >>> 32));
        }

        @Override
        public boolean equals(final Object o)
        {
            if (!(o instanceof Request))
            {
                return false;
            }
            final Request that = (Request) o;
            // Don't compare equality of mHires and mDarkTheme fields because
            // these are only used
            // in the default contact photo case. When the contact does have a
            // photo, the contact
            // photo is the same regardless of mHires and mDarkTheme, so we
            // shouldn't need to put
            // the photo request on the queue twice.
            
            
            
            return this.mId == that.mId && areEqual(this.mUri, that.mUri);
        }
        
        
        public static boolean areEqual(Uri uri1, Uri uri2)
        {
            if (uri1 == null && uri2 == null)
            {
                return true;
            }
            if (uri1 == null || uri2 == null)
            {
                return false;
            }
            return uri1.equals(uri2);
        }

        public Object getKey()
        {
            return this.mUri == null ? this.mId : this.mUri;
        }

        public void applyDefaultImage(final ImageView view)
        {
            this.mDefaultProvider.applyDefaultImage(view, this.mHires, this.mDarkTheme);
        }

        public boolean applyDefaultImageOverlay(final ImageView view, final Bitmap bitmap)
        {
            return this.mDefaultProvider.applyDefaultImageOverlay(view, this.mHires, this.mDarkTheme, bitmap);
        }
    }

    /* (non-Javadoc)
     * @see com.synchronoss.contenthub.utils.PreviewImageManager#applyBitmap(android.widget.ImageView, android.graphics.Bitmap)
     * @since 29 août 2012 09:08:42
     */
    @Override
    void applyBitmap(final ImageView view, final Bitmap bitmap)
    {
        final BitmapDrawable bm = new BitmapDrawable(view.getResources(), bitmap);

        // todo: it's be better to update the layer 0, but calling .setDrawableByLayerId doesn't trigger a redraw of view
        final Drawable drawable = view.getDrawable();
        Drawable layer;
        if (null != drawable && drawable instanceof LayerDrawable)
        {
            layer = ((LayerDrawable) drawable).getDrawable(1);
        }
        else
        {
            layer = sBlank;
        }

        final Drawable[] layers = new Drawable[] { bm, layer };
        final LayerDrawable ld = new LayerDrawable(layers);
        ld.setId(0, 0);
        ld.setId(1, 1);
        view.setImageDrawable(ld);
    }
}
