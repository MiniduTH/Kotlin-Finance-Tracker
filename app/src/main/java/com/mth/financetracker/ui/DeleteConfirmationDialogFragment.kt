package com.mth.financetracker.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.mth.financetracker.R
import com.mth.financetracker.data.PreferencesManager

class DeleteConfirmationDialogFragment : DialogFragment() {
    
    companion object {
        private const val ARG_TRANSACTION_ID = "transaction_id"
        
        fun newInstance(transactionId: String): DeleteConfirmationDialogFragment {
            val fragment = DeleteConfirmationDialogFragment()
            val args = Bundle()
            args.putString(ARG_TRANSACTION_ID, transactionId)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val transactionId = arguments?.getString(ARG_TRANSACTION_ID)
            ?: throw IllegalArgumentException("Transaction ID is required")
            
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_transaction)
            .setMessage(R.string.delete_transaction_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                val preferencesManager = PreferencesManager(requireContext())
                preferencesManager.deleteTransaction(transactionId)
                
                // Update UI
                if (parentFragment is TransactionsFragment) {
                    (parentFragment as TransactionsFragment).updateTransactions()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }
}