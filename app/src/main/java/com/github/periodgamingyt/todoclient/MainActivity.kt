
// my hope is that this code is so bad that i'm never allowed to write
// mobile ui/ui code again. and yet people have the audacity to wonder
// why people like programming in c. ImGUI is my beloved
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.runBlocking

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

data class DatastoreItem(
    var name: String,
    var value: String
)

fun boolToInt(value: Boolean): Int {
    return if(value)
        1
        else 0
}

fun intToBool(value: Int): Boolean {
    return value != 0
}

@Composable
fun AddItemDialog(
	text: String,
	showDialog: Boolean,
	setText: (String) -> Unit,
	setShowDialog: (Boolean) -> Unit,
	confirm: () -> Unit
): Unit? {
	if(!showDialog) {
		return null
	}

	val h6 = TextStyle(
		fontFamily = FontFamily.Default,
		fontWeight = FontWeight.Medium,
		fontSize = 20.sp,
		lineHeight = 24.sp,
		letterSpacing = 0.5.sp
	)

	return Dialog(
		onDismissRequest = {}
	) {
		Surface(shape = MaterialTheme.shapes.medium) {
			Column {
				Column(modifier = Modifier.padding(24.dp)) {
					Text(
						"What name do you want?",
						style = h6
					)

					Spacer(modifier = Modifier.size(16.dp))
					OutlinedTextField(
						value = text,
						onValueChange = { newValue ->
							setText(newValue)
						},

					   label = { Text("Name") }
					)
				}

				Spacer(modifier = Modifier.size(4.dp))
				Row(
					modifier = Modifier
						.padding(8.dp)
						.fillMaxWidth(),

					horizontalArrangement = Arrangement
						.spacedBy(4.dp, Alignment.End)
				) {
					TextButton(
						onClick = {
							setShowDialog(false)
						},

						content = { Text("CANCEL") }
					)

					TextButton(
						onClick = {
                            setShowDialog(false)
                            confirm()
                        },

						content = { Text("ADD") }
					)
				}
			}
		}
	}
}

@Composable
fun <T: Any> ItemColumn(
    scrollState: ScrollState,
    items: List<T>,
    context: Context,
    isItemNull: (String) -> Boolean,
    getName: (Int) -> String,
    removeItem: (String) -> Unit,
    setShowDialog: (Boolean) -> Unit,
    rowContent: @Composable (String, Int) -> Unit
): Unit {
	return LazyColumn(
		modifier = Modifier
		.verticalScroll(scrollState)
		.height(200.dp)
	) {
		items(items.size) { index ->
            val name = getName(index)
			if(isItemNull(name)) {
				return@items
			}

			Row {
				rowContent(name, index)
                Spacer(Modifier.width(8.dp))
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
                                removeItem(name)
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

		item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        setShowDialog(true)
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            R.drawable.add
                        ),

                        "Add an item"
                    )
                }
            }
        }
	}
}

@Composable
fun ActualApp(
    modifier: Modifier,
    context: Context,
    serverHandler: ServerHandler,
    dataStore: DataStore<Preferences>
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
    var checklistAddShowDialog by rememberSaveable { mutableStateOf(false) }
    var inventoryAddShowDialog by rememberSaveable { mutableStateOf(false) }
    var checklistAddText by rememberSaveable { mutableStateOf("") }
    var inventoryAddText by rememberSaveable { mutableStateOf("") }
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
                onClick = {
                    for(i in checklist.indices) {
                        serverHandler.checklist[checklist[i].name] = ChecklistItem(
                            checklist[i].name,
                            false,
                            i
                        )
                    }

                    for(i in inventory.indices) {
                        serverHandler.inventory[inventory[i].name] = InventoryItem(
                            inventory[i].name,
                            0,
                            inventory[i].max,
                            i
                        )
                    }

                    checklist = buildChecklist(serverHandler)
                    inventory = buildInventory(serverHandler)

                    // a trick to get LazyColumn to refresh
                    tabIndex += 2
                },

                content = { Text("RESET", onTextLayout = {}) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Button(
                onClick = {
                    val items: MutableList<DatastoreItem> = mutableListOf()
                    items.add(DatastoreItem("address", serverHandler.address))
                    items.add(DatastoreItem("password", serverHandler.password))
                    items.add(DatastoreItem("checklist-size", serverHandler.checklist.size.toString()))
                    items.add(DatastoreItem("inventory-size", serverHandler.inventory.size.toString()))
                    for(i in checklist.indices) {
                        val item = checklist[i]
                        items.add(DatastoreItem("checklist-$i-name", item.name))
                        items.add(DatastoreItem("checklist-$i-checked", boolToInt(item.checked).toString()))
                    }

                    for(i in inventory.indices) {
                        val item = inventory[i]
                        items.add(DatastoreItem("checklist-$i-name", item.name))
                        items.add(DatastoreItem("checklist-$i-current", item.current.toString()))
                        items.add(DatastoreItem("checklist-$i-max", item.max.toString()))
                    }

                    runBlocking { dataStore.edit { prefs ->
                        prefs.clear()
                        for (i in items.indices) {
                            val key = stringPreferencesKey(items[i].name)
                            prefs[key] = items[i].value
                        }
                    } }
                },

                content = { Text("SAVE", onTextLayout = {}) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Button(
                onClick = {
                    runBlocking { dataStore.edit { prefs ->
                        serverHandler.address = prefs[stringPreferencesKey("address")] ?: ""
                        serverHandler.password = prefs[stringPreferencesKey("password")] ?: ""
                        val checklistSize = (
                                prefs[stringPreferencesKey("checklist-size")] ?: ""
                        ).toIntOrNull() ?: 0

                        val inventorySize = (
                                prefs[stringPreferencesKey("inventory-size")] ?: ""
                        ).toIntOrNull() ?: 0

                        serverHandler.checklist.clear()
                        serverHandler.inventory.clear()
                        for (i in 0..<checklistSize) {
                            val name = prefs[stringPreferencesKey("checklist-$i-name")] ?: ""
                            val checked = intToBool(
                                (
                                    prefs[stringPreferencesKey("checklist-$i-checked")] ?: ""
                                ).toIntOrNull() ?: 0
                            )

                            serverHandler.checklist[name] = ChecklistItem(
                                name,
                                checked,
                                i
                            )
                        }

                        for (i in 0..<inventorySize) {
                            val name = prefs[stringPreferencesKey("inventory-$i-name")] ?: ""
                            val current = (
                                prefs[stringPreferencesKey("inventory-$i-current")] ?: ""
                            ).toIntOrNull() ?: 0

                            val max = (
                                prefs[stringPreferencesKey("inventory-$i-max")] ?: ""
                            ).toIntOrNull() ?: 0

                            serverHandler.inventory[name] = InventoryItem(
                                name,
                                current,
                                max,
                                i
                            )
                        }
                    } }

                    checklist = buildChecklist(serverHandler)
                    inventory = buildInventory(serverHandler)

                    // a trick to get LazyColumn to refresh
                    tabIndex += 2
                },

                content = { Text("LOAD", onTextLayout = {}) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Button(
                onClick = {},
                content = { Text("SEND", onTextLayout = {}) },
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
                ItemColumn(
                    checklistScrollState,
                    checklist,
                    context,
                    { name -> serverHandler.checklist[name] == null },
                    { index -> checklist[index].name },
                    { name ->
                        serverHandler.checklist.remove(name)
                        checklist = buildChecklist(serverHandler)

                        // a trick to get LazyColumn to refresh
                        tabIndex += 2
                    },

                    { value -> checklistAddShowDialog = value },
                    { name, index ->
                        println("item looped in checklist")
                        var value by rememberSaveable { mutableStateOf(checklist[index].checked) }
                        Checkbox(
                            checked = value,
                            onCheckedChange = { newValue ->
                                serverHandler.checklist[name]?.checked = newValue
                                value = newValue
                            }
                        )
                    }
                )

            tabIndexInventory ->
                ItemColumn(
                    inventoryScrollState,
                    inventory,
                    context,
                    { name -> serverHandler.inventory[name] == null },
                    { index -> inventory[index].name },
                    { name ->
                        serverHandler.inventory.remove(name)
                        inventory = buildInventory(serverHandler)

                        // a trick to get LazyColumn to refresh
                        tabIndex += 2
                    },

                    { value -> inventoryAddShowDialog = value },
                    { name, index ->
                        var current by remember { mutableIntStateOf(inventory[index].current) }
                        var max by remember { mutableIntStateOf(inventory[index].max) }
                        var currentText by remember { mutableStateOf(TextFieldValue(current.toString())) }
                        var maxText by remember { mutableStateOf(TextFieldValue(max.toString())) }
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

                            modifier = Modifier.size(50.dp, 50.dp)
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

                            modifier = Modifier.size(50.dp, 50.dp)
                        )
                    }
                )

            // trick to get lazy columns to forcefully refresh
            2 -> tabIndex = 0
            3 -> tabIndex = 1
        }

        AddItemDialog(
            checklistAddText,
            checklistAddShowDialog,
            { value -> checklistAddText = value },
            { value -> checklistAddShowDialog = value },
            {
                serverHandler.checklist[checklistAddText] = ChecklistItem(
                    checklistAddText,
                    false,
                    checklist.size
                )

                checklist = buildChecklist(serverHandler)

                // a trick to get LazyColumn to refresh
                tabIndex += 2
            }
        ) ?: Row {}

        AddItemDialog(
            inventoryAddText,
            inventoryAddShowDialog,
            { value -> inventoryAddText = value },
            { value -> inventoryAddShowDialog = value },
            {
                serverHandler.inventory[inventoryAddText] = InventoryItem(
                    inventoryAddText,
                    0,
                    0,
                    inventory.size
                )

                inventory = buildInventory(serverHandler)

                // a trick to get LazyColumn to refresh
                tabIndex += 2
            }
        ) ?: Row {}
    }
}

class MainActivity : ComponentActivity() {
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serverHandler = ServerHandler()
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
                        serverHandler,
                        dataStore
                    )
                }
            }
        }
    }
}
