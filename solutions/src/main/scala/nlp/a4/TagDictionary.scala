package nlp.a4

import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import dhg.util.StringUtil._
import nlpclass._
import scalaz._
import Scalaz._

/**
 * ONLY INSTANTIATE THIS VIA THE COMPANION OBJECT
 *
 * A Tag Dictionary is a mapping from words to all of their potential
 * tags. A word not found in the dictionary (including "unknown" words)
 * may take any tag.
 *
 * This class guarantees that looking up the startWord or endWord will
 * return a set containing ony the startTag or endTag, respectively.
 *
 * The allWords property is the complete set of known words excluding
 * the special startWord and endWord.  Likewise for allTags.  For the
 * complete set of known words and tags including these special tags
 * use allWordsSE and allTagsSE.
 */
class TagDictionary[Word, Tag] private (
  map: Map[Word, Set[Tag]],
  val allWords: Set[Word], val allTags: Set[Tag],
  val startWord: Word, val startTag: Tag, val endWord: Word, val endTag: Tag)
  extends (Word => Set[Tag]) {

  def apply(w: Word): Set[Tag] = {
    map.getOrElse(w, allTags)
  }

  def allWordsSE = allWords + (startWord, endWord)
  def allTagsSE = allTags + (startTag, endTag)

  def reversed: Map[Tag, Set[Word]] = ???

  def entriesUNSAFE = map
  def entries: Map[Word, Set[Tag]] = map
  def knownWordsForTag: Map[Tag, Set[Word]] = map.ungroup.map(_.swap).groupByKey.mapVals(_.toSet)

  def withWords(words: Set[Word]) = new TagDictionary(map, allWords ++ words, allTags, startWord, startTag, endWord, endTag)
  def withTags(tags: Set[Tag]) = new TagDictionary(map, allWords, allTags ++ tags, startWord, startTag, endWord, endTag)
}

object TagDictionary {
  def apply[Word, Tag](
    map: Map[Word, Set[Tag]],
    startWord: Word, startTag: Tag, endWord: Word, endTag: Tag,
    additionalWords: Set[Word] = Set[Word](), additionalTags: Set[Tag] = Set[Tag]()) = {

    val allAllWords = additionalWords ++ map.keys
    val allAllTags = additionalTags ++ map.flatMap(_._2)
    new TagDictionary(
      map ++ Map(startWord -> Set(startTag), endWord -> Set(endTag)),
      allAllWords - (startWord, endWord), allAllTags - (startTag, endTag),
      startWord, startTag, endWord, endTag)
  }
}

trait TagDictionaryFactory[Word, Tag] {
  def apply[Word, Tag](
    sentences: Vector[Vector[(Word, Tag)]],
    startWord: Word, startTag: Tag, endWord: Word, endTag: Tag,
    additionalWords: Set[Word] = Set[Word](), additionalTags: Set[Tag] = Set[Tag]() //
    ): TagDictionary[Word, Tag]
}

class SimpleTagDictionaryFactory[Word, Tag](tdCutoff: Option[Double] = None) extends TagDictionaryFactory[Word, Tag] {
  override def apply[Word, Tag](
    taggedSentences: Vector[Vector[(Word, Tag)]],
    startWord: Word, startTag: Tag, endWord: Word, endTag: Tag,
    additionalWords: Set[Word], additionalTags: Set[Tag]) = {
    val tagCounts = taggedSentences.flatten.groupByKey.mapVals(_.counts.normalizeValues)
    tdCutoff match {
      case None =>
        TagDictionary(Map(), startWord, startTag, endWord, endTag,
          additionalWords ++ tagCounts.keySet,
          additionalTags ++ tagCounts.flatMap(_._2.keySet))
      case Some(cutoff) =>
        val pruned = tagCounts.mapVals(_.collect { case (t, p) if p >= cutoff => t }.toSet)
        TagDictionary(pruned, startWord, startTag, endWord, endTag,
          additionalWords,
          additionalTags)
    }
  }
}
