package com.ergonomics.platform

/// 平台信息 — expect/actual 工具
expect object PlatformInfo {
    val name: String
    val isAndroid: Boolean
    val isIos: Boolean
}
