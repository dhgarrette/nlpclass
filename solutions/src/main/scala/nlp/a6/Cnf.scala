package nlp.a6

import nlpclass._

object Cnf {
  def convertTree(t: Tree): CnfTree = {
    t.children match {
      case Vector(child) if child.children.isEmpty => // LEAF
        CnfTreeLeaf(SimpleCnfNonTerm(t.label), child.label)
      case Vector(child) => // UNARY
        UnaryCnfTreeNode(SimpleCnfNonTerm(t.label), Cnf.convertTree(child))
      case Vector(left, right) => // BINARY
        BinaryCnfTreeNode(SimpleCnfNonTerm(t.label), Cnf.convertTree(left), Cnf.convertTree(right))
      case _ => // MORE THAN TWO CHILDREN
        val BinaryCnfTreeNode(_, cL, cR) =
          t.children.map(Cnf.convertTree).reduceRight {
            (c, z) => BinaryCnfTreeNode(CompositeCnfNonTerm(c.nt, z.nt), c, z)
          }
        BinaryCnfTreeNode(SimpleCnfNonTerm(t.label), cL, cR)
    }
  }

  def undoTree(cnfTree: CnfTree): Tree = {
    cnfTree.decnf
  }
}

//
//
//

trait CnfNonTerm
case class CompositeCnfNonTerm(left: CnfNonTerm, right: CnfNonTerm) extends CnfNonTerm { override def toString = f"{$left+$right}" }
case class SimpleCnfNonTerm(label: String) extends CnfNonTerm { override def toString = label }

//

trait CnfTree extends Tree {
  def nt: CnfNonTerm
  def label: String
  def sentence: Vector[String]
  def decnf: TreeNode
}

case class BinaryCnfTreeNode(nt: CnfNonTerm, left: CnfTree, right: CnfTree) extends CnfTree {
  override def label = nt.toString
  override def decnf = {
    nt match { case SimpleCnfNonTerm(lab) => TreeNode(lab, collapseBranching(left).map(_.decnf) ++ collapseBranching(right).map(_.decnf)) }
  }

  private[this] def collapseBranching(n: CnfTree): Vector[CnfTree] = {
    n match {
      case BinaryCnfTreeNode(CompositeCnfNonTerm(l, r), nL, nR) => collapseBranching(nL) ++ collapseBranching(nR)
      case _ => Vector(n)
    }
  }

  override def children = Vector(left, right)
}

case class UnaryCnfTreeNode(nt: CnfNonTerm, child: CnfTree) extends CnfTree {
  override def label = nt.toString
  override def decnf = {
    nt match { case SimpleCnfNonTerm(lab) => TreeNode(lab, Vector(child.decnf)) }
  }

  override def children = Vector(child)
}

case class CnfTreeLeaf(nt: CnfNonTerm, word: String) extends CnfTree {
  override def label = nt.toString
  override def decnf = {
    nt match { case SimpleCnfNonTerm(lab) => TreeNode(lab, Vector(TreeNode(word))) }
  }
  override def children = Vector(new Tree { def label = word; def children = Vector() })
}
