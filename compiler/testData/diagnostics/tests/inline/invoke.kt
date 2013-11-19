// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

inline fun inlineFunWithInvoke(s: (p: Int) -> Unit) {
    s(11)
    s.invoke(11)
    s invoke 11
}

<!NOTHING_TO_INLINE!>inline fun inlineFunWithInvokeNonInline(noinline s: (p: Int) -> Unit) {
    s(11)
    s.invoke(11)
    s invoke 11
}<!>

inline fun Function1<Int, Unit>.inlineExt() {
    invoke(11)
    this.invoke(11)
    this invoke 11
    this(11)
}
