plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
    id("com.gradle.develocity").version("3.17.1")
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("no")
    }
}

rootProject.name = "csa3-140324-asm-stack"

include("asm")
include("comp")
include("isa")
