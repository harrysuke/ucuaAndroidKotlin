package com.ucua.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ucua.app.ui.components.BannerTone
import com.ucua.app.ui.components.InlineBanner
import com.ucua.app.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onBack: () -> Unit,
    onGoToLogin: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (state.successMessage != null) {
            SuccessPanel(
                message = state.successMessage!!,
                onGoToLogin = onGoToLogin,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Employee registration",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Register with your Johor Port staff ID. New accounts are reviewed by PSSD before first sign-in.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 18.dp),
            )

            if (state.error != null) {
                InlineBanner(
                    text = state.error!!,
                    tone = BannerTone.Error,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Identity",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(10.dp))

                    AuthField(
                        value = state.staffId,
                        onValueChange = { viewModel.update("staffId", it) },
                        label = "Staff ID *",
                        error = state.fieldErrors["staffId"],
                    )
                    AuthField(
                        value = state.name,
                        onValueChange = { viewModel.update("name", it) },
                        label = "Full name *",
                        error = state.fieldErrors["name"],
                    )
                    AuthField(
                        value = state.email,
                        onValueChange = { viewModel.update("email", it) },
                        label = "Email",
                        error = state.fieldErrors["email"],
                        keyboardType = KeyboardType.Email,
                    )
                    AuthField(
                        value = state.phoneNo,
                        onValueChange = { viewModel.update("phoneNo", it) },
                        label = "Phone number",
                        keyboardType = KeyboardType.Phone,
                    )
                    AuthField(
                        value = state.department,
                        onValueChange = { viewModel.update("department", it) },
                        label = "Department",
                    )
                    AuthField(
                        value = state.designation,
                        onValueChange = { viewModel.update("designation", it) },
                        label = "Designation",
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Security",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.update("password", it) },
                        label = { Text("Password *") },
                        singleLine = true,
                        isError = state.fieldErrors["password"] != null,
                        supportingText = state.fieldErrors["password"]?.let { { Text(it) } },
                        visualTransformation = if (state.passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (state.passwordVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = "Toggle password visibility",
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    )

                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.update("confirmPassword", it) },
                        label = { Text("Confirm password *") },
                        singleLine = true,
                        isError = state.fieldErrors["confirmPassword"] != null,
                        supportingText = state.fieldErrors["confirmPassword"]?.let { { Text(it) } },
                        visualTransformation = if (state.passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = "Submit registration",
                onClick = viewModel::submit,
                loading = state.loading,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Already registered?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onGoToLogin) {
                    Text("Sign in", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun SuccessPanel(
    message: String,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Text(
            text = "Registration received",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp, bottom = 24.dp),
        )
        PrimaryButton(text = "Back to sign in", onClick = onGoToLogin)
    }
}
