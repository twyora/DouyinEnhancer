package io.github.twyora.douyinenhancer.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object RequireThen {
    @Suppress("FunctionName")
    inline fun __requireAllNotNullImpl(
        vararg candidates: Any?,
        crossinline message: (List<Int>) -> String = { indices ->
            "Require all not null failed: arguments at indices $indices must not be null."
        }
    ) {
        candidates.mapIndexed { index, value ->
            index.takeIf {
                value == null
            }
        }.filterNotNull().takeIf {
            it.isNotEmpty()
        }?.let {
            throw IllegalArgumentException(message(it))
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A> requireNotNullThen(a: A?, block: (A) -> Unit) {
        contract {
            returns() implies (a != null)
        }

        __requireAllNotNullImpl(a)

        block(a!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B> requireAllNotNullThen(a: A?, b: B?, block: (A, B) -> Unit) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
        }

        __requireAllNotNullImpl(a, b)

        block(a!!, b!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B> requireAllNotNullThen(a: A?, b: B?, crossinline message: (List<Int>) -> String, block: (A, B) -> Unit) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
        }

        __requireAllNotNullImpl(a, b, message = message)

        block(a!!, b!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C> requireAllNotNullThen(a: A?, b: B?, c: C?, block: (A, B, C) -> Unit) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
        }

        __requireAllNotNullImpl(a, b, c)

        block(a!!, b!!, c!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C> requireAllNotNullThen(a: A?, b: B?, c: C?, crossinline message: (List<Int>) -> String, block: (A, B, C) -> Unit) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
        }

        __requireAllNotNullImpl(a, b, c, message = message)

        block(a!!, b!!, c!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D> requireAllNotNullThen(a: A?, b: B?, c: C?, d: D?, block: (A, B, C, D) -> Unit) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
        }

        __requireAllNotNullImpl(a, b, c, d)

        block(a!!, b!!, c!!, d!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D> requireAllNotNullThen(
        a: A?,
        b: B?,
        c: C?,
        d: D?,
        crossinline message: (List<Int>) -> String,
        block: (A, B, C, D) -> Unit
    ) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
        }

        __requireAllNotNullImpl(a, b, c, d, message = message)

        block(a!!, b!!, c!!, d!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D, E> requireAllNotNullThen(a: A?, b: B?, c: C?, d: D?, e: E?, block: (A, B, C, D, E) -> Unit) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
            returns() implies (e != null)
        }

        __requireAllNotNullImpl(a, b, c, d, e)

        block(a!!, b!!, c!!, d!!, e!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D, E> requireAllNotNullThen(
        a: A?,
        b: B?,
        c: C?,
        d: D?,
        e: E?,
        crossinline message: (List<Int>) -> String,
        block: (A, B, C, D, E) -> Unit
    ) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
            returns() implies (e != null)
        }

        __requireAllNotNullImpl(a, b, c, d, e, message = message)

        block(a!!, b!!, c!!, d!!, e!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D, E, F> requireAllNotNullThen(a: A?, b: B?, c: C?, d: D?, e: E?, f: F?, block: (A, B, C, D, E, F) -> Unit) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
            returns() implies (e != null)
            returns() implies (f != null)
        }

        __requireAllNotNullImpl(a, b, c, d, e, f)

        block(a!!, b!!, c!!, d!!, e!!, f!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D, E, F> requireAllNotNullThen(
        a: A?,
        b: B?,
        c: C?,
        d: D?,
        e: E?,
        f: F?,
        crossinline message: (List<Int>) -> String,
        block: (A, B, C, D, E, F) -> Unit
    ) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
            returns() implies (e != null)
            returns() implies (f != null)
        }

        __requireAllNotNullImpl(a, b, c, d, e, f, message = message)

        block(a!!, b!!, c!!, d!!, e!!, f!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D, E, F, G> requireAllNotNullThen(
        a: A?,
        b: B?,
        c: C?,
        d: D?,
        e: E?,
        f: F?,
        g: G?,
        block: (A, B, C, D, E, F, G) -> Unit
    ) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
            returns() implies (e != null)
            returns() implies (f != null)
            returns() implies (g != null)
        }

        __requireAllNotNullImpl(a, b, c, d, e, f, g)

        block(a!!, b!!, c!!, d!!, e!!, f!!, g!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D, E, F, G> requireAllNotNullThen(
        a: A?,
        b: B?,
        c: C?,
        d: D?,
        e: E?,
        f: F?,
        g: G?,
        crossinline message: (List<Int>) -> String,
        block: (A, B, C, D, E, F, G) -> Unit
    ) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
            returns() implies (e != null)
            returns() implies (f != null)
            returns() implies (g != null)
        }

        __requireAllNotNullImpl(a, b, c, d, e, f, g, message = message)

        block(a!!, b!!, c!!, d!!, e!!, f!!, g!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D, E, F, G, H> requireAllNotNullThen(
        a: A?,
        b: B?,
        c: C?,
        d: D?,
        e: E?,
        f: F?,
        g: G?,
        h: H?,
        block: (A, B, C, D, E, F, G, H) -> Unit
    ) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
            returns() implies (e != null)
            returns() implies (f != null)
            returns() implies (g != null)
            returns() implies (h != null)
        }

        __requireAllNotNullImpl(a, b, c, d, e, f, g, h)

        block(a!!, b!!, c!!, d!!, e!!, f!!, g!!, h!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D, E, F, G, H> requireAllNotNullThen(
        a: A?,
        b: B?,
        c: C?,
        d: D?,
        e: E?,
        f: F?,
        g: G?,
        h: H?,
        crossinline message: (List<Int>) -> String,
        block: (A, B, C, D, E, F, G, H) -> Unit
    ) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
            returns() implies (e != null)
            returns() implies (f != null)
            returns() implies (g != null)
            returns() implies (h != null)
        }

        __requireAllNotNullImpl(a, b, c, d, e, f, g, h, message = message)

        block(a!!, b!!, c!!, d!!, e!!, f!!, g!!, h!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D, E, F, G, H, I> requireAllNotNullThen(
        a: A?,
        b: B?,
        c: C?,
        d: D?,
        e: E?,
        f: F?,
        g: G?,
        h: H?,
        i: I?,
        block: (A, B, C, D, E, F, G, H, I) -> Unit
    ) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
            returns() implies (e != null)
            returns() implies (f != null)
            returns() implies (g != null)
            returns() implies (h != null)
            returns() implies (i != null)
        }

        __requireAllNotNullImpl(a, b, c, d, e, f, g, h, i)

        block(a!!, b!!, c!!, d!!, e!!, f!!, g!!, h!!, i!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D, E, F, G, H, I> requireAllNotNullThen(
        a: A?,
        b: B?,
        c: C?,
        d: D?,
        e: E?,
        f: F?,
        g: G?,
        h: H?,
        i: I?,
        crossinline message: (List<Int>) -> String,
        block: (A, B, C, D, E, F, G, H, I) -> Unit
    ) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
            returns() implies (e != null)
            returns() implies (f != null)
            returns() implies (g != null)
            returns() implies (h != null)
            returns() implies (i != null)
        }

        __requireAllNotNullImpl(a, b, c, d, e, f, g, h, i, message = message)

        block(a!!, b!!, c!!, d!!, e!!, f!!, g!!, h!!, i!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D, E, F, G, H, I, J> requireAllNotNullThen(
        a: A?,
        b: B?,
        c: C?,
        d: D?,
        e: E?,
        f: F?,
        g: G?,
        h: H?,
        i: I?,
        j: J?,
        block: (A, B, C, D, E, F, G, H, I, J) -> Unit
    ) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
            returns() implies (e != null)
            returns() implies (f != null)
            returns() implies (g != null)
            returns() implies (h != null)
            returns() implies (i != null)
            returns() implies (j != null)
        }

        __requireAllNotNullImpl(a, b, c, d, e, f, g, h, i, j)

        block(a!!, b!!, c!!, d!!, e!!, f!!, g!!, h!!, i!!, j!!)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <A, B, C, D, E, F, G, H, I, J> requireAllNotNullThen(
        a: A?,
        b: B?,
        c: C?,
        d: D?,
        e: E?,
        f: F?,
        g: G?,
        h: H?,
        i: I?,
        j: J?,
        crossinline message: (List<Int>) -> String,
        block: (A, B, C, D, E, F, G, H, I, J) -> Unit
    ) {
        contract {
            returns() implies (a != null)
            returns() implies (b != null)
            returns() implies (c != null)
            returns() implies (d != null)
            returns() implies (e != null)
            returns() implies (f != null)
            returns() implies (g != null)
            returns() implies (h != null)
            returns() implies (i != null)
            returns() implies (j != null)
        }

        __requireAllNotNullImpl(a, b, c, d, e, f, g, h, i, j, message = message)

        block(a!!, b!!, c!!, d!!, e!!, f!!, g!!, h!!, i!!, j!!)
    }

    inline fun requireAllNotNullThen(vararg values: Any?, block: (Array<Any>) -> Unit) {
        __requireAllNotNullImpl(*values)

        @Suppress("UNCHECKED_CAST")
        block(values as Array<Any>)
    }

    inline fun requireAllNotNullThen(vararg values: Any?, crossinline message: (List<Int>) -> String, block: (Array<Any>) -> Unit) {
        __requireAllNotNullImpl(*values, message = message)

        @Suppress("UNCHECKED_CAST")
        block(values as Array<Any>)
    }
}
