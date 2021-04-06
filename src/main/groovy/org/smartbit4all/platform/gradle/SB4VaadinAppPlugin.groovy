package org.smartbit4all.platform.gradle

import com.vaadin.gradle.VaadinFlowPluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project

public class SB4VaadinAppPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.getPlugins().apply("org.smartbit4all.platform.gradle.vaadin-module")
        project.getPlugins().apply("org.springframework.boot")
        project.getPlugins().apply("com.vaadin")

        project.afterEvaluate { Project proj ->
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

            def vaadin = proj.getExtensions().getByType(VaadinFlowPluginExtension)
            vaadin.pnpmEnable = true

            proj.tasks.getByName("vaadinBuildFrontend", {
                doLast{
                    file('build/vaadin-generated/.keep').text=""
                    println "build/vaadin-generated/.keep has been generated."
                }
            })


            proj.tasks.create("eclipseVaadinSync", DefaultTask.class, {
                dependsOn("vaadinBuildFrontend")
                dependsOn("assemble")
            })

        }
    }

}
