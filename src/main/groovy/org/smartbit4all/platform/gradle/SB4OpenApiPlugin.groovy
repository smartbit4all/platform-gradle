package org.smartbit4all.platform.gradle

import org.apache.tools.ant.taskdefs.condition.HasMethod
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import groovy.io.FileType

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
    SourceSet mainSourceSet = sourceSets.getByName("main")
    mainSourceSet.getJava().srcDirs(srcGenMainJava)

    project.afterEvaluate { setupProject(it, extension, srcGenMainJava) }
  }

  void setupProject(Project proj, SB4PluginExtension extension, String srcGenMainJava) {
    def apiDescriptorPath = extension.openApi.apiDescriptorPath.get()
    if (!apiDescriptorPath) {
      apiDescriptorPath = "${proj.projectDir}/src/main/resources/descriptors/"
    }
    if (!apiDescriptorPath.endsWith("/")) {
      apiDescriptorPath += "/"
    }
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
      proj.logger.error "***** Generating restApiClient and restApiServer currently not supported! Unexpected results should expected! *****"
    }
    def runGenAllOnCompile = extension.openApi.runGenAllOnCompile.get()

    def descriptorList = []
    def apiDescriptorMappingParam = extension.openApi.apiDescriptorMapping.get()
    Map<String, String> apiDescriptorMapping = new HashMap<>(apiDescriptorMappingParam)
    if (apiDescriptorMapping.isEmpty()) {
      proj.logger.debug "apiDescriptorMapping is empty, creating from directory $apiDescriptorPath:"
      def dir = new File("$apiDescriptorPath")
      dir.eachFile(FileType.FILES) { file ->
        // no recurse!
        if (file.getName().endsWith("-api.yaml")) {
          def apiName = file.getName().replace("-api.yaml", "")
          apiDescriptorMapping.put("$apiDescriptorPath$apiName-api.yaml", apiName)
        }
      }
    } else {
      proj.logger.debug "apiDescriptorMapping is not empty:"
    }
    apiDescriptorMapping.each { it ->
      proj.logger.debug("$it.key: $it.value")
    }

    if (!genModel && !genApiRestClient && !genApiRestServer) {
      genModel = true
    }
    if ( (genApiRestServer && genApiRestClient)) {
      proj.logger.error "genApiRestClient, genApiRestServer cannot be true at the same time!"
      return
    }

    // add dependencies to project based on what to generate
    // TODO fix it, it doesn't work here
    if (genModel) {
      proj.dependencies {
        implementation 'org.openapitools:jackson-databind-nullable:0.2.1'
        implementation 'io.swagger:swagger-annotations:1.5.22'
        implementation 'javax.validation:validation-api:2.0.1.Final'
        implementation 'com.google.code.findbugs:jsr305:3.0.2'
      }
    }
    if (genApiRestServer) {
      proj.dependencies {
        implementation 'org.openapitools:jackson-databind-nullable:0.2.1'
        implementation 'io.swagger:swagger-annotations:1.5.22'
        implementation 'javax.validation:validation-api:2.0.1.Final'
        implementation 'com.google.code.findbugs:jsr305:3.0.2'

        implementation 'io.springfox:springfox-swagger2:2.9.2'
        implementation 'io.springfox:springfox-swagger-common:2.9.2'
        implementation 'io.springfox:springfox-swagger-ui:2.9.2'
      }
    }
    if (genApiRestClient) {
      // TODO which library?
    }

    proj.tasks.register('createGeneratorIgnoreFile') {
      doFirst {
        proj.logger.debug "createGeneratorIgnoreFil.doFirst"
      }
      // create .openapi-generator-ignore before generating
      doLast {
        proj.logger.debug "createGeneratorIgnoreFil.doLast begins"
        proj.mkdir(apiOutputDir)
        proj.file("$apiOutputDir/.openapi-generator-ignore").text = """
/*
api/*
docs/*
gradle*/
/src/
"""
        proj.logger.debug "createGeneratorIgnoreFil.doLast ends"
      }
    }

    def mappings = extension.openApi.importMappings
    def dateTimeMapping = extension.openApi.dateTimeMapping.get()
    def taskList = []

    apiDescriptorMapping.keySet().each {apiDescriptor ->
      proj.logger.debug "Creating task for $apiDescriptor"
      def apiName = apiDescriptorMapping.get(apiDescriptor)
      proj.logger.debug "API name $apiName"
      def taskName = "openApiGenerate" + apiName.capitalize()
      proj.logger.debug "Task name $taskName"
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

        proj.tasks.create(taskName, GenerateTask.class, {
          if (genModel && !genApis) {
            generatorName = "java"
            library = "resttemplate"
            inputSpec = "$apiDescriptor"
            outputDir = "$apiOutputDir"
            modelPackage = "$modelPackageToUse"
            globalProperties = [
              models: "",
              modelDocs: "false"
            ]
            configOptions = [
              dateLibrary            : "java8",
              useBeanValidation      : 'true',
              unhandledException     : 'true',
              hideGenerationTimestamp: 'true',
              useTags                : 'true',
              sourceFolder           : '', // without this the generatum is placed under 'src/main/java'
              interfaceOnly          : 'true'
            ]
          }
          if (genApiRestClient) {
            generatorName = "java"
            library = "resttemplate"
            inputSpec = "$apiDescriptor"
            outputDir = "$apiOutputDir"
            modelPackage = "$modelPackageToUse"
            apiPackage = "$apiPackageToUse"
            invokerPackage = "$invokerPackageToUse"
            globalProperties = [
              apis: "",
              apiTests: "false",
              supportingFiles: "",
              apiDocs: "false"
            ]
            if (genModel) {
              globalProperties.putAll([
                      models   : "",
                      modelDocs: "false"
              ])
            }
            configOptions = [
              dateLibrary: "java8",
              useAbstractionForFiles: 'true',
              unhandledException: 'true',
              hideGenerationTimestamp: 'true',
              useTags: 'true',
              sourceFolder: '', // without this the generatum is placed under 'src/main/java'
              interfaceOnly: 'true'
            ]
          }
          if (genApiRestServer) {
            generatorName = "spring"
            library = "spring-mvc"
            inputSpec = "$apiDescriptor"
            outputDir = "$apiOutputDir"
            modelPackage = "$modelPackageToUse"
            apiPackage = "$apiPackageToUse"
            invokerPackage = "$invokerPackageToUse"
            globalProperties = [
              apis: "",
              supportingFiles: "",
              apiTests: "false",
              apiDocs: "false"
            ]
            if (genModel) {
              globalProperties.putAll([
                      models   : "",
                      modelDocs: "false"
              ])
            }
            configOptions = [
              dateLibrary: "java8",
              delegatePattern: 'true',
              unhandledException: 'true',
              hideGenerationTimestamp: 'true',
              useTags: 'true',
              swaggerDocketConfig: 'true',
              annotationLibrary: 'swagger1',
              documentationProvider: 'springfox',
              sourceFolder: '' // without this the generatum is placed under 'src/main/java'
            ]
          }
          if (!"".equals(dateTimeMapping)) {
            typeMappings = [
              OffsetDateTime: "$dateTimeMapping"
            ]
          }

          if (mappings != []) {
            importMappings = mappings
          }
        })

        proj.tasks.getByName(taskName, {
          // delete all unnecessary files and folders after generating

          dependsOn('createGeneratorIgnoreFile')

          doFirst {
            proj.logger.debug("$taskName .doFirst begins")
            proj.logger.debug "$taskName .doFirst begins"
            proj.logger.lifecycle "API descriptor: $apiDescriptor"
            proj.logger.lifecycle "API name: $apiName"
            proj.logger.lifecycle "API output dir: $apiOutputDir"
            proj.logger.lifecycle "API modelPackage for $apiName: $modelPackageToUse"
            if (genApis) {
              proj.logger.lifecycle "API apiPackage for $apiName: $apiPackageToUse"
              proj.logger.lifecycle "API invokerPackage for $apiName: $invokerPackageToUse"
            }
            proj.logger.debug "$taskName .doFirst ends"
          }

          doLast {
            proj.logger.debug "$taskName .doLast begins"
            proj.delete "$apiOutputDir/.openapi-generator"
            proj.delete "$apiOutputDir/api"
            proj.delete "$apiOutputDir/gradle"
            proj.delete "$apiOutputDir/src"
            proj.delete "$apiOutputDir/.openapi-generator-ignore"
            if(genApiRestServer) {
              // delete unnecessary generated invoker folder (due unwanted Application class)
              // def invokerPackageFolder = "$invokerPackageToUse".replace(".","/")
              // proj.delete "$apiOutputDir/$invokerPackageFolder"

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
            proj.logger.debug "$taskName .doLast ends"

          }
        })
      }
    }
    proj.logger.debug("Tasklist: $taskList")
    if (taskList.size() > 0) {
      proj.tasks.create("genAll", DefaultTask, {
        dependsOn(taskList)
      })

      if (runGenAllOnCompile) {
        proj.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME, {
          dependsOn('genAll')
        })
      }
    }

  }
}
