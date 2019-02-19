package com.google.games.bridge;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;


class AchievementUiRequest implements HelperFragment.Request {
    private static final String TAG = "AchievementUiRequest";

    /** 
     * Should be aligned to:
     * PluginDev/Assets/GooglePlayGames/BasicApi/CommonTypes.cs enum UIStatus
     * */ 
    static final int UI_STATUS_VALID = 1;
    static final int UI_STATUS_INTERNAL_ERROR = -2;
    static final int UI_STATUS_NOT_AUTHORIZED = -3;
    static final int UI_STATUS_UI_BUSY = -12;

    private final TaskCompletionSource<Integer> resultTaskSource = new TaskCompletionSource<>();

    public Task<Integer> getTask() {
        return resultTaskSource.getTask();
    }

    public void process(final HelperFragment helperFragment) {
        final Activity activity = helperFragment.getActivity();
        GoogleSignInAccount account = HelperFragment.getAccount(activity);
        AchievementsClient achievementClient = Games.getAchievementsClient(activity, account);
        achievementClient
            .getAchievementsIntent()
            .addOnSuccessListener(
                activity,
                    new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            helperFragment.startActivityForResult(intent, HelperFragment.RC_ACHIEVEMENT_UI);
                        }
                    })
            .addOnFailureListener(
                activity,
                    new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            setFailure(e);
                        }
                    });
      

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == HelperFragment.RC_ACHIEVEMENT_UI) {
            if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) {
                setResult(UI_STATUS_VALID);
            } else if (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
                setResult(UI_STATUS_NOT_AUTHORIZED);
            } else {
                Log.d(TAG, "AchievementUiRequest.onActivityResult unknown resultCode: " + resultCode);
                setResult(UI_STATUS_INTERNAL_ERROR);
            }
        }
    }

    void setResult(Integer result) {
        resultTaskSource.setResult(result);
        HelperFragment.finishRequest(this);
    }

    void setFailure(Exception e) {
        resultTaskSource.setException(e);
        HelperFragment.finishRequest(this);
    }
}
