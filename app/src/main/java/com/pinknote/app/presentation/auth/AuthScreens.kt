package com.pinknote.app.presentation.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.pinknote.app.R
import com.pinknote.app.domain.model.AppLanguage
import com.pinknote.app.presentation.common.PinkPrimaryButton
import com.pinknote.app.presentation.localization.AppStrings
import com.pinknote.app.presentation.localization.LocalAppStrings
import com.pinknote.app.presentation.localization.languageLabel
import com.pinknote.app.presentation.settings.SettingsViewModel
import com.pinknote.app.presentation.theme.BlushSurface
import com.pinknote.app.presentation.theme.CreamWhite
import com.pinknote.app.presentation.theme.PastelPink
import com.pinknote.app.presentation.theme.PetalMist

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    onRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val strings = LocalAppStrings.current
    val uiState by viewModel.uiState.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetEmailError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val googleClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .build()
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        var googleErrorMessage: String? = null
        val token = runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
                .idToken
        }.fold(
            onSuccess = { it },
            onFailure = { error ->
                googleErrorMessage = error.toGoogleSignInMessage(strings)
                null
            }
        )

        if (token.isNullOrBlank()) {
            val message = googleErrorMessage ?: if (result.resultCode == Activity.RESULT_OK) {
                    strings.googleMissingIdToken
                } else {
                    strings.googleNoTokenWithResultCode.format(result.resultCode)
                }
            viewModel.showAuthError(message)
        } else {
            viewModel.loginWithGoogle(token)
        }
    }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    AuthFormScaffold(
        authMode = AuthMode.Login,
        title = strings.loginTitle,
        subtitle = strings.loginSubtitle,
        onModeSwitch = onRegister,
        heroAction = {
            LanguageSelector(
                selectedLanguage = settings.language,
                onLanguageSelected = settingsViewModel::setLanguage
            )
        }
    ) {
        PinkTextField(
            value = email,
            onValueChange = {
                email = it
                localError = null
            },
            label = strings.email,
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        PinkTextField(
            value = password,
            onValueChange = {
                password = it
                localError = null
            },
            label = strings.password,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation()
        )
        PinkPrimaryButton(
            onClick = {
                localError = validateEmailPassword(email, password, strings)
                if (localError == null) viewModel.login(email, password)
            },
            enabled = !uiState.isLoading
        ) {
            Text(strings.login, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        GoogleAuthButton(
            text = strings.loginWithGoogle,
            enabled = !uiState.isLoading,
            onClick = {
                googleClient.signOut().addOnCompleteListener {
                    launcher.launch(googleClient.signInIntent)
                }
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                resetEmail = email
                resetEmailError = null
                showResetDialog = true
            }) {
                Text(strings.forgotPassword)
            }
        }
        AuthStatus(
            isLoading = uiState.isLoading,
            message = localError ?: uiState.message,
            isError = localError != null || uiState.isMessageError
        )
    }

    if (showResetDialog) {
        PasswordResetDialog(
            email = resetEmail,
            emailError = resetEmailError,
            isLoading = uiState.isLoading,
            onEmailChange = {
                resetEmail = it
                resetEmailError = null
            },
            onDismiss = { showResetDialog = false },
            onSubmit = {
                resetEmailError = validatePasswordResetEmail(resetEmail, strings)
                if (resetEmailError == null) {
                    email = resetEmail.trim()
                    showResetDialog = false
                    viewModel.resetPassword(resetEmail, strings)
                }
            }
        )
    }
}

@Composable
private fun PasswordResetDialog(
    email: String,
    emailError: String?,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    val strings = LocalAppStrings.current

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = { Text(strings.passwordResetTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    strings.passwordResetBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PinkTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = strings.registeredEmail,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                emailError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = onSubmit, enabled = !isLoading) {
                Text(strings.sendEmail)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun RegisterScreen(
    onAuthenticated: () -> Unit,
    onLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val strings = LocalAppStrings.current
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    AuthFormScaffold(
        authMode = AuthMode.Register,
        title = strings.registerTitle,
        subtitle = strings.registerSubtitle,
        onModeSwitch = onLogin
    ) {
        PinkTextField(
            value = name,
            onValueChange = {
                name = it
                localError = null
            },
            label = strings.name,
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )
        PinkTextField(
            value = email,
            onValueChange = {
                email = it
                localError = null
            },
            label = strings.email,
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        PinkTextField(
            value = password,
            onValueChange = {
                password = it
                localError = null
            },
            label = strings.password,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation()
        )
        PinkPrimaryButton(
            onClick = {
                localError = when {
                    name.isBlank() -> strings.nameRequired
                    else -> validateEmailPassword(email, password, strings)
                }
                if (localError == null) viewModel.register(name, email, password)
            },
            enabled = !uiState.isLoading
        ) {
            Text(strings.register, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        AuthStatus(
            isLoading = uiState.isLoading,
            message = localError ?: uiState.message,
            isError = localError != null || uiState.isMessageError
        )
    }
}

private fun Throwable.toGoogleSignInMessage(strings: AppStrings): String {
    val statusCode = (this as? ApiException)?.statusCode
    return when (statusCode) {
        CommonStatusCodes.DEVELOPER_ERROR ->
            strings.googleConfigError
        CommonStatusCodes.NETWORK_ERROR ->
            strings.googleNetworkError
        GoogleSignInStatusCodes.SIGN_IN_CANCELLED ->
            strings.googleSignInCancelled
        GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS ->
            strings.googleSignInInProgress
        GoogleSignInStatusCodes.SIGN_IN_FAILED ->
            strings.googleSignInFailed
        else ->
            statusCode?.let {
                strings.googleSignInFailedWithCode.format(it, GoogleSignInStatusCodes.getStatusCodeString(it))
            } ?: strings.googleSignInFailed
    }
}

private enum class AuthMode {
    Login,
    Register
}

@Composable
private fun AuthFormScaffold(
    authMode: AuthMode,
    title: String,
    subtitle: String,
    onModeSwitch: () -> Unit,
    heroAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var formVisible by remember(authMode) { mutableStateOf(false) }

    LaunchedEffect(authMode) {
        formVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PetalMist,
                        BlushSurface,
                        MaterialTheme.colorScheme.background,
                        CreamWhite
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PastelPink.copy(alpha = 0.24f),
                            Color.Transparent
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuthHero(title = title, subtitle = subtitle, action = heroAction)

            AnimatedVisibility(
                visible = formVisible,
                enter = fadeIn(animationSpec = tween(420, easing = FastOutSlowInEasing)) +
                    slideInVertically(animationSpec = tween(420, easing = FastOutSlowInEasing)) { it / 8 } +
                    scaleIn(initialScale = 0.98f, animationSpec = tween(420, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.98f)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)),
                    shadowElevation = 18.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(width = 44.dp, height = 4.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        )
                        AuthModeTabs(
                            selectedMode = authMode,
                            onSwitchMode = onModeSwitch
                        )
                        content()
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun AuthModeTabs(
    selectedMode: AuthMode,
    onSwitchMode: () -> Unit
) {
    val strings = LocalAppStrings.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = BlushSurface.copy(alpha = 0.58f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AuthModeTab(
                text = strings.login,
                selected = selectedMode == AuthMode.Login,
                onClick = {
                    if (selectedMode != AuthMode.Login) onSwitchMode()
                },
                modifier = Modifier.weight(1f)
            )
            AuthModeTab(
                text = strings.register,
                selected = selectedMode == AuthMode.Register,
                onClick = {
                    if (selectedMode != AuthMode.Register) onSwitchMode()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AuthModeTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "auth_tab_background"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "auth_tab_content"
    )
    val elevation by animateDpAsState(
        targetValue = if (selected) 5.dp else 0.dp,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "auth_tab_elevation"
    )

    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        shadowElevation = elevation
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.textButtonColors(contentColor = contentColor)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AuthHero(
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val transition = rememberInfiniteTransition(label = "auth_hero_motion")
    val iconScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auth_icon_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CreamWhite, PastelPink.copy(alpha = 0.42f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.app_icon),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
                Column {
                    Text(
                        text = "Pink Note",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.personalCycleJournal,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            action?.invoke()
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AuthFeatureChip(
                text = strings.privacy,
                icon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
            AuthFeatureChip(
                text = strings.easyTracking,
                icon = { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
            AuthFeatureChip(
                text = strings.personal,
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AuthFeatureChip(
    text: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(16.dp),
        color = BlushSurface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.36f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(18.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelector(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    val strings = LocalAppStrings.current
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.48f))
    ) {
        TextButton(
            onClick = { showSheet = true },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = strings.languageLabel(selectedLanguage),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = strings.language,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AppLanguage.entries) { language ->
                        val selected = selectedLanguage == language
                        TextButton(
                            onClick = {
                                onLanguageSelected(language)
                                showSheet = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (selected) BlushSurface else Color.Transparent,
                                contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.languageLabel(language),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Spacer(Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun GoogleAuthButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(CreamWhite),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_google_logo),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PinkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.62f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f),
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun AuthStatus(isLoading: Boolean, message: String?, isError: Boolean) {
    val strings = LocalAppStrings.current
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(animationSpec = tween(180)) +
            slideInVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { it / 3 },
        exit = fadeOut(animationSpec = tween(140)) +
            slideOutVertically(animationSpec = tween(140)) { it / 4 }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = strings.processing,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(animationSpec = tween(180)) +
            slideInVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { it / 3 },
        exit = fadeOut(animationSpec = tween(140))
    ) {
        val messageColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = messageColor.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, messageColor.copy(alpha = 0.2f))
        ) {
            Text(
                text = message.orEmpty(),
                color = messageColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}

private fun validateEmailPassword(
    email: String,
    password: String,
    strings: AppStrings
): String? {
    return when {
        email.isBlank() -> strings.emailRequired
        !email.contains("@") -> strings.invalidEmail
        password.length < 6 -> strings.passwordTooShort
        else -> null
    }
}
