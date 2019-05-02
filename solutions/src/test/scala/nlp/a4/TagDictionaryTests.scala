package nlp.a4

import org.junit.Test
import dhg.util.TestUtil
import dhg.util.StringUtil._
import nlp.a4.EmHmmInitialization._
import math.{ log, exp, abs, pow }
import org.junit.Assert._
import Double.NaN
import nlp.a1.ProbabilityDistribution
import nlp.a1.ConditionalProbabilityDistribution

class TagDictionaryTests {

  @Test
  def test_SimpleTagDictionaryFactory {
    val cutoff = Some(0.2)
    val f = new SimpleTagDictionaryFactory[Int, Char](cutoff)
    val sentences = Vector(Vector(
      1 -> 'a',
      1 -> 'a',
      1 -> 'a',
      1 -> 'a',
      1 -> 'b',
      1 -> 'b',
      1 -> 'b',
      1 -> 'c',
      2 -> 'b',
      2 -> 'b',
      2 -> 'b',
      2 -> 'b',
      2 -> 'b',
      2 -> 'z',
      7 -> 'a',
      7 -> 'c'))
    val td = f(sentences, 0, 'A', 9, 'Z', Set(7, 8), Set('x', 'y'))

    assertEquals(Set(1, 2, 7, 8), td.allWords)
    assertEquals(Set('a', 'b', 'c', 'x', 'y'), td.allTags)

    assertEquals(Set('A'), td(0))
    assertEquals(Set('a', 'b'), td(1))
    assertEquals(Set('b'), td(2))
    assertEquals(Set('a', 'b', 'c', 'x', 'y'), td(3))
    assertEquals(Set('a', 'c'), td(7))
    assertEquals(Set('a', 'b', 'c', 'x', 'y'), td(8))
    assertEquals(Set('Z'), td(9))
  }

}
