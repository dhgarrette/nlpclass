package nlpclass

import java.awt.BorderLayout

import org.abego.treelayout.netbeans.AbegoTreeLayoutForNetbeans
import org.netbeans.api.visual.anchor.AnchorFactory
import org.netbeans.api.visual.graph.GraphScene
import org.netbeans.api.visual.layout.LayoutFactory
import org.netbeans.api.visual.widget.ConnectionWidget
import org.netbeans.api.visual.widget.LabelWidget
import org.netbeans.api.visual.widget.LayerWidget
import org.netbeans.api.visual.widget.Widget

import javax.swing.JDialog
import javax.swing.JScrollPane

object TreeViz {

  private[this] class TreeScene(root: String) extends GraphScene[String, String] {

    private[this] val mainLayer = new LayerWidget(this)
    private[this] val connectionLayer = new LayerWidget(this)

    addChild(mainLayer)
    addChild(connectionLayer)

    def addEdge(a: String, b: String): Unit = {
      addEdge(a + "->" + b)
      setEdgeSource(a + "->" + b, a)
      setEdgeTarget(a + "->" + b, b)
    }

    def attachNodeWidget(n: String): Widget = {
      val w = new LabelWidget(this)
      w.setLabel(" " + n + " ")
      mainLayer.addChild(w)
      w
    }

    def attachEdgeWidget(e: String): Widget = {
      val connectionWidget = new ConnectionWidget(this)
      //connectionWidget.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED)
      connectionLayer.addChild(connectionWidget)
      connectionWidget
    }

    def attachEdgeSourceAnchor(edge: String, oldSourceNode: String, sourceNode: String): Unit = {
      val edgeWidget = findWidget(edge).asInstanceOf[ConnectionWidget]
      val sourceNodeWidget = findWidget(sourceNode)
      val sourceAnchor = AnchorFactory.createRectangularAnchor(sourceNodeWidget)
      edgeWidget.setSourceAnchor(sourceAnchor)
    }

    def attachEdgeTargetAnchor(edge: String, oldTargetNode: String, targetNode: String): Unit = {
      val edgeWidget = findWidget(edge).asInstanceOf[ConnectionWidget]
      val targetNodeWidget = findWidget(targetNode)
      val targetAnchor = AnchorFactory.createRectangularAnchor(targetNodeWidget)
      edgeWidget.setTargetAnchor(targetAnchor)
    }
  }

  private[this] def showScene(scene: TreeScene, title: String): Unit = {
    val panel = new JScrollPane(scene.createView())
    val dialog = new JDialog()
    dialog.setModal(true)
    dialog.setTitle(title)
    dialog.add(panel, BorderLayout.CENTER)
    dialog.setSize(800, 600)
    dialog.setVisible(true)
    dialog.dispose()
  }

  private[this] def layoutScene(scene: GraphScene[String, String], root: String): Unit = {
    val graphLayout = new AbegoTreeLayoutForNetbeans[String, String](root, 100, 100, 50, 50, true)
    val sceneLayout = LayoutFactory.createSceneGraphLayout(scene, graphLayout)
    sceneLayout.invokeLayoutImmediately()
  }

  private[this] class Counter(start: Int = 0) {
    private[this] var i = start
    def get = { val c = i; i += 1; c }
  }

  private[this] def createScene(t: Tree): TreeScene = {
    val counter = new Counter
    val i = counter.get
    val scene = new TreeScene(f"${i}_${t.label}")
    def addTree(t: Tree, i: Int) {
      scene.addNode(f"${i}_${t.label}")
      for (c <- t.children) {
        val i2 = counter.get
        addTree(c, i2)
        scene.addEdge(f"${i}_${t.label}", f"${i2}_${c.label}")
      }
    }
    addTree(t, i)
    scene
  }

  def drawTree(t: Tree): Unit = {
    val s = createScene(t)
    layoutScene(s, f"0_${t.label}")
    showScene(s, "")
  }

}
