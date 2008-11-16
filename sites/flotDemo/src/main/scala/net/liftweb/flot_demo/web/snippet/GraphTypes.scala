package net.liftweb.flot_demo.web.snippet

import scala.xml.NodeSeq

import net.liftweb.util.Helpers._

import net.liftweb.widgets.flot._

/*
 *
 */

class GraphTypes {

  def render (xhtml: NodeSeq) = {

    def graph () = {

      var d1 : List [Pair [Double, Double]] = Nil

      for (i <- List.range (0, 140, 5))
        d1 = Pair (i / 10.0, Math.sin(i / 10.0)) :: d1

      val d2 : List [Pair [Double, Double]] = (0.0, 3.0) :: (4.0, 8.0) :: (8.0, 5.0) :: (9.0, 13.0) :: Nil

      var d3 : List [Pair [Double, Double]] = Nil
      for (i <- List.range (0, 140, 5))
        d3 = (i / 10.0, Math.cos(i / 10.0)) :: d3

      var d4 : List [Pair [Double, Double]] = Nil
      for (i <- List.range (0, 140, 5))
        d4 = (i / 10.0, Math.sqrt(i * 1.0)) :: d4

      var d5 : List [Pair [Double, Double]] = Nil
      for (i <- List.range (0, 140, 5))
        d5 = (i / 10.0, Math.sqrt(i / 10.0)) :: d5

      val s1 = new FlotSerie () {
        override val data = d1
        override val lines = Some (new FlotLinesOptions () {
          override val show = Some(true)
          override val fill = Some(true)
        })
      }

      val s2 = new FlotSerie () {
        override val data = d2
        override val bars = Some (new FlotBarsOptions () {
          override val show = Some(true)
        })
      }

      val s3 = new FlotSerie () {
        override val data = d3
        override val points = Some (new FlotPointsOptions () {
          override val show = Some(true)
        })
      }

      val s4 = new FlotSerie () {
        override val data = d4
        override val lines = Some (new FlotLinesOptions () {
          override val show = Some(true)
        })
      }

      val s5 = new FlotSerie () {
        override val data = d5
        override val lines = Some (new FlotLinesOptions () {
          override val show = Some(true)
        })
        override val points = Some (new FlotPointsOptions () {
          override val show = Some(true)
        })
      }

      Flot.render ( "ph_graph", s5 :: s4 :: s3 :: s2 :: s1 :: Nil, new FlotOptions ())
    }

    //

    bind ("flot", xhtml, "graph" --> graph)
  }
}