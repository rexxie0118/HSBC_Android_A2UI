package com.a2ui.renderer.icon

import android.content.Context
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.a2ui.renderer.R

object IconManager {
    
    private val iconMap = mapOf(
        "home" to R.drawable.home,
        "home_outline" to R.drawable.home_outline,
        "transfer" to R.drawable.transfer,
        "wealth" to R.drawable.wealth,
        "insurance" to R.drawable.insurance,
        "menu" to R.drawable.menu,
        "plus" to R.drawable.plus,
        "eye" to R.drawable.eye,
        "star" to R.drawable.star,
        "more_vert" to R.drawable.more_vert,
        "arrow_right" to R.drawable.arrow_right,
        "chart_up" to R.drawable.chart_up,
        "chart_flag" to R.drawable.chart_flag,
        "currency" to R.drawable.currency,
        "pie_chart" to R.drawable.pie_chart,
        "fund" to R.drawable.fund,
        "bond" to R.drawable.bond,
        "investment" to R.drawable.investment,
        "deposit" to R.drawable.deposit,
        "gold" to R.drawable.gold,
        "loan" to R.drawable.loan,
        "umbrella" to R.drawable.umbrella,
        "notebook" to R.drawable.notebook,
        "flag" to R.drawable.flag,
        "products" to R.drawable.products,
        "service" to R.drawable.service,
        "wallet" to R.drawable.wallet,
        "chat" to R.drawable.chat,
        "mic" to R.drawable.mic,
        "send" to R.drawable.send,
        "dollar" to R.drawable.dollar,
        "percent" to R.drawable.percent,
        "cards" to R.drawable.cards
    )
    
    @Composable
    fun getIcon(iconName: String?, modifier: Modifier = Modifier): Painter? {
        if (iconName == null) return null
        val resourceId = iconMap[iconName] ?: return null
        return painterResource(id = resourceId)
    }
    
    fun getIconResId(iconName: String?): Int {
        if (iconName == null) return 0
        return iconMap[iconName] ?: 0
    }
    
    fun hasIcon(iconName: String?): Boolean {
        if (iconName == null) return false
        return iconMap.containsKey(iconName)
    }
}
