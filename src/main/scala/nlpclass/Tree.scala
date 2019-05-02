package nlpclass

trait Tree {
  def label: String
  def children: Vector[Tree]

  def sentence: Vector[String] = {
    if (children.isEmpty) Vector(label)
    else children.flatMap(_.sentence)
  }

  def isPos = children match { case Vector(c) => c.isLeaf; case _ => false }
  def isLeaf = children.isEmpty

  def rewriteLabels(f: Tree => String): TreeNode = {
    TreeNode(f(this), children.map(_.rewriteLabels(f)))
  }

  override def toString() = {
    children match {
      case Vector(child) if child.children.isEmpty => f"($label ${child.label})"
      case _ => f"($label ${children.mkString(" ")} )"
    }
  }

  def pretty: String = prettyLines.mkString("\n")
  private def prettyLines: Vector[String] = {
    children.flatMap(_.prettyLines) match {
      case Vector(childLine) => Vector(label + " " + childLine)
      case childLines => label +: childLines.map("  " + _)
    }
  }
}

/**
 * A basic Tree implementation
 */
case class TreeNode(label: String, children: Vector[Tree] = Vector()) extends Tree

object Tree {

  /**
   * Turn a string representation of a tree in Penn Treebank style
   * into Tree object.
   */
  def fromString(treeString: String): Tree = {
    val tks = treeString.replace("(", " ( ").replace(")", " ) ").trim.split("\\s+").toVector
    val tokens = tks match {
      case "(" +: (stuff @ ("(" +: _)) :+ ")" => stuff
      case _ => tks
    }

    var i = 0

    def _parse(): Option[TreeNode] = {
      val tok = tokens(i)
      i += 1
      //println((tokens.take(i - 1) ++ Vector("[[" + tokens(i) + "]]") ++ tokens.drop(i)).mkString(" "))
      tok match {
        case "(" =>
          val nonterminal = tokens(i); i += 1
          var children = Vector[TreeNode]()
          while (tokens(i) != ")") {
            _parse().foreach { child =>
              if (nonterminal != "-NONE-") {
                children = children :+ child
              }
            }
          }
          i += 1
          if (children.size > 0)
            Some(TreeNode(nonterminal, children))
          else
            None
        case word => Some(TreeNode(word))
      }
    }

    _parse().get
  }

  private[this] def cleanWord(s: String) = {
    s
      .replaceAll("(?i)-LRB-", "(")
      .replaceAll("(?i)-RRB-", ")")
      .replaceAll("(?i)-LSB-", "[")
      .replaceAll("(?i)-RSB-", "]")
      .replaceAll("(?i)-LCB-", "{")
      .replaceAll("(?i)-RCB-", "}")
  }

}
