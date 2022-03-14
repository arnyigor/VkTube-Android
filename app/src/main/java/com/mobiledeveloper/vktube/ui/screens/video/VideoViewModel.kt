package com.mobiledeveloper.vktube.ui.screens.video

import androidx.lifecycle.viewModelScope
import com.mobiledeveloper.vktube.base.BaseViewModel
import com.mobiledeveloper.vktube.data.cache.InMemoryCache
import com.mobiledeveloper.vktube.data.comments.CommentsRepository
import com.mobiledeveloper.vktube.data.user.UserRepository
import com.mobiledeveloper.vktube.ui.screens.comments.CommentCellModel
import com.mobiledeveloper.vktube.ui.screens.comments.mapToCommentCellModel
import com.mobiledeveloper.vktube.ui.screens.video.models.VideoAction
import com.mobiledeveloper.vktube.ui.screens.video.models.VideoEvent
import com.mobiledeveloper.vktube.ui.screens.video.models.VideoViewState
import com.vk.dto.common.id.UserId
import com.vk.sdk.api.wall.dto.WallWallComment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val commentsRepository: CommentsRepository,
    private val userRepository: UserRepository
) : BaseViewModel<VideoViewState, VideoAction, VideoEvent>(
    VideoViewState()
) {

    private var videoId: Long? = null

    override fun obtainEvent(viewEvent: VideoEvent) {
        when (viewEvent) {
            is VideoEvent.LaunchVideo -> performVideoLaunch(viewEvent.videoId)
            is VideoEvent.SendComment -> performSendComment(viewEvent.comment)
            is VideoEvent.CommentsClick -> showComments()
            is VideoEvent.ClearAction -> clearAction()
        }
    }

    private fun performVideoLaunch(videoId: Long?) {
        this.videoId = videoId
        if (InMemoryCache.clickedVideos.isEmpty()) throw IllegalStateException("Can't show video without cache")

        viewModelScope.launch {
            val video = InMemoryCache.clickedVideos.first { it.videoId == videoId }
            val currentUser = userRepository.fetchLocalUser()

            updateState(
                viewState.copy(
                    video = video,
                    currentUser = currentUser
                )
            )

            try {
                val comments =
                    commentsRepository.fetchCommentsForVideo(videoId = video.videoId, count = 20)
                updateState(viewState.copy(
                    comments = comments.items.map { it.mapToCommentCellModel() }
                ))
            } catch (e: Exception) {
                println(e.localizedMessage)
            }
        }
    }

    private fun performSendComment(comment: String) {
        viewModelScope.launch {
            val storedUser = userRepository.fetchLocalUser()
            val userId = storedUser.userId
            val currentComments = viewState.comments.toMutableList()
            currentComments.add(
                CommentCellModel(
                    messageId = -1,
                    userId = userId,
                    text = comment,
                    dateAdded = "Только что",
                    userName = storedUser.name,
                    avatar = storedUser.avatar
                )
            )

            updateState(
                viewState.copy(
                    comments = currentComments
                )
            )

            videoId?.let {
                commentsRepository.addCommentForVideo(
                    userId = userId,
                    videoId = it,
                    comment = comment
                )
            }
        }
    }

    private fun showComments() {
        viewModelScope.launch {
            callAction(VideoAction.OpenComments)
        }
    }

    private fun clearAction() {
        viewModelScope.launch {
            callAction(null)
        }
    }
}