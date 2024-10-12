package net.ghosttrails.www.mydetic.migration

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import net.ghosttrails.www.mydetic.R
import net.ghosttrails.www.mydetic.ui.theme.MyDeticTheme
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

class MigrationActivity : FragmentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            MyDeticTheme {
                Scaffold(
                    // modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = colorResource(R.color.primary_dark),
                                titleContentColor = colorResource(R.color.text_ondark),
                            ),
                            title = {
                                Text("Data Migration")
                            }
                        )
                    },
                ) { innerPadding ->
                    MainContent(
                        // consume insets as scaffold doesn't do it by default
                        modifier = Modifier.consumeWindowInsets(innerPadding),
                        contentPadding = innerPadding
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    migrationViewModel: MigrationViewModel = viewModel()
) {
    LazyColumn(
        modifier = Modifier
            .padding(16.dp, contentPadding.calculateTopPadding()),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        stickyHeader {
            Surface(Modifier.fillParentMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Spacer(modifier = Modifier.padding(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Overwrite Existing ")
                        Checkbox(
                            checked = migrationViewModel.overwriteExisting.value,
                            onCheckedChange = { migrationViewModel.overwriteExisting.value = it })
                    }
                    ApiDropDown(
                        options = migrationViewModel.getApiList(),
                        apiVal = migrationViewModel.fromApi,
                        label = stringResource(R.string.source_api)
                    )
                    ApiDropDown(
                        options = migrationViewModel.getApiList(),
                        apiVal = migrationViewModel.toApi,
                        label = stringResource(R.string.destination_api)
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    ChooseDateTextField(label = "From Date", calendar = migrationViewModel.fromDate)
                    ChooseDateTextField(label = "To Date", calendar = migrationViewModel.toDate)
                    Spacer(modifier = Modifier.padding(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = { migrationViewModel.startMigrationClicked() }) {
                            Text(text = migrationViewModel.buttonText.value)
                        }
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                }
            }
        }
        items(items = migrationViewModel.logEntries) { entry ->
            Row(modifier = Modifier.fillMaxWidth()
            ) {
                Text(entry)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiDropDown(
    options: List<String>,
    apiVal: MutableState<String>,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            // The `menuAnchor` modifier must be passed to the text field to handle
            // expanding/collapsing the menu on click. A read-only text field has
            // the anchor type `PrimaryNotEditable`.
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            value = apiVal.value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        apiVal.value = option
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseDateTextField(label: String, calendar: MutableState<Long>) {
    var selectedDate by rememberSaveable { mutableStateOf(formatMillisDate(calendar.value)) }

    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge
    )
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = calendar.value)
    val showDialog = rememberSaveable { mutableStateOf(false) }
    if (showDialog.value) {
        DatePickerDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    calendar.value =
                        datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    selectedDate = formatMillisDate(calendar.value)
                    showDialog.value = false
                }) {
                    Text("Ok")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed: Boolean by interactionSource.collectIsPressedAsState()

    TextField(
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        value = selectedDate,
        onValueChange = {},
        trailingIcon = { Icons.Default.DateRange },
        interactionSource = interactionSource
    )

    if (isPressed) {
        showDialog.value = true
    }
}

fun formatMillisDate(millis: Long): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(
        Date.from(
            Instant.ofEpochMilli(millis)
        )
    )
}

fun Int.toMonthName(): String {
    return DateFormatSymbols().months[this]
}

fun Date.toFormattedString(): String {
    val simpleDateFormat = SimpleDateFormat("LLLL dd, yyyy", Locale.getDefault())
    return simpleDateFormat.format(this)
}


@Composable
fun MenuSample() {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopStart)
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = { /* Handle edit! */ },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null
                    )
                })
            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = { /* Handle settings! */ },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = null
                    )
                })
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Send Feedback") },
                onClick = { /* Handle send feedback! */ },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Email,
                        contentDescription = null
                    )
                },
                trailingIcon = { Text("F11", textAlign = TextAlign.Center) })
        }
    }
}
