package com.a2ui.renderer.performance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Performance utilities
 */
object PerformanceUtils {
    
    /**
     * Measure execution time
     */
    inline fun <T> measure(name: String, block: () -> T): T {
        val start = System.currentTimeMillis()
        val result = block()
        val duration = System.currentTimeMillis() - start
        PerformanceMonitor.record(name, duration)
        return result
    }
}

/**
 * Debounced validation
 */
@Composable
fun DebouncedValidation(
    value: String,
    waitMs: Long = 300L,
    onValidate: (String) -> Unit
) {
    LaunchedEffect(value) {
        delay(waitMs)
        onValidate(value)
    }
}
