package xyz.xenondevs.nova.addon.assets

import com.google.gson.JsonParser
import java.io.File

internal class AssetPack(val directory: File, val namespace: String) {
    
    val materialsFile = File(directory, "materials.json")
    val guisFile = File(directory, "guis.json")
    
    // Sub-folders
    val modelsDir = File(directory, "models").takeIf(File::exists)
    val texturesDir = File(directory, "textures").takeIf(File::exists)
    val fontsDir = File(directory, "fonts").takeIf(File::exists)
    val soundsDir = File(directory, "sounds").takeIf(File::exists)
    val langDir = File(directory, "lang").takeIf(File::exists)
    val guisDir = File(directory, "guis").takeIf(File::exists)
    
    val materialsIndex: List<RegisteredMaterial>? = if (materialsFile.exists())
        MaterialsIndexDeserializer.deserialize(namespace, JsonParser.parseReader(materialsFile.reader()))
    else null
    
    val guisIndex: Map<String, String>? = if (guisFile.exists())
        GUIsIndexDeserializer.deserialize(namespace, JsonParser.parseReader(guisFile.reader()))
    else null
    
}