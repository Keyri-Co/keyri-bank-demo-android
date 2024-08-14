package com.keyrico.keyrisdk.sec

internal fun checkFakeInstance(blockSwizzleDetection: Boolean) {
    if (blockSwizzleDetection) return

    val stackTrace = Thread.currentThread().stackTrace

    if (stackTrace.any { it.className.contains("reflect") && it.methodName == "newInstance" }) {
        throw RuntimeException("Not allowed")
    }
}

internal fun checkFakeNonKeyriInstance(blockSwizzleDetection: Boolean) {
    if (blockSwizzleDetection) return

    val stackTrace = Thread.currentThread().stackTrace

    if (stackTrace.any { it.className.contains("reflect") && it.methodName == "newInstance" } &&
        !stackTrace.any { it.className.contains("com.keyrico.keyrisdk.") }
    ) {
        throw RuntimeException("Not allowed")
    }
}

internal fun checkFakeInvocation(blockSwizzleDetection: Boolean) {
    if (blockSwizzleDetection) return

    val stackTrace = Thread.currentThread().stackTrace

    if (stackTrace.any { it.className.contains("reflect") && it.methodName == "invoke" && !it.isNativeMethod }) {
        throw RuntimeException("Not allowed")
    }
}

internal fun checkFakeNonKeyriInvocation(blockSwizzleDetection: Boolean) {
    if (blockSwizzleDetection) return

    val stackTrace = Thread.currentThread().stackTrace

    if (stackTrace.any { it.className.contains("reflect") && it.methodName == "invoke" && !it.isNativeMethod } &&
        !stackTrace.any { it.className.contains("com.keyrico.keyrisdk.") }
    ) {
        throw RuntimeException("Not allowed")
    }
}
