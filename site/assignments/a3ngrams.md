---
layout: default
title: Assignment 3 - N-Grams
root: "../"
---

**Due: Tuesday, October 15.  Programming at noon.  Written portions at 2pm.**

This assignment is based in part on on problems from [Jason Eisner](http://www.cs.jhu.edu/~jason/)'s [language modeling homework](https://18d120ec-a-e22e9223-s-sites.googlegroups.com/a/utcompling.com/nlp-s11/assignments/homework-1/eisner_lm_homework.pdf?attachauth=ANoY7crnvOj8DTMuEPniMbpaM6TsNW7G1t807GXUnn8-rZO14f7G_L8KTzU4c0c5E5rhcL0WVmS_yyfTN5B045b9SyrXABL8vTbH9ydSWRFcO8PbwlgbDqSbmYKa6VQk4evqMOfM12ArQ9VzhWd-SeHA6xkhiMFxULD7bAUkY5_bb3yIMj10NSm5lnUo_xIpoJy9kv8v6C2lh3sztweVkqhRJy0XfT0rCNbU8lJfp5RayzYAx0yLMDKeLfTrVQBYRoEnBaFwzr_P&attredirects=0). Many thanks to Jason E. for making this and other materials for teaching NLP available!

* Written portions are found throughout the assignment, and are clearly marked.
* Coding portions must be turned in via GitHub using the tag `a3`.




## Introduction

This assignment will guide you though the implementation of an ngram language model with various approaches to handling sparse data.  You will also apply your model to the task of decipherment.

To complete the homework, use the interfaces and stub program found in the class GitHub repository.

* Your written answers should be hand-written or printed and handed in before class. The problem descriptions clearly state where a written answer is expected.
* Programming portions should be turned in via GitHub by noon on the assignment due date.

There are 100 points total in this assignment. Point values for each problem/sub-problem are given below.

The used here classes will extend traits that are found version **0005** of the `nlpclass-fall2013` dependency.  In order to get these updates, you will need to edit your root `build.sbt` file and update the version of the dependency:

    libraryDependencies += "com.utcompling" % "nlpclass-fall2013_2.10" % "0005" changing()

If you use Eclipse, then after you modify the dependency you will once again have to run `sbt "eclipse with-source=true"` and refresh your project in Eclipse.


**If you have any questions or problems with any of the materials, don't hesitate to ask!**

**Tip:** Look over the entire homework before starting on it. Then read through each problem carefully, in its entirety, before answering questions and doing the implementation.

Finally: if possible, don't print this homework out! Just read it online, which ensures you'll be looking at the latest version of the homework (in case there are any corrections), you can easily cut-and-paste and follow links, and you won't waste paper.




## Problem 1: N-Gram Models (15 points)

A **language model** is a probability function *p* that assigns probabilities to word sequences such as `\( \vec{w} = \)` (`i`, `love`, `new york`). Think of `\( p(\vec{w}) \)` as the probability that if you turned on a radio at an arbitrary moment, its next four words would be "i love new york"---perhaps in the middle of a longer sentence such as "the latest bumper sticker says, i love new york more than ever." We often want to consider `\( p(\vec{w}) \)` to decide whether we like `\( p(\vec{w}) \)` better than an alternative sequence.

Formally, each element `\( W \in \mathcal{E} \)` of the underlying event space is a possible value of the *infinite* sequence of words that will come out of the radio after you turn it on.  `\( p(\vec{w}) \)` is really an abbreviation for `\( p(\text{prefix}(W,|\vec{w}|) = \vec{w} \)`, where `\( |\vec{w}|) \)` denotes the length of the sequence `\( p(\vec{w}) \)`.  Thus, *p*(`i`,`love`,`new`,`york`) is the total probability of all infinite word sequences *W* that begin "i love new york ...."

Suppose `\( p(\vec{w}) = w_1 w_2 \dots w_n \)` (a sequence of *n* words). A **trigram language model** defines
`\[ 
  p(\vec{w}) \stackrel{\tiny{\mbox{def}}}{=} p(w_1) \cdot 
                                             p(w_2 \mid w_1) \cdot 
                                             p(w_3 \mid w_1~w_2) \cdot 
                                             p(w_4 \mid w_2~w_3) \cdots
                                             p(w_n \mid w_{n-2}~w_{n-1})
\]`
on the assumption that the sequence was generated in the order `\( w_1, w_2, w_3, \dots \)` ("from left to right") with each word chosen in a way dependent on the previous two words. (But the first word `\( w_1 \)` is not dependent on anything, since we turned on the radio at a arbitrary moment.)

1. (4 points) Expand the above definition of `\( p(\vec{w}) \)` using naive estimates of the parameters, such as
    `\[
      \begin{align}
          p(w_4 \mid w_2, w_3) \stackrel{\tiny{\mbox{def}}}{=} & \frac{C(w_2~w_3~w_4)}{C(w_2~w_3)}
      \end{align}
    \]`
    where `\( C(w_2 w_3 w_4) \)` denotes the count of times the trigram `\( w_2 w_3 w_4 \)` was observed in a training corpus.

    *Remark:* Naive parameter estimates of this sort are called "maximum-likelihood estimates" (MLE). They have the advantage that they maximize the probability (equivalently, minimize the perplexity) of the training data. But they will generally perform badly on test data, unless the training data were so abundant as to include all possible trigrams many times. This is why we must smooth these estimates in practice.

2. (5 points) One could also define a kind of reversed trigram language model `\( p_{reversed} \)` that instead assumed the words were generated in reverse order ("from right to left"):
`\[ 
  \begin{align}
  p_{reversed}(\vec{w}) \stackrel{\tiny{\mbox{def}}}{=}&p(w_n) \cdot
                                                        p(w_{n-1} \mid w_n) \cdot
                                                        p(w_{n-2} \mid w_{n-1} w_n) \cdot
                                                        p(w_{n-3} \mid w_{n-2} w_{n-1}) \\
                                                       &\cdots
                                                        p(w_2 \mid w_3 w_4) \cdot
                                                        p(w_1 \mid w_2 w_3)
  \end{align}
\]`
By manipulating the notation, show that the two models are identical (i.e., `\( p(\vec{w}) = p_{reversed}(\vec{w}) \)` for any `\( \vec{w} \)` provided that both models use MLE parameters estimated from the same training data (see problem (1.1) above)).

3. (3 points) In the data you will use in questions 6 and 14, sentences are delimited by `<S>` at the start and `<E>` at the end. For example, the following data set consists of a sequence of 3 sentences:

        <S> do you think so <E> 
        <S> yes <E> 
        <S> at least i thought so <E>

    Given English training data, the probability of

        <S> do you think the <E>

    should be extremely low under any good language model. Why? In the case of the trigram model, which parameter or parameters are responsible for making this probability low?

4. (3 points)

    You turn on the radio as it is broadcasting an interview. Assuming a trigram model, match up expressions (A), (B), (C) with descriptions (1), (2), (3):

    The expression  

    (A)&nbsp; `\( p(\text{Do}) \cdot p(\text{you} \mid \text{Do}) \cdot p(\text{think} \mid \text{Do you}) \)`  
    (B)&nbsp; `\( p(\text{Do} \mid \text{<S>}) \cdot p(\text{you} \mid \text{<S> Do}) \cdot p(\text{think} \mid \text{Do you}) \cdot p(\text{<E>} \mid \text{you think}) \)`  
    (C)&nbsp; `\( p(\text{Do} \mid \text{<S>}) \cdot p(\text{you} \mid \text{<S> Do}) \cdot p(\text{think} \mid \text{Do you}) \)`  

    represents the probability that

    (1)&nbsp; the first complete sentence you hear is *Do you think* (as in, "D'ya think?")  
    (2)&nbsp; the first 3 words you hear are *Do you think*  
    (3)&nbsp; the first complete sentence you hear starts with *Do you think*  

    Explain your answers briefly. Which quantity is `\( p(\vec{w}) \)`? 

    *Remark:* The distinctions matter because "Do" is more probable at the start of an English sentence than in the middle, and because (3) describes a larger event set than (1) does.




## Problem 2: N-Gram Model Implementation (30 points)

An **ngram** is a sequences of *n* words.  Ngrams are useful for modeling the probabilities of sequences of words (i.e., modeling language).  With an ngram language model, we want to know the probability of the *nth* word in a sequence given that the *n-1* previous words.  

Your task for this assignment is to implement an N-Gram model and supervised learning for the model.

To simplify things, we will only be modeling full sentences, not arbitrary sequences of words.

Your task is to implement the traits provided in [AssignmentTraits.scala](https://github.com/utcompling/nlpclass-fall2013/blob/master/src/main/scala/nlpclass/AssignmentTraits.scala):


### NgramModel

First, you will need to implement the following trait

{% highlight scala %}
trait NgramModelToImplement {

  /**
   * The order of the ngram model.
   * 
   * This can be implemented as a `val`.
   */
  def n: Int
  
  /**
   * Determine the (log) probability of a full sentence.  Return 
   * the probability as the logarithm of the probability.
   *
   * USAGE: 
   *    val sent = Vector("this", "is", "a", "complete", "sentence")
   *    ngramModel.sentenceProb(sent)
   */
  def sentenceProb(sentenceTokens: Vector[String]): Double
  
  /**
   * Generate a sentence based on the model parameters.
   * 
   * Return something like: 
   *    Vector("this", "is", "a", "complete", "sentence")
   */
  def generate(): Vector[String]

}{% endhighlight %}

Notice that this interface provides a clean way for us to interact with the model.  It does not require us to know what *n* is, nor does it require us to know anything about the special start and end pseudo-words: we simply interact with full sentences.

You maybe implement this class any way you see fit (so long as it conforms to the trait).  One suggestion is to do it something like this:

{% highlight scala %}
class NgramModel(
  val n: Int,
  ngramProbs: ConditionalProbabilityDistribution[Vector[String], String])
  extends NgramModelToImplement {

  override def sentenceProb(sentenceTokens: Vector[String]): Double = {
    ???
  }

  override def generate(): Vector[String] = {
    ???
  }

}{% endhighlight %}

Notice that we are re-using `ConditionalProbabilityDistribution` that was written for Assignment 1, but for this assignment we are conditioning on a Vector of Strings (the preceding n-1 words) instead of a single String.

There are two methods that must be implemented:

1. `sentenceProb`, which takes a complete sentence and returns its *log* probability according to the model.  **You must compute this value in log-space and return the logarithm of the sentence probability**.  The input to this method might be something like `Vector("this", "is", "a", "complete", "sentence")`.  Remember that in order to assess the probability of a complete sentence, we must append the special start and end symbols to ensure that we are incorporating the probabilities of the words being at the beginning or end of a sentence.  However, these sentence boundary markers are not needed for unigram models.

2. `generate`, which generates a random sentence with likelihood according to the model.  For models with *n*>1, you should start the generation with *n*-1 'begin' symbols to sample the first word, and then sample each consecutive word using the previous *n*-1 words, stopping when you draw the 'end' symbol.  (Be sure that you do not include the start and end in the output.)  However, for unigram models (*n*=1), this will not work very well since we don't want to have to rely on drawing an 'end' symbol.  Thus, for unigram models, the `generate` method should simply sample a sequence of 10 words.

Learning the parameters for the model will not take place in this class.  Instead, separate classes will be used for training.

> **Written Answer (a):** What will happen if you don't compute the value in log-space?  What will happen if you try to return the result from log-space to a normal probability value?  What kinds of sentences will have problems and what kinds will not?


### UnsmoothedNgramModelTrainer

Like we did for or naive Bayes model implementation, we want to de-couple the actual ngram model from the way that it is trained.  Do do this, we will have special 'trainer' classes for training models.  The trainers should implement the following interface, given in [AssignmentTraits.scala](https://github.com/utcompling/nlpclass-fall2013/blob/master/src/main/scala/nlpclass/AssignmentTraits.scala):

{% highlight scala %}
trait NgramModelTrainerToImplement {

  def train(tokenizedSentences: Vector[Vector[String]]): NgramModelToImplement

}{% endhighlight %}

For this portion, you will implement a trainer that doesn't do any smoothing:

{% highlight scala %}
class UnsmoothedNgramModelTrainer(n: Int) extends NgramModelTrainerToImplement {

  override def train(tokenizedSentences: Vector[Vector[String]]): NgramModelToImplement = {
    ???
  }

}{% endhighlight %}

The `train` method of this class should take a Vector of tokenized training sentences (each as its own Vector) and return a new `NgramModel` with parameters based on Maximum Likelihood Estimates from the training data.

Since we are modeling full sentences, be sure to make use of the special start and end tokens during training.  However, you should not use them for unigram models.


### Trying out the model

First, we can try the model on a sample dataset:

{% highlight scala %}
import nlp.a3.UnsmoothedNgramModelTrainer

val data =
  Vector(
    "the dog runs .",
    "the dog walks .",
    "the man walks .",
    "a man walks the dog .",
    "the cat walks .",
    "the dog chases the cat .")
    .map(_.split("\\s+").toVector)
val ngt = new UnsmoothedNgramModelTrainer(2)
val ngm = ngt.train(data)
{% endhighlight %}

And check a few probabilities:

{% highlight scala %}
val p1 = ngm.sentenceProb("the dog walks .".split("\\s+").toVector)
println(f"$p1%.4f  ${math.exp(p1)}%.4f")  // -2.4159  0.0893

val p2 = ngm.sentenceProb("the cat walks the dog .".split("\\s+").toVector)
println(f"$p2%.4f  ${math.exp(p2)}%.4f")  // -5.4604  0.0043

val p3 = ngm.sentenceProb("the cat runs.".split("\\s+").toVector)
println(f"$p3%.4f  ${math.exp(p3)}%.4f")  // NaN  NaN
{% endhighlight %}



Let's test out the model on some books.  Download these three books from Project Gutenberg and save them with the following names:

* `alice.txt`: [Alice's Adventures in Wonderland](http://www.gutenberg.org/cache/epub/11/pg11.txt) 
* `lookingglass.txt`: [Through the Looking-Glass](http://www.gutenberg.org/cache/epub/12/pg12.txt) 
* `sherlock.txt`: [The Adventures of Sherlock Holmes](http://www.gutenberg.org/cache/epub/1661/pg1661.txt) 

To make things easier, I have provided you with an interface to the [Stanford CoreNLP](http://nlp.stanford.edu/software/corenlp.shtml) tokenizer which also segments text into sentences.  So calling `nlpclass.Tokenize(someText)` will return a Vector of sentences, where each sentence is a Vector of Strings for the tokens.  You should also lowercase all text (after running it through the tokenizer) to reduce some of the sparsity in the dataset.

You should be able to use this to make a trainer to train ngram models:

{% highlight scala %}
import nlpclass.Tokenize
import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import nlp.a3.UnsmoothedNgramModelTrainer

def fileTokens(filename: String) = {
  File(filename).readLines
    .split("")
    .flatMap(paragraph => Tokenize(paragraph.mkString(" ")))
    .map(_.map(_.toLowerCase))
    .toVector
}

val trainer = new UnsmoothedNgramModelTrainer(2){% endhighlight %}

With a trained model, we can check the probabilities of sentences:

{% highlight scala %}
val alice = trainer.train(fileTokens("alice.txt"))
alice.sentenceProb("the last came a little bird , so there was that .".split(" ").toVector)
// -41.39217559191104
alice.sentenceProb("so there was that .".split(" ").toVector)
// -19.451400403614677
alice.sentenceProb("the last came a little bird .".split(" ").toVector)
// -Infinity
{% endhighlight %}

> **Written Answer (b):** Why is the probability of "so there was that ." **higher** than that of "the last came a little bird , so there was that ."?

> **Written Answer (c):** Why does "the last came a little bird ." have a zero probability (negative infinity in log-space)?  Why did this zero not drive the probability of "the last came a little bird , so there was that ." to zero as well?

You can also use your trained models to generate random sentences:

{% highlight scala %}
val trigramTrainer = new UnsmoothedNgramModelTrainer(3)
val alice = trigramTrainer.train(fileTokens("alice.txt"))
println(alice.generate.mkString(" ")){% endhighlight %}

This will produce some sentences like:

1. *our family always hated cats : nasty , low , and were quite dry again :*
2. *u.s. laws alone swamp our small staff .*
3. *the only things in the distance , and every now and then , ' said the dormouse , without prominently displaying the sentence set forth in section 4 , '' he 's murdering the time he was going to begin again , to keep back the wandering hair that curled all over with william the conqueror . '*

You'll see sentences of a variety of lengths since the sentence end is drawn by chance.  Some will look mostly normal (1), some will be exact sentences from the text (2), and some will wander between ideas (3).  Of course, since we are not cleaning up the files, there will be a mixture of book text and Project Gutenberg legal junk too.

We can see that as we change *n* we generate very different sentences; small *n* gives more random sentences, large *n* takes more directly from the text.  Note that since our model is unsmoothed, all sequences of length *n* (or less) must be found exactly in the text.

{% highlight scala %}
def train(n: Int) = new UnsmoothedNgramModelTrainer(n).train(fileTokens("alice.txt"))
println(train(1).generate.mkString(" "))
// came , a to ' turtle near spreading you next
println(train(2).generate.mkString(" "))
// the question , and she said the jury-box , with oh , screamed ` call the list , we do .
println(train(4).generate.mkString(" "))
// ` please would you tell me , ' said the duchess : you 'd only have to whisper a hint to time , and round goes the clock in a twinkling !
println(train(5).generate.mkString(" "))
// ` in my youth , ' said his father , ' i took to the law , and argued each case with my wife ; and the muscular strength , which it gave to my jaw , has lasted the rest of my life . '{% endhighlight %}

With *n*=1, words are completely independent.  With *n*=2, pairs of words make sense, but the sentences don't.  Having *n*=4 gives us longer spans directly from the text, but that are glued together.  And with *n*=5 the long generated sentence is directly from the text.



### PerplexityNgramModelEvaluator

Create an object 

{% highlight scala %}nlp.a3.PerplexityNgramModelEvaluator{% endhighlight %} 

that extends the trait 

{% highlight scala %}nlpclass.NgramModelEvaluator{% endhighlight %} 

and implements the method:

{% highlight scala %}
def apply(model: NgramModelToImplement, tokenizedSentences: Vector[Vector[String]]): Double
{% endhighlight %}

that takes a model and a *test* corpus of tokenized sentences and outputs the perplexity score given the model.  **Your calcuations must be in log-space, but the result should not be.**

`\[ 
    PP(s_1, s_2, ...) = \sqrt[(\sum_i |s_i|)]{\frac{1}{\prod_i p(s_i)}}
                      = \exp\left(\frac{-\sum_i \log(p(s_i))}{\sum_i |s_i|}\right)
\]`


To test this, you can build a model of `alice.txt` and measure perplexity on the same text.  Note that typically you will measure perplexity on a different text, but without smoothing, we would end up with zero probabilities and perplexity would be infinite.

{% highlight scala %}
import nlp.a3.PerplexityNgramModelEvaluator

val aliceText = fileTokens("alice.txt")
val trainer = new UnsmoothedNgramModelTrainer(2)
val aliceModel = trainer.train(aliceText)
println(PerplexityNgramModelEvaluator(aliceModel, aliceText))
// 20.763056026820063
{% endhighlight %} 

Note that this gives us an extremely low perplexity, which we would expect given that we are evaluating on the training data.


### Command-Line Interface

In order for us to run your model, you should create an object `nlp.a3.Ngrams` with a `main` method that takes three options:

* `--n N`: the *n* to use for the *n*-gram model
* `--train FILE`: The file containing the text to train on
* `--test FILE`: The file containing the text to evaluate perplexity on

It should train an `NgramModel` using the `UnsmoothedNgramModelTrainer`, compute a perplexity score using `PerplexityNgramModelEvaluator`, and output the score calculated.  When run from the command line, you should see this:

    $ sbt "run-main nlp.a3.Ngrams --n 3 --train alice.txt --test alice.txt"
    3.6424244121974905


## Problem 3: Add-λ Smoothed NgramModelTrainer (20 points)

To improve our ngram model, we will implement add-λ smoothing.  You will implement a new `NgramModelTrainerToImplement` called `AddLambdaNgramModelTrainer`.  This class will take a parameter λ and add it to each of the ngram counts, including counts of ngrams that do not appear.

### Default for ConditionalProbabilityDistribution

Of course, you will want to continue to reuse your `ProbabilityDistribution` and `ConditionalProbabilityDistribution` classes.  However, you will encounter a problem with `ConditionalProbabilityDistribution` when checking the probability of a word based on a context that was never seen.  In order for your `ConditionalProbabilityDistribution` class to handle this situation correctly, you should add an additional field to it for a "default" `ProbabilityDistribution` that will be used to determine probabilities when the context is not found.  In other words, you should have this behavior:  

`\[ 
  p(B=b \mid A=a) = 
    \left\{
      \begin{array}{ll}
        p(B=b \mid A=a)       & \mbox{if } a \text{ is known} \\
        p_{default}(B=b) & \mbox{if } a \text{ is unknown}
      \end{array}
    \right.
\]`

You should also update the `ConditionalProbabilityDistribution.sample` method to draw from the default distribution when the given context is unknown.

So, if the training data contained 

* 10 instances labeled "Yes", 7 of which had value "hello" and 3 "goodbye"
* 5 instances labeled "No", 2 of which had value "hello", and 3 "goodbye"
* A default distribution built from 3 "hello" and 5 "goodbye"

then a `ConditionalProbabilityDistribution` trained on that data might work like this:

{% highlight scala %}
import nlp.a1.ProbabilityDistribution 
import nlp.a1.ConditionalProbabilityDistribution 

val default = new ProbabilityDistribution(...)
val cpd = new ConditionalProbabilityDistribution(..., default)

cpd("hello", "Yes")      // 0.7, since p(hello | Yes) = 7/10 = 0.7
cpd("hello", "No")       // 0.4, since p(hello | No) = 2/5 = 0.4
cpd("hello", "unknown")  // 0.375, since p_default(hello) = 3/8 = 0.375

cpd.sample("Yes")     // "hello" (70% of the time) or "goodbye" (30%)
cpd.sample("unknown") // "hello" (37.5% of the time) or "goodbye" (62.5%)
{% endhighlight %}


### Adding λ

In general, the add-λ smoothed probability of a word `\( w_0 \)` given the previous *n*-1 words is:
`\[ 
  p_{+\lambda}(w_0 \mid w_{-(n-1)}, ..., w_{-1}) = \frac{C(w_{-(n-1)}~...~w_{-1}~w_{0})+\lambda}{\sum_x (C(w_{-(n-1)}~...~w_{-1}~x)+\lambda)}
\]`

For a trigram model, for example, this would be
`\[ 
  p_{+\lambda}(w_0 \mid w_{-2}, w_{-1}) = \frac{C(w_{-2}~w_{-1}~w_{0})+\lambda}{\sum_x (C(w_{-2}~w_{-1}~x)+\lambda)}
                             = \frac{C(w_{-2}~w_{-1}~w_{0})+\lambda}{\left(\sum_x C(w_{-2}~w_{-1}~x)\right) + \lambda \cdot |V|}
\]`

where *V* is the number of possiblities for *x*, which all the words that could potentially follow in the sequence.  In our case, *V* will be the set of all known words, plus the special "end" symbol.

However, when the context has never been seen, we have to ensure that the conditional probability distribution returns the right result.  We will use the "default" distribution mentioned above for this.  The default distribution that you use for your add-λ smoothed ngram model will have to return a smoothed probability of a word given that its context was never seen.  But if the context (the sequence `\( w_{-2}~w_{-1} \)`) has never been seen, then `\( C(w_{-2}~w_{-1}~x)=0 \)` for any *x*, including when `\( w_0=x \)`. So, we can reduce this to:
`\[ 
  p_{+\lambda}(w_0 \mid w_{-2}, w_{-1}) = \frac{C(w_{-2}~w_{-1}~w_{0})+\lambda}{\left(\sum_x C(w_{-2}~w_{-1}~x)\right)+ \lambda \cdot |V|}
                             = \frac{0+\lambda}{\left(\sum_x 0\right) + \lambda \cdot |V|}
                             = \frac{0+\lambda}{0+\lambda \cdot |V|}
                             = \frac{\lambda}{\lambda \cdot |V|}
                             %= \frac{1}{1 \cdot |V|}
                             = \frac{1}{|V|}
\]`

So the probability of any word following an *unseen* sequence is uniform over the number of words.


### Modeling full texts

Now you should be able to train an ngram language model and evaluate it on new texts that contain unseen

{% highlight scala %}
sbt "run-main nlp.a3.Ngrams --n 3 --train alice.txt --lambda 1.0 --test alice.txt"
// 1173.074757950973
sbt "run-main nlp.a3.Ngrams --n 3 --train alice.txt --lambda 1.0 --test lookingglass.txt"
// 2179.3556392538153
sbt "run-main nlp.a3.Ngrams --n 3 --train alice.txt --lambda 1.0 --test sherlock.txt"
// 3598.0431975169186
{% endhighlight %} 


> **Written Answer (a):** Why is the perplexity when evaluated on `lookingglass.txt` worse (higher) than `alice.txt`?  Why is `sherlock.txt` worse than both?

> **Written Answer (b):** Why is the add-λ smoothed perplexity of a model both trained and evaluated on `alice.txt` worse than the perplexity of an *unsmoothed* model on the same text with the same *n*?

> **Written Answer (c):** Try a variety of values for λ training a trigram model on `alice.txt` and testing on `sherlock.txt`.  What is the best perplexity score you find, and what λ value yields that score?

> **Written Answer (d):** Repeat part (c) for unigram, bigram, and 4-gram models.  Does each model have the same optimal λ?  If not, give the best perplexity (and corresponding λ) you find for each model.




## Problem 4: Interpolation (20 points)

### Model

Create a class `nlp.a1.InterpolatedNgramModel` that, again, extends `nlpclass.NgramModelToImplement`.  This class should store multiple `NgramModelToImplement` subclass objects, each with an interpolation weight.  The interpolation weights should sum to 1.  The `sentenceProb` method should calculate the probability of a given sentence using each model, and then calculate the weighted sum of these probabilities.  This is the probability of the sentence according to the interpolated model.

Note that since each sub-model's `sentenceProb` returns a *log-*probability, you cannot simply sum them up, since summing log probabilites is equivalent to multiplying normal probabilities.  So for probabilities *p* and *q*, whose logs are *lp* and *lq*, one option would be to calculate it as `\( \log(\exp(lp)+\exp(lq)) \)`, but this would result numbers that cannot be represented as Doubles: the exact problem that we were trying to avoid by using log probabilities in the first place!  Instead, we will use a formula to add log probabilities that keeps them in log-space:

`\[
  logadd(x,y) = 
    \left\{
      \begin{array}{ll}
        x                        & \mbox{if } y = -\infty \\
        y                        & \mbox{if } x = -\infty \\
        x + \log(1 + \exp(y-x))  & \mbox{if } y \leq x \\
        y + \log(1 + \exp(x-y))  & \mbox{otherwise} \\
      \end{array}
    \right.
\]`

Note that log(1+*x*) can more quickly and more accurately be calculated using the built-in scala function `math.log1p(x)`

So now we can add *p* and *q* in log-space as *logadd(lp, lq)*.  

To implement the `generate` method you have a few choices.  The simple solution is to just generate from the highest-order sub-model.  With a different structure, one could implement a more advanced solution that samples a sub-model according to the model weights (which sum to 1, making it a probability distribution) and then generates the next token from that chosen model.  This more advanced version would make use of the full interpolated distribution when generating, allowing for more interesting generation.


### Trainer

Create a class `InterpolatedNgramModelTrainer` that extends `NgramModelTrainerToImplement`.  When `train` is called on a interpolated trainer with order *n*, the method should train an `AddLambdaNgramModelTrainer` for orders 1 to `n`.

For simplicity, you should use the same λ to smooth all the models

Also for simplicity, we will assign weights in a very specific way: each order-*n* model will have twice the weight of the order-(*n*-1) model.  So the unigram model will have weight proportional to 1, bigram proportional to 2, trigram proportional to 4, and so forth such that a model with order *n* has weight proportional to `\( 2^{(n-1)} \)`.  Of course, the weights must be normalized so that they sum to 1.

In a more sophisticated implementation you would likely want to allow a different λ and weight for each sub-model, but that is more complex than what is required for this assignment.


### Command-Line

Add a new option `--interp` that takes an argument `true` or `false` indicating whether the `InterpolatedNgramModelTrainer` should be used.

{% highlight scala %}
sbt "run-main nlp.a3.Ngrams --n 3 --train alice.txt --lambda 1.0 --interp true --test alice.txt"
// 351.53175535531716
sbt "run-main nlp.a3.Ngrams --n 3 --train alice.txt --lambda 1.0 --interp true --test lookingglass.txt"
// 419.7316785985396
sbt "run-main nlp.a3.Ngrams --n 3 --train alice.txt --lambda 1.0 --interp true --test sherlock.txt"
// 693.092620494836
{% endhighlight %} 


> **Written Answer (a):** Why does the interpolated model yield a lower perplexity than a simple add-λ smoothed model with the same λ?

> **Written Answer (b):** Try a variety of values for λ training a trigram model on `alice.txt` and testing on `sherlock.txt`.  What is the best perplexity score you find, and what λ value yields that score?  Is it different from the normal, non-interpolated add-λ smoothed model?

> **Written Answer (c):** Repeat part (b) for unigram, bigram, and 4-gram models.  Does each model have the same optimal λ?  If not, give the best perplexity (and corresponding λ) you find for each model.  Are they different the normal, non-interpolated add-λ smoothed models?




## Problem 5: Decipherment (15 points)

For this problem, you will use your ngram language model implementation to break a substituion cipher.  A substituion cipher is an encoding scheme in which each letter in a text is replaced with another letter.  So if 'a' maps to 'z' and 'b' maps to 'r', then a word like "Abba" would map to "Zrrz".  Note that, for simplicity, we always map capital letters and lowercase letters the same way, and that we will not change non-alphabetic characters.

You will complete the implementation of a program that solves substituion ciphers using a language model.

First, some terminology:

* plain text: some normal text
* cipher text: the enciphered version of some plain text
* cipher key: the mapping from letters to letters that was used to encipher the text

Your starting point is `nlpclass.DecipherToImplement`.  The program starts with some enciphered text, and the goal is to figure out what the original text was.  The basic idea works like this:

1. Build a language model from some text.
2. Make an initial guess for the cipher key.
3. For each pair of letters:  
  a. Swap their mappings in the current cipher key (so if we are swapping 'a' and 'c', and 'a' pointed to 'z' and 'c' pointed to 'h', then the new key would have 'a' pointing to 'h' and 'c' pointing to 'z').  
  b. Decipher the cipher text using the new key.  
  c. Score the key by checking how much the deciphered text looks like the underlying language.  
4. Take the highest-scoring cipher key resulting from a letter-swap.
5. Repeat steps 3-4 until the score does not improve.

This is a fairly basic kind of ["hill-climbing"](http://en.wikipedia.org/wiki/Hill-climbing) optimization algorithm: you start at an initial point, look at each direction in which you could take a "step" (by swapping two letters), find the step that is most "uphill" (the swap that results in the best-looking deciphered text), take that step, and repeat until no improvements can be made.

In `DecipherToImplement`, the cipher key is represented as a vector of 26 strings, where each string is a different single lowercase letter; the first letter in the vector indicates the letter that 'a' maps to, the second is the mapping for 'b', etc.  Texts are represented as vectors of sentences, where each sentence is a vector of string tokens.

The program takes the following options:

* `--n`: the order of the ngram language model
* `--lambda`: the smoothing parameter for the add-λ smoothed ngram language model
* `--train`: the file from which the language model will be consrtucted
* `--trainsize`: the number of tokens used for training
* `--cipher`: the file that will be enciphered at the start of the program
* `--ciphersize`: the number of tokens in the cipher text

The main method of `DecipherToImplement` starts by generating a random secret cipher key, reading `--ciphersize` number of tokens from the `--cipher` file and enciphering the text using the key.  Next, it reads `-trainsize` number of tokens from the `--train` file and calls the `train` method to construct a language model.  It then proceeds to iteratively try to find the best cipher key by making letter swaps and checking the deciphered output.

You will define an *object* `nlp.a3.Decipher` that extends `nlpclass.DecipherToImplement`.  You will have to implement the following methods:

{% highlight scala %}
/**
 * Train an ngram language model on the given tokens
 *
 * @param text    Tokenized sentences to train the language model
 * @param n       The order of the ngram language model
 * @param lambda  The add-lambda smoothing parameter of the model
 * @return        An ngram language model
 */
def train(text: Vector[Vector[String]], n: Int, lambda: Double): NgramModelToImplement

/**
 * Swap two letter mappings in the cipher key.
 *
 * @param i          The cipher key mapping position to swap with j
 * @param j          The cipher key mapping position to swap with i
 * @param cipherKey  The current cipher key, to be manipulated
 * @return           A new cipher key, with swapped letter mappings
 */
def swapLetters(i: Int, j: Int, cipherKey: Vector[String]): Vector[String]

/**
 * Assign a score for the cipher key.  The score should be based on
 * how much the output of deciphering the ciphertext with the key 
 * looks like the underlying language.
 *
 * @param cipherText  Tokenized sentences that have been enciphered
 * @param cipherKey   Mapping from letters to letters
 * @param ngramModel  An ngram language model
 * @return            A score.  (Lower is better)
 */
def scoreCipherKey(cipherText: Vector[Vector[String]], cipherKey: Vector[String], ngramModel: NgramModelToImplement): Double  
  
{% endhighlight %}

The `train` method corresponds to step 1 above, `swapLetters` to step 3a, and `scoreCipherKey` to 3b and 3c.  The `decipher` method on the trait may be useful for deciphering a text with a given key.

The `train` method that you must implement receives tokenized sentences.  However, unlike previous problem, you should train a *character* ngram model instead of a *word* model.  To do this, you will treat each word as a "sentence" of letters, and train the model as such.

You can run the decipher program from the command line:

    $ sbt "run-main nlp.a3.Decipher --n 3 --lambda 1.0 --train sherlock.txt --trainsize 1000 --cipher alice.txt --ciphersize 500"
    [...]
    CIPHER TEXT                       DECIPHERED TEXT

    Ugcyabp vklxnuyd!'' B ta p'o      `Explain yourself!'' I ca n't
    ugcyabp IVNUYD, B'i adxabf,       explain MYSELF, I'm afraid,
    nbx' nabf Aybtu, `qutalnu B'i     sir' said Alice, `because I'm
    pko ivnuyd, vkl nuu.'' B fk       not myself, you see.'' I do
    p'o nuu,' nabf osu                n't see,' said the
    Taouxcbyyax. `B'i adxabf B ta     Caterpillar. `I'm afraid I ca
    p'o clo bo ikxu tyuaxyv,'         n't put it more clearly,'
    Aybtu xucybuf huxv ckybouyv,      Alice replied very politely,
    [...]
    Alphabet:              abcdefghijklmnopqrstuvwxyz
    Deciphered:            abcdefghijklmnopqrstuvwxyz
    Final score: 45.591579702330975
    solved?: true

There are info logging messages if you want to see the progression of each of the iterations:

    $ sbt -Dorg.apache.logging.log4j.level=INFO "run-main nlp.a3.Decipher --n 3 --lambda 1.0 --train sherlock.txt --trainsize 1000 --cipher alice.txt --ciphersize 500"
    [...]
    1: score=163.23  ` Trkmzhx dyijftmn ! ' ' H wz x'v trkmzhx CDFTMN
    2: score=145.05  ` Trkmehx dyijftmn ! ' ' H we x'v trkmehx CDFTMN
    3: score=132.80  ` Trkmehx dyijftmn ! ' ' H we x'o trkmehx CDFTMN
    4: score=124.60  ` Trkmahx dyijftmn ! ' ' H wa x'o trkmahx CDFTMN
    5: score=119.70  ` Tjkmahx dyirftmn ! ' ' H wa x'o tjkmahx CDFTMN
    6: score=118.38  ` Tjkmahx dyirftmn ! ' ' H wa x'o tjkmahx CDFTMN
    [...]

There are also debug logging messages if you want to see the scores from each of the potential letter swaps:

    $ sbt -Dorg.apache.logging.log4j.level=DEBUG "run-main nlp.a3.Decipher --n 3 --lambda 1.0 --train sherlock.txt --trainsize 1000 --cipher alice.txt --ciphersize 500"
    [...]
    1: score=144.28  ` Kzcgsuj bynixkge ! ' ' U as j'h kzcgsuj TBXKGE
       n <-> y;  143.43;  ` Kzcgsuj bnyixkge ! ' ' U as j'h kzcgsuj TBXKGE
       d <-> z;  144.28;  ` Kdcgsuj bynixkge ! ' ' U as j'h kdcgsuj TBXKGE
       o <-> x;  136.75;  ` Kzcgsuj byniokge ! ' ' U as j'h kzcgsuj TBOKGE
       t <-> u;  137.47;  ` Kzcgstj bynixkge ! ' ' T as j'h kzcgstj UBXKGE
       b <-> d;  146.20;  ` Kzcgsuj dynixkge ! ' ' U as j'h kzcgsuj TDXKGE
       f <-> t;  144.70;  ` Kzcgsuj bynixkge ! ' ' U as j'h kzcgsuj FBXKGE
       g <-> u;  141.60;  ` Kzcusgj bynixkue ! ' ' G as j'h kzcusgj TBXKUE
       o <-> u;  144.71;  ` Kzcgsoj bynixkge ! ' ' O as j'h kzcgsoj TBXKGE
    [...]

Because the algorithm uses a hill-climbing approach, it is possible that sometimes the program will not find the correct solution.  This happens when it follows a series of swaps that seem to be leading toward a good solution, but, in actuality, ends up stuck at a cipher key that is wrong, but for which no single swap leads yields a key with a better score.


> **Written Answer (a):**  Why is a *character* ngram model more effective than a token model for this task?

> **Written Answer (b):**  Try reducing the `--trainsize` argument to use less data to train the language model.  At each training size, run the program a few times and report how often it gets the right answer.  Why does less data cause the program to fail more often?

> **Written Answer (c):**  Try reducing the `--ciphersize` argument to generate a smaller cipher text.  At each size, run the program a few times and report how often it gets the right answer.  Why does a smaller cipher text cause the program to fail more often?

> **Written Answer (d):**  Try different values for `--n`.  At each *n*, run the program a few times and report how often it gets the right answer.  Also report how many iterations it takes to reach that answer (use INFO logging to see iterations).  What order of ngrams works the best?

> **Written Answer (e):**  For step 2, the code makes a *random* initial guess for the key.  Why is it useful for this guess to be random, instead of starting with the same key every time?

> **Written Answer (f):**  What is a better strategy for coming up with an initial guess for the key than random?

> **Written Answer (g):**  Assuming you have a small `--trainsize` and `--ciphersize` that you are unable to change, what is a strategy that you could use to use to increase the likelihood of finding the right answer?





