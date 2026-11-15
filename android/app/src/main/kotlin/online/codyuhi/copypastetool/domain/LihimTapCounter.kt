package online.codyuhi.copypastetool.domain

/** Detects [taps] taps on the title within [windowMs], the gesture that toggles lihim. */
class LihimTapCounter(
    private val taps: Int = 5,
    private val windowMs: Long = 2_500,
) {
    private val times = ArrayDeque<Long>()

    /** Records a tap at [nowMs]; returns true (and resets) when the gesture completes. */
    fun tap(nowMs: Long): Boolean {
        times.addLast(nowMs)
        while (times.isNotEmpty() && nowMs - times.first() >= windowMs) times.removeFirst()
        if (times.size >= taps) {
            times.clear()
            return true
        }
        return false
    }
}
