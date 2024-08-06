package com.github.periodgamingyt.todoclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualApp(modifier: Modifier) {
    val maxWidthMod = Modifier
        .fillMaxWidth(fraction = 1f)
        .padding(
            horizontal = 8.dp,
            vertical = 4.dp
        )

    var serverText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue("")
        )
    }

    var passwordText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue("")
        )
    }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // TODO(ElkElan): false for now, will be done once server connection
    // is figured out
    var isConnected = false
    var tabIndex by remember { mutableStateOf(0) }
    var titles = listOf("Checklist", "Inventory")
    Column(
        modifier = modifier
    ) {
        TextField(
            value = serverText,
            onValueChange = {
                serverText = it
            },

            label = { Text("Server Address/IP", onTextLayout = {}) },
            singleLine = true,
            modifier = maxWidthMod
        )

        TextField(
            value = passwordText,
            onValueChange = {
                passwordText = it
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
            onClick = {},
            modifier = maxWidthMod,
            content = { Text("CONNECT", onTextLayout = {}) }
        )

        if(isConnected)
            Text("", onTextLayout = {})
        else Text(
            "Not connected to a server, all changes are local",
            modifier = maxWidthMod,
            color = Color.Red,
            textAlign = TextAlign.Center,
            onTextLayout = {}
        )

        Row(
            modifier = maxWidthMod,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {},
                content = { Text("RESET", onTextLayout = {}) },
                modifier = Modifier.padding( horizontal = 4.dp )
            )

            Button(
                onClick = {},
                content = { Text("SAVE", onTextLayout = {}) },
                modifier = Modifier.padding( horizontal = 4.dp )
            )

            Button(
                onClick = {},
                content = { Text("RESTORE", onTextLayout = {}) },
                modifier = Modifier.padding( horizontal = 4.dp )
            )
        }

        TabRow(
            selectedTabIndex = tabIndex
        ) {
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
                    ActualApp(Modifier.padding(innerPadding))
                }
            }
        }
    }
}