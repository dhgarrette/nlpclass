---
layout: default
title: Part 1 - Function Composition in Scala
root: "../../"
---

So, the goal of this post is to show how a list of functions can be composed to create a single function, in the context of mapping a set of values using those functions. It’s a cute example that shows off some of the goodness that comes with functional programming in Scala. And, while this isn’t a tutorial, it might still be useful for people who are just getting into functional programming.

We’ll start with the list of numbers 1 to 5 and some simple functions — one for adding 1, another for squaring, and third for adding 100.

{% highlight scala %}
scala> val foo = (1 to 5).toList
foo: List[Int] = List(1, 2, 3, 4, 5)
 
scala> val add1 = (x: Int) => x + 1
add1: (Int) => Int = <function1>
 
scala> val add100 = (x: Int) => x + 100
add100: (Int) => Int = <function1>
 
scala> val sq = (x: Int) => x * x
sq: (Int) => Int = <function1>
{% endhighlight %}

We can then apply any of these functions to each element in the list foo by using the map function.

{% highlight scala %}
scala> foo.map(add1)
res0: List[Int] = List(2, 3, 4, 5, 6)
 
scala> foo.map(add100)
res1: List[Int] = List(101, 102, 103, 104, 105)
 
scala> foo.map(sq)
res2: List[Int] = List(1, 4, 9, 16, 25)
{% endhighlight %}

We can save the results of mapping all the values through add1, and then map the resulting list through sq.

{% highlight scala %}
scala> val bar = foo.map(add1)
bar: List[Int] = List(2, 3, 4, 5, 6)
 
scala> bar.map(sq)
res3: List[Int] = List(4, 9, 16, 25, 36)
{% endhighlight %}

Or, if we don’t care about the intermediate result, we can just keep on mapping, through both functions.

{% highlight scala %}
scala> foo.map(add1).map(sq)
res4: List[Int] = List(4, 9, 16, 25, 36)
{% endhighlight %}

What we just did, above, was sq(add1(x)) for every x in the list foo. We could have instead composed the two functions, since sq(add1(x)) = sqοadd1(x). Here’s what it looks like in Scala:

{% highlight scala %}
scala> val sqComposeAdd1 = sq.compose(add1)
sqComposeAdd1: (Int) => Int = <function1>
 
scala> foo.map(sqComposeAdd1)
res5: List[Int] = List(4, 9, 16, 25, 36)
{% endhighlight %}

Of course, we can do this with more than two functions.

{% highlight scala %}
scala> foo.map(add1).map(sq).map(add100)
res6: List[Int] = List(104, 109, 116, 125, 136)
 
scala> foo.map(add100 compose sq compose add1)
res7: List[Int] = List(104, 109, 116, 125, 136)
{% endhighlight %}

And so on. Now, imagine that you want the user of a program you’ve written to be able to select the functions they want to apply to a list of items, perhaps from a set of predefined functions you’ve provided plus perhaps ones they are themselves defining. So, here’s the really useful part: we can compose that arbitrary bunch of functions on the fly to turn them into a single function, without having to write out “compose … compose … compose…” or “map … map … map …” We do this by building up a list of the functions (in the order we want to apply them to the values) and then reducing them using the compose function. Equivalent to what we had above:

{% highlight scala %}
scala> val fncs = List(add1, sq, add100)
fncs: List[(Int) => Int] = List(<function1>, <function1>, <function1>)
 
scala> foo.map(fncs.reverse.reduce(_ compose _))
res8: List[Int] = List(104, 109, 116, 125, 136)
{% endhighlight %}

Notice the that it was necessary to reverse the list in order for the composition to be ordered correctly. If you don’t feel like doing that, you can use andThen in Scala.

{% highlight scala %}
scala> foo.map(add1 andThen sq andThen add100)
res9: List[Int] = List(104, 109, 116, 125, 136)
{% endhighlight %}

Which we can of course use with reduce as well.

{% highlight scala %}
scala> foo.map(fncs.reduce(_ andThen _))
res10: List[Int] = List(104, 109, 116, 125, 136)
{% endhighlight %}

Since functions are first class citizens (something we used several times above), we can assign the composed or andThened result to a val and use it directly.

{% highlight scala %}
scala> val superFunction = fncs.reduce(_ andThen _)
superFunction: (Int) => Int = <function1>

scala> foo.map(superFunction)
res11: List[Int] = List(104, 109, 116, 125, 136)
{% endhighlight %}

This example is of course artificial, but the general pattern works nicely with much more complex/interesting functions and can provide a nice way of configuring a bunch of alternative functions for different use cases.
