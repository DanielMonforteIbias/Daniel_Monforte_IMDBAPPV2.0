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

import edu.pmdm.monforte_danielimdbapp.database.UsersDatabaseHelper;
import edu.pmdm.monforte_danielimdbapp.sync.UsersSync;

public class AppLifecycleManager extends Application implements Application.ActivityLifecycleCallbacks{
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private UsersDatabaseHelper dbHelper;
    private UsersSync sync;

    private static final String PREF_NAME="Prefs";
    private static final String PREF_IS_LOGGED_IN="isLoggedIn";
    private static final int DELAY =1000;

    private final Handler handler =new Handler();
    private final Runnable logoutRunnable=new Runnable() {
        @Override
        public void run() {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                registerUserLogout(currentUser);
                editor.putBoolean(PREF_IS_LOGGED_IN,false);
                editor.apply();
            }
        }
    };
    private final Runnable loginRunnable=new Runnable() {
        @Override
        public void run() {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                registerUserLogin(currentUser);
                editor.putBoolean(PREF_IS_LOGGED_IN,true);
                editor.apply();
            }
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

    @Override
    public void onCreate() {
        super.onCreate();
        preferences=getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor=preferences.edit();
        dbHelper=new UsersDatabaseHelper(this);
        registerActivityLifecycleCallbacks(this);
        sync=new UsersSync(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if(!isActivityChangingConfigurations) activityReferences++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        isInBackground=false;
        handler.removeCallbacks(logoutRunnable);
        handler.postDelayed(loginRunnable, DELAY);
        editor.putBoolean(PREF_IS_LOGGED_IN,true);
        editor.apply();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        isInBackground=true;
        handler.postDelayed(logoutRunnable, DELAY);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if(!isActivityChangingConfigurations){
            activityReferences--;
            if(activityReferences==0){
                isAppClosed=true;
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
        if(level==TRIM_MEMORY_UI_HIDDEN){
            FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null){
                registerUserLogout(user);
                editor.putBoolean(PREF_IS_LOGGED_IN,false);
                editor.apply();
            }
        }
        super.onTrimMemory(level);
    }

    private void registerUserLogout(FirebaseUser user){
        if(user==null) return;
        dbHelper.updateUserLogoutTime(user.getUid(),System.currentTimeMillis());
        sync.addActivityLogToUser(user.getUid());
    }

    private void registerUserLogin(FirebaseUser user){
        if(user==null) return;
        dbHelper.updateUserLoginTime(user.getUid(),System.currentTimeMillis());
    }
}
