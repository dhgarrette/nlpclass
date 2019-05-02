package nlp.a3

import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import dhg.util.StringUtil
import dhg.util.StringUtil._
import dhg.util.Pattern._
import nlpclass._
import com.typesafe.scalalogging.log4j.Logging

object Decipher extends DecipherToImplement {

  override def train(text: Vector[Vector[String]], nArg: Int, lambda: Double): NgramModelToImplement = {
    val ngt = new AddLambdaNgramModelTrainer(nArg, lambda)
    val trainData = text.flatten.map(_.map(_.toString).toVector).toVector
    ngt.train(trainData)
  }

  override def scoreCipherKey(cipherText: Vector[Vector[String]], cipherKey: Vector[String], ngramModel: NgramModelToImplement): Double = {
    val decipheredText = decipher(cipherKey, cipherText)
    PerplexityNgramModelEvaluator(ngramModel, decipheredText.flatten.map(_.map(_.toString).toVector))
  }

  override def swapLetters(i: Int, j: Int, cipherKey: Vector[String]): Vector[String] = {
    val (a, b) = if (i < j) (i, j) else (j, i)
    cipherKey.take(a) ++ Vector(cipherKey(b)) ++ cipherKey.slice(a + 1, b) ++ Vector(cipherKey(a)) ++ cipherKey.drop(b + 1)
  }

}
