// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

inline fun unsupported() {

    <!NOT_YET_SUPPORTED_IN_INLINE!>class A{}<!>

    <!NOT_YET_SUPPORTED_IN_INLINE!>object B{}<!>

    val s = <!NOT_YET_SUPPORTED_IN_INLINE!>object {
        fun a() {}
    }<!>

    <!NOT_YET_SUPPORTED_IN_INLINE!>fun local() {}<!>
}

inline fun unsupportedDefault(<!NOT_YET_SUPPORTED_IN_INLINE!>s : Int = 10<!>) {

}