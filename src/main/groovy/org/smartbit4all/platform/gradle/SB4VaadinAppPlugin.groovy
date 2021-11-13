package org.smartbit4all.platform.gradle


import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project

public class SB4VaadinAppPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.getPlugins().apply("org.smartbit4all.platform.gradle.vaadin-module")
        project.getPlugins().apply("org.springframework.boot")

        String vaadinVersion = project.properties.get("vaadinVersion")
        if (vaadinVersion.startsWith("14")) {
            project.tasks.getByName("vaadinBuildFrontend", {
                doLast {
                    project.file('build/vaadin-generated/.keep').text = ""
                    println "build/vaadin-generated/.keep has been generated."
                }
            })
        } else {
            // TODO anything??
        }

        project.tasks.create("eclipseVaadinSync", DefaultTask.class, {
            dependsOn("vaadinBuildFrontend")
            dependsOn("assemble")
        })

        project.afterEvaluate { setupProject(it) }

    }

    void setupProject(Project proj) {
        proj.defaultTasks("clean", "vaadinBuildFrontend", "build")

        proj.configurations {
            developmentOnly
            runtimeClasspath {
                extendsFrom developmentOnly
            }
        }

        proj.dependencies {
            developmentOnly 'org.springframework.boot:spring-boot-devtools'
            testImplementation('org.springframework.boot:spring-boot-starter-test') {
                exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
            }
        }

    }
}
