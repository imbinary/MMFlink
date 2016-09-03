package com.example.willi.mmflink;

import android.database.Cursor;
import android.util.Log;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.Calendar;

/**
 * Created by chris on 9/3/16.
 */
public class WalkReporter {

    private final HealthDataStore mStore;

    public WalkReporter(HealthDataStore store) {
        mStore = store;
    }

    public void start() {
        // Register an observer to listen changes of step count and get today step count
        HealthDataObserver.addObserver(mStore, HealthConstants.Exercise.HEALTH_DATA_TYPE, mObserver);
        readLastWalk();
    }

    // Read the today's step count on demand
    private void readLastWalk() {
        HealthDataResolver resolver = new HealthDataResolver(mStore, null);

        // Set time range from start time of today to the current time
        long startTime = getStartTimeOfToday();
        long endTime = System.currentTimeMillis();
        HealthDataResolver.Filter filter = HealthDataResolver.Filter.and(HealthDataResolver.Filter.greaterThanEquals(HealthConstants.StepCount.START_TIME, startTime),
                HealthDataResolver.Filter.lessThanEquals(HealthConstants.StepCount.START_TIME, endTime));

        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.Exercise.HEALTH_DATA_TYPE)
                .setProperties(new String[] {HealthConstants.Exercise.DISTANCE,HealthConstants.Exercise.DURATION})
                .setFilter(filter)
                .build();

        try {
            resolver.read(request).setResultListener(mListener);
        } catch (Exception e) {
            Log.e(MainActivity.APP_TAG, e.getClass().getName() + " - " + e.getMessage());
            Log.e(MainActivity.APP_TAG, "Getting step count fails.");
        }
    }

    private long getStartTimeOfToday() {
        Calendar today = Calendar.getInstance();

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return today.getTimeInMillis();
    }

    private final HealthResultHolder.ResultListener<HealthDataResolver.ReadResult> mListener = new HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>() {
        @Override
        public void onResult(HealthDataResolver.ReadResult result) {
            int count = 0;
            String time = "";
            Cursor c = null;

            try {
                c = result.getResultCursor();
                if (c != null) {
                    while (c.moveToNext()) {
                        count = c.getInt(c.getColumnIndex(HealthConstants.Exercise.DISTANCE));
                        time = c.getString(c.getColumnIndex(HealthConstants.Exercise.DURATION));
                    }
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            MainActivity.getInstance().drawWalk(String.valueOf(count),time);
        }
    };

    private final HealthDataObserver mObserver = new HealthDataObserver(null) {

        // Update the step count when a change event is received
        @Override
        public void onChange(String dataTypeName) {
            Log.d(MainActivity.APP_TAG, "Observer receives a data changed event");
            readLastWalk();
        }
    };

}
