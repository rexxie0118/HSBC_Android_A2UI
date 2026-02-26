package com.a2ui.renderer.form.dependency

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * NEW: Dependency Graph implementation that tracks relationships between form elements.
 * Enables centralized dependency management with transitive relationship tracking.
 * Provides visualization capabilities through dependency matrix access.
 */
class DependencyGraph {
    private val lock = Mutex()  // Thread safety for concurrent accesses
    
    // NEW: Store direct dependencies: elementId -> set of elements it depends ON
    private var elementDependsOn: Map<String, Set<String>> = emptyMap()
    
    // NEW: Store reverse dependencies: elementId -> set of elements that depend ON it  
    private var elementsThatDependOn: Map<String, Set<String>> = emptyMap()
    
    /**
     * NEW: Records a dependency relationship: elementDepending depends ON elementDependedOn.
     * e.g., "confirm_password" depends ON "password" field
     */
    suspend fun recordDependency(elementDepending: String, elementDependedOn: String) {
        lock.withLock {
            // Update forward dependencies: elementDepending -> dependsOn elements
            val currentDependencies = elementDependsOn[elementDepending]?.toMutableSet() ?: mutableSetOf()
            currentDependencies.add(elementDependedOn)
            elementDependsOn = elementDependsOn + (elementDepending to currentDependencies.toSet())
            
            // Update reverse dependencies: elementDependedOn -> dependedBy elements  
            val currentlyDepending = elementsThatDependOn[elementDependedOn]?.toMutableSet() ?: mutableSetOf()
            currentlyDepending.add(elementDepending)
            elementsThatDependOn = elementsThatDependOn + (elementDependedOn to currentlyDepending.toSet())
        }
    }
    
    /**
     * NEW: Get the elements that a given element directly depends on.
     */
    suspend fun getElementsDependedOn(elementId: String): Set<String> {
        return lock.withLock {
            elementDependsOn[elementId] ?: emptySet()
        }
    }
    
    /**
     * NEW: Get the elements that directly depend on a given element.
     */
    suspend fun getElementsDependingOn(elementId: String): Set<String> {
        return lock.withLock {
            elementsThatDependOn[elementId] ?: emptySet()
        }
    }
    
    /**
     * NEW: Calculate TRANSITIVELY which elements would need to be reprocessed when an element changes.
     * Uses breadth-first search to find all elements affected by a change.
     */
    suspend fun getTransitivelyAffectedElements(elementId: String): Set<String> {
        return lock.withLock {
            val affected = mutableSetOf<String>()
            val queue = ArrayDeque<String>()
            val visited = mutableSetOf<String>()
            
            // NEW: Start with direct dependents of the changed element
            val directDependents = elementsThatDependOn[elementId] ?: emptySet()
            queue.addAll(directDependents)
            visited.addAll(directDependents)
            affected.addAll(directDependents)
            
            // NEW: Breadth-first traversal to find all transitive dependents
            while (!queue.isEmpty()) {
                val currentElement = queue.removeFirst()
                
                // NEW: Find elements that depend on our current element
                val alsoDependOnCurrent = elementsThatDependOn[currentElement] ?: continue
                val newElements = alsoDependOnCurrent.filter { !visited.contains(it) } 
                
                affected.addAll(newElements)
                queue.addAll(newElements)
                visited.addAll(newElements)
            }
            
            affected
        }
    }
    
    /**
     * NEW: Create a visualization-friendly representation of the dependency graph.
     */
    suspend fun getDependencyMatrixVisual(): DependencyMatrixVisual {
        val edges = mutableListOf<DependencyEdge>()
        
        lock.withLock {
            elementsThatDependOn.forEach { (deponent, dependents) ->
                dependents.forEach { dependent ->
                    edges.add(DependencyEdge(fromElement = deponent, toElement = dependent))
                }
            }
        }
        
        return DependencyMatrixVisual(
            elements = (elementDependsOn.keys + elementsThatDependOn.keys).distinct(),
            dependencies = edges
        )
    }
    
    /**
     * NEW: Get direct dependencies count for statistics.
     */
    suspend fun getDirectDependenciesCount(elementId: String): Int {
        return lock.withLock {
            (elementDependsOn[elementId] ?: emptySet()).size
        }
    }
    
    /**
     * NEW: Get number of direct dependents (those that depend on this element) for statistics.
     */
    suspend fun getDirectDependentsCount(elementId: String): Int {
        return lock.withLock {
            (elementsThatDependOn[elementId] ?: emptySet()).size
        }
    }
    
    /**
     * NEW: Get total transitive dependencies for diagnostics/prediction.
     */
    suspend fun getTransitiveDependenciesCount(elementId: String): Int {
        return getElementsDependedOnTransitively(elementId).size
    }
    
    /**
     * NEW: Get total transitive dependents for diagnostics/prediction.
     */
    suspend fun getTransitiveDependentsCount(elementId: String): Int {
        return getElementsDependingOnTransitively(elementId).size
    }
    
    /**
     * NEW: Calculate transitive dependencies (elements this element DEPENDS ON).
     */
    privatesuspend fun getElementsDependedOnTransitively(elementId: String): Set<String> {
        return lock.withLock {
            val dependencies = mutableSetOf<String>()
            val queue = ArrayDeque<String>()
            val visited = mutableSetOf<String>()
            
            // NEW: Start with direct dependencies
            val directDependencies = elementDependsOn[elementId] ?: emptySet()
            queue.addAll(directDependencies)
            visited.addAll(directDependencies)
            dependencies.addAll(directDependencies)
            
            // NEW: BFS to get all transitive dependencies
            while (!queue.isEmpty()) {
                val currentElement = queue.removeFirst()
                
                val alsoDependedOnByCurrent = elementDependsOn[currentElement] ?: continue
                val unvisitedDepends = alsoDependedOnByCurrent.filter { !visited.contains(it) }
                
                dependencies.addAll(unvisitedDepends)
                queue.addAll(unvisitedDepends)
                visited.addAll(unvisitedDepends)
            }
            
            dependencies
        }
    }
    
    /**
     * NEW: Calculate transitive dependents (elements that depend ON this element).
     */
    private suspend fun getElementsDependingOnTransitively(elementId: String): Set<String> {
        return getTransitivelyAffectedElements(elementId)
    }
    
    /**
     * NEW: Remove a dependency relationship if it exists.
     */
    suspend fun removeDependency(elementDepending: String, elementDependedOn: String) {
        lock.withLock {
            // Update forward dependencies
            val currentDeps = elementDependsOn[elementDepending]?.toMutableSet() ?: mutableSetOf()
            currentDeps.remove(elementDependedOn)
            elementDependsOn = if (currentDeps.isEmpty()) {
                elementDependsOn - elementDepending  // Remove empty set entirely
            } else {
                elementDependsOn + (elementDepending to currentDeps.toSet())
            }
            
            // Update reverse dependencies  
            val currentlyDepend = elementsThatDependOn[elementDependedOn]?.toMutableSet() ?: mutableSetOf()
            currentlyDepend.remove(elementDepending)
            elementsThatDependOn = if (currentlyDepend.isEmpty()) {
                elementsThatDependOn - elementDependedOn  // Remove empty set entirely
            } else {
                elementsThatDependOn + (elementDependedOn to currentlyDepend.toSet())
            }
        }
    }
    
    /**
     * NEW: Clear all dependencies for a specific element.
     */
    suspend fun clearDependenciesFor(elementId: String) {
        lock.withLock {
            // NEW: Remove all outbound dependencies
            elementDependsOn = elementDependsOn - elementId
            
            // NEW: Remove all inbound dependencies (elements that depended on this element)
            elementsThatDependOn = elementsThatDependOn.mapValues { (key, dependents) ->
                dependents.filter { it != elementId }.toSet()
            }.filterValues { it.isNotEmpty() }
        }
    }
}

/**
 * NEW: Visualization-friendly data structure for dependency matrix viewing.
 */
data class DependencyMatrixVisual(
    val elements: List<String>,
    val dependencies: List<DependencyEdge>
)

/**
 * NEW: Representation of a single dependency edge in visualization.
 */
data class DependencyEdge(
    val fromElement: String,  // Element being depended ON
    val toElement: String     // Element DEPENDS ON
)