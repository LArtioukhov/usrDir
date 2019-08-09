/*
 *   Wapnee APP
 *    Global data
 *         Copyright (c) 2019. ITC
 *         http://mlsp.gov.by/
 *                Developed by Leonid Artioukhov on 27.03.19 14:32
 */

package itc.globals.exceptions

case class ErrorAppNotInitialized(
  msg: String = "",
  cause: Throwable = None.orNull)
  extends Error(msg, cause)
