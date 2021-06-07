ThisBuild / baseVersion := "0.0"

ThisBuild / organization := "com.armanbilge"
ThisBuild / publishGithubUser := "armanbilge"
ThisBuild / publishFullName := "Arman Bilge"
ThisBuild / startYear := Some(2021)

mimaPreviousArtifacts := Set()

enablePlugins(SonatypeCiReleasePlugin)
ThisBuild / homepage := Some(url("https://github.com/armanbilge/oxidized"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/armanbilge/oxidized"),
    "git@github.com:armanbilge/oxidized.git"))
sonatypeCredentialHost := "s01.oss.sonatype.org"

val Scala213 = "2.13.6"
val Scala3 = "3.0.0"
ThisBuild / crossScalaVersions := Seq(Scala3, Scala213)

replaceCommandAlias(
  "ci",
  "; project /; headerCheckAll; scalafmtCheckAll; scalafmtSbtCheck; clean; testIfRelevant; mimaReportBinaryIssuesIfRelevant"
)
addCommandAlias("prePR", "; root/clean; +root/scalafmtAll; scalafmtSbt; +root/headerCreate")

val CatsVersion = "2.6.1"
val CatsEffectVersion = "3.1.1"
val CatsMtlVersion = "1.2.1"
val DisciplineVersion = "1.1.6"
val Specs2Version = "4.12.0"

lazy val root =
  project.aggregate(kernel, laws, std, core).enablePlugins(NoPublishPlugin)

lazy val kernel = project
  .in(file("kernel"))
  .settings(
    name := "oxidized-kernel",
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % CatsVersion,
      "org.typelevel" %% "cats-mtl" % CatsMtlVersion
    )
  )

lazy val laws = project
  .in(file("laws"))
  .dependsOn(kernel)
  .settings(
    name := "oxidized-laws",
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-laws" % CatsVersion,
      "org.typelevel" %% "discipline-specs2" % DisciplineVersion
    )
  )

lazy val std = project
  .in(file("std"))
  .dependsOn(kernel)
  .settings(
    name := "oxidized-std",
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect-kernel" % CatsEffectVersion
    )
  )

lazy val core = project
  .in(file("core"))
  .dependsOn(std, laws % Test)
  .settings(
    name := "oxidized",
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,
      "org.typelevel" %% "cats-mtl" % CatsMtlVersion,
      "org.typelevel" %% "cats-mtl-laws" % CatsMtlVersion % Test,
      "org.typelevel" %% "cats-effect-testkit" % CatsEffectVersion % Test,
      "org.typelevel" %% "cats-effect-testing-specs2" % "1.1.1" % Test,
      "org.typelevel" %% "discipline-specs2" % DisciplineVersion % Test
    )
  )
