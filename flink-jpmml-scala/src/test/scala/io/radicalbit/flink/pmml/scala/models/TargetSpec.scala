package io.radicalbit.flink.pmml.scala.models

import org.scalatest.{Matchers, WordSpec}

class TargetSpec extends WordSpec with Matchers {
  private val target = Target(3.0)
  private val emptyTarget = Target.empty

  "Target" should {

    "With success value Score" in {
      target shouldBe Score(3.0)
    }

    "Empty method return EmptyScore" in {
      emptyTarget shouldBe EmptyScore
    }

    "get method on Score" in {
      target.get shouldBe 3.0
    }

    "get method on EmptyScore" in {
      an[NoSuchElementException] should be thrownBy emptyTarget.get
    }

    "getOrElse on Score" in {
      target.getOrElse(-1.0) shouldBe 3.0
    }

    "getOrElse on EmptyScore" in {
      emptyTarget.getOrElse(-1.0) shouldBe -1.0
    }

  }
}
