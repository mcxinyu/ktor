/*
 * Copyright 2014-2024 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

description = "Ktor network utilities"

kotlin {
    createCInterop("network", nixTargets()) {
        definitionFile = projectDir.resolve("nix/interop/network.def")
    }
    createCInterop("un", androidNativeTargets()) {
        definitionFile = projectDir.resolve("androidNative/interop/un.def")
    }
    createCInterop("un", iosTargets() + tvosTargets() + watchosTargets()) {
        definitionFile = projectDir.resolve("darwin/interop/un.def")
    }
    createCInterop("afunix", windowsTargets()) {
        definitionFile = projectDir.resolve("windows/interop/afunix.def")
    }

    sourceSets {
        jvmAndPosixMain {
            dependencies {
                api(project(":ktor-utils"))
            }
        }

        jvmAndPosixTest {
            dependencies {
                api(project(":ktor-test-dispatcher"))
            }
        }

        jvmTest {
            dependencies {
                implementation(project(":ktor-shared:ktor-test-base"))
                implementation(libs.mockk)
            }
        }
    }
}
