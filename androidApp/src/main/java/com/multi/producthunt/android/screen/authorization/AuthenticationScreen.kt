package com.multi.producthunt.android.screen.authorization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.multi.producthunt.MR
import com.multi.producthunt.android.screen.profile.ProfileScreen
import com.multi.producthunt.android.ui.*
import kotlinx.coroutines.flow.collectLatest

class AuthenticationScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<AuthorizationViewModel>()
        val navigator = LocalNavigator.current
        LaunchedEffect(null) {
            viewModel.effect.collectLatest { effect ->
                when (effect) {
                    AuthorizationViewModel.Effect.AuthorizationSuccess -> navigator?.replace(
                        ProfileScreen()
                    )
                }
            }
        }


        AuthenticationContent(
            authenticationState = viewModel.state.collectAsState().value,
            handleEvent = viewModel::sendEvent
        )
    }

    @Composable
    fun AuthenticationContent(
        modifier: Modifier = Modifier,
        authenticationState: AuthenticationState,
        handleEvent: (event: AuthorizationViewModel.Event) -> Unit
    ) {

        AuthenticationForm(
            name = authenticationState.name,
            username = authenticationState.username,
            password = authenticationState.password,
            passwordAgain = authenticationState.passwordAgain,
            headline = authenticationState.headline,
            coverImage = authenticationState.coverImage,
            profileImage = authenticationState.profileImage,
            enableAuthentication = authenticationState.isFormValid(),
            enableRegistration = authenticationState.isRegistrationFormValid(),
            completedPasswordRequirements = authenticationState.passwordRequirements,
            OnUsernameChanged = {
                handleEvent(AuthorizationViewModel.Event.UsernameChanged(it))
            },
            authenticationMode = authenticationState.authenticationMode,
            OnPasswordChanged = {
                handleEvent(AuthorizationViewModel.Event.PasswordChanged(it))
            },
            OnPasswordAgainChanged = {
                handleEvent(AuthorizationViewModel.Event.PasswordAgainChanged(it))
            },
            OnNameChanged = {
                handleEvent(AuthorizationViewModel.Event.NameChanged(it))
            },
            OnHeadlineChanged = {
                handleEvent(AuthorizationViewModel.Event.HeadlineChanged(it.orEmpty()))
            },
            OnCoverImageChanged = {
                handleEvent(AuthorizationViewModel.Event.CoverImageChanged(it.orEmpty()))
            },
            OnProfileImageChanged = {
                handleEvent(AuthorizationViewModel.Event.ProfileImageChanged(it.orEmpty()))
            },
            onAuthenticate = {
                handleEvent(AuthorizationViewModel.Event.Authenticate)
            },
            onToggleMode = {
                handleEvent(
                    AuthorizationViewModel.Event.ToggleMode
                )
            }
        )
        authenticationState.error?.let { error ->
            ErrorDialog(
                error = error,
                dismissError = {
                    handleEvent(
                        AuthorizationViewModel.Event.ErrorDismissed
                    )
                }
            )
        }

    }

    @Composable
    fun AuthenticationForm(
        modifier: Modifier = Modifier,
        authenticationMode: AuthenticationMode,
        name: String,
        username: String,
        headline: String? = null,
        coverImage: String? = null,
        profileImage: String? = null,
        password: String,
        passwordAgain: String,
        completedPasswordRequirements: List<PasswordRequirements>,
        enableAuthentication: Boolean,
        enableRegistration: Boolean,
        OnUsernameChanged: (username: String) -> Unit,
        OnPasswordChanged: (password: String) -> Unit,
        OnPasswordAgainChanged: (passwordAgain: String) -> Unit,
        OnNameChanged: (name: String) -> Unit,
        OnHeadlineChanged: (headline: String?) -> Unit,
        OnCoverImageChanged: (coverImage: String?) -> Unit,
        OnProfileImageChanged: (profileImage: String?) -> Unit,
        onAuthenticate: () -> Unit,
        onToggleMode: () -> Unit
    ) {
        Column(
            modifier = modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AuthenticationTitle(authenticationMode = authenticationMode)

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextFieldDefault(
                    modifier = modifier,
                    value = username,
                    label = stringResource(id = MR.strings.enter_username.resourceId),
                    onValueChange = OnUsernameChanged
                )

                AnimatedVisibility(
                    visible = authenticationMode == AuthenticationMode.SIGN_UP
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextFieldDefault(
                            modifier = modifier,
                            value = name,
                            label = stringResource(id = MR.strings.enter_name.resourceId),
                            onValueChange = OnNameChanged
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextFieldDefault(
                            modifier = modifier,
                            value = headline.orEmpty(),
                            label = stringResource(id = MR.strings.enter_headline.resourceId),
                            onValueChange = OnHeadlineChanged
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedPasswordTextField(
                    value = password, onValueChange = OnPasswordChanged
                )

                AnimatedVisibility(
                    visible = authenticationMode == AuthenticationMode.SIGN_UP
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedPasswordTextField(
                            value = passwordAgain, onValueChange = OnPasswordAgainChanged,
                            label = stringResource(id = MR.strings.password_again.resourceId)
                        )

                        PasswordRequirements(satisfiedRequirements = completedPasswordRequirements)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AuthenticationButton(
                    enableAuthentication = if (authenticationMode ==
                        AuthenticationMode.SIGN_IN
                    ) {
                        enableAuthentication
                    } else {
                        enableRegistration
                    },
                    authenticationMode = authenticationMode,
                    onAuthenticate = onAuthenticate
                )

                ToggleAuthenticationMode(
                    modifier = Modifier.fillMaxWidth(),
                    authenticationMode = authenticationMode,
                    toggleAuthentication = {
                        onToggleMode()
                    }
                )
            }
        }
    }


    @Composable
    fun AuthenticationTitle(
        modifier: Modifier = Modifier,
        authenticationMode: AuthenticationMode
    ) {
        Text(
            text = stringResource(
                if (authenticationMode == AuthenticationMode.SIGN_IN) {
                    MR.strings.login.resourceId
                } else {
                    MR.strings.create_account.resourceId
                }
            ), fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
    }

    @Composable
    fun PasswordRequirements(
        modifier: Modifier = Modifier,
        satisfiedRequirements: List<PasswordRequirements>
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center
        ) {
            PasswordRequirements.values().forEach { requirement ->
                Requirement(
                    message = requirement.label.toString(
                        LocalContext.current
                    ),
                    satisfied = satisfiedRequirements.contains(
                        requirement
                    )
                )
            }
        }
    }

    @Composable
    fun AuthenticationButton(
        modifier: Modifier = Modifier,
        authenticationMode: AuthenticationMode,
        enableAuthentication: Boolean,
        onAuthenticate: () -> Unit
    ) {
        ButtonDefault(
            modifier = modifier,
            onClick = {
                onAuthenticate()
            },
            enabled = enableAuthentication,
            text = stringResource(
                if (authenticationMode ==
                    AuthenticationMode.SIGN_IN
                ) {
                    MR.strings.login.resourceId
                } else {
                    MR.strings.create_account.resourceId
                }
            )
        )
    }


    @Composable
    fun ToggleAuthenticationMode(
        modifier: Modifier = Modifier,
        authenticationMode: AuthenticationMode,
        toggleAuthentication: () -> Unit
    ) {
        OutlinedButtonDefault(
            onClick = {
                toggleAuthentication()
            },
            text = stringResource(
                if (authenticationMode ==
                    AuthenticationMode.SIGN_IN
                ) {
                    MR.strings.create_account.resourceId

                } else {
                    MR.strings.login.resourceId
                }
            )
        )

    }
}