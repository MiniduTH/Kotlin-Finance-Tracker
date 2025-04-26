package com.mth.financetracker.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.mth.financetracker.R
import com.mth.financetracker.adapters.CategoryStatsAdapter
import com.mth.financetracker.data.PreferencesManager
import com.mth.financetracker.model.CategorySummary
import com.mth.financetracker.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible

class StatisticsFragment : Fragment(), OnChartValueSelectedListener {
    
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var incomeExpenseChart: BarChart
    private lateinit var chartTypeGroup: ChipGroup
    private lateinit var chipPieChart: Chip
    private lateinit var chipBarChart: Chip
    private lateinit var chipLineChart: Chip
    private lateinit var tabLayout: TabLayout
    private lateinit var chartTitle: TextView
    private lateinit var noDataText: TextView
    private lateinit var chartLoading: ProgressBar
    private lateinit var chartSummary: TextView
    private lateinit var incomeSummary: TextView
    private lateinit var expenseSummary: TextView
    private lateinit var noCategoriesText: TextView
    
    private val chartColors = intArrayOf(
        "#2ecc71".toColorInt(), "#3498db".toColorInt(),
        "#9b59b6".toColorInt(), "#e74c3c".toColorInt(),
        "#f39c12".toColorInt(), "#1abc9c".toColorInt(),
        "#34495e".toColorInt(), "#e67e22".toColorInt(),
        "#95a5a6".toColorInt(), Color.parseColor("#d35400")
    )
    
    private var currentChartType = 0 // 0: pie, 1: bar, 2: line
    
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
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_category_stats)
        pieChart = view.findViewById(R.id.pie_chart)
        barChart = view.findViewById(R.id.bar_chart)
        lineChart = view.findViewById(R.id.line_chart)
        incomeExpenseChart = view.findViewById(R.id.income_expense_chart)
        chartTypeGroup = view.findViewById(R.id.chart_type_group)
        chipPieChart = view.findViewById(R.id.chip_pie_chart)
        chipBarChart = view.findViewById(R.id.chip_bar_chart)
        chipLineChart = view.findViewById(R.id.chip_line_chart)
        tabLayout = view.findViewById(R.id.tab_data_period)
        chartTitle = view.findViewById(R.id.chart_title)
        noDataText = view.findViewById(R.id.no_data_text)
        chartLoading = view.findViewById(R.id.chart_loading)
        chartSummary = view.findViewById(R.id.chart_summary)
        noCategoriesText = view.findViewById(R.id.no_categories_text)
        
        // Initialize summaries if available
        try {
            incomeSummary = view.findViewById(R.id.text_income_summary)
            expenseSummary = view.findViewById(R.id.text_expense_summary)
        } catch (e: Exception) {
            // Ignore if not available
        }
        
        // Setup help buttons
        setupInfoButtons(view)
        
        // Setup individual chip click listeners instead of ChipGroup listener
        chipPieChart.setOnClickListener {
            currentChartType = 0
            updateChartVisibility()
            updateCharts()
        }
        
        chipBarChart.setOnClickListener {
            currentChartType = 1
            updateChartVisibility()
            updateCharts()
        }
        
        chipLineChart.setOnClickListener {
            currentChartType = 2
            updateChartVisibility()
            updateCharts()
        }
        
        // Setup tab layout listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateCharts()
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        
        // Configure pie chart
        setupPieChart()
        
        // Configure bar chart
        setupBarChart()
        
        // Configure line chart
        setupLineChart()
        
        // Configure income vs expense chart
        setupIncomeExpenseChart()
        
        // Start with pie chart selected
        currentChartType = 0
        chipPieChart.isChecked = true
        updateChartVisibility()
        updateCharts()
    }
    
    private fun updateChartVisibility() {
        // Update chip selection state
        chipPieChart.isChecked = currentChartType == 0
        chipBarChart.isChecked = currentChartType == 1
        chipLineChart.isChecked = currentChartType == 2
        
        // Update chart visibility
        pieChart.visibility = if (currentChartType == 0) View.VISIBLE else View.GONE
        barChart.visibility = if (currentChartType == 1) View.VISIBLE else View.GONE
        lineChart.visibility = if (currentChartType == 2) View.VISIBLE else View.GONE
        
        // Update chart title and summary
        when (currentChartType) {
            0 -> {
                chartTitle.text = getString(R.string.spending_by_category)
                chartSummary.text = getString(R.string.pie_chart_explanation)
            }
            1 -> {
                chartTitle.text = getString(R.string.monthly_trend)
                chartSummary.text = getString(R.string.bar_chart_explanation)
            }
            2 -> {
                chartTitle.text = getString(R.string.expense_trend)
                chartSummary.text = getString(R.string.line_chart_explanation)
            }
        }
        
        // Make sure summary is visible
        chartSummary.visibility = View.VISIBLE
    }
    
    private fun setupInfoButtons(view: View) {
        // Chart info button
        val chartInfoButton: ImageButton = view.findViewById(R.id.btn_chart_info)
        chartInfoButton.setOnClickListener {
            showInfoDialog(
                when (currentChartType) {
                    0 -> getString(R.string.pie_chart)
                    1 -> getString(R.string.bar_chart)
                    2 -> getString(R.string.line_chart)
                    else -> getString(R.string.statistics)
                },
                when (currentChartType) {
                    0 -> getString(R.string.pie_chart_explanation) + "\n\n" + getString(R.string.tap_for_details)
                    1 -> getString(R.string.bar_chart_explanation) + "\n\n" + getString(R.string.pinch_to_zoom)
                    2 -> getString(R.string.line_chart_explanation) + "\n\n" + getString(R.string.swipe_to_explore)
                    else -> getString(R.string.chart_type_explainer)
                }
            )
        }
        
        // Income expense info button
        view.findViewById<ImageButton>(R.id.btn_income_expense_info)?.setOnClickListener {
            showInfoDialog(
                getString(R.string.income_vs_expense),
                getString(R.string.income_expense_explanation)
            )
        }
        
        // Category info button
        view.findViewById<ImageButton>(R.id.btn_category_info)?.setOnClickListener {
            showInfoDialog(
                getString(R.string.spending_by_category),
                getString(R.string.pie_chart_explanation)
            )
        }
    }
    
    private fun showInfoDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
    
    private fun showLoading() {
        chartLoading.visibility = View.VISIBLE
        noDataText.visibility = View.GONE
    }
    
    private fun hideLoading() {
        chartLoading.visibility = View.GONE
    }
    
    override fun onResume() {
        super.onResume()
        updateCharts()
    }
    
    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400)
            setOnChartValueSelectedListener(this@StatisticsFragment)
            
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                xEntrySpace = 7f
                yEntrySpace = 0f
                yOffset = 0f
                isWordWrapEnabled = true
            }
            
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
        }
    }
    
    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled = false
            setPinchZoom(true)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            setFitBars(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textSize = 12f
                labelRotationAngle = 45f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                setDrawZeroLine(true) 
                setDrawAxisLine(true)
                textSize = 12f
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                form = Legend.LegendForm.SQUARE
                formSize = 9f
                textSize = 11f
                xEntrySpace = 4f
            }
            
            setOnChartValueSelectedListener(this@StatisticsFragment)
            animateY(1400)
        }
    }
    
    private fun setupLineChart() {
        lineChart.apply {
            description.isEnabled = false
            setPinchZoom(true)
            setDrawGridBackground(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                setDrawAxisLine(true)
                labelRotationAngle = 45f
                textSize = 12f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                setDrawZeroLine(true)
                setDrawAxisLine(true)
                textSize = 12f
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                form = Legend.LegendForm.LINE
                formSize = 9f
                textSize = 11f
                xEntrySpace = 4f
            }
            
            setOnChartValueSelectedListener(this@StatisticsFragment)
            animateX(1400)
        }
    }
    
    private fun setupIncomeExpenseChart() {
        incomeExpenseChart.apply {
            description.isEnabled = false
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            setFitBars(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textSize = 14f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                setDrawZeroLine(true)
                setDrawAxisLine(true)
                textSize = 12f
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                isEnabled = false
            }
            
            setOnChartValueSelectedListener(this@StatisticsFragment)
            animateY(1400)
        }
    }
    
    private fun updateCharts() {
        showLoading()
        
        val transactions = getTransactionsBySelectedPeriod()
        
        // Show or hide charts based on data availability
        if (transactions.isEmpty()) {
            hideLoading()
            noDataText.visibility = View.VISIBLE
            pieChart.visibility = View.GONE
            barChart.visibility = View.GONE
            lineChart.visibility = View.GONE
            noCategoriesText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }
        
        noDataText.visibility = View.GONE
        
        // Update category stats if the recycler view exists
        try {
            updateCategoryStats(transactions)
        } catch (e: Exception) {
            Log.e("StatisticsFragment", "Error updating category stats: ${e.message}")
        }
        
        // Update the charts based on the current chart type
        when (currentChartType) {
            0 -> updatePieChart(transactions)
            1 -> updateBarChart(transactions)
            2 -> updateLineChart(transactions)
        }
        
        // Update income vs expense chart
        try {
            updateIncomeExpenseChart(transactions)
        } catch (e: Exception) {
            Log.e("StatisticsFragment", "Error updating income/expense chart: ${e.message}")
        }
        
        // Hide loading indicator
        hideLoading()
    }
    
    private fun getTransactionsBySelectedPeriod(): List<Transaction> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Get selected time period
        val transactions = when (tabLayout.selectedTabPosition) {
            0 -> { // This month
                preferencesManager.getTransactions().filter {
                    val cal = Calendar.getInstance()
                    cal.time = it.date
                    cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                }
            }
            1 -> { // Last 6 months
                calendar.add(Calendar.MONTH, -5)
                val startMonth = calendar.get(Calendar.MONTH)
                val startYear = calendar.get(Calendar.YEAR)
                
                preferencesManager.getTransactions().filter {
                    val cal = Calendar.getInstance()
                    cal.time = it.date
                    val year = cal.get(Calendar.YEAR)
                    val month = cal.get(Calendar.MONTH)
                    
                    (year > startYear || (year == startYear && month >= startMonth)) &&
                    (year < currentYear || (year == currentYear && month <= currentMonth))
                }
            }
            2 -> { // Last 12 months
                calendar.add(Calendar.YEAR, -1)
                val startYear = calendar.get(Calendar.YEAR)
                val startMonth = calendar.get(Calendar.MONTH)
                
                preferencesManager.getTransactions().filter {
                    val cal = Calendar.getInstance()
                    cal.time = it.date
                    val year = cal.get(Calendar.YEAR)
                    val month = cal.get(Calendar.MONTH)
                    
                    (year > startYear || (year == startYear && month >= startMonth)) &&
                    (year < currentYear || (year == currentYear && month <= currentMonth))
                }
            }
            else -> preferencesManager.getTransactions()
        }
        
        return transactions
    }
    
    private fun updateCategoryStats(transactions: List<Transaction>) {
        // Calculate category stats for expenses
        val expensesByCategory = transactions
            .filter { it.type == Transaction.TransactionType.EXPENSE }
            .groupBy { it.category }
        
        val totalExpenses = transactions
            .filter { it.type == Transaction.TransactionType.EXPENSE }
            .sumOf { it.amount }
        
        if (expensesByCategory.isEmpty() || totalExpenses == 0.0) {
            noCategoriesText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }
        
        noCategoriesText.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        
        val categoryStats = expensesByCategory.map { (category, transactions) ->
            val amount = transactions.sumOf { it.amount }
            val percentage = if (totalExpenses > 0) (amount / totalExpenses * 100) else 0.0
            CategorySummary(category, amount, percentage)
        }.sortedByDescending { it.amount }
        
        // Setup recycler view
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CategoryStatsAdapter(categoryStats, preferencesManager.getCurrency())
        
        // Show top category in summary if available
        if (categoryStats.isNotEmpty()) {
            val topCategory = categoryStats[0]
            val formatter = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(preferencesManager.getCurrency())
            }
            val formattedAmount = formatter.format(topCategory.amount)
            
            chartSummary.text = getString(
                R.string.top_expense_category,
                topCategory.category,
                formattedAmount
            )
            chartSummary.visibility = View.VISIBLE
        }
    }
    
    private fun updatePieChart(transactions: List<Transaction>) {
        // Get expense categories and amounts
        val expensesByCategory = transactions
            .filter { it.type == Transaction.TransactionType.EXPENSE }
            .groupBy { it.category }
        
        val totalExpenses = transactions
            .filter { it.type == Transaction.TransactionType.EXPENSE }
            .sumOf { it.amount }
        
        if (expensesByCategory.isEmpty() || totalExpenses == 0.0) {
            pieChart.visibility = View.GONE
            noDataText.visibility = View.VISIBLE
            return
        }
        
        noDataText.visibility = View.GONE
        
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        
        expensesByCategory.entries.forEachIndexed { index, entry ->
            val amount = entry.value.sumOf { it.amount }
            val percentage = (amount / totalExpenses * 100).toFloat()
            if (percentage > 0) {
                entries.add(PieEntry(percentage, entry.key))
                colors.add(chartColors[index % chartColors.size])
            }
        }
        
        val dataSet = PieDataSet(entries, "")
        dataSet.apply {
            this.colors = colors
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(pieChart)
            valueLineColor = Color.WHITE
            valueLinePart1Length = 0.3f
            valueLinePart2Length = 0.4f
            valueLineWidth = 2f
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        }
        
        val data = PieData(dataSet)
        pieChart.apply { 
            this.data = data
            highlightValues(null)
            
            // Currency format for center text
            val formatter = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(preferencesManager.getCurrency())
            }
            centerText = formatter.format(totalExpenses)
            
            invalidate()
        }
    }
    
    private fun updateBarChart(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            barChart.visibility = View.GONE
            noDataText.visibility = View.VISIBLE
            return
        }
        
        noDataText.visibility = View.GONE
        
        // Group transactions by month
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val monthlyData = transactions
            .groupBy { 
                val cal = Calendar.getInstance()
                cal.time = it.date
                dateFormat.format(cal.time)
            }
            .toSortedMap()
        
        val labels = monthlyData.keys.toList()
        val expenseEntries = ArrayList<BarEntry>()
        val incomeEntries = ArrayList<BarEntry>()
        
        monthlyData.values.forEachIndexed { index, transactions ->
            val expenses = transactions
                .filter { it.type == Transaction.TransactionType.EXPENSE }
                .sumOf { it.amount }
                .toFloat()
            
            val income = transactions
                .filter { it.type == Transaction.TransactionType.INCOME }
                .sumOf { it.amount }
                .toFloat()
            
            expenseEntries.add(BarEntry(index.toFloat(), expenses))
            incomeEntries.add(BarEntry(index.toFloat(), income))
        }
        
        val expenseDataSet = BarDataSet(expenseEntries, getString(R.string.expense))
        expenseDataSet.apply {
            color = Color.rgb(231, 76, 60) // Red
            valueTextSize = 10f
            setDrawValues(false)  // Hide values initially, shown on selection
        }
        
        val incomeDataSet = BarDataSet(incomeEntries, getString(R.string.income))
        incomeDataSet.apply {
            color = Color.rgb(46, 204, 113) // Green
            valueTextSize = 10f
            setDrawValues(false)  // Hide values initially, shown on selection
        }
        
        val data = BarData(expenseDataSet, incomeDataSet)
        data.barWidth = 0.3f
        
        barChart.apply {
            this.data = data
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            groupBars(0f, 0.1f, 0.05f)
            setVisibleXRangeMaximum(6f) // Show max 6 months at a time
            moveViewToX(labels.size.toFloat()) // Move to latest month
            
            // Set marker view for details
            val mv = MonthlyChartMarkerView(
                requireContext(),
                R.layout.marker_view,
                preferencesManager.getCurrency()
            )
            mv.chartView = this
            marker = mv
            
            invalidate()
        }
    }
    
    private fun updateLineChart(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            lineChart.visibility = View.GONE
            noDataText.visibility = View.VISIBLE
            return
        }
        
        noDataText.visibility = View.GONE
        
        // Sort transactions by date
        val sortedTransactions = transactions.sortedBy { it.date }
        
        // Group by date for expenses
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val dailyExpenses = sortedTransactions
            .filter { it.type == Transaction.TransactionType.EXPENSE }
            .groupBy { dateFormat.format(it.date) }
            .toSortedMap()
        
        val labels = dailyExpenses.keys.toList()
        val entries = ArrayList<Entry>()
        
        dailyExpenses.values.forEachIndexed { index, transactions ->
            val totalExpense = transactions.sumOf { it.amount }.toFloat()
            entries.add(Entry(index.toFloat(), totalExpense))
        }
        
        val dataSet = LineDataSet(entries, getString(R.string.expense_trend))
        dataSet.apply {
            color = Color.rgb(231, 76, 60) // Red
            setCircleColor(Color.rgb(231, 76, 60))
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 10f
            setDrawValues(false)  // Hide values initially, shown on selection
            setDrawFilled(true)
            fillColor = Color.rgb(231, 76, 60)
            fillAlpha = 30
            mode = LineDataSet.Mode.CUBIC_BEZIER
            
            // Add gradient fill
            val drawable = context?.getDrawable(R.drawable.fade_red)
            fillDrawable = drawable
        }
        
        val data = LineData(dataSet)
        
        lineChart.apply {
            this.data = data
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = minOf(7, labels.size) // Show max 7 labels to avoid crowding
            setVisibleXRangeMaximum(7f) // Show at most 7 data points
            moveViewToX(entries.size.toFloat()) // Move to latest point
            
            // Set marker view for details
            val mv = DailyChartMarkerView(
                requireContext(), 
                R.layout.marker_view,
                preferencesManager.getCurrency()
            )
            mv.chartView = this
            marker = mv
            
            invalidate()
        }
    }
    
    private fun updateIncomeExpenseChart(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            incomeExpenseChart.visibility = View.GONE
            return
        }
        
        incomeExpenseChart.visibility = View.VISIBLE
        
        // Group transactions by type (Income vs Expense)
        val totalIncome = transactions
            .filter { it.type == Transaction.TransactionType.INCOME }
            .sumOf { it.amount }
            .toFloat()
        
        val totalExpense = transactions
            .filter { it.type == Transaction.TransactionType.EXPENSE }
            .sumOf { it.amount }
            .toFloat()
        
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, totalIncome))
        entries.add(BarEntry(1f, totalExpense))
        
        val dataSet = BarDataSet(entries, "")
        dataSet.apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.income_green),
                ContextCompat.getColor(requireContext(), R.color.expense_red)
            )
            valueTextSize = 12f
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val formatter = NumberFormat.getCurrencyInstance().apply {
                        currency = Currency.getInstance(preferencesManager.getCurrency())
                    }
                    return formatter.format(value.toDouble())
                }
            }
        }
        
        val data = BarData(dataSet)
        data.barWidth = 0.6f
        
        incomeExpenseChart.apply {
            this.data = data
            xAxis.valueFormatter = IndexAxisValueFormatter(
                listOf(getString(R.string.income), getString(R.string.expense))
            )
            invalidate()
        }
        
        // Update summary texts
        try {
            val formatter = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(preferencesManager.getCurrency())
            }
            
            incomeSummary.text = getString(
                R.string.total_income_summary, 
                formatter.format(totalIncome)
            )
            
            expenseSummary.text = getString(
                R.string.total_expense_summary, 
                formatter.format(totalExpense)
            )
            
            // Add conditional savings/overspending summary
            if (totalIncome > totalExpense) {
                val savings = totalIncome - totalExpense
                val savingPercent = (savings / totalIncome * 100).toInt()
                
                incomeSummary.append("\n" + getString(
                    R.string.savings_summary,
                    formatter.format(savings),
                    "$savingPercent%"
                ))
                
                if (savings > totalIncome * 0.3) {
                    Snackbar.make(
                        requireView(),
                        "Great job! You're saving more than 30% of your income.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else if (totalExpense > totalIncome) {
                val overspending = totalExpense - totalIncome
                val overspendingPercent = if (totalIncome > 0) {
                    (overspending / totalIncome * 100).toInt()
                } else {
                    100
                }
                
                expenseSummary.append("\n" + getString(
                    R.string.overspending_summary,
                    formatter.format(overspending),
                    "$overspendingPercent%"
                ))
                
                if (overspending > totalIncome * 0.1) {
                    Snackbar.make(
                        requireView(),
                        "Warning: You're spending more than you earn.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            // Ignore if summary views are not available
        }
    }
    
    // Chart value selection listeners
    override fun onValueSelected(e: Entry?, h: Highlight?) {
        e ?: return
        
        when {
            pieChart.visibility == View.VISIBLE -> {
                if (e is PieEntry) {
                    val categoryName = e.label
                    val percentage = e.value
                    showCategoryDetails(categoryName, percentage)
                }
            }
            barChart.isVisible -> {
                // Bar chart selection is handled by marker view
            }
            lineChart.isVisible -> {
                // Line chart selection is handled by marker view
            }
        }
    }
    
    override fun onNothingSelected() {
        // No action needed
    }
    
    private fun showCategoryDetails(category: String, percentage: Float) {
        val transactions = getTransactionsBySelectedPeriod()
            .filter { it.type == Transaction.TransactionType.EXPENSE && it.category == category }
        
        val totalAmount = transactions.sumOf { it.amount }
        val avgAmount = if (transactions.isNotEmpty()) totalAmount / transactions.size else 0.0
        val maxAmount = transactions.maxOfOrNull { it.amount } ?: 0.0
        
        val formatter = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(preferencesManager.getCurrency())
        }
        
        val message = StringBuilder()
            .append("Category: $category\n")
            .append("Percentage: ${String.format("%.1f", percentage)}%\n")
            .append("Total: ${formatter.format(totalAmount)}\n")
            .append("Transactions: ${transactions.size}\n")
            .append("Average: ${formatter.format(avgAmount)}\n")
            .append("Highest: ${formatter.format(maxAmount)}")
            .toString()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Category Details")
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
    
    // Custom marker view for bar chart
    inner class MonthlyChartMarkerView(
        context: Context,
        layoutResource: Int,
        private val currencySymbol: String
    ) : MarkerView(context, layoutResource) {
        
        private val tvContent: TextView = findViewById(R.id.tvContent)
        
        override fun refreshContent(e: Entry, highlight: Highlight) {
            if (e is BarEntry) {
                val dataset = if (highlight.dataSetIndex == 0) getString(R.string.expense) else getString(R.string.income)
                val amount = e.y
                
                val formatter = NumberFormat.getCurrencyInstance().apply {
                    currency = Currency.getInstance(currencySymbol)
                }
                
                tvContent.text = "$dataset: ${formatter.format(amount.toDouble())}"
            }
            super.refreshContent(e, highlight)
        }
        
        override fun getOffset(): MPPointF {
            return MPPointF(-(width / 2f), -height.toFloat() - 10)
        }
    }
    
    // Custom marker view for line chart
    inner class DailyChartMarkerView(
        context: Context,
        layoutResource: Int,
        private val currencySymbol: String
    ) : MarkerView(context, layoutResource) {
        
        private val tvContent: TextView = findViewById(R.id.tvContent)
        
        override fun refreshContent(e: Entry, highlight: Highlight) {
            val formatter = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(currencySymbol)
            }
            
            tvContent.text = "${getString(R.string.expense)}: ${formatter.format(e.y.toDouble())}"
            super.refreshContent(e, highlight)
        }
        
        override fun getOffset(): MPPointF {
            return MPPointF(-(width / 2f), -height.toFloat() - 10)
        }
    }
}