# Introduction 

smartbit4all gradle plugin

# Getting Started

1.	Modify settings.gradle or local.settings.gradle with includeBuild pointing to platform-gradle source
2.	Add plugins and setup options to build.gradle, as necessary

# Available plugins

## Java plugin

Include plugin:

```
plugins {
    id 'org.smartbit4all.platform.gradle.java'
}
```

Setup options and defaults:

```
smartbit4all {
    sourceEncoding = 'UTF-8'
    springBootTest = true
}
```

Example gradle.properties
```
springBootVersion=2.3.12.RELEASE
```


Effect:
- repositories: add mavenCentral
- dependencies:
  - add java-library plugin
  - add io.spring.dependency-management plugin
  - add org.springframework.boot:spring-boot-dependencies:${springBootVersion} BOM dependencies
    - springBootVersion = 2.3.12.RELEASE if not specified 
  - implementation 'org.slf4j:slf4j-api:1.7.31'
  - implementation 'javax.annotation:javax.annotation-api:1.3.2'
  - implementation 'javax.validation:validation-api:2.0.1.Final'
  - testImplementation 'org.junit.jupiter:junit-jupiter-api:5.7.2'
  - testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.7.2'
- javac: use sourceEncoding
- test: useJUnitPlatform
- springBootTest: if true, add spring-boot-starter-test to testImplementation, otherwise use slf4j-simple for tests

## Vaadin module

Include plugin:

```
plugins {
    id 'org.smartbit4all.platform.gradle.vaadin-module'
}
```
Requires:
- vaadinVersion in project properties (e.g. gradle.properties)

Effect:
- apply io.spring.dependency-management plugin
- add vaadinVersion dependencies
- add vaadin-addons repository
- add spring-boot-starter-web, vaadin-spring-boot-starter
- test: useJUnitPlatform

## Vaadin app

Include plugin:

```
plugins {
    id 'com.vaadin'
    id 'org.smartbit4all.platform.gradle.vaadin-app'
}

// recommended
vaadin {
  pnpmEnable = true
}
```

Requires:
- vaadin plugin defined in settings.gradle (should be at first line)
```
pluginManagement {
  plugins {
        id 'com.vaadin' version "${vaadinPluginVersion}" apply false
    }
}
```

Example gradle.properties
```
vaadinVersion=14.7.5
vaadinPluginVersion=0.14.7.3
```

Effect:
- apply plugins:
  - org.smartbit4all.platform.gradle.vaadin-module
  - org.springframework.boot
- make defaultTasks: clean, vaadinBuildFrontend, build
- enable vaadin.pnpm
- add .keep to build/vaadin-generated
- create task eclipseVaadinSync
  - depends on vaadinBuildFrontend, assemble

## OpenApi generator

Include plugin:

```
plugins {
    id 'org.smartbit4all.platform.gradle.openapi'
}
```

Setup options and defaults:

```
smartbit4all {
    openApi {
        apiDescriptorPath = ${projectDir}/src/main/resources/descriptors/
        modelPackage = ""
        modelPackagePrefix = "org.smartbit4all.api"
        modelPackagePostfix = "model"
        apiPackage = ""
        apiPackagePrefix = "org.smartbit4all.api"
        apiPackagePostfix = "service"

        genModel = true
        genApiRestClient = false
        genApiRestServer = false

        runGenAllOnCompile = false
        
        importMappings = []
        
        dateTimeMapping = "" // may use java.time.LocalDateTime, java.time.ZonedDateTime, etc.
    }
}
```

Effect:
- add src-gen/main/java to project
- add openApiGenerate<API> task for all <API>-api.yaml files found in apiDescriptorPath (non-recursive). openApiGenerate<API> task will generate:
  - model files (beans) to modelPackage, if specified, otherwise to modelPackagePrefix.<API>.modelPackagePostfix (if genModel==true)
  - service files (apis) to apiPackagePrefix.<API>.apiPackagePostfix for client and server (if genApiRestClient==true || genApiRestServer == true)
- add genAll task, which depends on all openApiGenerate<API> tasks
- make compileJava depend on genAll if runGenAllOnCompile == true

Limitations:
- only one of genModel/genApiRestClient/genApiRestServer can be true at a time!
