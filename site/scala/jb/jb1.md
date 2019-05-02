---
layout: default
title: First Steps in Scala
root: "../../"
---

## Preface

The is the first of several Scala tutorials I’m creating for my Fall 2011 graduate Introduction to Computational Linguistics course at UT Austin, loosely based on similar tutorials that Katrin Erk created for teaching Python in a similar course. These tutorials assume no previous programming background, an assumption which is unfortunately still quite rare in the help-folks-learn-Scala universe, and which more or less necessitates the creation of these tutorials. The one exception I’m aware of is SimplyScala.

Note: if you already know a programming language, this tutorial will probably not be very useful for you. (Though perhaps some of the later ones on functional programming and such that I intend to do will be, so check back.) In the meantime, check out existing Scala learning materials I’ve listed in the links page for the course.

This tutorial assumes you have Scala installed and that you are using some form of Unix (if you use Windows, you’ll want to look into Cygwin). If you are having problems with this, you might try the examples by evaluating them in the code box on SimplyScala.

A (partial) starter tour of Scala expressions, variables, and basic types

We’ll use the Scala REPL for entering Scala expressions and seeing what the result of evaluating them is. REPL stands for read-eval(uate)-print-loop, which means it is a program that (1) reads the expressions you type in, (2) evaluates them using the Scala compiler, (3) prints out the result of the evaluation, and then (4) waits for you to enter further expressions.

Note: it is very important that you actually type the commands given below into the REPL. If you just read them over, it will in many cases look quite obvious (and it is), but someone who is new to programming will generally find many gaps in their understanding by actually trying things out. In particular, programming languages are very exact, so they’ll do exactly what you tell them to do — and you’ll almost surely mess a few things up, and learn from that.

In a Unix shell, type:

{% highlight text %}
$ scala
{% endhighlight %}

You should see something like the following:

{% highlight text %}
Welcome to Scala version 2.9.0.1 (Java HotSpot(TM) 64-Bit Server VM, Java 1.6.0_26).
Type in expressions to have them evaluated.
Type :help for more information.

scala>
{% endhighlight %}

The scala> line is the prompt that the REPL is waiting for you to enter expressions. Let’s enter some.

{% highlight scala %}
scala> "Hello world"
res0: java.lang.String = Hello world

scala> 2
res1: Int = 2
{% endhighlight %}

The REPL tells us that the first is a String which contains the characters Hello world, that the second is an Int whose value is the integer 2. Strings and Ints are types — this is an easy but important distinction that sometimes takes beginning programmers a while to get used to. This allows Scala to know what to do when you want to use the numerical value 2 (an Int) or the character representing 2 (a String). For example, here is the latter.

{% highlight scala %}
scala> "2"
res3: java.lang.String = 2
{% endhighlight %}

Scala knows that different actions are afforded by different types. For example, Ints can be added to each other.

{% highlight scala %}
scala> 2+3
res4: Int = 5
{% endhighlight %}

Scala evaluates the result and prints the result to the screen. No surprise, the result is what you think it should be. With Strings, addition doesn’t make sense, but the + operator instead indicates concatenation of the strings.

{% highlight scala %}
scala> "Portis" + "head"
res5: java.lang.String = Portishead
{% endhighlight %}

So, if you consider the String “2″ and the String “3″, and use the + operator on them, you don’t get “5″ — instead you get “23″.

{% highlight scala %}
scala> "2" + "3"
res6: java.lang.String = 23
{% endhighlight %}

We can ask Scala to display the result of evaluating a given expression by using the print command.

{% highlight scala %}
scala> print("Hello world")
Hello world
scala> print(2)
scala> print(2 + 3)
{% endhighlight %}

Note that the result is the action of printing, not a value with a type. For the last item, what happens is:

1. Scala evaluates 2 + 3, which is 5.
2. Scala passes that value to the command print.
3. print outputs “5″

You can think of the print command as a verb, and its parameter (e.g. Hello world or 2) as its object.

We often need to store the result of evaluating an expression to a variable for later use (in fact, programming doesn’t get done with out doing this). Let’s do this trivially to start with, breaking down the print statement above into two steps.

{% highlight scala %}
scala> val x = 2 + 3
x: Int = 5
 
scala> print(x)
5
{% endhighlight %}

Here, x is a variable, which we’ve indicate by prefacing it with val, which indicates it is a fixed variable whose value cannot change.

You can choose the names for variables, but they must follow some rules.

* Variable names may contain: letters, numbers, underscore
* They must not start with a number
* They must not be identical to one of the “reserved words”  that Scala has already defined, such as for, if, def, val, and var
* Typical names for variables will be strings like x, y1, result, firstName, here_is_a_long_variable_name.

Returning to the variable x, we can now use it for other computations.

{% highlight scala %}
scala> x + 3
res12: Int = 8
 
scala> x * x
res13: Int = 25
{% endhighlight %}

And of course, we can assign the results of such computations to other variables.

{% highlight scala %}
scala> val y = 3 * x + 2
y: Int = 17
{% endhighlight %}

Notice that Scala knows that multiplication takes precedence over addition in such computations. If we wanted to override that, we’d need to use parentheses to indicate it, just like in basic algebra.

{% highlight scala %}
scala> val z = 3 * (x + 2)
z: Int = 21
{% endhighlight %}

Now, let’s introduce another type, Double, for working with real-valued numbers. The Ints considered thus far are whole numbers, which do fine with multiplication, but will lead to behavior you might not expect when used with division.

{% highlight scala %}
scala> 5 * 2
res12: Int = 10
 
scala> 7/2
res13: Int = 3
{% endhighlight %}

You probably expected to get 3.5 for the latter. However, because both 7 and 2 are Ints, Scala returns an Int — specifically it returns the number of times the denominator can go entirely into the numerator. To get the result you’d normally want here, you need to use Doubles such as the following.

{% highlight scala %}
scala> 7.0/2.0
res14: Double = 3.5
{% endhighlight %}

Now the result is the value you’d expect, and it is of type Double. Scala uses conventions to know the type of values, e.g. it knows that things in quotes are Strings, that numbers that have a “.” in them are Doubles, and that numbers without “.” are Ints. This is an important part of how it Scala infers the types of variables, which is a very useful and somewhat unique property among of languages of its kind (which are called statically typed languages). To see this in a bit more detail, note that you can tell Scala explicitly what a variable’s type is.

{% highlight scala %}
scala> val a: Int  = 2
a: Int = 2
{% endhighlight %}

The a: Int portion of the line indicates that the variable a has the type Int. Here are some examples of other variables with different types.

{% highlight scala %}
scala> val b: Double = 3.14
b: Double = 3.14
 
scala> val c: String = "Hello world"
c: String = Hello world
{% endhighlight %}

Because Scala already knows these types, it is redundant to specify them in these cases. However, when expressions are more complicated, it is at times necessary to specify types explicitly.

Importantly, we cannot assign the variable a type that conflicts with the result of the expression. Here, we try to assign a Double value to a variable of type Int, and Scala reports an error.

{% highlight scala %}
scala> val d: Int = 6.28
<console>:7: error: type mismatch;
found   : Double(6.28)
required: Int
val d: Int = 6.28
^
{% endhighlight %}

In many cases, especially with beginning programming, you won’t have to worry about declaring the types of your variables. We’ll see situations where it is necessary as we progress.

In addition to variables declared with val, Scala allows variables to be declared with var — these variable can have their values reassigned. A few examples are the easiest way to see the difference.

{% highlight scala %}
scala> val a = 1
a: Int = 1
 
scala> a = 2
<console>:8: error: reassignment to val
a = 2
^
 
scala> var b = 5
b: Int = 5
 
scala> b = 6
b: Int = 6
{% endhighlight %}

You can think of a val variable as a sealed glass container into which you can look to see its value, but into which you cannot put anything new, and a var variable as an openable container that allows you both to see the value and to swap a new value in for the old one. We’re going to focus on using vals mostly as they ultimately provide many advantages when combined with functional programming, and because I hope to get you thinking in terms of vals rather than vars while you are starting out.


## Functions

Variables are more useful when used in the context of functions in which a variable like x can be injected with different values by the user of a function. Let’s consider converting degrees Fahrenheit to Celsius. To convert 87, 92, and 100 from Fahrenheit to Celcius, we could do the following.

{% highlight scala %}
scala> (87 - 32) * 5 / 9.0
res15: Double = 30.555555555555557
 
scala> (92 - 32) * 5 / 9.0
res16: Double = 33.333333333333336
 
scala> (100 - 32) * 5 / 9.0
res17: Double = 37.77777777777778
{% endhighlight %}

Obviously, there is a lot of repetition here. Functions allow us to specify the common parts of such calculations, while allowing variables to specify the parts that may be different. In the conversion case, the only thing that changes is the temperature reading in Fahrenheit. Here’s how we declare the appropriate function in Scala.

{% highlight scala %}
scala> def f2c (x: Double) = (x - 32) * 5/9.0
f2c: (x: Double)Double
{% endhighlight %}

Breaking this down we have:

* def is a Scala keyword indicating that a function is being defined
* f2c is the name given to the function
* (x: Double) is the parameter to the function, which is a variable named x of type Double
* (x – 32) * 5/9.0 is the body of the function, which will take the value given by the user of the function, subtract 32 from it and then multiply the result of that by five-ninths

Using the function is easy — give the name of the function, and then provide the value you are passing into the function in parentheses.

{% highlight scala %}
scala> f2c(87)
res18: Double = 30.555555555555557
 
scala> f2c(92)
res19: Double = 33.333333333333336
 
scala> f2c(100)
res20: Double = 37.77777777777778
{% endhighlight %}

And so on. For each call, the function evaluates the expression for x equal  to the value passed into the function. Now we don’t have to retype all the common stuff again and again.

Functions can have multiple arguments. For example, the following is a function which takes two integers, squares each of them and then adds the squared values.

{% highlight scala %}
scala> def squareThenAdd(x: Int, y: Int) = x*x + y*y
squareThenAdd: (x: Int, y: Int)Int
 
scala> squareThenAdd(3,4)
res21: Int = 25
{% endhighlight %}

Which indeed is the same as doing it explicitly.

{% highlight scala %}
scala> 3*3 + 4*4
res22: Int = 25
{% endhighlight %}

An important aspect of functions is that all of the variables must be bound. If not, we get an error.

{% highlight scala %}
scala> def badFunctionWithUnboundVariable(x: Int) = x + y
<console>:8: error: not found: value y
def badFunctionWithUnboundVariable(x: Int) = x + y
{% endhighlight %}

Functions can do much more complex and interesting things that what I’ve shown here, which we’ll get to in another tutorial.

## Editing programs in a text editor and running them on the command line

The REPL is very useful for trying out Scala expressions and seeing how they are evaluated in real time, but actual program development is done by writing a text file that contains a series of expressions that perform interesting behaviors. Doing this is straightforward. Open a text editor (see the course links page for some suggestions), and put the following as the first line, with nothing else.

{% highlight scala %}
print("Hello world")
{% endhighlight %}

Save this file as HelloWorld.scala, making sure it is saved as text only. Then in a Unix shell, go to the directory where that file is saved, and type the following.

{% highlight text %}
$ scala HelloWorld.scala
{% endhighlight %}

You’ll see that Hello world is output, but that the Unix prompt is jammed up right after it. You may have expected it to print out and then leave the Unix prompt on the next line; however, there is nothing in the print command or in the string we asked it to print that indicates that a newline should have been used. To fix this, go back to the editor and change the line to be the following.

{% highlight scala %}
print("Hello world\n")
{% endhighlight %}

When you run this, your Unix prompt appears on the line following Hello world. Characters like ‘\n‘ are metacharacters that indicate outputs other than standard characters like letters, numbers and symbols.

Now, you could also have achieved the same result by writing.

{% highlight scala %}
println("Hello world")
{% endhighlight %}

The functions print and println are the same except that the latter always adds a newline at the end of its output — something that is often desired and thus simplifies the programmer’s life. However, we still often need to use the newline character and other characters when outputting strings. For example, put the following into HelloWorld.scala and run it again.

{% highlight scala %}
println("Hello world\nHere is a list:\n\t1\n\t2\n\t3")
{% endhighlight %}

From the output, it should be quite clear what ‘\t‘ means. Notice that it wasn’t necessary to put ‘\n‘ after the final 3 because println was used instead of print.

This is a trivial program, but in general they tend to get quite complex. This is where code comments come in handy. You can indicate that a line should be ignored by Scala as a comment by using two forward slashes. Comments can be used to indicate who the author of a program is, what the license is for it, documentation to help others (and your future self) understand what various parts of the code are doing, and commenting out lines of code that you don’t want to erase but which you want temporarily inactive.

Here’s a slightly lengthier program with comments and function definitions and uses of those functions along with printing.

{% highlight scala %}
// Author: Jason Baldridge (jasonbaldridge@gmail.com)
 
// This is a trivial program for students learning to program with Scala.
 
// This is a comment. The next line defines a function that squares
// its argument.
def sq(x: Int) = x * x
 
// The next line prints the result of calling sq with the argument 3.
println("3 squared = " + sq(3))
 
// The next line is commented out, so even though it is a valid Scala
// expression, it won't be evaluated by Scala.
// println("4 squared = " + sq(4))
 
// Now, we define a function that uses the previously defined sq
// (rather than using x*x and y*y as before).
def squareThenAdd(x: Int, y: Int) = sq(x) + sq(y)
 
// Now we use it.
println("Squaring 3 and 4 and adding the results = "
        + squareThenAdd(3,4))
{% endhighlight %}


Save this as ScalaFirstStepsPart1.scala and run it with the Scala executable. You should see the following results.

{% highlight text %}
$ scala ScalaFirstStepsPart1.scala
3 squared = 9
Squaring 3 and 4 and adding the results = 25
{% endhighlight %}

Looks good, right? But what is going on with those print statements? We saw earlier that 2+3 evaluates to 5, but that “2″+”3″ evaluates to “23″, and here we have used + on a String and an Int. Shouldn’t that result in an error? What Scala is doing is converting the Int into a string automatically for us, which simplifies the outputing of results considerably. That means we can do things like the following (back to using the REPL).

{% highlight scala %}
scala> println("August " + 22 + ", " + 2011)
August 22, 2011
{% endhighlight %}

That seems a bit pointless because we could have just written “August 22, 2011″, but here’s an example where it is a bit more useful: we can name tomorrow’s day by using an Int for today’s and adding one to it.

{% highlight scala %}
scala> val dayOfTheMonthToday = 22
dayOfTheMonthToday: Int = 22
 
scala> println("Today is August " + dayOfTheMonthToday + " and tomorrow is August " + (dayOfTheMonthToday+1))
{% endhighlight %}

Today is August 22 and tomorrow is August 23
Note that the (dayOfTheMonthToday+1) part is actually Int addition, and the result of that is converted to a String that is concatenated with the rest of the string. This example is still fairly contrived (and obviously doesn’t deal with the end of the month and all), but this autoconversion gets used a lot when you start working with more complex programs. And, perhaps even more obviously, we might reasonably want to add an Int and a Double.

{% highlight scala %}
scala> 2 + 3.0
res27: Double = 5.0
{% endhighlight %}

Here, the result is a Double, since it is the more general type than Int. This kind of autoconversion happens a lot, and often you won’t even realize it is going on.

Another thing to note is that the last print statement went over multiple lines. We’ll be seeing more about what the rules are for statements that run over multiple lines, but this example shows perhaps the easiest one to remember: when you open a parenthesis with “(“, you can keep on going multiple lines until its partner “)” is encountered. So, for example, we could have done the following very spread-out statement.

{% highlight scala %}
println(
  "Squaring 3 and 4 and adding the results = "
 
  +
 
  squareThenAdd(3,4)
)
{% endhighlight %}

As well as being used for boxing in a bunch of code that is an argument to a function, as above, parentheses are quite useful for indicating the order of precedence of combining multiple items, such as indicating that an addition should be done before a multiplication, or that an Int addition should be done before a String concatenation, as shown earlier in the tutorial. Basically, parentheses are often optional, but if you can’t remember what the default rules for expressions being grouped together are, then you can group them explicitly with parentheses.

