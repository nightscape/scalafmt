package org.scalafmt

import org.scalafmt.internal.BestFirstSearch
import org.scalafmt.internal.ScalaFmtLogger._

import scala.meta.Source
import scala.meta.Tree
import scala.meta.parseSource
import scala.meta.parsers.common.Parse
import scala.meta.parsers.common.ParseException
import scala.util.control.NonFatal

object ScalaFmt {

  /**
    * Safely format contents of a Scala compilation unit.
    *
    * The original code is returned in case of any unexpected failure.
    *
    * For a more flexible method, see [[format_!]].
    *
    * Note. Formatting sbt files is not yet supported.
    *
    * @param code The code to format.
    * @param style The coding style to use for formatting.
    * @param range EXPERIMENTAL. Format only certain ranges of the file.
    * @return The code formatted if successful, the original code otherwise.
    */
  def format(code: String,
             style: ScalaStyle = ScalaStyle.Default,
             range: Set[Range] = Set.empty[Range]): String = {
    try {
      format_![Source](code, style, range)(parseSource)
    } catch {
      case NonFatal(e) =>
        // TODO(olafur) return more meaningful result type than string.
        code
    }
  }

  /**
    * Formats any kind of Scala tree structure.
    *
    * WARNING. Could run for a very long time on large input.
    *
    * Safe alternatives: [[format]].
    *
    * @param code The source code to format.
    * @param ev See [[scala.meta.parsers]] for available parsers.
    * @tparam T The type of the source code, refer to [[scala.meta.parsers.Api]]
    *           for available types.
    * @return The source code formatted.
    */
  def format_![T <: Tree](code: String,
                          style: ScalaStyle,
                          range: Set[Range] = Set.empty[Range])(
      implicit ev: Parse[T]): String = {
    import scala.meta._
    val source = code.parse[T]
    val graphSearch = new BestFirstSearch(style, source, range)
    graphSearch.formatTree()
  }
}