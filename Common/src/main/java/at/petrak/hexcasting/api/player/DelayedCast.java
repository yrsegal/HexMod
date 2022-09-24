package at.petrak.hexcasting.api.player;

import at.petrak.hexcasting.api.spell.casting.CastingContext;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import at.petrak.hexcasting.api.spell.casting.SpellCircleContext;
import at.petrak.hexcasting.api.spell.casting.SpellContinuation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

public record DelayedCast(CastingHarness harness, SpellContinuation continuation, int delay) {
	public static final String
		TAG_TIME_LEFT = "time_left",
		TAG_HARNESS = "harness",
		TAG_CONTINUATION = "continuation",
		TAG_MAIN_HAND = "hand",
		TAG_DEPTH = "depth",
		TAG_CONTEXT_POS = "ctx_pos";

	public static DelayedCast fromNBT(CompoundTag tag, ServerPlayer owner, @Nullable SpellCircleContext context) {
		var hand = tag.getBoolean(TAG_MAIN_HAND) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		var depth = tag.getInt(TAG_DEPTH);
		var contextPos = tag.contains(TAG_CONTEXT_POS, Tag.TAG_COMPOUND) ?
			NbtUtils.readBlockPos(tag.getCompound(TAG_CONTEXT_POS)) : null;
		var ctx = new CastingContext(owner, hand, context);
		ctx.setDepth(depth);
		ctx.setMishapContextPos(contextPos);

		return new DelayedCast(
			CastingHarness.fromNBT(tag.getCompound(TAG_HARNESS), ctx),
			SpellContinuation.fromNBT(tag.getCompound(TAG_CONTINUATION), owner.getLevel()),
			tag.getInt(TAG_TIME_LEFT)
		);
	}

	public CompoundTag serializeToNBT() {
		var tag = new CompoundTag();
		var ctx = harness.getCtx();
		tag.putBoolean(TAG_MAIN_HAND, ctx.getCastingHand() == InteractionHand.MAIN_HAND);
		tag.putInt(TAG_DEPTH, ctx.getDepth());
		tag.put(TAG_HARNESS, harness.serializeToNBT());
		tag.put(TAG_CONTINUATION, continuation.serializeToNBT());
		tag.putInt(TAG_TIME_LEFT, delay);
		if (ctx.getMishapContextPos() != null)
			tag.put(TAG_CONTEXT_POS, NbtUtils.writeBlockPos(ctx.getMishapContextPos()));
		return tag;
	}
}
