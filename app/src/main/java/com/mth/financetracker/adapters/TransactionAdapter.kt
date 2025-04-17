package com.mth.financetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mth.financetracker.R
import com.mth.financetracker.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val listener: TransactionListener
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    interface TransactionListener {
        fun onTransactionClick(transaction: Transaction)
        fun onTransactionLongClick(transaction: Transaction): Boolean
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        this.transactions = newTransactions
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_transaction_title)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_transaction_amount)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_transaction_category)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_transaction_date)

        fun bind(transaction: Transaction) {
            tvTitle.text = transaction.title
            
            // Format amount based on transaction type
            val amount = when (transaction.type) {
                Transaction.TransactionType.INCOME -> transaction.amount
                Transaction.TransactionType.EXPENSE -> -transaction.amount
            }
            tvAmount.text = currencyFormat.format(amount)
            tvAmount.setTextColor(
                itemView.context.getColor(
                    when (transaction.type) {
                        Transaction.TransactionType.INCOME -> R.color.income_green
                        Transaction.TransactionType.EXPENSE -> R.color.expense_red
                    }
                )
            )
            
            tvCategory.text = transaction.category
            tvDate.text = dateFormat.format(transaction.date)

            // Set click listeners
            itemView.setOnClickListener { listener.onTransactionClick(transaction) }
            itemView.setOnLongClickListener { listener.onTransactionLongClick(transaction) }
        }
    }
}