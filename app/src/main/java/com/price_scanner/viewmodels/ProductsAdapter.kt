package com.price_scanner.viewmodels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.price_scanner.data.entities.ScannedProduct
import com.price_scanner.databinding.ItemProductBinding

class ProductsAdapter(
    private val products: List<ScannedProduct>,
    private val onQuantityChanged: (ScannedProduct) -> Unit,
    private val onProductLongPressed: (ScannedProduct) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ScannedProduct) {
            binding.productNameTextView.text = product.productName
            binding.productPriceTextView.text = String.format("%.2f €", product.price)
            binding.productQuantityTextView.text = "Quantità: x${product.quantity}"

            binding.root.setOnClickListener {
                onQuantityChanged(product)
            }

            binding.root.setOnLongClickListener {
                onProductLongPressed(product)
                true
            }
        }
    }
}
