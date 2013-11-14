class Foo(val prop1 : Object, val prop2 : Object){
    fun f(p1: Foo, p2: Foo) {
        if (p1.prop1 is String && p2.prop2 is String && prop2 is String){
            var a : String = p1.<caret>
        }
    }
}

// EXIST: prop1
// ABSENT: prop2
