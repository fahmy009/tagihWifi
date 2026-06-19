package com.example.tagihinternet.ui.bill

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tagihinternet.data.entity.Bill
import com.example.tagihinternet.data.entity.Role
import com.example.tagihinternet.data.entity.User
import com.example.tagihinternet.ui.ViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillManagementScreen(
    factory: ViewModelFactory, 
    role: Role, 
    currentUser: User?
) {
    val billViewModel: BillViewModel = viewModel(factory = factory)
    val bills by billViewModel.allBills.collectAsState()
    
    if (role == Role.USER) {
        UserInputBillScreen(billViewModel, bills, currentUser)
    } else {
        AdminRecapBillScreen(billViewModel, bills, role)
    }
}

@Composable
fun UserInputBillScreen(viewModel: BillViewModel, allBills: List<Bill>, currentUser: User?) {
    val context = LocalContext.current
    var customerName by remember { mutableStateOf("") }
    var customerLocation by remember { mutableStateOf("") }
    val allCustomers by viewModel.allCustomers.collectAsState()
    var showSuggestions by remember { mutableStateOf(false) }

    var manualNominal by remember { mutableStateOf("") }
    var billSearchQuery by remember { mutableStateOf("") }
    
    val isLoading by viewModel.isLoading.collectAsState()

    val myBills = remember(allBills, currentUser) {
        allBills.filter { it.createdByUserId == currentUser?.id }.sortedByDescending { it.date }
    }

    val filteredBills = remember(myBills, billSearchQuery) {
        if (billSearchQuery.isEmpty()) myBills
        else myBills.filter { it.customerName.contains(billSearchQuery, ignoreCase = true) }
    }

    // Filter Pelanggan: Cek secara Global di allBills (bukan hanya myBills)
    val filteredCustomers = remember(customerName, allCustomers, customerLocation, allBills) {
        val existingCustomerNamesInHistory = allBills.map { it.customerName.lowercase() }.toSet()
        if (customerName.isEmpty() || customerLocation.isNotEmpty()) emptyList()
        else allCustomers.filter { 
            it.name.contains(customerName, ignoreCase = true) && 
            !existingCustomerNamesInHistory.contains(it.name.lowercase()) 
        }
    }

    var editingBill by remember { mutableStateOf<Bill?>(null) }
    var billToDelete by remember { mutableStateOf<Bill?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Buat Tagihan Baru", 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { 
                            customerName = it
                            showSuggestions = true
                            if (customerLocation.isNotEmpty()) customerLocation = ""
                        },
                        placeholder = { Text("Nama Pelanggan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        trailingIcon = {
                            if (customerName.isNotEmpty()) {
                                IconButton(onClick = { 
                                    customerName = ""
                                    customerLocation = ""
                                    showSuggestions = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showSuggestions && filteredCustomers.isNotEmpty(),
                        onDismissRequest = { showSuggestions = false },
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier
                            .width(with(LocalDensity.current) { 320.dp })
                            .heightIn(max = 240.dp)
                    ) {
                        filteredCustomers.forEach { customer ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(customer.name, fontWeight = FontWeight.Bold)
                                        Text(customer.location, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    customerName = customer.name
                                    customerLocation = customer.location
                                    showSuggestions = false
                                }
                            )
                        }
                    }
                }
                
                if (customerLocation.isNotEmpty()) {
                    Text(
                        "Lokasi: $customerLocation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = manualNominal,
                        onValueChange = { if (it.all { char -> char.isDigit() }) manualNominal = it },
                        placeholder = { Text("Nominal (Rp)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Button(
                        onClick = {
                            val finalAmount = manualNominal.toDoubleOrNull() ?: 0.0
                            if (customerName.isBlank()) {
                                Toast.makeText(context, "Nama pelanggan belum diisi!", Toast.LENGTH_SHORT).show()
                            } else if (finalAmount <= 0) {
                                Toast.makeText(context, "Nominal tidak valid!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.insertBill(
                                    Bill(
                                        customerName = customerName,
                                        customerLocation = customerLocation,
                                        amount = finalAmount,
                                        date = System.currentTimeMillis(),
                                        createdByUserId = currentUser?.id ?: 0,
                                        createdByUsername = currentUser?.username ?: "Unknown"
                                    )
                                ).invokeOnCompletion { 
                                    customerName = ""
                                    customerLocation = ""
                                    manualNominal = ""
                                    Toast.makeText(context, "Tagihan berhasil disimpan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Text("SIMPAN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Setoran Hari Ini", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Rp ${formatCurrency(myBills.filter { isToday(it.date) }.sumOf { it.amount })}", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("RIWAYAT TRANSAKSI", style = MaterialTheme.typography.labelLarge, color = Color.Gray, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = billSearchQuery,
                onValueChange = { billSearchQuery = it },
                placeholder = { Text("Cari Riwayat...", fontSize = 12.sp) },
                modifier = Modifier.width(180.dp).height(48.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall,
                trailingIcon = {
                    if (billSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { billSearchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredBills) { bill ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bill.customerName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            if (!bill.customerLocation.isNullOrBlank()) {
                                Text(bill.customerLocation, fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(formatDateWithTime(bill.date), fontSize = 10.sp, color = Color.LightGray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Rp ${formatCurrency(bill.amount)}", 
                                fontSize = 15.sp, 
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row {
                                IconButton(onClick = { editingBill = bill }, modifier = Modifier.size(32.dp)) {
                                    Text("✎", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = { billToDelete = bill }, modifier = Modifier.size(32.dp)) {
                                    Text("✕", color = Color.Red.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        com.example.tagihinternet.ui.components.DeveloperFooter()
    }

    if (editingBill != null) {
        EditBillNominalDialog(
            bill = editingBill!!,
            onDismiss = { editingBill = null },
            onConfirm = { newAmount ->
                viewModel.updateBill(editingBill!!.copy(amount = newAmount))
                editingBill = null
                Toast.makeText(context, "Nominal diperbarui", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (billToDelete != null) {
        AlertDialog(
            onDismissRequest = { billToDelete = null },
            title = { Text("Hapus Riwayat", fontWeight = FontWeight.Bold) },
            text = { Text("Yakin ingin menghapus riwayat tagihan untuk ${billToDelete?.customerName}?") },
            confirmButton = {
                Button(onClick = {
                    billToDelete?.let { viewModel.deleteBill(it) }
                    billToDelete = null
                    Toast.makeText(context, "Riwayat dihapus", Toast.LENGTH_SHORT).show()
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("HAPUS")
                }
            },
            dismissButton = {
                TextButton(onClick = { billToDelete = null }) { Text("BATAL") }
            }
        )
    }
}

@Composable
fun EditBillNominalDialog(bill: Bill, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var amountText by remember { mutableStateOf(bill.amount.toLong().toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Nominal", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Pelanggan: ${bill.customerName}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                    label = { Text("Nominal Baru (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(amountText.toDoubleOrNull() ?: 0.0) }, shape = RoundedCornerShape(8.dp)) {
                Text("UPDATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("BATAL") }
        }
    )
}

@Composable
fun AdminRecapBillScreen(viewModel: BillViewModel, bills: List<Bill>, role: Role) {
    val calendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedUser by remember { mutableStateOf("Semua User") }
    
    val months = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val years = (2024..2030).map { it.toString() }
    
    val userList = remember(bills) {
        listOf("Semua User") + bills.map { it.createdByUsername }.distinct()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Dropdown Bulan
            var monthExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(onClick = { monthExpanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(months[selectedMonth], fontSize = 12.sp)
                }
                DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                    months.forEachIndexed { index, month ->
                        DropdownMenuItem(text = { Text(month) }, onClick = { selectedMonth = index; monthExpanded = false })
                    }
                }
            }
            
            // Dropdown Tahun
            var yearExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(0.7f)) {
                OutlinedButton(onClick = { yearExpanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(selectedYear.toString(), fontSize = 12.sp)
                }
                DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                    years.forEach { year ->
                        DropdownMenuItem(text = { Text(year) }, onClick = { selectedYear = year.toInt(); yearExpanded = false })
                    }
                }
            }
        }
        
        // Filter Petugas
        var userExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.padding(vertical = 8.dp)) {
            OutlinedButton(
                onClick = { userExpanded = true }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Petugas: $selectedUser", fontSize = 12.sp)
            }
            DropdownMenu(expanded = userExpanded, onDismissRequest = { userExpanded = false }) {
                userList.forEach { user ->
                    DropdownMenuItem(text = { Text(user) }, onClick = { selectedUser = user; userExpanded = false })
                }
            }
        }

        val filteredBills = bills.filter { bill ->
            val bCal = Calendar.getInstance().apply { timeInMillis = bill.date }
            val matchPeriod = bCal.get(Calendar.MONTH) == selectedMonth && bCal.get(Calendar.YEAR) == selectedYear
            val matchUser = selectedUser == "Semua User" || bill.createdByUsername == selectedUser
            matchPeriod && matchUser
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Transaksi", color = MaterialTheme.colorScheme.onPrimary)
                    Text("${filteredBills.size} Item", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Nominal", color = MaterialTheme.colorScheme.onPrimary)
                    Text("Rp ${formatCurrency(filteredBills.sumOf { it.amount })}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        if (filteredBills.isNotEmpty() && (role == Role.ADMIN || role == Role.SUPERADMIN)) {
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("HAPUS DATA ${months[selectedMonth].uppercase()} $selectedYear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredBills) { bill ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(bill.customerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (!bill.customerLocation.isNullOrBlank()) {
                                    Text(bill.customerLocation, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            Text("Rp ${formatCurrency(bill.amount)}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Petugas: ${bill.createdByUsername}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(formatDateWithTime(bill.date), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }

        com.example.tagihinternet.ui.components.DeveloperFooter()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Konfirmasi Hapus", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus SEMUA data transaksi untuk bulan ${months[selectedMonth]} $selectedYear? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBillsByPeriod(selectedMonth, selectedYear)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("HAPUS") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("BATAL") }
            }
        )
    }
}

@Composable
fun DatePickerButton(label: String, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    OutlinedButton(
        onClick = {
            DatePickerDialog(context, { _, year, month, day ->
                val result = Calendar.getInstance()
                result.set(year, month, day)
                onDateSelected(result.timeInMillis)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        },
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label, fontSize = 12.sp)
    }
}

fun formatCurrency(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)
}

fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
}

fun formatDateWithTime(timestamp: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}

fun isToday(timestamp: Long): Boolean {
    val c1 = Calendar.getInstance()
    c1.timeInMillis = timestamp
    val c2 = Calendar.getInstance()
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
           c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

fun getStartOfDay(): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun getEndOfDay(): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 23)
    c.set(Calendar.MINUTE, 59)
    c.set(Calendar.SECOND, 59)
    c.set(Calendar.MILLISECOND, 999)
    return c.timeInMillis
}
