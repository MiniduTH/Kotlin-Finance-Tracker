package com.mth.financetracker.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mth.financetracker.R
import com.mth.financetracker.model.CategorySummary
import java.text.NumberFormat
import java.util.Locale

class CategoryStatsAdapter(
    private val categories: List<CategorySummary>,
    private val currencySymbol: String
) : RecyclerView.Adapter<CategoryStatsAdapter.ViewHolder>() {

    private val chartColors = arrayOf(
        R.color.income_green, R.color.chart_blue, R.color.chart_purple,
        R.color.expense_red, R.color.chart_orange, R.color.chart_teal,
        R.color.chart_navy, R.color.chart_amber, R.color.chart_gray,
        R.color.chart_brown
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryIndicator: View = view.findViewById(R.id.category_color_indicator)
        val categoryName: TextView = view.findViewById(R.id.text_category_name)
        val categoryAmount: TextView = view.findViewById(R.id.text_category_amount)
        val categoryPercentage: TextView = view.findViewById(R.id.text_category_percentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_stat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        val context = holder.itemView.context
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        
        // Set category name and amount
        holder.categoryName.text = category.category
        holder.categoryAmount.text = currencySymbol + numberFormat.format(category.amount)
        
        // Set category color
        val colorResId = chartColors[position % chartColors.size]
        val color = ContextCompat.getColor(context, colorResId)
        holder.categoryIndicator.backgroundTintList = ColorStateList.valueOf(color)
        
        // Set percentage with custom background
        holder.categoryPercentage.text = String.format("%.1f%%", category.percentage)
        holder.categoryPercentage.backgroundTintList = ColorStateList.valueOf(color)
        
        // Set detailed information on click
        holder.itemView.setOnClickListener {
            // You could show a dialog with more details here
        }
    }

    override fun getItemCount() = categories.size
}