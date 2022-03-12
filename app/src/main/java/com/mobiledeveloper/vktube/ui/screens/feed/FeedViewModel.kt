package com.mobiledeveloper.vktube.ui.screens.feed

import androidx.lifecycle.viewModelScope
import com.mobiledeveloper.vktube.base.BaseViewModel
import com.mobiledeveloper.vktube.data.cache.InMemoryCache
import com.mobiledeveloper.vktube.data.clubs.ClubsRepository
import com.mobiledeveloper.vktube.data.user.UserRepository
import com.mobiledeveloper.vktube.ui.common.cell.VideoCellModel
import com.mobiledeveloper.vktube.ui.common.cell.mapToVideoCellModel
import com.mobiledeveloper.vktube.ui.screens.feed.models.FeedAction
import com.mobiledeveloper.vktube.ui.screens.feed.models.FeedEvent
import com.mobiledeveloper.vktube.ui.screens.feed.models.FeedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val clubsRepository: ClubsRepository
) : BaseViewModel<FeedState, FeedAction, FeedEvent>(initialState = FeedState()) {
    override fun obtainEvent(viewEvent: FeedEvent) {
        when (viewEvent) {
            FeedEvent.ScreenShown -> fetchVideos()
            FeedEvent.ClearAction -> clearAction()
            is FeedEvent.VideoClicked -> obtainVideoClick(viewEvent.videoCellModel)
        }
    }

    private fun obtainVideoClick(videoCellModel: VideoCellModel) {
        viewModelScope.launch {
            InMemoryCache.clickedVideos.add(videoCellModel)
            callAction(FeedAction.OpenVideoDetail(videoCellModel.videoId))
        }
    }

    private fun clearAction() {
        viewModelScope.launch {
            callAction(null)
        }
    }

    private fun fetchVideos() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = clubsRepository.fetchVideos(count = 20)
            updateState(viewState.copy(
                items = response.items.mapNotNull {
                    it.mapToVideoCellModel(
                        userImage = response.groups?.get(0)?.photo200.orEmpty(),
                        userName = response.groups?.get(0)?.screenName.orEmpty()
                    )
                }
            ))
        }
    }
}