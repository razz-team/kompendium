import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishBasePlugin
import com.vanniktech.maven.publish.SonatypeHost
import io.bkbn.sourdough.gradle.library.jvm.LibraryJvmPlugin
import io.bkbn.sourdough.gradle.library.jvm.LibraryJvmExtension
import java.net.URI

plugins {
  kotlin("jvm") version "2.1.0" apply false
  kotlin("plugin.serialization") version "2.1.0" apply false
  id("io.bkbn.sourdough.library.jvm") version "0.13.1" apply false
  id("io.bkbn.sourdough.application.jvm") version "0.13.1" apply false
  id("com.vanniktech.maven.publish") version "0.30.0" apply false
  id("io.bkbn.sourdough.root") version "0.13.1"
  id("org.jetbrains.kotlinx.kover") version "0.9.1"
  `maven-publish`
}

dependencies {
  subprojects.forEach { kover(it) }
}

allprojects {
  group = "io.bkbn"
  version = run {
    val baseVersion =
      project.findProperty("project.version") ?: error("project.version needs to be set in gradle.properties")
    when ((project.findProperty("releaseVersion") as? String)?.toBoolean()) {
      true -> baseVersion
      else -> "$baseVersion-SNAPSHOT"
    }
  }
}

subprojects {
  apply(plugin = "maven-publish")

  // it requires signing now for non-snapshot versions
  // for now left as-is, since it doesn't change how we use the library
  publishing {
    repositories {
      maven {
        name = "GitHubPackages"
        url = URI.create("https://maven.pkg.github.com/razz-team/kompendium")
        credentials {
          username = "ignored"
          password = System.getenv("GITHUB_TOKEN")
        }
      }
    }
  }

  // required by the sourdough plugin
  plugins.withType(LibraryJvmPlugin::class) {
    extensions.configure(LibraryJvmExtension::class) {
      githubOrg.set("razz-team")
      githubRepo.set("kompendium")
      licenseName.set("MIT License")
      licenseUrl.set("https://mit-license.org")
      developerId.set("unredundant")
      developerName.set("Ryan Brink")
      developerEmail.set("admin@bkbn.io")
      sonatypeHost.set(SonatypeHost.CENTRAL_PORTAL)
    }
  }
}
