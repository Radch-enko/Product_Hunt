package com.multi.producthunt.android.screen.create_discussion

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.multi.producthunt.MR
import com.multi.producthunt.android.screen.authorization.AuthenticationMode
import com.multi.producthunt.android.ui.*
import com.multi.producthunt.ui.models.SelectableTopicUI
import kotlinx.coroutines.flow.collectLatest

class AddDiscussionScreen() : AndroidScreen() {

    @Composable
    override fun Content() {
        val viewModel: AddDiscussionViewModel = rememberScreenModel()
        val state by viewModel.state.collectAsState()
        val context = LocalContext.current
        val navigator = LocalNavigator.current

        LaunchedEffect(key1 = null, block = {
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    is AddDiscussionViewModel.Effect.Success -> {
                        Toast.makeText(context, MR.strings.success.resourceId, Toast.LENGTH_LONG)
                            .show()

                    }
                }
            }
        })

        when {
            state.isLoading -> {
                ProgressBar()
            }
            else -> {
                Box(
                    modifier = Modifier
                        .systemBarsPadding()
                        .imePadding()
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    AddDiscussionForm(
                        title = state.title,
                        description = state.description,
                        topics = state.topics,
                        isValid = state.isFormValid(),
                        isTopicsValid = state.isTopicsValid(),
                        handleEvent = viewModel::sendEvent
                    )
                }
            }
        }

        state.error?.let { error ->
            ErrorDialog(
                error = error,
                dismissError = {
                    viewModel.sendEvent(
                        AddDiscussionViewModel.Event.ErrorDismissed
                    )
                }
            )
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun AddDiscussionForm(
        title: String,
        description: String,
        isValid: Boolean,
        topics: List<SelectableTopicUI>,
        handleEvent: (event: AddDiscussionViewModel.Event) -> Unit,
        isTopicsValid: Boolean,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(MR.strings.creating_of_discussion.resourceId), fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextFieldDefault(
                value = title,
                onValueChange = { handleEvent(AddDiscussionViewModel.Event.TitleChanged(it)) },
                label = stringResource(
                    id = MR.strings.discussion_title.resourceId
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextFieldDefault(
                value = description,
                onValueChange = { handleEvent(AddDiscussionViewModel.Event.DescriptionChanged(it)) },
                label = stringResource(
                    id = MR.strings.discussion_description.resourceId
                ),
                singleLine = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            TitleMedium(text = stringResource(id = MR.strings.project_topics.resourceId))

            Spacer(modifier = Modifier.height(16.dp))

            TopicsSelector(
                topics,
                onToggle = { handleEvent(AddDiscussionViewModel.Event.TopicChanged(it)) })

            Spacer(modifier = Modifier.height(8.dp))

            Requirement(
                message = stringResource(id = MR.strings.topics_required.resourceId),
                satisfied = isTopicsValid
            )

            Spacer(modifier = Modifier.height(8.dp))

            Requirement(
                message = stringResource(id = MR.strings.all_fields_filled.resourceId),
                satisfied = isValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            ButtonDefault(
                text = stringResource(id = MR.strings.save.resourceId),
                onClick = { handleEvent(AddDiscussionViewModel.Event.Save) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid
            )
        }
    }

    @Composable
    fun TopicsSelector(
        topics: List<SelectableTopicUI>,
        onToggle: (topicUI: SelectableTopicUI) -> Unit
    ) {
        FlowRow(mainAxisSpacing = 16.dp) {
            topics.forEach { topicUI ->
                var selected by remember {
                    mutableStateOf(topicUI.selected)
                }
                CustomChip(selected = selected, text = topicUI.title, onToggle = {
                    selected = !selected
                    onToggle(topicUI)
                })
            }
        }
    }
}