package com.nummulus.amqp.driver.matcher

import org.scalatest.matchers.BeMatcher
import org.scalatest.matchers.MatchResult
import org.scalatest.matchers.Matcher

trait TypeMatcher {
  def ofType[T: Manifest] = BeMatcher { left: Any =>
    val expectedClass = scala.reflect.classTag[T].runtimeClass
    
    val actualClassName = left.getClass.getCanonicalName
    val expectedClassName = expectedClass.getCanonicalName
    
    MatchResult(
        expectedClass.isAssignableFrom(left.getClass),
        s"$actualClassName is not an instance of $expectedClassName",
        s"$actualClassName is an instance of $expectedClassName")
  }
}