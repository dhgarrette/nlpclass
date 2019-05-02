---
layout: default
title: Assignment 1 - Probability
root: "../"
---

**Due: Thursday, Sept 19.  Written portion by 2pm, programming by noon**

This assignment is based on problems 1-5 of [Jason Eisner](http://www.cs.jhu.edu/~jason/)'s [language modeling homework](https://18d120ec-a-e22e9223-s-sites.googlegroups.com/a/utcompling.com/nlp-s11/assignments/homework-1/eisner_lm_homework.pdf?attachauth=ANoY7crnvOj8DTMuEPniMbpaM6TsNW7G1t807GXUnn8-rZO14f7G_L8KTzU4c0c5E5rhcL0WVmS_yyfTN5B045b9SyrXABL8vTbH9ydSWRFcO8PbwlgbDqSbmYKa6VQk4evqMOfM12ArQ9VzhWd-SeHA6xkhiMFxULD7bAUkY5_bb3yIMj10NSm5lnUo_xIpoJy9kv8v6C2lh3sztweVkqhRJy0XfT0rCNbU8lJfp5RayzYAx0yLMDKeLfTrVQBYRoEnBaFwzr_P&attredirects=0) plus a small programming problem (problem 5). Many thanks to Jason E. for making this and other materials for teaching NLP available!

* Answers to problems 1-4 should be hand-written or printed and handed in before class.
* Problem 5 should be turned in via GitHub.

You are welcome to consult books that cover probability theory, such as DeGroot and Schervish or the appendices of [Cormen et al](http://www.amazon.com/Introduction-Algorithms-Thomas-H-Cormen/dp/0262032937), as well as the slides on probability from Dickinson, Eisner and Martin. Also, usage of Wikipedia in conjunction with the course readings, notes and assignments is acceptable (especially if you learn something from it). For this assignment, it may be helpful to consult the following: [Algebra of sets](http://en.wikipedia.org/wiki/Algebra_of_sets) (especially if you're rusty on set theory) and [Bayes' theorem](http://en.wikipedia.org/wiki/Bayes%27_theorem) which is not extensively discussed in Jurafsy & Martin.

There are 100 points total in this assignment. Point values for each problem/sub-problem are given below.



## Overview

The programming portion of this assignment is meant to help you work with computing probabilities and to put in place code that you will use in subsequent assignments.

In the root of your repository, create a file called `Assignment1_README.md` that contains:
* A short overview of what you've done
* A list of files relevant to this assignment
* Any commands needed to demonstrate your programs

We will grade whatever code is pushed to your GitHub repository at noon on the due date.  If your assignment will be late, please send an email by noon to both me (dhg@cs.utexas.edu) and Lewis (lewfish@cs.utexas.edu) or else we will simply grade what is there.



## Problem 1: 33 points 

(3 points per subproblem)

These short problems will help you get the hang of manipulating probabilities. Let `\( \mathcal{E} \neq \emptyset \)` denote the event space (it's just a set, also known as the sample space), and `\( p \)` be a function that assigns a real number in `\( [0,1] \)` to any subset of `\( \mathcal{E} \)`. This number is called the probability of the subset.

You are told that `\( p \)` satisfies the following two axioms:&nbsp; `\( p(\mathcal{E})=1 \)`.&nbsp;&nbsp; `\( p(X \cup Y) = p(X) + p(Y) \)` provided that `\( X \cap Y = \emptyset \)`.

As a matter of notation, remember that the **conditional probability**&nbsp; `\( p(X \mid Z) \stackrel{\tiny{\mbox{def}}}{=} \frac{p(X \cap Z)}{p(Z)} \)`.  For example, singing in the rain is one of my favorite rainy-day activities: so my ratio `\( p(\text{singing} \mid \text{rainy}) = \frac{p(\text{singing}~AND~\text{rainy})}{p(\text{rainy})} \)` is high.  Here the predicate "singing" picks out the set of singing events in `\( \mathcal{E} \)`, "rainy" picks out the set of rainy events, and the conjoined predicate "singing AND rainy" picks out the interesction of these two sets---that is, all the vents that are both singing AND rainy.

1. Prove from the axioms that if `\( Y \subseteq Z \)`, then `\( p(Y) \leq p(Z) \)`.

    You may use any and all set manipulations you like.  Remember that `\( p(A) = 0 \)` does not imply that `\( A = \emptyset \)` (why not?), and similarly, that `\( p(B) = p(C) \)` does not imply that `\( B = C \)` (even if `\( B \subseteq C \)`).

2. Use the above fact to prove that conditional probabilities `\( p(X \mid Z) \)`, just like ordinary probabilities, always fall in the range `\( [0,1] \)`.

3. Prove from the axioms that `\( p(\emptyset) = 0 \)`.

4. Let `\( \bar{X} \)` denote `\( \mathcal{E} - X \)`.  Prove from the axioms that `\( p(X) = 1-p(\bar{X}) \)`.  For example, `\( p(\text{singing}) = 1 - p(\text{NOT singing}) \)`.

5. Prove from the axioms that `\( p(\text{singing AND rainy} \mid \text{rainy}) = p(\text{singing} \mid \text{rainy}) \)`.

6. Prove from the axioms that `\( p(X \mid Y) = 1 - p(\bar{X} \mid Y) \)`.  For example, `\( p(\text{singing} \mid \text{rainy}) = 1 - p(\text{NOT singing} \mid \text{rainy}) \)`.  This is a generalization of (1.4).

7. Simplify: `\( (p(X \mid Y) \cdot p(Y) + p(X \mid \bar{Y}) \cdot p(\bar{Y})) \cdot p(\bar{Z} \mid X) / p(\bar{Z}) \)`

8. Under what conditions is it true that `\( p(\text{singing OR rainy} = p(\text{singing}) + p(\text{rainy}) \)`?

9. Under what conditions is it true that `\( p(\text{singing AND rainy} = p(\text{singing}) \cdot p(\text{rainy}) \)`?

10.  Suppose you know that `\( p(X \mid Y) = 0 \)`.  Prove that `\( p(X \mid Y,Z) = 0 \)`.

11.  Suppose you know that `\( p(W \mid Y) = 1 \)`.  Prove that `\( p(W \mid Y,Z) = 1 \)`.


## Problem 2: 15 points

All cars are either red or blue.  The witness claimed the car that hit the pedestrian was blue.  Witnesses are bleieved to be about 80% reliable in reporting car color (regardless of the actual car color).  But only 10% of all cars are blue.

1. (1 point) Write an equation relating the following quantities and perhaps other quantities.

    `\[
        \begin{align}
        & p(true = \text{blue})  \\
        & p(true = \text{blue} \mid claimed = \text{blue})  \\
        & p(claimed = \text{blue} \mid true = \text{blue})  
        \end{align}
    \]`

    Reminder: Here, *claimed* and *true* are *random variables*, which means that they are functions over some outcome space.  For example, the probability that *claimed* = blue really means the probability of getting an outcome *x* such that *claimed*(x) = blue.  We are implicitly assuming that the space of outcomes *x* is something like the set of witnessed car accidents.

2. (1 point) Match the three probabilites above with the following terms: *prior probablity*, *likelihood of the evidence*, *posterior probability*.

3. (4 points) Give the values of all three probabilities. (Hint: Use Bayes' Theorem.) Which probability should the judge care about?

4. (4 points) Let's suppose the numbers 80% and 10% are specific to Baltimore. So in the previous problem, you were implicitly using the following more general version of Bayes' Theorem:

    `\[
    p(A \mid B,Y) = \frac{
        p(B \mid A,Y) \cdot p(A \mid Y)
    }{
        p(B \mid Y)
    }
    \]`

    where *Y* is *city* = Baltimore. Just as (1.6) generalized (1.4), by adding a "background" condition *Y*, this version generalizes Bayes' Theorem. Carefully prove it.

5. (4 points) Now prove the more detailed version

    `\[
    p(A \mid B,Y) = \frac{
        p(B \mid A,Y) \cdot p(A \mid Y)
    }{
        p(B \mid A,Y) \cdot p(A \mid Y) + p(B \mid \bar{A},Y) \cdot p(\bar{A} \mid Y)
    }
    \]`

6. (1 point) Write out the equation given in question (2.5) with *A*, *B*, and *Y* replaced by specific propositions from the red-and-blue car problem. For example, *Y* is "*city* = Baltimore" (or just "Baltimore" for short). Now replace the probabilities with actual numbers from the problem, such as 0.8.

    Yeah, it's a mickeymouse problem, but I promise that writing out a real case of this important formula won't kill you, and may even be good for you (like, on an exam).


## Problem 3: 15 points

Beavers can make three cries (and only three cries), which they use to communicate. `bwa` and `bwee` usually mean something like "come" and "go" respectively, and are used during dam maintenance. `kiki` means "watch out!" The following **conditional probability table** shows the probability of the various cries in different situations.

<table class="simple" style="width: 60%;">
    <tr>
        <td><i>p</i>( <i>cry</i> | <i>situation</i> )</td>
        <td>Predator!</td>
        <td>Timber!</td>
        <td>I need help!</td>
    </tr>
    <tr>
        <td style="text-align: right"><i>bwa</i></td>
        <td>0</td>
        <td>0.1</td>
        <td>0.8</td>
    </tr>
    <tr>
        <td style="text-align: right"><i>bwee</i></td>
        <td>0</td>
        <td>0.6</td>
        <td>0.1</td>
    </tr>
    <tr>
        <td style="text-align: right"><i>kiki</i></td>
        <td>1.0</td>
        <td>0.3</td>
        <td>0.1</td>
    </tr>
</table>

1. (1 point) Notice that each column of the above table sums to 1. Write an equation stating
this, in the form `\( \sum_{variable} p(\cdots) = 1 \)`

2. (4 point) A certain colony of beavers has already cut down all the trees around their dam. As there are no more to chew, *p(timber)* = 0. Getting rid of the trees has also reduced *p(predator)* to 0.2. These facts are shown in the following **joint probability table**.  Assuming that the three given situtation are the only possible situations in which the cries might occur, fill in the rest of the table, using the previous table and the laws of probability. (Note that the meaning of each table is given in its top left cell.)

    <table class="simple" style="width: 80%;">
        <tr>
            <td><i>p</i>( <i>cry</i>, <i>situation</i> )</td>
            <td>Predator!</td>
            <td>Timber!</td>
            <td>I need help!</td>
            <td>TOTAL</td>
        </tr>
        <tr>
            <td style="text-align: right"><i>bwa</i></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td style="text-align: right"><i>bwee</i></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td style="text-align: right"><i>kiki</i></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <td style="text-align: ">TOTAL</td>
            <td>0.2</td>
            <td>0</td>
            <td></td>
            <td></td>
        </tr>
    </table>


3. (10 point, 2 pts per subproblem)  A beaver in this colony cries `kiki`. Given this cry, other beavers try to figure out the probability that there is a predator

    i. This probability is written as: *p*(_________)  
    ii. It can be rewritten without the | symbol as: _________  
    iii. Using the above tables, its value is: _________  
    iv. Alternatively, Bayes' Theorem allows you to express this probability as:
    `\[ \frac{
            p(\_\_\_\_) \cdot p(\_\_\_\_)
        }{
            p(\_\_\_\_) \cdot p(\_\_\_\_) + p(\_\_\_\_) \cdot p(\_\_\_\_) + p(\_\_\_\_) \cdot p(\_\_\_\_)
        }
    \]`  
    v. Using the above tables, the value of this is:  
    `\[ \frac{
            p(\_\_\_\_) \cdot p(\_\_\_\_)
        }{
            p(\_\_\_\_) \cdot p(\_\_\_\_) + p(\_\_\_\_) \cdot p(\_\_\_\_) + p(\_\_\_\_) \cdot p(\_\_\_\_)
        }
    \]`  
        This should give the same result as in part iii., and it should be clear that
        they are really the same computation---by constructing table (b) and doing
        part iii., you were *implicitly* using Bayes' Theorem. (I told you it was a
        trivial theorem!)


## Problem 4: 7 points

1\. `\( p(\neg \text{shoe} \mid \neg \text{nail}) = 1 \)`&nbsp;&nbsp; *For want of a nail the shoe was lost*,  
2\. `\( p(\neg \text{horse} \mid \neg \text{shoe}) = 1 \)`&nbsp;&nbsp; *For want of a shoe the horse was lost*,  
3\. `\( p(\neg \text{race} \mid \neg \text{horse}) = 1 \)`&nbsp;&nbsp; *For want of a horse the race was lost*,  
4\. `\( p(\neg \text{fortune} \mid \neg \text{race}) = 1 \)`&nbsp;&nbsp; *For want of a race the fortune was lost*,  
5\. `\( p(\neg \text{fortune} \mid \neg \text{nail}) = 1 \)` And all for the want of a horseshoe nail.  

Show carefully that (5) follows from (1)--(4). Hint: Consider
`\[
    p(\neg \text{fortune}, \neg \text{race}, \neg \text{horse}, \neg \text{shoe} \mid \neg \text{nail}),
\]`
as well as the "chain rule" and problems (1.1), (1.2), and (1.11).

*Note:* The `\( \neg \)` symbol denotes the boolean operator NOT.

*Note:* This problem is supposed to convince you that logic is just a special case of probability theory.

*Note:* Be glad I didn't ask you to prove the correct operation of the pencil sharpener!



## Problem 5: 30 points

This problem builds on the work done in [Assingment 0, Part 6](a0programming.html#part_6_reading_a_data_file).  For that problem we read a file containing features and labels and computed both label counts and feature counts.  In this problem we will write code that computes probability distributions.  

For this task, you will implement two classes that that will represent a probability distribution and a conditional probability distribution.  

The classes will extend traits that are found version **0002** of the `nlpclass-fall2013` dependency.  In order to get these updates, you will need to edit your root `build.sbt` file and update the version of the dependency:

    libraryDependencies += "com.utcompling" % "nlpclass-fall2013_2.10" % "0002" changing()

If you use Eclipse, then after you modify the dependency you will once again have to run `sbt "eclipse with-source=true"` and refresh your project in Eclipse.

The discrete probability distribution classes to implement are as follows:

1. A class that represents a probability distribution: 
{% highlight scala %}
package nlp.a1

import nlpclass.ProbabilityDistributionToImplement

class ProbabilityDistribution[B](...) extends ProbabilityDistributionToImplement[B] {
  override def apply(x: B): Double = ???
  override def sample(): B = ???
}{% endhighlight %}


2. A class that represents a conditional probability distribution: 
{% highlight scala %}
package nlp.a1

import nlpclass.ConditionalProbabilityDistributionToImplement

class ConditionalProbabilityDistribution[A,B](...) extends ConditionalProbabilityDistributionToImplement[A,B] {
  override def apply(x: B, given: A): Double = ???
  override def sample(given: A): B = ???
}{% endhighlight %}

Each of these classes must store relevant training data extracted from a file of features and labels.  Each class has two methods: `apply` and `sample`.  The `apply` method takes an item and returns its probability according to the distribution.  The `sample` method returns an item from the distribution with likelihood according to its probability.  These classes will allow us to interact with the probability distributions is a simple way.

Note that both of these classes are *generic* in that they are written so that they can work with any type of data, not simply Strings.  Your code should also be generic.  This will allow you to work with probability distributions over various types like integers, characters, Vectors, or anything else.  This will be useful in later assignments.


### The `apply` method

The `apply` method for each discrete distribution class should return a probability derived from the training data.  For this assignment, these probabilites should be computed from the relative frequencies of labels and features given in an input file.

*Remember:* A method named `apply` is special in that it can be called as `x.apply(y)` or simply `x(y)`.

For example, if the training data contained 10 labels, 6 of which were "Yes", then a `ProbabilityDistribution` trained on that data might work like this:

{% highlight scala %}
import nlp.a1.ProbabilityDistribution 
val pd = new ProbabilityDistribution[String](...something...)
pd("Yes")  // 0.6, since p(Yes) = 0.6
{% endhighlight %}

Similarly, if the training data contained 

* 10 instances labeled "Yes", 7 of which had value "hello" and 3 "goodbye"
* 5 instances labeled "No", 2 of which had value "hello", and 3 "goodbye"

then a `ConditionalProbabilityDistribution` trained on that data might work like this:

{% highlight scala %}
import nlp.a1.ConditionalProbabilityDistribution 
val cpd = new ConditionalProbabilityDistribution[String, String](...)
cpd("hello", "Yes")  // 0.7, since p(hello | Yes) = 0.7
{% endhighlight %}

Make sure that the method returns 0.0 for items that were never seen in the data:

{% highlight scala %}
pd("unknown")            // 0.0
cpd("unknown", "Yes")    // 0.0
cpd("hello", "unknown")  // 0.0
{% endhighlight %}


### The `sample` method

In order to be able to generate random data for a model, it is useful to have a way to randomly sample items from a probability distribution.  Therefore, you should implement a `sample` method on each discrete distribution class.  

It is important that your `sample` method not simply pick an item uniformally; it should choose items with frequencies determined by the distribution.  For example, if the training data described above was used, then we might get results similar to this work like this:

{% highlight scala %}
import dhg.util.CollectionUtil._
Vector.fill(1000)(pd.sample).counts         
// Map(No -> 393, Yes -> 607)
Vector.fill(1000)(cpd.sample("Yes")).counts 
// Map(goodbye -> 296, hello -> 704)
Vector.fill(1000)(cpd.sample("No")).counts  
// Map(goodbye -> 597, hello -> 403)
{% endhighlight %}

The conditional version works similarly, but requires a parameter for the conditioning item.

*Note:* You are unlikely to get *exactly* these numbers since the sampling is, after all, random.

Sampling from a probability distribution can easily be accomplished with the following algorithm:

1. Generate a random number between 0 and 1 (`scala.util.Random.nextDouble`)
2. Iterate over each item and its probability.
3. Keep a running sum of all the item probabilities.
4. When the sum exceeds the random number, return the current item.

This algorithm can be made more efficient by first sorting the items by their probabilities, largest to smallest.  This will help the algorithm to traverse a smaller number of elements during each call to `sample`.


### Executing the code

In order to make it easy for me to test your code, you will also need to implement an `object` for building these representations from a feature file:

{% highlight scala %}
package nlp.a1

import nlpclass.FeatureFileAsDistributionsToImplement

object FeatureFileAsDistributions extends FeatureFileAsDistributionsToImplement{% endhighlight %}

This `object` will require a method `fromFile(filename: String)` to be implemented that reads a file of features and labels (as was done for [Assingment 0, Part 6](a0programming.html#part_6_reading_a_data_file)) and produce three things:

1. A set of labels
2. A probability distribution over labels
3. A `Map` from features to conditional probability distribution over feature values given labels.

The source code for the traits can be [viewed](https://github.com/utcompling/nlpclass-fall2013/blob/master/src/main/scala/nlpclass/AssignmentTraits.scala#L21) in the class GitHub repository.

So, given a file `data2.txt`:

    word=loved,word=film,word=loved,word=actor,pos=loved,pos=loved,positive
    word=film,word=bad,word=plot,word=worst,neg=bad,neg=worst,negative
    word=worst,word=film,word=dumb,word=film,neg=worst,neg=dumb,negative
    word=car,word=chase,word=fight,word=scene,word=film,neutral
    word=great,word=plot,word=best,word=film,pos=great,pos=best,positive
    word=best,word=actor,word=bad,word=plot,pos=best,neg=bad,negative
    word=hated,word=terrible,word=film,neg=hated,neg=terrible,negative

I should be able to do:

{% highlight scala %}
import nlp.a1.FeatureFileAsDistributions 
val (labels, pLabel, pFeatureValueGivenLabelByFeature) = FeatureFileAsDistributions.fromFile("data2.txt")

println(labels) // Set(neutral, negative, positive)

println(f"p(label=negative) = ${pLabel("negative")}%.2f") // 0.57
println(f"p(label=neutral)  = ${pLabel("neutral")}%.2f")  // 0.14
println(f"p(label=positive) = ${pLabel("positive")}%.2f") // 0.29

val featureNeg = pFeatureValueGivenLabelByFeature("neg")
println(f"p(neg=bad | label=negative) = ${featureNeg("bad", "negative")}%.2f") // 0.29

val featurePos = pFeatureValueGivenLabelByFeature("pos")
println(f"p(pos=best | label=negative) = ${featurePos("best", "negative")}%.2f") // 1.00
println(f"p(pos=best | label=positive) = ${featurePos("best", "positive")}%.2f") // 0.25

val featureWord = pFeatureValueGivenLabelByFeature("word")
println(f"${featureWord("best", "negative")}%.2f") // p(word=best | label=negative) = 0.07
println(f"${featureWord("best", "positive")}%.2f") // p(word=best | label=positive) = 0.13
{% endhighlight %}



