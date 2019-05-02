package nlp.a6

import dhg.util.CollectionUtil._
import dhg.util.FileUtil._
import dhg.util.Pattern._
import dhg.util.StringUtil._
import dhg.util.NumberUtil._
import nlpclass._
import nlp.a1.ConditionalProbabilityDistribution
import nlp.a1.ProbabilityDistribution
import com.typesafe.scalalogging.log4j.Logging
import nlpclass.CommandLineUtil
import scala.math.{ log, exp }
import scalaz._
import Scalaz._
import dhg.util.math.LogDouble
import scala.util.Random
import nlp.a1.ProbabilityDistribution
import nlpclass.Tree

trait Production
case class BinaryNonTermProduction(left: CnfNonTerm, right: CnfNonTerm) extends Production
case class UnaryNonTermProduction(child: CnfNonTerm) extends Production
case class TermProduction(word: String) extends Production

class PcfgParser(grammar: ConditionalProbabilityDistribution[CnfNonTerm, Production], starts: ProbabilityDistribution[CnfNonTerm]) extends Parser {
  override def likelihood(t: Tree): Double = {
    def _prob(t: CnfTree): Double = {
      t match {
        case BinaryCnfTreeNode(nt, l, r) => log(grammar(BinaryNonTermProduction(l.nt, r.nt), nt)) + _prob(l) + _prob(r)
        case UnaryCnfTreeNode(nt, c) => log(grammar(UnaryNonTermProduction(c.nt), nt)) + _prob(c)
        case CnfTreeLeaf(nt, w) => log(grammar(TermProduction(w), nt))
      }
    }
    val cnfTree = Cnf.convertTree(t)
    log(starts(cnfTree.nt)) + _prob(cnfTree)
  }

  trait Back
  case class BinaryBack(k: Int, l: CnfNonTerm, r: CnfNonTerm) extends Back
  case class UnaryBack(c: CnfNonTerm) extends Back
  case class LexBack(word: String) extends Back

  override def parse(tokens: Vector[String]): Option[Tree] = {
    // Use Probabilistic CKY (JM p465)

    val table = Array.fill(tokens.length + 1)(Array.fill(tokens.length + 1)(collection.mutable.Map[CnfNonTerm, Double]()))
    val back = Array.fill(tokens.length + 1)(Array.fill(tokens.length + 1)(collection.mutable.Map[CnfNonTerm, Back]()))

    def printTable(): Unit = {
      //val maxLen = table.flatten.flatten.map{case (l,p) => f"$l%-10s -> $p%.2f".length}.max
      val emptyCol = Vector.fill(table.flatten.map(_.size).max + 1)("")
      for ((row, i) <- table.zipWithIndex) {
        println(sideBySide(2, (Vector(f"$i") +: row.zipWithIndex.map {
          case (m, j) =>
            (m match {
              case _ if m.isEmpty && i == (j + 1) => Vector(tokens(i - 1).padLeft(20))
              case _ if m.isEmpty && i == j => Vector(i.toString.padLeft(20))
              case _ => m.toVector.sortBy(-_._2).map { case (l, p) => f"$l%-2s -> $p%.2f" }
            }) :+ (" " * 20)
        }.toVector :+ emptyCol): _*).mkString("\n"))
      }
      println; println
    }

    def printBack(): Unit = {
      //val maxLen = table.flatten.flatten.map{case (l,p) => f"$l%-10s -> $p%.2f".length}.max
      val emptyCol = Vector.fill(back.flatten.map(_.size).max + 1)("")
      for ((row, i) <- back.zipWithIndex) {
        println(sideBySide(2, (Vector(f"$i") +: row.zipWithIndex.map {
          case (m, j) =>
            (m match {
              case _ if m.isEmpty && i == (j + 1) => Vector(tokens(i - 1).padLeft(20))
              case _ if m.isEmpty && i == j => Vector(i.toString.padLeft(20))
              case _ => m.toVector.sortBy { case (a, b) => -table(i)(j)(a) }.map {
                case (a, BinaryBack(k, b, c)) => f"$a%-2s -> ($k, $b, $c)"
                case (a, UnaryBack(b)) => f"$a%-2s -> $b"
                case (a, LexBack(w)) => f"$a%-2s -> $w"
              }
            }) :+ (" " * 20)
        }.toVector :+ emptyCol): _*).mkString("\n"))
      }
      println; println
    }

    for (j <- 1 to tokens.length) {
      // Fill in the bottom of the chart
      {
        val i = j - 1
        val w_j = tokens(i)
        for (a <- grammar.allKnownAs) {
          val p = grammar(TermProduction(w_j), a)
          if (p > 0.0) {
            table(i)(j)(a) = log(p)
            back(i)(j)(a) = LexBack(w_j)
          }
        }
      }

      // Fill in the higher levels of the chart
      for (i <- (j - 1) downto 0) {

        // BINARY RULES
        for (k <- i + 1 to j - 1) {
          for (
            (b, bp) <- table(i)(k);
            (c, cp) <- table(k)(j)
          ) {
            for (a <- grammar.allKnownAs /*+ CompositeCnfNonTerm(b, c)*/ ) {
              val p = //a match {
                //                case CompositeCnfNonTerm(b, c) => 1.0
                //                case _ => 
                grammar(BinaryNonTermProduction(b, c), a)
              //              }
              if (p > 0.0) {
                val p2 = log(p) + bp + cp
                if (!table(i)(j).contains(a) || table(i)(j)(a) < p2) {
                  table(i)(j)(a) = p2
                  back(i)(j)(a) = BinaryBack(k, b, c)
                }
              }
            }
          }
        }

        // UNARY RULES
        var done = false
        while (!done) {
          done = true
          for ((b, bp) <- table(i)(j)) {
            for (a <- grammar.allKnownAs) {
              val p = grammar(UnaryNonTermProduction(b), a)
              if (p > 0.0) {
                val p2 = log(p) + bp
                if (!table(i)(j).contains(a) || table(i)(j)(a) < p2) {
                  table(i)(j)(a) = p2
                  back(i)(j)(a) = UnaryBack(b)
                  done = false
                }
              }
            }
          }
        }
      }
    }

    def _back(nt: CnfNonTerm, i: Int, j: Int): CnfTree = {
      //assert(i == j, f"$i != $j")
      back(i)(j)(nt) match {
        case BinaryBack(k, b, c) => BinaryCnfTreeNode(nt, _back(b, i, k), _back(c, k, j))
        case UnaryBack(b) => UnaryCnfTreeNode(nt, _back(b, i, j))
        case LexBack(w) => CnfTreeLeaf(nt, w)
      }
    }

        printTable()
        printBack()

    val rootOptions = table(0)(tokens.length).map { case (l, p) => l -> (p + log(starts(l))) }.filter(_._2 > Double.NegativeInfinity)
    if (rootOptions.nonEmpty) {
      val root = rootOptions.maxBy(_._2)._1
      val t = _back(root, 0, tokens.length)
      println(t)
      Some(t.decnf)
    }
    else None
  }

  override def generate() = {
    def _generate(nt: CnfNonTerm): CnfTree = {
      grammar.sample(nt) match {
        case BinaryNonTermProduction(l, r) => BinaryCnfTreeNode(nt, _generate(l), _generate(r))
        case UnaryNonTermProduction(c) => UnaryCnfTreeNode(nt, _generate(c))
        case TermProduction(w) => CnfTreeLeaf(nt, w)
      }
    }
    _generate(starts.sample).decnf
  }

  def print(): Unit = {
    val b =
      starts.allKnownProbs.toVector.sortBy(-_._2).map { case (s, p) => ("<S>", s, "", p) } +:
        grammar.allKnownConditionedDistributions.toVector.sortBy(_._1.toString).map {
          case (nt, productionDist) => productionDist.allKnownProbs.toVector.map {
            case (prod, p) =>
              val (l, r) = prod match {
                case BinaryNonTermProduction(l, r) => (l, r)
                case UnaryNonTermProduction(c) => (c, "")
                case TermProduction(w) => (f""""$w"""", "")
              }
              (nt, l, r, p)
          }
        }
    val maxLen = b.flatten.flatMap { case (a, b, c, _) => Set(a, b, c).map(_.toString.length) }.max

    b.foreach { v =>
      v.sortBy {
        case (nt, l, r, p) =>
          (-p, l.toString, r.toString)
      }
        .foreach {
          case (nt, l, r, p) =>
            println(f"${nt.toString.padRight(maxLen)} -> ${l.toString.padRight(maxLen)} ${r.toString.padRight(maxLen)}  [$p%.4f]")
        }
      println
    }
  }
}

abstract class PcfgParserTrainer extends ParserTrainer {
  final protected[this] def getNonTermProductions(t: CnfTree): Vector[(CnfNonTerm, Production)] = {
    t match {
      case BinaryCnfTreeNode(nt, left, right) => getNonTermProductions(left) ++ getNonTermProductions(right) :+ (nt -> BinaryNonTermProduction(left.nt, right.nt))
      case UnaryCnfTreeNode(nt, child) => getNonTermProductions(child) :+ (nt -> UnaryNonTermProduction(child.nt))
      case CnfTreeLeaf(nt: SimpleCnfNonTerm, word) => Vector()
    }
  }

  final protected[this] def getTermProductions(t: CnfTree): Vector[(CnfNonTerm, TermProduction)] = {
    t match {
      case BinaryCnfTreeNode(nt, left, right) => getTermProductions(left) ++ getTermProductions(right)
      case UnaryCnfTreeNode(nt, child) => getTermProductions(child)
      case CnfTreeLeaf(nt: SimpleCnfNonTerm, word) => Vector(nt -> TermProduction(word))
    }
  }
}

class UnsmoothedPcfgParserTrainer() extends PcfgParserTrainer {
  def train(trees: Vector[Tree]): PcfgParser = {
    val cnfTrees = trees.map(Cnf.convertTree)
    val allProductionRuleOccurrences = cnfTrees.flatMap(t => getNonTermProductions(t) ++ getTermProductions(t))
    val grammar = new ConditionalProbabilityDistribution(allProductionRuleOccurrences.groupByKey.mapVals(p => new ProbabilityDistribution(p.counts.mapVals(_.toDouble))))
    val starts = new ProbabilityDistribution(cnfTrees.map(_.nt).counts.mapVals(_.toDouble))
    new PcfgParser(grammar, starts)
  }
}

class AddLambdaPcfgParserTrainer(lambda: Double) extends PcfgParserTrainer {
  def train(trees: Vector[Tree]): PcfgParser = {
    val cnfTrees = trees.map(Cnf.convertTree)
    val allTermProductionRuleOccurrences = cnfTrees.flatMap(getTermProductions) // (P -> w)
    val allNonTermProductionRuleOccurrences = cnfTrees.flatMap(getNonTermProductions) // (A -> B C) | (A -> B)

    // RULE ROOTS
    val allKnownPosTags = allTermProductionRuleOccurrences.map(_._1).toSet // (P -> ___)
    val allKnownSimpleNonPosLabels = allNonTermProductionRuleOccurrences.map(_._1).collect { case a: SimpleCnfNonTerm => a }.toSet[CnfNonTerm] // (A -> ___) where A != {X+Y}
    val allKnownCompositeNonPosLabels = allNonTermProductionRuleOccurrences.map(_._1).collect { case a: CompositeCnfNonTerm => a }.toSet[CnfNonTerm] // ({X+Y} -> ___)
    val allKnownNonTerminalLabels = allKnownPosTags | allKnownSimpleNonPosLabels | allKnownCompositeNonPosLabels

    //
    def fullProductionSet(s: Set[CnfNonTerm]) = {
      val x = s.map(UnaryNonTermProduction(_)).toSet[Production]
      val y = (for (b <- s; c <- s) yield BinaryNonTermProduction(b, c)).toSet[Production]
      x | y
    }

    // PRODUCTIONS
    val allKnownTermProductions = allTermProductionRuleOccurrences.map(_._2).toSet[Production] // (_ -> w)
    val allKnownNonTermProductions = allNonTermProductionRuleOccurrences.map(_._2).toSet // (_ -> B C) | (_ -> B)

    val allProductionRuleOccurrences = allTermProductionRuleOccurrences ++ allNonTermProductionRuleOccurrences
    val productionCounts = allProductionRuleOccurrences.groupByKey.mapVals(_.counts.mapVals(_.toDouble))

    val isTermProduction: (Production => Boolean) = { case _: TermProduction => true; case _ => false }
    val isNonTermProduction = !isTermProduction(_: Production)

    val grammar = new ConditionalProbabilityDistribution(
      productionCounts.map {
        case (nt, c) => nt -> (nt match {
          case _ if allKnownPosTags(nt) => new ProbabilityDistribution(c, Some(allKnownTermProductions), Some(isNonTermProduction), lambda) // (P -> ___), can produce only "w"
          case _: SimpleCnfNonTerm => new ProbabilityDistribution(c, Some(fullProductionSet(allKnownNonTerminalLabels)), Some(isTermProduction), lambda) // (A -> ___) where A != {X+Y}, can produce only non-"w"
          case _: CompositeCnfNonTerm => new ProbabilityDistribution(c) // ({X+Y} -> ___), no smoothing allowed
        })
      })

    val starts = new ProbabilityDistribution(cnfTrees.map(_.nt).counts.mapVals(_.toDouble), Some(allKnownSimpleNonPosLabels | allKnownPosTags), Some(allKnownCompositeNonPosLabels), lambda)
    new PcfgParser(grammar, starts)
  }
}

object Parsing {

  def main(args: Array[String]): Unit = {

    //    println(Tree.fromString(Vector("((S (NP (D the) (A big) (A2 brown) (N dog)) (VP-Int (V walks) (-NONE- *))))")).str(0))
    //    println(Tree.fromString(Vector("( ( S-HLN ( NP-SBJ ( -NONE- *)) ( VP ( VB Atone))))")).str(0))

    //        TreeViz.drawTree(Tree.fromString("( (S (A a) (B b) (C c) (D c)))").cnf)
    //    TreeViz.drawTree(Tree.fromString("( (S (A a) (B b) (C c) (D c)))").cnf.decnf)

    val trees0 = Vector(
      "(S (X (NP (D the) (N dog))))",
      "(S (Y (NP (D a) (N man))))")
      .map(Tree.fromString)
    val trees1 = Vector(
      "( (S (NP (D the) (N dog)) (VP (V barks))))",
      "( (S (NP (D the) (A big) (A brown) (N dog)) (VP (V walks))))",
      "( (S (NP (D the) (N dog)) (VP (V runs) (R quickly))))",
      "( (S (NP (D the) (N man)) (VP (V runs))))",
      "( (S (NP (D the) (A tall) (N man)) (VP (V walks))))",
      "( (S (NP (D the) (N man)) (VP (V walks) (NP (D the) (N dog)))))",
      "( (S (NP (D the) (N man)) (VP (V saw) (NP (D the) (N dog) (PP (P in) (NP (D a) (N house)))))))",
      "( (S (NP (D the) (N man)) (VP (V saw) (NP (D the) (N dog)) (PP (P with) (NP (D a) (N telescope))))))")
      .map(Tree.fromString)
    val trees2 = Vector(
      "( (S (NP (D all) (N dogs)) (VP (V bark))))",
      "( (S (NP (D a) (A big) (N dog)) (VP (V walks))))",
      "( (S (NP (D a) (N dog)) (VP (V runs) (R quickly))))",
      "( (S (NP (D a) (N man)) (VP (V runs))))",
      "( (S (NP (D all) (A tall) (N men)) (VP (V walk))))",
      "( (S (NP (D a) (N man)) (VP (V walks) (NP (D a) (N dog)))))")
      .map(Tree.fromString)
    val trees3 = Vector(
      "( (S (NP[pl] (D[pl] all) (N[pl] dogs)) (VP[pl] (V[pl] bark))))",
      "( (S (NP[sg] (D[sg] a) (A big) (N[sg] dog)) (VP[sg] (V[sg] walks))))",
      "( (S (NP[sg] (D[sg] a) (N[sg] dog)) (VP (V[sg] runs) (R quickly))))",
      "( (S (NP[sg] (D[sg] a) (N[sg] man)) (VP[sg] (V[sg] runs))))",
      "( (S (NP[pl] (D[pl] all) (A tall) (N[pl] men)) (VP[pl] (V[pl] walk))))",
      "( (S (NP[sg] (D[sg] a) (N[sg] man)) (VP[sg] (V[sg] walks) (NP[sg] (D[sg] a) (N[sg] dog)))))")
      .map(Tree.fromString)

    def simplifyLabel(t: Tree) = if (t.isLeaf) t.label else if (t.isPos) t.label.take(2) else t.label.split("-").head
    val ptbTrain = File("parsed/train.txt").readLines.map(s => Tree.fromString(s).rewriteLabels(simplifyLabel))
val ptbDev = File("parsed/dev.txt").readLines.map(s => Tree.fromString(s).rewriteLabels(simplifyLabel))
val ptbTest = File("parsed/test.txt").readLines.map(s => Tree.fromString(s).rewriteLabels(simplifyLabel))
    
	val roots = (ptbTrain ++ ptbDev ++ ptbTest).map(_.label)
	val sortedRoots = roots.counts
	println(sortedRoots.toVector.sortBy(_._1).map{case (k,v) => f"$k -> $v"}.mkString(", "))
    sortedRoots.foreach(println)

    
//    
//    
//    val trees = ptbTrain.toVector
//    val trainer = new AddLambdaPcfgParserTrainer(1)
//    //val trainer = new UnsmoothedPcfgParserTrainer()
//    //val pcfg = trainer.train(trees)
//    //pcfg.print(); println
//    //    for (t <- trees) println(f"${exp(pcfg.treeProb(t))}%.4f  ${pcfg.treeProb(t)}%.4f  ${t.sentence.mkString(" ")}"); println
//    //    for (_ <- 1 to 10) {
//    //      val t = pcfg.generate
//    //      println(t.sentence.mkString(" "))
//    //      println(t.str(0))
//    //      println
//    //    }
//
//    def getProd(t: Tree): Vector[(String, Vector[String])] = {
//      (t.label -> t.children.map(_.label)) +: t.children.flatMap(getProd)
//    }
//    val allProductions = trees.flatMap(getProd)
//    val prodCounts = allProductions.groupByKey.mapVals(_.counts)
//    def prunedCounts(n: Int) = prodCounts.mapVals(_.filter(_._2 > n))
//    for (n <- 0 to 10)
//      println(f"Pruned rules >$n: " + prunedCounts(n).sumBy(_._2.keys.size))
//    println("# NP distinct productions: " + prodCounts("NP").keys.size)
//    println("# VP distinct productions: " + prodCounts("VP").keys.size)
//    println("# IV: " + allProductions.filter(_._1 == "VP").count(_._2 == Vector("V")))
//    println("# TV: " + allProductions.filter(_._1 == "VP").count(_._2 == Vector("V", "NP")))
//
//    prodCounts("VP").toVector.sortBy(-_._2).take(20).foreach {
//      case (v, c) => println(f"$c%10d  ${v.mkString(" ")}")
//    }

    //    println(pcfg.parse("the man saw a big dog with a telescope".split(" ").toVector).fold("None")(_.pretty))
    //
    //    return
    //
    //    val r = pcfg.parse("the man saw a big dog with a telescope".split(" ").toVector)
    //    println(r.getOrElse("None"))
    //    println(r.fold("None")(_.pretty))
    //    //TreeViz.drawTree(r)

  }

}
