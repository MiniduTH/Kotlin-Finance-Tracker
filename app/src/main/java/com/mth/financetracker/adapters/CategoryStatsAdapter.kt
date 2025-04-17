package com.mth.financetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mth.financetracker.R
import com.mth.financetracker.model.CategorySummary
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class CategoryStatsAdapter(
    private val categoryStats: List<CategorySummary>,
    private val currencyCode: String
) : RecyclerView.Adapter<CategoryStatsAdapter.CategoryStatsViewHolder>() {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        currency = Currency.getInstance(currencyCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryStatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_stats, parent, false)
        return CategoryStatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryStatsViewHolder, position: Int) {
        holder.bind(categoryStats[position])
    }

    override fun getItemCount() = categoryStats.size

    inner class CategoryStatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_category_name)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_category_amount)
        private val tvPercentage: TextView = itemView.findViewById(R.id.tv_category_percentage)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_category)

        fun bind(categorySummary: CategorySummary) {
            tvCategory.text = categorySummary.categoryName
            tvAmount.text = currencyFormatter.format(categorySummary.amount)
            tvPercentage.text = String.format("%.1f%%", categorySummary.percentage)
            progressBar.progress = categorySummary.percentage.toInt()
        }
    }
}