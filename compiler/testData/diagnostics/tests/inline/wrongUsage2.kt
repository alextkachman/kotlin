// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

public inline fun assertNot(message: String, block: ()-> Boolean) {}

public inline fun assertNot(block: ()-> Boolean) : Unit = assertNot(<!USAGE_IS_NOT_INLINABLE!>block<!>.toString(), block)



public fun <T> callable(action: ()-> T) {

}

public inline fun <T> String.submit(action: ()->T) {
    callable(action)
}