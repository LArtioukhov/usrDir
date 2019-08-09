/*
 *   Wapnee APP
 *    Global data
 *         Copyright (c) 2019. ITC
 *         http://mlsp.gov.by/
 *                Developed by Leonid Artioukhov on 27.03.19 14:32
 */

package itc.globals.actorMessages

sealed trait BaseMessage extends Product with Serializable

case object AlreadyStarted extends BaseMessage
case object AlreadyStopped extends BaseMessage
case object DoStart extends BaseMessage
case object DoStop extends BaseMessage
case object Started extends BaseMessage
case object Stopped extends BaseMessage
case object GetStatus extends BaseMessage
case object Pause extends BaseMessage
case object Continue extends BaseMessage
case object Busy extends BaseMessage
