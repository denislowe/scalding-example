import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

object BuildSettings {
  
  lazy val basicSettings = Seq[Setting[_]](
    version       := "0.0.1",
    description   := "scalding example",
    scalaVersion  := "2.9.2", 
    scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
    resolvers     ++= Seq(ScalaToolsSnapshots,"Concurrent Maven Repo" at "http://conjars.org/repo")
  )

  // sbt-assembly settings for building a fat jar
  import sbtassembly.Plugin._
  import AssemblyKeys._
  lazy val sbtAssemblySettings = assemblySettings ++ Seq(

    // Slightly cleaner jar name
    jarName in assembly <<= (name, version) { (name, version) => name + "-" + version + ".jar" },
    
    // Drop these jars
    excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      val excludes = Set(
        "jsp-api-2.1-6.1.14.jar",
        "jsp-2.1-6.1.14.jar",
        "jasper-compiler-5.5.12.jar",
        "minlog-1.2.jar", // Otherwise causes conflicts with Kyro (which bundles it)
        "janino-2.5.16.jar", // Janino includes a broken signature, and is not needed anyway
        "commons-beanutils-core-1.8.0.jar", // Clash with each other and with commons-collections
        "commons-beanutils-1.7.0.jar",      // "
        "hadoop-core-0.20.2.jar", // Provided by Amazon EMR. Delete this line if you're not on EMR
        "hadoop-tools-0.20.2.jar" // "
      ) 
      cp filter { jar => excludes(jar.data.getName) }
    },
    
    mergeStrategy in assembly <<= (mergeStrategy in assembly) {
      (old) => {
        case "project.clj" => MergeStrategy.discard // Leiningen build files
        case x => old(x)
      }
    }
  )

  lazy val buildSettings = basicSettings ++ sbtAssemblySettings
}

object ScaldingBatchProjectBuild extends Build {
  import BuildSettings._

  // Configure prompt to show current project
  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  }

  // Define our project, with basic project information and library dependencies
  lazy val project = Project("scalding-example", file("."))
    .settings(buildSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "com.twitter" %% "scalding-core" % "0.8.5",
        "org.apache.hadoop" % "hadoop-core" % "0.20.2",
        "org.slf4j" % "slf4j-log4j12" % "1.6.6",
        "org.json4s" %% "json4s-native" % "3.2.2",
        "com.typesafe" % "config" % "1.0.1",
        "org.scalatest" %% "scalatest" % "1.9.1" % "test", 
        "org.apache.commons" % "commons-math3" % "3.0"
      )
    )
    .settings( parallelExecution in Test := false )
}
