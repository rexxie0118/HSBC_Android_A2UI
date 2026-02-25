package com.a2ui.renderer.bridge

import android.util.Patterns
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.text.DecimalFormat
import java.util.*

/**
 * Registry for native functions callable from JSON
 * All functions must be pre-registered for security
 */
object NativeFunctionRegistry {

    private val registeredFunctions = mutableMapOf<String, (List<Any?>) -> Any?>()
    private val functionLog = mutableListOf<FunctionCall>()

    data class FunctionCall(
        val name: String,
        val timestamp: Long,
        val parameters: List<Any?>
    )

    /**
     * Register a native function for use in JSON
     */
    fun register(name: String, function: (List<Any?>) -> Any?) {
        registeredFunctions[name] = function
        android.util.Log.d("NativeFunctionRegistry", "Registered function: $name")
    }

    /**
     * Execute a registered function
     */
    fun execute(name: String, parameters: List<Any?>): Any? {
        val function = registeredFunctions[name]
            ?: throw IllegalArgumentException("Function not registered: $name")

        try {
            // Log function call for auditing
            functionLog.add(FunctionCall(name, System.currentTimeMillis(), parameters))

            // Limit log size
            if (functionLog.size > 1000) {
                functionLog.removeAt(0)
            }

            return function(parameters)
        } catch (e: Exception) {
            android.util.Log.e("NativeFunctionRegistry", "Error executing $name: ${e.message}", e)
            return null
        }
    }

    /**
     * Check if function is registered
     */
    fun isRegistered(name: String): Boolean = name in registeredFunctions

    /**
     * Get list of registered functions
     */
    fun getRegisteredFunctions(): Set<String> = registeredFunctions.keys.toSet()

    /**
     * Get function call log
     */
    fun getFunctionLog(): List<FunctionCall> = functionLog.toList()

    /**
     * Clear function log
     */
    fun clearLog() {
        functionLog.clear()
    }
}

/**
 * Built-in safe functions
 */
object BuiltinFunctions {

    fun registerAll() {
        registerValidationFunctions()
        registerFormatFunctions()
        registerCalculationFunctions()
    }

    private fun registerValidationFunctions() {
        // Email validation
        NativeFunctionRegistry.register("validateEmail") { params ->
            val email = params[0] as? String ?: return@register false
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        // Phone validation
        NativeFunctionRegistry.register("validatePhone") { params ->
            val phone = params[0] as? String ?: return@register false
            Patterns.PHONE.matcher(phone).matches()
        }

        // URL validation
        NativeFunctionRegistry.register("validateUrl") { params ->
            val url = params[0] as? String ?: return@register false
            android.webkit.URLUtil.isValidUrl(url)
        }

        // Credit card validation (Luhn algorithm)
        NativeFunctionRegistry.register("validateCreditCard") { params ->
            val number = params[0] as? String ?: return@register false
            val digits = number.filter { it.isDigit() }
            
            if (digits.length < 13 || digits.length > 19) return@register false
            
            var sum = 0
            var isEven = false
            
            for (i in digits.length - 1 downTo 0) {
                var digit = digits[i].toString().toInt()
                
                if (isEven) {
                    digit *= 2
                    if (digit > 9) digit -= 9
                }
                
                sum += digit
                isEven = !isEven
            }
            
            sum % 10 == 0
        }

        // Date validation
        NativeFunctionRegistry.register("validateDate") { params ->
            val dateStr = params[0] as? String ?: return@register false
            val format = params[1] as? String ?: "yyyy-MM-dd"
            
            try {
                SimpleDateFormat(format, Locale.getDefault()).apply {
                    isLenient = false
                }.parse(dateStr)
                true
            } catch (e: Exception) {
                false
            }
        }

        // Age validation
        NativeFunctionRegistry.register("validateAge") { params ->
            val birthDateStr = params[0] as? String ?: return@register false
            val minAge = params[1] as? Int ?: 0
            val maxAge = params[2] as? Int ?: 150
            
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val birthDate = format.parse(birthDateStr) ?: return@register false
                val today = Date()
                
                val age = calculateAge(birthDate, today)
                age in minAge..maxAge
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun registerFormatFunctions() {
        // Currency formatting
        NativeFunctionRegistry.register("formatCurrency") { params ->
            val amount = params[0] as? Double ?: return@register ""
            val currencyCode = params[1] as? String ?: "USD"
            
            try {
                val formatter = NumberFormat.getCurrencyInstance().apply {
                    setCurrency(Currency.getInstance(currencyCode))
                }
                formatter.format(amount)
            } catch (e: Exception) {
                "$$amount"
            }
        }

        // Number formatting
        NativeFunctionRegistry.register("formatNumber") { params ->
            val number = params[0] as? Double ?: return@register ""
            val pattern = params[1] as? String ?: "#,##0.00"
            
            try {
                DecimalFormat(pattern).format(number)
            } catch (e: Exception) {
                number.toString()
            }
        }

        // Date formatting
        NativeFunctionRegistry.register("formatDate") { params ->
            val timestamp = params[0] as? Long ?: return@register ""
            val format = params[1] as? String ?: "yyyy-MM-dd"
            
            try {
                SimpleDateFormat(format, Locale.getDefault()).format(Date(timestamp))
            } catch (e: Exception) {
                timestamp.toString()
            }
        }

        // Percentage formatting
        NativeFunctionRegistry.register("formatPercent") { params ->
            val value = params[0] as? Double ?: return@register ""
            val decimals = params[1] as? Int ?: 1
            
            try {
                NumberFormat.getPercentInstance().apply {
                    minimumFractionDigits = decimals
                    maximumFractionDigits = decimals
                }.format(value / 100.0)
            } catch (e: Exception) {
                "${value}%"
            }
        }
    }

    private fun registerCalculationFunctions() {
        // Tax calculation
        NativeFunctionRegistry.register("calculateTax") { params ->
            val amount = params[0] as? Double ?: return@register 0.0
            val rate = params[1] as? Double ?: 0.0
            amount * (rate / 100.0)
        }

        // Discount calculation
        NativeFunctionRegistry.register("calculateDiscount") { params ->
            val amount = params[0] as? Double ?: return@register 0.0
            val percent = params[1] as? Double ?: 0.0
            amount * (percent / 100.0)
        }

        // Total calculation
        NativeFunctionRegistry.register("calculateTotal") { params ->
            val subtotal = params[0] as? Double ?: return@register 0.0
            val tax = params[1] as? Double ?: 0.0
            val discount = params[2] as? Double ?: 0.0
            
            subtotal + tax - discount
        }

        // Interest calculation (simple)
        NativeFunctionRegistry.register("calculateSimpleInterest") { params ->
            val principal = params[0] as? Double ?: return@register 0.0
            val rate = params[1] as? Double ?: 0.0
            val time = params[2] as? Double ?: 0.0
            
            (principal * rate * time) / 100.0
        }

        // Interest calculation (compound)
        NativeFunctionRegistry.register("calculateCompoundInterest") { params ->
            val principal = params[0] as? Double ?: return@register 0.0
            val rate = params[1] as? Double ?: 0.0
            val time = params[2] as? Double ?: 0.0
            val n = params[3] as? Int ?: 1 // Compounding frequency per year
            
            val amount = principal * Math.pow(1 + (rate / 100.0) / n, n * time)
            amount - principal
        }

        // Monthly payment calculation (mortgage/loan)
        NativeFunctionRegistry.register("calculateMonthlyPayment") { params ->
            val principal = params[0] as? Double ?: return@register 0.0
            val annualRate = params[1] as? Double ?: 0.0
            val months = params[2] as? Int ?: 0
            
            if (annualRate == 0.0) return@register principal / months
            
            val monthlyRate = annualRate / 100.0 / 12.0
            val payment = principal * monthlyRate / (1 - Math.pow(1 + monthlyRate, -months.toDouble()))
            payment
        }
    }

    private fun calculateAge(birthDate: Date, currentDate: Date): Int {
        val birthCalendar = Calendar.getInstance().apply { time = birthDate }
        val currentCalendar = Calendar.getInstance().apply { time = currentDate }
        
        var age = currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        
        if (currentCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        
        return age
    }
}
