// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE -NOTHING_TO_INLINE

inline private fun a() {}

inline fun b() {}

inline public fun c() {}

abstract class A {
    inline private fun good1() {}
    inline public final fun good2() {}
    inline protected final fun good3() {}
    inline final fun good4() {}


    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline open protected fun wrong1() {}<!>

    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline open public fun wrong2() {}<!>

    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline open fun wrong3() {}<!>

    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline abstract protected fun wrong4()<!>

    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline abstract public fun wrong5()<!>

    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline abstract fun wrong6()<!>
}


trait B {

    inline private fun good1() {}
    inline public final fun good2() {}
    inline protected final fun good3() {}
    inline final fun good4() {}

    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline fun wrong1() {}<!>

    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline open protected fun wrong2() {}<!>

    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline open public fun wrong3() {}<!>

    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline open fun wrong4() {}<!>

    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline protected fun wrong5()<!>

    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline public fun wrong6()<!>

    <!WRONG_MEMBER_MODALITY_FOR_INLINE!>inline fun wrong7()<!>
}
