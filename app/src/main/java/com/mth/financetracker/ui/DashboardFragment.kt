package com.mth.financetracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.mth.financetracker.R
import com.mth.financetracker.data.PreferencesManager
import com.mth.financetracker.model.Transaction
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var tvTotalBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpenses: TextView
    private lateinit var tvBudgetStatus: TextView
    private lateinit var cardAddTransaction: MaterialCardView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = PreferencesManager(requireContext())
        
        tvTotalBalance = view.findViewById(R.id.tv_total_balance)
        tvIncome = view.findViewById(R.id.tv_income)
        tvExpenses = view.findViewById(R.id.tv_expenses)
        tvBudgetStatus = view.findViewById(R.id.tv_budget_status)
        cardAddTransaction = view.findViewById(R.id.card_add_transaction)
        
        cardAddTransaction.setOnClickListener {
            // Show add transaction dialog
            AddTransactionDialogFragment().show(
                parentFragmentManager,
                "AddTransactionDialog"
            )
        }
        
        updateDashboard()
    }
    
    override fun onResume() {
        super.onResume()
        updateDashboard()
    }
    
    private fun updateDashboard() {
        val transactions = preferencesManager.getTransactions()
        val currency = preferencesManager.getCurrency()
        val budget = preferencesManager.getBudget()
        
        val income = transactions.filter { it.type == Transaction.TransactionType.INCOME }
            .sumOf { it.amount }
        val expenses = transactions.filter { it.type == Transaction.TransactionType.EXPENSE }
            .sumOf { it.amount }
        val balance = income - expenses
        
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        formatter.currency = Currency.getInstance(currency)
        
        tvTotalBalance.text = formatter.format(balance)
        tvIncome.text = formatter.format(income)
        tvExpenses.text = formatter.format(expenses)
        
        // Budget status
        if (budget > 0) {
            val budgetRemaining = budget - expenses
            val percentage = (expenses / budget) * 100
            
            when {
                percentage >= 100 -> {
                    tvBudgetStatus.text = getString(R.string.budget_exceeded, formatter.format(budgetRemaining * -1))
                    tvBudgetStatus.setTextColor(resources.getColor(R.color.budget_exceeded, null))
                }
                percentage >= 80 -> {
                    tvBudgetStatus.text = getString(R.string.budget_warning, formatter.format(budgetRemaining))
                    tvBudgetStatus.setTextColor(resources.getColor(R.color.budget_warning, null))
                }
                else -> {
                    tvBudgetStatus.text = getString(R.string.budget_good, formatter.format(budgetRemaining))
                    tvBudgetStatus.setTextColor(resources.getColor(R.color.budget_good, null))
                }
            }
        } else {
            tvBudgetStatus.text = getString(R.string.no_budget_set)
            tvBudgetStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
    }
}