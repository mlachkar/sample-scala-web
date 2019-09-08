package com.sample.web.controllers.ui

import java.util.concurrent.Executors

import cats.effect.{ContextShift, Effect}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.staticcontent.WebjarService.{Config, WebjarAsset}
import org.http4s.server.staticcontent.webjarService

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

class AssetCtrl[F[_] : Effect](implicit cs: ContextShift[F]) extends Http4sDsl[F] {
  val blockingEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
  val service: HttpRoutes[F] = webjarService(
    Config(
      filter = (asset: WebjarAsset) => isJsAsset(asset) || isCssAsset(asset),
      blockingExecutionContext = blockingEc
    )
  )

  def isJsAsset(asset: WebjarAsset): Boolean =
    asset.asset.endsWith(".js")

  def isCssAsset(asset: WebjarAsset): Boolean =
    asset.asset.endsWith(".css")
}
