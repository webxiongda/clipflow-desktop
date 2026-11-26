package com.clipflow.app

interface RatingPromptManager {

    fun trackAppLaunch()

    fun trackSignificantAction()

    fun checkAndShowRatingPrompt(): Boolean
}
