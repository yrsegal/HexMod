package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.player.DelayedCast
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.CastingHarness
import at.petrak.hexcasting.api.spell.casting.SpellContinuation
import at.petrak.hexcasting.xplat.IXplatAbstractions
import kotlin.math.ceil
import kotlin.math.max

object OpDelay : Operator {
    override fun operate(continuation: SpellContinuation, stack: MutableList<SpellDatum<*>>, local: SpellDatum<*>, ctx: CastingContext): OperationResult =
        throw NotImplementedError()

    override fun operate(continuation: SpellContinuation, harness: CastingHarness): OperationResult {
        val stack = harness.stack
        val ctx = harness.ctx

        val delay = max(1, stack.getChecked<Double>(stack.lastIndex).toInt())
        stack.removeLast()

        for (i in 0 until ceil(delay / 20.0).toInt())
            ctx.incDepth()

        IXplatAbstractions.INSTANCE.addDelayedCast(ctx.caster, DelayedCast(harness, continuation, delay))
        return OperationResult(SpellContinuation.Done, listOf(), SpellDatum.make(Widget.NULL), listOf())
    }
}
