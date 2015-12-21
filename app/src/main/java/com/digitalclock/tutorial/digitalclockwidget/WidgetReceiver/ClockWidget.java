package com.digitalclock.tutorial.digitalclockwidget.WidgetReceiver;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

import com.digitalclock.tutorial.digitalclockwidget.R;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * Created by Osama on 12/21/2015.
 */
public class ClockWidget extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        context.startService(new Intent(UpdateService.ACTION_UPDATE));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        context.stopService(new Intent(context, UpdateService.class));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        context.startService(new Intent(UpdateService.ACTION_UPDATE));
    }

    public static final class UpdateService extends Service {

        public static String ACTION_UPDATE = "com.tutorial.digitalclockwidget.action.UPDATE";

        private final static IntentFilter sIntentFilter;

        private final static String FORMAT_12_HOURS = "h:mm";
        private final static String FORMAT_24_HOURS = "kk:mm";

        private String mTimeFormat;
        private String mDateFormat;
        private Calendar mCalendar;
        private String mAM, mPM;

        static {
            sIntentFilter = new IntentFilter();
            sIntentFilter.addAction(Intent.ACTION_TIME_TICK);
            sIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            sIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        }

        @Override
        public void onCreate() {
            super.onCreate();
            reinit();
            registerReceiver(mTimeChangedReceiver, sIntentFilter);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterReceiver(mTimeChangedReceiver);
        }

        @Override
        public void onStart(Intent intent, int startId) {
            super.onStart(intent, startId);
            if(ACTION_UPDATE.equals(intent.getAction()))
                update();
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private void update(){
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            final CharSequence time = DateFormat.format(mTimeFormat, mCalendar);
            final CharSequence date = DateFormat.format(mDateFormat, mCalendar);

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.Time, time);
            views.setTextViewText(R.id.Date, date);

            if(! is24HourMode(this)){
                final boolean isMorning = mCalendar.get(Calendar.AM_PM) == 0;
                views.setTextViewText(R.id.AM_PM, (isMorning ? mAM : mPM));
            } else {
                views.setTextViewText(R.id.AM_PM, "");
            }

            ComponentName widget = new ComponentName(this, ClockWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(widget, views);
        }

        private void reinit(){
            final String[] ampm = new DateFormatSymbols().getAmPmStrings();
            mDateFormat = getString(R.string.date_format);
            mTimeFormat = is24HourMode(this) ? FORMAT_24_HOURS : FORMAT_12_HOURS;
            mCalendar = Calendar.getInstance();
            mAM = ampm[0].toLowerCase();
            mPM = ampm[1].toLowerCase();
        }

        private static boolean is24HourMode(final Context context){
            return android.text.format.DateFormat.is24HourFormat(context);
        }

        private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if(action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED)){
                    reinit();
                }

                update();
            }
        };
    }

}



