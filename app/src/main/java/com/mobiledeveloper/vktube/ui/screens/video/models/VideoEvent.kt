package com.mobiledeveloper.vktube.ui.screens.video.models

sealed class VideoEvent {
    data class LaunchVideo(val videoId: Long?) : VideoEvent()
    data class SendComment(val comment: String) : VideoEvent()
    object CommentsClick : VideoEvent()
    object ClearAction : VideoEvent()
}