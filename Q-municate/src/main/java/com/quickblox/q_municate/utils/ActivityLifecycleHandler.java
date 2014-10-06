package com.quickblox.q_municate.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.quickblox.internal.core.helper.Lo;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.qb.commands.QBLoginAndJoinDialogsCommand;
import com.quickblox.q_municate.qb.commands.QBLogoutAndDestroyChatCommand;
import com.quickblox.q_municate.ui.base.QBLogeable;

public class ActivityLifecycleHandler implements Application.ActivityLifecycleCallbacks {

    private static final boolean SHOULD_START_MULTICHAT = true;
    private int numberOfActivitiesInForeground;
    private boolean chatDestroyed = false;
    Lo lo = new Lo(this);

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityStarted(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
        lo.g("onActivityResumed" + numberOfActivitiesInForeground);
        //Count only our app logeable activity
        boolean activityLogeable = isActivityLogeable(activity);
        chatDestroyed = chatDestroyed && !isLogedIn();
        if (numberOfActivitiesInForeground == 0 && chatDestroyed && activityLogeable) {
            boolean canLogin = chatDestroyed && AppSession.getSession().isSessionExist();
            if (canLogin) {
                QBLoginAndJoinDialogsCommand.start(activity);
            }
        }
        if (activityLogeable) {
            ++numberOfActivitiesInForeground;
        }
    }

    public boolean isActivityLogeable(Activity activity) {
        return (activity instanceof QBLogeable);
    }

    public void onActivityPaused(Activity activity) {
    }

    public void onActivityStopped(Activity activity) {
        //Count only our app logeable activity
        if (activity instanceof QBLogeable) {
            --numberOfActivitiesInForeground;
        }
        lo.g("onActivityStopped" + numberOfActivitiesInForeground);

        if (numberOfActivitiesInForeground == 0 && activity instanceof QBLogeable) {
            boolean isLogedIn = isLogedIn();
            if (!isLogedIn) {
                return;
            }
            chatDestroyed = ((QBLogeable) activity).isCanPerformLogoutInOnStop();
            if (chatDestroyed) {
                QBLogoutAndDestroyChatCommand.start(activity, false);
            }
        }
    }

    private boolean isLogedIn() {
        boolean result = false;
        try {
            result = QBChatService.getInstance()
                    .isLoggedIn(); //Chat service isn't initialized  -> return false
        } catch (IllegalStateException e) {
            ErrorUtils.logError(e);
        }
        return result;
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityDestroyed(Activity activity) {
    }
}