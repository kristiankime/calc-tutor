import dao.user.UserDAO

import scala.reflect.ClassTag

package object support {

  implicit class EnhancedInjector(injector: play.api.inject.Injector) {

    def instanceOf1[A : ClassTag]                                                         = injector.instanceOf[A]
    def instanceOf2[A : ClassTag, B : ClassTag]                                           = (injector.instanceOf[A], injector.instanceOf[B])
    def instanceOf3[A : ClassTag, B : ClassTag, C : ClassTag]                             = (injector.instanceOf[A], injector.instanceOf[B], injector.instanceOf[C])
    def instanceOf4[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag]               = (injector.instanceOf[A], injector.instanceOf[B], injector.instanceOf[C], injector.instanceOf[D])
    def instanceOf5[A : ClassTag, B : ClassTag, C : ClassTag, D : ClassTag, E : ClassTag] = (injector.instanceOf[A], injector.instanceOf[B], injector.instanceOf[C], injector.instanceOf[D], injector.instanceOf[E])
  }

}
