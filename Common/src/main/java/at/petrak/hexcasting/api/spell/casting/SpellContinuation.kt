package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.getList
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel

/**
 * A continuation during the execution of a spell.
 */
sealed interface SpellContinuation {

    object Done : SpellContinuation

    data class NotDone(val frame: ContinuationFrame, val next: SpellContinuation) : SpellContinuation

    fun pushFrame(frame: ContinuationFrame): SpellContinuation = NotDone(frame, this)
    fun serializeToNBT(): CompoundTag = NBTBuilder {
        "frames" %= list {
            var currentContinuation: SpellContinuation = this@SpellContinuation
            while (currentContinuation is NotDone) {
                add(currentContinuation.frame.serializeToNBT())

                currentContinuation = currentContinuation.next
            }
        }
    }

    companion object {
        @JvmStatic
        fun fromNBT(tag: CompoundTag, world: ServerLevel): SpellContinuation {
            val frames = tag.getList("frames", Tag.TAG_COMPOUND)
            var currentContinuation: SpellContinuation = Done
            for (frameTag in frames.asReversed()) {
                currentContinuation = currentContinuation.pushFrame(ContinuationFrame.fromNBT(frameTag.asCompound, world))
            }
            return currentContinuation
        }
    }
}
