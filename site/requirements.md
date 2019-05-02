---
layout: default
title: Assignment Requirements
---

## Overview

This page describes the expectations for how you will prepare your coding assignments and submit them for grading.




## GitHub

Your code should be hosted in a *private* GitHub repository.  Follow these instructions:

1. If you do not have an account on GitHub, you need to create one here: [github.com](https://github.com/).
2. Register as a student here: [github.com/edu](https://github.com/edu).  This will give you five free private repositories, one of which will be used for this class.
3. Create a new repository for your classwork called 
    {% highlight text %}nlpclass-fall2013-lastname-firstname{% endhighlight %}
    by clicking on "New Repository" on the GitHub website.  Be sure to select ***PRIVATE*** repository.
4. Add me as a "collaborator".  Do this from the GitHub webpage for your repository: `Settings` `->` `Collaborators` `->` `Add a friend` and enter my username: `dhgarrette`.
5. Clone your repositiory.
    {% highlight text %}$ git clone git@github.com:USERNAME/REPOSITORY-NAME.git{% endhighlight %}
6. Follow the instructions on the [Scala Environment Setup]({{ page.root }}scala/setup.html) page to create a scala project in your repository directory.
7. Create a file `.gitignore` in the root of your repository that contains this:

		*.class
		*.log
		.DS_STORE

		# sbt specific
		dist/*
		target/
		lib_managed/
		src_managed/
		project/boot/
		project/plugins/project/

		# Scala-IDE specific
		.scala_dependencies
		.classpath
		.project
		.settings/
		.cache

8. Add the following to your main `build.sbt`:
    {% highlight text %}
resolvers ++= Seq(
  "dhg releases repo" at "http://www.cs.utexas.edu/~dhg/maven-repository/releases",
  "dhg snapshot repo" at "http://www.cs.utexas.edu/~dhg/maven-repository/snapshots"
)
    
libraryDependencies += "dhg" % "nlpclass_2.10" % "001" changing(){% endhighlight %}

    This creates a dependency from your project to the course project code, which exists online.  I will use this project to provide code to you that your code can access.

    If you use Eclipse, then you will need to re-run `sbt "eclipse with-source=true"` and refresh the project from within Eclipse before you will see the changes.

    Whenever I update this dependency, I will push a new version online with a new version number.  The first version number is `001`, and I will tell you each time I increment it.  When I do, you will have to correspondingly update the number in this file.


## Turning in your assignments

The code for your assignments will be "turned in" via your GitHub repo.  You should probably develop it there too, checking things in as you work so that you don't accidentally lose your work. (And you should definitely not develop it in a *public* repository!)

For each assignment, I will give you instructions on what to call certain classes, but the rest of the structure of your up to you.  You will, however, have to document the files used in a README for each assignment to make grading easier.  Each README file will need to be in the root of your project repository, and named, for example, `README_a0.md` for assignment 0.

You will turn in your code via a Git "tag", indicating to us the state of the code that you want graded.  When you are ready to turn in an assignment, you will tag your code with the name of the assignment (e.g. `a0`).  You can do this with

    git tag a0
    git push origin a0

Please verify that your tag worked by checking it out yourself and making sure everything runs as expected:

	cd <some_other_directory>
	git clone <my_repository_name>
	cd <repository_directory>
	git checkout tags/a0
	sbt ...

If you want to change your assignment code after you have already tagged it, you will have to delete the existing tag before recreating it with the usual steps:

    git tag -d a0

The timestamp of the tagged commit will be used to determine lateness.  


## Running Speed

No program should take more than two minutes to run.  Most should run in only a few seconds.  If your code runs too slowly, points will be deducted:

* 2 points off per problem that runs between 2 and 5 min
* 5 points off per problem that runs between 5 and 10 min
* No credit for any problem that takes longer than 10 minutes to run.

There are two main reasons for the time limit.
 
1) You should be thinking about time complexity and speed somewhat (but I don't want you obsessing about it.)
 
2) We can't have it take dozens of hours just to run the code for grading.
 

