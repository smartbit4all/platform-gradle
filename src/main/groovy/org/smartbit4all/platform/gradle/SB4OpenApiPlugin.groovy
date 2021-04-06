package org.smartbit4all.platform.gradle

import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class SB4OpenApiPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        SB4PluginExtension extension = project.extensions.findByName(SB4PluginExtension.EXTENSION_NAME)
        if (extension == null) {
            extension = project.extensions.create(SB4PluginExtension.EXTENSION_NAME, SB4PluginExtension)
        }
        project.getPlugins().apply("org.openapi.generator")

        project.dependencies {
            implementation 'org.openapitools:jackson-databind-nullable:0.2.1'
            implementation 'io.swagger:swagger-annotations:1.5.22'
            implementation 'javax.validation:validation-api:2.0.1.Final'
        }

        def srcGenMainJava = 'src-gen/main/java'
        project.ext.set('srcGenMainJava', srcGenMainJava)
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet generated = sourceSets.create("generated")
        generated.getJava().setSrcDirs(Arrays.asList(srcGenMainJava));

        project.afterEvaluate { Project proj ->
            def apiDescriptorPath = extension.openApi.apiDescriptorPath.get()
            if (!apiDescriptorPath) {
                apiDescriptorPath = "${proj.projectDir}/src/main/resources/descriptors/"
            }
            if (!apiDescriptorPath.endsWith("/")) {
                apiDescriptorPath += "/"
            }
            // TODO parameterize outputDir
//            def apiOutputDir = extension.openApi.outputDir.get()
//            if (!apiOutputDir) {
//                apiOutputDir = "${proj.projectDir}/${srcGenMainJava}"
//            }
            def apiOutputDir = "${proj.projectDir}/${srcGenMainJava}"
            def apiModelPackagePrefix = extension.openApi.modelPackagePrefix.get()
            if (!apiModelPackagePrefix.endsWith(".")) {
                apiModelPackagePrefix += "."
            }
            def apiModelPackagePostfix = extension.openApi.modelPackagePostfix.get()
            if (apiModelPackagePostfix && !apiModelPackagePostfix.startsWith(".")) {
                apiModelPackagePostfix = "." + apiModelPackagePostfix
            }
            def genModel = extension.openApi.genModel.get()
            def genApis = extension.openApi.genApis.get()
            def runGenAllOnCompile = extension.openApi.runGenAllOnCompile.get()

            def descriptorList = []
            def dir = new File("$apiDescriptorPath")
            dir.eachFile(FileType.FILES) { file -> // no recurse!
                if (file.getName().endsWith(".yaml"))
                    descriptorList << file
            }
            println "API descriptor path: $apiDescriptorPath"
            println "API output dir: $apiOutputDir"
            println "API generated package: $apiModelPackagePrefix<API>$apiModelPackagePostfix"

            def taskList = []

            descriptorList.each {
                def apiName = it.getName().replace("-api.yaml", "");
                def taskName = "openApiGenerate" + apiName.capitalize()
                taskList << taskName

                if (genModel || genApis) {
                    proj.tasks.create(taskName, GenerateTask.class, {
                        generatorName = "spring"
                        inputSpec = "$apiDescriptorPath$apiName-api.yaml"
                        outputDir = "$apiOutputDir"
                        modelPackage = "$apiModelPackagePrefix$apiName$apiModelPackagePostfix"
                        if (genModel && genApis) {
                            systemProperties = [
                                    models: "",
                                    apis: ""
                            ]
                        } else if (genModel) {
                            systemProperties = [
                                    models: ""
                            ]
                        } else {
                            systemProperties = [
                                    apis: ""
                            ]
                        }

                        configOptions = [
                                dateLibrary            : "java8",
                                unhandledException     : 'true',
                                hideGenerationTimestamp: 'true',
                                useTags                : 'true',
                                sourceFolder           : '', // without this the generatum is placed under 'src/main/java'
                                interfaceOnly          : 'true'
                        ]
                    })
                }
            }
            if (taskList.size() > 0) {
                proj.tasks.create("genAll", DefaultTask, {
                    dependsOn(taskList)
                })
                if (runGenAllOnCompile) {
                    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME, {
                        dependsOn('genAll')
                    })

                }
//                    proj.tasks.
            }

        }

    }
}
