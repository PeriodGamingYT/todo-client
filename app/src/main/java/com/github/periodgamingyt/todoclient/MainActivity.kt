package com.github.periodgamingyt.todoclient

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.saveable.mapSaver
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
    SUCCESS_CONN;

    companion object {
        fun toInt(status: ConnectionStatus): Int {
            return when(status) {
                NO_CONN -> 0
                ATTEMPT_CONN -> 1
                BAD_PASS -> 2
                FAIL_CONN -> 3
                SUCCESS_CONN -> 4
            }
        }

        fun fromInt(int: Int): ConnectionStatus {
            return when(int) {
                0 -> NO_CONN
                1 -> ATTEMPT_CONN
                2 -> BAD_PASS
                3 -> FAIL_CONN
                4 -> SUCCESS_CONN
                else -> {
                    throw Error("We have REALLY bad data")
                }
            }
        }
    }
}

data class InventoryItem(
    var current: Int = 0,
    var max: Int = 0
)

data class ServerHandler(
    var address: String = "",
    var password: String = "",
    var status: ConnectionStatus = ConnectionStatus.NO_CONN,

    // TODO(ElkElan): convert these from hashmaps to lists
    var checklist: MutableMap<String, Boolean> = mutableMapOf(),
    var inventory: MutableMap<String, InventoryItem> = mutableMapOf()
)

val ServerHandlerSaver = run {
    val addressKey = "address"
    val passwordKey = "password"
    val statusKey = "status"
    val checklistSizeKey = "checklist-size"
    val inventorySizeKey = "inventory-size"
    val checklistKey = "checklist"
    val inventoryKey = "inventory"
    mapSaver(
        save = { serverHandler ->
            val map = mutableMapOf(
                addressKey to serverHandler.address,
                passwordKey to serverHandler.password,
                statusKey to ConnectionStatus.toInt(serverHandler.status),
                checklistSizeKey to serverHandler.checklist.size,
                inventorySizeKey to serverHandler.inventory.size
            )

            var index = 0
            for((key, value) in serverHandler.checklist) {
                map["$checklistKey-$index-key"] = key
                map["$checklistKey-$index-value"] = value
                index++
            }

            index = 0
            for((key, value) in serverHandler.inventory) {
                map["$inventoryKey-$index-key"] = key
                map["$inventoryKey-$index-value-current"] = value.current
                map["$inventoryKey-$index-value-max"] = value.max
                index++
            }

            return@mapSaver map
        },

        restore = { map ->
            val checklistSize = map[checklistSizeKey] as Int? ?: 0
            val inventorySize = map[inventorySizeKey] as Int? ?: 0
            val serverHandler = ServerHandler()
            serverHandler.address = map[addressKey] as String? ?: ""
            serverHandler.password = map[passwordKey] as String? ?: ""
            serverHandler.status = ConnectionStatus.fromInt(
                map[statusKey] as Int? ?: 0
            )

            for(i in 0..checklistSize) {
                val key = map["$checklistSizeKey-$i-key"] as String?
                val value = map["$checklistSizeKey-$i-value"] as Boolean?
                key?. let { value?. let {
                    serverHandler.checklist[key] = value
                } }
            }

            for(i in 0..inventorySize) {
                val key = map["$inventorySizeKey-$i-key"] as String?
                val current = map["$inventorySizeKey-$i-value-current"] as Int?
                val max = map["$inventorySizeKey-$i-value-max"] as Int?
                key?. let { current?. let { max?. let {
                    serverHandler.inventory[key] = InventoryItem(
                        current,
                        max
                    )
                } } }
            }

            return@mapSaver serverHandler
        }
    )
}

@Composable
fun ActualApp(modifier: Modifier, context: Context) {
    val maxWidthMod = Modifier
        .fillMaxWidth(fraction = 1f)
        .padding(
            horizontal = 8.dp,
            vertical = 4.dp
        )

    val addressText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue("")
        )
    }

    val passwordText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue("")
        )
    }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabIndexChecklist = 0
    val tabIndexInventory = 1
    val titles = listOf("Checklist", "Inventory")
    val listsScrollState by rememberSaveable(stateSaver = ScrollState.Saver) {
        mutableStateOf(ScrollState(0))
    }

    val actionsScrollState by rememberSaveable(stateSaver = ScrollState.Saver) {
        mutableStateOf(ScrollState(0))
    }

    val serverHandler by rememberSaveable(stateSaver = ServerHandlerSaver) {
        mutableStateOf(ServerHandler())
    }

    serverHandler.checklist["test"] = true
    Column(
        modifier = modifier
    ) {
        TextField(
            value = serverHandler.address,
            onValueChange = { newText ->
                serverHandler.address = newText
            },

            label = { Text("Server Address/IP", onTextLayout = {}) },
            singleLine = true,
            modifier = maxWidthMod
        )

        TextField(
            value = serverHandler.password,
            onValueChange = { newText ->
                serverHandler.password = newText
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
                onClick = {},
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
                        modifier = Modifier.verticalScroll(listsScrollState)
                    ) {
                        items(serverHandler.checklist.size) { index ->
                            var value by rememberSaveable {
                                mutableStateOf(serverHandler.checklist[key])
                            }

                            value?.let {
                                Row {
                                    Checkbox(
                                        checked = value!!,
                                        onCheckedChange = { newChecked ->
                                            serverHandler.checklist[key] = newChecked
                                            value = newChecked
                                        }
                                    )

                                    Text(
                                        key,
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
                                                    "YES",
                                                    DialogInterface.OnClickListener { _, _ ->
                                                        serverHandler.checklist.remove(key)

                                                    })

                                                .setNegativeButton(
                                                    "NO",
                                                    DialogInterface.OnClickListener { dialog, _ ->
                                                        dialog.cancel()
                                                    })

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
                    }

                tabIndexInventory ->
                    serverHandler.inventory.forEach { (key, value) ->

                    }

                else ->
                    Text(
                        "Something went seriously wrong...",
                        color = Color.Red,
                        modifier = maxWidthMod,
                        onTextLayout = {}
                    )
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    ActualApp(Modifier.padding(innerPadding), this)
                }
            }
        }
    }
}