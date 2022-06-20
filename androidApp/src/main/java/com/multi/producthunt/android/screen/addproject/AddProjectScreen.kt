package com.multi.producthunt.android.screen.addproject

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import coil.compose.rememberImagePainter
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.multi.producthunt.MR
import com.multi.producthunt.android.R
import com.multi.producthunt.android.screen.detail.DetailProjectScreen
import com.multi.producthunt.android.ui.ButtonDefault
import com.multi.producthunt.android.ui.CustomChip
import com.multi.producthunt.android.ui.DeleteButton
import com.multi.producthunt.android.ui.ErrorDialog
import com.multi.producthunt.android.ui.OutlinedButtonDefault
import com.multi.producthunt.android.ui.OutlinedTextFieldDefault
import com.multi.producthunt.android.ui.ProgressBar
import com.multi.producthunt.android.ui.Requirement
import com.multi.producthunt.android.ui.TitleMedium
import com.multi.producthunt.ui.models.SelectableTopicUI
import kotlinx.coroutines.flow.collectLatest

class AddProjectScreen(private val projectToRedact: Int = 0) : AndroidScreen() {

    @Composable
    override fun Content() {
        val viewModel: AddProjectViewModel = rememberScreenModel(arg = projectToRedact)
        val state by viewModel.state.collectAsState()
        val context = LocalContext.current
        val navigator = LocalNavigator.current

        LaunchedEffect(key1 = null, block = {
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    is AddProjectViewModel.Effect.Success -> {
                        Toast.makeText(context, MR.strings.success.resourceId, Toast.LENGTH_LONG)
                            .show()
                        navigator?.replace(DetailProjectScreen(effect.projectId))
                    }
                    AddProjectViewModel.Effect.SuccessDelete -> {
                        Toast.makeText(context, MR.strings.deleted.resourceId, Toast.LENGTH_LONG)
                            .show()
                        navigator?.popUntilRoot()
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
                    AddProjectForm(
                        name = state.name,
                        tagline = state.tagline,
                        description = state.description,
                        ownerLink = state.ownerLink,
                        thumbnail = state.thumbnail,
                        media = state.media,
                        topics = state.topics,
                        isValid = state.isFormValid(),
                        isThumbnailValid = state.thumbnail != null,
                        isMediaValid = state.media.isNotEmpty(),
                        isRedact = state.isRedact,
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
                        AddProjectViewModel.Event.ErrorDismissed
                    )
                }
            )
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun AddProjectForm(
        name: String,
        tagline: String,
        description: String,
        ownerLink: String,
        thumbnail: Uri?,
        media: List<Uri?>,
        isValid: Boolean,
        isThumbnailValid: Boolean,
        isMediaValid: Boolean,
        isRedact: Boolean,
        topics: List<SelectableTopicUI>,
        handleEvent: (event: AddProjectViewModel.Event) -> Unit,
        isTopicsValid: Boolean,
    ) {
        val launcherForProjectAvatarImage = rememberLauncherForActivityResult(
            contract =
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                handleEvent(AddProjectViewModel.Event.ThumbnailChanged(uri))
            }
        }

        val launcherMultipleImages = rememberLauncherForActivityResult(
            contract =
            ActivityResultContracts.GetMultipleContents()
        ) { list ->
            list.forEach { uri ->
                handleEvent(AddProjectViewModel.Event.MediaChanged(uri))
            }
        }

        val storagePermissions = rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            PickProjectAvatar(
                modifier = Modifier.align(CenterHorizontally),
                image = thumbnail
            ) {
                if (storagePermissions.allPermissionsGranted) {
                    launcherForProjectAvatarImage.launch("image/*")
                } else {
                    storagePermissions.launchMultiplePermissionRequest()
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextFieldDefault(
                value = name,
                onValueChange = { handleEvent(AddProjectViewModel.Event.NameChanged(it)) },
                label = stringResource(
                    id = MR.strings.project_name.resourceId
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextFieldDefault(
                value = tagline,
                onValueChange = { handleEvent(AddProjectViewModel.Event.TaglineChanged(it)) },
                label = stringResource(
                    id = MR.strings.project_tagline.resourceId
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextFieldDefault(
                value = description,
                onValueChange = { handleEvent(AddProjectViewModel.Event.DescriptionChanged(it)) },
                label = stringResource(
                    id = MR.strings.project_description.resourceId
                ),
                singleLine = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextFieldDefault(
                value = ownerLink,
                onValueChange = { handleEvent(AddProjectViewModel.Event.OwnerLinkChanged(it)) },
                label = stringResource(
                    id = MR.strings.project_ownerlink.resourceId
                ),
                singleLine = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            TitleMedium(text = stringResource(id = MR.strings.project_topics.resourceId))

            Spacer(modifier = Modifier.height(16.dp))

            TopicsSelector(
                topics,
                onToggle = { handleEvent(AddProjectViewModel.Event.TopicChanged(it)) })

            MediasPicker(media, onAddImages = {
                if (storagePermissions.allPermissionsGranted) {
                    launcherMultipleImages.launch("image/*")
                } else {
                    storagePermissions.launchMultiplePermissionRequest()
                }
            }, onDelete = {
                handleEvent(AddProjectViewModel.Event.MediaDeleted(it))
            })

            Spacer(modifier = Modifier.height(8.dp))

            Requirement(
                message = stringResource(id = MR.strings.topics_required.resourceId),
                satisfied = isTopicsValid
            )

            Spacer(modifier = Modifier.height(8.dp))

            Requirement(
                message = stringResource(id = MR.strings.thumbnail_required.resourceId),
                satisfied = isThumbnailValid
            )

            Spacer(modifier = Modifier.height(8.dp))

            Requirement(
                message = stringResource(id = MR.strings.media_list_required.resourceId),
                satisfied = isMediaValid
            )

            Spacer(modifier = Modifier.height(8.dp))

            Requirement(
                message = stringResource(id = MR.strings.all_fields_filled.resourceId),
                satisfied = isValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            ButtonDefault(
                text = stringResource(id = MR.strings.save.resourceId),
                onClick = { handleEvent(AddProjectViewModel.Event.SaveProject) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid
            )

            if (isRedact) {
                Spacer(modifier = Modifier.height(16.dp))

                var deleteDialogVisible by remember {
                    mutableStateOf(false)
                }
                DeleteButton(
                    text = stringResource(id = MR.strings.delete_project.resourceId),
                    onClick = {
                        deleteDialogVisible = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isValid
                )


                if (deleteDialogVisible) {
                    AlertDialog(title = {
                        Text(
                            text = stringResource(id = MR.strings.delete_project_dialog_title.resourceId),
                            fontSize = 18.sp
                        )
                    }, onDismissRequest = { deleteDialogVisible = false }, confirmButton = {
                        androidx.compose.material.TextButton(
                            onClick = {
                                deleteDialogVisible = false
                                handleEvent(AddProjectViewModel.Event.DeleteProject)
                            }
                        ) {
                            androidx.compose.material.Text(
                                text = stringResource(
                                    id = MR.strings.yes.resourceId
                                ).uppercase()
                            )
                        }
                    }, dismissButton = {
                        androidx.compose.material.TextButton(
                            onClick = {
                                deleteDialogVisible = false
                            }
                        ) {
                            androidx.compose.material.Text(
                                text = stringResource(
                                    id = MR.strings.cancel_button.resourceId
                                ).uppercase()
                            )
                        }
                    })
                }
            }
        }
    }

    @Composable
    fun MediasPicker(
        mediasList: List<Uri?>,
        onAddImages: () -> Unit,
        onDelete: (position: Int) -> Unit
    ) {
        Column {

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButtonDefault(
                text = stringResource(id = MR.strings.upload_medias.resourceId),
                onClick = onAddImages
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                itemsIndexed(mediasList.filterNotNull()) { index, item ->
                    MediaPickerImage(item, onDelete = { onDelete(index) })
                }
            }
        }
    }

    @Composable
    fun MediaPickerImage(image: Uri, onDelete: () -> Unit) {
        Surface(
            modifier = Modifier
                .size(70.dp)
                .clip(
                    RoundedCornerShape(16.dp)
                )
                .border(
                    BorderStroke(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Image(
                painter = rememberImagePainter(
                    data = image
                ), contentDescription = null,
                contentScale = ContentScale.Crop
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White)
            }
        }
    }

    @Composable
    fun PickProjectAvatar(modifier: Modifier, image: Uri?, onClick: () -> Unit) {
        val commonModifier = modifier
            .size(100.dp)
            .clip(CircleShape)
            .border(2.dp, Color.DarkGray, CircleShape)
            .background(MaterialTheme.colorScheme.primary)

        Box(modifier = modifier) {
            if (image != null) {
                Image(
                    painter = rememberImagePainter(
                        data = image
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = commonModifier,
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.image_svgrepo_com),
                    contentDescription = null,
                    contentScale = ContentScale.Inside,
                    modifier = commonModifier,
                )
            }

            IconButton(
                modifier = Modifier.align(BottomEnd),
                onClick = onClick
            ) {
                Icon(
                    Icons.Outlined.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
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