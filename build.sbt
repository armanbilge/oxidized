ThisBuild / tlBaseVersion := "0.1"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2021)

ThisBuild / tlSonatypeUseLegacyHost := false

val Scala212 = "2.12.15"
val Scala213 = "2.13.8"
val Scala3 = "3.1.1"
ThisBuild / crossScalaVersions := Seq(Scala3, Scala212, Scala213)

val CatsVersion = "2.7.0"
val CatsEffectVersion = "3.3.5"
val CatsMtlVersion = "1.2.1"
val DisciplineVersion = "1.1.6"
val Specs2Version = "4.13.3"

lazy val root = tlCrossRootProject.aggregate(kernel, laws, std, core)

lazy val kernel = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("kernel"))
  .settings(
    name := "oxidized-kernel",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % CatsVersion,
      "org.typelevel" %%% "cats-mtl" % CatsMtlVersion
    )
  )

lazy val laws = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("laws"))
  .dependsOn(kernel)
  .settings(
    name := "oxidized-laws",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-laws" % CatsVersion,
      "org.typelevel" %%% "discipline-specs2" % DisciplineVersion
    )
  )

lazy val std = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("std"))
  .dependsOn(kernel)
  .settings(
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-kernel" % CatsEffectVersion
    )
  )

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .dependsOn(std, laws % Test)
  .settings(
    name := "oxidized",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
      "org.typelevel" %%% "cats-mtl" % CatsMtlVersion,
      "org.typelevel" %%% "cats-mtl-laws" % CatsMtlVersion % Test,
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test,
      "org.typelevel" %%% "cats-effect-testing-specs2" % "1.4.0" % Test,
      "org.typelevel" %%% "discipline-specs2" % DisciplineVersion % Test
    )
  )
