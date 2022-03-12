package com.mobiledeveloper.vktube.ui.screens.video

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mobiledeveloper.vktube.R
import com.mobiledeveloper.vktube.ui.common.cell.VideoCellModel
import com.mobiledeveloper.vktube.ui.common.views.VideoActionView
import com.mobiledeveloper.vktube.ui.screens.comments.CommentsScreen
import com.mobiledeveloper.vktube.ui.screens.video.models.VideoAction
import com.mobiledeveloper.vktube.ui.screens.video.models.VideoEvent
import com.mobiledeveloper.vktube.ui.screens.video.models.VideoViewState
import com.mobiledeveloper.vktube.ui.theme.Fronton
import com.vk.sdk.api.wall.dto.WallWallComment
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun VideoScreen(
    videoId: Long?,
    videoViewModel: VideoViewModel
) {
    val viewState by videoViewModel.viewStates().observeAsState(VideoViewState())
    val viewAction by videoViewModel.viewEffects().observeAsState()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val videoHeight = (screenWidth / 16) * 9
    val bottomSheetPeekHeight = screenHeight - videoHeight

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )
    var bottomSheetHeight by remember { mutableStateOf(0.dp) }
    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            CommentsScreen(viewState = viewState)
        },
        sheetPeekHeight = bottomSheetHeight
    ) {
        VideoScreenView(viewState = viewState, onCommentsClick = {
            videoViewModel.obtainEvent(VideoEvent.CommentsClick)
        })
    }

    LaunchedEffect(key1 = viewAction, block = {
        when (viewAction) {
            VideoAction.OpenComments -> {
                coroutineScope.launch {
                    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                        bottomSheetHeight = bottomSheetPeekHeight
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    } else {
                        bottomSheetHeight = 0.dp
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }
            }
        }

        videoViewModel.obtainEvent(VideoEvent.ClearAction)
    })

    LaunchedEffect(key1 = Unit, block = {
        videoViewModel.obtainEvent(VideoEvent.LaunchVideo(videoId))
    })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoScreenView(
    viewState: VideoViewState,
    onCommentsClick: () -> Unit
) {
    val video = viewState.video ?: return

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val videoHeight = (screenWidth / 16) * 9

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(Fronton.color.backgroundAccent)
                    .fillMaxWidth()
                    .height(videoHeight)
            )
        }

        item {
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                text = video.title,
                style = Fronton.typography.body.large.long,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
        }

        item {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                text = "${video.viewsCount} • ${video.dateAdded}",
                color = Fronton.color.textSecondary,
                style = Fronton.typography.body.medium.short,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
        }

        item {
            VideoActionsRow(
                video = video,
                onLikeClick = {

                },
            )
        }

        item {
            Divider(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp), thickness = 1.dp,
                color = Fronton.color.controlMinor
            )
        }

        item {
            VideoUserRow(video)
        }

        item {
            Divider(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp), thickness = 1.dp,
                color = Fronton.color.controlMinor
            )
        }

        item {
            VideoCommentsView(viewState.comments, video, onCommentsClick)
        }
    }
}

@Composable
private fun VideoActionsRow(
    video: VideoCellModel,
    onLikeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(Fronton.color.backgroundPrimary)
            .fillMaxWidth()
            .height(80.dp)
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        VideoActionView(
            res = R.drawable.ic_baseline_thumb_up_24,
            title = video.likes.toString(),
            isPressed = video.likesByMe,
            onClick = onLikeClick
        )
    }
}

@Composable
private fun VideoUserRow(video: VideoCellModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            model = video.userImage,
            contentDescription = "user image preview",
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = video.userName,
                color = Fronton.color.textPrimary,
                style = Fronton.typography.body.medium.long
            )
            Text(
                text = video.subscribers,
                color = Fronton.color.textSecondary,
                style = Fronton.typography.body.small.short
            )
        }
    }
}

@Composable
private fun VideoCommentsView(
    comments: List<WallWallComment>, video: VideoCellModel,
    onCommentsClick: () -> Unit
) {
    val hasComments = comments.count() > 0

    Column(
        modifier = Modifier
            .clickable { onCommentsClick.invoke() }
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)
    ) {
        Row {
            Text(
                text = "Комментарии",
                color = Fronton.color.textPrimary,
                style = Fronton.typography.body.small.short
            )

            if (hasComments) {
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = comments.count().toString(),
                    color = Fronton.color.textPrimary,
                    style = Fronton.typography.body.small.short
                )
            }
        }

        if (hasComments) {
            Row(modifier = Modifier.padding(top = 8.dp)) {
                AsyncImage(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    model = "https://sun1-30.userapi.com/s/v1/if1/HWVwYg9TvGZA1YCuBgOtSz3rb68518tAc8rH0SSdAdoGtsfF-YJ41XhlPJN0tmXhtryAjhGG.jpg?size=100x100&quality=96&crop=389,241,1069,1069&ava=1",
                    contentDescription = "comment user image preview",
                    contentScale = ContentScale.Crop
                )

                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "Если будет возможность через VPN выкладывай плиз и на ютуб. Я в Германии и буду смотреть. И таких как я очень много",
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    model = "https://sun1-30.userapi.com/s/v1/if1/HWVwYg9TvGZA1YCuBgOtSz3rb68518tAc8rH0SSdAdoGtsfF-YJ41XhlPJN0tmXhtryAjhGG.jpg?size=100x100&quality=96&crop=389,241,1069,1069&ava=1",
                    contentDescription = "comment user image preview",
                    contentScale = ContentScale.Crop
                )

                TextField(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                        .height(48.dp),
                    value = "Введите текст комментария",
                    onValueChange = { },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Fronton.color.backgroundSecondary,
                        textColor = Fronton.color.textPrimary,
                        unfocusedIndicatorColor = Fronton.color.backgroundSecondary,
                        focusedIndicatorColor = Fronton.color.backgroundSecondary
                    ),
                    textStyle = TextStyle(fontSize = 10.sp),
                    readOnly = true
                )
            }
        }
    }
}

