package com.price_scanner.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.price_scanner.data.PriceScannerDatabase
import com.price_scanner.data.entities.ScannedProduct
import com.price_scanner.repositories.PriceScannerRepository
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = PriceScannerRepository(
        PriceScannerDatabase.getDatabase(app).scannedProductDao()
    )

    val products = repository.allProducts.asLiveData()
    val totalPrice = repository.totalPrice.asLiveData()

    fun addProduct(productName: String, price: Double, quantity: Int = 1) {
        viewModelScope.launch {
            repository.insertProduct(
                ScannedProduct(
                    productName = productName,
                    price = price,
                    quantity = quantity
                )
            )
        }
    }

    fun updateProduct(product: ScannedProduct) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: ScannedProduct) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
