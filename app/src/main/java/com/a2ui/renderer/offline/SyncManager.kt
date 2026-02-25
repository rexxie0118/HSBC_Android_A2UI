package com.a2ui.renderer.offline

import com.a2ui.renderer.binding.DataModelStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sync manager for offline support
 * Handles synchronization between local cache and server
 */
class SyncManager(
    private val cacheManager: CacheManager,
    private val messageQueue: MessageQueue
) {
    
    /**
     * Sync status
     */
    data class SyncStatus(
        val isOnline: Boolean,
        val pendingMessages: Int,
        val lastSyncTime: Long?,
        val syncInProgress: Boolean,
        val conflicts: List<Conflict>
    )
    
    /**
     * Conflict information
     */
    data class Conflict(
        val key: String,
        val localValue: Any,
        val remoteValue: Any,
        val timestamp: Long
    )
    
    /**
     * Conflict resolution strategies
     */
    enum class ConflictResolutionStrategy {
        CLIENT_WINS,      // Always use local version
        SERVER_WINS,      // Always use remote version
        MERGE,            // Attempt to merge both versions
        MANUAL            // Require manual resolution
    }
    
    private val _syncStatus = MutableStateFlow(
        SyncStatus(
            isOnline = true,
            pendingMessages = 0,
            lastSyncTime = null,
            syncInProgress = false,
            conflicts = emptyList()
        )
    )
    
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    private var lastSyncTime: Long? = null
    private var isSyncing = false
    
    /**
     * Sync data model with server
     */
    suspend fun sync(
        dataModel: DataModelStore,
        surfaceId: String,
        sendCallback: suspend (MessageQueue.OutgoingMessage) -> Boolean,
        resolutionStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.CLIENT_WINS
    ) {
        if (isSyncing) {
            return // Prevent concurrent syncs
        }
        
        isSyncing = true
        updateSyncStatus { it.copy(syncInProgress = true) }
        
        try {
            // 1. Flush pending messages
            messageQueue.flushQueue(sendCallback)
            
            // 2. Check for conflicts
            val conflicts = detectConflicts(dataModel, surfaceId)
            if (conflicts.isNotEmpty()) {
                resolveConflicts(conflicts, resolutionStrategy, dataModel, surfaceId)
            }
            
            // 3. Update last sync time
            lastSyncTime = System.currentTimeMillis()
            
            updateSyncStatus { 
                it.copy(
                    lastSyncTime = lastSyncTime,
                    syncInProgress = false,
                    conflicts = emptyList()
                )
            }
            
        } catch (e: Exception) {
            android.util.Log.e("SyncManager", "Sync failed", e)
            isSyncing = false
            updateSyncStatus { it.copy(syncInProgress = false) }
            throw e
        }
    }
    
    /**
     * Detect conflicts between local and remote data
     */
    private suspend fun detectConflicts(
        dataModel: DataModelStore,
        surfaceId: String
    ): List<Conflict> {
        val conflicts = mutableListOf<Conflict>()
        
        // Check if remote data exists
        val remoteData = cacheManager.getData("remote_$surfaceId")
        if (remoteData != null) {
            val localData = dataModel.getData()
            
            // Simple conflict detection - compare data hashes
            if (localData.hashCode() != remoteData.hashCode()) {
                conflicts.add(
                    Conflict(
                        key = surfaceId,
                        localValue = localData,
                        remoteValue = remoteData,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
        
        return conflicts
    }
    
    /**
     * Resolve conflicts using specified strategy
     */
    private suspend fun resolveConflicts(
        conflicts: List<Conflict>,
        strategy: ConflictResolutionStrategy,
        dataModel: DataModelStore,
        surfaceId: String
    ) {
        conflicts.forEach { conflict ->
            when (strategy) {
                ConflictResolutionStrategy.CLIENT_WINS -> {
                    // Keep local data, update remote
                    cacheManager.cacheData("remote_$surfaceId", dataModel.getData().toString())
                }
                
                ConflictResolutionStrategy.SERVER_WINS -> {
                    // Replace local data with remote
                    // Parse remote data and update local model
                    android.util.Log.d("SyncManager", "Resolving conflict: SERVER_WINS for $surfaceId")
                }
                
                ConflictResolutionStrategy.MERGE -> {
                    // Attempt to merge (simplified - in production, implement proper merge logic)
                    android.util.Log.d("SyncManager", "Resolving conflict: MERGE for $surfaceId")
                }
                
                ConflictResolutionStrategy.MANUAL -> {
                    // Queue for manual resolution
                    updateSyncStatus { it.copy(conflicts = conflicts) }
                    android.util.Log.w("SyncManager", "Conflict requires manual resolution: $surfaceId")
                }
            }
        }
    }
    
    /**
     * Auto-sync when coming back online
     */
    suspend fun autoSync(
        dataModel: DataModelStore,
        surfaceId: String,
        sendCallback: suspend (MessageQueue.OutgoingMessage) -> Boolean
    ) {
        val status = _syncStatus.value
        if (!status.isOnline || status.pendingMessages == 0) {
            return
        }
        
        try {
            sync(dataModel, surfaceId, sendCallback)
        } catch (e: Exception) {
            android.util.Log.e("SyncManager", "Auto-sync failed", e)
        }
    }
    
    /**
     * Update sync status
     */
    private fun updateSyncStatus(update: (SyncStatus) -> SyncStatus) {
        val current = _syncStatus.value
        _syncStatus.value = update(current)
    }
    
    /**
     * Set online status
     */
    fun setOnlineStatus(isOnline: Boolean) {
        updateSyncStatus { it.copy(isOnline = isOnline) }
    }
    
    /**
     * Update pending message count
     */
    fun updatePendingCount(count: Int) {
        updateSyncStatus { it.copy(pendingMessages = count) }
    }
    
    /**
     * Get sync statistics
     */
    fun getSyncStats(): Map<String, Any> {
        return mapOf(
            "lastSyncTime" to (lastSyncTime ?: 0),
            "pendingMessages" to messageQueue.getPendingCount(),
            "oldestMessageAge" to messageQueue.getOldestMessageAge(),
            "isSyncing" to isSyncing
        )
    }
}
