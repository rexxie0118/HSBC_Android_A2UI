package com.a2ui.renderer.state

import org.junit.Assert.*
import org.junit.Test

class UIStateHolderTest {

    @Test
    fun `initial state should be expanded`() {
        assertTrue("Account list should be expanded by default", UIStateHolder.accountListExpanded)
    }

    @Test
    fun `toggleAccountList should collapse when expanded`() {
        // Given: Account list is expanded (default state)
        UIStateHolder.accountListExpanded = true
        
        // When: Toggle is called
        UIStateHolder.toggleAccountList()
        
        // Then: Account list should be collapsed
        assertFalse("Account list should be collapsed after toggle", UIStateHolder.accountListExpanded)
    }

    @Test
    fun `toggleAccountList should expand when collapsed`() {
        // Given: Account list is collapsed
        UIStateHolder.accountListExpanded = false
        
        // When: Toggle is called
        UIStateHolder.toggleAccountList()
        
        // Then: Account list should be expanded
        assertTrue("Account list should be expanded after toggle", UIStateHolder.accountListExpanded)
    }

    @Test
    fun `multiple toggles should alternate state correctly`() {
        // Given: Starting from expanded state
        UIStateHolder.accountListExpanded = true
        
        // When: Toggle multiple times
        UIStateHolder.toggleAccountList() // collapsed
        UIStateHolder.toggleAccountList() // expanded
        UIStateHolder.toggleAccountList() // collapsed
        
        // Then: Should end in collapsed state
        assertFalse("Account list should be collapsed after 3 toggles", UIStateHolder.accountListExpanded)
    }

    @Test
    fun `setting accountListExpanded directly should update state`() {
        // When: Set to false directly
        UIStateHolder.accountListExpanded = false
        
        // Then: State should reflect the change
        assertFalse("Account list should be false when set directly", UIStateHolder.accountListExpanded)
        
        // When: Set to true directly
        UIStateHolder.accountListExpanded = true
        
        // Then: State should reflect the change
        assertTrue("Account list should be true when set directly", UIStateHolder.accountListExpanded)
    }
}
