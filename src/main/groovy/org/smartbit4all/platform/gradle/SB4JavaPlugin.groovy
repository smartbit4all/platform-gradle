package org.smartbit4all.platform.gradle


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

        // apply plugin java-library
        project.getPlugins().apply(JavaLibraryPlugin.class);

        // repositories
        project.repositories {
            jcenter()
            mavenCentral()
        }

        // test
        project.tasks.getByName(JavaPlugin.TEST_TASK_NAME, {
            useJUnitPlatform()
        })

        project.afterEvaluate { setupProject(it, extension) }
    }

    void setupProject(Project proj, SB4PluginExtension extension) {
        // source encoding
        proj.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
            getOptions().setEncoding(extension.sourceEncoding)
        }
        proj.tasks.getByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME) {
            getOptions().setEncoding(extension.sourceEncoding)
        }
        proj.dependencies {
            implementation 'org.slf4j:slf4j-api:1.7.31'
            implementation 'javax.annotation:javax.annotation-api:1.3.2'
            testImplementation 'org.junit.jupiter:junit-jupiter-api:5.7.2'
            testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.7.2'
        }

        if (extension.springBootTest) {
            proj.dependencies {
                testImplementation 'org.junit.jupiter:junit-jupiter'
                testImplementation('org.springframework.boot:spring-boot-starter-test:2.2.6.RELEASE') {
                    exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
                }
                testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
            }

        } else {
            proj.dependencies {
                testImplementation 'org.slf4j:slf4j-simple:1.7.31'
            }
        }
    }

}
