---
layout: default
title: My Scala Utilities Library
root: "../"
---

## Overview

I have created a libarary for all of the things that I find missing or broken in Scala.

To include this library, add the following to your `build.sbt`:

	resolvers ++= Seq(
	  "dhg releases repo" at "http://www.cs.utexas.edu/~dhg/maven-repository/releases",
	  "dhg snapshot repo" at "http://www.cs.utexas.edu/~dhg/maven-repository/snapshots"
	)

	libraryDependencies += "dhg" % "scala-util_2.10" % "1.0.0-SNAPSHOT" changing()

*NOTE:* This is already included in the `nlpclass-fall2013` dependency, so you do *not* need to include it again.

The [API](http://www.cs.utexas.edu/~dhg/maven-repository/snapshots/dhg/scala-util_2.10/1.0.0-SNAPSHOT/api/#package) is available online.

Some highlights of the library are below.  There is **much more** beyond what is described here.

## Collection extension methods

To use:
{% highlight scala %}
import dhg.util.CollectionUtil._
{% endhighlight %}

These methods are defined on as wide a type as makes sense.  Most work on `Iterator` as well.

### Basic math stuff
{% highlight scala %}
Vector(1,2,3,4).avg      // 2.5

Vector(1,2,1).normalize  // Vector(0.25, 0.5, 0.25)

Vector(('a,1), ('b,2), ('c,1)).normalizeValues
// Vector(('a,0.25), ('b,0.5), ('c,0.25))
{% endhighlight %}

### Counting
{% highlight scala %}
Vector('a, 'b, 'a, 'c, 'a, 'b).counts 
// Map('b -> 2, 'a -> 3, 'c -> 1)
{% endhighlight %}

### Grouping / Ungrouping
{% highlight scala %}
Vector(('a, 1), ('b, 2), ('b, 3), ('a, 4)).groupByKey
// Map('b -> Vector(2, 3), 'a -> Vector(1, 4))

val a = Vector(('a, Vector(1,2)), ('b, Vector(2,3))).ungroup  
// Iterator[(Symbol, Int)]
a.toVector  // Vector(('a,1), ('a,2), ('b,2), ('b,3))
{% endhighlight %}

### mapVals
This corrects the suprising behavior of `mapValues` that creates a *view* of a `Map`, which means that values are *recomputed* on each access.  My method `mapVals` always creates a new collection, not a view, so values are only computed once.
{% highlight scala %}
Vector(('a, 1), ('b, 2), ('b, 3)).mapVals(_ + 1)
// Vector(('a,2), ('b,3), ('b,4))
{% endhighlight %}

### maxByN / minByN
Get the `N` max or min results, sorted.  Prevents you from having to sort the whole collection or traverse it more than once.
{% highlight scala %}
Vector("be", "what", "a", "the").maxByN(_.size, 2) // Vector(what, the)
Vector("be", "what", "a", "the").minByN(_.size, 2) // Vector(a, be)
{% endhighlight %}

### split / splitWhere
Works on all `Seq`.  Allows you to keep the delimiter at the front or back.  Produces and `Iterator`.
{% highlight scala %}
import KeepDelimiter._
val a = Vector("A", "B", ".", "C", ".", "D").split(".")
// Iterator[Vector[String]]
a.toVector  // Vector(Vector(A, B), Vector(C), Vector(D))

val b = Iterator("A", "B", ".", "C", ".", "D")
          .splitWhere((_: String) == ".", KeepDelimiterAsFirst)
b.toVector  // Vector(Vector(A, B), Vector(., C), Vector(., D))
{% endhighlight %}

### splitAt on Iterator
Split `Iterator` in two.  Front must be traversed first.
{% highlight scala %}
val (a,b) = Iterator(1,2,3,4,5).splitAt(3)
a.toVector   // (1, 2, 3)
b.toVector   // Vector(4, 5)
{% endhighlight %}

### zipSafe
Throw an exception if the two parts are different sizes.
{% highlight scala %}
Vector(1,2,3) zipSafe Vector(1,2)   // ERROR!
{% endhighlight %}


## File utilities

To use:
{% highlight scala %}
import dhg.util.FileUtil._
{% endhighlight %}

### Self-closing file reader
{% highlight scala %}
File(filename).readLines.foreach(println)
// file now closed
{% endhighlight %}

### Self-closing file writing (and a `writeLine` method)
{% highlight scala %}
writeUsing(File(filename)) { 
  f => f.writeLine("something")
}
// file now closed
{% endhighlight %}


## Graphing

To use:
{% highlight scala %}
import dhg.util.viz._
{% endhighlight %}

Create a `Chart` and `draw()` it.

{% highlight scala %}
import java.awt.Color.{red, lightGray}
Chart(
    Histogram(Vector(0, 4, 5, 1, 1.5, 3), 3, lightGray), 
    ScatterGraph(Vector((0.5, 0.6), (1,2), (4,3), (2.5,2.7))), 
    LineGraph(Vector((1,1), (3,3), (5,4)), red))
  .draw(exitOnClose = false)
{% endhighlight %}

