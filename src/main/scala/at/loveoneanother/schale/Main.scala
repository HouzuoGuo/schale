package main.scala.at.loveoneanother.schale

package tmp {

  package object pobj {
    implicit val default: Int = 1
  }

  class A(val newInt: Int) {
    object B {
      def apply()(implicit i: Int) { println(i) }
    }
  }
}

object Main {
  def main(args: Array[String]) {
    implicit val localInt = 1
    new tmp.A(1).B()
  }
}