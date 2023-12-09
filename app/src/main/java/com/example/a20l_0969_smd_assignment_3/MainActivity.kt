package com.example.a20l_0969_smd_assignment_3

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a20l_0969_smd_assignment_3.ui.theme._20L_0969_SMD_ASSIGNMENT_3Theme


class MainActivity : ComponentActivity() {
    private val READ_CONTACTS_PERMISSION_REQUEST = 1
    data class Contact(val displayName: String, val phoneNumber: String)

    // Initialize the DatabaseHelper as a property of MainActivity
    private val dbHelper by lazy { DatabaseHelper(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            _20L_0969_SMD_ASSIGNMENT_3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContactList(dbHelper) // Pass dbHelper to ContactList
                }
            }
        }
    }

    private fun requestContactsPermissionAndImport(onContactsImported: (List<Contact>) -> Unit) {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request it
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                READ_CONTACTS_PERMISSION_REQUEST
            )
        } else {
            // Permission is already granted, proceed to import contacts
            getContacts(onContactsImported)
        }
    }



    @SuppressLint("Range")
    private fun getContacts(onContactsImported: (List<Contact>) -> Unit) {
        val context = this
        val contacts = mutableListOf<Contact>()

        // Create an instance of the DatabaseHelper
        val dbHelper = DatabaseHelper(context)
        val db = dbHelper.writableDatabase

        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val displayName =
                    it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val contactId = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                val phoneNumber = getPhoneNumber(contentResolver, contactId)

                // Insert the contact into the database
                dbHelper.insertContact(displayName, phoneNumber)

                contacts.add(Contact(displayName, phoneNumber))
            }
        }

        // Invoke the lambda with the imported contacts
        onContactsImported(contacts)

        // Close the database
        db.close()
    }


    @SuppressLint("Range")
    private fun getPhoneNumber(contentResolver: android.content.ContentResolver, contactId: String): String {
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )

        return if (phoneCursor != null && phoneCursor.moveToFirst()) {
            val phoneNumber =
                phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            phoneCursor.close()
            phoneNumber
        } else {
            phoneCursor?.close()
            ""
        }
    }


    @Composable
    fun ContactList(dbHelper: DatabaseHelper) {
        val context = LocalContext.current

        var contactsState by remember { mutableStateOf(emptyList<Contact>()) }
        var isImported by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Contact List",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Display the import button only if contacts are not imported
            if (!isImported) {
                Button(
                    onClick = {
                        // Request contacts permission and import contacts
                        requestContactsPermissionAndImport() { contacts ->
                            contactsState = contacts
                            isImported = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(text = "Import Contacts")
                }
            }

            // Display the contacts if imported
            if (isImported) {
                ContactListView(
                    contacts = contactsState,
                    onMessageClicked = { contact ->
                        composeMessage(context, contact.phoneNumber)
                    },
                    onCallClicked = { contact ->
                        composeCall(context, contact.phoneNumber)
                    },
                    onDeleteClicked = { contact ->
                        dbHelper.deleteContact(contact.phoneNumber)
                        contactsState = contactsState.filterNot { it == contact }
                    }
                )
            }
        }
    }



    // Function to launch the messaging app with the selected contact as the recipient
    private fun composeMessage(context: Context, phoneNumber: String) {
        val uri = Uri.parse("smsto:$phoneNumber")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", "Hello ${phoneNumber},")
        context.startActivity(intent)
    }


    // Function to initiate a call with the provided phone number
    private fun composeCall(context: Context, phoneNumber: String) {
        val uri = Uri.parse("tel:$phoneNumber")
        val intent = Intent(Intent.ACTION_DIAL, uri)
        context.startActivity(intent)
    }

    @Composable
    fun ContactListView(
        contacts: List<Contact>,
        onMessageClicked: (Contact) -> Unit,
        onCallClicked: (Contact) -> Unit,
        onDeleteClicked: (Contact) -> Unit
    ) {
        LazyColumn {
            items(contacts) { contact ->
                ContactItem(
                    contact = contact,
                    onMessageClicked = onMessageClicked,
                    onCallClicked = onCallClicked,
                    onDeleteClicked = { onDeleteClicked(contact) } // Pass the contact to onDeleteClicked
                )
            }
        }
    }

    @Composable
    fun ContactItem(
        contact: Contact,
        onMessageClicked: (Contact) -> Unit,
        onCallClicked: (Contact) -> Unit,
        onDeleteClicked: (Contact) -> Unit
    ) {
        var isMessageClicked by remember { mutableStateOf(false) }
        var isCallClicked by remember { mutableStateOf(false) }
        var isDeleteClicked by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Handle click on contact item if needed */ }
                .padding(8.dp)
        ) {
            Text(
                text = "${contact.displayName} - ${contact.phoneNumber}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        // Handle message button click
                        isMessageClicked = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                ) {
                    Text(text = "Message")
                }

                Button(
                    onClick = {
                        // Handle call button click
                        isCallClicked = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(text = "Call")
                }

                Button(
                    onClick = {
                        isDeleteClicked = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                ) {
                    Text(text = "Delete")
                }

            }

            // Handle actions based on button clicks
            if (isMessageClicked) {
                // Add logic for handling message action
                // For example, you can launch the messaging app with the contact's phone number
                onMessageClicked(contact)
                isMessageClicked = false
            } else if (isCallClicked) {
                // Add logic for handling call action
                // For example, you can initiate a call using the contact's phone number
                onCallClicked(contact)
                isCallClicked = false
            } else if (isDeleteClicked) {
                // Add logic for handling delete action
                // For example, you can remove the contact from the list
                onDeleteClicked(contact)
                isDeleteClicked = false
            }
        }
    }

}