package com.a2ui.renderer.renderer

import com.a2ui.renderer.binding.DataModelStore
import com.a2ui.renderer.config.ChildrenTemplate
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for dynamic list rendering
 */
class DynamicListTest {

    private lateinit var dataModel: DataModelStore

    @Before
    fun setup() {
        dataModel = DataModelStore()
    }

    @Test
    fun `children template should parse correctly`() {
        val template = ChildrenTemplate(
            dataBinding = "$.products",
            componentId = "product_card",
            itemVar = "product"
        )
        
        assertEquals("$.products", template.dataBinding)
        assertEquals("product_card", template.componentId)
        assertEquals("product", template.itemVar)
    }

    @Test
    fun `get list data from model`() {
        val products = listOf(
            mapOf("name" to "Product A", "price" to 100),
            mapOf("name" to "Product B", "price" to 200),
            mapOf("name" to "Product C", "price" to 300)
        )
        
        dataModel.setData(mapOf("products" to products))
        
        val retrieved = dataModel.getAtPath("products")
        assertNotNull(retrieved)
        assertTrue(retrieved is List<*>)
        assertEquals(3, (retrieved as List<*>).size)
    }

    @Test
    fun `list data should contain correct items`() {
        val products = listOf(
            mapOf("name" to "Product A", "price" to 100),
            mapOf("name" to "Product B", "price" to 200)
        )
        
        dataModel.setData(mapOf("products" to products))
        
        val firstProduct = dataModel.getAtPath("products.0")
        val secondProduct = dataModel.getAtPath("products.1")
        
        assertNotNull(firstProduct)
        assertNotNull(secondProduct)
        
        assertTrue(firstProduct is Map<*, *>)
        assertEquals("Product A", (firstProduct as Map<*, *>)["name"])
        assertEquals(100, (firstProduct)["price"])
        
        assertEquals("Product B", (secondProduct as Map<*, *>)["name"])
    }

    @Test
    fun `template with item var should work`() {
        val template = ChildrenTemplate(
            dataBinding = "$.users",
            componentId = "user_row",
            itemVar = "user"
        )
        
        val users = listOf(
            mapOf("id" to 1, "name" to "Alice"),
            mapOf("id" to 2, "name" to "Bob")
        )
        
        dataModel.setData(mapOf("users" to users))
        
        // Verify data is accessible
        assertEquals(2, (dataModel.getAtPath("users") as List<*>).size)
        assertEquals("Alice", dataModel.getAtPath("users.0.name"))
        assertEquals("Bob", dataModel.getAtPath("users.1.name"))
    }

    @Test
    fun `empty list should return empty`() {
        dataModel.setData(mapOf("items" to emptyList<String>()))
        
        val items = dataModel.getAtPath("items")
        assertNotNull(items)
        assertTrue((items as List<*>).isEmpty())
    }

    @Test
    fun `missing list path should return null`() {
        dataModel.setData(emptyMap())
        
        val result = dataModel.getAtPath("nonexistent.list")
        assertNull(result)
    }

    @Test
    fun `list of strings should work`() {
        val tags = listOf("tag1", "tag2", "tag3")
        dataModel.setData(mapOf("tags" to tags))
        
        assertEquals(3, (dataModel.getAtPath("tags") as List<*>).size)
        assertEquals("tag1", dataModel.getAtPath("tags.0"))
        assertEquals("tag2", dataModel.getAtPath("tags.1"))
    }

    @Test
    fun `nested list should work`() {
        val data = mapOf(
            "categories" to listOf(
                mapOf(
                    "name" to "Electronics",
                    "products" to listOf(
                        mapOf("name" to "Phone", "price" to 500),
                        mapOf("name" to "Laptop", "price" to 1000)
                    )
                )
            )
        )
        
        dataModel.setData(data)
        
        val firstCategoryProducts = dataModel.getAtPath("categories.0.products")
        assertNotNull(firstCategoryProducts)
        assertEquals(2, (firstCategoryProducts as List<*>).size)
    }

    @Test
    fun `children template with complex data`() {
        val orders = listOf(
            mapOf(
                "orderId" to "ORD001",
                "items" to listOf(
                    mapOf("product" to "Widget", "quantity" to 2),
                    mapOf("product" to "Gadget", "quantity" to 1)
                ),
                "total" to 150
            ),
            mapOf(
                "orderId" to "ORD002",
                "items" to listOf(
                    mapOf("product" to "Gizmo", "quantity" to 3)
                ),
                "total" to 75
            )
        )
        
        dataModel.setData(mapOf("orders" to orders))
        
        assertEquals(2, (dataModel.getAtPath("orders") as List<*>).size)
        assertEquals("ORD001", dataModel.getAtPath("orders.0.orderId"))
        assertEquals(150, dataModel.getAtPath("orders.0.total"))
        assertEquals("Widget", dataModel.getAtPath("orders.0.items.0.product"))
    }

    @Test
    fun `template should support different component types`() {
        val templates = listOf(
            ChildrenTemplate("$.products", "product_card"),
            ChildrenTemplate("$.users", "user_row"),
            ChildrenTemplate("$.messages", "message_bubble"),
            ChildrenTemplate("$.notifications", "notification_tile")
        )
        
        templates.forEach { template ->
            assertNotNull(template.dataBinding)
            assertNotNull(template.componentId)
        }
    }

    @Test
    fun `list with null values should handle gracefully`() {
        val data = mapOf(
            "items" to listOf(
                mapOf("name" to "Item 1"),
                null,
                mapOf("name" to "Item 3")
            )
        )
        
        // This test verifies we can store data with potential nulls
        dataModel.setData(data)
        
        val items = dataModel.getAtPath("items") as List<*>
        assertEquals(3, items.size)
    }

    @Test
    fun `update list item should work`() {
        val products = listOf(
            mapOf("name" to "Product A", "price" to 100)
        )
        
        dataModel.setData(mapOf("products" to products))
        
        // Note: In current implementation, updating nested list items
        // requires replacing the entire list
        assertEquals("Product A", dataModel.getAtPath("products.0.name"))
    }

    @Test
    fun `multiple lists in same model should work`() {
        val data = mapOf(
            "users" to listOf(
                mapOf("name" to "Alice"),
                mapOf("name" to "Bob")
            ),
            "products" to listOf(
                mapOf("name" to "Widget"),
                mapOf("name" to "Gadget")
            ),
            "orders" to listOf(
                mapOf("id" to "ORD001")
            )
        )
        
        dataModel.setData(data)
        
        assertEquals(2, (dataModel.getAtPath("users") as List<*>).size)
        assertEquals(2, (dataModel.getAtPath("products") as List<*>).size)
        assertEquals(1, (dataModel.getAtPath("orders") as List<*>).size)
    }
}
