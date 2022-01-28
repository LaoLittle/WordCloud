plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.10.0-RC2"
}

group = "org.laolittle.plugin"
version = "1.1"

repositories {
    maven("https://maven.aliyun.com/repository/central")
    mavenCentral()
}

dependencies {
    val kumoVersion = "1.28"
    val exposedVersion = "0.37.2"
    implementation("org.quartz-scheduler:quartz:2.3.2")
    implementation("com.kennycason:kumo-core:$kumoVersion")
    implementation("com.kennycason:kumo-tokenizers:$kumoVersion")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("com.alibaba:druid:1.2.8")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
}