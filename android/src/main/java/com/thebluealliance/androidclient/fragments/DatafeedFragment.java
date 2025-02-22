package com.thebluealliance.androidclient.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;

import com.google.android.gms.analytics.Tracker;
import com.thebluealliance.androidclient.activities.DatafeedActivity;
import com.thebluealliance.androidclient.api.ApiConstants;
import com.thebluealliance.androidclient.binders.AbstractDataBinder;
import com.thebluealliance.androidclient.binders.NoDataBinder;
import com.thebluealliance.androidclient.datafeed.CacheableDatafeed;
import com.thebluealliance.androidclient.datafeed.refresh.RefreshController;
import com.thebluealliance.androidclient.datafeed.refresh.RefreshController.RefreshType;
import com.thebluealliance.androidclient.datafeed.refresh.Refreshable;
import com.thebluealliance.androidclient.models.NoDataViewParams;
import com.thebluealliance.androidclient.subscribers.BaseAPISubscriber;
import com.thebluealliance.androidclient.subscribers.EventBusSubscriber;
import com.thebluealliance.androidclient.tracing.TracingController;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Easy abstraction of Fragment datafeed bindings
 *
 * @param <T> Type returned by the API
 * @param <V> Type to be bound to a view
 * @param <S> {@link BaseAPISubscriber} that will take API Data -> prepare data to render
 * @param <B> {@link AbstractDataBinder} that will take prepared data -> view
 */
public abstract class DatafeedFragment
        <T, V, S extends BaseAPISubscriber<T, V>, B extends AbstractDataBinder<V>>
        extends Fragment implements Refreshable {

    @Inject protected S mSubscriber;
    @Inject protected B mBinder;
    @Inject protected EventBus mEventBus;
    @Inject protected Lazy<EventBusSubscriber> mEventBusSubscriber;
    @Inject protected NoDataBinder mNoDataBinder;
    @Inject protected Tracker mAnalyticsTracker;
    @Inject protected TracingController mTracingController;
    @Inject protected CacheableDatafeed mDatafeed;

    protected @Nullable RefreshController mRefreshController;
    protected Observable<? extends T> mObservable;
    protected String mRefreshTag;
    protected boolean isCurrentlyVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() instanceof DatafeedActivity) {
            mRefreshController = ((DatafeedActivity) getActivity()).getRefreshController();
        } else {
            mRefreshController = null;
        }

        isCurrentlyVisible = false;
        mRefreshTag = getRefreshTag();
        mSubscriber.setConsumer(mBinder);
        mSubscriber.setRefreshController(mRefreshController);
        mSubscriber.setRefreshTag(mRefreshTag);
        mSubscriber.setTracker(mAnalyticsTracker);
        mSubscriber.setTracingController(mTracingController);
        mBinder.setActivity(getActivity());
        mBinder.setNoDataBinder(mNoDataBinder);
        mBinder.setNoDataParams(getNoDataParams());
    }

    @Override
    public void onResume() {
        super.onResume();
        getNewObservables(RefreshController.NOT_REQUESTED_BY_USER);
        if (mRefreshController != null) {
            mRefreshController.registerRefreshable(mRefreshTag, this);
        }
        if (shouldRegisterSubscriberToEventBus()) {
            mEventBus.register(mSubscriber);
        }
        if (shouldRegisterBinderToEventBus()) {
            mEventBus.register(mBinder);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRefreshController != null) {
            mRefreshController.unregisterRefreshable(mRefreshTag);
        }
        if (mSubscriber != null) {
            if (shouldRegisterSubscriberToEventBus()) {
                mEventBus.unregister(mSubscriber);
            }
        }
        if (mBinder != null) {
            if (shouldRegisterBinderToEventBus()) {
                mEventBus.unregister(mBinder);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSubscriber != null) {
            mSubscriber.onParentStop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinder.unbind(true);
    }

    /**
     * Allows other things to bind this instance
     */
    public void bind() {
        if (mSubscriber != null) {
            mSubscriber.bindData();
        }
    }

    public boolean isBound() {
        return mBinder != null && mBinder.isDataBound();
    }

    public void setShouldBindImmediately(boolean shouldBind) {
        if (mSubscriber != null) {
            mSubscriber.setShouldBindImmediately(shouldBind);
        }
    }

    public void setShouldBindOnce(boolean shouldBind) {
        if (mSubscriber != null) {
            mSubscriber.setShouldBindOnce(shouldBind);
        }
    }

    public void setIsCurrentlyVisible(boolean visible) {
        isCurrentlyVisible = visible;
    }

    /**
     * Registers and subscribes new observables
     */
    private void getNewObservables(@RefreshType int refreshType) {
        if (mSubscriber != null) {
            mObservable = getObservable(
                    refreshType == RefreshController.REQUESTED_BY_USER
                            ? ApiConstants.TBA_CACHE_WEB
                            : null);
            if (mObservable != null) {
                mObservable.subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe(mSubscriber);
                mSubscriber.onRefreshStart(refreshType);
            }

        }
    }

    @Override
    public void onRefreshStart(@RefreshType int refreshType) {
        if (mSubscriber != null && mBinder != null) {
            mBinder.unbind(false);
            setShouldBindOnce(isCurrentlyVisible);
            getNewObservables(refreshType);
        }
    }

    /**
     * For child to make a call to return the Observable containing the main data model
     * Called in {@link #onResume()}
     *
     * @param tbaCacheHeader String param to tell the datafeed how to load the data. Use
     *                       {@link ApiConstants#TBA_CACHE_WEB}, {@link ApiConstants#TBA_CACHE_LOCAL}, or {@code
     *                       null} for regular usage
     */
    protected abstract Observable<? extends T> getObservable(String tbaCacheHeader);

    /**
     * @return A string identifying what data this fragment is loading
     */
    protected abstract String getRefreshTag();

    @VisibleForTesting
    public NoDataViewParams getNoDataParams() {
        return null;
    }

    @VisibleForTesting
    public B getBinder() {
        return mBinder;
    }

    protected boolean shouldRegisterSubscriberToEventBus() {
        return false;
    }

    protected boolean shouldRegisterBinderToEventBus() {
        return false;
    }
}
