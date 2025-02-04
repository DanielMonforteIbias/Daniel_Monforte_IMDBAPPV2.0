package edu.pmdm.monforte_danielimdbapp.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.pmdm.monforte_danielimdbapp.database.FavoritesDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.sync.UsersSync;

public class AppLifecycleManager extends Application implements Application.ActivityLifecycleCallbacks{
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private FavoritesDatabaseHelper dbHelper;
    private UsersSync sync;

    private static final String PREF_NAME="Prefs";
    private static final String PREF_IS_LOGGED_IN="isLoggedIn";
    private static final int DELAY =1000;

    private final Handler handler =new Handler();
    private final Runnable logoutRunnable=new Runnable() {
        @Override
        public void run() {
            checkForPendingLogout();
        }
    };
    private final Runnable loginRunnable=new Runnable() {
        @Override
        public void run() {
            checkForPendingLogin();
        }
    };

    private boolean isInBackground=false;
    private boolean isActivityChangingConfigurations=false;
    private boolean isAppClosed=false;
    private int activityReferences=0;

    private void checkForPendingLogout(){
        boolean wasLoggedIn=preferences.getBoolean(PREF_IS_LOGGED_IN,false);
        if(wasLoggedIn){
            FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
            registerUserLogout(currentUser);
            editor.putBoolean(PREF_IS_LOGGED_IN,false);
            editor.apply();
        }
    }
    private void checkForPendingLogin(){
        boolean wasLoggedIn=preferences.getBoolean(PREF_IS_LOGGED_IN,false);
        if(!wasLoggedIn){
            FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
            registerUserLogin(currentUser);
            editor.putBoolean(PREF_IS_LOGGED_IN,true);
            editor.apply();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences=getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor=preferences.edit();
        dbHelper=new FavoritesDatabaseHelper(this);
        registerActivityLifecycleCallbacks(this);
        sync=new UsersSync(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (!isActivityChangingConfigurations) {
            if (activityReferences == 0) {
                checkForPendingLogin();
            }
            activityReferences++;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        isInBackground=false;
        handler.removeCallbacks(loginRunnable);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        isInBackground=true;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (!isActivityChangingConfigurations) {
            activityReferences--;
            if (activityReferences == 0) {
                isAppClosed = true;
                handler.postDelayed(logoutRunnable, DELAY);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        isActivityChangingConfigurations=activity.isChangingConfigurations();
    }

    @Override
    public void onTrimMemory(int level) {
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            checkForPendingLogout();
        }
        super.onTrimMemory(level);
    }

    private void registerUserLogout(FirebaseUser user){
        if(user==null) return;
        long time=System.currentTimeMillis();
        dbHelper.updateUserLogoutTime(user.getUid(),time);
        sync.addActivityLogoutToUser(user.getUid(),time);
    }

    private void registerUserLogin(FirebaseUser user){
        if(user==null) return;
        long time=System.currentTimeMillis();
        dbHelper.updateUserLoginTime(user.getUid(),time);
        sync.addActivityLoginToUser(user.getUid(),time);
    }
}
