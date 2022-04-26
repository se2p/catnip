package de.uni_passau.fim.se2.catnip.common

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode
import org.apache.commons.lang3.reflect.FieldUtils
import org.slf4j.LoggerFactory

object FieldSetter {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /** Sets the field of `node` to the new value.
    *
    * @param node
    *   the element on which the field should be replaced by the new value.
    * @param fieldName
    *   the name of the field to replace the value of.
    * @param newFieldValue
    *   the new value of the field.
    */
  def setField[T](node: T, fieldName: String, newFieldValue: Any): Unit = {
    FieldUtils
      .getAllFields(node.getClass)
      .toList
      .find(_.getName == fieldName) match {
      case Some(field) =>
        field.setAccessible(true)
        FieldUtils
          .writeField(field, node, newFieldValue, true)
      case None =>
        logger.warn(
          s"Could not change field $fieldName of $node because it does not exist!"
        )
    }
  }

  /** Get the current value of a field with the given name.
    * @param node
    *   the ASTNode the fields of which should be inspected.
    * @param fieldName
    *   the name of the field to get the value of.
    * @tparam T
    *   the concrete type of the ASTNode.
    * @return
    *   the current value of the field with name `fieldName`. `None`, if no
    *   field with name `fieldName` could be found.
    */
  def getFieldValue[T <: ASTNode](
      node: T,
      fieldName: String
  ): Option[ASTNode] = {
    Option(FieldUtils.getField(node.getClass, fieldName, true))
      .map(field => { field.setAccessible(true); field })
      .collect(_.get(node) match {
        case f: ASTNode => f
      })
  }

  /** Searches through all fields of this class and superclasses and finds one
    * which holds the given value.
    * @param node
    *   any object.
    * @param fieldValue
    *   the current value the field holds.
    * @tparam T
    *   the concrete type of `node`.
    * @tparam F
    *   the concrete type of the field.
    * @return
    *   the name of the found field, if any was found.
    */
  def getFieldNameWithValue[T, F](node: T, fieldValue: F): Option[String] = {
    FieldUtils
      .getAllFields(node.getClass)
      .map(field => { field.setAccessible(true); field })
      .find(field => field.get(node) == fieldValue)
      .map(_.getName)
  }

  /** Searches through all fields of this class and superclasses and replaces
    * one with `oldFieldValue` with `newFieldValue`.
    * @param node
    *   of which the field should be replaced.
    * @param oldFieldValue
    *   the current value of the field.
    * @param newFieldValue
    *   the value the field should be set to.
    * @tparam T
    *   the concrete type of the class which is searched.
    * @tparam F
    *   the concrete type of the field value that is replaced.
    */
  def replaceValueInField[T, F](
      node: T,
      oldFieldValue: F,
      newFieldValue: F
  ): Unit = {
    getFieldNameWithValue(node, oldFieldValue).map(
      FieldUtils.getField(node.getClass, _, true)
    ) match {
      case Some(field) => field.set(node, newFieldValue)
      case None =>
        logger.warn(
          s"Could not find any field of $node that contained $oldFieldValue!"
        )
    }
  }
}
