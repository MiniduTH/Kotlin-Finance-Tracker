package com.mth.financetracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.mth.financetracker.R
import com.mth.financetracker.data.PreferencesManager
import com.mth.financetracker.model.Transaction
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class BudgetFragment : Fragment() {
    
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var tvCurrentBudget: TextView
    private lateinit var tvSpentAmount: TextView
    private lateinit var tvRemainingAmount: TextView
    private lateinit var progressBudget: ProgressBar
    private lateinit var etBudgetAmount: EditText
    private lateinit var btnSetBudget: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = PreferencesManager(requireContext())
        
        tvCurrentBudget = view.findViewById(R.id.tv_current_budget)
        tvSpentAmount = view.findViewById(R.id.tv_spent_amount)
        tvRemainingAmount = view.findViewById(R.id.tv_remaining_amount)
        progressBudget = view.findViewById(R.id.progress_budget)
        etBudgetAmount = view.findViewById(R.id.et_budget_amount)
        btnSetBudget = view.findViewById(R.id.btn_set_budget)
        
        btnSetBudget.setOnClickListener {
            val budgetText = etBudgetAmount.text.toString()
            if (budgetText.isNotEmpty()) {
                val budget = budgetText.toDoubleOrNull() ?: 0.0
                preferencesManager.setBudget(budget)
                updateBudgetInfo()
                etBudgetAmount.text.clear()
            }
        }
        
        updateBudgetInfo()
    }
    
    override fun onResume() {
        super.onResume()
        updateBudgetInfo()
    }
    
    private fun updateBudgetInfo() {
        val budget = preferencesManager.getBudget()
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        
        // Calculate expenses for the current month
        val expenses = preferencesManager.getTransactions()
            .filter { it.type == Transaction.TransactionType.EXPENSE }
            .filter {
                val cal = java.util.Calendar.getInstance()
                cal.time = it.date
                cal.get(java.util.Calendar.MONTH) == currentMonth && 
                cal.get(java.util.Calendar.YEAR) == currentYear
            }
            .sumOf { it.amount }
        
        val remaining = budget - expenses
        
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        formatter.currency = Currency.getInstance(preferencesManager.getCurrency())
        
        tvCurrentBudget.text = formatter.format(budget)
        tvSpentAmount.text = formatter.format(expenses)
        tvRemainingAmount.text = formatter.format(remaining)
        
        // Update progress bar
        if (budget > 0) {
            val progress = ((expenses / budget) * 100).toInt().coerceAtMost(100)
            progressBudget.progress = progress
            
            // Set color based on progress
            val color = when {
                progress >= 100 -> R.color.budget_exceeded
                progress >= 80 -> R.color.budget_warning
                else -> R.color.budget_good
            }
            progressBudget.progressTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(color, null)
            )
        } else {
            progressBudget.progress = 0
        }
    }
}