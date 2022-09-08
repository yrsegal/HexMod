package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.player.DelayedCast;
import at.petrak.hexcasting.api.spell.casting.CastingContext;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import at.petrak.hexcasting.api.spell.casting.SpellCircleContext;
import at.petrak.hexcasting.api.spell.casting.SpellContinuation;
import com.google.common.collect.Lists;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.List;

public class CCDelayedCasts implements Component {
    public static final String
        TAG_CASTS = "casts",
        TAG_TIME_LEFT = "time_left",
        TAG_HARNESS = "harness",
        TAG_CONTINUATION = "continuation",
        TAG_MAIN_HAND = "hand",
        TAG_CIRCLE_CTX = "circle";

    private final ServerPlayer owner;
    private List<DelayedCast> delayedCasts = Lists.newArrayList();

    public CCDelayedCasts(ServerPlayer owner) {
        this.owner = owner;
    }

    public List<DelayedCast> getCasts() {
        return delayedCasts;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        List<DelayedCast> delayed = Lists.newArrayList();
        var casts = tag.getList(TAG_CASTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < casts.size(); i++) {
            var cast = casts.getCompound(i);
            var hand = cast.getBoolean(TAG_MAIN_HAND) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            var spellCircleCtx = cast.contains(TAG_CIRCLE_CTX, Tag.TAG_COMPOUND) ? SpellCircleContext.fromNBT(cast.getCompound(TAG_CIRCLE_CTX)) : null;
            var ctx = new CastingContext(owner, hand, spellCircleCtx);
            delayed.add(new DelayedCast(
                CastingHarness.fromNBT(cast.getCompound(TAG_HARNESS), ctx),
                SpellContinuation.fromNBT(cast.getCompound(TAG_CONTINUATION), owner.getLevel()),
                cast.getInt(TAG_TIME_LEFT)
            ));
        }
        delayedCasts = delayed;
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        var casts = new ListTag();
        for (var cast : delayedCasts) {
            var castTag = new CompoundTag();
            var ctx = cast.harness().getCtx();
            var circle = ctx.getSpellCircle();
            if (circle != null)
                castTag.put(TAG_CIRCLE_CTX, circle.serializeToNBT());
            castTag.putBoolean(TAG_MAIN_HAND, ctx.getCastingHand() == InteractionHand.MAIN_HAND);
            castTag.put(TAG_HARNESS, cast.harness().serializeToNBT());
            castTag.put(TAG_CONTINUATION, cast.continuation().serializeToNBT());
            castTag.putInt(TAG_TIME_LEFT, cast.delay());
            casts.add(castTag);
        }
        tag.put(TAG_CASTS, casts);
    }
}
