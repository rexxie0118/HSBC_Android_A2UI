package com.a2ui.renderer.model

import com.google.gson.annotations.SerializedName

sealed class A2UIMessage {
    abstract val type: String
}

data class BeginRenderingMessage(
    override val type: String = "beginRendering",
    val surfaceId: String,
    val root: String,
    val catalogId: String? = null,
    val styles: Map<String, String>? = null
) : A2UIMessage()

data class SurfaceUpdateMessage(
    override val type: String = "surfaceUpdate",
    val surfaceId: String,
    val components: List<Component>
) : A2UIMessage()

data class DataModelUpdateMessage(
    override val type: String = "dataModelUpdate",
    val surfaceId: String,
    val path: String,
    val contents: List<DataModelEntry>
) : A2UIMessage()

data class DataModelEntry(
    val key: String,
    val valueString: String? = null,
    val valueNumber: Double? = null,
    val valueBoolean: Boolean? = null,
    val valueObject: Any? = null
)

data class DeleteSurfaceMessage(
    override val type: String = "deleteSurface",
    val surfaceId: String
) : A2UIMessage()

data class UserAction(
    val event: String,
    val context: Map<String, Any>? = null
)

data class BoundValue(
    @SerializedName("path") val path: String? = null,
    @SerializedName("literalString") val literalString: String? = null,
    @SerializedName("literalNumber") val literalNumber: Double? = null,
    @SerializedName("literalBoolean") val literalBoolean: Boolean? = null
) {
    fun resolve(dataModel: Map<String, Any?>): Any? {
        return if (path != null) {
            dataModel[path] ?: literalString ?: literalNumber ?: literalBoolean
        } else {
            literalString ?: literalNumber ?: literalBoolean
        }
    }
}
