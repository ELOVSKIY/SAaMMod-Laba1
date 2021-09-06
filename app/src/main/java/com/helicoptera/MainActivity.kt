package com.helicoptera

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private var a = 0
    private var m: Int = 0
    private var r0: Int = 0
    private var rn1 = 0.0
    private var rn = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.calculateButton).setOnClickListener {
            onCalculateClick()
        }
    }

    private fun lehmerAlgorithm(): Double {
        rn1 = rn
        rn = a * rn1 % m
        return rn / m
    }

    private fun calculateHistogram(randoms: List<Double>) {
        val count = DoubleArray(INTERVAL_COUNT) { 0.0 }
        val intervals = DoubleArray(INTERVAL_COUNT) { 0.0 }
        var left = MIN
        var right = left + INTERVAL_STEP

        for (i in 0 until INTERVAL_COUNT) {
            for (j in randoms.indices) {
                if (randoms[j] >= left && randoms[j] < right) {
                    count[i]++
                }
            }
            val intervalValue = (right - left) / 2
            intervals[i] = intervalValue

            left += INTERVAL_STEP
            right += INTERVAL_STEP
        }

        val frequencies = count.map { it / randoms.size }


        val chart = findViewById<BarChart>(R.id.barchart)

        chart.xAxis.apply {
            position = XAxisPosition.BOTTOM
            labelCount = INTERVAL_COUNT + 1
            mEntryCount = INTERVAL_COUNT + 1

            textSize = 0.5F
            valueFormatter = formatter
        }

        chart.axisLeft.apply {
            axisMinimum = 0F
            axisMaximum = 1F / INTERVAL_COUNT * 2
            labelCount = 10
        }

        val entries = mutableListOf<BarEntry>()
        for (i in 0 until INTERVAL_COUNT) {
            entries.add(BarEntry(i.toFloat() , frequencies[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "Частота значений").apply {
            valueTextSize = 5F
            colors = ColorTemplate.COLORFUL_COLORS.toList()
        }

        val data = BarData(dataSet)
        chart.animateY(3000)
        chart.data = data;
        chart.setFitBars(true);
        chart.invalidate()

    }

    private fun calculateEstimates(randoms: List<Double>) {
        val mx = findViewById<TextView>(R.id.mx)
        val dx = findViewById<TextView>(R.id.dx)
        val sx = findViewById<TextView>(R.id.sx)

        val mxValue = randoms.sum() / N
        var dxValue = 0.0


        for (i in 0 until N) {
            dxValue += (randoms[i] - mxValue).pow(2)
        }

        dxValue /= N - 1
        val sxValue = sqrt(dxValue)

        mx.text = mxValue.toString()
        dx.text = dxValue.toString()
        sx.text = sxValue.toString()
    }

    private fun check(randoms: List<Double>) {
        val check = findViewById<TextView>(R.id.check)
        val pi = findViewById<TextView>(R.id.pi)

        var count = 0
        for (i in 0 until N / 2) {
            if (randoms[2 * i].pow(2) + randoms[2 * i + 1].pow(2) < 1) {
                count++
            }
        }

        check.text = ((count * 2).toDouble() / N.toDouble()).toString()
        pi.text = (Math.PI / 4).toString()
    }

    private fun calculatePeriodsInfo(randoms: List<Double>) {
        val periodTextView = findViewById<TextView>(R.id.period)
        val aperiodicIntervalTextView = findViewById<TextView>(R.id.aperiodicInterval)

        val checkValue: Double = lehmerAlgorithm()
        var i1 = -1
        var i2 = -1

        var count = 1
        var exit = false
        for (i in 0 until N) {
            if (randoms[i] == checkValue) {
                when (count) {
                    1 -> {
                        i1 = i
                        count++
                    }
                    2 -> {
                        i2 = i
                        count++
                    }
                    else -> exit = true
                }
                if (exit) break
            }
        }

        val P = i2 - i1
        var i3 = 0
        val L: Int

        if (i1 == -1 || i2 == -1) {
            L = N.coerceAtMost(m)
        } else {
            for (i in i2 downTo P + 1) {
                if (randoms[i] != randoms[i - P]) {
                    i3 = i
                    break
                }
            }
            if (i3 == 0) i3 = i1
            L = i3 + P
        }

        aperiodicIntervalTextView.text = L.toString()
        periodTextView.text = P.toString()
    }

    private fun onCalculateClick() {
        a = findViewById<EditText>(R.id.aField).text.toString().toInt()
        m = findViewById<EditText>(R.id.mField).text.toString().toInt()
        r0 = findViewById<EditText>(R.id.r0Field).text.toString().toInt()
        rn = r0.toDouble();

        val randoms = mutableListOf<Double>()
        for (i in 0 until N) {
            randoms.add(lehmerAlgorithm())
        }
        calculateHistogram(randoms)
        calculateEstimates(randoms)
        check(randoms)
        calculatePeriodsInfo(randoms)
    }

    private val formatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return ((value + 1) / INTERVAL_COUNT).toString()
        }
    }

    companion object {
        private const val N = 10_000_000
        private const val INTERVAL_COUNT = 20
        private const val MIN = 0.0
        private const val MAX = 1.00
        private const val INTERVAL_STEP = (MAX - MIN) / INTERVAL_COUNT
    }
}