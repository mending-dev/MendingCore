plugins {
    id("mending.java-conventions")
    `maven-publish`
}

dependencies {
    // Paper is provided by the server at runtime, but consumers need it on their compile classpath.
    compileOnlyApi(libs.paper.api)
}

base {
    archivesName = "mending-core-api"
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "mending-core-api"
            from(components["java"])
        }
    }
    repositories {
        // `publish` writes into a local folder that the GitHub workflow then mirrors
        // into mending-dev/maven-repo. Snapshots and releases live in separate folders.
        maven {
            name = "MendingCoreRepo"
            url = layout.buildDirectory.dir(
                if (version.toString().endsWith("-SNAPSHOT")) "repo/snapshots" else "repo/releases"
            ).get().asFile.toURI()
        }
    }
}