package com.example.arigo.presentation.auth.profile_setup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.arigo.core.theme.BlackText
import com.example.arigo.core.theme.GrayText
import com.example.arigo.core.theme.GreenSurface
import com.example.arigo.core.theme.LightGray
import com.example.arigo.core.theme.TealHeader
import com.example.arigo.core.theme.White
import com.example.arigo.presentation.components.AuthFieldTextStyle
import com.example.arigo.presentation.components.AuthHeader
import com.example.arigo.presentation.components.AuthPillButton
import com.example.arigo.presentation.components.authTextFieldColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AvatarBackground = Color(0xFFB3E5FC)

private data class Country(
    val flag: String,
    val name: String,
    val code: String
)

private val Countries = listOf(
    Country("🇮🇳", "India", "+91"),
    Country("🇺🇸", "United States", "+1"),
    Country("🇬🇧", "United Kingdom", "+44"),
    Country("🇦🇪", "UAE", "+971"),
    Country("🇸🇬", "Singapore", "+65"),
    Country("🇦🇺", "Australia", "+61"),
    Country("🇨🇦", "Canada", "+1"),
    Country("🇩🇪", "Germany", "+49"),
    Country("🇫🇷", "France", "+33"),
    Country("🇯🇵", "Japan", "+81")
)

@Composable
fun ProfileSetupScreen(
    onProfileComplete: () -> Unit,
    viewModel: ProfileSetupViewModel = viewModel(factory = ProfileSetupViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onProfileImageSelected(it.toString()) }
    }

    LaunchedEffect(state.isSaveSuccess) {
        if (state.isSaveSuccess) onProfileComplete()
    }

    LaunchedEffect(state.saveError) {
        state.saveError?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearSaveError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AuthHeader(title = "PROFILE")

            Spacer(modifier = Modifier.height(24.dp))

            ProfileAvatar(
                imageUri = state.profileImageUri,
                onClick = { pickImageLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp)) {
                ProfileField(
                    label = "Profile Name",
                    value = state.name,
                    onValueChange = viewModel::onNameChange
                )

                Spacer(modifier = Modifier.height(12.dp))

                PhoneField(
                    label = "Phone Number",
                    countryCode = state.selectedCountryCode,
                    onCountryCodeChange = viewModel::onCountryCodeChange,
                    phone = state.phone,
                    onPhoneChange = viewModel::onPhoneChange
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    DateOfBirthField(
                        value = state.dateOfBirth,
                        onValueChange = viewModel::onDobChange,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    GenderDropdown(
                        value = state.gender,
                        onValueChange = viewModel::onGenderChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ProfileField(
                    label = "Address",
                    value = state.address,
                    onValueChange = viewModel::onAddressChange
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    ProfileField(
                        label = "City",
                        value = state.city,
                        onValueChange = viewModel::onCityChange,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    ProfileField(
                        label = "PIN Number",
                        value = state.pinCode,
                        onValueChange = viewModel::onPinCodeChange,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PhoneField(
                    label = "Emergency Phone Number",
                    countryCode = state.selectedEmergencyCountryCode,
                    onCountryCodeChange = viewModel::onEmergencyCountryCodeChange,
                    phone = state.emergencyPhone,
                    onPhoneChange = viewModel::onEmergencyPhoneChange
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileField(
                    label = "Health Issues(if any)",
                    value = state.healthIssues,
                    onValueChange = viewModel::onHealthIssuesChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthPillButton(
                    text = "Continue",
                    isLoading = state.isLoading,
                    onClick = viewModel::saveProfile,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(140.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TealHeader)
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    imageUri: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(AvatarBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri.isNotBlank()) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profile picture",
                tint = TealHeader,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = TealHeader,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        FieldLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp),
            singleLine = true,
            colors = authTextFieldColors(),
            textStyle = AuthFieldTextStyle,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Composable
private fun PhoneField(
    label: String,
    countryCode: String,
    onCountryCodeChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit
) {
    var showCountrySheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        FieldLabel(label)
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp),
            singleLine = true,
            colors = authTextFieldColors(),
            textStyle = AuthFieldTextStyle,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            prefix = {
                Row(
                    modifier = Modifier
                        .clickable { showCountrySheet = true }
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = countryCode,
                        color = TealHeader,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = TealHeader,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(LightGray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        )
    }

    if (showCountrySheet) {
        CountryCodeBottomSheet(
            selectedCode = countryCode,
            onSelect = onCountryCodeChange,
            onDismiss = { showCountrySheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        FieldLabel("Date of Birth")
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                singleLine = true,
                colors = authTextFieldColors(),
                textStyle = AuthFieldTextStyle
            )
            // Transparent overlay so taps open the picker instead of focusing the field
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showPicker = true }
            )
        }
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onValueChange(formatDateMillis(millis))
                        }
                        showPicker = false
                    }
                ) {
                    Text("OK", color = TealHeader)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel", color = TealHeader)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Male", "Female", "Other")

    Column(modifier = modifier) {
        FieldLabel("Gender")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                singleLine = true,
                colors = authTextFieldColors(),
                textStyle = AuthFieldTextStyle,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun formatDateMillis(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryCodeBottomSheet(
    selectedCode: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = White
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Select Country Code",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TealHeader,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            Countries.forEach { country ->
                CountryRow(
                    country = country,
                    isSelected = country.code == selectedCode,
                    onClick = {
                        onSelect(country.code)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onDismiss()
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CountryRow(
    country: Country,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) GreenSurface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Text(
            text = country.flag,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = country.name,
            fontSize = 14.sp,
            color = BlackText,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = country.code,
            fontSize = 14.sp,
            color = GrayText
        )
    }
}
