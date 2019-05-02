---
layout: default
title: Assignment 4 - Hidden Markov Models
root: "../"
---

**Due: Thursday, October 31.  Programming at noon.  Written portions at 2pm.**

* Written portions are found throughout the assignment, and are clearly marked.
* Coding portions must be turned in via GitHub using the tag `a4`.




## Introduction

This assignment will guide you though the implementation of a Hidden Markov Model with various approaches to handling sparse data.  You will apply your model to the task of part-of-speech tagging.

To complete the homework, use the interfaces found in the class GitHub repository.

* Your written answers should be hand-written or printed and handed in before class. The problem descriptions clearly state where a written answer is expected.
* Programming portions should be turned in via GitHub by noon on the assignment due date.

There are 100 points total in this assignment. Point values for each problem/sub-problem are given below.

The classes used here will extend traits that are found in the `nlpclass-fall2013` dependency.  In order to get these updates, you will need to edit your root `build.sbt` file and update the version of the dependency:

    libraryDependencies += "com.utcompling" % "nlpclass-fall2013_2.10" % "0006" changing()

If you use Eclipse, then after you modify the dependency you will once again have to run `sbt "eclipse with-source=true"` and refresh your project in Eclipse.


**If you have any questions or problems with any of the materials, don't hesitate to ask!**

**Tip:** Look over the entire homework before starting on it. Then read through each problem carefully, in its entirety, before answering questions and doing the implementation.

Finally: if possible, don't print this homework out! Just read it online, which ensures you'll be looking at the latest version of the homework (in case there are any corrections), you can easily cut-and-paste and follow links, and you won't waste paper.





## Problem 1: Implement an Unsmoothed HMM Tagger (60 points)

You will implement a Hidden Markov Model for tagging sentences with part-of-speech tags.  The data we will be using comes from the [Penn Treebank](http://www.cis.upenn.edu/~treebank/) corpus.  The list of tags used can be found [here](http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html).

Create a class `nlp.a4.HiddenMarkovModel[Word, Tag]` that extends the trait [`nlpclass.Tagger[Word, Tag]`](https://github.com/utcompling/nlpclass-fall2013/blob/master/src/main/scala/nlpclass/AssignmentTraits.scala#L163).

Your class will implement two methods:

{% highlight scala %}
/**
 * Compute the probability of the tagged sentence.  The result
 * should be represented as a logarithm.
 */
def sentenceProb(sentence: Vector[(Word, Tag)]): Double

/**
 * Accepts a sentence of word tokens and returns a sequence of 
 * tags corresponding to each of those words.
 */
def tagSentence(sentence: Vector[Word]): Vector[Tag]
{% endhighlight %}

In order to train your model, you will implement a class `UnsmoothedHmmTrainer[Word, Tag]` that extends the trait [`nlpclass.TaggerTrainer[Word, Tag]`](https://github.com/utcompling/nlpclass-fall2013/blob/master/src/main/scala/nlpclass/AssignmentTraits.scala#L179).  It must have the following `train` method:

{% highlight scala %}
def train(taggedSentences: Vector[Vector[(Word, Tag)]]): Tagger[Word, Tag]
{% endhighlight %}

Assuming some training dataset that contains this:

    the|D man|N walks|V the|D dog|N
    the|D dog|N runs|V
    the|D dog|N walks|V
    the|D man|N walks|V
    a|D man|N saw|V the|D dog|N
    the|D cat|N walks|V

The `sentenceProb` method should compute the probability in log-space and return it as a logarithm.  It should  behave like this:

{% highlight scala %}
val trainData = ... from the above data ...
val trainer = new UnsmoothedHmmTrainer[String, String](...)
val model = trainer.train(trainData)

val s1 = Vector(("the", "D"), ("dog", "N"), ("runs", "V"))
val p1 = model.sentenceProb(s1)
println(f"$p1%.4f  ${exp(p1)}%.4f") // -3.3116  0.0365

val s2 = Vector(("the", "D"), ("cat", "N"), ("runs", "V"))
val p2 = model.sentenceProb(s2)
println(f"$p2%.4f  ${exp(p2)}%.4f") // -4.6979  0.0091

val s3 = Vector(("the", "D"), ("man", "N"), ("held", "V"), ("the", "D"), ("saw", "N"))
val p3 = model.sentenceProb(s3)
println(f"$p3%.4f  ${exp(p3)}%.4f") // -Infinity  0.0000
{% endhighlight %}

The `tagSentence` method should implement the Viterbi algorithm to find the most likely tag sequence for a given sentence.  It should behave like this:

{% highlight scala %}
println(model.tagSentence("the dog runs".split("\\s+").toVector))
// Vector(D, N, V)
println(model.tagSentence("the cat runs".split("\\s+").toVector))
// Vector(D, N, V)
{% endhighlight %}


Here are the viterbi tables for each of these sentences, with backpointers for each non-zero cell:

<table>
  <tr> <td>                  </td>  <td><b>&lt;S&gt;</b></td><td></td>  <td><b>the</b>        </td>  <td><b>dog</b></td>  <td><b>runs</b></td>  <td><b>&lt;E&gt;</b></td> </tr>
  <tr> <td> <b>&lt;S&gt;</b> </td>  <td>0.0             </td><td></td>  <td>                  </td>  <td>          </td>  <td>           </td>  <td>                </td> </tr>
  <tr> <td> <b>D</b>         </td>  <td>                </td><td></td>  <td>-0.1335, &lt;S&gt;</td>  <td>-Infinity </td>  <td>-Infinity  </td>  <td>                </td> </tr>
  <tr> <td> <b>N</b>         </td>  <td>                </td><td></td>  <td>-Infinity         </td>  <td>-0.8267, D</td>  <td>-Infinity  </td>  <td>                </td> </tr>
  <tr> <td> <b>V</b>         </td>  <td>                </td><td></td>  <td>-Infinity         </td>  <td>-Infinity </td>  <td>-2.9061, N </td>  <td>                </td> </tr>
  <tr> <td> <b>&lt;E&gt;</b> </td>  <td>                </td><td></td>  <td>                  </td>  <td>          </td>  <td>           </td>  <td>-3.3116, V      </td> </tr>
</table>
&nbsp;   
<table>
  <tr> <td>                  </td>  <td><b>&lt;S&gt;</b></td><td></td>  <td><b>the</b>        </td>  <td><b>cat</b></td>  <td><b>runs</b></td>  <td><b>&lt;E&gt;</b></td> </tr>
  <tr> <td> <b>&lt;S&gt;</b> </td>  <td>0.0             </td><td></td>  <td>                  </td>  <td>          </td>  <td>           </td>  <td>                </td> </tr>
  <tr> <td> <b>D</b>         </td>  <td>                </td><td></td>  <td>-0.1335, &lt;S&gt;</td>  <td>-Infinity </td>  <td>-Infinity  </td>  <td>                </td> </tr>
  <tr> <td> <b>N</b>         </td>  <td>                </td><td></td>  <td>-Infinity         </td>  <td>-2.2130, D</td>  <td>-Infinity  </td>  <td>                </td> </tr>
  <tr> <td> <b>V</b>         </td>  <td>                </td><td></td>  <td>-Infinity         </td>  <td>-Infinity </td>  <td>-4.2924, N </td>  <td>                </td> </tr>
  <tr> <td> <b>&lt;E&gt;</b> </td>  <td>                </td><td></td>  <td>                  </td>  <td>          </td>  <td>           </td>  <td>-4.6967, V      </td> </tr>
</table>


Testing on the `ptbtag` data should behave like this:

{% highlight scala %}
val trainData = ... read from ptbtag/train.txt ...
val trainer = new UnsmoothedHmmTrainer[String, String](...)
val model = trainer.train(trainData)
val s = Vector(("The","DT"), ("man","NN"), ("saw","VBD"), ("a","DT"), ("house","NN"), (".","."))

model.sentenceProb(s)
// -34.38332797005687

model.tagSentence("The man saw a house .".split("\\s+").toVector)
// Vector(DT, NN, VBD, DT, NN, .)
{% endhighlight %}




Finally, you should create an object `nlp.a4.Hmm` with a main method.  The program should accept the following parameters: 

* `--train FILE`, which specifies a file of pos-tagged sentences for training
* `--test FILE`, which specifies a file of pos-tagged sentences for evaluation

The main method should output the accuracy of the tagger as the percentage of tokens that are labeled correctly.  It should also output an ordered list of the top ten most frequent mistakes made by the tagger showing the "gold" tag (what the tag should have been), the "model" tag (what the model outputed), and the number of times that specific mistagging occurred.

You should get this output from this command:

    $ sbt "run-main nlp.a4.Hmm --train ptbtag/train.txt --test ptbtag/dev.txt"
    Accuracy: 64.82  (58191/89773)
    count  gold  model
     4820    NN     IN
     3865   NNP     IN
     2295    DT     IN
     2160    JJ     IN
     2105   NNS     IN
     2098     ,     IN
     1824     .     IN
     1699    CD     IN
      977   VBD     IN
      941    CC     IN

(Mine runs in 85 sec.  Avg time to tag a sentence: 0.0212 sec)

**It's possible that your numbers won't match this exactly since there could be some randomness in choosing equally-likely tags**

> **Written Answer (a):** Why does the error report say that the model is outputting the same tag (in this case, "IN") so often?  




## Problem 2: Add-λ Smoothed HMM Tagger (40 points)

Implement a class `AddLambdaSmoothedHmmTrainer[Word, Tag]` that extends the trait [`nlpclass.TaggerTrainer[Word, Tag]`](https://github.com/utcompling/nlpclass-fall2013/blob/master/src/main/scala/nlpclass/AssignmentTraits.scala#L179)

With λ=0.1, you should see behavior like this:

{% highlight scala %}
val trainData = ... from the above data ...
val trainer = new AddLambdaSmoothedHmmTrainer[String, String](...)
val model = trainer.train(trainData)

val s1 = Vector(("the", "D"), ("dog", "N"), ("runs", "V"))
val p1 = model.sentenceProb(s1)
println(f"$p1%.4f  ${exp(p1)}%.4f") // -3.6339  0.0264

val s2 = Vector(("the", "D"), ("cat", "N"), ("runs", "V"))
val p2 = model.sentenceProb(s2)
println(f"$p2%.4f  ${exp(p2)}%.4f") // -4.9496  0.0071

val s3 = Vector(("the", "D"), ("man", "N"), ("held", "V"), ("the", "D"), ("saw", "N"))
val p3 = model.sentenceProb(s3)
println(f"$p3%.4f  ${exp(p3)}%.4f") // -13.0951  0.0000

model.tagSentence("the dog runs".split("\\s+").toVector)
// Vector(D, N, V)
model.tagSentence("the cat runs".split("\\s+").toVector)
// Vector(D, N, V)
model.tagSentence("the man held the saw".split("\\s+").toVector)
// Vector(D, N, V, D, N)
{% endhighlight %}

Here are the viterbi tables for each of these sentences, with backpointers for each non-zero cell:

<table>
  <tr> <td>                  </td>  <td><b>&lt;S&gt;</b></td><td></td>  <td><b>the</b>        </td>  <td><b>dog</b></td>  <td><b>runs</b></td>  <td><b>&lt;E&gt;</b></td> </tr>
  <tr> <td> <b>&lt;S&gt;</b> </td>  <td>0.0             </td><td></td>  <td>                  </td>  <td>          </td>  <td>           </td>  <td>                </td> </tr>
  <tr> <td> <b>D</b>         </td>  <td>                </td><td></td>  <td>-0.2469, &lt;S&gt;</td>  <td>-9.1551, D</td>  <td>-9.9552, N </td>  <td>                </td> </tr>
  <tr> <td> <b>N</b>         </td>  <td>                </td><td></td>  <td>-8.6205, &lt;S&gt;</td>  <td>-1.0471, D</td>  <td>-9.9552, N </td>  <td>                </td> </tr>
  <tr> <td> <b>V</b>         </td>  <td>                </td><td></td>  <td>-8.3626, &lt;S&gt;</td>  <td>-8.8972, D</td>  <td>-3.1886, N </td>  <td>                </td> </tr>
  <tr> <td> <b>&lt;E&gt;</b> </td>  <td>                </td><td></td>  <td>                  </td>  <td>          </td>  <td>           </td>  <td>-3.6339, V      </td> </tr>
</table>
&nbsp;   
<table>
  <tr> <td>                  </td>  <td><b>&lt;S&gt;</b></td><td></td>  <td><b>the</b>        </td>  <td><b>cat</b></td>  <td><b>runs</b></td>  <td><b>&lt;E&gt;</b></td> </tr>
  <tr> <td> <b>&lt;S&gt;</b> </td>  <td>0.0             </td><td></td>  <td>                  </td>  <td>          </td>  <td>           </td>  <td>                </td> </tr>
  <tr> <td> <b>D</b>         </td>  <td>                </td><td></td>  <td>-0.2469, &lt;S&gt;</td>  <td>-9.1551, D</td>  <td>-11.2709, N</td>  <td>                </td> </tr>
  <tr> <td> <b>N</b>         </td>  <td>                </td><td></td>  <td>-8.6205, &lt;S&gt;</td>  <td>-2.3627, D</td>  <td>-11.2709, N</td>  <td>                </td> </tr>
  <tr> <td> <b>V</b>         </td>  <td>                </td><td></td>  <td>-8.3626, &lt;S&gt;</td>  <td>-8.8972, D</td>  <td> -4.5043, N</td>  <td>                </td> </tr>
  <tr> <td> <b>&lt;E&gt;</b> </td>  <td>                </td><td></td>  <td>                  </td>  <td>          </td>  <td>           </td>  <td>-4.9496, V      </td> </tr>
</table>


Testing on the `ptbtag` data with λ=1.0 should behave like this:

{% highlight scala %}
val trainData = ... read from ptbtag/train.txt ...
val trainer = new AddLambdaSmoothedHmmTrainer[String, String](...)
val model = trainer.train(trainData)
val s = Vector(("The","DT"), ("man","NN"), ("saw","VBD"), ("a","DT"), ("house","NN"), (".","."))
model.sentenceProb(s)
// -37.56746722307677
model.tagSentence("The man saw a house .".split("\\s+").toVector)
// Vector(DT, NN, VBD, DT, NN, .)
{% endhighlight %}


Add the option `--lambda` to your `main` method to specify the amount of smoothing.

    $ sbt "run-main nlp.a4.Hmm --train ptbtag/train.txt --test ptbtag/dev.txt --lambda 1.0"
    Accuracy: 92.13  (82704/89773)
    count  gold  model
      349    NN     JJ
      241   NNP     JJ
      206    NN    NNP
      159   NNP     NN
      159    JJ     NN
      157    RB     IN
      153   NNS     NN
      151  NNPS    NNP
      142   VBG     NN
      136    JJ     DT

(Mine runs in 87 sec.  Avg time to tag a sentence: 0.0216 sec)

> **Written Answer (a):**  Experiment with different values for `--lambda`.  Report your findings on **ptbtag/dev.txt**.

> **Written Answer (b):**  Using the best value found on ptbdev/dev.txt, report your results on **test.txt**.




## Problem 3: Tag Dictionary (NOT REQUIRED)

In order to improve your tagger, you will now update your implementation to allow for the specification of a **tag dictionary**.  A tag dictionary is a mapping from word types to sets of their potential tags.  For example, *the* may point to the set *{DT}*, while *walks* may point to *{VBZ, NNS}*.

You may want to represent your tag dictionary with something like the following:

{% highlight scala %}
case class TagDictionary[Word, Tag](map: Map[Word, Set[Tag]], allTags: Set[Tag]) {
  def apply(w: Word): Set[Tag] = map.getOrElse(w, allTags)
}
{% endhighlight %}

You should update your **trainer** to have a parameter that determines whether a tag dictionary should be used.  If this parameter says so, then the trainer should create a tag dictionary based on the training data.  So the tag dictionary entry for a particular word will be the set of all tags that were seen with that word in the training data.  If a word was never seen in the training corpus, then you should assume that it can take *any* tag (except the special start or end tags).  Also ensure that the only valid tag for the start word is the start tag and likewise for the end word/tag.

You should also update your **model** to have the newly-constructed tag dictionary as a parameter for use during tagging.  In other words, you should use the tag dictionary to restrict your search for the best tag for each word.

Then update your command-line interface to allow for a tag dictionary option:

* `--tagdict true|false`, which specifies whether a tag dictionary should be used

If the `--tagdict` parameter is `false` or not specified, then no tag dictionary should be used.  Note that this is equivalent to having an empty tag dictionary, where every word is mapped to the set of all tags.

You should be able to run your code like this:

    $ sbt "run-main nlp.a4.Hmm --train ptbtag/train.txt --test ptbtag/dev.txt --tagdict true"
    Accuracy: 84.02  (75430/89773)
    count  gold  model
     1238    DT     JJ
     1232   NNP     IN
      793    NN     JJ
      786    CC     IN
      717    DT     IN
      698    TO     IN
      632   VBD    VBN
      443    JJ     IN
      434    CD     IN
      430   NNP     NN

(Mine runs in 7 sec.  Avg time to tag a sentence: 0.0005 sec)

**It's possible that your numbers won't match this exactly since there could be some randomness in choosing equally-likely tags**

> **Written Answer (a):** Why are the results so dramatically better when the tag dictionary is used on an unsmoothed HMM?


With smoothing, things are slightly trickier.  If a word was unseen during training (ie, it doesn't appear in the tag dictionary), then we still assume it can have any tag (except start/end).  But if a word *is* in the tag dictionary, we want to restrict it accordingly, even after smoothing.  This is not hard to represent in the tag dictionary, but our emission distribution maps from tags to words, not words to tags.  Therefore, the emission distribution for a tag *t* is a smoothed distribution over all words whose tag dictionary entries contain *t*, as well as any words not appearing in the tag dictionary (since they must be allowed with any tag), and *specifically excluding* and words that appear in the tag dictionary but for which $t$ is not in their entry.

You should be able to run your smoothed code like this:

    $ sbt "run-main nlp.a4.Hmm --train ptbtag/train.txt --test ptbtag/dev.txt --tagdict true --lambda 1.0"
    Accuracy: 93.34  (83794/89773)
    count  gold  model
      386   NNP   SBAR
      384   NNP   NNPS
      290    NN     JJ
      202   VBD    VBN
      160    NN    NNP
      140    IN     RB
      126    NN   SBAR
      125   VBN    VBD
      122    JJ   SBAR
      120    JJ     NN

(Mine runs in 8 sec.  Avg time to tag a sentence: 0.0005 sec)




## Problem 4: Pruned Tag Dictionary (NOT REQUIRED)

Unfortunately, it is the case that the Penn Treebank corpus contains a large number of tagging mistakes.  As an example, the word *the* is actually tagged with several tags other than *DT*, even though it is reasonable for the tagger to always assign *DT* to *the*.  These mistakes can lead to confusion in the tagger when it is trying to handle ambiguous words.  

To help the tagger, we can implement a simple strategy for cleaning up the tag dictionary: remove low-probability tags.  So, for a given word, we can remove any tags that occur less than, for example, 10% of the time.  This will remove tags that were mistakenly used on a word, since those mistakes will likely be seen very few times relative to the number of times the word appears.

You should add a parameter `--tdcutoff` that is used to determine the minimum percentage that a tag must occur.  So `--tdcutoff 0.1` will remove any tags that occur less than 10% of the time for a given word.  (If the `--tdcutoff` parameter is given, but `--tagdict` is not, then assume `--tagdict` is `true`.)

    $ sbt "run-main nlp.a4.Hmm --train ptbtag/train.txt --test ptbtag/dev.txt --tagdict true --tdcutoff 0.1"
    Accuracy: 91.35  (82009/89773)
    count  gold  model
     1226   NNP     IN
      455   VBD    VBN
      436    JJ     IN
      434    CD     IN
      420    NN     IN
      393    NN     JJ
      264    VB     NN
      254   NNS     IN
      217    RB     IN
      168   NNP     JJ

(Mine runs in 7 sec.  Avg time to tag a sentence: 0.0004 sec)


When smoothing, you will apply the same technique as above.

    $ sbt "run-main nlp.a4.Hmm --train ptbtag/train.txt --test ptbtag/dev.txt --tagdict true --tdcutoff 0.1 --lambda 1.0"
    Accuracy: 93.40  (83846/89773)
    count  gold  model
      470   NNP   NNPS
      313    NN     JJ
      257   NNP     LS
      212   VBD    VBN
      159    NN    NNP
      143   VBN    VBD
      126    IN     RB
      121    JJ     NN
      108    RB     IN
      105   NNP     JJ

(Mine runs in 7 sec.  Avg time to tag a sentence: 0.0004 sec)
