package com.multi.producthunt.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.multi.producthunt.MR
import com.multi.producthunt.android.screen.discussions.DiscussionListScreen

object DiscussionsTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val title = stringResource(id = MR.strings.discussion_tab_title.resourceId)
            val icon = rememberVectorPainter(Icons.Default.RecordVoiceOver)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        Box(modifier = Modifier.statusBarsPadding()) {
            Navigator(screen = DiscussionListScreen())
        }
    }

}