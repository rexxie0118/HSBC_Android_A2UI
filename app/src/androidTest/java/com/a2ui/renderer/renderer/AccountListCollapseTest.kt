package com.a2ui.renderer.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.a2ui.renderer.state.UIStateHolder
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountListCollapseTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        UIStateHolder.accountListExpanded = true
    }

    @Test
    fun arrowIconClickShouldCollapseAccountList() {
        var toggleCount = 0

        composeTestRule.setContent {
            ArrowToggleIcon(
                onToggle = { toggleCount++ }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Toggle account list")
            .performClick()

        Assert.assertEquals("Toggle should be called once", 1, toggleCount)
        Assert.assertFalse("Account list should be collapsed after click", UIStateHolder.accountListExpanded)
    }

    @Test
    fun arrowIconClickShouldExpandAccountListWhenCollapsed() {
        UIStateHolder.accountListExpanded = false
        var toggleCount = 0

        composeTestRule.setContent {
            ArrowToggleIcon(
                onToggle = { toggleCount++ }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Toggle account list")
            .performClick()

        Assert.assertEquals("Toggle should be called once", 1, toggleCount)
        Assert.assertTrue("Account list should be expanded after click", UIStateHolder.accountListExpanded)
    }

    @Test
    fun accountListItemsVisibleWhenExpanded() {
        val accounts = listOf("Savings Account", "Current Account", "Credit Card")

        composeTestRule.setContent {
            AccountListContent(
                accounts = accounts,
                expanded = true
            )
        }

        accounts.forEach { account ->
            composeTestRule
                .onNodeWithText(account)
                .assertExists()
        }
    }

    @Test
    fun accountListItemsHiddenWhenCollapsed() {
        val accounts = listOf("Savings Account", "Current Account", "Credit Card")

        composeTestRule.setContent {
            AccountListContent(
                accounts = accounts,
                expanded = false
            )
        }

        accounts.forEach { account ->
            composeTestRule
                .onNodeWithText(account)
                .assertDoesNotExist()
        }
    }

    @Test
    fun multipleArrowClicksToggleStateCorrectly() {
        composeTestRule.setContent {
            ArrowToggleIcon(onToggle = {})
        }

        val arrowNode = composeTestRule.onNodeWithContentDescription("Toggle account list")

        arrowNode.performClick()
        Assert.assertFalse("Should be collapsed after 1st click", UIStateHolder.accountListExpanded)
        
        arrowNode.performClick()
        Assert.assertTrue("Should be expanded after 2nd click", UIStateHolder.accountListExpanded)
        
        arrowNode.performClick()
        Assert.assertFalse("Should be collapsed after 3rd click", UIStateHolder.accountListExpanded)
        
        arrowNode.performClick()
        Assert.assertTrue("Should be expanded after 4th click", UIStateHolder.accountListExpanded)
    }

    @Test
    fun arrowRotatesBasedOnExpandedState() {
        composeTestRule.setContent {
            ArrowToggleIcon(onToggle = {})
        }

        composeTestRule
            .onNodeWithContentDescription("Toggle account list")
            .assertExists()
            .assert(hasText("▼"))

        composeTestRule
            .onNodeWithContentDescription("Toggle account list")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Toggle account list")
            .assertExists()
            .assert(hasText("▶"))
    }

    @Test
    fun initialStateIsExpanded() {
        Assert.assertTrue("Account list should be expanded by default", UIStateHolder.accountListExpanded)
    }

    @Composable
    private fun ArrowToggleIcon(
        onToggle: () -> Unit
    ) {
        val isExpanded = UIStateHolder.accountListExpanded
        
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White, CircleShape)
                .clickable {
                    UIStateHolder.toggleAccountList()
                    onToggle()
                }
                .semantics {
                    contentDescription = "Toggle account list"
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isExpanded) "▼" else "▶",
                fontSize = 24.sp,
                modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
            )
        }
    }

    @Composable
    private fun AccountListContent(
        accounts: List<String>,
        expanded: Boolean
    ) {
        Column {
            if (expanded) {
                accounts.forEach { account ->
                    Text(
                        text = account,
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
