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
    javaVersion = JavaVersion_1_8
    sourceEncoding = 'UTF-8'
}
```

Effect:
- java: set javaVersion
- javac: use sourceEncoding
- repositories: add jcenter and mavenCentral
- test: useJUnitPlatform

## Vaadin module

Include plugin:

```
plugins {
    id 'org.smartbit4all.platform.gradle.vaadin-module'
}
```

Setup options and defaults:

```
smartbit4all {
    vaadinVersion = "14.4.6"
}
```

Effect:
- apply io.spring.dependency-management plugin
- add vaadin.vaadinVersion dependencies
- add vaadin-addons repository
- add spring-boot-starter-web, vaadin-spring-boot-starter
- test: useJUnitPlatform

## Vaadin app

Include plugin:

```
plugins {
    id 'org.smartbit4all.platform.gradle.vaadin-app'
}
```

Setup options and defaults:

```
// none
```

Effect:
- apply plugins:
  - org.smartbit4all.platform.gradle.vaadin-module
  - org.springframework.boot
  - com.vaadin
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
        modelPackagePrefix = "org.smartbit4all.api"
        modelPackagePostfix = "model"
        apiPackagePrefix = "org.smartbit4all.api"
        apiPackagePostfix = "service"

        genModel = true
        genApis = false

        runGenAllOnCompile = false
    }
}
```

Effect:
- add src-gen/main/java to project
- add openApiGenerate<API> task for all <API>-api.yaml files found in apiDescriptorPath (non-recursive). openApiGenerate<API> task will generate:
  - model files (beans) to modelPackagePrefix.<API>.modelPackagePostfix (if genModel==true)
  - service files (apis) to apiPackagePrefix.<API>.apiPackagePostfix (if genApis==true)
- add genAll task, which depends on all openApiGenerate<API> tasks
- make compileJava depend on genAll if runGenAllOnCompile == true

