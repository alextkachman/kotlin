// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

<!NOTHING_TO_INLINE!>inline fun test() {

}<!>

inline fun test2(s : (Int) -> Int) {

}

<!NOTHING_TO_INLINE!>inline fun test3(noinline s : (Int) -> Int) {

}<!>

<!NOTHING_TO_INLINE!>inline fun test4(noinline s : Int.() -> Int) {

}<!>