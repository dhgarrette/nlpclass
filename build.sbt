import com.typesafe.sbt.SbtStartScript

name := "nlpclass"

organization := "dhg"

version := "001"

scalaVersion := "2.10.3"

resolvers ++= Seq(
  "dhg releases repo" at "http://www.cs.utexas.edu/~dhg/maven-repository/releases",
  "dhg snapshot repo" at "http://www.cs.utexas.edu/~dhg/maven-repository/snapshots",
  "OpenNLP repo" at "http://opennlp.sourceforge.net/maven2"
)

libraryDependencies ++= Seq(
   "dhg" % "scala-util_2.10" % "1.0.0-SNAPSHOT" changing(),
   "edu.stanford.nlp" % "stanford-corenlp" % "3.2.0",
   "org.apache.opennlp" % "opennlp-tools" % "1.5.3",
   "org.abego.treelayout" % "org.abego.treelayout.netbeans" % "1.0.1" exclude("org.netbeans.api", "org-netbeans-api-visual"),
   "org.codeartisans.thirdparties.swing" % "org-netbeans-api-visual" % "2.23.1",
   "com.typesafe" % "scalalogging-log4j_2.10" % "1.0.1",
   "org.apache.logging.log4j" % "log4j-core" % "2.0-beta3",
   "junit" % "junit" % "4.10" % "test",
   "com.novocode" % "junit-interface" % "0.8" % "test->default"
  )

seq(SbtStartScript.startScriptForClassesSettings: _*)

SbtStartScript.stage in Compile := Unit

scalacOptions ++= Seq("-deprecation")

