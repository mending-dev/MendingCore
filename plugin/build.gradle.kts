plugins {
    id("mending.java-conventions")
    alias(libs.plugins.run.paper)
}

dependencies {
    implementation(project(":api"))
    compileOnly(libs.paper.api)
}

base {
    archivesName = "MendingCore"
}

val minecraftVersion = libs.versions.minecraft.get()

tasks {
    processResources {
        val props = mapOf("version" to project.version.toString(), "apiVersion" to minecraftVersion)
        inputs.properties(props)
        filesMatching("paper-plugin.yml") { expand(props) }
    }

    // The plugin jar also carries the API classes, so dependent plugins find them at runtime.
    // Deliberately not relocated: relocating the API would break every consumer.
    jar {
        // The files below come from :api:jar; this makes Gradle build it first.
        dependsOn(configurations.runtimeClasspath)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.map { classpath ->
            classpath.map { if (it.isDirectory) it else zipTree(it) }
        })
    }

    runServer {
        minecraftVersion(minecraftVersion)
    }
}