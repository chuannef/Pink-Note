package com.pinknote.app.presentation.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.pinknote.app.R
import com.pinknote.app.presentation.common.PinkCard
import com.pinknote.app.presentation.common.PinkPage
import com.pinknote.app.presentation.common.PinkPrimaryButton

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    onRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val googleClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(context.getString(R.string.google_web_client_id))
                .build()
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val token = runCatching {
                GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                    .idToken
            }.getOrNull()
            token?.let(viewModel::loginWithGoogle)
        }
    }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    AuthFormScaffold(
        title = "PinkNote",
        subtitle = "Theo dõi chu kỳ với cảm giác nhẹ nhàng, riêng tư và dễ tin cậy."
    ) {
        PinkTextField(
            value = email,
            onValueChange = {
                email = it
                localError = null
            },
            label = "Email",
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        PinkTextField(
            value = password,
            onValueChange = {
                password = it
                localError = null
            },
            label = "Mật khẩu",
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation()
        )
        PinkPrimaryButton(
            onClick = {
                localError = validateEmailPassword(email, password)
                if (localError == null) viewModel.login(email, password)
            },
            enabled = !uiState.isLoading
        ) {
            Text("Đăng nhập")
        }
        OutlinedButton(
            onClick = {
                googleClient.signOut().addOnCompleteListener {
                    launcher.launch(googleClient.signInIntent)
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Đăng nhập bằng Google")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.resetPassword(email) }) {
                Text("Quên mật khẩu")
            }
            TextButton(onClick = onRegister) {
                Text("Tạo tài khoản")
            }
        }
        AuthStatus(
            isLoading = uiState.isLoading,
            message = localError ?: uiState.message
        )
    }
}

@Composable
fun RegisterScreen(
    onAuthenticated: () -> Unit,
    onLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    AuthFormScaffold(
        title = "Tạo tài khoản",
        subtitle = "Bắt đầu bằng vài thông tin cơ bản, phần còn lại có thể cập nhật sau."
    ) {
        PinkTextField(
            value = name,
            onValueChange = {
                name = it
                localError = null
            },
            label = "Tên",
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )
        PinkTextField(
            value = email,
            onValueChange = {
                email = it
                localError = null
            },
            label = "Email",
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        PinkTextField(
            value = password,
            onValueChange = {
                password = it
                localError = null
            },
            label = "Mật khẩu",
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation()
        )
        PinkPrimaryButton(
            onClick = {
                localError = when {
                    name.isBlank() -> "Hãy nhập tên của bạn."
                    else -> validateEmailPassword(email, password)
                }
                if (localError == null) viewModel.register(name, email, password)
            },
            enabled = !uiState.isLoading
        ) {
            Text("Đăng ký")
        }
        TextButton(onClick = onLogin) {
            Text("Đã có tài khoản")
        }
        AuthStatus(
            isLoading = uiState.isLoading,
            message = localError ?: uiState.message
        )
    }
}

@Composable
private fun AuthFormScaffold(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    PinkPage {
        Spacer(Modifier.height(52.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("PinkNote", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Text(title, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))
        PinkCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        }
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
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.72f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun AuthStatus(isLoading: Boolean, message: String?) {
    if (isLoading) {
        CircularProgressIndicator(Modifier.padding(top = 8.dp))
    }
    message?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun validateEmailPassword(email: String, password: String): String? {
    return when {
        email.isBlank() -> "Hãy nhập email."
        !email.contains("@") -> "Email chưa đúng định dạng."
        password.length < 6 -> "Mật khẩu cần ít nhất 6 ký tự."
        else -> null
    }
}
