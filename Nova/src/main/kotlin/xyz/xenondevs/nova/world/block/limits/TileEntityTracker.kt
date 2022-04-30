package xyz.xenondevs.nova.world.block.limits

import xyz.xenondevs.nova.NOVA
import xyz.xenondevs.nova.data.NamespacedId
import xyz.xenondevs.nova.data.config.PermanentStorage
import xyz.xenondevs.nova.material.TileEntityNovaMaterial
import xyz.xenondevs.nova.tileentity.TileEntity
import xyz.xenondevs.nova.util.runTaskTimer
import xyz.xenondevs.nova.world.block.context.BlockBreakContext
import xyz.xenondevs.nova.world.block.context.BlockPlaceContext
import java.lang.Integer.max
import java.util.*

object TileEntityTracker {
    
    private val BLOCK_COUNTER: HashMap<UUID, HashMap<NamespacedId, Int>> =
        PermanentStorage.retrieve("block_counter", ::HashMap)
    private val BLOCK_WORLD_COUNTER: HashMap<UUID, HashMap<UUID, HashMap<NamespacedId, Int>>> =
        PermanentStorage.retrieve("block_world_counter", ::HashMap)
    
    init {
        runTaskTimer(20 * 60, 20 * 60, ::saveCounters)
        NOVA.disableHandlers += ::saveCounters
    }
    
    private fun saveCounters() {
        PermanentStorage.store("block_counter", BLOCK_COUNTER)
        PermanentStorage.store("block_world_counter", BLOCK_WORLD_COUNTER)
    }
    
    internal fun handleBlockPlace(material: TileEntityNovaMaterial, ctx: BlockPlaceContext) {
        modifyCounters(ctx.ownerUUID, ctx.pos.world.uid, material.id, 1)
    }
    
    internal fun handleBlockBreak(tileEntity: TileEntity, ctx: BlockBreakContext) {
        modifyCounters(tileEntity.ownerUUID, ctx.pos.world.uid, tileEntity.material.id, -1)
    }
    
    private fun modifyCounters(player: UUID, world: UUID, id: NamespacedId, add: Int) {
        val playerMap = BLOCK_COUNTER.getOrPut(player, ::HashMap)
        playerMap[id] = max(0, (playerMap[id] ?: 0) + add)
        
        val playerWorldMap = BLOCK_WORLD_COUNTER.getOrPut(player, ::HashMap).getOrPut(world, ::HashMap)
        playerWorldMap[id] = max(0, (playerWorldMap[id] ?: 0) + add)
    }
    
    fun getBlocksPlacedAmount(player: UUID, blockId: NamespacedId): Int {
        return BLOCK_COUNTER[player]?.get(blockId) ?: 0
    }
    
    fun getBlocksPlacedAmount(player: UUID, world: UUID, blockId: NamespacedId): Int {
        return BLOCK_WORLD_COUNTER[player]?.get(world)?.get(blockId) ?: 0
    }
    
}