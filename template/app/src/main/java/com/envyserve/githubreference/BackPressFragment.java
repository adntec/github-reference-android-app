package com.envyserve.githubreference;

/**
 * Created by Mark
 */
public interface BackPressFragment {

    /**
     * Send the fact that the back button has been pressed to the fragment
     * @return true if the backpress has been handled or false if it has not been handled
     */
    boolean handleBackPress();
}
