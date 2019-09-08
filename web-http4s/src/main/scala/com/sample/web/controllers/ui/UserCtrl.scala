package com.sample.web.controllers.ui

import cats.effect.Effect
import com.sample.core.storage.SampleDb
import com.sample.lib.scalautils.Extensions._
import com.sample.web.controllers.utils.UiCtrl
import com.sample.web.domain.NoAuthRequest
import org.http4s.{Header, HttpRoutes, Request}

import scala.language.higherKinds

class UserCtrl[F[_] : Effect](db: SampleDb[F]) extends UiCtrl[F] {
  val service: HttpRoutes[F] =  HttpRoutes.of[F] {
    case r@GET -> Root => db.findUsers().flatMap { users =>
      val res = html.UserList(users)(NoAuthRequest(r.asInstanceOf[Request[Any]]))
      Ok(res.toString(), Header("Content-type", "text/html"))
    }
  }
}
