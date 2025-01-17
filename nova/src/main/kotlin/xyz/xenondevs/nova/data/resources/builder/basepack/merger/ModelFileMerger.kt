package xyz.xenondevs.nova.data.resources.builder.basepack.merger

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.bukkit.Material
import xyz.xenondevs.nova.data.resources.builder.basepack.BasePacks
import xyz.xenondevs.nova.util.data.GSON
import xyz.xenondevs.nova.util.data.getInt
import xyz.xenondevs.nova.util.data.getString
import xyz.xenondevs.nova.util.data.parseJson
import xyz.xenondevs.nova.util.data.set
import java.io.File
import java.util.*

internal class ModelFileMerger(basePacks: BasePacks) : FileMerger(basePacks, "assets/minecraft/models") {
    
    override fun merge(source: File, destination: File) {
        if (destination.exists()) {
            val sourceObj = source.parseJson() as? JsonObject ?: return
            val sourceOverrides = sourceObj.get("overrides") as? JsonArray ?: return
            
            val destObj = destination.parseJson() as? JsonObject
            val destOverrides = destObj?.get("overrides") as? JsonArray
            
            if (destOverrides != null) {
                destOverrides.addAll(sourceOverrides)
                val overrides = processOverrides(destination, destOverrides)
                destObj["overrides"] = overrides
                destination.writeText(GSON.toJson(destObj))
                
                return
            }
        }
        
        source.copyTo(destination)
        processOverrides(destination)
    }
    
    private fun processOverrides(file: File) {
        val obj = file.parseJson() as? JsonObject ?: return
        val array = obj.get("overrides") as? JsonArray ?: return
        processOverrides(file, array)
    }
    
    private fun processOverrides(file: File, array: JsonArray): JsonArray {
        val matName = file.nameWithoutExtension.uppercase()
        val material = Material.values().firstOrNull { it.name == matName } ?: return array
        
        val overrides = TreeMap<Int, String>()
        val occupiedModelData = basePacks.occupiedModelData.getOrPut(material, ::HashSet)
        
        array.forEach { element ->
            val (customModelData, model) = getModelConfig(element) ?: return@forEach
            
            overrides[customModelData] = model
            occupiedModelData += customModelData
        }
        
        val sortedArray = JsonArray()
        overrides.forEach { (customModelData, model) ->
            sortedArray.add(createModelDataEntry(customModelData, model))
        }
        
        return sortedArray
    }
    
    companion object {
        
        fun sortOverrides(array: JsonArray): JsonArray {
            val overrides = TreeMap<Int, String>()
            array.forEach {
                val (customModelData, model) = getModelConfig(it) ?: return@forEach
                overrides[customModelData] = model
            }
            
            val sortedArray = JsonArray()
            overrides.forEach { (customModelData, model) ->
                sortedArray.add(createModelDataEntry(customModelData, model))
            }
            
            return sortedArray
        }
        
        fun getModelConfig(element: JsonElement): Pair<Int, String>? {
            val obj = element as? JsonObject ?: return null
            val predicate = obj.get("predicate") as? JsonObject ?: return null
            val customModelData = predicate.getInt("custom_model_data") ?: return null
            val model = obj.getString("model") ?: return null
            
            return customModelData to model
        }
        
        fun createModelDataEntry(customModelData: Int, path: String): JsonObject {
            val entry = JsonObject()
            val predicate = JsonObject().also { entry.add("predicate", it) }
            predicate.addProperty("custom_model_data", customModelData)
            entry.addProperty("model", path)
            return entry
        }
        
    }
    
}