package com.example.calculatorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculatorapp.ui.theme.CalculatorAppTheme
import kotlin.math.*
import java.text.DecimalFormat

// Global variables
var lastAns: Double = 0.0
var justEvaluated: Boolean = false
var useDegrees: Boolean = true

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalculatorApp()
                }
            }
        }
    }
}

@Composable
fun CalculatorApp() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var lastResult by remember { mutableStateOf(0.0) }
    var newInput by remember { mutableStateOf(true) } // <--- track if next input should start fresh
    var justEvaluated by remember { mutableStateOf(false) }
    var useDegrees by remember { mutableStateOf(true) }
    var lastAnsDouble by remember { mutableStateOf(0.0) } // Stores last answer as number


    fun resetIfJustEvaluatedForTyping() {
        if (justEvaluated) {
            input = ""
            justEvaluated = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display (right aligned)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(text = input, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(text = result, fontSize = 28.sp, fontWeight = FontWeight.Medium)
        }

        // Buttons
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            // Row 1: 1 2 3 +
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1", "2", "3").forEach { num ->
                    Button(onClick = {
                        if (justEvaluated) { input = ""; justEvaluated = false }
                        input += num
                    }, modifier = Modifier.weight(1f)) { Text(num) }
                }
                Button(onClick = {
                    if (justEvaluated) { input = result; justEvaluated = false }
                    input += "+"
                }, modifier = Modifier.weight(1f)) { Text("+") }
            }

            // Row 2: 4 5 6 -
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("4", "5", "6").forEach { num ->
                    Button(onClick = {
                        if (justEvaluated) { input = ""; justEvaluated = false }
                        input += num
                    }, modifier = Modifier.weight(1f)) { Text(num) }
                }
                Button(onClick = {
                    if (justEvaluated) { input = result; justEvaluated = false }
                    input += "-"
                }, modifier = Modifier.weight(1f)) { Text("-") }
            }

            // Row 3: 7 8 9 ×
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("7", "8", "9").forEach { num ->
                    Button(onClick = {
                        if (justEvaluated) { input = ""; justEvaluated = false }
                        input += num
                    }, modifier = Modifier.weight(1f)) { Text(num) }
                }
                Button(onClick = {
                    if (justEvaluated) { input = result; justEvaluated = false }
                    input += "*"
                }, modifier = Modifier.weight(1f)) { Text("×") }
            }

            // Row 4: 0 . 1/x ÷
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (justEvaluated) { input = ""; justEvaluated = false }
                    input += "0"
                }, modifier = Modifier.weight(1f)) { Text("0") }

                Button(onClick = {
                    if (justEvaluated) { input = ""; justEvaluated = false }
                    input = appendDot(input)
                }, modifier = Modifier.weight(1f)) { Text(".") }

                Button(onClick = {
                    if (justEvaluated && result.isNotEmpty()) {
                        val value = result.replace(",", "").toDoubleOrNull()
                        if (value != null && value != 0.0) {
                            lastResult = 1 / value
                            result = formatNumber(lastResult)
                            input = "1/(${value})"
                        }
                    } else if (input.isNotEmpty()) {
                        val lastNum = Regex("(\\d+\\.?\\d*)$").find(input)?.value
                        if (lastNum != null) {
                            input = input.dropLast(lastNum.length) + "1/($lastNum)"
                        }
                    }
                }, modifier = Modifier.weight(1f)) { Text("1/x") }


                Button(onClick = {
                    if (justEvaluated) { input = result; justEvaluated = false }
                    input += "/"
                }, modifier = Modifier.weight(1f)) { Text("÷") }
            }

            // Row 5: sin cos tan exp
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("sin", "cos", "tan").forEach { fn ->
                    Button(onClick = {
                        if (justEvaluated) { input = ""; justEvaluated = false }
                        input += fn
                    }, modifier = Modifier.weight(1f)) { Text(fn) }
                }
                Button(onClick = {
                    if (justEvaluated) { input = ""; justEvaluated = false }
                    input += "e"
                }, modifier = Modifier.weight(1f)) { Text("exp") }
            }

            // Row 6: ( ) ln log
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("(", ")", "ln", "log").forEach { fn ->
                    Button(onClick = {
                        if (justEvaluated) { input = ""; justEvaluated = false }
                        input += fn
                    }, modifier = Modifier.weight(1f)) { Text(fn) }
                }
            }

            // Row 7: ^ x² n√ √
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { input += "^" }, modifier = Modifier.weight(1f)) { Text("^") }
                Button(onClick = { input += "^2" }, modifier = Modifier.weight(1f)) { Text("x²") }
                Button(onClick = { input += "√" }, modifier = Modifier.weight(1f)) { Text("n√") }
                Button(onClick = {
                    if (justEvaluated) { input = ""; justEvaluated = false }
                    input += "√"
                }, modifier = Modifier.weight(1f)) { Text("√") }

            }

            // Row 8: π % DEG/RAD, fraction
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { input += Math.PI.toString() }, modifier = Modifier.weight(1f)) { Text("π") }
                Button(onClick = { input += "%" }, modifier = Modifier.weight(1f)) { Text("%") }
                Button(onClick = { useDegrees = !useDegrees }) { Text(if (useDegrees) "DEG" else "RAD") }
                Button(
                    onClick = {
                        val value = result.toDoubleOrNull() ?: 0.0
                        input = decimalToFractionWithMixed(value)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("a/b") }


            }

            // Row 9: Clear Back Ans =
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { input = ""; result = "" }, modifier = Modifier.weight(1f)) { Text("Clear") }
                Button(onClick = { if (input.isNotEmpty()) input = input.dropLast(1) }, modifier = Modifier.weight(1f)) { Text("⌫") }
                Button(onClick = { input += "Ans" }, modifier = Modifier.weight(1f)) { Text("Ans") }
                Button(onClick = {
                    try {
                        val valDouble = simpleEval(input)
                        result = formatNumber(valDouble)
                        lastAns = valDouble   // ✅ now Double-to-Double
                        justEvaluated = true
                    } catch (e: Exception) {
                        result = "Error"
                        justEvaluated = true
                    }
                }, modifier = Modifier.weight(1f)) { Text("=") }

            }
        }
    }
}


// ------------------- UTILITY FUNCTIONS -------------------


// Backspace function
fun backspace(input: String): String {
    return if (input.isNotEmpty()) input.dropLast(1) else input
}

// Append decimal safely (reuse from before)
fun appendDot(input: String): String {
    if (input.isEmpty() || input.last() in "+-*/^") return input + "0."
    val lastNumber = input.split("+", "-", "*", "/", "^").last()
    if (lastNumber.contains(".")) return input
    return input + "."
}

// Append square root safely
fun appendRoot(input: String): String {
    // Automatically wrap the next number
    return input + "√"
}


// Safely append an operator
fun appendOperator(input: String, operator: String): String {
    if (input.isEmpty()) return input
    if (input.last() in "+-*/^") return input.dropLast(1) + operator
    return input + operator
}


fun formatNumber(num: Double): String {
    return if (num % 1.0 == 0.0) {
        // It's an integer
        val formatter = DecimalFormat("#,###")
        formatter.format(num.toLong())
    } else {
        // It's a decimal
        val formatter = DecimalFormat("#,###.########") // up to 8 decimal places
        formatter.format(num)
    }
}
fun formatForInput(num: Double): String {
    return if (num % 1.0 == 0.0) num.toInt().toString() else num.toString()
}

// Convert decimal to fraction
fun decimalToFractionWithMixed(decimal: Double, maxDenominator: Int = 10000): String {
    if (decimal.isNaN() || decimal.isInfinite()) return decimal.toString()

    val integerPart = decimal.toInt()
    var fractionalPart = decimal - integerPart

    // If no fractional part → return integer
    if (fractionalPart == 0.0) return integerPart.toString()

    // Continued fraction approximation
    var num0 = 0
    var denom0 = 1
    var num1 = 1
    var denom1 = 0
    var value = fractionalPart
    var a: Int

    while (true) {
        a = kotlin.math.floor(value).toInt()
        val num2 = a * num1 + num0
        val denom2 = a * denom1 + denom0

        if (denom2 > maxDenominator) break

        num0 = num1; denom0 = denom1
        num1 = num2; denom1 = denom2

        val fracPart = value - a
        if (fracPart < 1e-10) break
        value = 1.0 / fracPart
    }

    val numerator = num1
    val denominator = denom1

    return if (integerPart != 0) {
        "$integerPart($numerator/$denominator)"  // mixed fraction
    } else {
        "$numerator/$denominator"
    }
}


// Convert fraction back to decimal
fun fractionToDecimal(fraction: String): Double {
    val parts = fraction.split("/")
    return if (parts.size == 2) parts[0].toDouble() / parts[1].toDouble() else fraction.toDouble()
}


// ---------------- EVALUATION ----------------
fun evaluateExpression(expr: String): String {
    return try {
        val res = simpleEval(expr)
        formatNumber(res)
    } catch (e: Exception) {
        "Error"
    }
}

fun simpleEval(expr: String): Double {
    var sanitized = expr.replace("×", "*").replace("÷", "/")

    // Normalize leading negative like "-2+3"
    if (sanitized.startsWith("-")) sanitized = "0$sanitized"

    // Ans replacement
    sanitized = sanitized.replace("Ans", lastAns.toString())

    // Replace π with Math.PI
    sanitized = sanitized.replace("π", Math.PI.toString())

    // Replace standalone 'e' with Math.E (not part of 2e2)
    sanitized = sanitized.replace(Regex("(?<!\\d)\\be(?![\\d.])"), Math.E.toString())

    // handle e2 → e^2
    sanitized = sanitized.replace(Regex("e(\\d+)")) {
        val exp = it.groupValues[1].toInt()
        Math.E.pow(exp).toString()
    }


    // ------------------ Nth root ------------------
    val nthRootRegex = Regex("(\\d+)√(\\d+(\\.\\d+)?)")
    while (nthRootRegex.containsMatchIn(sanitized)) {
        val match = nthRootRegex.find(sanitized)!!
        val n = match.groupValues[1].toDouble()
        val x = match.groupValues[2].toDouble()
        val value = x.pow(1.0 / n)
        sanitized = sanitized.replaceRange(match.range, value.toString())
    }

    // ------------------ HANDLE PARENTHESES ------------------
    while (sanitized.contains("(")) {
        val start = sanitized.lastIndexOf("(")
        val end = sanitized.indexOf(")", start)
        val inner = sanitized.substring(start + 1, end)
        val value = simpleEval(inner)
        sanitized = sanitized.replaceRange(start, end + 1, value.toString())
    }

    // ------------------ Square root ------------------
    while (sanitized.contains("√")) {
        val start = sanitized.indexOf("√")
        var end = start + 1

        val numberExpr: String
        if (sanitized.getOrNull(end) == '(') {
            end++
            var count = 1
            val exprStart = end
            while (count > 0 && end < sanitized.length) {
                if (sanitized[end] == '(') count++
                else if (sanitized[end] == ')') count--
                end++
            }
            numberExpr = sanitized.substring(exprStart, end - 1)
        } else {
            val exprStart = end
            while (end < sanitized.length && (sanitized[end].isDigit() || sanitized[end] == '.' || sanitized[end] == 'e' || sanitized[end] == '-')) end++
            numberExpr = sanitized.substring(exprStart, end)
        }

        val value = simpleEval(numberExpr)
        sanitized = sanitized.replaceRange(start, end, sqrt(value).toString())
    }

    // ------------------ ln(...) ------------------
    while (sanitized.contains("ln")) {
        val idx = sanitized.indexOf("ln")
        var start = idx + 2
        var end = start
        val numberExpr: String
        if (sanitized.getOrNull(end) == '(') {
            end++
            var count = 1
            val startExpr = end
            while (count > 0 && end < sanitized.length) {
                if (sanitized[end] == '(') count++
                else if (sanitized[end] == ')') count--
                end++
            }
            numberExpr = sanitized.substring(startExpr, end - 1)
        } else {
            while (end < sanitized.length && (sanitized[end].isDigit() || sanitized[end] == '.' || sanitized[end] == 'e' || sanitized[end] == '-')) end++
            numberExpr = sanitized.substring(start, end)
        }
        val value = simpleEval(numberExpr)
        sanitized = sanitized.replaceRange(idx, end, ln(value).toString())
    }

    // ------------------ log(...) ------------------
    while (sanitized.contains("log")) {
        val idx = sanitized.indexOf("log")
        var start = idx + 3
        var end = start
        val numberExpr: String
        if (sanitized.getOrNull(end) == '(') {
            end++
            var count = 1
            val startExpr = end
            while (count > 0 && end < sanitized.length) {
                if (sanitized[end] == '(') count++
                else if (sanitized[end] == ')') count--
                end++
            }
            numberExpr = sanitized.substring(startExpr, end - 1)
        } else {
            while (end < sanitized.length && (sanitized[end].isDigit() || sanitized[end] == '.' || sanitized[end] == 'e' || sanitized[end] == '-')) end++
            numberExpr = sanitized.substring(start, end)
        }
        val value = simpleEval(numberExpr)
        sanitized = sanitized.replaceRange(idx, end, kotlin.math.log10(value).toString())
    }

    // ------------------ Trig functions (degrees → radians) ------------------
    val trigFunctions = listOf("sin", "cos", "tan")
    for (func in trigFunctions) {
        while (sanitized.contains(func)) {
            val idx = sanitized.indexOf(func)
            var start = idx + func.length
            var end = start
            val numberExpr: String
            if (sanitized.getOrNull(end) == '(') {
                end++
                var count = 1
                val startExpr = end
                while (count > 0 && end < sanitized.length) {
                    if (sanitized[end] == '(') count++
                    else if (sanitized[end] == ')') count--
                    end++
                }
                numberExpr = sanitized.substring(startExpr, end - 1)
            } else {
                while (end < sanitized.length && (sanitized[end].isDigit() || sanitized[end] == '.' || sanitized[end] == 'e' || sanitized[end] == '-')) end++
                numberExpr = sanitized.substring(start, end)
            }
            val value = simpleEval(numberExpr)
            val trigValue = when (func) {
                "sin" -> if (useDegrees) sin(Math.toRadians(value)) else sin(value)
                "cos" -> if (useDegrees) cos(Math.toRadians(value)) else cos(value)
                "tan" -> if (useDegrees) tan(Math.toRadians(value)) else tan(value)
                else -> value
            }

            sanitized = sanitized.replaceRange(idx, end, trigValue.toString())
        }
    }
    // ------------------ HANDLE x² ------------------
    val squareRegex = Regex("(\\d+(\\.\\d+)?)\\^2")
    while (squareRegex.containsMatchIn(sanitized)) {
        val match = squareRegex.find(sanitized)!!
        val value = match.groupValues[1].toDouble()
        sanitized = sanitized.replaceRange(match.range, (value * value).toString())
    }


    // ------------------ HANDLE PERCENTAGES ------------------
    val percentRegex = Regex("(\\d+(\\.\\d+)?)%")
    while (percentRegex.containsMatchIn(sanitized)) {
        val match = percentRegex.find(sanitized)!!
        val value = match.groupValues[1].toDouble()
        sanitized = sanitized.replaceRange(match.range, (value / 100).toString())
    }

    // ------------------ HANDLE RECIPROCAL 1/x ------------------
    val recipRegex = Regex("1/\\(([^)]+)?") // optional closing parenthesis
    while (recipRegex.containsMatchIn(sanitized)) {
        val match = recipRegex.find(sanitized)!!
        val exprToEval = match.groupValues[1]
        val value = simpleEval(exprToEval)
        sanitized = sanitized.replaceRange(match.range, (1 / value).toString())
    }


    // ------------------ TOKENIZE AND EVALUATE ------------------
    val tokens = mutableListOf<String>()
    var number = ""
    for ((i, c) in sanitized.withIndex()) {
        if (c.isDigit() || c == '.') {
            number += c
        } else if (c == '-') {
            if (i == 0 || sanitized[i - 1] in "+-*/^(") {
                // negative number
                number += c
            } else {
                if (number.isNotEmpty()) {
                    tokens.add(number)
                    number = ""
                }
                tokens.add("-")
            }
        } else if (c in "+*/^") {
            if (number.isNotEmpty()) {
                tokens.add(number)
                number = ""
            }
            tokens.add(c.toString())
        }
    }
    if (number.isNotEmpty()) tokens.add(number)


    // ------------------ HANDLE POWERS ------------------
    while (tokens.contains("^")) {
        val idx = tokens.indexOf("^")
        val base = tokens[idx - 1].toDouble()
        val exp = tokens[idx + 1].toDouble()
        val value = base.pow(exp)
        tokens[idx - 1] = value.toString()
        tokens.removeAt(idx + 1)
        tokens.removeAt(idx)
    }

    // ------------------ HANDLE MULTIPLICATION AND DIVISION ------------------
    var i = 0
    while (i < tokens.size) {
        val token = tokens[i]
        if (token == "*" || token == "/") {
            val left = tokens[i - 1].toDouble()
            val right = tokens[i + 1].toDouble()
            val value = if (token == "*") left * right else left / right
            tokens[i - 1] = value.toString()
            tokens.removeAt(i + 1)
            tokens.removeAt(i)
            i--
        }
        i++
    }

    // ------------------ HANDLE ADDITION AND SUBTRACTION ------------------
    var result = tokens[0].toDouble()
    var j = 1
    while (j < tokens.size) {
        val op = tokens[j]
        val next = tokens[j + 1].toDouble()
        result = when (op) {
            "+" -> result + next
            "-" -> result - next
            else -> result
        }
        j += 2
    }

    return result
}