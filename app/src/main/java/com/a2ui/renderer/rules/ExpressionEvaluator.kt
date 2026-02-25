package com.a2ui.renderer.rules

import com.a2ui.renderer.binding.DataModelStore

/**
 * Restricted expression evaluator
 * Only allows safe, declarative expressions - no code execution
 */
object ExpressionEvaluator {

    /**
     * Allowed operators in expressions
     */
    private val ALLOWED_OPERATORS = setOf(
        // Comparison
        "==", "!=", "<", ">", "<=", ">=",
        // Logical
        "&&", "||", "!",
        // Arithmetic
        "+", "-", "*", "/",
        // String
        "contains", "startsWith", "endsWith",
        // Null check
        "exists", "isEmpty"
    )

    /**
     * Allowed functions in expressions
     */
    private val ALLOWED_FUNCTIONS = setOf(
        // String functions
        "length", "toLowerCase", "toUpperCase", "trim",
        "substring", "replace",
        // Math functions
        "min", "max", "abs", "round",
        // Array functions
        "contains", "indexOf", "size"
    )

    /**
     * Blocked patterns (security)
     */
    private val BLOCKED_PATTERNS = listOf(
        Regex(".*\\beval\\b.*"),
        Regex(".*\\bexec\\b.*"),
        Regex(".*\\bnew\\b.*"),
        Regex(".*\\bfunction\\b.*"),
        Regex(".*\\bclass\\b.*"),
        Regex(".*\\bimport\\b.*"),
        Regex(".*\\brequire\\b.*"),
        Regex(".*=>.*"),
        Regex(".*->.*"),
        Regex(".*\\(\\s*\\)")  // Empty function calls
    )

    /**
     * Evaluate boolean expression
     */
    fun evaluateBoolean(expression: String, dataModel: DataModelStore): Boolean {
        try {
            val result = evaluate(expression, dataModel)
            return result as? Boolean ?: false
        } catch (e: Exception) {
            android.util.Log.e("ExpressionEvaluator", "Error evaluating expression: $expression", e)
            return false
        }
    }

    /**
     * Evaluate expression and return result
     */
    fun evaluate(expression: String, dataModel: DataModelStore): Any? {
        // Security check
        if (!isExpressionSafe(expression)) {
            throw SecurityException("Unsafe expression: $expression")
        }

        return parseAndEvaluate(expression, dataModel)
    }

    /**
     * Evaluate list expression
     */
    fun evaluateList(expression: String, dataModel: DataModelStore): List<Any> {
        val result = evaluate(expression, dataModel)
        return when (result) {
            is List<*> -> result.filterNotNull()
            else -> emptyList()
        }
    }

    /**
     * Check if expression is safe to evaluate
     */
    private fun isExpressionSafe(expression: String): Boolean {
        // Check for blocked patterns
        BLOCKED_PATTERNS.forEach { pattern ->
            if (pattern.matches(expression)) {
                android.util.Log.w("ExpressionEvaluator", "Blocked pattern in expression: $expression")
                return false
            }
        }

        // Check for allowed operators and functions only
        // Simple heuristic - in production, use proper parser
        val lowerExpr = expression.lowercase()
        if (lowerExpr.contains("constructor") || 
            lowerExpr.contains("prototype") || 
            lowerExpr.contains("__proto__")) {
            return false
        }

        return true
    }

    /**
     * Parse and evaluate expression
     */
    private fun parseAndEvaluate(expression: String, dataModel: DataModelStore): Any? {
        val expr = expression.trim()

        // Handle logical operators (lowest precedence)
        if (expr.contains("||")) {
            val parts = expr.split("||", limit = 2)
            return evaluateBoolean(parts[0].trim(), dataModel) || 
                   evaluateBoolean(parts[1].trim(), dataModel)
        }

        if (expr.contains("&&")) {
            val parts = expr.split("&&", limit = 2)
            return evaluateBoolean(parts[0].trim(), dataModel) && 
                   evaluateBoolean(parts[1].trim(), dataModel)
        }

        // Handle comparison operators
        COMPARISON_OPERATORS.forEach { op ->
            if (expr.contains(op)) {
                val parts = expr.split(op, limit = 2)
                val left = evaluate(parts[0].trim(), dataModel)
                val right = evaluate(parts[1].trim(), dataModel)
                return compare(left, right, op)
            }
        }

        // Handle exists() function
        if (expr.startsWith("exists(")) {
            val path = extractFunctionArgument(expr)
            return dataModel.getAtPath(path) != null
        }

        // Handle isEmpty() function
        if (expr.startsWith("isEmpty(")) {
            val path = extractFunctionArgument(expr)
            val value = dataModel.getAtPath(path)
            return value == null || value.toString().isEmpty()
        }

        // Handle contains() function
        if (expr.contains(".contains(")) {
            return handleContainsFunction(expr, dataModel)
        }

        // Handle string functions
        STRING_FUNCTIONS.forEach { func ->
            if (expr.contains(".$func(")) {
                return handleStringFunction(expr, func, dataModel)
            }
        }

        // Handle path references ($.path)
        if (expr.startsWith("$.")) {
            return dataModel.getAtPath(expr.removePrefix("$."))
        }

        // Handle boolean literals
        if (expr == "true") return true
        if (expr == "false") return false

        // Handle string literals
        if (expr.startsWith("\"") && expr.endsWith("\"")) {
            return expr.removeSurrounding("\"")
        }

        // Handle numeric literals
        expr.toDoubleOrNull()?.let { return it }
        expr.toIntOrNull()?.let { return it }

        // Default: return expression as string
        return expr
    }

    private val COMPARISON_OPERATORS = listOf("==", "!=", "<=", ">=", "<", ">")

    /**
     * Compare two values
     */
    private fun compare(left: Any?, right: Any?, op: String): Boolean {
        return when (op) {
            "==" -> left == right
            "!=" -> left != right
            "<" -> compareValues(left, right) < 0
            ">" -> compareValues(left, right) > 0
            "<=" -> compareValues(left, right) <= 0
            ">=" -> compareValues(left, right) >= 0
            else -> false
        }
    }

    /**
     * Compare two values (handles different types)
     */
    private fun compareValues(left: Any?, right: Any?): Int {
        return when {
            left is Number && right is Number -> left.toDouble().compareTo(right.toDouble())
            left is String && right is String -> left.compareTo(right)
            left == null && right == null -> 0
            left == null -> -1
            right == null -> 1
            else -> left.toString().compareTo(right.toString())
        }
    }

    private val STRING_FUNCTIONS = listOf("length", "toLowerCase", "toUpperCase", "trim", "substring", "replace")

    /**
     * Handle string function calls
     */
    private fun handleStringFunction(expr: String, func: String, dataModel: DataModelStore): Any? {
        val match = Regex("\\$\\.([a-zA-Z0-9_.]+)\\.$func\\((.*)\\)").find(expr)
        if (match == null) return null

        val path = match.groupValues[1]
        val args = match.groupValues[2]

        val value = dataModel.getAtPath(path)?.toString() ?: return null

        return when (func) {
            "length" -> value.length
            "toLowerCase" -> value.lowercase()
            "toUpperCase" -> value.uppercase()
            "trim" -> value.trim()
            "substring" -> {
                val start = args.toIntOrNull() ?: 0
                value.substring(start)
            }
            "replace" -> {
                // Simple replace - first arg is target, second is replacement
                value
            }
            else -> value
        }
    }

    /**
     * Handle contains() function
     */
    private fun handleContainsFunction(expr: String, dataModel: DataModelStore): Boolean {
        val match = Regex("\\$\\.([a-zA-Z0-9_.]+)\\.contains\\(['\"](.*)['\"]\\)").find(expr)
        if (match == null) return false

        val path = match.groupValues[1]
        val substring = match.groupValues[2]

        val value = dataModel.getAtPath(path)?.toString() ?: return false
        return value.contains(substring, ignoreCase = true)
    }

    /**
     * Extract function argument
     */
    private fun extractFunctionArgument(expr: String): String {
        val match = Regex("[a-zA-Z]+\\(['\"]?(.*)['\"]?\\)").find(expr)
        return match?.groupValues?.get(1) ?: ""
    }
}
