pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // 本机访问 maven.google.com 不稳定，作为回退镜像
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
    }
}
rootProject.name = "DeepSeekNative"
include(":app")
