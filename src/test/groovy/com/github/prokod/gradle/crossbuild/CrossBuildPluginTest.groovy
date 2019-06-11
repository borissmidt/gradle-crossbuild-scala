/*
 * Copyright 2016-2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.prokod.gradle.crossbuild

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Ignore
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class CrossBuildPluginTest extends CrossBuildGradleRunnerSpec {
    File buildFile
    File propsFile

    def setup() {
        buildFile = file('build.gradle')
        propsFile = file('gradle.properties')
    }

    @Unroll
    def "[#gradleVersion] applying crossbuild plugin with default archiveAppendix and value for each specified targetVersion and no publishing block should produce expected set of crossbuild jar task(s) when calling `tasks`"() {
        given:
        buildFile << """
plugins {
    id 'com.github.prokod.gradle-crossbuild'
}

crossBuild {
    builds {
        v211
        v212
    }
}
"""

        when:
        def result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(dir.root)
                .withPluginClasspath()
                .withArguments('tasks', '--info', '--stacktrace')
                .build()

        then:
        result.output.contains('crossBuild211Jar')
        !result.output.contains('publishCrossBuild211PublicationToMavenLocal')
        result.output.contains('crossBuild212Jar')
        !result.output.contains('publishCrossBuild212PublicationToMavenLocal')
        result.task(":tasks").outcome == SUCCESS

        where:
        gradleVersion << ['4.2', '4.9']
    }

    @Unroll
    def "[#gradleVersion] applying crossbuild plugin with default archiveAppendix for each specified targetVersion and no publishing block should produce expected set of crossbuild jar task(s) when calling `tasks`"() {
        given:
        buildFile << """
plugins {
    id 'com.github.prokod.gradle-crossbuild'
}

crossBuild {
    archive {
        appendixPattern = '_?'
    }
    builds {
        v211
        v212
    }
}
"""

        when:
        def result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(dir.root)
                .withPluginClasspath()
                .withArguments('tasks', '--info', '--stacktrace')
                .build()

        then:
        result.output.contains('crossBuild211Jar')
        !result.output.contains('publishCrossBuild211PublicationToMavenLocal')
        result.output.contains('crossBuild212Jar')
        !result.output.contains('publishCrossBuild212PublicationToMavenLocal')
        result.task(":tasks").outcome == SUCCESS

        where:
        gradleVersion << ['4.2', '4.9']
    }

    @Unroll
    def "[#gradleVersion] applying crossbuild plugin with default archiveAppendix for each specified targetVersion and calling `tasks` should produce expected set of crossbuild jar task(s)"() {
        given:
        buildFile << """
plugins {
    id 'com.github.prokod.gradle-crossbuild'
    id 'maven-publish'
}

group = 'project.group'

crossBuild {
    builds {
        v211
        v212
    }
}

publishing {
    publications {
        crossBuild211(MavenPublication) {
            artifact crossBuild211Jar
        }
        crossBuild212(MavenPublication) {
            artifact crossBuild212Jar
        }
    }
}
"""

        when:
        def result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(dir.root)
                .withPluginClasspath()
                .withArguments('tasks', '--info', '--stacktrace')
                .build()

        then:
        result.output.contains('crossBuild211Jar')
        result.output.contains('publishCrossBuild211PublicationToMavenLocal')
        result.output.contains('crossBuild212Jar')
        result.output.contains('publishCrossBuild212PublicationToMavenLocal')
        result.task(":tasks").outcome == SUCCESS

        where:
        gradleVersion << ['4.2','4.9']
    }

    @Unroll
    def "[#gradleVersion] applying crossbuild plugin with default archiveAppendix for each specified targetVersion and calling `crossBuild2XJar` should produce expected set of crossbuild jar task(s) with matching archive name"() {
        given:
        buildFile << """
plugins {
    id 'com.github.prokod.gradle-crossbuild'
}

crossBuild {
    builds {
        v211
        v212
    }
}
"""

        when:
        def result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(dir.root)
                .withPluginClasspath()
                .withArguments('crossBuild211Jar', 'crossBuild212Jar', '--info', '--stacktrace')
                .build()

        then:
        result.output.contains('_2.11')
        result.output.contains('_2.12')
        result.task(":crossBuild211Jar").outcome == SUCCESS
        result.task(":crossBuild212Jar").outcome == SUCCESS

        where:
        gradleVersion << ['4.2', '4.9']
    }

    @Unroll
    def "[#gradleVersion] applying crossbuild plugin with custom archiveAppendix for each specified targetVersion should produce expected set of crossbuild jar task(s) with matching archive name"() {
        given:
        propsFile << 'prop=A'

        buildFile << """
plugins {
    id 'com.github.prokod.gradle-crossbuild'
}

crossBuild {
    builds {
        v210 {
            archive.appendixPattern = "_?_\${prop}"
        }
        v211 {
            archive.appendixPattern = "_?_\${prop}"
        }
    }
}
"""

        when:
        def result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(dir.root)
                .withPluginClasspath()
                .withArguments('crossBuild210Jar', 'crossBuild211Jar', '--info', '--stacktrace')
                .build()

        then:
        result.output.contains('_2.10_A')
        result.output.contains('_2.11_A')
        result.task(":crossBuild210Jar").outcome == SUCCESS
        result.task(":crossBuild211Jar").outcome == SUCCESS

        where:
        gradleVersion << ['4.2', '4.9']
    }

    @Unroll
    def "[#gradleVersion] applying crossbuild plugin with custom archiveAppendix and custom archivesBaseName for each specified targetVersion should produce expected set of crossbuild jar task/s with matching archive name"() {
        given:
        propsFile << 'prop=A'

        buildFile << """
plugins {
    id 'com.github.prokod.gradle-crossbuild'
    id 'maven-publish'
}

archivesBaseName = 'mylib'

crossBuild {
    builds {
        v210 {
            archive.appendixPattern = "_?_\${prop}"
        }
        v211 {
            archive.appendixPattern = "_?_\${prop}"
        }
    }
}

gradle.projectsEvaluated {
    logger.info(tasks.crossBuild210Jar.baseName)
    logger.info(tasks.crossBuild211Jar.baseName)
}
"""

        when:
        def result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(dir.root)
                .withPluginClasspath()
                .withDebug(true)
                .withArguments('crossBuild210Jar', 'crossBuild211Jar', '--info', '--stacktrace')
                .build()

        then:
        result.output.contains('mylib_2.10_A')
        result.output.contains('mylib_2.11_A')
        result.task(":crossBuild210Jar").outcome == SUCCESS
        result.task(":crossBuild211Jar").outcome == SUCCESS

        where:
        gradleVersion << ['4.2', '4.9']
    }

    @Unroll
    @Ignore
    def "[#gradleVersion] applying crossbuild plugin with custom target version value for each specified ScalaVer should produce expected set of crossbuild jar task/s with matching archive name"() {
        given:
        propsFile << 'prop=A'

        buildFile << """
import com.github.prokod.gradle.crossbuild.model.*

plugins {
    id 'com.github.prokod.gradle-crossbuild'
}

model {
    crossBuild {
        targetVersions {
            archivesBaseName = 'mylib'
            v210(ScalaVer) {
                value = '2.10'
            }
            v210_A(ScalaVer) {
                value = '2.10'
                archiveAppendix = "_?_\${prop}"
            }
            V211(ScalaVer) {
                value = '2.11'
            }
            V211_A(ScalaVer) {
                value = '2.11'
                archiveAppendix = "_?_\${prop}"
            }
        }
    }
}
"""

        when:
        def result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(dir.root)
                .withPluginClasspath()
                .withArguments('crossBuild210Jar', 'crossBuild211Jar', 'crossBuild210_AJar', 'crossBuild211_AJar', '--info', '--stacktrace')
                .build()

        then:
        result.output.contains('\'mylib_2.10_A\'')
        result.output.contains('\'mylib_2.10\'')
        result.output.contains('\'mylib_2.11_A\'')
        result.output.contains('\'mylib_2.11\'')
        result.task(":crossBuild210Jar").outcome == SUCCESS
        result.task(":crossBuild211Jar").outcome == SUCCESS
        result.task(":crossBuild210_AJar").outcome == SUCCESS
        result.task(":crossBuild211_AJar").outcome == SUCCESS

        where:
        gradleVersion << ['2.14.1', '3.0', '4.1']
    }

    @Unroll
    def "[#gradleVersion] applying crossbuild plugin with default archiveAppendix for each specified targetVersion and calling `publishToMavenLocal` should produce expected set of crossbuild jar task/s with matching archive name and expected matching publications"() {
        given:
        buildFile << """
plugins {
    id 'com.github.prokod.gradle-crossbuild'
    id 'maven-publish'
}

group = 'test'

crossBuild {
    builds {
        v210
        v211
    }
}

publishing {
    publications {
        crossBuild210(MavenPublication) {
            artifact crossBuild210Jar
        }
        crossBuild211(MavenPublication) {
            artifact crossBuild211Jar
        }
    }
}
"""

        when:
        def result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(dir.root)
                .withPluginClasspath()
                .withArguments('crossBuild210Jar', 'crossBuild211Jar', 'publishToMavenLocal', '--info', '--stacktrace')
                .build()

        then:
        result.output.contains('_2.10')
        result.output.contains('_2.11')
        result.task(":crossBuild210Jar").outcome == SUCCESS
        result.task(":publishCrossBuild210PublicationToMavenLocal").outcome == SUCCESS
        result.task(":crossBuild211Jar").outcome == SUCCESS
        result.task(":publishCrossBuild211PublicationToMavenLocal").outcome == SUCCESS

        where:
        gradleVersion << ['4.2', '4.9']
    }

    @Unroll
    @Ignore
    def "[#gradleVersion] applying crossbuild plugin with default archiveAppendix for each specified targetVersion and adding user defined configurations should not cause any exception"() {
        given:
        buildFile << """
import com.github.prokod.gradle.crossbuild.model.*

plugins {
    id 'com.github.prokod.gradle-crossbuild'
}

group = 'test'

model {
    crossBuild {
        targetVersions {
            v210(ScalaVer)
            v211(ScalaVer)
            
            dependencyResolution.includes = [configurations.integrationTestCompile]
        }
    }
}

sourceSets {
    integrationTest
}
"""

        when:
        def result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(dir.root)
                .withPluginClasspath()
                .withArguments('crossBuild210Jar', 'crossBuild211Jar', '--info', '--stacktrace')
                .build()

        then:
        result.output.contains('_2.10\'')
        result.output.contains('_2.11\'')
        result.task(":crossBuild210Jar").outcome == SUCCESS
        result.task(":crossBuild211Jar").outcome == SUCCESS

        where:
        gradleVersion << ['2.14.1', '3.0', '4.1']
    }

    @Unroll
    def "[#gradleVersion] applying crossbuild plugin with default archiveAppendix and value for each specified targetVersion should throw when scalaVersions catalog is missing needed version"() {
        given:
        buildFile << """
plugins {
    id 'com.github.prokod.gradle-crossbuild'
}

crossBuild {
    builds {
        v213 {
            scalaVersion = '2.13'
        }
    }
}
"""

        when:
        GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(dir.root)
                .withPluginClasspath()
                .withArguments('crossBuild213Jar', '--info')
                .build()

        then:
        thrown(RuntimeException)

        where:
        gradleVersion << ['4.2', '4.9']
    }
}
