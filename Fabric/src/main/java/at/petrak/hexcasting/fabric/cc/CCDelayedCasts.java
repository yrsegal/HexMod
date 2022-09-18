package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.player.DelayedCast;
import com.google.common.collect.Lists;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class CCDelayedCasts implements Component {
    public static final String TAG_CASTS = "casts";

    private final ServerPlayer owner;
    private List<DelayedCast> delayedCasts = Lists.newArrayList();

    public CCDelayedCasts(ServerPlayer owner) {
        this.owner = owner;
    }

    public List<DelayedCast> getCasts() {
        return delayedCasts;
    }

    public static void tickAllPlayers(ServerLevel world) {
        for (var player : world.players()) {
            var cc = HexCardinalComponents.DELAYED_CAST.get(player);
            cc.tickDown();
        }
    }

    public void tickDown() {
        List<DelayedCast> casts = delayedCasts;
        delayedCasts = Lists.newArrayList();
        for (var cast : casts) {
            if (cast.delay() <= 1) {
                cast.harness().executeSpell(cast.continuation(), owner.getLevel());
            } else {
                delayedCasts.add(new DelayedCast(cast.harness(), cast.continuation(), cast.delay() - 1));
            }
        }
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        List<DelayedCast> delayed = Lists.newArrayList();
        var casts = tag.getList(TAG_CASTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < casts.size(); i++) {
            var cast = casts.getCompound(i);
            delayed.add(DelayedCast.fromNBT(cast, owner, null));
        }
        delayedCasts = delayed;
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        var casts = new ListTag();
        for (var cast : delayedCasts) {
            casts.add(cast.serializeToNBT());
        }
        tag.put(TAG_CASTS, casts);
    }
}
