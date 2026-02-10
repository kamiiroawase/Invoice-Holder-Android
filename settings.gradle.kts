pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.cnb.cool/xizhongyou123/xzy-sdk/-/packages/")
            credentials {
                username = "cnb"
                password = "1z1ea1HWb2SemFA9BMI0e80Ka1E"
            }
        }
    }
}

rootProject.name = "Invoice Holder"
include(":app")
