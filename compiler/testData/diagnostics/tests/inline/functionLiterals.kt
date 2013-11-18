// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE
fun Function1<Int, Unit>.noInlineExt(p: Int) {}

inline fun Function1<Int, Unit>.inlineExt2(p: Int) {}

inline fun Function1<Int, Unit>.inlineExt() {
    {(i: Int)-> }.inlineExt2(1);
    {(i: Int)-> } inlineExt2 1

    {(i: Int)-> }.noInlineExt(1);
    {(i: Int)-> } noInlineExt 1
}

inline fun inlineFun2(s: (p: Int) -> Unit) {
    inlineFun{(i: Int)-> }
    noinlineFun{(i: Int)-> }
}

inline fun inlineFun(s: (p: Int) -> Unit) {

}

fun noinlineFun(s: (p: Int) -> Unit) {

}
