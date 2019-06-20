package com.github.prokod.gradle.crossbuild.model

import com.github.prokod.gradle.crossbuild.ScalaVersionInsights
import groovy.json.JsonOutput

/**
 * Immutable representation of build item inside {@code builds} DSL block after resolving has been made
 *
 * @see com.github.prokod.gradle.crossbuild.BuildResolver
 */
class ResolvedBuildAfterEvalLifeCycle extends ResolvedBuildConfigLifecycle {

    final ResolvedArchiveNaming archive

    ResolvedBuildAfterEvalLifeCycle(Build build, ScalaVersionInsights scalaVersionInsights,
                                    ResolvedArchiveNaming archive) {
        super(build, scalaVersionInsights)
        this.archive = archive
    }

    ResolvedBuildAfterEvalLifeCycle(ResolvedBuildConfigLifecycle resolvedBuild, ResolvedArchiveNaming archive) {
        super(resolvedBuild)
        this.archive = archive
    }

    String toString() {
        JsonOutput.toJson([name:name,
                           scalaVersion:scalaVersion,
                           archive:[appendixPattern:archive.appendixPattern,
                                     appendix:archive.appendix],
                           scalaVersionInsights:[baseVersion:scalaVersionInsights.baseVersion,
                                                  compilerVersion:scalaVersionInsights.compilerVersion,
                                                  artifactInlinedVersion:scalaVersionInsights.artifactInlinedVersion,
                                                  strippedArtifactInlinedVersion:scalaVersionInsights.
                                                          strippedArtifactInlinedVersion,
                                                  underscoredBaseVersion:scalaVersionInsights.underscoredBaseVersion,
                                                  underscoredCompilerVersion:scalaVersionInsights.
                                                          underscoredCompilerVersion,
                                                  underscoredArtifactInlinedVersion:scalaVersionInsights.
                                                          underscoredArtifactInlinedVersion]])
    }
}
