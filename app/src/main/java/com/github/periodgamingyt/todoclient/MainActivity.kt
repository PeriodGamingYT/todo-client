package com.github.periodgamingyt.todoclient

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class ConnectionStatus {
    NO_CONN,
    ATTEMPT_CONN,
    BAD_PASS,
    FAIL_CONN,
    SUCCESS_CONN
}

data class ChecklistItem(
    var name: String = "",
    var checked: Boolean = false,
    var index: Int = 0
)

data class InventoryItem(
    var name: String = "",
    var current: Int = 0,
    var max: Int = 0,
    var index: Int = 0
)

data class ServerHandler(
    var address: String = "",
    var password: String = "",
    var status: ConnectionStatus = ConnectionStatus.NO_CONN,
    var checklist: MutableMap<String, ChecklistItem> = mutableMapOf(),
    var inventory: MutableMap<String, InventoryItem> = mutableMapOf()
)

fun sortChecklist(items: List<ChecklistItem>): List<ChecklistItem> {
    return items.sortedWith(compareBy {
        if (it.checked) 1 else 0
    })
}

fun buildChecklist(serverHandler: ServerHandler): List<ChecklistItem> {
    val result: MutableList<ChecklistItem> = mutableListOf()
    serverHandler.checklist.forEach { (_, value) ->
        result.add(value)
    }

    return result.toList().sortedWith(compareBy {
        it.index
    })
}

fun sortInventory(items: List<InventoryItem>): List<InventoryItem> {
    return items.sortedWith(compareBy {
        it.current
    })
}

fun buildInventory(serverHandler: ServerHandler): List<InventoryItem> {
    val result: MutableList<InventoryItem> = mutableListOf()
    serverHandler.inventory.forEach { (_, value) ->
        result.add(value)
    }

    return result.toList().sortedWith(compareBy {
        it.index
    })
}

@Composable
fun ActualApp(
    modifier: Modifier,
    context: Context,
    serverHandler: ServerHandler
) {
    val maxWidthMod = Modifier
        .fillMaxWidth(fraction = 1f)
        .padding(
            horizontal = 8.dp,
            vertical = 4.dp
        )

    var addressText by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var passwordText by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabIndexChecklist = 0
    val tabIndexInventory = 1
    val titles = listOf("Checklist", "Inventory")
    val checklistScrollState by rememberSaveable(stateSaver = ScrollState.Saver) { mutableStateOf(ScrollState(0)) }
    val inventoryScrollState by rememberSaveable(stateSaver = ScrollState.Saver) { mutableStateOf(ScrollState(0)) }
    val actionsScrollState by rememberSaveable(stateSaver = ScrollState.Saver) { mutableStateOf(ScrollState(0)) }
    var checklist: List<ChecklistItem> by rememberSaveable { mutableStateOf(listOf()) }
    var inventory: List<InventoryItem> by rememberSaveable { mutableStateOf(listOf()) }
    checklist = buildChecklist(serverHandler)
    inventory = buildInventory(serverHandler)
    Column(
        modifier = modifier
    ) {
        TextField(
            value = addressText,
            onValueChange = { newText ->
                addressText = newText
            },

            label = { Text("Server Address/IP", onTextLayout = {}) },
            singleLine = true,
            modifier = maxWidthMod
        )

        TextField(
            value = passwordText,
            onValueChange = { newText ->
                passwordText = newText
            },

            label = { Text("Password", onTextLayout = {}) },
            singleLine = true,
            modifier = maxWidthMod,
            trailingIcon = {
                val image = if(passwordVisible)
                    ImageVector.vectorResource(R.drawable.visibility_off)
                    else ImageVector.vectorResource(R.drawable.visibility)

                val description = if(passwordVisible)
                    "Hide Password"
                    else "Show password"

                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    Icon(imageVector = image, description)
                }
            },

            visualTransformation = if(passwordVisible)
                VisualTransformation.None
                else PasswordVisualTransformation(),

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Button(
            onClick = {
                serverHandler.address = addressText.text
                serverHandler.password = passwordText.text
            },

            modifier = maxWidthMod,
            content = { Text("CONNECT", onTextLayout = {}) }
        )

        Text(
            text = when(serverHandler.status) {
                ConnectionStatus.NO_CONN -> "Not connected to a server, all changes are local"
                ConnectionStatus.ATTEMPT_CONN -> "Attempting connection"
                ConnectionStatus.BAD_PASS -> "Incorrect password, all changes are local"
                ConnectionStatus.FAIL_CONN -> "Failed connection, all changed are local"
                ConnectionStatus.SUCCESS_CONN -> "Connected, click save to sync changes"
            },

            modifier = maxWidthMod,
            color = when(serverHandler.status) {
                ConnectionStatus.NO_CONN -> Color.Red
                ConnectionStatus.ATTEMPT_CONN -> Color.Yellow
                ConnectionStatus.BAD_PASS -> Color.Red
                ConnectionStatus.FAIL_CONN -> Color.Red
                ConnectionStatus.SUCCESS_CONN -> Color.Green
            },

            textAlign = TextAlign.Center,
            onTextLayout = {}
        )

        Row(
            modifier = maxWidthMod.horizontalScroll(actionsScrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {},
                content = { Text("RESET", onTextLayout = {}) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Button(
                onClick = {},
                content = { Text("SAVE", onTextLayout = {}) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Button(
                onClick = {},
                content = { Text("RESTORE", onTextLayout = {}) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Button(
                onClick = {
                    val sortedChecklist = sortChecklist(buildChecklist(serverHandler))
                    val sortedInventory = sortInventory(buildInventory(serverHandler))
                    for(i in sortedChecklist.indices) {
                        serverHandler.checklist[sortedChecklist[i].name] = ChecklistItem(
                            sortedChecklist[i].name,
                            sortedChecklist[i].checked,
                            i
                        )
                    }

                    for(i in sortedInventory.indices) {
                        serverHandler.inventory[sortedInventory[i].name] = InventoryItem(
                            sortedInventory[i].name,
                            sortedInventory[i].current,
                            sortedInventory[i].max,
                            i
                        )
                    }

                    checklist = buildChecklist(serverHandler)
                    inventory = buildInventory(serverHandler)

                    // a trick to get LazyColumn to refresh
                    tabIndex += 2
                },

                content = { Text("SORT", onTextLayout = {}) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        TabRow(selectedTabIndex = tabIndex) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = {
                        Text(
                            text = title,
                            maxLines = 2,
                            onTextLayout = {}
                        )
                    }
                )
            }
        }

        when (tabIndex) {
            tabIndexChecklist ->
                LazyColumn(
                    modifier = Modifier
                        .verticalScroll(checklistScrollState)
                        .height(200.dp)
                ) {
                    items(checklist.size) { index ->
                        var value by remember { mutableStateOf(checklist[index].checked) }
                        val name = checklist[index].name
                        if(serverHandler.checklist[name] == null) {
                            return@items
                        }

                        Row {
                            Checkbox(
                                checked = value,
                                onCheckedChange = { newChecked ->
                                    serverHandler.checklist[name]?.checked = newChecked
                                    value = newChecked
                                }
                            )

                            Text(
                                name,
                                modifier = Modifier.align(Alignment.CenterVertically),
                                onTextLayout = {}
                            )

                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    val dialogBuilder = AlertDialog.Builder(context)
                                    dialogBuilder
                                        .setMessage("Do you want to delete this?")
                                        .setCancelable(false)
                                        .setPositiveButton(
                                            "YES"
                                        ) { _, _ ->
                                            serverHandler.checklist.remove(name)
                                            checklist = buildChecklist(serverHandler)
                                        }

                                        .setNegativeButton(
                                            "NO"
                                        ) { dialog, _ ->
                                            dialog.cancel()
                                        }

                                    val alert = dialogBuilder.create()
                                    alert.setTitle("Are you sure you want to delete this?")
                                    alert.show()
                                },
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(
                                        R.drawable.delete
                                    ),

                                    "Delete this item"
                                )
                            }
                        }
                    }
                }

            tabIndexInventory ->
                LazyColumn(
                    modifier = Modifier
                        .verticalScroll(inventoryScrollState)
                        .height(200.dp)
                ) {
                    items(inventory.size) { index ->
                        var current by remember { mutableIntStateOf(inventory[index].current) }
                        var max by remember { mutableIntStateOf(inventory[index].max) }
                        var currentText by remember { mutableStateOf(TextFieldValue(current.toString())) }
                        var maxText by remember { mutableStateOf(TextFieldValue(max.toString())) }
                        val name = inventory[index].name
                        if(serverHandler.inventory[name] == null) {
                            return@items
                        }

                        Row {
                            TextField(
                                value = currentText,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                ),

                                onValueChange = { newValue ->
                                    currentText = newValue
                                    current = newValue.text.toIntOrNull() ?: 0
                                    serverHandler.inventory[name]?.current = current
                                },

                                modifier = Modifier.width(100.dp)
                            )

                            Text(
                                "/",
                                modifier = Modifier.align(Alignment.CenterVertically),
                                onTextLayout = {}
                            )

                            TextField(
                                value = maxText,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                ),

                                onValueChange = { newValue ->
                                    maxText = newValue
                                    max = newValue.text.toIntOrNull() ?: 0
                                    serverHandler.inventory[name]?.max = max
                                },

                                modifier = Modifier.width(100.dp)
                            )

                            Text(
                                name,
                                modifier = Modifier.align(Alignment.CenterVertically),
                                onTextLayout = {}
                            )

                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    val dialogBuilder = AlertDialog.Builder(context)
                                    dialogBuilder
                                        .setMessage("Do you want to delete this?")
                                        .setCancelable(false)
                                        .setPositiveButton(
                                            "YES"
                                        ) { _, _ ->
                                            serverHandler.inventory.remove(name)
                                            inventory = buildInventory(serverHandler)
                                        }

                                        .setNegativeButton(
                                            "NO"
                                        ) { dialog, _ ->
                                            dialog.cancel()
                                        }

                                    val alert = dialogBuilder.create()
                                    alert.setTitle("Are you sure you want to delete this?")
                                    alert.show()
                                },
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(
                                        R.drawable.delete
                                    ),

                                    "Delete this item"
                                )
                            }
                        }
                    }
                }

            2 -> tabIndex = 0
            3 -> tabIndex = 1
        }
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serverHandler = ServerHandler()
        serverHandler.checklist["test1"] = ChecklistItem("test1", true, 0)
        serverHandler.checklist["test2"] = ChecklistItem("test2", false, 1)
        serverHandler.inventory["test1"] = InventoryItem("test1", 1, 5, 0)
        serverHandler.inventory["test2"] = InventoryItem("test2", 0, 6, 0)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),

                            title = {
                                Text("TODO Client", onTextLayout = {})
                            }
                        )
                    }
                ) { innerPadding ->
                    ActualApp(
                        Modifier.padding(innerPadding),
                        this,
                        serverHandler
                    )
                }
            }
        }
    }
}