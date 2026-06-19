package com.example.tagihinternet.data

import android.content.Context
import com.example.tagihinternet.data.entity.Bill
import com.example.tagihinternet.data.entity.Customer
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BillingRepository(private val context: Context) {
    private val _allBills = MutableStateFlow<List<Bill>>(emptyList())
    val allBills: StateFlow<List<Bill>> = _allBills
    
    private val _allCustomers = MutableStateFlow<List<Customer>>(emptyList())
    val allCustomers: StateFlow<List<Customer>> = _allCustomers
    
    private val gson = Gson()

    private fun getService() = RetrofitClient.getGasService(context)

    suspend fun refreshData() {
        try {
            _allBills.value = getService().getBills()
            _allCustomers.value = getService().getCustomers()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertBill(bill: Bill) {
        try {
            getService().addData(table = "Bills", jsonData = gson.toJson(bill))
            refreshData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun deleteBill(bill: Bill) {
        try {
            getService().deleteData(table = "Bills", id = bill.id)
            refreshData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun updateBill(bill: Bill) {
        try {
            getService().updateData(table = "Bills", jsonData = gson.toJson(bill))
            refreshData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun insertCustomer(customer: Customer) {
        try {
            getService().addData(table = "Customers", jsonData = gson.toJson(customer))
            refreshData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun updateCustomer(customer: Customer) {
        try {
            getService().updateData(table = "Customers", jsonData = gson.toJson(customer))
            refreshData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun deleteByPeriod(month: Int, year: Int) {
        try {
            getService().deleteByPeriod(month = month, year = year)
            refreshData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun deleteCustomer(customer: Customer) {
        try {
            getService().deleteData(table = "Customers", id = customer.id)
            refreshData()
        } catch (e: Exception) { e.printStackTrace() }
    }
}
