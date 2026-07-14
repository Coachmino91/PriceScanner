package com.price_scanner.activities

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.price_scanner.R
import com.price_scanner.data.entities.ScannedProduct
import com.price_scanner.databinding.ActivityMainBinding
import com.price_scanner.services.PriceScanningService
import com.price_scanner.viewmodels.MainViewModel
import com.price_scanner.viewmodels.ProductsAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    // Ricevitore locale per i prezzi dal servizio
    private val priceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val price = intent.getStringExtra(PriceScanningService.EXTRA_PRICE) ?: return
            val priceDouble = price.replace(",", ".").toDoubleOrNull() ?: return
            viewModel.addProduct("Prodotto scansionato", priceDouble)
        }
    }

    // Richiesta permesso fotocamera
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchScanningService()
        } else {
            Toast.makeText(this, "Permesso fotocamera necessario per la scansione", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            priceReceiver,
            IntentFilter(PriceScanningService.ACTION_PRICE_SCANNED)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(priceReceiver)
    }

    private fun setupClickListeners() {
        binding.resetButton.setOnClickListener {
            viewModel.resetAll()
        }
        binding.scanButton.setOnClickListener {
            requestCameraAndScan()
        }
    }

    private fun observeViewModel() {
        viewModel.products.observe(this) { products ->
            binding.productsRecyclerView.adapter = ProductsAdapter(products) { product ->
                showQuantityDialog(product)
            }
        }
        viewModel.totalPrice.observe(this) { total ->
            binding.totalTextView.text = String.format("%.2f", total ?: 0.0)
        }
    }

    private fun requestCameraAndScan() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchScanningService()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchScanningService() {
        val scannerIntent = Intent(this, PriceScanningService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(scannerIntent)
        } else {
            startService(scannerIntent)
        }
    }

    private fun showQuantityDialog(product: ScannedProduct) {
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter.createFromResource(
                this@MainActivity,
                R.array.quantities,
                android.R.layout.simple_spinner_item
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            setSelection((product.quantity - 1).coerceAtLeast(0))
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.select_quantity)
            .setView(spinner)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val selectedQuantity = spinner.selectedItemPosition + 1
                viewModel.updateProduct(product.copy(quantity = selectedQuantity))
            }
            .show()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
