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


        def srcGenMainJava = 'src-gen/main/java'
        project.ext.set('srcGenMainJava', srcGenMainJava)
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet generatedSourceSet = sourceSets.create("generated")
        generatedSourceSet.getJava().setSrcDirs(Arrays.asList(srcGenMainJava));
        SourceSet mainSourceSet = sourceSets.getByName("main")
        mainSourceSet.compileClasspath += generatedSourceSet.output
        mainSourceSet.runtimeClasspath += generatedSourceSet.output
        project.dependencies {
            generatedImplementation 'org.openapitools:jackson-databind-nullable:0.2.1'
            generatedImplementation 'io.swagger:swagger-annotations:1.5.22'
            generatedImplementation 'javax.validation:validation-api:2.0.1.Final'
        }

        project.configurations {
            // for all configurations see:
            // https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_configurations_graph
            // Table 1. Java Library plugin - configurations used to declare dependencies
            generatedApi.extendsFrom(api)
            generatedImplementation.extendsFrom(implementation)
            generatedCompileOnly.extendsFrom(compileOnly)
            generatedCompileOnlyApi.extendsFrom(compileOnlyApi)
            generatedRuntimeOnly.extendsFrom(runtimeOnly)
            generatedTestImplementation.extendsFrom(testImplementation)
            generatedTestCompileOnly.extendsFrom(testCompileOnly)
            generatedTestRuntimeOnly.extendsFrom(testRuntimeOnly)
        }

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

            // model package
            def apiModelPackage = extension.openApi.modelPackage.get()
            def apiModelPackagePrefix = extension.openApi.modelPackagePrefix.get()
            if (!apiModelPackagePrefix.endsWith(".")) {
                apiModelPackagePrefix += "."
            }
            def apiModelPackagePostfix = extension.openApi.modelPackagePostfix.get()
            if (apiModelPackagePostfix && !apiModelPackagePostfix.startsWith(".")) {
                apiModelPackagePostfix = "." + apiModelPackagePostfix
            }
            // api package
            def apiApiPackage = extension.openApi.apiPackage.get()
            def apiApiPackagePrefix = extension.openApi.apiPackagePrefix.get()
            if (!apiApiPackagePrefix.endsWith(".")) {
                apiApiPackagePrefix += "."
            }
            def apiApiPackagePostfix = extension.openApi.apiPackagePostfix.get()
            if (apiApiPackagePostfix && !apiApiPackagePostfix.startsWith(".")) {
                apiApiPackagePostfix = "." + apiApiPackagePostfix
            }
            // invoker package
            def apiInvokerPackage = extension.openApi.invokerPackage.get()
            def apiInvokerPackagePrefix = extension.openApi.invokerPackagePrefix.get()
            if (!apiInvokerPackagePrefix.endsWith(".")) {
                apiInvokerPackagePrefix += "."
            }
            def apiInvokerPackagePostfix = extension.openApi.invokerPackagePostfix.get()
            if (apiInvokerPackagePostfix && !apiInvokerPackagePostfix.startsWith(".")) {
                apiInvokerPackagePostfix = "." + apiInvokerPackagePostfix
            }

            def genModel = extension.openApi.genModel.get()
            def genApiRestClient = extension.openApi.genApiRestClient.get()
            def genApiRestServer = extension.openApi.genApiRestServer.get()
            def genApis = genApiRestClient || genApiRestServer
            if (genApiRestClient && genApiRestServer) {
                // TODO better error handling
                println "***** Generating restApiClient and restApiServer currently not supported! Unexpected results should expected! *****"
            }
            def runGenAllOnCompile = extension.openApi.runGenAllOnCompile.get()

            def descriptorList = []
            def dir = new File("$apiDescriptorPath")
            dir.eachFile(FileType.FILES) { file -> // no recurse!
                if (file.getName().endsWith(".yaml"))
                    descriptorList << file
            }
            println "API descriptor path: $apiDescriptorPath"
            println "API output dir: $apiOutputDir"

            def taskList = []

            if (!genModel && !genApiRestClient && !genApiRestServer) {
                genModel = true
            }
            if ( (genApiRestServer && genApiRestClient) || (genModel && genApiRestClient) || (genModel && genApiRestServer)) {
                println "genModel, genApiRestClient, genApiRestServer cannot be true at the same time!"
                return
            }

            // add springfox dependency to rest server
            if(genApiRestServer) {
                proj.dependencies {
                    generatedImplementation 'io.springfox:springfox-swagger2:2.9.2'
                }
            }

            proj.tasks.register('createGeneratorIgnoreFile') {
                // create .openapi-generator-ignore before generating
                doLast {
                    project.mkdir(apiOutputDir)
                    project.file("$apiOutputDir/.openapi-generator-ignore").text = """
/*
api/*
docs/*
gradle*/
/src/
"""
                }
            }



            descriptorList.each {
                def apiName = it.getName().replace("-api.yaml", "");
                def taskName = "openApiGenerate" + apiName.capitalize()
                taskList << taskName

                if (genModel || genApis) {
                    def modelPackageToUse = apiModelPackage
                    if (!modelPackageToUse) {
                        modelPackageToUse = "$apiModelPackagePrefix$apiName$apiModelPackagePostfix"
                    }
                    def apiPackageToUse = apiApiPackage
                    if (!apiPackageToUse) {
                        apiPackageToUse = "$apiApiPackagePrefix$apiName$apiApiPackagePostfix"
                    }
                    def invokerPackageToUse = apiInvokerPackage
                    if (!invokerPackageToUse) {
                        invokerPackageToUse = "$apiInvokerPackagePrefix$apiName$apiInvokerPackagePostfix"
                    }
                    if (genModel) {
                        println "API modelPackage for $apiName: $modelPackageToUse"
                    }
                    if (genApis) {
                        println "API apiPackage for $apiName: $apiPackageToUse"
                        println "API invokerPackage for $apiName: $invokerPackageToUse"
                    }

                    proj.tasks.create(taskName, GenerateTask.class, {
                        if (genModel) {
                            generatorName = "spring"
                            inputSpec = "$apiDescriptorPath$apiName-api.yaml"
                            outputDir = "$apiOutputDir"
                            modelPackage = "$modelPackageToUse"
                            systemProperties = [
                                    models: ""
                            ]
                            configOptions = [
                                    dateLibrary: "java8",
                                    unhandledException: 'true',
                                    hideGenerationTimestamp: 'true',
                                    useTags: 'true',
                                    sourceFolder: '', // without this the generatum is placed under 'src/main/java'
                                    interfaceOnly: 'true'
                            ]
                            typeMappings = [
                                    OffsetDateTime: 'java.time.LocalDateTime'
                            ]
                        }
                        if (genApiRestClient) {
                            generatorName = "java"
                            inputSpec = "$apiDescriptorPath$apiName-api.yaml"
                            outputDir = "$apiOutputDir"
                            modelPackage = "$modelPackageToUse"
                            apiPackage = "$apiPackageToUse"
                            invokerPackage = "$invokerPackageToUse"
                            library = "resttemplate"
                            systemProperties = [
                                    apis: "",
                                    apiTests: "false",
                                    supportingFiles: "",
                                    apiDocs: "false"
                            ]
                            configOptions = [
                                    dateLibrary: "java8",
                                    unhandledException: 'true',
                                    hideGenerationTimestamp: 'true',
                                    useTags: 'true',
                                    sourceFolder: '', // without this the generatum is placed under 'src/main/java'
                                    interfaceOnly: 'true'
                            ]
                        }
                        if (genApiRestServer) {
                            generatorName = "spring"
                            inputSpec = "$apiDescriptorPath$apiName-api.yaml"
                            outputDir = "$apiOutputDir"
                            modelPackage = "$modelPackageToUse"
                            apiPackage = "$apiPackageToUse"
                            invokerPackage = "$invokerPackageToUse"
                            library = "spring-boot"
                            systemProperties = [
                                    apis: "",
                                    supportingFiles: "",
                                    apiTests: "false",
                                    apiDocs: "false"
                            ]
                            configOptions = [
                                    dateLibrary: "java8",
                                    delegatePattern: 'true',
                                    unhandledException: 'true',
                                    hideGenerationTimestamp: 'true',
                                    useTags: 'true',
                                    swaggerDocketConfig: 'true',
                                    sourceFolder: '' // without this the generatum is placed under 'src/main/java'
                            ]
                        }
                    })

                    proj.tasks.getByName(taskName, {
                        // delete all unnecessary files and folders after generating

                        dependsOn('createGeneratorIgnoreFile')

                        doLast{
                            proj.delete "$apiOutputDir/.openapi-generator"
                            proj.delete "$apiOutputDir/api"
                            proj.delete "$apiOutputDir/gradle"
                            proj.delete "$apiOutputDir/src"
                            proj.delete "$apiOutputDir/.openapi-generator-ignore"
                            if(genApiRestServer) {
                                // delete unnecessary generated invoker folder (due unwanted Application class)
                                def invokerPackageFolder = "$invokerPackageToUse".replace(".","/")
                                proj.delete "$apiOutputDir/$invokerPackageFolder"

                                // copy DocumentationConfig to api package
                                def apiPackageFolder = "$apiPackageToUse".replace(".","/")
                                def configFolder = "$apiOutputDir/$apiPackageFolder/config"
                                proj.mkdir configFolder
                                def generatedConfigFolderBase = "$apiOutputDir/org/openapitools"
                                ant.move file: "$generatedConfigFolderBase/configuration/OpenAPIDocumentationConfig.java",
                                         todir: configFolder
                                proj.delete generatedConfigFolderBase

                                ant.replaceregexp(match:"package org.openapitools.configuration", replace:"package $apiPackageToUse" + ".config", flags:'g', byline:true) {
                                    fileset(dir: configFolder, includes: "OpenAPIDocumentationConfig.java")
                                }
                            }
                        }
                    })
                }
            }
            if (taskList.size() > 0) {
                proj.tasks.create("genAll", DefaultTask, {
                    dependsOn(taskList)
                })
                mainSourceSet.output.dir(generatedSourceSet.output)

                if (runGenAllOnCompile) {
                    project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME, {
                        dependsOn('genAll')
                    })
                }
            }

        }

    }
}
