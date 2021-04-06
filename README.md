# Introduction 

smartbit4all gradle plugin

# Getting Started

1.	Modify local.settings.gradle with includeBuild pointing to platform-gradle source
2.	Add plugins to build.gradle, where necessary

# Available plugins

## Java plugin

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
- add io.spring.dependency-management
- add vaadin.vaadinVersion dependencies
- add vaadin-addons repository
- add spring-boot-starter-web

## Vaadin app

## OpenApi generator