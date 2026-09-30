package com.example.billing

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BillingManager {
    private const val TAG = "BillingManager"
    const val ENTITLEMENT_PRO = "memoryos_pro"

    private val _isProActive = MutableStateFlow(false)
    val isProActive: StateFlow<Boolean> = _isProActive.asStateFlow()

    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _billingError = MutableStateFlow<String?>(null)
    val billingError: StateFlow<String?> = _billingError.asStateFlow()

    fun initialize(context: Context) {
        val apiKey = BuildConfig.REVENUECAT_API_KEY
        if (apiKey.isBlank() || apiKey == "UNCONFIGURED" || apiKey.startsWith("sk_")) {
            Log.e(TAG, "RevenueCat API Key is missing, unconfigured, or a secret key! Use a public key (e.g., goog_...)")
            _billingError.value = "Incorrect API Key format. Use a public key."
            return
        }

        try {
            Purchases.configure(
                PurchasesConfiguration.Builder(context, apiKey).build()
            )
            Log.i(TAG, "RevenueCat configured successfully.")
            
            fetchCustomerInfo()
            fetchOfferings()
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring RevenueCat: ${e.message}")
            _billingError.value = "Billing initialization failed."
        }
    }

    fun fetchCustomerInfo(onComplete: (CustomerInfo?) -> Unit = {}) {
        if (!Purchases.isConfigured) return
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                val hasPro = customerInfo.entitlements[ENTITLEMENT_PRO]?.isActive == true
                _isProActive.value = hasPro
                _billingError.value = null
                onComplete(customerInfo)
            }

            override fun onError(error: PurchasesError) {
                // Ignore benign errors like billing unavailable in emulator
                if (error.code != PurchasesErrorCode.PurchaseNotAllowedError) {
                    Log.e(TAG, "Error fetching customer info: ${error.message}")
                    _billingError.value = error.message
                }
                onComplete(null)
            }
        })
    }

    fun fetchOfferings() {
        if (!Purchases.isConfigured) return
        _isLoading.value = true
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                _offerings.value = offerings
                _isLoading.value = false
                _billingError.value = null
            }

            override fun onError(error: PurchasesError) {
                _isLoading.value = false
                // Ignore benign errors like billing unavailable in emulator
                if (error.code != PurchasesErrorCode.PurchaseNotAllowedError) {
                    Log.e(TAG, "Error fetching offerings: ${error.message}")
                    _billingError.value = error.message
                }
            }
        })
    }

    fun purchasePackage(activity: android.app.Activity, rPackage: Package, onResult: (Boolean, String?) -> Unit) {
        if (!Purchases.isConfigured) {
            onResult(false, "Billing system is not ready.")
            return
        }
        _isLoading.value = true
        _billingError.value = null

        Purchases.sharedInstance.purchase(
            PurchaseParams.Builder(activity, rPackage).build(),
            object : PurchaseCallback {
                override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                    val hasPro = customerInfo.entitlements[ENTITLEMENT_PRO]?.isActive == true
                    _isProActive.value = hasPro
                    _isLoading.value = false
                    _billingError.value = null
                    Log.i(TAG, "Purchase completed successfully. memoryos_pro active: $hasPro")
                    onResult(true, null)
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    _isLoading.value = false
                    if (userCancelled) {
                        Log.i(TAG, "Purchase cancelled by user.")
                        onResult(false, "Purchase cancelled.")
                    } else {
                        Log.e(TAG, "Purchase error: ${error.message}")
                        _billingError.value = error.message
                        onResult(false, error.message)
                    }
                }
            }
        )
    }

    fun restorePurchases(onResult: (Boolean, String?) -> Unit) {
        if (!Purchases.isConfigured) {
            onResult(false, "Billing system is not ready.")
            return
        }
        _isLoading.value = true
        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                val hasPro = customerInfo.entitlements[ENTITLEMENT_PRO]?.isActive == true
                _isProActive.value = hasPro
                _isLoading.value = false
                _billingError.value = null
                Log.i(TAG, "Purchases restored. memoryos_pro active: $hasPro")
                onResult(true, if (hasPro) "Subscription restored successfully!" else "No active premium subscription found.")
            }

            override fun onError(error: PurchasesError) {
                _isLoading.value = false
                _billingError.value = error.message
                Log.e(TAG, "Restore error: ${error.message}")
                onResult(false, error.message)
            }
        })
    }
}
