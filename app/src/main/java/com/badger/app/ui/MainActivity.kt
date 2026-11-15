@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.badger.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
                    2 -> NagListScreen("Completed", viewModel.completedNags.collectAsState().value, dark)
                    3 -> BurrowScreen(viewModel, dark, forest)
                    4 -> PlaceholderScreen("Settings")
                    else -> PlaceholderScreen("Calendar")
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
    val titleColor = if (dark) BadgerColors.darkTitle else BadgerColors.lightTitle
    val subColor = if (dark) BadgerColors.darkSubtitle else BadgerColors.lightSubtitle

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
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
                    .heightIn(min = 72.dp)
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
                    modifier = Modifier.padding(top = 4.dp)
                )
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Nag") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What needs doing?") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = p.stripe.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onAdd(title, priority) },
                colors = ButtonDefaults.textButtonColors(contentColor = forest)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun BurrowScreen(viewModel: NagViewModel, dark: Boolean, forest: Color) {
    val nags by viewModel.burrowedNags.collectAsState()
    val titleColor = if (dark) BadgerColors.darkTitle else BadgerColors.lightTitle
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Burrow", color = titleColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (nags.isEmpty()) {
            Text("Nothing parked.", color = if (dark) BadgerColors.darkSubtitle else BadgerColors.lightSubtitle)
        } else {
            nags.forEach { nag ->
                TextButton(onClick = { viewModel.unburrowNag(nag) }) {
                    Text("${nag.title}  ·  Bring back", color = forest)
                }
            }
        }
    }
}

@Composable
private fun NagListScreen(heading: String, nags: List<NagItem>, dark: Boolean) {
    val titleColor = if (dark) BadgerColors.darkTitle else BadgerColors.lightTitle
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(heading, color = titleColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (nags.isEmpty()) {
            Text("None yet.", color = if (dark) BadgerColors.darkSubtitle else BadgerColors.lightSubtitle)
        } else {
            nags.forEach { Text(it.title, color = titleColor, modifier = Modifier.padding(vertical = 8.dp)) }
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(name, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
    }
}
