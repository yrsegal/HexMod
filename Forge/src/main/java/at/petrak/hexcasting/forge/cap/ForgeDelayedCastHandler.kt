package at.petrak.hexcasting.forge.cap

import at.petrak.hexcasting.api.player.DelayedCast
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.putList
import at.petrak.hexcasting.forge.xplat.ForgeXplatImpl
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

fun tickDownDelayedCasts(entity: LivingEntity) {
    if (entity !is ServerPlayer) return
    val casts = entity.persistentData.getList(ForgeXplatImpl.TAG_DELAYED_CASTS, Tag.TAG_COMPOUND.toInt())
    val newCasts = ListTag()
    entity.persistentData.putList(ForgeXplatImpl.TAG_DELAYED_CASTS, newCasts)
    for (cast in casts) {
        val delay = cast.asCompound.getInt(DelayedCast.TAG_TIME_LEFT)
        if (delay <= 1) {
            val delayed = DelayedCast.fromNBT(cast.asCompound, entity, null)
            delayed.harness.executeSpell(delayed.continuation, entity.getLevel())
        } else {
            cast.asCompound.putInt(DelayedCast.TAG_TIME_LEFT, delay - 1)
            newCasts.add(cast)
        }
    }
}
