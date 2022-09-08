package at.petrak.hexcasting.api.player;

import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import at.petrak.hexcasting.api.spell.casting.SpellContinuation;

public record DelayedCast(CastingHarness harness, SpellContinuation continuation, int delay) {
}
