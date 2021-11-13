package org.smartbit4all.platform.gradle

import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

public class SB4VaadinModulePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        SB4PluginExtension extension = project.extensions.findByName(SB4PluginExtension.EXTENSION_NAME)
        if (extension == null) {
            extension = project.extensions.create(SB4PluginExtension.EXTENSION_NAME, SB4PluginExtension)
        }
        // apply plugins
        project.getPlugins().apply("io.spring.dependency-management")
        // repositories
        project.repositories {
            mavenCentral()
            maven {
                url = "https://maven.vaadin.com/vaadin-addons"
            }
        }
        // test
        project.tasks.getByName(JavaPlugin.TEST_TASK_NAME, {
            useJUnitPlatform()
        })
        // vaadin dependencies
        project.afterEvaluate { setupProject(it, extension) }
    }

    void setupProject(Project p, SB4PluginExtension extension) {
        String vaadinVersion = p.properties.get("vaadinVersion")
        p.ext.set('vaadinVersion', vaadinVersion)
        DependencyManagementExtension dependencyManagement = p.extensions.getByName("dependencyManagement")
        dependencyManagement.imports {
            mavenBom "com.vaadin:vaadin-bom:${vaadinVersion}"
        }
        p.dependencies {
            api 'org.springframework.boot:spring-boot-starter-web'
            implementation('com.vaadin:vaadin-spring-boot-starter') {
                // Webjars are only needed when running in Vaadin 13 compatibility mode
                ["com.vaadin.webjar", "org.webjars.bowergithub.insites",
                 "org.webjars.bowergithub.polymer", "org.webjars.bowergithub.polymerelements",
                 "org.webjars.bowergithub.vaadin", "org.webjars.bowergithub.webcomponents"]
                        .forEach { group -> exclude(group: group) }
            }
        }

    }

}
