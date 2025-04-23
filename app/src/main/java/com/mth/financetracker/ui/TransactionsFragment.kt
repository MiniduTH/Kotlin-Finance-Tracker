package com.mth.financetracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mth.financetracker.R
import com.mth.financetracker.adapters.TransactionAdapter
import com.mth.financetracker.data.PreferencesManager
import com.mth.financetracker.model.Transaction

class TransactionsFragment : Fragment() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddTransaction: FloatingActionButton
    private lateinit var transactionAdapter: TransactionAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = PreferencesManager(requireContext())
        
        recyclerView = view.findViewById(R.id.recycler_transactions)
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction)
        
        setupRecyclerView()
        
        fabAddTransaction.setOnClickListener {
            AddTransactionDialogFragment().show(
                parentFragmentManager,
                "AddTransactionDialog"
            )
        }
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
    
    override fun onResume() {
        super.onResume()
        updateTransactions()
    }
    
    fun updateTransactions() {
        transactionAdapter.updateTransactions(
            preferencesManager.getTransactions().sortedByDescending { it.date }
        )
    }
}