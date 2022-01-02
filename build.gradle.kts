plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.9.0-M1"
}

group = "org.laolittle.plugin"
version = "1.0"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    val kumoVersion = "1.28"
    implementation("com.kennycason:kumo-core:$kumoVersion")
    implementation("com.kennycason:kumo-tokenizers:$kumoVersion")
}