package com.github.prokod.gradle.crossbuild.model

import com.github.prokod.gradle.crossbuild.ScalaVersions
import groovy.transform.EqualsAndHashCode

import java.util.regex.Pattern

/**
 * Represents a parsed dependency name.
 *
 */
@EqualsAndHashCode(excludes = ['appendix'])
class DependencyLimitedInsight {
    String baseName
    String supposedScalaVersion
    String appendix

    /**
     * Parses given dependency name to its baseName part and its scala version part.
     * returns the dependency name unparsed if dependency name does not contain separating char '_'
     *
     * @param depName dependency name to parse
     * @parma scalaVersions
     * @return tuple in the form of (baseName, scalaVersion, appendix) i.e. ('lib', '2.11', '2.2.0')
     *         returns (name, {@code null}, {@code null}) otherwise.
     */
    static DependencyLimitedInsight parseByDependencyName(String name, ScalaVersions scalaVersions, String scalaTag) {
        def refTargetVersions = scalaVersions.mkRefTargetVersions()
        def qMarkDelimiter = Pattern.quote(scalaTag)
        def qMarkSplitPattern = "(?=(?!^)$qMarkDelimiter)|(?<=$qMarkDelimiter)"
        def qMarkTokens = name.split(qMarkSplitPattern)
        def qMarkParsedTuple =  parseTokens(qMarkTokens,scalaTag)
        def parsedTuples = refTargetVersions.findAll{version -> '_' + version != scalaTag}.collect { version ->
            def delimiter = Pattern.quote('_' + version)
            def splitPattern = "(?=(?!^)$delimiter)|(?<=$delimiter)"
            def tokens = name.split(splitPattern)
            parseTokens(tokens, scalaTag)
        }
        def allParsedTuples = parsedTuples + [qMarkParsedTuple]
        def filtered = allParsedTuples.findAll { it != null }.toSet()
        if (filtered.size() == 1) {
            def tuple = filtered.head()
            new DependencyLimitedInsight(baseName:tuple[0], supposedScalaVersion:tuple[1], appendix:tuple[2])
        }
        else {
            new DependencyLimitedInsight(baseName:name, supposedScalaVersion:null, appendix:null)
        }
    }

    private static Tuple parseTokens(String[] tokens, String scalaTag) {
        def scalaTagVersion = scalaTag.substring(1)
        if (tokens.size() < 2) {
            null
        }
        else if (tokens.size() == 2) {
            def baseName = tokens[0]
            def supposedScalaVersion = tokens[1].substring(1)
            if(supposedScalaVersion == scalaTagVersion){
                new Tuple(baseName, '?', null)
            } else {
                new Tuple(baseName, supposedScalaVersion, null)
            }
        }
        else if (tokens.size() == 3) {
            def baseName = tokens[0]
            def supposedScalaVersion = tokens[1].substring(1)
            def appendix = tokens[2]
            if(supposedScalaVersion == scalaTagVersion){
                new Tuple(baseName, '?', null)
            } else {
                new Tuple(baseName, supposedScalaVersion, appendix)
            }
        }
        else {
            null
        }
    }
}
