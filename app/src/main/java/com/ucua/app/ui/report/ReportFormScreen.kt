package com.ucua.app.ui.report

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ucua.app.core.model.ReportPhoto
import com.ucua.app.core.model.User
import com.ucua.app.ui.components.BannerTone
import com.ucua.app.ui.components.DropdownField
import com.ucua.app.ui.components.InlineBanner
import com.ucua.app.ui.components.PrimaryButton
import com.ucua.app.ui.theme.UcuaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    viewModel: ReportFormViewModel,
    user: User?,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onViewReport: (Long) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5),
    ) { uris ->
        viewModel.addPhotos(context.contentResolver, uris)
    }

    val result = state.result
    if (result != null) {
        FormResultPanel(
            result = result,
            onDone = onDone,
            onViewReport = onViewReport,
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditing) "Edit report" else if (state.isUnsafeCondition) {
                            "Report unsafe condition"
                        } else {
                            "Report unsafe action"
                        },
                    )
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp),
        ) {
            InlineBanner(
                text = if (state.isUnsafeCondition) {
                    "Use this form to report a hazardous physical condition — for example a spill, missing guard rail or faulty equipment."
                } else {
                    "Use this form to report an unsafe act — for example working without PPE, unsafe lifting or ignoring procedures."
                },
                tone = BannerTone.Info,
                modifier = Modifier.padding(bottom = 14.dp),
            )

            if (state.lookupsError != null) {
                InlineBanner(
                    text = state.lookupsError!!,
                    tone = BannerTone.Warning,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            if (state.submitError != null) {
                InlineBanner(
                    text = state.submitError!!,
                    tone = BannerTone.Error,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hazard details",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(12.dp))

                    DropdownField(
                        label = "Location *",
                        selectedLabel = state.lookups.locations
                            .firstOrNull { it.LocationId == state.locationId }?.LocationName
                            .orEmpty(),
                        options = state.lookups.locations,
                        optionLabel = { it.LocationName },
                        onSelect = { viewModel.setField("locationId", it.LocationId) },
                        placeholder = if (state.loadingLookups) "Loading locations…" else "Select location",
                        enabled = !state.loadingLookups,
                        isError = state.fieldErrors["locationId"] != null,
                        supportingText = state.fieldErrors["locationId"],
                    )

                    Spacer(Modifier.height(10.dp))

                    if (state.isUnsafeCondition) {
                        OutlinedTextField(
                            value = state.conditionDetails,
                            onValueChange = { viewModel.setField("conditionDetails", it) },
                            label = { Text("Unsafe condition details *") },
                            placeholder = {
                                Text("Describe what you observed and why it is unsafe")
                            },
                            minLines = 5,
                            isError = state.fieldErrors["conditionDetails"] != null,
                            supportingText = {
                                Text(
                                    state.fieldErrors["conditionDetails"]
                                        ?: "${state.conditionDetails.length}/500 characters",
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                        )
                    } else {
                        DropdownField(
                            label = "Offence code *",
                            selectedLabel = state.lookups.offences
                                .firstOrNull { it.OffenceCode == state.offenceCode }
                                ?.let { "${it.OffenceCode} · ${it.Offence.orEmpty()}" }
                                .orEmpty(),
                            options = state.lookups.offences,
                            optionLabel = { "${it.OffenceCode} · ${it.Offence.orEmpty()}" },
                            onSelect = { viewModel.setField("offenceCode", it.OffenceCode) },
                            placeholder = if (state.loadingLookups) "Loading offences…" else "Select offence",
                            enabled = !state.loadingLookups,
                            isError = state.fieldErrors["offenceCode"] != null,
                            supportingText = state.fieldErrors["offenceCode"],
                        )

                        Spacer(Modifier.height(10.dp))

                        DropdownField(
                            label = "Violator company *",
                            selectedLabel = state.lookups.companies
                                .firstOrNull { it.companyName == state.violatorCompany }
                                ?.companyName
                                .orEmpty(),
                            options = state.lookups.companies,
                            optionLabel = { it.companyName },
                            onSelect = { viewModel.setField("violatorCompany", it.companyName) },
                            placeholder = if (state.loadingLookups) "Loading companies…" else "Select company",
                            enabled = !state.loadingLookups,
                            isError = state.fieldErrors["violatorCompany"] != null,
                            supportingText = state.fieldErrors["violatorCompany"],
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.violatorName,
                            onValueChange = { viewModel.setField("violatorName", it) },
                            label = { Text("Violator name (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.violatorDept,
                            onValueChange = { viewModel.setField("violatorDept", it) },
                            label = { Text("Violator department (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.actionTaken,
                            onValueChange = { viewModel.setField("actionTaken", it) },
                            label = { Text("Immediate action taken (optional)") },
                            placeholder = { Text("What did you do to make the situation safe?") },
                            minLines = 3,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Photo evidence",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${state.photoCount}/5",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "Photos help PSSD verify the hazard. JPG, PNG or WEBP up to 8 MB.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        itemsIndexed(state.existingPhotos) { _, photo ->
                            ExistingPhotoThumb(photo)
                        }
                        itemsIndexed(state.photos) { index, photo ->
                            PendingPhotoThumb(
                                photo = photo,
                                onRemove = { viewModel.removePhoto(index) },
                            )
                        }
                        if (state.photoCount < 5) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(92.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            photoPicker.launch(
                                                PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                                ),
                                            )
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = "Add photo",
                                            tint = UcuaPrimary,
                                        )
                                        Text(
                                            text = "Add photo",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = UcuaPrimary,
                                            modifier = Modifier.padding(top = 4.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(
                            text = "Observer details",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = listOfNotNull(
                                user?.name?.takeIf { it.isNotBlank() },
                                user?.staffId,
                                user?.department?.takeIf { it.isNotBlank() },
                            ).joinToString(" · ").ifBlank { "Signed-in user" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = if (state.isEditing && state.editingStatus == "Draft") {
                    "Submit report"
                } else {
                    "Submit report"
                },
                onClick = viewModel::submit,
                loading = state.submitting,
                icon = Icons.Default.Send,
            )

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = viewModel::saveDraft,
                enabled = !state.submitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = "Save as draft",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PendingPhotoThumb(photo: PendingPhoto, onRemove: () -> Unit) {
    val bitmap = remember(photo) {
        BitmapFactory.decodeByteArray(photo.bytes, 0, photo.bytes.size)?.asImageBitmap()
    }
    Box(modifier = Modifier.size(92.dp)) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = photo.fileName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text("Preview", style = MaterialTheme.typography.labelSmall)
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .background(MaterialTheme.colorScheme.error, RoundedCornerShape(11.dp))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove photo",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun ExistingPhotoThumb(photo: ReportPhoto) {
    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = photo.url,
            contentDescription = photo.filename,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun FormResultPanel(
    result: FormResult,
    onDone: () -> Unit,
    onViewReport: (Long) -> Unit,
) {
    val submitted = result as? FormResult.Submitted
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(64.dp),
        )
        Text(
            text = if (submitted != null) "Report submitted" else "Draft saved",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 18.dp),
        )
        Text(
            text = when (result) {
                is FormResult.Submitted -> "Reference ${result.reference}"
                is FormResult.DraftSaved -> "Reference UCUA-%05d".format(result.id)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = if (submitted != null) {
                "Your report is now with the PSSD HSE Officer for initial review. You can track its progress under My reports."
            } else {
                "You can finish and submit this report later from My reports."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp),
        )

        if (submitted != null && submitted.photoFailures > 0) {
            InlineBanner(
                text = "${submitted.photoFailures} photo(s) failed to upload. Open the report to retry.",
                tone = BannerTone.Warning,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        Spacer(Modifier.height(26.dp))

        if (submitted != null) {
            PrimaryButton(
                text = "View report",
                onClick = { onViewReport(submitted.id) },
            )
            Spacer(Modifier.height(10.dp))
        }

        OutlinedButton(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text("Back to dashboard")
        }
    }
}
