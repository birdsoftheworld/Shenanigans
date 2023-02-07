package shenanigans.engine.util

import kotlin.math.abs
import kotlin.math.sign

fun moveTowards(current: Float, target: Float, maxDelta: Float): Float {
    if (abs(target - current) <= maxDelta)
        return target
    return current + sign(target - current) * maxDelta
}