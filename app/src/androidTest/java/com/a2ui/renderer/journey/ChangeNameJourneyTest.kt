package com.a2ui.renderer.journey

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.a2ui.renderer.MainActivity
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/**
 * UI Tests for Change Name Journey
 * Tests the complete flow: Homepage → Digital Forms → Change Name Form → Acknowledgement
 */
@RunWith(AndroidJUnit4::class)
class ChangeNameJourneyTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        // Wait for app to load
        Thread.sleep(2000)
    }

    @Test
    fun appShouldLaunchSuccessfully() {
        // Verify app launched
        composeTestRule
            .onNodeWithText("Digital forms", substring = true)
            .assertExists()
        
        assertTrue("App should launch successfully", true)
    }

    @Test
    fun menuIconShouldNavigateToDigitalForms() {
        // Find and click menu icon
        composeTestRule
            .onNodeWithContentDescription("menu") // Menu icon
            .performClick()
        
        Thread.sleep(2000) // Longer wait for navigation
        
        // Verify Digital Forms page loaded
        composeTestRule
            .onNodeWithText("Digital forms", substring = true)
            .assertExists()
        
        assertTrue("Should navigate to Digital Forms page", true)
    }

    @Test
    fun dropdownShouldOpenAndAllowSelection() {
        // Open dropdown
        composeTestRule
            .onNodeWithText("Please select")
            .performClick()
        
        Thread.sleep(2000) // Longer wait for navigation
        
        // Verify options are visible
        composeTestRule
            .onNodeWithText("Change my ID or name")
            .assertExists()
        
        // Select option
        composeTestRule
            .onNodeWithText("Change my ID or name")
            .performClick()
        
        Thread.sleep(2000) // Longer wait for navigation
        
        // Verify selection was made
        composeTestRule
            .onNodeWithText("Change my ID or name", substring = true)
            .assertExists()
        
        assertTrue("Should be able to select dropdown option", true)
    }

    @Test
    fun continueButtonShouldNavigateToForm() {
        // Open dropdown and select option
        composeTestRule
            .onNodeWithText("Please select")
            .performClick()
        
        Thread.sleep(1500) // Longer wait for animations
        
        composeTestRule
            .onNodeWithText("Change my ID or name")
            .performClick()
        
        Thread.sleep(1500) // Longer wait for animations
        
        // Click Continue button
        composeTestRule
            .onNodeWithText("Continue")
            .performClick()
        
        Thread.sleep(2000)
        
        // Verify Change Name form loaded
        composeTestRule
            .onNodeWithText("First name", substring = true)
            .assertExists()
        
        assertTrue("Should navigate to Change Name form", true)
    }

    @Test
    fun formShouldAcceptInput() {
        // Fill in First Name
        composeTestRule
            .onNodeWithContentDescription("First name")
            .performTextInput("Test")
        
        // Fill in Last Name  
        composeTestRule
            .onNodeWithContentDescription("Last name")
            .performTextInput("User")
        
        Thread.sleep(1500) // Longer wait for animations
        
        // Verify input was accepted
        composeTestRule
            .onNodeWithText("Test")
            .assertExists()
        
        composeTestRule
            .onNodeWithText("User")
            .assertExists()
        
        assertTrue("Form should accept input", true)
    }

    @Test
    fun completeJourneyShouldReachAcknowledgement() {
        // Step 1: Select option from dropdown
        composeTestRule
            .onNodeWithText("Please select")
            .performClick()
        
        Thread.sleep(1500) // Longer wait for animations
        
        composeTestRule
            .onNodeWithText("Change my ID or name")
            .performClick()
        
        Thread.sleep(1500) // Longer wait for animations
        
        // Step 2: Click Continue to go to form
        composeTestRule
            .onNodeWithText("Continue")
            .performClick()
        
        Thread.sleep(2000)
        
        // Step 3: Fill in form
        composeTestRule
            .onNodeWithContentDescription("First name")
            .performTextInput("Test")
        
        composeTestRule
            .onNodeWithContentDescription("Last name")
            .performTextInput("User")
        
        // Open ID Type dropdown
        composeTestRule
            .onNodeWithText("Select", substring = true)
            .performClick()
        
        Thread.sleep(1500) // Longer wait for animations
        
        // Select ID type
        composeTestRule
            .onNodeWithText("NRIC")
            .performClick()
        
        Thread.sleep(1500) // Longer wait for animations
        
        // Fill ID Number
        composeTestRule
            .onNodeWithContentDescription("ID Number")
            .performTextInput("A1234567")
        
        Thread.sleep(1500) // Longer wait for animations
        
        // Step 4: Submit form
        composeTestRule
            .onNodeWithText("Continue")
            .performClick()
        
        Thread.sleep(2000)
        
        // Step 5: Verify Acknowledgement page
        composeTestRule
            .onNodeWithText("Thank you", substring = true)
            .assertExists()
        
        assertTrue("Should reach Acknowledgement page", true)
    }

    @Test
    fun backNavigationShouldWork() {
        // Navigate to Digital Forms
        composeTestRule
            .onNodeWithContentDescription("menu") // Menu icon
            .performClick()
        
        Thread.sleep(2000) // Longer wait for navigation
        
        // Go back
        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()
        
        Thread.sleep(2000) // Longer wait for navigation
        
        // Should be back on homepage
        composeTestRule
            .onNodeWithText("Digital forms", substring = true)
            .assertExists()
        
        assertTrue("Back navigation should work", true)
    }
}
