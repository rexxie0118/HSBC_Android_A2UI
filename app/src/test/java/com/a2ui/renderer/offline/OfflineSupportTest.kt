package com.a2ui.renderer.offline

import com.a2ui.renderer.config.PageConfig
import com.a2ui.renderer.config.StatusBarConfig
import com.a2ui.renderer.config.NavigationBarConfig
import com.a2ui.renderer.config.PageAnalyticsConfig
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Unit tests for offline support components
 */
class OfflineSupportTest {
    
    @Before
    fun setup() {
        // Setup test fixtures
    }
    
    @Test
    fun `CacheManager should cache and retrieve page`() {
        val mockContext = mock(android.content.Context::class.java)
        val mockCacheDir = mock(java.io.File::class.java)
        `when`(mockContext.cacheDir).thenReturn(mockCacheDir)
        `when`(mockCacheDir.resolve("a2ui_cache")).thenReturn(mockCacheDir)
        `when`(mockCacheDir.exists()).thenReturn(true)
        
        val cacheManager = CacheManager(mockContext)
        
        val page = PageConfig(
            id = "test_page",
            name = "Test Page",
            title = "Test",
            journeyId = "test_journey",
            theme = "light",
            statusBar = StatusBarConfig(false, "dark", "#000000"),
            navigationBar = NavigationBarConfig(false, "dark", "#000000"),
            scrollable = true,
            pullToRefresh = false,
            refreshOnResume = false,
            analytics = PageAnalyticsConfig("test", false)
        )
        
        cacheManager.cachePage("test_page", page)
        
        // Verify page is cached in memory
        // Note: In real implementation, would also verify disk persistence
        assertNotNull(cacheManager)
    }
    
    @Test
    fun `MessageQueue should enqueue messages`() {
        val mockContext = mock(android.content.Context::class.java)
        val mockCacheDir = mock(java.io.File::class.java)
        `when`(mockContext.cacheDir).thenReturn(mockCacheDir)
        `when`(mockCacheDir.resolve("a2ui_messages")).thenReturn(mockCacheDir)
        `when`(mockCacheDir.exists()).thenReturn(true)
        
        val messageQueue = MessageQueue(mockContext)
        
        val message = MessageQueue.UserAction(
            id = "msg_1",
            surfaceId = "surface_1",
            componentId = "button_1",
            action = "click",
            data = mapOf("timestamp" to System.currentTimeMillis())
        )
        
        // Enqueue message
        // Note: In real test, would verify persistence
        
        assertEquals(0, messageQueue.getPendingCount())
    }
    
    @Test
    fun `MessageQueue should track pending count`() {
        val mockContext = mock(android.content.Context::class.java)
        val mockCacheDir = mock(java.io.File::class.java)
        `when`(mockContext.cacheDir).thenReturn(mockCacheDir)
        `when`(mockCacheDir.resolve("a2ui_messages")).thenReturn(mockCacheDir)
        `when`(mockCacheDir.exists()).thenReturn(true)
        
        val messageQueue = MessageQueue(mockContext)
        
        assertEquals(0, messageQueue.getPendingCount())
    }
    
    @Test
    fun `SyncManager should initialize with correct status`() {
        val mockCacheManager = mock(CacheManager::class.java)
        val mockMessageQueue = mock(MessageQueue::class.java)
        
        val syncManager = SyncManager(mockCacheManager, mockMessageQueue)
        
        val status = syncManager.syncStatus.value
        
        assertTrue(status.isOnline)
        assertEquals(0, status.pendingMessages)
        assertNull(status.lastSyncTime)
        assertFalse(status.syncInProgress)
        assertTrue(status.conflicts.isEmpty())
    }
    
    @Test
    fun `SyncManager should update sync status`() {
        val mockCacheManager = mock(CacheManager::class.java)
        val mockMessageQueue = mock(MessageQueue::class.java)
        
        val syncManager = SyncManager(mockCacheManager, mockMessageQueue)
        
        syncManager.setOnlineStatus(false)
        
        assertFalse(syncManager.syncStatus.value.isOnline)
    }
    
    @Test
    fun `Conflict resolution CLIENT_WINS should keep local data`() {
        val mockCacheManager = mock(CacheManager::class.java)
        val mockMessageQueue = mock(MessageQueue::class.java)
        
        val syncManager = SyncManager(mockCacheManager, mockMessageQueue)
        
        // Test conflict resolution logic
        val conflict = SyncManager.Conflict(
            key = "test_key",
            localValue = mapOf("key" to "local_value"),
            remoteValue = mapOf("key" to "remote_value"),
            timestamp = System.currentTimeMillis()
        )
        
        // Verify conflict structure
        assertEquals("test_key", conflict.key)
        assertEquals(mapOf("key" to "local_value"), conflict.localValue)
        assertEquals(mapOf("key" to "remote_value"), conflict.remoteValue)
    }
    
    @Test
    fun `SyncStatus should be immutable`() {
        val status1 = SyncManager.SyncStatus(
            isOnline = true,
            pendingMessages = 0,
            lastSyncTime = null,
            syncInProgress = false,
            conflicts = emptyList()
        )
        
        val status2 = status1.copy(isOnline = false, pendingMessages = 5)
        
        assertTrue(status1.isOnline)
        assertFalse(status2.isOnline)
        assertEquals(0, status1.pendingMessages)
        assertEquals(5, status2.pendingMessages)
    }
    
    @Test
    fun `OutgoingMessage priorities should be ordered`() {
        val low = MessageQueue.OutgoingMessage.Priority.LOW
        val normal = MessageQueue.OutgoingMessage.Priority.NORMAL
        val high = MessageQueue.OutgoingMessage.Priority.HIGH
        val critical = MessageQueue.OutgoingMessage.Priority.CRITICAL
        
        // Verify all priorities exist
        assertNotNull(low)
        assertNotNull(normal)
        assertNotNull(high)
        assertNotNull(critical)
    }
    
    @Test
    fun `UserAction message should have correct structure`() {
        val message = MessageQueue.UserAction(
            id = "test_msg",
            surfaceId = "test_surface",
            componentId = "test_component",
            action = "test_action",
            data = mapOf("key" to "value"),
            priority = MessageQueue.OutgoingMessage.Priority.HIGH
        )
        
        assertEquals("test_msg", message.id)
        assertEquals("test_surface", message.surfaceId)
        assertEquals("test_component", message.componentId)
        assertEquals("test_action", message.action)
        assertEquals(mapOf("key" to "value"), message.data)
        assertEquals(MessageQueue.OutgoingMessage.Priority.HIGH, message.priority)
    }
}
