package com.mobiledeveloper.vktube.ui.screens.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mobiledeveloper.vktube.R
import com.mobiledeveloper.vktube.ui.screens.video.models.VideoViewState
import com.mobiledeveloper.vktube.ui.theme.Fronton

@Composable
fun CommentsScreen(
    viewState: VideoViewState,
    onCloseClick: () -> Unit,
    onSendClick: (String) -> Unit
) {
    Column {
        CommentsHeaderView(count = viewState.video?.viewsCount.orEmpty())
        CommentsAddView(viewState, onSendClick)
        Divider(thickness = 1.dp, color = Fronton.color.controlMinor)
        CommentsList(viewState)
    }
}

@Composable
private fun CommentsHeaderView(count: String) {
    Column(
        modifier = Modifier
            .background(Fronton.color.backgroundSecondary)
            .height(80.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Комментарии",
                style = Fronton.typography.headings.h3,
                color = Fronton.color.textPrimary
            )

            Text(
                modifier = Modifier.padding(start = 12.dp),
                text = count
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Divider(thickness = 1.dp, color = Fronton.color.controlMinor)
    }
}

@Composable
private fun CommentsAddView(viewState: VideoViewState, onSendClick: (String) -> Unit) {
    var textState by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier
                .padding(start = 16.dp, end = 8.dp)
                .size(40.dp)
                .clip(CircleShape),
            model = viewState.currentUser?.avatar.orEmpty(),
            contentDescription = "comment user image preview",
            contentScale = ContentScale.Crop
        )

        TextField(
            modifier = Modifier.weight(1f),
            value = textState,
            onValueChange = {
                textState = it
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Fronton.color.backgroundPrimary,
                textColor = Fronton.color.textPrimary,
                unfocusedIndicatorColor = Fronton.color.backgroundPrimary,
                focusedIndicatorColor = Fronton.color.backgroundPrimary
            ),
            placeholder = { Text("Введите текст комментария") }
        )

        Icon(
            modifier = Modifier
                .clickable {
                    onSendClick.invoke(textState)
                    textState = ""
                }
                .padding(16.dp)
                .size(24.dp),
            painter = painterResource(id = R.drawable.ic_baseline_send_24),
            contentDescription = "Send",
            tint = Fronton.color.controlPrimary
        )
    }
}

@Composable
fun CommentsList(viewState: VideoViewState) {
    LazyColumn {
        viewState.comments.forEach {
            item {
                CommentCell(it)
            }
        }
    }
}