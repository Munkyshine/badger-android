@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.badger.app.ui

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.badger.app.R
import com.badger.app.model.NagItem
import com.badger.app.viewmodel.NagViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Register Google Assistant / Launcher Dynamic App Shortcuts
        try {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            val shortcut = ShortcutInfo.Builder(this, "add_nag")
                .setShortLabel("New Nag")
                .setLongLabel("Create a new Nag")
                .setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.leaf_plus))
                .setIntents(arrayOf(
                    Intent(Intent.ACTION_VIEW, Uri.parse("badger://add"), this, MainActivity::class.java)
                ))
                .build()
            shortcutManager?.setDynamicShortcuts(listOf(shortcut))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setContent { BadgerApp() }
    }
}

@Composable
fun BadgerApp(viewModel: NagViewModel = viewModel()) {
    val dark = isSystemInDarkTheme()
    var tab by remember { mutableIntStateOf(0) }
    val bg = if (dark) BadgerColors.darkBg else BadgerColors.lightBg
    val forest = if (dark) BadgerColors.forestDark else BadgerColors.forestLight

    MaterialTheme {
        Scaffold(
            containerColor = bg,
            bottomBar = {
                NavigationBar(containerColor = if (dark) BadgerColors.darkCard else Color.White) {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0 },
                        icon = { Icon(Icons.Filled.List, "Today") }, label = { Text("Today") })
                    NavigationBarItem(selected = tab == 1, onClick = { tab = 1 },
                        icon = { Icon(Icons.Filled.DateRange, "Calendar") }, label = { Text("Calendar") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2 },
                        icon = { Icon(Icons.Filled.CheckCircle, "Completed") }, label = { Text("Completed") })
                    NavigationBarItem(selected = tab == 3, onClick = { tab = 3 },
                        icon = { Icon(Icons.Filled.Star, "Burrow") }, label = { Text("Burrow") })
                    NavigationBarItem(selected = tab == 4, onClick = { tab = 4 },
                        icon = { Icon(Icons.Filled.Settings, "Settings") }, label = { Text("Settings") })
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize().background(bg)) {
                when (tab) {
                    0 -> TodayScreen(viewModel, dark, forest)
                    1 -> CalendarScreen(viewModel.openNags.collectAsState().value, dark)
                    2 -> NagListScreen("Completed", viewModel.completedNags.collectAsState().value, dark)
                    3 -> BurrowScreen(viewModel, dark, forest)
                    4 -> SettingsScreen(dark, forest)
                }
            }
        }
    }
}

@Composable
private fun TodayScreen(viewModel: NagViewModel, dark: Boolean, forest: Color) {
    val nags by viewModel.openNags.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    val titleColor = if (dark) BadgerColors.darkTitle else BadgerColors.lightTitle
    val cardBg = if (dark) BadgerColors.darkCard else Color.White

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Text(
                "Today",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                color = titleColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            if (nags.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No nags right now.", color = if (dark) BadgerColors.darkSubtitle else BadgerColors.lightSubtitle)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(nags, key = { it.id }) { nag ->
                        NagCard(
                            nag = nag,
                            dark = dark,
                            forest = forest,
                            cardBg = cardBg,
                            onComplete = { viewModel.completeNag(nag) },
                            onBurrow = { viewModel.burrowNag(nag) }
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(64.dp),
            shape = CircleShape,
            containerColor = forest,
            contentColor = Color.White
        ) {
            Icon(
                painter = painterResource(id = R.drawable.leaf_plus),
                contentDescription = "Add",
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp)
            )
        }
        if (showAdd) {
            AddNagDialog(
                forest = forest,
                onDismiss = { showAdd = false },
                onAdd = { title, priority ->
                    viewModel.addNag(title, priority)
                    showAdd = false
                }
            )
        }
    }
}

@Composable
private fun NagCard(
    nag: NagItem,
    dark: Boolean,
    forest: Color,
    cardBg: Color,
    onComplete: () -> Unit,
    onBurrow: () -> Unit
) {
    val priority = Priority.fromRaw(nag.priorityRaw)
    var offsetX by remember { mutableStateOf(0f) }
    var expanded by remember { mutableStateOf(false) }
    var nibbleChecked by remember { mutableStateOf(false) }
    val titleColor = if (dark) BadgerColors.darkTitle else BadgerColors.lightTitle
    val subColor = if (dark) BadgerColors.darkSubtitle else BadgerColors.lightSubtitle

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(nag.id) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount -> offsetX += amount },
                    onDragEnd = {
                        if (offsetX > 150f) onComplete()
                        else if (offsetX < -150f) onBurrow()
                        offsetX = 0f
                    }
                )
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    .background(priority.stripe)
            )
            Column(Modifier.padding(horizontal = 16.dp, vertical = 20.dp).weight(1f)) {
                Text(nag.title, color = titleColor, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(priority.label, color = subColor, fontSize = 13.5.sp)
                Text(
                    "Want to take a nibble?",
                    color = forest,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable { expanded = !expanded }
                )
                if (expanded) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { nibbleChecked = !nibbleChecked }) {
                        Checkbox(checked = nibbleChecked, onCheckedChange = { nibbleChecked = it })
                        Spacer(Modifier.width(8.dp))
                        Text("Step 1: First action item", color = titleColor, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddNagDialog(
    forest: Color,
    onDismiss: () -> Unit,
    onAdd: (String, Priority) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.ToDo) }
    var radius by remember { mutableStateOf(360f) }
    var trigger by remember { mutableStateOf("When I arrive") }
    var saveAsPlace by remember { mutableStateOf(false) }
    var placeName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Nag & Location") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What needs doing?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = p.stripe.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Radius", style = MaterialTheme.typography.labelMedium)
                    Text("${radius.toInt()} m", style = MaterialTheme.typography.labelMedium, color = forest)
                }
                Slider(
                    value = radius,
                    onValueChange = { radius = it },
                    valueRange = 100f..1000f,
                    steps = 18
                )
                Spacer(Modifier.height(8.dp))
                Text("Trigger", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("When I arrive", "When I leave", "Both").forEach { t ->
                        FilterChip(
                            selected = trigger == t,
                            onClick = { trigger = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Save as place", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = saveAsPlace, onCheckedChange = { saveAsPlace = it })
                }
                if (saveAsPlace) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = placeName,
                        onValueChange = { placeName = it },
                        label = { Text("Place Name (e.g. Walmart)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onAdd(title, priority) },
                colors = ButtonDefaults.textButtonColors(contentColor = forest)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CalendarScreen(nags: List<NagItem>, dark: Boolean) {
    val titleColor = if (dark) BadgerColors.darkTitle else BadgerColors.lightTitle
    val subColor = if (dark) BadgerColors.darkSubtitle else BadgerColors.lightSubtitle
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Calendar & Schedule", color = titleColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (nags.isEmpty()) {
            Text("No scheduled nags.", color = subColor)
        } else {
            nags.forEach { nag ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color.Blue)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(nag.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("Scheduled Action", fontSize = 12.sp, color = subColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BurrowScreen(viewModel: NagViewModel, dark: Boolean, forest: Color) {
    val nags by viewModel.burrowedNags.collectAsState()
    val titleColor = if (dark) BadgerColors.darkTitle else BadgerColors.lightTitle
    val subColor = if (dark) BadgerColors.darkSubtitle else BadgerColors.lightSubtitle
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("The Burrow", color = titleColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (nags.isEmpty()) {
            Text("Nothing in the burrow.", color = subColor)
        } else {
            nags.forEach { nag ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(nag.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("Archived", fontSize = 12.sp, color = subColor)
                        }
                        TextButton(onClick = { viewModel.unburrowNag(nag) }) {
                            Text("Bring Back", color = forest)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NagListScreen(heading: String, nags: List<NagItem>, dark: Boolean) {
    val titleColor = if (dark) BadgerColors.darkTitle else BadgerColors.lightTitle
    val subColor = if (dark) BadgerColors.darkSubtitle else BadgerColors.lightSubtitle
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(heading, color = titleColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (nags.isEmpty()) {
            Text("None yet.", color = subColor)
        } else {
            nags.forEach { nag ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.Green)
                        Spacer(Modifier.width(12.dp))
                        Text(nag.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(dark: Boolean, forest: Color) {
    val context = LocalContext.current
    var darkMode by remember { mutableStateOf(dark) }
    var notifications by remember { mutableStateOf(true) }
    var selectedTone by remember { mutableStateOf("Arp L") }
    var subScreen by remember { mutableStateOf("main") }

    val tones = listOf(
        "Arp L" to R.raw.arp_l,
        "Arp S" to R.raw.arp_s,
        "Doots" to R.raw.doots,
        "Tone 1" to R.raw.notification_tone1,
        "Tone 3" to R.raw.notification_tone3,
        "Celestial L" to R.raw.celestial_l,
        "Dopplar Extended" to R.raw.dopplar_extended,
        "Aliens Most Likely" to R.raw.aliens_most_likely
    )

    when (subScreen) {
        "places" -> {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Manage Saved Places", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Home", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Radius: 360m · Geofence Active", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { subScreen = "main" }) { Text("Back to Settings") }
            }
        }
        "family" -> {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Family Badger (v2.0)", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Primary Family Group", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Members: Joshua, Family", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { subScreen = "main" }) { Text("Back to Settings") }
            }
        }
        else -> {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Dark Mode", fontSize = 16.sp)
                            Switch(checked = darkMode, onCheckedChange = { darkMode = it })
                        }
                        Divider()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Notifications", fontSize = 16.sp)
                            Switch(checked = notifications, onCheckedChange = { notifications = it })
                        }
                        Divider()
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Notification Tone (Tap to preview)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            tones.forEach { (name, resId) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedTone = name
                                            try {
                                                MediaPlayer.create(context, resId)?.apply {
                                                    setOnCompletionListener { release() }
                                                    start()
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, fontSize = 14.sp)
                                    if (selectedTone == name) {
                                        Text("Selected", fontSize = 12.sp, color = forest, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Divider()
                        TextButton(onClick = { subScreen = "places" }) {
                            Text("Manage Saved Places", color = forest, fontSize = 16.sp)
                        }
                        Divider()
                        TextButton(onClick = { subScreen = "family" }) {
                            Text("Manage Family Group (v2.0)", color = forest, fontSize = 16.sp)
                        }
                        Divider()
                        TextButton(onClick = {
                            val exportIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Badger Export Data")
                                putExtra(Intent.EXTRA_TEXT, "{\"app\": \"Badger\", \"version\": \"1.0.4\", \"data\": \"Exported Nags\"}")
                            }
                            context.startActivity(Intent.createChooser(exportIntent, "Export Local Data (JSON)"))
                        }) {
                            Text("Export Local Data (JSON)", color = forest, fontSize = 16.sp)
                        }
                        Divider()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("App Version", fontSize = 16.sp)
                            Text("1.0.4", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
