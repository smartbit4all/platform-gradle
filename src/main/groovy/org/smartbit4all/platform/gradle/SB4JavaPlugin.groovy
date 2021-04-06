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

        // java version
        project.afterEvaluate {
            Project p ->
                def javaVersion = extension.javaVersion
                String javaVersionString = javaVersion.toString()
                project.setProperty("sourceCompatibility", javaVersionString)
                project.setProperty("targetCompatibility", javaVersionString)
                def javaPluginExt = project.getExtensions().getByType(JavaPluginExtension)
                javaPluginExt.sourceCompatibility = javaVersion
                javaPluginExt.targetCompatibility = javaVersion
                // source encoding
                p.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
                    getOptions().setEncoding(extension.sourceEncoding)
                }
                p.tasks.getByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME) {
                    getOptions().setEncoding(extension.sourceEncoding)
                }

        }
        // repositories
        project.repositories {
            jcenter()
            mavenCentral()
        }
//        project.repositories.addAll(project.repositories.jcenter())
//        project.repositories.addAll(project.repositories.mavenCentral())
        // test
        project.tasks.getByName(JavaPlugin.TEST_TASK_NAME, {
            useJUnitPlatform()
        })

    }
}
