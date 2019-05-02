package nlp.a6

import nlpclass._

object A6Check {

  def main(args: Array[String]): Unit = {

    if (false) {
      println("\nPart 1")

      import nlpclass._
      val t = Tree.fromString("(S (NP (D the) (N man)) (VP (V walks) (NP (D the) (N dog))))")
      println(t)
      println(t.pretty)
      //TreeViz.drawTree(t)
    }

    if (false) {
      println("\nPart 2")

      import nlpclass._
      val t = Tree.fromString("(S (NP (D the) (A big) (N dog)) (VP (V walks)))")
      println(t.pretty)

      val c = Cnf.convertTree(t)
      println(c)
      println(c.pretty)

      val u = Cnf.undoTree(c)
      println(u)
      println(u.pretty)
    }

    val trees1 = """
            (V walk)
		    (S (NP (D the) (A big) (N dogs)) (VP (V walk)))
		    (S (NP (D the) (A tall) (N men)) (VP (V walk) (NP (D the) (N dogs))))
    		""".split("\n").map(_.trim).collect { case l if l.nonEmpty => Tree.fromString(l) }.toVector

    if (false) {
      println("\nPart 3")

      import nlpclass._
      val trainer = new UnsmoothedPcfgParserTrainer()
      val pcfg = trainer.train(trees1)
      pcfg.print()

      val t = Tree.fromString("(S (NP (D the) (A tall) (N dogs)) (VP (V walk)))")
      println(t.pretty)
      println(pcfg.likelihood(t))
    }

    val trees2 = """
		    (S (NP (D the) (N dog)) (VP (V barks)))
		    (S (NP (D the) (N dog)) (VP (V walks)))
		    (S (NP (D the) (N man)) (VP (V walks) (NP (D the) (N dog))))
    		""".split("\n").map(_.trim).collect { case l if l.nonEmpty => Tree.fromString(l) }.toVector

    if (false) {
      println("\nPart 4")

      val trainer = new UnsmoothedPcfgParserTrainer()
      val pcfg = trainer.train(trees2)
      pcfg.print()

      val s1 = "(S (NP (D the) (N dog)) (VP (V walks) (NP (D the) (N man)))))"
      println(pcfg.likelihood(Tree.fromString(s1))) // -2.7725887222397816
      val s2 = "(S (NP (D a) (N cat)) (VP (V runs)))"
      println(pcfg.likelihood(Tree.fromString(s2))) // -Infinity
    }

    val trees3 = """
		    (S (NP (D the) (A big) (N dog)) (VP (V barks)))
		    (S (NP (D the) (N dog)) (VP (V walks)))
		    (S (NP (D the) (A tall) (N man)) (VP (V walks) (NP (D the) (N dog))))
		    (S (NP (D the) (N man)) (VP (V saw) (NP (D the) (N dog) (PP (P in) (NP (D a) (N house))))))
		    (S (NP (D the) (N man)) (VP (V saw) (NP (D the) (N dog)) (PP (P with) (NP (D a) (N telescope)))))
    		""".split("\n").map(_.trim).collect { case l if l.nonEmpty => Tree.fromString(l) }.toVector

    if (false) {
      println("\nPart 5")

      val trainer = new UnsmoothedPcfgParserTrainer()
      val pcfg = trainer.train(trees3)
      pcfg.print()

      for (t <- trees3) { println(pcfg.likelihood(t)) }

      val s1 = "the dog walks the man".split(" ").toVector
      println(pcfg.parse(s1).fold("None")(_.pretty))
      val s2 = "a man in the telescope barks the dog with the house with a telescope".split(" ").toVector
      println(pcfg.parse(s2).fold("None")(_.pretty))
      val s3 = "a cat walks".split(" ").toVector
      println(pcfg.parse(s3).fold("None")(_.pretty))
    }

    if (false) {
      println("\nPart 6")

      val trainer = new UnsmoothedPcfgParserTrainer()
      val pcfg = trainer.train(trees3)
      println(pcfg.generate().pretty)

      val treesNoFeatures = """
		    (S (NP (D all) (N dogs)) (VP (V bark)))
		    (S (NP (D a) (N man)) (VP (V walks) (NP (D a) (N dog))))
    		""".split("\n").map(_.trim).collect { case l if l.nonEmpty => Tree.fromString(l) }.toVector

      val treesSgPl = """
		    (S (NP[pl] (D[pl] all) (N[pl] dogs)) (VP[pl] (V[pl] bark)))
		    (S (NP[sg] (D[sg] a) (N[sg] man)) (VP[sg] (V[sg] walks) (NP[pl] (D[pl] all) (N[pl] dogs))))
    		""".split("\n").map(_.trim).collect { case l if l.nonEmpty => Tree.fromString(l) }.toVector
    }

    {
      println("\nPart 7")

      val trainer = new AddLambdaPcfgParserTrainer(0.1)
      //val trainer = new UnsmoothedPcfgParserTrainer()
      val pcfg = trainer.train(trees3)
      pcfg.print()

      val s1l = "(S (NP (D the) (N dog)) (VP (V walks) (NP (D the) (N man)))))"
      println(pcfg.likelihood(Tree.fromString(s1l))) // -5.9925527234040326
      val s2l = "(S (NP (D a) (N cat)) (VP (V runs)))"
      println(pcfg.likelihood(Tree.fromString(s2l))) // -12.245637908262681

      //      val s1 = "the dog walks the man".split(" ").toVector
      //      println(pcfg.parse(s1).fold("None")(_.pretty))
      //      val s2 = "a man in the telescope barks the dog with the house with a telescope".split(" ").toVector
      //      println(pcfg.parse(s2).fold("None")(_.pretty))
      //      val s3 = "a cat walks".split(" ").toVector
      //      println(pcfg.parse(s3).fold("None")(_.pretty))
      val s5 = "a fast cat runs".split(" ").toVector
      println(pcfg.parse(s5).fold("None")(_.pretty))
      val s4 = "oh no ! what's happening ? ahhhhhhh !!!!!!".split(" ").toVector
      println(pcfg.parse(s4).fold("None")(_.pretty))
      //      val s6 = "a big fast cat runs".split(" ").toVector
      //      println(pcfg.parse(s6).fold("None")(_.pretty))
      //      val s7 = "the big big dog barks".split(" ").toVector
      //      println(pcfg.parse(s7).fold("None")(_.pretty))
    }

  }

}
