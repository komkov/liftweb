/*
 * Copyright 2007-2010 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liftweb {
package sitemap {

import _root_.net.liftweb.http._
import _root_.net.liftweb.common._
import _root_.net.liftweb.util._
import Helpers._

/**
 * A common trait between Menu and something that can be converted to a Menu.
 * This makes building Lists of things that can be converted to Menu instance
 * easier because there's a common trait.
 */
trait ConvertableToMenu {
  def toMenu: Menu
}

/**
 * A DSL for building menus.
 */
object Menu {
  /**
   * A Menu can be created with the syntax <pre>Menu("Home") / "index"</pre>
   * The first parameter is the LinkText which calculates how Links are presented.  The
   * parameter to Menu will be treated as call-by-name such that it is re-evaluated each time
   * the menu link is needed.  That means you can do <pre>Menu(S ? "Home") / "index"</pre>
   * and the menu link will be localized for each display.
   */
  def apply(linkText: Loc.LinkText[Unit]): PreMenu = this.apply(Helpers.randomString(20), linkText)

  /**
   * A Menu can be created with the syntax <pre>Menu("home_page", "Home") / "index"</pre>
   * The first parameter is the name of the page and the second is the LinkText which calculates how Links are presented.
   * The LinkText
   * parameter to Menu will be treated as call-by-name such that it is re-evaluated each time
   * the menu link is needed.  That means you can do <pre>Menu("home_page", S ? "Home") / "index"</pre>
   * and the menu link will be localized for each display.  You can look up a Menu item by
   * name as well as using the &lt;lift:menu.item name="home_page"&gt; snippet.
   */
  def apply(name: String,linkText: Loc.LinkText[Unit]): PreMenu = new PreMenu(name, linkText)

  /**
   * A convenient way to define a Menu items that's got the same name as it does it's localized LinkText.
   * <pre>Menu.i("Home") / "index"</pre> is short-hand for <pre>Menu("Home", S ? "Home") / "index"</pre>
   */
  def i(nameAndLink: String) = Menu(nameAndLink, S ? nameAndLink)

  /**
   * An intermediate class that holds the basic stuff that's needed to make a Menu item for SiteMap.
   * You must include at least one URI path element by calling the / method
   */
  class PreMenu(name: String, linkText: Loc.LinkText[Unit]) {

    /**
     * The method to add a path element to the URL representing this menu item
     */
    def /(pathElement: String): Menuable = new Menuable(name, linkText, pathElement :: Nil, Nil, Nil)
  }


  class Menuable(val name: String,val linkText: Loc.LinkText[Unit],
                 val path: List[String],
                 val params: List[Loc.LocParam[Unit]],val submenus: List[ConvertableToMenu]) extends ConvertableToMenu
  {
    /**
     * The method to add a path element to the URL representing this menu item
     */
    def /(pathElement: String): Menuable = new Menuable(name, linkText, path ::: List(pathElement), params, submenus)

    /**
     * Append a LocParam to the Menu item
     */
    def rule(param: Loc.LocParam[Unit]): Menuable = >>(param)

    /**
     * Append a LocParam to the Menu item
     */
    def >>(param: Loc.LocParam[Unit]): Menuable =
    new Menuable(name, linkText, path, params ::: List(param), submenus)
    
    /**
     * Define the submenus of this menu item
     */
    def submenus(subs: ConvertableToMenu*): Menuable =
    new Menuable(name, linkText, path, params, submenus ::: subs.toList)

    /**
     * Define the submenus of this menu item
     */
    def submenus(subs: List[ConvertableToMenu]): Menuable =
    new Menuable(name, linkText, path, params, submenus ::: subs.toList)

    /**
     * Convert the Menuable into a Menu instance
     */
    def toMenu: Menu = Menuable.toMenu(this)
  }

  /**
   * The companion object to Menuable that has convenience methods
   */
  object Menuable {
    /**
     * Convert a Menuable into a Menu when you need a Menu.
     */
    implicit def toMenu(able: Menuable): Menu = Menu(Loc(able.name,new Loc.Link(able.path, false),
                                                         able.linkText, able.params), able.submenus :_*)
  }
}

case class Menu(loc: Loc[_], private val convertableKids: ConvertableToMenu*) extends HasKids with ConvertableToMenu {
  lazy val kids: Seq[Menu] = convertableKids.map(_.toMenu)
  private[sitemap] var _parent: Box[HasKids] = Empty
  private[sitemap] var siteMap: SiteMap = _

  private[sitemap] def init(siteMap: SiteMap) {
    this.siteMap = siteMap
    kids.foreach(_._parent = Full(this))
    kids.foreach(_.init(siteMap))
    loc.menu = this
  }

  private[sitemap] def validate {
    _parent.foreach(p => if (p.isRoot_?) throw new SiteMapException("Menu items with root location (\"/\") cannot have children"))
    kids.foreach(_.validate)
  }

  private[sitemap] def testParentAccess: Either[Boolean, Box[() => LiftResponse]] = _parent match {
    case Full(p) => p.testAccess
    case _ => Left(true)
  }

  override private[sitemap] def testAccess: Either[Boolean, Box[() => LiftResponse]] = loc.testAccess

  def toMenu = this

  def findLoc(req: Req): Box[Loc[_]] =
  if (loc.doesMatch_?(req)) Full(loc)
  else first(kids)(_.findLoc(req))

  def locForGroup(group: String): Seq[Loc[_]] =
  (if (loc.inGroup_?(group)) List[Loc[_]](loc) else Nil) ++
  kids.flatMap(_.locForGroup(group))


  override def buildUpperLines(pathAt: HasKids, actual: Menu, populate: List[MenuItem]): List[MenuItem]
  = {
    val kids: List[MenuItem] =
    _parent.toList.flatMap(_.kids.toList.flatMap(m => m.loc.buildItem(if (m == this)
                                                                      populate else
                                                                      Nil, m == actual, m == pathAt)))

    _parent.toList.flatMap(p => p.buildUpperLines(p, actual, kids))
  }
  // def buildChildLine: List[MenuItem] = kids.toList.flatMap(m => m.loc.buildItem(Nil, false, false))

  def makeMenuItem(path: List[Loc[_]]): Box[MenuItem] =
  loc.buildItem(loc.buildKidMenuItems(kids), _lastInPath(path), _inPath(path))

  /**
   * Make a menu item only of the current loc is in the given group
   */
  def makeMenuItem(path: List[Loc[_]], group: String): Box[MenuItem] =
  if (loc.inGroup_?(group)) loc.buildItem(loc.buildKidMenuItems(kids), _lastInPath(path), _inPath(path))
  else Empty

  private def _inPath(in: List[Loc[_]]): Boolean = in match {
    case Nil => false
    case x :: xs if x eq loc => true
    case x :: xs => _inPath(xs)
  }

  private def _lastInPath(path: List[Loc[_]]): Boolean = path match {
    case Nil => false
    case xs => xs.last eq loc
  }

  def breadCrumbs: List[Loc[_]] = _parent match {
    case Full(m: Menu) => m.loc.breadCrumbs
    case _ => Nil
  }
}

}
}
