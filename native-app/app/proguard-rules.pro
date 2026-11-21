# 网页通过 window.NativeShell 按名字调用这些方法：R8 不能改名或删除。
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
