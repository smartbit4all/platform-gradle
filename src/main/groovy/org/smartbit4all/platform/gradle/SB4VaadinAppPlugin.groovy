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

        project.defaultTasks("clean", "vaadinBuildFrontend", "build")

        project.configurations {
            developmentOnly
            runtimeClasspath {
                extendsFrom developmentOnly
            }
        }

        project.dependencies {
            developmentOnly 'org.springframework.boot:spring-boot-devtools'
            testImplementation('org.springframework.boot:spring-boot-starter-test') {
                exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
            }
        }

        def vaadin = project.getExtensions().getByType(VaadinFlowPluginExtension)
        vaadin.pnpmEnable = true

        project.tasks.getByName("vaadinBuildFrontend", {
            doLast{
                file('build/vaadin-generated/.keep').text=""
                println "build/vaadin-generated/.keep has been generated."
            }
        })

        project.tasks.create("eclipseVaadinSync", DefaultTask.class, {
            dependsOn(vaadinBuildFrontend)
            dependsOn(assemble)
        })
    }

}
