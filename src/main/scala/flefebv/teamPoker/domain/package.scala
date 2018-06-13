package flefebv.teamPoker

package object domain {

  trait Value[T] extends Any {
    def value: T
  }
}
