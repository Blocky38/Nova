package xyz.xenondevs.nova.world.block.limits

import xyz.xenondevs.nova.LOGGER
import xyz.xenondevs.nova.data.config.DEFAULT_CONFIG
import xyz.xenondevs.nova.world.block.context.BlockPlaceContext
import java.util.logging.Level

object TileEntityLimits {
    
    private val limiters: List<BlockLimiter>
    
    init {
        val limiters = ArrayList<BlockLimiter>()
        
        try {
            val blockLimits = DEFAULT_CONFIG.getConfigurationSection("performance.tile_entity_limits")
            blockLimits?.getKeys(false)?.forEach { type ->
                val section = blockLimits.getConfigurationSection(type) ?: return@forEach
                val limiter = BlockLimiter.createNew(type, section) ?: return@forEach
                limiters += limiter
            }
        } catch (e: Exception) {
            LOGGER.log(Level.SEVERE, "An exception occurred trying to load block limits (invalid configuration?)", e)
        }
        
        this.limiters = limiters
    }
    
    
    fun canPlace(ctx: BlockPlaceContext): PlaceResult {
        limiters.forEach {
            if (!it.canPlace(ctx)) {
                return PlaceResult(false, it.denyMessage)
            }
        }
        
        return PlaceResult(true, "")
    }
    
    data class PlaceResult(val allowed: Boolean, val message: String)
    
}
