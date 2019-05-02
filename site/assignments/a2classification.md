---
layout: default
title: Assignment 2 - Classification
root: "../"
---

**Due: Tuesday, October 1.  Written portion by 2pm, programming by noon**


## Introduction

For this homework, you will implement a naive Bayes classifier that estimates its parameters from training material in order to classify the unseen instances in test material. You will also extract features from data instances to improve classification accuracy.

To complete the homework, use the stub programs and data found in the class GitHub repository.

* Your written answers should be hand-written or printed and handed in before class. The problem descriptions clearly state where a written answer is expected.
* Programming portions should be turned in via GitHub by noon on the assignment due date.

There are 100 points total in this assignment. Point values for each problem/sub-problem are given below.


The used here classes will extend traits that are found version **0004** of the `nlpclass-fall2013` dependency.  In order to get these updates, you will need to edit your root `build.sbt` file and update the version of the dependency:

    libraryDependencies += "com.utcompling" % "nlpclass-fall2013_2.10" % "0004" changing()

If you use Eclipse, then after you modify the dependency you will once again have to run `sbt "eclipse with-source=true"` and refresh your project in Eclipse.


**If you have any questions or problems with any of the materials, don't hesitate to ask!**

**Tip:** Look over the entire homework before starting on it. Then read through each problem carefully, in its entirety, before answering questions and doing the implementation.

Finally: if possible, don't print this homework out! Just read it online, which ensures you'll be looking at the latest version of the homework (in case there are any corrections), you can easily cut-and-paste and follow links, and you won't waste paper.



## Problem 1 - A good day to play tennis? (10 pts)

Let’s start with a simple example problem from Tom Mitchell’s book Machine Learning. The problem is to predict whether it is a good day to play tennis given various factors and some initial data that provides information about whether previous days were good or bad days for tennis. The factors include (in the format "Attribute: List, of, Possible, Values"):

    Outlook: Sunny, Rain, Overcast
    Temperature: Hot, Mild, Cool
    Humidity: High, Normal
    Wind: String, Weak

Table 3.2 on page 59 of Mitchell’s book contains information for fourteen days; this data is encoded into a [training file](https://raw.github.com/utcompling/nlpclass-fall2013/master/data/classify/tennis/train.txt). There is a separate [test file](https://raw.github.com/utcompling/nlpclass-fall2013/master/data/classify/tennis/test.txt). As you might expect, you will learn model parameters using the training data and test the resulting model on the examples in the test data.

Each row in the data files corresponds to a single classification instance. For example, here is the training set:

    Outlook=Sunny,Temperature=Hot,Humidity=High,Wind=Weak,No
    Outlook=Sunny,Temperature=Hot,Humidity=High,Wind=Strong,No    
    Outlook=Overcast,Temperature=Hot,Humidity=High,Wind=Weak,Yes
    Outlook=Rain,Temperature=Mild,Humidity=High,Wind=Weak,Yes
    Outlook=Rain,Temperature=Cool,Humidity=Normal,Wind=Weak,Yes
    Outlook=Rain,Temperature=Cool,Humidity=Normal,Wind=Strong,No
    Outlook=Overcast,Temperature=Cool,Humidity=Normal,Wind=Strong,Yes
    Outlook=Sunny,Temperature=Mild,Humidity=High,Wind=Weak,No
    Outlook=Sunny,Temperature=Cool,Humidity=Normal,Wind=Weak,Yes
    Outlook=Rain,Temperature=Mild,Humidity=Normal,Wind=Weak,Yes
    Outlook=Sunny,Temperature=Mild,Humidity=Normal,Wind=Strong,Yes
    Outlook=Overcast,Temperature=Mild,Humidity=High,Wind=Strong,Yes
    Outlook=Overcast,Temperature=Hot,Humidity=Normal,Wind=Weak,Yes
    Outlook=Rain,Temperature=Mild,Humidity=High,Wind=Strong,No

Each instance consists of a list of attribute values, separated by commas, and the last element is the classification value. The value is "Yes" if it is a good day to play tennis based on the conditions, and "No" if it is not.  This is the same format of the data used in [Assignment 0, Part 6](http://utcompling.github.io/nlpclass-fall2013/assignments/a0programming.html#part_6_reading_a_data_file).

What we are interested in for this toy example is to determine whether the probability of playing tennis is higher than the probability of not playing tennis. We can represent the probability of playing tennis as: 

* p(Label=yes | Outlook=o, Temperator=t, Humidity=h, Wind=w)

Note that *Label*, *Outlook*, *Temperature*, *Humidity*, and *Wind* are all random variables, *yes* is a value, and *o*, *t*, *h*, and *w* are variables for values. In order to reduce clutter, we'll write expression without explicit random variables, so the above will be written just as:

* p( **yes** | o, t, h, w)

So, we want to find out whether:

* p( **yes** | o,t,h,w) > p( **no** | o,t,h,w)

Another way of stating this is that for each instance (with values for *o*, *t*, *h*, and *w*), we seek to find the label *x* with maximum probability:

`\[
    \begin{align}
    \hat{x} 
      &= \stackrel{\arg\!\max}{\tiny{x\hspace{-1mm}\in\hspace{-1mm}\text{yes},\text{no}}} p(x \mid o,t,h,w) \\
      &= \stackrel{\arg\!\max}{\tiny{x\hspace{-1mm}\in\hspace{-1mm}\text{yes},\text{no}}} p(x)~p(o \mid x)~p(t \mid x)~p(h \mid x)~p(w \mid x)
    \end{align}
\]`

> **Part (a) [4 pts].** Written answer. Show explicitly how the last line above is derived from the first line using Bayes rule, the chain rule, independence assumptions, and from the fact we are finding the argmax.


So, if we have a new instance that we wish to classify, like:

    Outlook=Sunny,Temperature=Cool,Humidity=High,Wind=Strong

what we seek is:

`\[
    \begin{align}
    \hat{x} 
      &= \stackrel{\arg\!\max}{\tiny{x\hspace{-1mm}\in\hspace{-1mm}\text{yes},\text{no}}} p(x \mid \text{sunny}, \text{cool}, \text{high}, \text{strong})\\
      &= \stackrel{\arg\!\max}{\tiny{x\hspace{-1mm}\in\hspace{-1mm}\text{yes},\text{no}}} p(x)~p(\text{sunny} \mid x)~p(\text{cool} \mid x)~p(\text{high} \mid x)~p(\text{strong} \mid x)
    \end{align}
\]`


This simply means we need to compute the two values:

`\[
    \begin{align}    
      & p(\text{yes})~p(\text{sunny} \mid \text{yes})~p(\text{cool} \mid \text{yes})~p(\text{high} \mid \text{yes})~p(\text{strong} \mid \text{yes}) \\
      & p(\text{no})~p(\text{sunny} \mid \text{no})~p(\text{cool} \mid \text{no})~p(\text{high} \mid \text{no})~p(\text{strong} \mid \text{no})
    \end{align}
\]`

And pick the label that produced the higher value. 

Terms like p(yes) and p(sunny|no) are just parameters that we can estimate from a corpus, like the training corpus above. We'll start by doing maximum likelihood estimation, which means that the values assigned to the parameters are those which maximize the probability of the training corpus. We'll return to what this means precisely later in the course; for now, it just means that you do exactly what you'd think: count the number of times (frequency) each possibility happened and divide it by the number of times it could have happened.  (Note that in class we wrote *C(x)* to mean *count* instead of *freq(x)*, but these are the same).  Here are some examples:

`\[
    p(\text{yes}) 
      = \frac{freq(\text{yes})}{\sum_x freq(D=x)} 
      = \frac{freq(\text{yes})}{freq(\text{yes}) + freq(\text{no})} 
      = \frac{9}{9 + 5} = 0.643
\]`
&nbsp; 
`\[
    \begin{align}
    p(\text{sunny} \mid \text{yes}) 
      &= \frac{freq(\text{yes},\text{sunny})}{\sum_x freq(\text{yes},O=x)} \\
      &= \frac{freq(\text{yes},\text{sunny})}{freq(\text{yes},\text{sunny}) + freq(\text{yes},\text{rain}) + freq(\text{yes},\text{overcast})} \\
      &= \frac{2}{2 + 3 + 4} = 0.222
    \end{align}
\]`
&nbsp; 
`\[
    \begin{align}
    p(\text{sunny} \mid \text{no}) 
      &= \frac{freq(\text{no},\text{sunny})}{\sum_x freq(\text{no},O=x)} \\
      &= \frac{freq(\text{no},\text{sunny})}{freq(\text{no},\text{sunny}) + freq(\text{no},\text{rain}) + freq(\text{no},\text{overcast})} \\
      &= \frac{2}{3 + 2 + 0} = 0.6
    \end{align}
\]`

Easy! 

*Note:* you might have noticed that *freq*(**yes**, **sunny**) + *freq*(**yes**, **rain**) + *freq*(**yes**, **overcast**) = *freq*(**yes**). This is true for this example because each attribute only occurs exactly once per instance. Later on we'll need the extra flexibility of being able to see the same attribute multiple times per instance, such as multiple words.

The data includes a test set for the tennis task as well, provided in full here:

    Outlook=Sunny,Temperature=Cool,Humidity=High,Wind=Strong,No
    Outlook=Overcast,Temperature=Cool,Humidity=Normal,Wind=Weak,Yes
    Outlook=Sunny,Temperature=Hot,Humidity=Normal,Wind=Weak,Yes
    Outlook=Rain,Temperature=Hot,Humidity=High,Wind=Strong,No
    Outlook=Sunny,Temperature=Cool,Humidity=Normal,Wind=Weak,Yes
    Outlook=Overcast,Temperature=Hot,Humidity=High,Wind=Strong,No
    Outlook=Sunny,Temperature=Mild,Humidity=High,Wind=Weak,Yes
    Outlook=Overcast,Temperature=Mild,Humidity=Normal,Wind=Strong,Yes
    Outlook=Rain,Temperature=Cool,Humidity=Normal,Wind=Strong,No
    Outlook=Overcast,Temperature=Cool,Humidity=Normal,Wind=Strong,Yes
    Outlook=Rain,Temperature=Hot,Humidity=Normal,Wind=Weak,Yes
    Outlook=Sunny,Temperature=Cool,Humidity=High,Wind=Weak,Yes
    Outlook=Rain,Temperature=Hot,Humidity=Normal,Wind=Strong,No

Like the training set, this provides a list of instances, and for each instance, the values for each of the attributes and the classification outcome.

**Part (b) [2 pts].** Written answer. Using the training set to determine the relevant parameters, what is the most probable label for:

    Outlook=Sunny,Temperature=Hot,Humidity=Normal,Wind=Weak

Make sure to show your work, including the values you obtained for each label. Does it match the label given for the third instance in the test set above?

**Part (c) [3 pts].** Written answer. Derive the general formula for calculating p(x|o,t,h,w) and calculate p(yes|overcast,cool,normal,weak) based on parameters estimated from the training set.

**Part (d) [1 pt].** Written answer. Provide a set of attribute values o, t, h, and w for which the probability of either yes or no is zero.



## Problem 2 - Implement basic naive Bayes (30 pts)

This problem will walk you through the implementation and training of a naive Bayes model.


### NaiveBayesModel

Implement a class 

{% highlight scala %}
nlp.a2.NaiveBayesModel[Label, Feature, Value]
{% endhighlight %}

that extends

{% highlight scala %}
nlpclass.Classifier[Label, Feature, Value]
{% endhighlight %}

The `NaiveBayesModel` class will contain your naive Bayes implementation.  It requires a method:

{% highlight scala %}
def predict(features: Vector[(Feature, Value)]): Label
{% endhighlight %}

The `predict` method takes in a Vector of (feature,value) pairs and outputs the most likely label given the features.

The structure in which you store the underlying data within the `NaiveBayesModel` class is ultimately up to you, but I highly encourage you to make use of the `ProbabilityDistribution` and `ConditionalProbabilityDistribution` classes that you wrote for [Assignment 1, Problem 5](http://utcompling.github.io/nlpclass-fall2013/assignments/a1prob.html#problem_5_30_points).  In fact, you'll probably want something like this, which should look familiar:

{% highlight scala %}
class NaiveBayesModel[Label, Feature, Value](
  labels: Set[Label],
  pLabel: ProbabilityDistributionToImplement[Label],
  pValue: Map[Feature, ConditionalProbabilityDistributionToImplement[Label, Value]])
  extends Classifier[Label, Feature, Value]
{% endhighlight %}

When trained on the tennis [training file](https://raw.github.com/utcompling/nlpclass-fall2013/master/data/classify/tennis/train.txt), you should get the following behavior:

{% highlight scala %}
scala> val nbm = new NaiveBayesModel[String,String,String](...)
scala> nbm.predict(Vector("Outlook"->"Sunny", "Temperature"->"Cool", "Humidity"->"High", "Wind"->"Strong"))
res0: String = No
scala> nbm.predict(Vector("Outlook"->"Overcast", "Temperature"->"Cool", "Humidity"->"Normal", "Wind"->"Weak"))
res1: String = Yes
{% endhighlight %}


### UnsmoothedNaiveBayesModelTrainer

In order to maintain good modularity in your code, you will not actually calculate the probability distributions from within the `NaiveBayesModel` class; you will only *use* the distributions to predict a label.

Instead, you will use a distinct *trainer* class to turn the raw data into a `NaiveBayesModel`, calculating the probability distributions along the way.

So you will implement a class 

{% highlight scala %}
nlp.a2.UnsmoothedNaiveBayesTrainer[Label, Feature, Value]
{% endhighlight %}

that extends

{% highlight scala %}
nlpclass.ClassifierTrainer[Label, Feature, Value]
{% endhighlight %}

The `UnsmoothedNaiveBayesTrainer` class requires a method:

{% highlight scala %}
def train(instances: Vector[(Label, Vector[(Feature, Value)])]): Classifier[Label, Feature, Value]
{% endhighlight %}

The `train` method takes a Vector of labeled instances, uses those instances to calculate probability distributions (without smoothing, of course), and then instantiates a `NaiveBayesModel` to be returned.  

When trained on the tennis [training file](https://raw.github.com/utcompling/nlpclass-fall2013/master/data/classify/tennis/train.txt), you should get the following behavior:

{% highlight scala %}
scala> val instances = ... from tennis training file ...
scala> val nbt = new UnsmoothedNaiveBayesTrainer[String, String, String](...)
scala> val nbm = nbt.train(instances)
scala> nbm.predict(Vector("Outlook"->"Sunny", "Temperature"->"Cool", "Humidity"->"High", "Wind"->"Strong"))
res0: String = No
scala> nbm.predict(Vector("Outlook"->"Overcast", "Temperature"->"Cool", "Humidity"->"Normal", "Wind"->"Weak"))
res1: String = Yes
{% endhighlight %}


*Note:*  This separation of concerns is nice because it means that the `NaiveBayesModel` needs only to be concerned with using the parameters to predict labels; it does not care where those parameters come from, meaning that it can be reused under all sorts of parameter-estimating scenarios.  Thus, the trainer's only job is to estimate the parameters from raw data and produce a model.  This means that we can have many kinds of trainers that all have the same interface (train from data to make a model), but vary in their parameter estimation techniques.



### NaiveBayesScorer

In order to evaluate your model, you will need to implement an object that runs labeled test instances through your classifier and checks the results.  

So you will implement an object:

{% highlight scala %}
nlp.a2.NaiveBayesScorer
{% endhighlight %}

that extends

{% highlight scala %}
nlpclass.ClassifierScorerToImplement
{% endhighlight %}

The `NaiveBayesScorer` class requires a method:

{% highlight scala %}
def score[Label, Feature, Value](
    naiveBayesModel: Classifier[Label, Feature, Value],
    testInstances: Vector[(Label, Vector[(Feature, Value)])],
    positveLabel: Label)
{% endhighlight %}


The `score` method takes a three arguments.  

1. `naiveBayesModel`: A trained `NaiveBayesModel` instance
2. `testInstances`: A Vector of test instances and their *correct* labels
3. `positveLabel`: The label to be treated as "positive" for precision and recall calculations

Notice that the `score` method does not return anything.  Instead, it should simply print out the information:

* Accuracy
* Precision (based on the `positiveLabel`)
* Recall (based on the `positiveLabel`)
* F1 (based on the `positiveLabel`)

When trained on the tennis [training file](https://raw.github.com/utcompling/nlpclass-fall2013/master/data/classify/tennis/train.txt) and tested on the tennis [testing file](https://raw.github.com/utcompling/nlpclass-fall2013/master/data/classify/tennis/test.txt), you should get the following behavior:

{% highlight scala %}
scala> val trainInstances = ... from tennis training file ...
scala> val nbt = new UnsmoothedNaiveBayesTrainer[String, String, String](...)
scala> val nbm = nbt.train(trainInstances)
scala> val testInstances = ... from tennis test file ...
scala> NaiveBayesScorer.score(nbm, testInstances, "Yes")
accuracy = 61.54
precision (Yes) = 66.67
recall (Yes) = 75.00
f1 = 70.59
scala> NaiveBayesScorer.score(nbm, testInstances, "No")
accuracy = 61.54
precision (No) = 50.00
recall (No) = 40.00
f1 = 44.44
{% endhighlight %}




### Naive Bayes from the command-line

In order for us to test your code, you will need to create an object `nlp.a2.NaiveBayes` with a `main` method so that we can train and test a model from the command line.

There should be three options that correspond to the three scorer arguments above:

* `--train FILE`
* `--test FILE`
* `--poslab LABEL`

Then we should be able to this:

    $ sbt "run-main nlp.a2.NaiveBayes --train tennis/train.txt --test tennis/test.txt --poslab Yes"
    accuracy = 61.54
    precision (Yes) = 66.67
    recall (Yes) = 75.00
    f1 = 70.59
    $ sbt "run-main nlp.a2.NaiveBayes --train tennis/train.txt --test tennis/test.txt --poslab No"
    accuracy = 61.54
    precision (No) = 50.00
    recall (No) = 40.00
    f1 = 44.44


### Logging

A logging framework is one that lets you print information to the screen (or to a file) from your program in a clean way.  Whereas println statements always show up, logging statements can be controlled when the program is run.  You will use logging statements to show extra information in your output.

You will have to take a few steps to make this work.

First, you will have to extend the trait `com.typesafe.scalalogging.log4j.Logging` from any class or object that you want to be able to log.  If your class or object is already extending something, then you should used the `with` keyword to indicate a second trait to extend:

{% highlight scala %}
import com.typesafe.scalalogging.log4j.Logging

class NaiveBayesModel[Label, Feature, Value](...)
  extends Classifier[Label, Feature, Value]
  with Logging
{% endhighlight %}

Then you will have to add logging statements to print out relevant information.  There are 6 levels of logging statements: trace, debug, info, warn, error, and fatal.  Trace is the lowest, fatal is the highest.  You can log statements using the syntax `logger.debug`, `logger.info`, etc.  (`logger` is a field on the `Logging` trait, which is why it exists even though you aren't explicitly declaring it.)

{% highlight scala %}
  override def predict(features: Vector[(Feature, Value)]): Label = {
    [...]
    logger.debug("something at the debug level")
    [...]
    logger.info("something at the info level")
    [...]
  }
{% endhighlight %}

When `predict` is run, it will print two statement at different logging levels.  

To see the logging statements when you run your program, you should pass a VM argument indicating the log level that you want to show:

    sbt -Dorg.apache.logging.log4j.level=INFO "run-main nlp.a2.NaiveBayes ..."

The way the logging framework works, you will get all the logging statements at the level you specify and above.  So, if you specify INFO, you will get info, warn, error, and fatal statements.  If you specify DEBUG, you will get debug, info, warn, error, and fatal statements.

For this assignment, you should add log statements to the `predict` method of `NaiveBayesModel` so that, for each test instance, it logs posterior probabilities for each label, given the features.

When `predict` receieves a feature vector, it will have to compute the probabilities of each of the labels in order to make a decision about which label is best.  So, for each prediction, you should compute the probability of each label given the features, normalize them so that they sum to 1, sort them from greatest to least, and log them at the INFO level.  

If all probabilities are zero, then there will be no way to normalize them to 1, so you should instead log the statement "All posteriors are zero!".  In such a case, you should simply return the label with the highest prior, since all labels have feature counts with zeros.

So I should be able to do this (minus sbt output noise and logging line noise):

    $ sbt -Dorg.apache.logging.log4j.level=INFO "run-main nlp.a2.NaiveBayes --train tennis/train.txt --test tennis/test.txt --poslab Yes"
    [...logging stuff...] - No   0.7954  Yes  0.2046
    [...logging stuff...] - Yes  1.0000  No   0.0000
    [...logging stuff...] - Yes  0.6729  No   0.3271
    [...logging stuff...] - No   0.8383  Yes  0.1617
    [...logging stuff...] - Yes  0.8606  No   0.1394
    [...logging stuff...] - Yes  1.0000  No   0.0000
    [...logging stuff...] - No   0.6603  Yes  0.3397
    [...logging stuff...] - Yes  1.0000  No   0.0000
    [...logging stuff...] - Yes  0.8224  No   0.1776
    [...logging stuff...] - Yes  1.0000  No   0.0000
    [...logging stuff...] - Yes  0.8224  No   0.1776
    [...logging stuff...] - No   0.5645  Yes  0.4355
    [...logging stuff...] - Yes  0.6068  No   0.3932
    accuracy = 61.54
    precision (Yes) = 66.67
    recall (Yes) = 75.00
    f1 = 70.59

And when the logging flag is not given, I should still be able to do this:

    $ sbt "run-main nlp.a2.NaiveBayes --train tennis/train.txt --test tennis/test.txt --poslab Yes"
    accuracy = 61.54
    precision (Yes) = 66.67
    recall (Yes) = 75.00
    f1 = 70.59


*NOTE:* You are welcome to (and encouraged) to log additional information at the debug and trace levels if you find it useful as you are developing and testing your code.  But please do not log additional information at the info level or above since it will interfere with grading.



## Problem 3 - Prepositional Phrase Attachment and smoothing (25 pts)

Prepositional phrase attachment is the well-known task of resolving a common ambiguity in English syntax regarding whether a prepositional phrase attaches to the verb or the noun in sentences with the pattern 

> Verb Noun_Phrase Prepositional_Phrase 

An example is I saw the man with the telescope. If the prepositional phrase attaches to the verb, then the seeing was done with the telescope; if it attaches to the noun, it indicates that the man had the telescope in his possession. A clear difference can be seen with the following related examples:

* Attach to the noun: He ate the spaghetti with meatballs.
* Attach to the verb: He ate the spaghetti with chopsticks.

We can deal with this decision just like any simple labeling problem: each sentence receives a label *V* or *N* indicating the attachment decision, and there is no benefit to be gained from using previous attachment decisions.

For this problem, you will use a conveniently formatted [data set](https://github.com/utcompling/nlpclass-fall2013/tree/master/data/classify/ppa) for prepositional phrase attachment which has been made available by Adwait Ratnaparkhi. There are three files which you will use for this problem: `train.txt`, `dev.txt`, and `test.txt`. Look at the contents of training:

    verb=join,noun=board,prep=as,prep_obj=director,V
    verb=is,noun=chairman,prep=of,prep_obj=N.V.,N
    verb=named,noun=director,prep=of,prep_obj=conglomerate,N
    verb=caused,noun=percentage,prep=of,prep_obj=deaths,N
    verb=using,noun=crocidolite,prep=in,prep_obj=filters,V
    ...

Each row lists an abbreviated form of a prepositional phrase attachment. The four words correspond to the head verb, head noun, preposition, and head noun object of the preposition, in that order. The final element indicates whether the attachment was to the verb (V) or to the noun (N).

For this exercise, you will train a classifier that learns a model from the data in training and use it to classify new instances. You will develop your model using the material in `dev.txt`. You must not personally inspect the contents of the test data — you will run your classifier on `test.txt` only once, when you are done developing.

The first thing you should do is train your unsmoothed naive Bayes classifier on the ppa data:

    $ sbt "run-main nlp.a2.NaiveBayes --train ppa/train.txt --test ppa/dev.txt --poslab V"
    accuracy = 68.63
    precision (V) = (1596 / 2562) 62.30
    recall (V) = (1596 / 1897) 84.13
    f1 = 71.59

Ratnaparkhi et al (1994) obtain accuracies of around 80%, so we clearly should be able to do much better.  One obvious problem shows up if you look at the actual output (minus sbt output junk and logging line noise):

    $ sbt -Dorg.apache.logging.log4j.level=INFO "run-main nlp.a2.NaiveBayes --train ppa/train.txt --test ppa/dev.txt --poslab N" | head -n20
    [...logging stuff...] - V    0.8328  N    0.1672
    [...logging stuff...] - V    1.0000  N    0.0000
    [...logging stuff...] - V    1.0000  N    0.0000
    [...logging stuff...] - V    1.0000  N    0.0000
    [...logging stuff...] - V    1.0000  N    0.0000
    [...logging stuff...] - All posteriors are zero!
    [...logging stuff...] - V    1.0000  N    0.0000
    [...logging stuff...] - V    1.0000  N    0.0000
    [...logging stuff...] - All posteriors are zero!
    [...logging stuff...] - V    1.0000  N    0.0000
    [...logging stuff...] - V    0.9998  N    0.0002
    [...logging stuff...] - V    0.9998  N    0.0002
    [...logging stuff...] - V    0.9998  N    0.0002
    [...logging stuff...] - V    1.0000  N    0.0000
    [...logging stuff...] - V    1.0000  N    0.0000
    [...logging stuff...] - V    1.0000  N    0.0000

There are many items that have zero probability for N, V, or both. The problem is that we haven't done any smoothing, so there are many parameters that we assign zero to, and then the overall probability for the class becomes zero. For example, the tenth line in `dev.txt` is:

    verb=was,noun=performer,prep=among,prep_obj=groups,N

The output gives zero probability to N because the only training instance that has noun=performer is with the V label:

    verb=juxtapose,noun=performer,prep=with,prep_obj=tracks,V

Thus, the value of p(Noun=performer | Label=V) is zero, making p(Label=V | Verb=juxtapose, Noun=performer, Prep=with, PrepObj=tracks) also zero, regardless of how much the rest of the information looks like a V attachment.

We can fix this by using add-λ smoothing. For example, we can smooth the prior probabilities of the labels as follows:

`\[
    p(Label = x) = \frac{
        freq(Label=x) + \lambda
    }{
        \left[\sum_y freq(Label=y)\right] + \lambda \cdot |L|
    }
\]`

Here, L is the set of labels, like {V, N} or {yes, no}, and |L| is the size of that set. Quite simply, we've added an extra λ count to both labels, so we've added λ|L| hallucinated counts. We ensure it still is a probability distribution by adding λ|L| to the denominator.

**Part (a) [5 pts].** Written answer. Provide the general formula for a similar smoothed estimate for p(Feature=x|Label=y) in terms of the relevant frequencies of x and y and the set ValuesFeature consisting of the values associated with the attribute. (For example, ValuesOutlook from the tennis example is {sunny,rainy,overcast}.) If it helps, first write it down as the estimate for a specific parameter, like p(Outlook=sunny|Label=yes), and then do the more general formula.


**Part (b) [20 pts].** Implementation. 

Similar to what you did for [Problem 2](#problem_2__implement_basic_naive_bayes_30_pts), you should create a class {% highlight scala %}nlp.a2.AddLambdaNaiveBayesTrainer[Label, Feature, Value]{% endhighlight %} that extends {% highlight scala %}nlpclass.ClassifierTrainer[Label, Feature, Value]{% endhighlight %}

Again, you will need to implement the `train` method, but this time it should perform smoothing on the training data.  The λ class-level parameter should be configurable.

Be sure that, when calculating the demonminator for the smoothed probability, your code should be sure to take into account even the values that are not seen with a particular label.

You should also update your `ProbabilityDistribution` class to allow for a "default" parameter so that if a previously-unseen key is looked up in the distribution, the default will be used to return the probability based on there being λ counts.

With λ=2.0, you should see this behavior using the `tennis` training set:

`\[
    \begin{align}
      p_{+1}(Outlook=Overcast \mid No) 
        &= \frac{C(Overcast \mid No)}{C(Rain \mid No) + C(Sunny \mid No) + C(Overcast \mid No)} \\ 
        &= \frac{0+2}{(2+2) + (3+2) + (0+2)}
        = \frac{2}{11}
    \end{align}
\]`

And this behavior:

{% highlight scala %}
val instances = ... from tennis train ...
val nbt = new AddLambdaNaiveBayesTrainer[String, String, String](...)  // with lambda=2.0
val nbm = nbt.train(instances)
nbm.predict(Vector("Outlook" -> "Overcast", "Temperature" -> "Hot", "Humidity" -> "High", "Wind" -> "Strong"))
// Yes
{% endhighlight %}


Finally, you should update the `main` method of the NaiveBayes object to support an extra `--lambda` option where the λ parameter can be specified from the command line.  If specified, the option should specify the parameter lambda for the `AddLambdaNaiveBayesTrainer`.  If the `--lambda` option is not specified, then `UnsmoothedNaiveBayesTrainer` should be used.  Using this should yield these results:

    $ sbt "run-main nlp.a2.NaiveBayes --train tennis/train.txt --test tennis/test.txt --poslab Yes --lambda 1.0"
    accuracy = 61.54
    precision (Yes) = 66.67
    recall (Yes) = 75.00
    f1 = 70.59

    $ sbt "run-main nlp.a2.NaiveBayes --train tennis/train.txt --test tennis/test.txt --poslab Yes --lambda 4.0"
    accuracy = 76.92
    precision (Yes) = 72.73
    recall (Yes) = 100.00
    f1 = 84.21

Now when you run on the `ppa` dataset, you should see that they are no more zero posteriors:

    $ sbt -Dorg.apache.logging.log4j.level=INFO "run-main nlp.a2.NaiveBayes --train ppa/train.txt --test ppa/dev.txt --poslab N --lambda 1.0" | head -n20
    [...logging stuff...] - V    0.8010  N    0.1990
    [...logging stuff...] - V    0.9997  N    0.0003
    [...logging stuff...] - V    0.9997  N    0.0003
    [...logging stuff...] - V    0.9982  N    0.0018
    [...logging stuff...] - V    0.9999  N    0.0001
    [...logging stuff...] - V    0.9986  N    0.0014
    [...logging stuff...] - V    0.9996  N    0.0004
    [...logging stuff...] - V    0.9998  N    0.0002
    [...logging stuff...] - V    0.9996  N    0.0004
    [...logging stuff...] - V    0.5166  N    0.4834
    [...logging stuff...] - V    0.9997  N    0.0003
    [...logging stuff...] - V    0.9996  N    0.0004
    [...logging stuff...] - V    0.9995  N    0.0005
    [...logging stuff...] - V    0.9999  N    0.0001
    [...logging stuff...] - V    0.9999  N    0.0001
    [...logging stuff...] - V    0.9997  N    0.0003


**Written answer.**  Experiment on the `ppa` dataset with various λ values, checking accuracy on `dev.txt`.  Report the best λ you found and what accuracy you obtained on `dev.txt`.



## Problem 4 - Computing with logarithms (15 pts)

When you calculated the values to determine the most probable label for problems 2 and 3, you (probably) followed the equation directly and used multiplication to combine the various probabilities. Doing so works fine on small examples like those in problems 2 and 3, but for problem 5 you will be using a much wider set of attributes with even more values than those used so far. This means that you will be combining a much larger group of much smaller probabilities, so you might easily end up exceeding the floating point precision when many more probabilties are to be combined. A straightforward way of getting around this is to convert the probabilities to logarithms and use addition of log probabilities instead of multiplication of probabilities.

First, here's a reminder of the basic property of interest:

`\[
    a \cdot b = e^{\log_e a + \log_e b}
\]`

Try it out in Scala:

{% highlight scala %}
scala> import scala.math
scala> 6 * 7
42
scala> math.exp(math.log(6) + math.log(7))
41.999999999999986
{% endhighlight %}

More generally:

`\[
    \prod_{x \in X} P(x) = e^{\sum_{x \in X} \log_e P(x)}
\]`

Since multiplying numbers becomes addition of logarithms values, when determining the most probable label we can do the following:

`\[
  \begin{align*}
    \hat{x} &= \text{argmax}_{x \in L}~p(Label=x) \cdot \prod_i p(Attribute_i = y_i \mid Label=x) \\
            &= \text{argmax}_{x \in L}~\log(p(Label=x)) + \sum_i \log(p(Attribute_i = y_i \mid Label=x))
  \end{align*}
\]`



**Part (a) [3 pts].** Written answer. Provide the formula for calculating p(yes|overcast,cool,normal,weak) when using log values of the parameters, such as *log p(yes)*  and *log p(no|yes)*. Note: you need to determine the probability, not the argmax. This is simple, but writing this out explicitly will help you for part (b).

**Part (b) [12 pts].** Implementation. 

Create a new class {% highlight scala %}nlp.a2.LogNaiveBayesModel[Label, Feature, Value]{% endhighlight %} 

that, like the previous version, extends {% highlight scala %}nlpclass.Classifier[Label, Feature, Value]{% endhighlight %}

As before, you will have to implement the `predict` method: {% highlight scala %}def predict(features: Vector[(Feature, Value)]): Label{% endhighlight %}

But, this time, you will compute the argmax using logarithms of probabilities instead of multiplying simple probabilities together.

You should also create a class {% highlight scala %}nlp.a2.LogAddLambdaNaiveBayesTrainer[Label, Feature, Value]{% endhighlight %}

That is exactly the same as your `AddLambdaNaiveBayesTrainer` except that it returns a `LogNaiveBayesModel` instead of a `NaiveBayesModel`.  Feel free to reuse code as much as possible here.

Finally, add an option `--log` to the `main` function of `NaiveBayes` that takes an argument `true` or `false` specifying whether `LogAddLambdaNaiveBayesTrainer` should be used.  If `--log` is not specified, then it should be treated as `false` so that the behavior of the program is as it was before.

You should verify that your new class yields the same results as your previous version:

    $ sbt "run-main nlp.a2.NaiveBayes --train tennis/train.txt --test tennis/test.txt --poslab Yes --lambda 4.0 --log true"
    accuracy = 76.92
    precision (Yes) = 72.73
    recall (Yes) = 100.00
    f1 = 84.21

Keep in mind that you must exponentiate the log scores for each label before you normalize to get the probabilities for logging to the console.  You won't be able to call `math.exp(p)` directly since the values are too small, which is why we were using logarithms in the first place.  Instead, to normalize, you'll need to make the values large enough to exponentiate.  You can do this by dividing each of the values by the largest of them, since dividing all values by the same amount will increase them proportionally, making the largest value 1.0, and then exponentitating --- and remember that dividing exponents is done via subtraction.  So if *P* contains all the calculations for each of the of the labels:

`\[
    p^* = \exp(\log(p) - \max_{v \in P} \log(p))
\]`

Then you can normalize the *p** values as usual.


Notes: You can do logs in various bases; which base you use doesn’t matter, as long as you use it consistently. The easiest thing to do would be to use math.log(number), which gives you base *e*.  Also, the log of zero corresponds to negative infinity in log-space (`Double.NegativeInfinity` in Scala). 


## Problem 5 - Extending the feature set (20 pts)

The simple set of features used for the ppa data above can definitely be improved upon. For example, you could
have features that:

* are combinations of the head noun and verb, e.g. `verb+noun=join+board`
* are a lemmatized version of the verb, e.g. `verb_lemma=cause` from *cause*, *causes*, *caused*, and *causing*
* identify all numbers, e.g. noun_form=number for 42, 1.5, 100,000, etc.
* identify capitalized forms, e.g. noun_form=Xx for Texas and noun_form=XX for USA.

**Implementation.** 

Create a new class that extends the trait

{% highlight scala %}
nlpclass.FeatureExtender[Feature, Value]
{% endhighlight %}

Your implementation will have to implement a method `extendFeatures` that takes an instance's feature vector and produces a new feature vector containing any new features you want the classifier to use.  Be sure to include the original features in the output unless you intentionally want to exclude them.

{% highlight scala %}
def extendFeatures(features: Vector[(Feature, Value)]): Vector[(Feature, Value)]
{% endhighlight %}

You may call your implementation class whatever you want (maybe `PpaFeatureExtender`?).  

In order to make use of your `FeatureExtender` class, you will need to update your `NaiveBayesModel` and `NaiveBayesTrainer` implementations (as well as their `Log` versions from Problem 4) to store an additional field of type `FeatureExtender[Feature, Label]`.  Your trainer requires this field so that it can adjust the features of any training instances that it sees as it counts for the model.  It also needs the field so that it can give it as an argument to `NaiveBayesModel` during construction.  The model needs the field so that it can adjust the features of a instance during prediction. 

For command-line use, add an option `--extend` to `NaiveBayes` that takes either a key indicating a specific FeatureExtender to use, or "none", indicating that no extended features should be used.  If a key is given, then the `main` method should instantiate the FeatureExtender specific to this key and pass it to the trainer.  If "none", or if the option is not present, you should, instead, instantiate `nlpclass.NoOpFeatureExtender` and pass *it* to the trainer.  `NoOpFeatureExtender` is a `FeatureExtender` implementation that does not change the feature it is given.  For this assignment, you will have the key "ppa" point to the `FeatureExtender` you create specifically for the ppa dataset.  We should be able to run your classifier like this:

    $ sbt "run-main nlp.a2.NaiveBayes --train ppa/train.txt --test ppa/dev.txt --poslab V --lambda 1.0 --log true --extend ppa"

To keep your code modular, you may want to create a variety of `FeatureExtender` implementations that each focus on a subset of useful features: one for number features, one for lemma features, one for wordshape feature, etc.  This could be useful during testing when you want to easily try different combinations of features.  To make it easy to combine these separate implementations, I have created a class `CompositeFeatureExtender` (which is itself a `FeatureExtender`) that will compose separate other `FeatureExtender`s into one `FeatureExtender`:

{% highlight scala %}
val featureExtender = 
  new CompositeFeatureExtender[String, String](Vector(
    new NumberFeatureExtender(),
    new LemmaFeatureExtender(),
    new WordShapeFeatureExtender()))
{% endhighlight %}

> **Written answer.** Come up with at least five **new** feature extensions.  You can implement the feature extensions described here, but you must come up with five novel extensions on your own that are not just variants of each other.  Be creative!  Describe five of the features you added, and why you think they would be useful.



**Tip.** Some resources have been imported for you:

* A *lemmatizer* is available for getting lemmas from words.  

    {% highlight scala %}
    import nlpclass.Lemmatize

    println(Lemmatize("causing"))  // cause
    println(Lemmatize("cause"))    // cause
    println(Lemmatize("causes"))   // cause
    println(Lemmatize("caused"))   // cause{% endhighlight %}


As before with the basic features, find an optimal λ on the dev set with the extended features. (Note that this may change as you add more features.)  When you are satisfied that you cannot improve the performance any further, you can finally try out the test set! (Once you've done this, there is no going back to change the features any more.) 

> **Written answer.** What is the performance you obtain when using the basic features and the extended features, each with their best λ?  Report the performance on `dev.txt` and `test.txt`.



## Additional Notes


### Note 1

Keep in mind that it is possible to have the same attribute occur more than once in a given instance, so your code should handle this. What this means is that to get p(a=v|l), you do not divide by the frequency of l, but by the overall frequency of a and l occurring together. This will give you the same value for the tennis data and the ppa data *before* you add your own features. 

In general, we can have lots instances of the same attribute with text classification -- for example, with twitter sentiment analysis, you will have tweets like:

> Love the angelic music behind Luke Russert #HCR reporting.

With a positive sentiment. This will turn into a set of attribute-values + label like:

    word=love,word=angelic,word=music,word=behind,word=luke,word=russert,hashtag=hcr,word=reporting,positive

So, your code should be set up to handle that!


### Note 2

Values can be shared across multiple random variables, which means that you can't just use a tuple containing the value and the label -- you have to make sure to include the attribute. For example, if you have ppa instances like:

    verb=join,noun=board,prep=as,prep_obj=director,V
    verb=saw,noun=director,prep=of,prep_obj=company,N

You do not want the prep_obj=director and noun=director to be mixed in the same counts!
