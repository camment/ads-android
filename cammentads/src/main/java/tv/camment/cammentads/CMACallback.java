/*
 * Created by Camment OY on 07/19/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 */

package tv.camment.cammentads;

/**
 * A callback for {@link CMAAsyncClient}.
 */
public interface CMACallback<T> {

    /**
     * Invoked when the async operation has completed successfully.
     *
     * @param result The result, which the async operation returned.
     */
    void onSuccess(final T result);

    /**
     * Invoked when the async operation has completed with an exception.
     *
     * @param exception The error from the async operation.
     */
    void onException(final Exception exception);

}
