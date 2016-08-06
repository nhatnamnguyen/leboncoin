/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.nhatnam.android.leboncoin.utils;

import java.lang.ref.WeakReference;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;

/**
 * Slightly more abstract {@link AsyncQueryHandler} that helps keep a {@link WeakReference} back to a listener. Will properly close any {@link Cursor} if the listener ceases to exist.
 * <p>
 * This pattern can be used to perform background queries without leaking {@link Context} objects.
 * 
 * @hide pending API council review
 */
public class NotifyingAsyncQueryHandler extends AsyncQueryHandler
{
    /**
     * Listener of async query.
     */
    private WeakReference<AsyncQueryListener> mListener;

    /**
     * Interface to listen for completed query operations.
     */
    public interface AsyncQueryListener
    {
        void onQueryComplete(int token, Object cookie, Cursor cursor);
    }

    public NotifyingAsyncQueryHandler(final Context context, final AsyncQueryListener listener)
    {
        super(context.getContentResolver());

        this.setQueryListener(listener);
    }

    /**
     * Assign the given {@link AsyncQueryListener} to receive query events from
     * asynchronous calls. Will replace any existing listener.
     */
    public void setQueryListener(final AsyncQueryListener listener)
    {
        this.mListener = new WeakReference<AsyncQueryListener>(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.content.AsyncQueryHandler#onQueryComplete(int,
     * java.lang.Object, android.database.Cursor)
     */
    @Override
    protected void onQueryComplete(final int token, final Object cookie, final Cursor cursor)
    {
        final AsyncQueryListener listener = mListener.get();

        if (null != listener)
        {
            listener.onQueryComplete(token, cookie, cursor);
        }
        else if (null != cursor)
        {
            cursor.close();
        }
    }
}
