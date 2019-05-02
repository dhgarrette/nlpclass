package nlpclass

import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import dhg.util.StringUtil
import dhg.util.StringUtil._
import dhg.util.Pattern._
import nlpclass._
import nlpclass.Utilities._
import com.typesafe.scalalogging.log4j.Logging
import scala.annotation.tailrec

trait DecipherToImplement extends Logging {

  val letters = "abcdefghijklmnopqrstuvwxyz".toVector.map(_.toString)

  /**
   * Run the decipher program
   */
  final def main(args: Array[String]): Unit = {

    val (arguments, options) = CommandLineUtil.parseArgs(args)

    val n = options("n").toInt
    val lambda = options("lambda").toDouble
    val trainsize = options("trainsize").toInt
    val ciphersize = options("ciphersize").toInt
    val trainFile = options("train")
    val ciphertextFile = options("cipher")

    /*
     * A cipher key is represented as a vector of letters, where each entry
     * is a letter-mapping.  So the first letter in the vector is what 'a'
     * is mapped to, the second is what 'b' is mapped to.  So a key starting:
     * Vector("z", "r", ...) would map "abba" to "zrrz".
     */
    val cipherKey = letters.shuffle

    val cipherText = { // read some text and encipher it
      val plainText = fileTokens(ciphertextFile).drop(500).takeSub(ciphersize).toVector
      val encipher = (letters zip cipherKey).toMap
      keyText(encipher, plainText)
    }

    println
    println(f"CIPHER TEXT")
    println
    println(detokenize(cipherText))
    println

    val (finalGuessCipherKey, finalScore) = run(cipherText, n, lambda, trainFile, trainsize, ciphertextFile, ciphersize)

    println("\n\n")
    println(f"${"CIPHER TEXT"}%-50s    DECIPHERED TEXT")
    println
    println(StringUtil.sideBySideStrings(4, detokenize(cipherText), detokenize(decipher(finalGuessCipherKey, cipherText))))
    println

    println("Alphabet:              " + letters.mkString)
    println("Guess cipher encoding: " + finalGuessCipherKey.mkString)
    println("Deciphered:            " + decipher(finalGuessCipherKey, Vector(cipherKey)).head.mkString)
    println("Final score: " + finalScore)

    val solved = cipherKey == finalGuessCipherKey
    println("solved?: " + solved)
  }

  final def run(cipherText: Vector[Vector[String]], n: Int, lambda: Double, trainFile: String, trainsize: Int, ciphertextFile: String, ciphersize: Int) = {
    val trainData = fileTokens(trainFile).drop(100).takeSub(trainsize).toVector
    val ngramModel = train(trainData, n, lambda)
    solve(letters.shuffle, cipherText, ngramModel)
  }

  /**
   * Get tokenized sentences from a file
   *
   * @param	filename	The name of the text file to read
   * @return			Tokenized sentences
   */
  final def fileTokens(filename: String): Iterator[Vector[String]] = {
    File(filename).readLines
      .split("")
      .flatMap(paragraph => Tokenize(paragraph.mkString(" ")))
  }

  /**
   * TO IMPLEMENT
   *
   * Train an ngram language model on the given tokens
   *
   * @param	text	Tokenized sentences to train the language model
   * @param n		The order of the ngram language model
   * @param lambda	The add-lambda smoothing parameter of the model
   * @return		An ngram language model
   */
  def train(text: Vector[Vector[String]], n: Int, lambda: Double): NgramModelToImplement

  /**
   * Iteratively search for a solution to the cipher by improving guesses for
   * the cipher key and evaluating them against the language model.
   *
   * @param currentGuessCipherKey	The current cipher key, to be manipulated
   * @param cipherText				A vector of tokenized sentences
   * @param ngramModel				An ngram language model
   */
  final def solve(initialGuessCipherKey: Vector[String], cipherText: Vector[Vector[String]], ngramModel: NgramModelToImplement) = {
    @tailrec def inner(i: Int, currentGuessCipherKey: Vector[String], currentScore: Double): (Vector[String], Double) = {
      logger.info(f"$i%3d: score=${currentScore}%.2f  ${decipher(currentGuessCipherKey, cipherText).flatten.take(20).mkString(" ")}")

      val pairs = allSwapPairs.shuffle.iterator
      val (bestGuessCipherKey, bestGuessScore) =
        Iterator.fill(50)(pairs.next) // start with a bucket of swap pairs
          .map { case (i, j) => handle(i, j, currentGuessCipherKey, cipherText, ngramModel) }
          .minBy(_._2)

      if (bestGuessScore < currentScore) { // if we found a pair that improves our score
        inner(i + 1, bestGuessCipherKey, bestGuessScore) // recurse with that better cipher key
      }
      else { // didn't find anything better
        pairs // greedily search remaining swap pairs to find one that improves on the guess 
          .map { // swap the letters, decipher the text, and calculate the score
            case (i, j) => handle(i, j, currentGuessCipherKey, cipherText, ngramModel)
          }
          .find { // iterate through the 
            case (newGuessCipherKey, newScore) => newScore < currentScore
          } match {
            case Some((newGuessCipherKey, newScore)) => inner(i + 1, newGuessCipherKey, newScore) // if a better cipher was found, recurse with it
            case None => (currentGuessCipherKey, currentScore) // if no better cipher was found, stop looking and return current
          }
      }
    }
    inner(1, initialGuessCipherKey, scoreCipherKey(cipherText, initialGuessCipherKey, ngramModel))
  }

  /**
   * Get all the swap pairs
   */
  val allSwapPairs = { // 325 total pairs
    val indices = (0 to 25)
    (for (i <- indices; j <- indices if i < j) yield (i, j))
  }

  /**
   * Swap two letter mappings in the current cipher key guess, decipher the
   * text using that new key, and and check how much the output looks like
   * the underlying language.
   *
   * @param i						The cipher key mapping position to swap with j
   * @param j						The cipher key mapping position to swap with i
   * @param currentGuessCipherKey	The current cipher key, to be manipulated
   * @param cipherText				A vector of tokenized sentences
   * @param ngramModel				An ngram language model
   * @return						The new cipher key with transposed letter mappings and the new key's score
   */
  final def handle(i: Int, j: Int, currentGuessCipherKey: Vector[String], cipherText: Vector[Vector[String]], ngramModel: NgramModelToImplement) = {
    val newGuessCipherKey = swapLetters(i, j, currentGuessCipherKey)
    val score = scoreCipherKey(cipherText, newGuessCipherKey, ngramModel)
    logger.debug(f"     ${letters(i)} <-> ${letters(j)};  $score%.2f;  ${decipher(newGuessCipherKey, cipherText).flatten.take(20).mkString(" ")}")
    (newGuessCipherKey, score)
  }

  /**
   * TO IMPLEMENT
   *
   * Swap two letter mappings in the cipher key.
   *
   * @param i			The cipher key mapping position to swap with j
   * @param j			The cipher key mapping position to swap with i
   * @param cipherKey	The current cipher key, to be manipulated
   * @return			A new cipher key, with swapped letter mappings
   */
  def swapLetters(i: Int, j: Int, cipherKey: Vector[String]): Vector[String]

  /**
   * Take a cipher and a ciphertext, and decipher it into a plaintext
   *
   * @param cipherKey		A key to the cipher
   * @param cipherText		Some enciphered text
   * @return				The given ciphertext, deciphered using the key
   */
  final def decipher(cipherKey: Vector[String], cipherText: Vector[Vector[String]]): Vector[Vector[String]] = {
    val key = (cipherKey zip letters).toMap
    keyText(key, cipherText)
  }

  /**
   * Apply a cipher key or decipher key to a text.
   *
   * @param key		Map from lowercase letters to other lowercase letters
   * @param text	A vector of tokenized sentences
   * @return		A vector of tokenized sentences
   */
  final def keyText(key: Map[String, String], text: Vector[Vector[String]]): Vector[Vector[String]] = {
    text.map(sentence =>
      sentence.map(token =>
        token
          .map(_.toString)
          .map(c =>
            key
              .get(c)
              .orElse(key.get(c.toLowerCase).map(_.toUpperCase))
              .getOrElse(c))
          .mkString))
  }

  /**
   * TO IMPLEMENT
   *
   * Assign a score for the cipher key.  The score should be based on how much
   * the output of deciphering the ciphertext with the key looks like the
   * underlying language.
   *
   * @param	cipherText		Text that has been deciphered: tokenized sentences
   * @param cipherKey		Mapping from letters to letters
   * @param ngramModel		An ngram language model
   * @return				A score.  (Lower is better)
   */
  def scoreCipherKey(cipherText: Vector[Vector[String]], cipherKey: Vector[String], ngramModel: NgramModelToImplement): Double

  /**
   * @param text	A vector of tokenized sentences
   */
  final def detokenize(text: Vector[Vector[String]]) = {
    text
      .flatten
      .mkString(" ")
      .replaceAll(" ([^A-Za-z0-9`\\(\\[\\{])", "$1")
      .replaceAll("([`\\(\\[\\{]) ", "$1")
      .replace("\\*", "")
      .wrap(50)
  }

}
