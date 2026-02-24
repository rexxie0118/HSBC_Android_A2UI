package com.a2ui.renderer.binding

import com.a2ui.renderer.config.TextValue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DataModelStore and BindingResolver
 */
class DataBindingTest {

    private lateinit var dataModel: DataModelStore

    @Before
    fun setup() {
        dataModel = DataModelStore()
    }

    @Test
    fun `empty data model should return empty map`() {
        assertTrue(dataModel.getData().isEmpty())
    }

    @Test
    fun `setData should update entire model`() {
        val testData = mapOf(
            "user" to mapOf("name" to "John", "age" to 30),
            "products" to listOf("A", "B", "C")
        )
        
        dataModel.setData(testData)
        
        assertEquals(testData, dataModel.getData())
    }

    @Test
    fun `updateAtPath should set nested value`() {
        dataModel.updateAtPath("user.name", "Alice")
        
        assertEquals("Alice", dataModel.getAtPath("user.name"))
    }

    @Test
    fun `updateAtPath should create intermediate maps`() {
        dataModel.updateAtPath("a.b.c.d", "deep value")
        
        assertEquals("deep value", dataModel.getAtPath("a.b.c.d"))
    }

    @Test
    fun `getAtPath should return null for non-existent path`() {
        val result = dataModel.getAtPath("nonexistent.path")
        
        assertNull(result)
    }

    @Test
    fun `getAtPath should resolve nested objects`() {
        val testData = mapOf(
            "user" to mapOf(
                "name" to "Bob",
                "address" to mapOf(
                    "city" to "NYC",
                    "zip" to "10001"
                )
            )
        )
        dataModel.setData(testData)
        
        assertEquals("Bob", dataModel.getAtPath("user.name"))
        assertEquals("NYC", dataModel.getAtPath("user.address.city"))
        assertEquals("10001", dataModel.getAtPath("user.address.zip"))
    }

    @Test
    fun `updateAtPath should update existing nested value`() {
        dataModel.setData(mapOf("user" to mapOf("name" to "Original")))
        
        dataModel.updateAtPath("user.name", "Updated")
        
        assertEquals("Updated", dataModel.getAtPath("user.name"))
    }

    @Test
    fun `updateAtPath with null should delete value`() {
        dataModel.setData(mapOf("user" to mapOf("name" to "ToDelete", "age" to 25)))
        
        dataModel.updateAtPath("user.name", null)
        
        assertNull(dataModel.getAtPath("user.name"))
        assertEquals(25, dataModel.getAtPath("user.age"))
    }

    @Test
    fun `mergeData should combine models`() {
        dataModel.setData(mapOf("a" to "1", "b" to "2"))
        
        dataModel.mergeData(mapOf("b" to "updated", "c" to "3"))
        
        assertEquals("1", dataModel.getAtPath("a"))
        assertEquals("updated", dataModel.getAtPath("b"))
        assertEquals("3", dataModel.getAtPath("c"))
    }

    @Test
    fun `mergeData should deeply merge nested objects`() {
        dataModel.setData(mapOf(
            "user" to mapOf("name" to "John", "age" to 30)
        ))
        
        dataModel.mergeData(mapOf(
            "user" to mapOf("age" to 31, "email" to "john@example.com")
        ))
        
        assertEquals("John", dataModel.getAtPath("user.name"))
        assertEquals(31, dataModel.getAtPath("user.age"))
        assertEquals("john@example.com", dataModel.getAtPath("user.email"))
    }

    @Test
    fun `clear should empty the model`() {
        dataModel.setData(mapOf("key" to "value"))
        
        dataModel.clear()
        
        assertTrue(dataModel.getData().isEmpty())
    }

    @Test
    fun `BindingResolver should resolve simple path`() {
        dataModel.setData(mapOf("name" to "Test"))
        
        val result = BindingResolver.resolve("$.name", dataModel)
        
        assertEquals("Test", result)
    }

    @Test
    fun `BindingResolver should resolve nested path`() {
        dataModel.setData(mapOf(
            "user" to mapOf("profile" to mapOf("displayName" to "User123"))
        ))
        
        val result = BindingResolver.resolve("$.user.profile.displayName", dataModel)
        
        assertEquals("User123", result)
    }

    @Test
    fun `BindingResolver should return original string if not a binding`() {
        val result = BindingResolver.resolve("not.a.binding", dataModel)
        
        assertEquals("not.a.binding", result)
    }

    @Test
    fun `BindingResolver should handle missing path gracefully`() {
        dataModel.setData(emptyMap())
        
        val result = BindingResolver.resolve("$.missing", dataModel)
        
        assertNull(result)
    }

    @Test
    fun `resolveText should use literal string`() {
        val textValue = TextValue("Hello World")
        
        val result = BindingResolver.resolveText(textValue, dataModel)
        
        assertEquals("Hello World", result)
    }

    @Test
    fun `resolveText should resolve binding`() {
        dataModel.setData(mapOf("greeting" to "Hi there!"))
        val textValue = TextValue("$.greeting")
        
        val result = BindingResolver.resolveText(textValue, dataModel)
        
        assertEquals("Hi there!", result)
    }

    @Test
    fun `resolveText should return empty string for null`() {
        val result = BindingResolver.resolveText(null, dataModel)
        
        assertEquals("", result)
    }

    @Test
    fun `resolveColor should resolve color binding`() {
        dataModel.setData(mapOf("primaryColor" to "#FF0000"))
        
        val result = BindingResolver.resolveColor("$.primaryColor", dataModel)
        
        assertEquals("#FF0000", result)
    }

    @Test
    fun `resolveColor should return literal color`() {
        val result = BindingResolver.resolveColor("#00FF00", dataModel)
        
        assertEquals("#00FF00", result)
    }

    @Test
    fun `hasBinding should detect binding paths`() {
        assertTrue(BindingResolver.hasBinding("$.user.name"))
        assertFalse(BindingResolver.hasBinding("literal value"))
        assertFalse(BindingResolver.hasBinding(null))
    }

    @Test
    fun `resolveAll should resolve multiple paths`() {
        dataModel.setData(mapOf(
            "a" to "1",
            "b" to "2",
            "c" to "3"
        ))
        
        val results = BindingResolver.resolveAll(
            listOf("$.a", "$.b", "$.missing"),
            dataModel
        )
        
        assertEquals("1", results["$.a"])
        assertEquals("2", results["$.b"])
        assertNull(results["$.missing"])
    }

    @Test
    fun `updateWithLiteral should update data model`() {
        dataModel.setData(emptyMap())
        
        BindingResolver.updateWithLiteral("$.user.name", "NewUser", dataModel)
        
        assertEquals("NewUser", dataModel.getAtPath("user.name"))
    }

    @Test
    fun `resolveProperties should update text with binding`() {
        dataModel.setData(mapOf("title" to "Dynamic Title"))
        
        val properties = com.a2ui.renderer.config.ComponentProperties(
            text = TextValue("$.title"),
            color = "#000000"
        )
        
        val resolved = BindingResolver.resolveProperties(properties, dataModel)
        
        assertEquals("Dynamic Title", resolved?.text?.literalString)
    }

    @Test
    fun `updateAtPath should trigger data change`() {
        dataModel.setData(emptyMap())
        
        dataModel.updateAtPath("key", "value")
        
        assertEquals("value", dataModel.getAtPath("key"))
        assertEquals(1, dataModel.getData().size)
    }
}
