package com.a2ui.renderer.performance

/**
 * Performance monitor for A2UI Renderer
 */
object PerformanceMonitor {
    
    private val metrics = mutableMapOf<String, MutableList<Long>>()
    private var isEnabled = true
    
    /**
     * Enable/disable monitoring
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
    
    /**
     * Start monitoring
     */
    fun startMonitoring() {
        isEnabled = true
    }
    
    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        isEnabled = false
    }
    
    /**
     * Record metric
     */
    fun record(metricName: String, durationMs: Long) {
        if (!isEnabled) return
        
        metrics.getOrPut(metricName) { mutableListOf() }.add(durationMs)
        
        // Report slow operations
        if (durationMs > 16) {
            android.util.Log.w("PerformanceMonitor", "Slow operation: $metricName took ${durationMs}ms")
        }
    }
    
    /**
     * Get average duration for metric
     */
    fun getAverage(metricName: String): Double {
        val values = metrics[metricName] ?: return 0.0
        return if (values.isEmpty()) 0.0 else values.average()
    }
    
    /**
     * Get 90th percentile
     */
    fun get90thPercentile(metricName: String): Long {
        val values = metrics[metricName] ?: return 0
        if (values.isEmpty()) return 0
        
        val sorted = values.sorted()
        val index = (sorted.size * 0.9).toInt()
        return sorted.getOrElse(index) { sorted.lastOrNull() ?: 0 }
    }
    
    /**
     * Clear metrics
     */
    fun clear() {
        metrics.clear()
    }
    
    /**
     * Get all metrics
     */
    fun getMetrics(): Map<String, List<Long>> {
        return metrics.toMap()
    }
}

/**
 * Performance targets
 */
object PerformanceTargets {
    const val FRAME_TIME_MS = 16L  // 60fps
    const val LIST_SCROLL_FPS = 55
    const val COLD_STARTUP_MS = 2000L
    const val FIRST_CONTENT_PAINT_MS = 100L
    const val THEME_SWITCH_MS = 100L
    const val BINDING_RESOLVE_MS = 1L
    const val MEMORY_USAGE_MB = 100L
}
