package com.example.tagihinternet.ui.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tagihinternet.data.BillingRepository
import com.example.tagihinternet.data.entity.Bill
import com.example.tagihinternet.data.entity.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BillViewModel(private val billingRepository: BillingRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val allBills = billingRepository.allBills
    val allCustomers = billingRepository.allCustomers

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _isLoading.value = true
        billingRepository.refreshData()
        _isLoading.value = false
    }

    fun insertBill(bill: Bill) = viewModelScope.launch {
        _isLoading.value = true
        billingRepository.insertBill(bill)
        _isLoading.value = false
    }
    
    fun deleteBill(bill: Bill) = viewModelScope.launch {
        _isLoading.value = true
        billingRepository.deleteBill(bill)
        _isLoading.value = false
    }

    fun updateBill(bill: Bill) = viewModelScope.launch {
        _isLoading.value = true
        billingRepository.updateBill(bill)
        _isLoading.value = false
    }

    fun insertCustomer(customer: Customer) = viewModelScope.launch {
        _isLoading.value = true
        billingRepository.insertCustomer(customer)
        _isLoading.value = false
    }

    fun updateCustomer(customer: Customer) = viewModelScope.launch {
        _isLoading.value = true
        billingRepository.updateCustomer(customer)
        _isLoading.value = false
    }

    fun deleteCustomer(customer: Customer) = viewModelScope.launch {
        _isLoading.value = true
        billingRepository.deleteCustomer(customer)
        _isLoading.value = false
    }

    fun deleteBillsByPeriod(month: Int, year: Int) = viewModelScope.launch {
        _isLoading.value = true
        billingRepository.deleteByPeriod(month, year)
        _isLoading.value = false
    }
}
