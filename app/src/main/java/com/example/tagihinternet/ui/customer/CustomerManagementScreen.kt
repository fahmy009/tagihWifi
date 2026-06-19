package com.example.tagihinternet.ui.customer

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tagihinternet.data.entity.Customer
import com.example.tagihinternet.ui.ViewModelFactory
import com.example.tagihinternet.ui.bill.BillViewModel
import com.example.tagihinternet.ui.components.DeveloperFooter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerManagementScreen(factory: ViewModelFactory) {
    val viewModel: BillViewModel = viewModel(factory = factory)
    val customers by viewModel.allCustomers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Kelola Pelanggan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ PELANGGAN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(customers) { customer ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Lokasi: ${customer.location}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("Pasang: ${customer.installationDate}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Row {
                            IconButton(onClick = { customerToEdit = customer }) {
                                Text("✎", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { viewModel.deleteCustomer(customer) }) {
                                Text("✕", color = Color.Red.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        DeveloperFooter()
    }

    if (showAddDialog) {
        CustomerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, location, date ->
                viewModel.insertCustomer(Customer(name = name, location = location, installationDate = date))
                showAddDialog = false
            }
        )
    }

    if (customerToEdit != null) {
        CustomerDialog(
            customer = customerToEdit,
            onDismiss = { customerToEdit = null },
            onConfirm = { name, location, date ->
                viewModel.updateCustomer(customerToEdit!!.copy(name = name, location = location, installationDate = date))
                customerToEdit = null
            }
        )
    }
}

@Composable
fun CustomerDialog(
    customer: Customer? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var location by remember { mutableStateOf(customer?.location ?: "") }
    var date by remember { mutableStateOf(customer?.installationDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (customer == null) "Pelanggan Baru" else "Edit Pelanggan", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Pelanggan") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        try {
                            val existingDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                            if (existingDate != null) calendar.time = existingDate
                        } catch (e: Exception) {}
                        
                        DatePickerDialog(context, { _, year, month, day ->
                            val selectedCalendar = Calendar.getInstance()
                            selectedCalendar.set(year, month, day)
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tgl Pasang: $date")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, location, date) }, enabled = name.isNotBlank() && location.isNotBlank(), shape = RoundedCornerShape(8.dp)) { Text("SIMPAN") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("BATAL") }
        }
    )
}
