package com.anidesk.tv.feature.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.anidesk.tv.core.designsystem.dimensions.TvCardSpacing
import com.anidesk.tv.core.designsystem.dimensions.TvScreenPadding
import com.anidesk.tv.core.designsystem.focus.TvRetryButton
import com.anidesk.tv.core.designsystem.locals.LocalPreferredContentFocusRequester
import com.anidesk.tv.core.designsystem.tv.TvChip
import com.anidesk.tv.core.model.settings.PosterCardSize
import com.anidesk.tv.core.model.settings.PosterQuality
import com.anidesk.tv.core.preferences.settings.SettingsStore
import com.anidesk.tvfeature.settings.uitv.R
import kotlinx.coroutines.flow.Flow

@Composable
fun SettingsTvScreen(
    state: SettingsState.State,
    effect: Flow<SettingsState.Effect>,
    onEvent: (SettingsState.Event) -> Unit,
) {
    val registerPreferredContentFocusRequester = LocalPreferredContentFocusRequester.current
    val firstActionFocusRequester = remember { FocusRequester() }
    DisposableEffect(registerPreferredContentFocusRequester, firstActionFocusRequester) {
        registerPreferredContentFocusRequester?.invoke(firstActionFocusRequester)
        onDispose {
            registerPreferredContentFocusRequester?.invoke(null)
        }
    }

    var showLoginDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = TvScreenPadding.Horizontal,
                vertical = TvScreenPadding.Vertical,
            ),
        verticalArrangement = Arrangement.spacedBy(TvCardSpacing.Vertical),
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_poster_quality)) {
                PosterQuality.entries.forEach { quality ->
                    TvChip(
                        label = qualityLabel(quality),
                        selected = state.posterQuality == quality,
                        onClick = { onEvent(SettingsState.Event.PosterQualitySelected(quality)) },
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_poster_size)) {
                PosterCardSize.entries.forEach { size ->
                    TvChip(
                        label = sizeLabel(size),
                        selected = state.posterCardSize == size,
                        onClick = { onEvent(SettingsState.Event.PosterCardSizeSelected(size)) },
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_api)) {
                SettingsStore.API_ENDPOINTS.forEach { (host, label) ->
                    TvChip(
                        label = label,
                        selected = state.apiEndpoint == host,
                        onClick = { onEvent(SettingsState.Event.ApiEndpointSelected(host)) },
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_account)) {
                AccountStatus(
                    state = state,
                    onSignIn = { showLoginDialog = true },
                    onSkip = { onEvent(SettingsState.Event.AuthSkipped) },
                    onSignOut = { onEvent(SettingsState.Event.LogoutRequested) },
                )
            }
        }
    }

    if (showLoginDialog) {
        LoginDialog(
            sessionError = state.sessionError,
            sessionLoading = state.sessionLoading,
            onDismiss = { showLoginDialog = false },
            onLogin = { login, password ->
                showLoginDialog = false
                onEvent(SettingsState.Event.LoginRequested(login, password))
            },
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    chips: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            chips()
        }
    }
}

@Composable
private fun AccountStatus(
    state: SettingsState.State,
    onSignIn: () -> Unit,
    onSkip: () -> Unit,
    onSignOut: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val statusText = when {
            state.isAuthenticated -> stringResource(
                R.string.settings_logged_in,
                state.profileLogin.ifBlank { "…" },
            )

            state.authSkipped -> stringResource(R.string.settings_auth_skipped_label)
            else -> stringResource(R.string.settings_sign_in_prompt)
        }
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            when {
                state.isAuthenticated -> TvRetryButton(
                    text = stringResource(R.string.settings_sign_out),
                    onClick = onSignOut,
                )

                state.authSkipped -> TvRetryButton(
                    text = stringResource(R.string.settings_sign_in),
                    onClick = onSignIn,
                )

                else -> {
                    TvRetryButton(
                        text = stringResource(R.string.settings_sign_in),
                        onClick = onSignIn,
                    )
                    TvRetryButton(
                        text = stringResource(R.string.settings_skip_auth),
                        onClick = onSkip,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginDialog(
    sessionError: String?,
    sessionLoading: Boolean,
    onDismiss: () -> Unit,
    onLogin: (login: String, password: String) -> Unit,
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_login_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = login,
                    onValueChange = { login = it },
                    label = { Text(stringResource(R.string.settings_login_label)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.settings_password_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                sessionError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !sessionLoading,
                onClick = { onLogin(login, password) },
            ) {
                Text(stringResource(R.string.settings_sign_in))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    )
}

@Composable
private fun qualityLabel(quality: PosterQuality): String = stringResource(
    when (quality) {
        PosterQuality.LOW -> R.string.settings_poster_quality_low
        PosterQuality.STANDARD -> R.string.settings_poster_quality_standard
        PosterQuality.MEGA -> R.string.settings_poster_quality_mega
        PosterQuality.HIGH -> R.string.settings_poster_quality_high
    },
)

@Composable
private fun sizeLabel(size: PosterCardSize): String = stringResource(
    when (size) {
        PosterCardSize.COMPACT -> R.string.settings_poster_size_compact
        PosterCardSize.STANDARD -> R.string.settings_poster_size_standard
        PosterCardSize.LARGE -> R.string.settings_poster_size_large
    },
)