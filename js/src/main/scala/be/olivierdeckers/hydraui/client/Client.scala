package be.olivierdeckers.hydraui.client

import org.scalajs.dom.ext.Ajax
import ujson.Js.Value
import upickle.Js
import upickle.default._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object Client extends autowire.Client[Js.Value, Reader, Writer] {
  override def doCall(req: Request): Future[Js.Value] = {
    Ajax.post( // TODO configurable api host uri
      url = "//localhost:8080/api/" + req.path.mkString("/"),
      data = upickle.json.write(Js.Obj(req.args.toSeq:_*))
    ).map(_.responseText)
      .map(s => upickle.json.read(s))
  }

  override def read[Result: Reader](p: Value): Result = readJs[Result](p)

  override def write[Result: Writer](r: Result): Value = writeJs(r)
}
