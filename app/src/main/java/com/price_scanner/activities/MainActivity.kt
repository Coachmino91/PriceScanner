package com.price_scanner.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.price_scanner.R
import com.price_scanner.data.entities.ScannedProduct
import com.price_scanner.databinding.ActivityMainBinding
import com.price_scanner.viewmodels.MainViewModel
import com.price_scanner.viewmodels.ProductsAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val price = result.data?.getStringExtra(ScannerActivity.EXTRA_PRICE)
                ?.toDoubleOrNull() ?: return@registerForActivityResult
            viewModel.addProduct("Prodotto scansionato", price)
            Toast.makeText(this, "Aggiunto: %.2f €".format(price), Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openScanner()
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

    private fun setupClickListeners() {
        binding.scanButton.setOnClickListener { requestCameraAndScan() }
        binding.resetButton.setOnClickListener { viewModel.resetAll() }
    }

    private fun observeViewModel() {
        viewModel.products.observe(this) { products ->
            binding.productsRecyclerView.adapter = ProductsAdapter(
                products,
                onQuantityChanged = { product ->
                    showQuantityDialog(product)
                },
                onProductLongPressed = { product ->
                    showDeleteProductDialog(product)
                }
            )
        }

        viewModel.totalPrice.observe(this) { total ->
            binding.totalTextView.text = "Totale: %.2f €".format(total ?: 0.0)
        }
    }

    private fun requestCameraAndScan() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openScanner()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openScanner() {
        scannerLauncher.launch(Intent(this, ScannerActivity::class.java))
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
                viewModel.updateProduct(product.copy(quantity = spinner.selectedItemPosition + 1))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showDeleteProductDialog(product: ScannedProduct) {
        AlertDialog.Builder(this)
            .setTitle("Elimina prodotto")
            .setMessage("Vuoi eliminare questo prodotto dalla lista?")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.deleteProduct(product)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
