// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

inline fun inlineFunWithInvoke(s: (p: Int) -> Unit) {
    subInline(s)
    subNoInline(<!USAGE_IS_NOT_INLINABLE!>s<!>)
}

inline fun inlineFunWithInvokeClosure(s: (p: Int) -> Unit) {
    subInline({(p: Int) -> s(p)})
    subNoInline({(p: Int) -> s(p)})
}

inline fun inlineFunWithInvokeNonInline(noinline s: (p: Int) -> Unit) {
    subInline(s)
    subNoInline(s)
}

inline fun Function1<Int, Unit>.inlineExt(s: (p: Int) -> Unit) {
    subInline(this)
    subNoInline(<!USAGE_IS_NOT_INLINABLE!>this<!>)

    subInline({(p: Int) -> this(p)})
    subNoInline({(p: Int) -> this(p)})

    subInline(s)
    subNoInline(<!USAGE_IS_NOT_INLINABLE!>s<!>)

    subInline({(p: Int) -> s(p)})
    subNoInline({(p: Int) -> s(p)})
}

inline fun subInline(s: (p: Int) -> Unit) {}

fun subNoInline(s: (p: Int) -> Unit) {}


