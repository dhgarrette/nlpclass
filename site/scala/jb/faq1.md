---
layout: default
title: Questions from Students 1
root: "../../"
---

## Preface

I’m currently teaching a course on Applied Text Analysis and am using Scala as the programming language taught and used in the course. Rather than creating more tutorials, I figured I’d take a page from Brian Dunning’s playbook on his Skeptoid podcast (highly recommended) when he takes student questions.  So, I had the students in the course submit questions about Scala that they had, based on the readings and assignments thus far. This post covers over half of them — the rest will be covered in a follow up post.

I start with some of the more basic questions, and the questions and/or answers progressively get into more intermediate level topics. Suggestions and comments to improve any of the answers are very welcome!


## Basic Questions

**Q. Concerning addressing parts of variables: To address individual parts of lists, the numbering of the items is (List 0,1,2 etc.) That is, the first element is called “0″. It seems to be the same for Arrays and Maps, but not for Tuples- to get the first element of a Tuple, I need to use Tuple._1. Why is that?**

The most important thing to remember here is that tuples are *not* sequences, and, thus, do not need to have the same conventions.  Tuples have used a 1-based index in other languages like Haskell, and it seems that Scala has adopted the same convention/tradition. See:

[http://stackoverflow.com/questions/6241464/why-are-the-indexes-of-scala-tuples-1-based](http://stackoverflow.com/questions/6241464/why-are-the-indexes-of-scala-tuples-1-based)


**Q. It seems that Scala doesn’t recognize the “b” boundary character as a regular expression.  Is there something similar in Scala?**

Scala does recognize boundary characters. For example, the following REPL session declares a regex that finds “the” with boundaries, and successfully retrieves the three tokens of “the” in the example sentence.

{% highlight scala %}
scala> val TheRE = """\bthe\b""".r
TheRE: scala.util.matching.Regex = \bthe\b
 
scala> val sentence = "She think the man is a stick-in-the-mud, but the man disagrees."
sentence: java.lang.String = She think the man is a stick-in-the-mud, but the man disagrees.
 
scala> TheRE.findAllIn(sentence).toList
res1: List[String] = List(the, the, the)
{% endhighlight %}


**Q. Why doesn’t the method “split” work on args? Example: val arg = args.split(” “). Args are strings right, so split should work?**

The `args` variable is an Array, so split doesn’t work on them. Arrays are, in effect, already split.


**Q. What is the major difference between foo.mapValues(x=>x.length) and foo.map(x=>x.length). Some places one works and one does not.**

The `map` method works on all sequence types, including Seqs and Maps (note that Maps can be seen as sequences of Tuple2s). The `mapValues` method, however, only works on collections of `Tuple2` (such as `Map`). It is essentially a convenience function. As an example, let’s start with a simple Map from Ints to Ints.

{% highlight scala %}
scala> val foo = List((1,2),(3,4)).toMap
foo: scala.collection.immutable.Map[Int,Int] = Map(1 -> 2, 3 -> 4)
{% endhighlight %}

Now consider the task of adding 2 to each value in the Map. This can be done with the `map` method as follows.

{% highlight scala %}
scala> foo.map { case(key,value) => (key,value+2) }
res5: scala.collection.immutable.Map[Int,Int] = Map(1 -> 4, 3 -> 6)
{% endhighlight %}

So, the `map` method iterates over key/value pairs. We need to match both of them, and then output the key and the changed value to create the new Map. The `mapValues` method makes this quite a bit easier.

{% highlight scala %}
scala> foo.mapValues(2+)
res6: scala.collection.immutable.Map[Int,Int] = Map(1 -> 4, 3 -> 6)
{% endhighlight %}

Returning to the question about computing the length using `mapValues` or `map` — then it is just a question of which values you are transforming, as in the following examples.

{% highlight scala %}
scala> val sentence = "here is a sentence with some words".split(" ").toList
sentence: List[java.lang.String] = List(here, is, a, sentence, with, some, words)
 
scala> sentence.map(_.length)
res7: List[Int] = List(4, 2, 1, 8, 4, 4, 5)
 
scala> val firstCharTokens = sentence.groupBy(x=>x(0))
firstCharTokens: scala.collection.immutable.Map[Char,List[java.lang.String]] = Map(s -> List(sentence, some), a -> List(a), i -> List(is), h -> List(here), w -> List(with, words))
 
scala> firstCharTokens.mapValues(_.length)
res9: scala.collection.immutable.Map[Char,Int] = Map(s -> 2, a -> 1, i -> 1, h -> 1, w -> 2)
{% endhighlight %}


**Q. Is there any function that splits a list into two lists with the elements in the alternating positions of the original list? For example,**

**MainList =(1,2,3,4,5,6)**

**List1 = (1,3,5)**  
**List2 = (2,4,6)**

Given the exact main list you provided, one can use the partition function and use the modulo operation to see whether the value is divisible evenly by 2 or not.

{% highlight scala %}
scala> val mainList = List(1,2,3,4,5,6)
mainList: List[Int] = List(1, 2, 3, 4, 5, 6)
 
scala> mainList.partition(_ % 2 == 0)
res0: (List[Int], List[Int]) = (List(2, 4, 6),List(1, 3, 5))
{% endhighlight %}

So, partition returns a pair of Lists. The first has all the elements that match the condition and the second has all the ones that do not.

Of course, this wouldn’t work in general for Lists that have Strings, or that don’t have Ints in order, etc. 

For sequences of abitrarily-typed items, we can use the `grouped` and `unzip` methods:

{% highlight scala %}
scala> val unordered = List("b","2","a","4","z","8")
unordered: List[java.lang.String] = List(b, 2, a, 4, z, 8)
 
scala> unordered.grouped(2).toVector
res1: Vector[List[String]] = Vector(List(b, 2), List(a, 4), List(z, 8))

scala> unordered.grouped(2).map { case Seq(a,b) => (a,b) }.toVector
res2: Vector[(String, String)] = Vector((b,2), (a,4), (z,8)) 

scala> val (odds, evens) = unordered.grouped(2).map { case Seq(a,b) => (a,b) }.toVector.unzip
odds: scala.collection.immutable.Vector[String] = Vector(b, a, z)
evens: scala.collection.immutable.Vector[String] = Vector(2, 4, 8)
{% endhighlight %}


**Q. How to convert a List to a Vector and vice-versa?**

Use `toVector` and `toList`.

{% highlight scala %}
scala> val foo = List(1,2,3,4)
foo: List[Int] = List(1, 2, 3, 4)
 
scala> val bar = foo.toVector
bar: scala.collection.immutable.Vector[Int] = Vector(1, 2, 3, 4)
 
scala> val baz = bar.toList
baz: List[Int] = List(1, 2, 3, 4)
 
scala> foo == baz
res0: Boolean = true
{% endhighlight %}


**Q. The advantage of a vector over a list is the constant time look-up. What is the advantage of using a list over a vector?**

A List is slightly faster for operations at the head (front) of the sequence, so if all you are doing is doing a traversal (accessing each element in order, e.g. when mapping), then Lists are perfectly adequate and may be more efficient. They also have some nice pattern matching behavior for case statements.

However, common wisdom seems to be that you should default to using Vectors. See Daniel Spiewak’s nice answer on Stackoverflow:

[http://stackoverflow.com/questions/6928327/when-should-i-choose-vector-in-scala](http://stackoverflow.com/questions/6928327/when-should-i-choose-vector-in-scala)


**Q. With splitting strings, holmes.split(“\\s”) – \n and \t just requires a single ‘\’ to recognize its special functionality but why two ‘\’s are required for white space character?**

That’s because `\n` and `\t` actually mean something in a String.

{% highlight scala %}
scala> println("Here is a line with a tab\tor\ttwo, followed by\na new line.")
Here is a line with a tab    or    two, followed by
a new line.
 
scala> println("This will break\s.")
<console>:1: error: invalid escape character
println("This will break\s.")
{% endhighlight %}

So, you are supplying a String argument to split, and it uses that to construct a regular expression. Given that \s is not a string character, but is a regex metacharacter, you need to escape it. You can of course use `split(“”"\s”"”)`, though that isn’t exactly better in this case.


**Q. I have long been programming in C++ and Java. Therefore, I put semicolon at the end of the line unconsciously. It seems that the standard coding style of Scala doesn’t recommend to use semicolons. However, I saw that there are some cases that require semicolons as you showed last class. Is there any specific reason why semicolon loses its role in Scala?**

The main reason is to improve readability since the semicolon is rarely needed when writing standard code in editors (as opposed to one liners in the REPL). However, when you want to do something in a single line, like handling multiple cases, you need the semicolons.

{% highlight scala %}
scala> val foo = List("a",1,"b",2)
foo: List[Any] = List(a, 1, b, 2)
 
scala> foo.map { case(x: String) => x; case(x: Int) => x.toString }
res5: List[String] = List(a, 1, b, 2)
{% endhighlight %}

But, in general, it’s best to just split these cases over multiple lines in any actual code.


**Q. Is there no way to use _ in map like methods for collections that consist of pairs? For example, List((1,1),(2,2)).map(e => e._1 + e._2) works, but List((1,1),(2,2)).map(_._1 + _._2) does not work.**

The scope in which the `_` remains unanambigious runs out past its first invocation, so you only get to use it once. It is better anyway to use a case statement that makes it clear what the members of the pairs are.

{% highlight scala %}
scala>  List((1,1),(2,2)).map { case(num1, num2) => num1+num2 }
res6: List[Int] = List(2, 4)
{% endhighlight %}


**Q. I am unsure about the exact meaning of and the difference between “=>” and “->”. They both seem to mean something like “apply X to Y” and I see that each is used in a particular context, but what is the logic behind that?**

The use of -> simply constructs a Tuple2, as is pretty clear in the following snippet.

{% highlight scala %}
scala> val foo = (1,2)
foo: (Int, Int) = (1,2)
 
scala> val bar = 1->2
bar: (Int, Int) = (1,2)
 
scala> foo == bar
res11: Boolean = true
{% endhighlight %}

Primarily, it is syntactic sugar that provides an intuitive symbol for creating elements of a a Map. Compare the following two ways of declaring the same Map.

{% highlight scala %}
scala> Map(("a",1),("b",2))
res9: scala.collection.immutable.Map[java.lang.String,Int] = Map(a -> 1, b -> 2)
 
scala> Map("a"->1,"b"->2)
res10: scala.collection.immutable.Map[java.lang.String,Int] = Map(a -> 1, b -> 2)
The second seems more readable to me.
{% endhighlight %}

The use of => indicates that you are defining a function. The basic form is ARGUMENTS => RESULT.

{% highlight scala %}
scala> val addOne = (x: Int) => x+1
addOne: Int => Int = <function1>
 
scala> addOne(2)
res7: Int = 3
 
scala> val addTwoNumbers = (num1: Int, num2: Int) => num1+num2
addTwoNumbers: (Int, Int) => Int = <function2>
 
scala> addTwoNumbers(3,5)
res8: Int = 8
{% endhighlight %}

Normally, you use it in defining anonymous functions as arguments to functions like `map`, `filter`, and such.


**Q. Is there a more convenient way of expressing vowels as [AEIOUaeiou] and consonants as [BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz] in RegExes?**

You can use Strings when defining regexes, so you can have a variable for vowels and one for consonants.

{% highlight scala %}
scala> val vowel = "[AEIOUaeiou]"
vowel: java.lang.String = [AEIOUaeiou]
 
scala> val consonant = "[BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz]"
consonant: java.lang.String = [BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz]
 
scala> val MyRE = ("("+vowel+")("+consonant+")("+vowel+")").r
MyRE: scala.util.matching.Regex = ([AEIOUaeiou])([BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz])([AEIOUaeiou])
 
scala> val MyRE(x,y,z) = "aJE"
x: String = a
y: String = J
z: String = E
{% endhighlight %}


**Q. The “\b” in RegExes marks a boundary, right? So, it also captures the “-”. But if I have a single string “sdnfeorgn”, it does NOT capture the boundaries of that, is that correct? And if so, why doesn’t it?**

Because there are no boundaries in that string!


## Intermediate questions

**Q. The flatMap function takes lists of lists and merges them to single list. But in the example:**

{% highlight scala %}
scala> (1 to 10).toList.map(x=>squareOddNumber(x))
res16: List[Option[Int]] = List(Some(1), None, Some(9), None, Some(25), None, Some(49), None, Some(81), None)
 
scala> (1 to 10).toList.flatMap(x=>squareOddNumber(x))
res17: List[Int] = List(1, 9, 25, 49, 81)
{% endhighlight %}

**Here it is not list of list but just a list. In this case it expects the list to be Option list.**

**I tried running the code with function returning just number or None. It showed error. So is there any way to use flatmap without Option lists and just list. For example, List(1, None, 9, None, 25) should be returned as List(1, 9, 25).**

A. No, this won’t work because `List(1, None, 9, None, 25)` mixes `Option` with `Int` items.

{% highlight scala %}
scala> val mixedup = List(1, None, 9, None, 25)
mixedup: List[Any] = List(1, None, 9, None, 25)
{% endhighlight %}

So, you should have your function return an `Option` which means returning `Some` or `None`. Then `flatMap` will work happily.

One way of think of `Option` is that they are like `List` with zero or one element, as can be noted by the parallels in the following snippet.

{% highlight scala %}
scala> val foo = List(List(1), Nil, List(3), List(6), Nil)
foo: List[List[Int]] = List(List(1), List(), List(3), List(6), List())
 
scala> foo.flatten
res12: List[Int] = List(1, 3, 6)
 
scala> val bar = List(Option(1), None, Option(3), Option(6), None)
bar: List[Option[Int]] = List(Some(1), None, Some(3), Some(6), None)
 
scala> bar.flatten
res13: List[Int] = List(1, 3, 6)
{% endhighlight %}


**Q. Does scala have generic templates (like C++, Java)? Is that possible in scala? If so, how?**

Yes, every collection type is parameterized. Notice that each of the following variables is parameterized by the type of the elements they are initialized with.

{% highlight scala %}
scala> val foo = List(1,2,3)
foo: List[Int] = List(1, 2, 3)
 
scala> val bar = List("a","b","c")
bar: List[java.lang.String] = List(a, b, c)
 
scala> val baz = List(true, false, true)
baz: List[Boolean] = List(true, false, true)
{% endhighlight %}

You can create your own parameterized classes straightforwardly.

{% highlight scala %}
scala> class Flexible[T] (val data: T)
defined class Flexible
 
scala> val foo = new Flexible(1)
foo: Flexible[Int] = Flexible@7cd0570e
 
scala> val bar = new Flexible("a")
bar: Flexible[java.lang.String] = Flexible@31b6956f
 
scala> val baz = new Flexible(true)
baz: Flexible[Boolean] = Flexible@5b58539f
 
scala> foo.data
res0: Int = 1
 
scala> bar.data
res1: java.lang.String = a
 
scala> baz.data
res2: Boolean = true
{% endhighlight %}


**Q. How can we easily create, initialize and work with multi-dimensional arrays (and dictionaries)?**

Use the `fill` function of the `Array` object to create them.

{% highlight scala %}
scala> Array.fill(2)(1.0)
res8: Array[Double] = Array(1.0, 1.0)
 
scala> Array.fill(2,3)(1.0)
res9: Array[Array[Double]] = Array(Array(1.0, 1.0, 1.0), Array(1.0, 1.0, 1.0))
 
scala> Array.fill(2,3,2)(1.0)
res10: Array[Array[Array[Double]]] = Array(Array(Array(1.0, 1.0), Array(1.0, 1.0), Array(1.0, 1.0)), Array(Array(1.0, 1.0), Array(1.0, 1.0), Array(1.0, 1.0)))
{% endhighlight %}

Once you have these in hand, you can iterate over them as usual.

{% highlight scala %}
scala> val my2d = Array.fill(2,3)(1.0)
my2d: Array[Array[Double]] = Array(Array(1.0, 1.0, 1.0), Array(1.0, 1.0, 1.0))
 
scala> my2d.map(row => row.map(x=>x+1))
res11: Array[Array[Double]] = Array(Array(2.0, 2.0, 2.0), Array(2.0, 2.0, 2.0))
{% endhighlight %}

For dictionaries (Maps), you can use mutable HashMaps to create an empty Map and then add elements to it. For that, see this blog post:

[http://bcomposes.wordpress.com/2011/09/19/first-steps-in-scala-for-beginning-programmers-part-8/](http://bcomposes.wordpress.com/2011/09/19/first-steps-in-scala-for-beginning-programmers-part-8/)


**Q. Is the apply function similar to constructor in C++, Java? Where will the apply function be practically used? Is it for intialising values of attributes?**

No, the `apply` function is like any other function except that it allows you to call it without writing out “apply”. Consider the following class.

{% highlight scala %}
class AddX (x: Int) {
  def apply(y: Int) = x+y
  override def toString = "My number is " + x
}
{% endhighlight %}

Here’s how we can use it.

{% highlight scala %}
scala> val add1 = new AddX(1)
add1: AddX = My number is 1
 
scala> add1(4)
res0: Int = 5
 
scala> add1.apply(4)
res1: Int = 5
 
scala> add1.toString
res2: java.lang.String = My number is 1
{% endhighlight %}

So, the `apply` method is just (very handy) syntactic sugar that allows you to specify one function as fundamental to a class you have designed (actually, you can have multiple `apply` methods as long as each one has a unique parameter list). For example, with Lists, the `apply` method returns the value at the index provided, and for Maps it returns the value associated with the given key.

{% highlight scala %}
scala> val foo = List(1,2,3)
foo: List[Int] = List(1, 2, 3)
 
scala> foo(2)
res3: Int = 3
 
scala> foo.apply(2)
res4: Int = 3
 
scala> val bar = Map(1->2,3->4)
bar: scala.collection.immutable.Map[Int,Int] = Map(1 -> 2, 3 -> 4)
 
scala> bar(1)
res5: Int = 2
 
scala> bar.apply(1)
res6: Int = 2
{% endhighlight %}


**Q. In the SBT tutorial you discuss “Node” and “Value” as being case classes. What is the alternative to a case class?**

A normal class. Case classes are the special case. They do two things (and more) for you. The first is that you don’t have to use “new” to create a new object. Consider the following otherwise identical classes.

{% highlight scala %}
scala> class NotACaseClass (val data: Int)
defined class NotACaseClass
 
scala> case class IsACaseClass (val data: Int)
defined class IsACaseClass
 
scala> val foo = new NotACaseClass(4)
foo: NotACaseClass = NotACaseClass@a5c0f8f
 
scala> val bar = IsACaseClass(4)
bar: IsACaseClass = IsACaseClass(4)
{% endhighlight %}

That may seem like a little thing, but it can significantly improve code readability. Consider creating Lists within Lists within Lists if you had to use “new” all the time, for example. This is definitely true for `Node` and `Value`, which are used to build trees.

Case classes also support matching, as in the following.

{% highlight scala %}
scala> val IsACaseClass(x) = bar
x: Int = 4
{% endhighlight %}

A normal class cannot do this.

{% highlight scala %}scala> val NotACaseClass(x) = foo
<console>:13: error: not found: value NotACaseClass
val NotACaseClass(x) = foo
^
<console>:13: error: recursive value x needs type
val NotACaseClass(x) = foo
^
{% endhighlight %}

If you mix the case class into a List and map over it, you can match it like you can with other classes, like Lists and Ints. Consider the following heterogeneous List.

{% highlight scala %}
scala> val stuff = List(IsACaseClass(3), List(2,3), IsACaseClass(5), 4)
stuff: List[Any] = List(IsACaseClass(3), List(2, 3), IsACaseClass(5), 4)
{% endhighlight %}

We can convert this to a List of Ints by processing each element according to its type by matching.

{% highlight scala %}
scala> stuff.map { case List(x,y) => x; case IsACaseClass(x) => x; case x: Int => x }
<console>:13: warning: match is not exhaustive!
missing combination              *           Nil             *             *
 
stuff.map { case List(x,y) => x; case IsACaseClass(x) => x; case x: Int => x }
^
 
warning: there were 1 unchecked warnings; re-run with -unchecked for details
res10: List[Any] = List(3, 2, 5, 4)
{% endhighlight %}

If you don’t want to see the warning in the REPL, add a case for things that don’t match that throws a MatchError.

{% highlight scala %}scala> stuff.map { case List(x,y) => x; case IsACaseClass(x) => x; case x: Int => x; case _ => throw new MatchError }
warning: there were 1 unchecked warnings; re-run with -unchecked for details
res13: List[Any] = List(3, 2, 5, 4)
{% endhighlight %}

Better yet, return Options (using None for the unmatched case) and flatMapping instead.

{% highlight scala %}
scala> stuff.flatMap { case List(x,y) => Some(x); case IsACaseClass(x) => Some(x); case x: Int => Some(x); case _ => None }
warning: there were 1 unchecked warnings; re-run with -unchecked for details
res14: List[Any] = List(3, 2, 5, 4)
{% endhighlight %}


**Q. In C++ the default access specifier is private; in Java one needs to specify private or public for each class member where as in Scala the default access specifier for a class is public. What could be the design motivation behind this when one of the purpose of the class is data hiding?**

The reason is that Scala has a much more refined access specification scheme than Java that makes `public` the rational choice. See the discussion here:

[http://stackoverflow.com/questions/4656698/default-public-access-in-scala](http://stackoverflow.com/questions/4656698/default-public-access-in-scala)

Another key aspecte of this is that the general emphasis in Scala is on using immutable data structures, so there isn’t any danger of someone changing the internal state of your objects if you have designed them in this way. This in turn gets rid of the ridiculous getter and setter methods that breed and multiply in Java programs. See “Why getters and setters are evil” for more discussion:

[http://www.javaworld.com/javaworld/jw-09-2003/jw-0905-toolbox.html](http://www.javaworld.com/javaworld/jw-09-2003/jw-0905-toolbox.html)

After you get used to programming in Scala, the whole getter/setter thing that is so common in Java code is pretty much gag worthy.

In general, it is still a good idea to use `private[this]` as a modifier to methods and variables whenever they are only needed by an object itself.


**Q. How do we define overloaded constructors in Scala?**

**Q. The way a class is defined in Scala introduced in the tutorial, seems to have only one constructor. Is there any way to provide multiple constructors like Java?**

You can add additional constructors with `this` declarations.

{% highlight scala %}
class SimpleTriple (x: Int, y: Int, z: String) {
  def this (x: Int, z: String) = this(x,0,z)
  def this (x: Int, y: Int) = this(x,y,"a")
  override def toString = x + ":" + y + ":" + z
}
 
scala> val foo = new SimpleTriple(1,2,"hello")
foo: SimpleTriple = 1:2:hello
 
scala> val bar = new SimpleTriple(1,"goodbye")
bar: SimpleTriple = 1:0:goodbye
 
scala> val baz = new SimpleTriple(1,3)
baz: SimpleTriple = 1:3:a
{% endhighlight %}

Notice that you must supply an initial value for every one of the parameters of the class. This contrasts with Java, which allows you to leave some fields uninitialized (and which tends to lead to nasty bugs and bad design).

Note that you can also provide defaults to parameters.

{% highlight scala %}
class SimpleTripleWithDefaults (x: Int, y: Int = 0, z: String = "a") {
  override def toString = x + ":" + y + ":" + z
}
 
scala> val foo = new SimpleTripleWithDefaults(1)
foo: SimpleTripleWithDefaults = 1:0:a
 
scala> val bar = new SimpleTripleWithDefaults(1,2)
bar: SimpleTripleWithDefaults = 1:2:a
{% endhighlight %}

However, you can’t omit a middle parameter while specifying the last one.

{% highlight scala %}
scala> val foo = new SimpleTripleWithDefaults(1,"xyz")
<console>:12: error: type mismatch;
found   : java.lang.String("xyz")
required: Int
Error occurred in an application involving default arguments.
val foo = new SimpleTripleWithDefaults(1,"xyz")
^
{% endhighlight %}

But, you can name the parameters in the initialization if you want to be able to do this.

{% highlight scala %}
scala> val foo = new SimpleTripleWithDefaults(1,z="xyz")
foo: SimpleTripleWithDefaults = 1:0:xyz
{% endhighlight %}

You then have complete freedom to change the parameters around.

{% highlight scala %}
scala> val foo = new SimpleTripleWithDefaults(z="xyz",x=42,y=3)
foo: SimpleTripleWithDefaults = 42:3:xyz
{% endhighlight %}


**Q. I’m still not clear on the difference between classes and traits.  I guess I see a conceptual difference but I don’t really understand what the functional difference is — how is creating a “trait” different from creating a class with maybe fewer methods associated with it?**

Yes, they are different. First off, traits are abstract, which means you cannot create any members. Consider the following contrast.

{% highlight scala %}
scala> class FooClass
defined class FooClass
 
scala> trait FooTrait
defined trait FooTrait
 
scala> val fclass = new FooClass
class: FooClass = FooClass@1b499616
 
scala> val ftrait = new FooTrait
<console>:8: error: trait FooTrait is abstract; cannot be instantiated
val ftrait = new FooTrait
^
{% endhighlight %}

You can extend a trait to make a concrete class, however.

{% highlight scala %}
scala> class FooTraitExtender extends FooTrait
defined class FooTraitExtender
 
scala> val ftraitExtender = new FooTraitExtender
ftraitExtender: FooTraitExtender = FooTraitExtender@53d26552
{% endhighlight %}

This gets more interesting if the trait has some methods, of course. Here’s a trait, `Animal`, that declares two abstract methods, `makeNoise` and `doBehavior`.

{% highlight scala %}
trait Animal {
  def makeNoise: String
  def doBehavior (other: Animal): String
}
{% endhighlight %}

We can extend this trait with new class definitions; each extending class must implement both of these methods (or else be declared abstract).

{% highlight scala %}
case class Bear (name: String, defaultBehavior: String = "Regard warily...") extends Animal {
  def makeNoise = "ROAR!"
  def doBehavior (other: Animal) = other match {
    case b: Bear => makeNoise + " I'm " + name + "."
    case m: Mouse => "Eat it!"
    case _ => defaultBehavior
  }
  override def toString = name
}
 
case class Mouse (name: String) extends Animal {
  def makeNoise = "Squeak?"
  def doBehavior (other: Animal) = other match {
    case b: Bear => "Run!!!"
    case m: Mouse => makeNoise + " I'm " + name + "."
    case _ => "Hide!"
  }
  override def toString = name
}
{% endhighlight %}

Notice that Bear and Mouse have different parameter lists, but both can be Animals because they fully implement the Animal trait. We can now start creating objects of the Bear and Mouse classes and have them interact. We don’t need to use “new” because they are case classes (and this also allowed them to be used in the match statements of the `doBehavior` methods).

{% highlight scala %}
val yogi = Bear("Yogi", "Hello!")
val baloo = Bear("Baloo", "Yawn...")
val grizzly = Bear("Grizzly")
val stuart = Mouse("Stuart")
 
println(yogi + ": " + yogi.makeNoise)
println(stuart + ": " + stuart.makeNoise)
println("Grizzly to Stuart: " + grizzly.doBehavior(stuart))
{% endhighlight %}

We can also create a singleton object that is of the `Animal` type by using the following declaration.

{% highlight scala %}
object John extends Animal {
  def makeNoise = "Hullo!"
  def doBehavior (other: Animal) = other match {
    case b: Bear => "Nice bear... nice bear..."
    case _ => makeNoise
  }
  override def toString = "John"
}
{% endhighlight %}

Here, John is an object, not a class. Because this object implements the Animal trait, it successfully extends it and can act as an `Animal`. This means that a `Bear` like `baloo` can interact with `John`.

{% highlight scala %}
println("Baloo to John: " + baloo.doBehavior(John))
{% endhighlight %}

The output of the above code when run as a script is the following.

	Yogi: ROAR!  
	Stuart: Squeak?  
	Grizzly to Stuart: Eat it!  
	Baloo to John: Yawn…  

The closer distinction is between traits and abstract classes. In fact, everything shown above could have been done with `Animal` as an abstract class rather than as a trait. One difference is that an abstract class can have a constructor while traits cannot. Another key difference between them is that traits can be used to support limited multiple inheritance, as shown in the next question/answer.


**Q. Does Scala support multiple inheritance?**

Yes, via traits with implementations of some methods. Here’s an example, with a trait `Clickable` that has an abstract (unimplemented) method `getMessage`, an implemented method click, and a private, reassignable variable `numTimesClicked` (the latter two show clearly that traits are different from Java interfaces).

{% highlight scala %}
trait Clickable {
  private var numTimesClicked = 0
  def getMessage: String
  def click = {
    val output = numTimesClicked + ": " + getMessage
    numTimesClicked += 1
    output
  }
}
{% endhighlight %}

Now let’s say we have a `MessageBearer` class (that we may have wanted for entirely different reasons having nothing to do with clicking).

{% highlight scala %}
class MessageBearer (val message: String) {
  override def toString = message
}
{% endhighlight %}

A new class can be now created by extending `MessageBearer` and “mixing in” the `Clickable` trait.

{% highlight scala %}
class ClickableMessageBearer(message: String) extends MessageBearer(message) with Clickable {
  def getMessage = message
}
{% endhighlight %}

`ClickableMessageBearer` now has the abilities of both `MessageBearers` (which is to be able to retrieve its message) and Clickables.

{% highlight scala %}
scala> val cmb1 = new ClickableMessageBearer("I'm number one!")
cmb1: ClickableMessageBearer = I'm number one!
 
scala> val cmb2 = new ClickableMessageBearer("I'm number two!")
cmb2: ClickableMessageBearer = I'm number two!
 
scala> cmb1.click
res3: java.lang.String = 0: I'm number one!
 
scala> cmb1.message
res4: String = I'm number one!
 
scala> cmb1.click
res5: java.lang.String = 1: I'm number one!
 
scala> cmb2.click
res6: java.lang.String = 0: I'm number two!
 
scala> cmb1.click
res7: java.lang.String = 2: I'm number one!
 
scala> cmb2.click
res8: java.lang.String = 1: I'm number two!
{% endhighlight %}


**Q. Why are there `toString`, `toInt`, and `toList` functions, but there isn’t a `toTuple` function?**

This is a basic question that leads directly to the more advanced topic of implicits. There are a number of reasons behind this. To start with, it is important to realize that there are many types of Tuples, starting with a Tuple with a single element (a Tuple1) up to 22 elements (a Tuple22). Note that when you use (,) to create a tuple, it is implicitly invoking a constructor for the corresponding TupleN of the correct arity.

{% highlight scala %}
scala> val b = (1,2,3)
b: (Int, Int, Int) = (1,2,3)
 
scala> val c = Tuple3(1,2,3)
c: (Int, Int, Int) = (1,2,3)
 
scala> b==c
res4: Boolean = true
{% endhighlight %}


Given this, it is obviously not meaningful to have a function `toTuple` on sequences that are longer than 22. This means there is no generic way to have, say a List or Array, and then call `toTuple` on it and expect reliable behavior to happen.

However, if you want this functionality (even though limited by the above constraint of 22 elements max), Scala allows you to “add” methods to existing classes by using **implicit classes**. You can find lots of discussions about implicits by search for “scala implicits”. But, here’s an example that shows how it works for this particular case.

{% highlight scala %}
val foo = List(1,2)
val bar = List(3,4,5)
 
foo.toTuple2
 
implicit class TupleAble[T](val elements: Seq[T]) extends AnyVal {
  def toTuple2 = elements match { case Seq(a, b) => (a, b) }
  def toTuple3 = elements match { case Seq(a, b, c) => (a, b, c) }
}
 
foo.toTuple2
bar.toTuple3
{% endhighlight %}

If you put this into the Scala REPL, you’ll see that the first invocation of `foo.toTuple` gets an error:

{% highlight scala %}
scala> foo.toTuple2
<console>:9: error: value toTuple is not a member of List[Int]
foo.toTuple
^
{% endhighlight %}

Note that class `TupleAble` takes a Seq in its constructor and then provides the method `toTuple`, using that Seq. It is able to do so for Seqs with 1, 2 or 3 elements, and above that it throws an exception. (We could of course keeping listing more cases out and go up to 22 element tuples, but this shows the point.)

The implicit class tells Scala that when it finds a call to `toTuple2` on an instance of `Seq`, then it should call the method on `TupleAble`.  Additionally, since we have declared this class to be a **value class** with `extends AnyVal`, Scala will rewrite our code at compile-time to make the call to `toTuple2` a static function call instead of actually doing the wrapping.

Note that the reason we have to have `toTuple2` and not `toTuple` is that since tuples are not collections, and each tuple size has its own class, the return type of `toTuple` would have to be the union of all TupleN types, which is `Product with Serializable`, which is pretty much uselessly vague. So no single single function that adapts based on the situation can have the correct return type based on the input length.

