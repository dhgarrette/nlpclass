package nlpclass

import scala.collection.JavaConverters._
import dhg.util.CollectionUtil._

/**
 * Tokenize and sentence-split raw text (and lemmatize)
 */
object Tokenize extends (String => Vector[Vector[String]]) {

  private[this] val j = new JStanfordAnnotator

  /**
   * Turn raw text into a Vector of tokenized sentences
   */
  override def apply(text: String): Vector[Vector[String]] = {
    withLemmas(text).map(_.map(_.word))
  }
  
  /**
   * Turn raw text into a Vector of tokenized sentences of (word, lemma) pairs
   */
  def withLemmas(text: String): Vector[Vector[Token]] = {
    j.annotate(text).asScala.toVector.map(sentence =>
      sentence.asScala.toVector.map(tok => Token(clean(tok.word), clean(tok.lemma))))
  }

  /**
   * Replace tokenizer's special characters with normal ones
   */
  def clean(s: String) = {
    s
      .replaceAll("(?i)-LRB-", "(")
      .replaceAll("(?i)-RRB-", ")")
      .replaceAll("(?i)-LSB-", "[")
      .replaceAll("(?i)-RSB-", "]")
      .replaceAll("(?i)-LCB-", "{")
      .replaceAll("(?i)-RCB-", "}")
  }

}

case class Token(word: String, lemma: String)
