package com.mth.financetracker.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.mth.financetracker.R
import com.mth.financetracker.data.PreferencesManager
import com.mth.financetracker.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionDialogFragment : DialogFragment() {
    
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var tvDate: TextView
    private lateinit var radioIncome: RadioButton
    private lateinit var radioExpense: RadioButton
    private lateinit var etNote: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    
    private var selectedDate: Date = Calendar.getInstance().time
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_transaction, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferencesManager = PreferencesManager(requireContext())
        
        etTitle = view.findViewById(R.id.et_transaction_title)
        etAmount = view.findViewById(R.id.et_transaction_amount)
        spinnerCategory = view.findViewById(R.id.spinner_category)
        tvDate = view.findViewById(R.id.tv_transaction_date)
        radioIncome = view.findViewById(R.id.radio_income)
        radioExpense = view.findViewById(R.id.radio_expense)
        etNote = view.findViewById(R.id.et_transaction_note)
        btnSave = view.findViewById(R.id.btn_save)
        btnCancel = view.findViewById(R.id.btn_cancel)
        
        // Default to expense
        radioExpense.isChecked = true
        
        // Setup category spinner
        val categories = resources.getStringArray(R.array.expense_categories)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        
        // Setup date picker
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(selectedDate)
        
        tvDate.setOnClickListener {
            showDatePicker()
        }
        
        // Transaction type changes category options
        radioIncome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateCategorySpinner(true)
            }
        }
        
        radioExpense.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateCategorySpinner(false)
            }
        }
        
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        btnSave.setOnClickListener {
            saveTransaction()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                selectedDate = calendar.time
                
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvDate.text = dateFormat.format(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }
    
    private fun updateCategorySpinner(isIncome: Boolean) {
        val categories = if (isIncome) {
            resources.getStringArray(R.array.income_categories)
        } else {
            resources.getStringArray(R.array.expense_categories)
        }
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }
    
    private fun saveTransaction() {
        val title = etTitle.text.toString().trim()
        val amountStr = etAmount.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val note = etNote.text.toString().trim()
        
        // Validate input
        if (title.isEmpty()) {
            etTitle.error = "Title is required"
            return
        }
        
        if (amountStr.isEmpty()) {
            etAmount.error = "Amount is required"
            return
        }
        
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            etAmount.error = "Enter a valid amount"
            return
        }
        
        // Create transaction object
        val transaction = Transaction(
            title = title,
            amount = amount,
            category = category,
            date = selectedDate,
            type = if (radioIncome.isChecked) Transaction.TransactionType.INCOME else Transaction.TransactionType.EXPENSE,
            note = note
        )
        
        // Save to preferences
        preferencesManager.addTransaction(transaction)
        
        // Update UI
        if (parentFragment is TransactionsFragment) {
            (parentFragment as TransactionsFragment).updateTransactions()
        }
        
        dismiss()
    }
}