class ProductsAdapter(
    private val products: List<ScannedProduct>,
    private val onQuantityChanged: (ScannedProduct) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ProductViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(itemView: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(product: ScannedProduct) {
            itemView.findViewById<android.widget.TextView>(R.id.productNameTextView).text = product.productName
            itemView.findViewById<android.widget.TextView>(R.id.productPriceTextView).text = String.format("%.2f", product.price)
            itemView.findViewById<android.widget.TextView>(R.id.productQuantityTextView).text = "x${product.quantity}"

            itemView.setOnClickListener {
                onQuantityChanged(product)
            }
        }
    }
}
