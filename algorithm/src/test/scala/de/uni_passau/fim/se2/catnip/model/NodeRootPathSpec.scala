package de.uni_passau.fim.se2.catnip.model

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.util.{
  ASTNodeExtTyped,
  NodeGen,
  NodeListVisitor
}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.{
  Add,
  Minus,
  Mult,
  Round
}
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  NumberLiteral,
  StringLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode

class NodeRootPathSpec extends UnitSpec {
  "The RootPathRoot" should "not allow containing a node with a parent" in {
    val n1    = new NumberLiteral(1.0)
    val round = new Round(n1, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(round)

    the[IllegalArgumentException] thrownBy {
      RootPathRoot(n1)
    } should have message "requirement failed: The parent of a RootPathRoot has to be null!"
  }

  "The RootPathElement with FieldName" should "not allow nodes without a parent" in {
    val n1 = new NumberLiteral(1.0)

    the[IllegalArgumentException] thrownBy {
      RootPathElementField(n1, "someField")
    } should have message "requirement failed: The node of a RootPathElement has to have a parent!"
  }

  "The RootPathElement with Index" should "not allow indices less than 0" in {
    val n1 = new NumberLiteral(1.0)

    the[IllegalArgumentException] thrownBy {
      RootPathElementIndex(n1, -1)
    } should have message "requirement failed: The index of a child cannot be <0!"
  }

  it should "not allow indices larger than the parent’s children list" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add)
    n2.setParentNode(add)

    val _ = RootPathElementIndex(n1, add.getChildren.indexOf(n1))

    the[IllegalArgumentException] thrownBy {
      RootPathElementIndex(n1, 3)
    } should have message "requirement failed: The index of a child cannot be >= than the parent’s children count (3)!"
  }

  it should "not allow nodes without a parent" in {
    val n1 = new NumberLiteral(1.0)

    the[IllegalArgumentException] thrownBy {
      RootPathElementIndex(n1, 0)
    } should have message "requirement failed: The node of a RootPathElement has to have a parent!"
  }

  "RootPath equality" should "not consider objects of different type as equal" in {
    val n1 = new NumberLiteral(1.0)
    val r1 = new NodeRootPath(List(RootPathRoot(n1)))

    r1 should not equal n1
    n1 should not equal r1
  }

  it should "not consider paths of different length as equal" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add)
    n2.setParentNode(add)
    val n3   = new NumberLiteral(3.0)
    val mult = new Mult(add, n3, NodeGen.generateNonDataBlockMetadata())
    add.setParentNode(mult)
    n3.setParentNode(mult)

    val p1 = NodeRootPath(n1)
    val p2 = NodeRootPath(add)

    p1 should not equal p2
    p2 should not equal p1
  }

  it should "consider them equal only if the root element nodes have the same class" in {
    val n1    = new NumberLiteral(1.0)
    val path1 = NodeRootPath(List(RootPathRoot(n1)))

    val n2    = new NumberLiteral(2.0)
    val path2 = NodeRootPath(List(RootPathRoot(n2)))

    val s1    = new StringLiteral("s")
    val path3 = NodeRootPath(List(RootPathRoot(s1)))

    path1 shouldEqual path2
    path2 shouldEqual path1
    path1 should not equal path3
    path3 should not equal path1
  }

  it should "consider them equal only if the node classes and fieldNames are equal for path elements with fields" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add)
    n2.setParentNode(add)

    val root = RootPathRoot(add)
    val r1   = RootPathElementField(n1, "operand1")
    val r2   = RootPathElementField(n2, "operand2")

    val s1 = new StringLiteral("x")
    s1.setParentNode(add)
    val r3 = RootPathElementField(s1, "operand1")

    val path1 = NodeRootPath(List(root, r1))
    val path2 = NodeRootPath(List(root, r2))
    val path3 = NodeRootPath(List(root, r3))

    path1 should not equal path2
    path2 should not equal path1
    path1 should not equal path3
    path3 should not equal path1
    path2 should not equal path3
    path3 should not equal path2
  }

  it should "consider them equal only if the node classes and indices are equal for path elements with indices" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add)
    n2.setParentNode(add)

    val root = RootPathRoot(add)
    val r1   = RootPathElementIndex(n1, 0)
    val r2   = RootPathElementIndex(n2, 1)

    val s1 = new StringLiteral("x")
    s1.setParentNode(add)
    val r3 = RootPathElementIndex(s1, 0)

    val path1 = NodeRootPath(List(root, r1))
    val path2 = NodeRootPath(List(root, r2))
    val path3 = NodeRootPath(List(root, r3))

    path1 should not equal path2
    path2 should not equal path1
    path1 should not equal path3
    path3 should not equal path1
    path2 should not equal path3
    path3 should not equal path2
  }

  it should "consider them not equal if elements of the path are of different type" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add)
    n2.setParentNode(add)

    val root = RootPathRoot(add)
    val r1   = RootPathElementIndex(n1, 0)
    val r2   = RootPathElementField(n2, "operand2")

    val path1 = NodeRootPath(List(root, r1))
    val path2 = NodeRootPath(List(root, r2))

    path1 should not equal path2
    path2 should not equal path1
  }

  "The NodeRootPath" should "never be empty" in {
    the[IllegalArgumentException] thrownBy {
      NodeRootPath(List())
    } should have message "requirement failed: The root path cannot be empty!"
  }

  it should "have to contain at least a RootElement" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add)
    n2.setParentNode(add)

    val root = RootPathElementIndex(n1, 0)

    the[IllegalArgumentException] thrownBy {
      NodeRootPath(List(root))
    } should have message "requirement failed: The root path has to contain a RootPathRoot as first element!"
  }

  it should "only contain the node itself if it has no parent" in {
    val n1 = new NumberLiteral(1.0)
    NodeRootPath(n1).path should contain only RootPathRoot(n1)
  }

  it should "start with the root and have the node itself last" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add)
    n2.setParentNode(add)

    val n3    = new NumberLiteral(3.0)
    val minus = new Minus(n3, add, NodeGen.generateNonDataBlockMetadata())
    n3.setParentNode(minus)
    add.setParentNode(minus)

    val actual = NodeRootPath(n1)
    actual.path should contain theSameElementsInOrderAs Seq(
      RootPathRoot(minus),
      RootPathElementField(add, "operand2"),
      RootPathElementField(n1, "operand1")
    )
    actual(1) shouldBe RootPathElementField(add, "operand2")
  }

  "Following a root path" should "yield the same path as before in the same program" in {
    val p =
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if_if.sb3")

    {
      val setVariableTo = NodeListVisitor(p, matchesSetVarTo(_, 0)).head
      val rootPath      = NodeRootPath(setVariableTo)
      NodeRootPath.followRootPath(p, rootPath) should be(rootPath)
    }

    {
      val setVariableTo = NodeListVisitor(p, matchesSetVarTo(_, 100)).head
      val rootPath      = NodeRootPath(setVariableTo)
      NodeRootPath.followRootPath(p, rootPath) should be(rootPath)
    }
  }

  it should "yield the same path in a cloned program" in {
    val p =
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if_if.sb3")

    // node and path in the old program
    val setVariableTo = NodeListVisitor(p, matchesSetVarTo(_, 0)).head
    val rootPath      = NodeRootPath(setVariableTo)

    val p2 = p.cloned

    val foundPath = NodeRootPath.followRootPath(p2, rootPath)
    foundPath.path should have length rootPath.path.length
    forAll(foundPath.path.zip(rootPath.path)) { case (a, b) =>
      a.getClass should be(b.getClass)
    }
    foundPath shouldEqual rootPath
  }

  private def matchesSetVarTo(node: ASTNode, value: Double): Boolean = {
    node.isInstanceOf[SetVariableTo] && node
      .asInstanceOf[SetVariableTo]
      .getExpr == new NumberLiteral(value)
  }
}
