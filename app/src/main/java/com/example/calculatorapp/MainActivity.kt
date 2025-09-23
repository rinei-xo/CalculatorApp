package com.example.calculatorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculatorapp.ui.theme.CalculatorAppTheme
import kotlin.math.sqrt
import kotlin.math.ln
import kotlin.math.pow
import java.text.DecimalFormat


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
    var lastEvaluated by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Display
        Text(text = input, modifier = Modifier.fillMaxWidth(), fontSize = 32.sp, fontWeight = FontWeight.Bold)

        fun numberButton(num: String) {
            if (lastEvaluated) { input = num; lastEvaluated = false } else input += num
        }

        fun operatorButton(op: String) {
            input = appendOperator(input, op)
            lastEvaluated = false
        }

        // First row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("1","2","3").forEach { num ->
                Button(onClick = {
                    if (lastEvaluated) { input = num; lastEvaluated = false } else input += num
                }, modifier = Modifier.weight(1f)) { Text(num) }
            }
            Button(onClick = { input = appendOperator(input, "+"); lastEvaluated = false }, modifier = Modifier.weight(1f)) { Text("+") }
        }

        // Second row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("4","5","6").forEach { num ->
                Button(onClick = {
                    if (lastEvaluated) { input = num; lastEvaluated = false } else input += num
                }, modifier = Modifier.weight(1f)) { Text(num) }
            }
            Button(onClick = { input = appendOperator(input, "-"); lastEvaluated = false }, modifier = Modifier.weight(1f)) { Text("-") }
        }

        // Third row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("7","8","9").forEach { num ->
                Button(onClick = {
                    if (lastEvaluated) { input = num; lastEvaluated = false } else input += num
                }, modifier = Modifier.weight(1f)) { Text(num) }
            }
            Button(onClick = { input = appendOperator(input, "*"); lastEvaluated = false }, modifier = Modifier.weight(1f)) { Text("×") }
        }

        // Fourth row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (lastEvaluated) { input = "0"; lastEvaluated = false } else input += "0"
            }, modifier = Modifier.weight(1f)) { Text("0") }
            Button(onClick = { input = ""; lastEvaluated = false }, modifier = Modifier.weight(1f)) { Text("C") }
            Button(onClick = { input = evaluateExpression(input); lastEvaluated = true }, modifier = Modifier.weight(1f)) { Text("=") }
            Button(onClick = { input = appendOperator(input, "/"); lastEvaluated = false }, modifier = Modifier.weight(1f)) { Text("÷") }
        }

        // Fifth row: decimal, backspace, power, root
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (lastEvaluated) { input = "0."; lastEvaluated = false } else input = appendDot(input)
            }, modifier = Modifier.weight(1f)) { Text(".") }

            Button(onClick = { input = backspace(input); lastEvaluated = false }, modifier = Modifier.weight(1f)) { Text("⌫") }
            Button(onClick = { input = appendOperator(input, "^"); lastEvaluated = false }, modifier = Modifier.weight(1f)) { Text("^") }
            Button(onClick = { input = appendRoot(input); lastEvaluated = false }, modifier = Modifier.weight(1f)) { Text("√") }
        }
        // Sixth row: parentheses, ln, e
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { input += "(" }, modifier = Modifier.weight(1f)) { Text("(") }
            Button(onClick = { input += ")" }, modifier = Modifier.weight(1f)) { Text(")") }
            Button(onClick = { input += "ln" }, modifier = Modifier.weight(1f)) { Text("ln") }
            Button(onClick = { input += "e" }, modifier = Modifier.weight(1f)) { Text("e") }
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

// ---------------- EVALUATION ----------------
fun evaluateExpression(expr:String): String {
    return try {
        val res = simpleEval(expr)
        formatNumber(res)
    } catch(e:Exception){
        "Error"
    }
}


fun simpleEval(expr: String): Double {
    var sanitized = expr.replace("×", "*").replace("÷", "/")

    // Handle square root: √number or √(expression)
    while (sanitized.contains("√")) {
        val start = sanitized.indexOf("√")
        var end = start + 1

        // If next char is '(', find matching ')'
        val numberExpr: String
        if (sanitized.getOrNull(end) == '(') {
            end++  // skip '('
            var count = 1
            val exprStart = end
            while (count > 0 && end < sanitized.length) {
                if (sanitized[end] == '(') count++
                else if (sanitized[end] == ')') count--
                end++
            }
            numberExpr = sanitized.substring(exprStart, end - 1)
        } else {
            // Take consecutive digits and decimal point
            val exprStart = end
            while (end < sanitized.length && (sanitized[end].isDigit() || sanitized[end] == '.')) {
                end++
            }
            numberExpr = sanitized.substring(exprStart, end)
        }

        val value = simpleEval(numberExpr)
        val sqrtValue = sqrt(value)
        sanitized = sanitized.replaceRange(start, end, sqrtValue.toString())
    }

// Handle ln(...) function
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
            while (end < sanitized.length && (sanitized[end].isDigit() || sanitized[end] == '.' || sanitized[end] == 'e' || sanitized[end]=='-')) end++
            numberExpr = sanitized.substring(start, end)
        }
        val value = simpleEval(numberExpr)
        sanitized = sanitized.replaceRange(idx, end, ln(value).toString())
    }

    // Tokenize numbers and operators
    val tokens = mutableListOf<String>()
    var number = ""
    for (c in sanitized) {
        if (c.isDigit() || c == '.') {
            number += c
        } else if (c in "+-*/^") {
            if (number.isNotEmpty()) {
                tokens.add(number)
                number = ""
            }
            tokens.add(c.toString())
        }
    }
    if (number.isNotEmpty()) tokens.add(number)

    // Handle ^ first
    val expRegex = Regex("(-?\\d+(\\.\\d+)?(e-?\\d+)?)\\^(-?\\d+(\\.\\d+)?(e-?\\d+)?)")
    while (expRegex.containsMatchIn(sanitized)) {
        val match = expRegex.find(sanitized)!!
        val base = match.groupValues[1].toDouble()
        val exponent = match.groupValues[4].toDouble()
        sanitized = sanitized.replaceRange(match.range, base.pow(exponent).toString())
    }

    // Handle * and /
    val mulDivRegex = Regex("(-?\\d+(\\.\\d+)?(e-?\\d+)?)([*/])(-?\\d+(\\.\\d+)?(e-?\\d+)?)")
    while (mulDivRegex.containsMatchIn(sanitized)) {
        val match = mulDivRegex.find(sanitized)!!
        val left = match.groupValues[1].toDouble()
        val op = match.groupValues[4]
        val right = match.groupValues[5].toDouble()
        val res = if (op == "*") left * right else left / right
        sanitized = sanitized.replaceRange(match.range, res.toString())
    }


    // Handle + and -
    val addSubRegex = Regex("(-?\\d+(\\.\\d+)?(e-?\\d+)?)([+-])(-?\\d+(\\.\\d+)?(e-?\\d+)?)")
    while (addSubRegex.containsMatchIn(sanitized)) {
        val match = addSubRegex.find(sanitized)!!
        val left = match.groupValues[1].toDouble()
        val op = match.groupValues[4]
        val right = match.groupValues[5].toDouble()
        val res = if (op == "+") left + right else left - right
        sanitized = sanitized.replaceRange(match.range, res.toString())
    }


    // Handle parentheses
    while (sanitized.contains("(")) {
        val start = sanitized.lastIndexOf("(")
        val end = sanitized.indexOf(")", start)
        val inner = sanitized.substring(start + 1, end)
        val value = simpleEval(inner)
        sanitized = sanitized.replaceRange(start, end + 1, value.toString())
    }

// Replace standalone 'e' with Math.E
    sanitized = sanitized.replace(Regex("(?<!\\d|\\.)e(?!\\d)"), Math.E.toString())

    return sanitized.toDouble()
}
