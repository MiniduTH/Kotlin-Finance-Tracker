package com.mth.financetracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mth.financetracker.R
import com.mth.financetracker.adapters.TransactionAdapter
import com.mth.financetracker.data.PreferencesManager
import com.mth.financetracker.model.Transaction
import com.mth.financetracker.util.CurrencyManager

class TransactionsFragment : Fragment() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddTransaction: FloatingActionButton
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var currencyManager: CurrencyManager
    private lateinit var totalAmountText: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)
        
        preferencesManager = PreferencesManager(requireContext())
        currencyManager = CurrencyManager(requireContext())
        
        recyclerView = view.findViewById(R.id.recycler_transactions)
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction)
        
        setupRecyclerView()
        
        fabAddTransaction.setOnClickListener {
            AddTransactionDialogFragment().show(
                parentFragmentManager,
                "AddTransactionDialog"
            )
        }
        
        // Example of using the currency manager to format an amount
        totalAmountText.text = "Total: ${currencyManager.formatAmount(1250.75)}"
        
        // Load transactions and apply currency formatting to each one
        loadTransactions()
        
        return view
    }
    
    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            preferencesManager.getTransactions().sortedByDescending { it.date },
            object : TransactionAdapter.TransactionListener {
                override fun onTransactionClick(transaction: Transaction) {
                    // Open transaction details/edit dialog
                    EditTransactionDialogFragment.newInstance(transaction.id)
                        .show(parentFragmentManager, "EditTransactionDialog")
                }

                override fun onTransactionLongClick(transaction: Transaction): Boolean {
                    // Show delete confirmation
                    DeleteConfirmationDialogFragment.newInstance(transaction.id)
                        .show(parentFragmentManager, "DeleteConfirmationDialog")
                    return true
                }
            }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }
    
    private fun loadTransactions() {
        // This would typically load transactions from a database
        // For each transaction, we'd format the amount using:
        // currencyManager.formatAmount(transaction.amount)
    }
    
    override fun onResume() {
        super.onResume()
        updateTransactions()
        // Refresh the display when returning to this fragment
        // to ensure the currency is updated if changed in settings
        updateCurrencyDisplay()
    }
    
    fun updateTransactions() {
        transactionAdapter.updateTransactions(
            preferencesManager.getTransactions().sortedByDescending { it.date }
        )
    }
    
    private fun updateCurrencyDisplay() {
        // Update any currency displays
        totalAmountText.text = "Total: ${currencyManager.formatAmount(1250.75)}"
        
        // If you have an adapter, you'd notify it to refresh
        // transactionsAdapter.notifyDataSetChanged()
    }
}