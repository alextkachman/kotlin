// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

inline fun inlineFunWrongUsage(s: (p: Int) -> Unit) {
    <!USAGE_IS_NOT_INLINABLE!>s<!>

    if (true) <!USAGE_IS_NOT_INLINABLE!>s<!> else 0

    val c = <!USAGE_IS_NOT_INLINABLE!>s<!>
}

inline fun inlineFunWrongUsageInClosure(s: (p: Int) -> Unit) {
    {
        <!USAGE_IS_NOT_INLINABLE!>s<!>

        if (true) <!USAGE_IS_NOT_INLINABLE!>s<!> else 0

        val c = <!USAGE_IS_NOT_INLINABLE!>s<!>
    }()
}

inline fun inlineFunNoInline(noinline s: (p: Int) -> Unit) {
    s
    if (true) s else 0
    val c = s
}

