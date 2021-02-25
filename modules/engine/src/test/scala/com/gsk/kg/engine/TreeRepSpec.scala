package com.gsk.kg.engine

import cats.instances.string._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TreeRepSpec extends AnyFlatSpec with Matchers {

  "TreeRep.draw" should "generate a tree representation" in {

    TreeRep.Node(
      "1",
      Stream(
        TreeRep.Node(
          "2",
          Stream(
            TreeRep.Leaf("5")
          )
        ),
        TreeRep.Node(
          "3",
          Stream(
            TreeRep.Leaf("6")
          )
        ),
        TreeRep.Node(
          "4",
          Stream(
            TreeRep.Leaf("7")
          )
        )
      )
    ).drawTree shouldEqual """1
       ||
       |+- 2
       ||  |
       ||  `- 5
       ||
       |+- 3
       ||  |
       ||  `- 6
       ||
       |`- 4
       |   |
       |   `- 7
       |""".stripMargin

    }

}
