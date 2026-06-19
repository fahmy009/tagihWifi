package com.example.tagihinternet.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tagihinternet.data.BillingRepository
import kotlinx.coroutines.flow.first

class BillingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = BillingRepository(applicationContext)
        val prefs = applicationContext.getSharedPreferences("billing_prefs", Context.MODE_PRIVATE)
        val lastBillId = prefs.getLong("last_bill_id", 0L)

        return try {
            repository.refreshData()
            val bills = repository.allBills.first()
            val latestBill = bills.maxByOrNull { it.id }

            if (latestBill != null && latestBill.id > lastBillId) {
                NotificationHelper.showNotification(
                    applicationContext,
                    "Tagihan Baru!",
                    "${latestBill.createdByUsername} baru saja menginput tagihan untuk ${latestBill.customerName}"
                )
                prefs.edit().putLong("last_bill_id", latestBill.id).apply()
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
