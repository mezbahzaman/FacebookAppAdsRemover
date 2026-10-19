package tn.loukious.facebookappadsremover.hooks

/** Stable serializer for cached guard roles; persisted roles include the colon. */
internal data class FeedGuardCacheEntry(
    val role: String,
    val className: String,
    /**
     * Optional late-feed shape hint ("STORAGE" / "VENDING" / "LIFECYCLE").
     *
     * A late-feed class can carry methods matching more than one shape; on a
     * cache hit the shape is what lets [FeedGuardHook] re-resolve exactly the
     * method the full DexKit discovery hooked, instead of refusing the
     * ambiguous class and forcing a full rescan on every launch.
     */
    val lateShape: String? = null,
) {
    companion object {
        /** Separates the class name from the shape hint; class names never contain it. */
        const val SHAPE_SEPARATOR = '|'

        fun parse(value: String): FeedGuardCacheEntry? {
            val separator = value.indexOf(':')
            if (separator <= 0 || separator == value.lastIndex) return null
            var className = value.substring(separator + 1)
            var shape: String? = null
            val shapeAt = className.indexOf(SHAPE_SEPARATOR)
            if (shapeAt > 0 && shapeAt < className.lastIndex) {
                shape = className.substring(shapeAt + 1)
                className = className.substring(0, shapeAt)
            }
            return FeedGuardCacheEntry(
                role = value.substring(0, separator + 1),
                className = className,
                lateShape = shape,
            )
        }

        fun format(role: String, className: String, lateShape: String? = null): String =
            if (lateShape == null) "$role$className" else "$role$className$SHAPE_SEPARATOR$lateShape"
    }
}
