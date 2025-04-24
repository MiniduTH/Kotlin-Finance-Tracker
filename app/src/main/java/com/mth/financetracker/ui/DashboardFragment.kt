package com.mth.financetracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.mth.financetracker.R
import com.mth.financetracker.data.PreferencesManager
import com.mth.financetracker.model.Transaction
import java.text.NumberFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var tvTotalBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpenses: TextView
    private lateinit var tvBudgetStatus: TextView
    private lateinit var cardAddTransaction: MaterialCardView
    private lateinit var rvUpcomingExpenses: RecyclerView
    private lateinit var tvNoUpcomingExpenses: TextView
    private lateinit var upcomingExpensesAdapter: UpcomingExpensesAdapter
    
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
        rvUpcomingExpenses = view.findViewById(R.id.rv_upcoming_expenses)
        tvNoUpcomingExpenses = view.findViewById(R.id.tv_no_upcoming_expenses)
        
        // Setup RecyclerView for upcoming expenses
        rvUpcomingExpenses.layoutManager = LinearLayoutManager(requireContext())
        upcomingExpensesAdapter = UpcomingExpensesAdapter(emptyList())
        rvUpcomingExpenses.adapter = upcomingExpensesAdapter
        
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
            tvBudgetStatus.setTextColor(resources.getColor(R.color.black, null))
        }
        
        // Update upcoming expenses
        updateUpcomingExpenses(transactions, formatter)
    }
    
    private fun updateUpcomingExpenses(transactions: List<Transaction>, formatter: NumberFormat) {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        
        // Get upcoming expenses for the next 30 days
        calendar.add(Calendar.DAY_OF_MONTH, 30)
        val futureDate = calendar.time
        
        val upcomingExpenses = transactions.filter { 
            it.type == Transaction.TransactionType.EXPENSE && 
            it.date != null && 
            it.date.after(currentDate) && 
            it.date.before(futureDate)
        }.sortedBy { it.date }
        
        if (upcomingExpenses.isEmpty()) {
            tvNoUpcomingExpenses.visibility = View.VISIBLE
            rvUpcomingExpenses.visibility = View.GONE
        } else {
            tvNoUpcomingExpenses.visibility = View.GONE
            rvUpcomingExpenses.visibility = View.VISIBLE
            upcomingExpensesAdapter.updateExpenses(upcomingExpenses, formatter)
        }
    }
    
    inner class UpcomingExpensesAdapter(
        private var expenses: List<Transaction>
    ) : RecyclerView.Adapter<UpcomingExpensesAdapter.ViewHolder>() {
        
        private lateinit var formatter: NumberFormat
        
        fun updateExpenses(newExpenses: List<Transaction>, currencyFormatter: NumberFormat) {
            this.expenses = newExpenses
            this.formatter = currencyFormatter
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_upcoming_expense, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val expense = expenses[position]
            holder.bind(expense)
        }
        
        override fun getItemCount(): Int = expenses.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvExpenseTitle: TextView = itemView.findViewById(R.id.tv_expense_title)
            private val tvExpenseNote: TextView = itemView.findViewById(R.id.tv_expense_note)
            private val tvExpenseAmount: TextView = itemView.findViewById(R.id.tv_expense_amount)
            private val tvExpenseDate: TextView = itemView.findViewById(R.id.tv_expense_date)

            
            fun bind(expense: Transaction) {
                tvExpenseTitle.text = expense.title
                tvExpenseNote.text = expense.note
                tvExpenseAmount.text = formatter.format(expense.amount)
                tvExpenseDate.text = formatDate(expense.date)
            }
            
            private fun formatDate(date: Date?): String {
                if (date == null) return ""
                val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                return dateFormat.format(date)
            }
        }
    }
}