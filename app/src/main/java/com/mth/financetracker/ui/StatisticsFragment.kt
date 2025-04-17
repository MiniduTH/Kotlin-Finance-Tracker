package com.mth.financetracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mth.financetracker.R
import com.mth.financetracker.adapters.CategoryStatsAdapter
import com.mth.financetracker.data.PreferencesManager
import com.mth.financetracker.model.CategorySummary
import com.mth.financetracker.model.Transaction
import java.util.Calendar

class StatisticsFragment : Fragment() {
    
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var recyclerView: RecyclerView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = PreferencesManager(requireContext())
        recyclerView = view.findViewById(R.id.recycler_category_stats)
        
        updateStats()
    }
    
    override fun onResume() {
        super.onResume()
        updateStats()
    }
    
    private fun updateStats() {
        // Get all transactions for the current month
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        val transactions = preferencesManager.getTransactions().filter {
            val cal = Calendar.getInstance()
            cal.time = it.date
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }
        
        // Calculate category stats for expenses
        val expensesByCategory = transactions
            .filter { it.type == Transaction.TransactionType.EXPENSE }
            .groupBy { it.category }
        
        val totalExpenses = transactions
            .filter { it.type == Transaction.TransactionType.EXPENSE }
            .sumOf { it.amount }
        
        val categoryStats = expensesByCategory.map { (category, transactions) ->
            val amount = transactions.sumOf { it.amount }
            val percentage = if (totalExpenses > 0) (amount / totalExpenses * 100) else 0.0
            CategorySummary(category, amount, percentage)
        }.sortedByDescending { it.amount }
        
        // Setup recycler view
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CategoryStatsAdapter(categoryStats, preferencesManager.getCurrency())
    }
}