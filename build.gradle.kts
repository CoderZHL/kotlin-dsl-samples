import org.gradle.internal.hash.HashUtil
import org.gradle.samples.Sample

plugins {
    base
    id("org.gradle.samples")
}

tasks {

    val samplesWrappers by registering {
        doLast {
            val wrapperFiles = wrapper.get().run {
                listOf(scriptFile, batchScript, jarFile, propertiesFile).associateBy { it.name }
            }
            val hashes = wrapperFiles.mapValues { HashUtil.sha256(it.value) }
            file("samples").walk().filter { it.isFile && it.name in wrapperFiles }.forEach { sampleWrapperFile ->
                wrapperFiles.getValue(sampleWrapperFile.name).let { wrapperFile ->
                    if (HashUtil.sha256(sampleWrapperFile) != hashes.getValue(sampleWrapperFile.name)) {
                        logger.lifecycle("Updating ${sampleWrapperFile.relativeTo(rootDir)}")
                        wrapperFile.copyTo(sampleWrapperFile, overwrite = true)
                    }
                }
            }
        }
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        finalizedBy(samplesWrappers)
    }
}

val samples = project.extensions.getByName("samples") as NamedDomainObjectContainer<Sample>
samples.create("ant") {
    sampleDirectory.set(file("samples/ant"))
    withKotlinDsl {
        archiveContent.from(fileTree(sampleDirectory).exclude("gradle*"))
    }
}