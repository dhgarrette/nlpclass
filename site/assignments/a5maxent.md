---
layout: default
title: Assignment 5 - Maximum Entropy Models
root: "../"
---

**Due: Thursday, November 14.  Programming at noon.  Written portions at 2pm.**

* Written portions are found throughout the assignment, and are clearly marked.
* Coding portions must be turned in via GitHub using the tag `a5`.

&nbsp;<span><span style="color: red; font-weight: bold">UPDATE (11/5):</span> nlpclass-fall2013 dependency changed from version 0007 to 0008</span>  
&nbsp;<span><span style="color: red; font-weight: bold">UPDATE (11/12):</span> nlpclass-fall2013 dependency changed to version 0009</span>


## Overview

To complete the homework, use the code and interfaces found in the class GitHub repository.

* Your written answers should be hand-written or printed and handed in before class. The problem descriptions clearly state where a written answer is expected.
* Programming portions should be turned in via GitHub by noon on the assignment due date.

There are 100 points total in this assignment. Point values for each problem/sub-problem are given below.

The classes used here will extend traits that are found in the `nlpclass-fall2013` dependency.  In order to get these updates, you will need to edit your root `build.sbt` file and update the version of the dependency:

    libraryDependencies += "com.utcompling" % "nlpclass-fall2013_2.10" % "0009" changing()

If you use Eclipse, then after you modify the dependency you will once again have to run `sbt "eclipse with-source=true"` and refresh your project in Eclipse.


**If you have any questions or problems with any of the materials, don't hesitate to ask!**

**Tip:** Look over the entire homework before starting on it. Then read through each problem carefully, in its entirety, before answering questions and doing the implementation.

Finally: if possible, don't print this homework out! Just read it online, which ensures you'll be looking at the latest version of the homework (in case there are any corrections), you can easily cut-and-paste and follow links, and you won't waste paper.


## Introduction

For this homework, you will create a sentiment analysis system for tweets by implementing features that will be used by supervised machine learners.   You will also use a MaxEnt Markov Model to perform part-of-speech tagging, comparing your approach against your HMM from the previous assignment.

To complete the homework, you need to obtain some training and testing data:

* [Bing Liu](http://www.google.com/url?q=http%3A%2F%2Fwww.cs.uic.edu%2F%257Eliub%2F&sa=D&sntz=1&usg=AFrqEzdMHP-wFPNtogumAiASdvJ6BXGaWQ)'s [Opinion Lexicon](https://github.com/utcompling/nlpclass-fall2013/tree/master/data/classify/opinion-lexicon)
* The [debate08](https://github.com/utcompling/nlpclass-fall2013/tree/master/data/classify/debate08) data
* The [health care reform](https://github.com/utcompling/nlpclass-fall2013/tree/master/data/classify/hcr) data

Check out Bo Pang and Lillian Lee's book: Opinion Mining and Sentiment Analysis (free online!)

Warning: there is a mix of software and resources in this homework, and you should not assume you can use them outside of the academic context. In other words, there are several things here that you cannot use in closed commercial applications. Licenses for each for resource are stated in the files themselves, or in the README. The core code is licensed under the GNU General Public License, which means you must follow the rules of that license: in sum, if you use it in any other application, then that application must also be released according to the GPL license. 



## Problem 1: Lexicon Ratio Sentiment Analysis Baseline (20 points)

The first thing that you will implement is an extremely simple baseline classifier for deciding whether a tweet has positive, negative, or neutral sentiment.  The classifier will make use a [polarity lexicon](https://github.com/utcompling/nlpclass-fall2013/tree/master/data/classify/opinion-lexicon) consisting of a set of words indicating *positive* sentiment and a set of words indicating *negative* sentiment.  This classifier will compare tokens in the tweet against the lexicon and will judge a tweet to be *positive* if it has more positive words than negative words, *negative* if it has more negative than positive words, and *neutral* if it has the same number of positive and negative words.

Your job is to implement a class `nlp.a5.LexiconRatioSentimentClassifier` that extends `nlpclass.Classifier[String, String, String]`.  (You can assume that the Feature, Value, and Label are all Strings since the OpenNLP code only works with Strings.)  This `predict` method of this class should examine the *token* features of the given tweet and compare use sets of positive and negative words to make a judgement.

To run this baseline, create an object `nlp.a5.SentimentLexiconRatio` with a main method that accepts the following parameters:

* `--pos`: path to the *positive* lexicon file
* `--neg`: path to the *negative* lexicon file
* `--test`: path to the file to test on

Note that no *training* file is needed since the classifications are made purely based on the lexicon files.

The main method should construct a classifier and evaluate it on the `test` data using the `ClassifierScorer` object.  You should assume that your test data is in the same format as we used for Assignment 2, so you should be able to re-use your file-reading code.

Here is an example of what your output should look like:

    $ sbt "run-main nlp.a5.SentimentLexiconRatio --pos positive-words.txt --neg negative-words.txt --test hcr/dev.txt"
    accuracy = 44.04  (351 / 797)
              P      R      F1
    negative  72.46  36.85  48.86
    neutral   28.82  62.11  39.37
    positive  37.38  46.51  41.45
    avg       46.22  48.49  43.23

The lexicon ratio accuracy of 44% isn't great, but it's better than the expected 33% from random guessing.

A couple notes:

1. Both lexicon files have comments at the top.  Your code should ignore these when reading the files.
2. The lexicon files contain all lower-cased words, but the tweets do not.



## Problem 2: Sentiment Analysis with a MaxEnt Model (20 points)

We will now attempt to improve our sentiment classifier by actually using training data.  To accomplish this, I have provided you a few useful classes that wrap functionality in the [OpenNLP MaxEnt](http://opennlp.apache.org/) library: 

* `nlpclass.MaxEntModel`
* `nlpclass.MaxEntModelTrainer`

You **do not** need to implement these classes.  They are already avilable to you through the nlpclass-fall2013 dependency.  However, if you are interested, you can take a look at the [source code](https://github.com/utcompling/nlpclass-fall2013/blob/master/src/main/scala/nlpclass/MaxEntClassifier.scala).


### MaxEntModel

A `MaxEntModel` is a `Classifier` that can make label predictions from features.

{% highlight scala %}
class MaxEntModel extends Classifier[String, String, String] {

  def predict(features: Vector[(Feature, Value)]): Label = {
{% endhighlight %}

(Again, it only works with Strings (`Classifier[String, String, String]`) because OpenNLP only deals with Strings.)

*Note:* It also has a method `saveToFile(modelFilename: String)` that can be used to persist the trained model to a file to be used later.  The model can be restored from file using the `MaxEntModel.fromFile(modelFilename: String)`.


### MaxEntModelTrainer

A `MaxEntModelTrainer` is a `ClassifierTrainer` that uses training instances to learn a `MaxEntModel`.  It trains using an iterative algorithm.  It takes a few optional parameters, two of which are relevant for this problem:

* `sigma` controls the amount of regularization.  Remember that regularization in a MaxEnt model is analogous to smoothing in naive Bayes.  A higher `sigma` value means that models parameters (the weights) will be more normal and adhere less to the training data.
* `maxIterations` dictates the maximum number of iterations that the learning algorithm will be allowed to run.  More iterations mean better parameter estimates, but it also means that training may take longer.  Note that the algorithm may take fewer than the maximum number of iterations.  This happens when the learner finds an optimal set of parameters since it does not need to look any further.

{% highlight scala %}
class MaxEntModelTrainer(
  featureExtender: FeatureExtender[String, String] = new NoOpFeatureExtender[String, String]
  sigma: Double = 1.0, maxIterations: Int = 100, cutoff: Int = 1) 
  extends ClassifierTrainer[String, String, String] {

  def train(instances: Vector[(Label, Vector[(Feature, Value)])]): Classifier[Label, Feature, Value] = {
{% endhighlight %}

You can ignore the `featureExtender` parameter until Problem 3, so when you instantiate this trainer class, you can do so with by naming the parameters you are passing in:

{% highlight scala %}
new MaxEntModelTrainer(sigma = 0.1)
{% endhighlight %}



### MaxEnt main method

For this problem, you will need to create an object `nlp.a5.MaxEnt` with a main method that accepts the following parameters:

* `--train`: path to the file to train on 
* `--test`: path to the file to test on
* `--sigma`: a Double value that controls the amount of regularization

Here is an expected run:

    $ sbt "run-main nlp.a5.MaxEnt --train hcr/train.txt --test hcr/dev.txt --sigma 0.1"
    Indexing events using cutoff of 1

      Computing event counts...  done. 797 events
      Indexing...  done.
    Sorting and merging events... done. Reduced 797 events to 793.
    Done indexing.
    Incorporating indexed data for training...
    done.
      Number of Event Tokens: 793
          Number of Outcomes: 3
        Number of Predicates: 5069
    ...done.
    Computing model parameters ...
    Performing 100 iterations.
      1:  ... loglikelihood=-875.5939940685004  0.5094102885821832
      2:  ... loglikelihood=-727.0049502761096  0.6888331242158093
      3:  ... loglikelihood=-649.4591428267686  0.7565872020075283
      4:  ... loglikelihood=-601.6288853298706  0.794228356336261
      5:  ... loglikelihood=-569.6074858149511  0.8117942283563363
    [...]
     95:  ... loglikelihood=-409.9199660094035  0.9008782936010038
     96:  ... loglikelihood=-409.7914217929766  0.9021329987452948
     97:  ... loglikelihood=-409.6656997949007  0.9021329987452948
     98:  ... loglikelihood=-409.5427133223546  0.9021329987452948
     99:  ... loglikelihood=-409.4223790721019  0.9021329987452948
    100:  ... loglikelihood=-409.30461696932133 0.9021329987452948
    accuracy = 61.73  (492 / 797)
              P      R      F1
    negative  68.64  77.37  72.75
    neutral   52.34  41.61  46.37
    positive  45.21  38.37  41.51
    avg       55.40  52.45  53.54

In the "iterations", you will see how the likelihood of the training data increases as the iterations progress.

Experiment with different values for `--sigma` and see how it affects the accuracy.

> **Written Answer (a):** Use the hcr dev set to find a sigma value that works well.  Report that that value and the accuracy it produces.  (We will be using this value below).


Now, since both `MaxEntModel` and `NaiveBayesModel` extend `Classifier` we can directly compare them on the same data.  Thus, you should update `NaiveBayes.main` to evaluate the model using `ClassifierScorer`:

    $ sbt "run-main nlp.a2.NaiveBayes --train hcr/train.txt --test hcr/dev.txt --log true --lambda 0.1"
    accuracy = 42.66  (340 / 797)
              P      R      F1
    negative  65.37  36.21  46.60
    neutral   32.21  65.22  43.12
    positive  31.31  38.95  34.72
    avg       42.96  46.79  41.48

Experiment with different values for `--lambda` and see how it affects the accuracy.

> **Written Answer (b):** Use the hcr dev set to find a lambda value that works well.  Report that that value and the accuracy it produces.  (We will be using this value below).


## Problem 3: Extended Features for Sentiment Analysis (60 points)

Similarly to what we did in Assignment 2 with the extended feature set for our naive Bayes model of prepositional phrase attachment, we will implement extended features for sentiment classification.

First, you should update your `NaiveBayes.main` code so that the option `--extend` takes either a key indicating a specific FeatureExtender to use, or "none", indicating that no extended features should be used.  If a key is given, then the `main` method should instantiate the FeatureExtender specific to this key and pass it to the trainer.  If "none", or if the option is not present, you should, instead, instantiate `nlpclass.NoOpFeatureExtender` and pass *it* to the trainer.  You should have the key "ppa" point to the `FeatureExtender` that you created specifically for the ppa dataset in Assignment 2.  

You should then extract the code for instantiating the `FeatureExtender` into its own function so that it can be called from both `NaiveBayes` and your new `MaxEnt` code.  This will ensure that the exact same `FeatureExtenders` will be used by both.  

For this assignment, you will create a `FeatureExtender` specific to the hcr data that can be used with `--extend hcr`.

As with Assignment 2, your hcr `FeatureExtender` should add various kinds of features.  Here are several ideas:

1. Lower-cased versions of all tokens
2. Features that make use of the polarity lexicons seen in Problem 1.  You should, again, pass the positive and negative files into the classifier on the command line via the `--pos` and `--neg` options.
3. Some analysis of the username.  E.g. contains "gop" vs. "dem"
4. You might try conditioning some new features based on the "target" feature.  For example: 
  * `if(target == "obama" && username.contains("gop"))` add a feature `user-target=gop-obama`
  * `if(target == "hcr" && username.contains("lib"))` add a feature `user-target=dems-hcr`

You might even want to remove features:

1. Maybe the `target` feature is too noisy?
2. Maybe stopword tokens confuse the learner?

Come up with at least five **new** feature extensions.  You can implement the feature extensions described here, but you must come up with five novel extensions on your own that are not just variants of each other.  Be creative!  

You are allowed, and *encouraged*, to examine both the hcr train and dev datasets while you design your features.  But **do not** look at the test set and do not look at **any** of the debate08 dataset.  One good strategy when designing features is to add some features, run the model, examine the mistakes, and add new features that specifically target those mistakes.

You should be able to run both `NaiveBayes` and `MaxEnt` with the `--extend hcr` features.  You should be able to run them like this:

    $ sbt "run-main nlp.a2.NaiveBayes --train hcr/train.txt --test hcr/dev.txt --lambda 1.0 --log true --extend hcr --pos positive-words.txt --neg negative-words.txt"

and this:

    $ sbt "run-main nlp.a5.MaxEnt --train hcr/train.txt --test hcr/dev.txt --sigma 1.0 --extend hcr --pos positive-words.txt --neg negative-words.txt"

> **Written Answer (a):** Describe five of the novel features you came up with, and why you think they would be useful.  

> **Written Answer (b):** Using the sigma and lambda features you decided on for Problem 2, report the accuracies of the NaiveBayes and MaxEnt models on the hcr dev set using your feature extensions.

> **Written Answer (c):** For MaxEnt only, see if you can find a better sigma for the hcr dev set.  Use this value for the subsequent questions.  If you found a better sigma, was it higher or lower?  Why do you think it changed?

In order to determine how much a particular feature extension helps (or *whether* it actually help!), it is common to run *ablation* experiments.  This is done by removing just one extension at a time and running the model.  So if you had extensions A, B and C, you would do an ablation by running with B and C (but not A), A and C, and A and B.  This would let you how much A, B, and C, respectively, helped.

> **Written Answer (d):** For the five novel features you described, run a set of ablations on `MaxEnt` on the hcr dev set with the sigma value you decided on in Problem 3c.  For each each of the five runs, list the feature that was ablated and how much the accuracy decreased (or increased!).

Now that you've refined your model by adding lots of good features and choosing the best sigma, you are ready to run your final tests of the model!

> **Written Answer (e):** Using your final selection of feature extenders and your best sigma value, run MaxEnt once on `hcr/test.txt`.  Report the accuracy.

As we have discussed before, we have separate dev and test sets because we want to be able to see how our model performs on new data and using the test set for tuning would introduce an unrealistic bias.  The fact that we have carefully tuned our model for the specific task classifying the sentiment of tweets related to health care reform hopefully means that it will perform well on the hcr test set.  However, it does *not* necessarily mean that the model will work well as a general-purpose tweet sentiment analyzer.  

In order to make claims about the generality of the model, it is important to evaluate the model "blind" on data from an entirely different dataset.  We often do this for NLP models when we want to make statements about effectiveness across languages; we develop on English and then run blind on other languages to make sure it works.  In this case, we will evaluate our model on the dev set of the debate08 data.  The debate08 data is still comprised of tweets about contemporary American politics, so it is reasonable to believe that our hcr features will still be useful.

> **Written Answer (f):** Using the same feature extenders sigma value as Problem 3e, run MaxEnt once on `debate08/dev.txt`.  Report the accuracy.

