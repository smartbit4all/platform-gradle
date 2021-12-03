package org.smartbit4all.platform.gradle


import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension

public class SB4JavaPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        SB4PluginExtension extension = project.extensions.findByName(SB4PluginExtension.EXTENSION_NAME)
        if (extension == null) {
            extension = project.extensions.create(SB4PluginExtension.EXTENSION_NAME, SB4PluginExtension)
        }

        // apply plugins
        project.getPlugins().apply(JavaLibraryPlugin.class);
        project.getPlugins().apply("io.spring.dependency-management")

        // repositories
        project.repositories {
            mavenCentral()
        }

        // test
        project.tasks.getByName(JavaPlugin.TEST_TASK_NAME, {
            useJUnitPlatform()
        })

        project.afterEvaluate { setupProject(it, extension) }
    }

    void setupProject(Project proj, SB4PluginExtension extension) {
        String springBootVersion = proj.properties.get("springBootVersion")
        if (!springBootVersion) {
            springBootVersion = "2.3.12.RELEASE"
        }
        proj.ext.set('springBootVersion', springBootVersion)
        DependencyManagementExtension dependencyManagement = proj.extensions.getByName("dependencyManagement")
        dependencyManagement.imports {
            mavenBom "org.springframework.boot:spring-boot-dependencies:${springBootVersion}"
        }
        // source encoding
        proj.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
            getOptions().setEncoding(extension.sourceEncoding)
        }
        proj.tasks.getByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME) {
            getOptions().setEncoding(extension.sourceEncoding)
        }
        proj.dependencies {
            implementation 'org.slf4j:slf4j-api:1.7.32'
            implementation 'javax.annotation:javax.annotation-api:1.3.2'
            implementation 'javax.validation:validation-api:2.0.1.Final'
            testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.2'
            testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.2'
        }

        if (extension.springBootTest) {
            proj.dependencies {
                testImplementation 'org.junit.jupiter:junit-jupiter'
                testImplementation('org.springframework.boot:spring-boot-starter-test') {
                    exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
                }
                testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
            }

        } else {
            proj.dependencies {
                testImplementation 'org.slf4j:slf4j-simple:1.7.32'
            }
        }
    }

}
