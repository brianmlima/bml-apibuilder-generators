import scoverage.ScoverageKeys


name := "generator"
organization := "bml"

val scalaVer = "2.12.8"
scalaVersion in ThisBuild := scalaVer

lazy val generated = project
  .in(file("generated"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      ws,
      "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
    )
  )


// TODO: lib will eventually be published as a jar if it turns out
// that we need it. For now it is here mostly for reference - hoping
// we end up not needing it.
lazy val lib = project
  .in(file("lib"))
  .dependsOn(generated % "compile; test->test")
  .settings(commonSettings: _*)

lazy val generator = project
  .in(file("generator"))
  .dependsOn(javaGenerator, bmlLombokSpring, gqlschemaGenerator, bmlGeneratorShared, javaPersistanceSql, springServiceGenerator, springServiceTestGenerator, springCouldContractGenerator, bmlLombokTest, jpaRepository, javaClient, bmlOpenapi, javascriptPojos, javaRestTemplateClient)
  .aggregate(javaGenerator, bmlLombokSpring, gqlschemaGenerator, bmlGeneratorShared, javaPersistanceSql, springServiceGenerator, springServiceTestGenerator, springCouldContractGenerator, bmlLombokTest, jpaRepository, javaClient, bmlOpenapi, javascriptPojos, javaRestTemplateClient)
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    routesImport += "io.apibuilder.generator.v0.Bindables.Core._",
    routesImport += "io.apibuilder.generator.v0.Bindables.Models._",
    routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= Seq(
      ws,
      "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % "test"
    )
  )

lazy val gqlschemaGenerator = project
  .in(file("graphql-schema-generator"))
  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
  .settings(commonSettings: _*)
  .settings(Seq(ScoverageKeys.coverageMinimum := 66.98))

lazy val bmlGeneratorShared = project
  .in(file("bml-generator-shared"))
  .dependsOn(lib, lib % "test->test")
  .settings(commonSettings: _*)
  .settings(Seq(ScoverageKeys.coverageMinimum := 66.98))

lazy val bmlLombokTest = project
  .in(file("generators/bml-lombok-test"))
  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
  .settings(commonSettings: _*)
  .settings(Seq(ScoverageKeys.coverageMinimum := 66.98))

lazy val bmlOpenapi = project
  .in(file("generators/bml-openapi"))
  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
  .settings(commonSettings: _*)
  .settings(Seq(ScoverageKeys.coverageMinimum := 66.98))


lazy val javascriptPojos = project
  .in(file("generators/javascript-pojos"))
  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
  .settings(commonSettings: _*)
  .settings(Seq(ScoverageKeys.coverageMinimum := 66.98))

lazy val javaRestTemplateClient = project
  .in(file("generators/java-rest-template-client"))
  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
  .settings(commonSettings: _*)
  .settings(Seq(ScoverageKeys.coverageMinimum := 66.98))


lazy val springServiceGenerator = project
  .in(file("generators/spring-service"))
  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
  .settings(commonSettings: _*)
  .settings(Seq(ScoverageKeys.coverageMinimum := 66.98))

lazy val springServiceTestGenerator = project
  .in(file("generators/spring-service-test"))
  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
  .settings(commonSettings: _*)
  .settings(Seq(ScoverageKeys.coverageMinimum := 66.98))

lazy val springCouldContractGenerator = project
  .in(file("generators/spring-cloud-contract"))
  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
  .settings(commonSettings: _*)
  .settings(Seq(ScoverageKeys.coverageMinimum := 66.98))

lazy val javaGenerator = project
  .in(file("java-generator"))
  .dependsOn(lib, lib % "test->test")
  .settings(commonSettings: _*)
  .settings(Seq(ScoverageKeys.coverageMinimum := 66.98))

//lazy val bmlLombok = project
//  .in(file("bml-lombok"))
//  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
//  .settings(commonSettings: _*)
//  .settings(
//    Seq(ScoverageKeys.coverageMinimum := 69.5)
//  )
lazy val bmlLombokSpring = project
  .in(file("generators/bml-lombok"))
  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
  .settings(commonSettings: _*)
  .settings(
    Seq(ScoverageKeys.coverageMinimum := 69.5)
  )

lazy val jpaRepository = project
  .in(file("generators/jpa-repository"))
  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
  .settings(commonSettings: _*)
  .settings(
    Seq(ScoverageKeys.coverageMinimum := 69.5)
  )


lazy val javaPersistanceSql = project
  .in(file("bml-java-persistance-sql"))
  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
  .settings(commonSettings: _*)
  .settings(
    Seq(ScoverageKeys.coverageMinimum := 69.5)
  )

lazy val javaClient = project
  .in(file("generators/java-client"))
  .dependsOn(lib, lib % "test->test", bmlGeneratorShared)
  .settings(commonSettings: _*)
  .settings(
    Seq(ScoverageKeys.coverageMinimum := 69.5)
  )


val springFrameworkVersion = "5.2.1.RELEASE"
val springBootVersion = "2.2.1.RELEASE"

lazy val commonSettings: Seq[Setting[_]] = Seq(
  name ~= ("bml-generator-" + _),
  organization := "bml",
  ScoverageKeys.coverageFailOnMinimum := true,
  libraryDependencies ++= Seq(
    "org.atteo" % "evo-inflector" % "1.2.2",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "org.mockito" % "mockito-core" % "2.23.4" % "test",
    "com.github.javaparser" % "javaparser-core" % "3.8.3" % "test",
    "org.scala-lang" % "scala-compiler" % scalaVer % "test",
    "org.scalameta" %% "scalameta" % "4.0.0" % "test",
    "com.squareup" % "javapoet" % "1.12.1",
    "com.squareup.retrofit2" % "retrofit" % "2.5.0",
    "io.reactivex.rxjava2" % "rxjava" % "2.2.4",
    "com.graphql-java" % "graphql-java-tools" % "5.2.4",
    //            "javax.validation" % "validation-api" % "2.0.1.Final",
    "javax.persistence" % "javax.persistence-api" % "2.2",
    "org.projectlombok" % "lombok" % "1.18.4",
    "com.google.googlejavaformat" % "google-java-format" % "1.7",
    "org.javers" % "javers-spring" % "5.6.0",

    "org.junit.jupiter" % "junit-jupiter-api" % "5.6.0-M1",
    "org.junit.jupiter" % "junit-jupiter-engine" % "5.6.0-M1",

    "org.mockito" % "mockito-junit-jupiter" % "3.1.0",


    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.10.2",
    "org.yaml" % "snakeyaml" % "1.25",
    "javax.validation" % "validation-api" % "2.0.1.Final"


  ),
  libraryDependencies += guice,
  scalacOptions ++= Seq("-feature", "-Ycache-plugin-class-loader:last-modified", "-Ycache-macro-class-loader:last-modified"),
  sources in(Compile, doc) := Seq.empty,
  publishArtifact in(Compile, packageDoc) := false
)
version := "0.5.77"

