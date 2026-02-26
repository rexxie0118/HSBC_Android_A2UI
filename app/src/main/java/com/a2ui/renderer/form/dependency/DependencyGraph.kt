package com.a2ui.renderer.form.dependency

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Tracks dependencies between form elements using a directed graph structure.
 * Thread-safe implementation using Mutex for synchronization.
 * Allows determining which elements are directly or transitively affected by a change to any given element.
 */
class DependencyGraph {
    // Protect all state mutations with mutex to stay threadsafe
    private val lock = Mutex()
    
    // elementId -> set of other element IDs it depends ON
    private var _dependentOnGraph: MutableMap<String, MutableSet<String>> = mutableMapOf()
    
    // elementId -> set of other elements that depend on it
    private var _affectsGraph: MutableMap<String, MutableSet<String>> = mutableMapOf()
    
    // Immutable snapshots for consumers
    val dependentOnGraph: Map<String, Set<String>>
        get() = _dependentOnGraph.toMap().mapValues { (_, value) -> value.toSet() }
        
    val affectsGraph: Map<String, Set<String>>
        get() = _affectsGraph.toMap().mapValues { (_, value) -> value.toSet() }
    
    /**
     * Establish a dependency where dependentElement depends on parentElement.
     * e.g., "confirmPassword" depends on "password" -> confirmPassword depends-on password
     */
    suspend fun trackDependency(dependentElement: String, parentElement: String) {
        lock.withLock {
            // Update dependent graph (this element has dependency on parent)
            val currentDependencies = _dependentOnGraph.getOrPut(dependentElement) { mutableSetOf() }
            currentDependencies.add(parentElement)
            
            // Update affects graph (parent affects dependent element)
            val currentlyAffected = _affectsGraph.getOrPut(parentElement) { mutableSetOf() }
            currentlyAffected.add(dependentElement)
        }
    }
    
    /**
     * Get all elements that the given element directly depends on.
     * @return Set of element ids that this element's state depends on.
     */
    suspend fun getDependedOnElements(elementId: String): Set<String> {
        return lock.withLock {
            (_dependentOnGraph[elementId] ?: emptySet<String>()).toSet()
        }
    }
    
    /**
     * Get all elements that are directly affected by changes to the given element.
     * @return Set of element ids that are immediately affected by changes to this element.
     */
    suspend fun getAffectingElements(elementId: String): Set<String> {
        return lock.withLock {
            (_affectsGraph[elementId] ?: emptySet<String>()).toSet()
        }
    }
    
    /**
     * Get all elements that should be re-evaluated when the given element changes.
     * This includes both direct and transitive dependencies.
     */
    suspend fun getAffectedElementsTransitively(elementId: String): Set<String> {
        val result = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        val visited = mutableSetOf<String>()
        
        lock.withLock {
            // Start with immediate effects
            val initialAffects = _affectsGraph[elementId]?.toList() ?: emptyList()
            queue.addAll(initialAffects)
            visited.addAll(initialAffects)
        
            // Traverse transitively to find all affected elements
            while (queue.isNotEmpty()) {
                val currentElement = lock.withLock { queue.removeFirst() }
                result.add(currentElement)
            
                // Add more elements affected by this element for further processing
                val directlyAffected = lock.withLock {
                    _affectsGraph[currentElement]?.filterNot { item -> visited.contains(item) } ?: emptyList()
                }
                
                lock.withLock {
                    queue.addAll(directlyAffected)
                    visited.addAll(directlyAffected)
                }
            }
        }
        
        return result
    }
    
    /**
     * Remove a dependency relationship.
     */
    suspend fun removeDependency(dependentElement: String, parentElement: String) {
        lock.withLock {
            // Update dependent graph
            val currentDeps = _dependentOnGraph[dependentElement]
            if (currentDeps != null) {
                currentDeps.remove(parentElement)
                if (currentDeps.isEmpty()) {
                    _dependentOnGraph.remove(dependentElement)
                }
            }
            
            // Update affects graph
            val currentlyAffected = _affectsGraph[parentElement]
            if (currentlyAffected != null) {
                currentlyAffected.remove(dependentElement)
                if (currentlyAffected.isEmpty()) {
                    _affectsGraph.remove(parentElement)
                }
            }
        }
    }
    
    /**
     * Get all elements that directly depend on a specific element.
     * This is the reverse of getAffectingElements.
     */
    suspend fun getDirectDependers(elementId: String): Set<String> {
        return lock.withLock {
            (_dependentOnGraph[elementId] ?: emptySet<String>()).toSet()
        }
    }
    
    /**
     * Clear all dependencies for an element.
     */
    suspend fun clearDependenciesFor(elementId: String) {
        lock.withLock {
            // Remove this element from all parent dependencies
            val elementsThatDepend = _dependentOnGraph[elementId]
            elementsThatDepend?.forEach { dependent ->
                val currentDeps = _dependentOnGraph[dependent]
                currentDeps?.remove(elementId)
                if (currentDeps?.isEmpty() == true) {
                    _dependentOnGraph.remove(dependent)
                }
            }
            
            // Remove this element from all affects relationships
            val elementsAffected = _affectsGraph[elementId]
            elementsAffected?.forEach { affected ->
                val currentlyAffected = _affectsGraph[affected]
                currentlyAffected?.remove(elementId)
                if (currentlyAffected?.isEmpty() == true) {
                    _affectsGraph.remove(affected)
                }
            }
        }
    }
    
    /**
     * Get the total number of elements in the graph.
     */
    suspend fun size(): Int {
        return lock.withLock {
            _affectsGraph.size.coerceAtLeast(_dependentOnGraph.size)
        }
    }
}