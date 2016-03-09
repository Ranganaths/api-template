package ro.mihneabaia.api.utils.json

import spray.json._

object JsUtils {
  implicit class MergeableJsObject(jsObject: JsObject) {
    def mergeWith(other: JsObject): JsObject = new JsObject(
      jsObject.fields.map {
        case (key, obj: JsObject) =>
          key -> other.fields.get(key).map {
            case o: JsObject => obj ++ o
            case v => v
          }.getOrElse(obj)
        case (key, value) => key -> other.fields.getOrElse(key, value)
      } ++ other.fields.filterNot {
        case (key, value) => jsObject.fields.contains(key)
      }
    )

    def ++(other: JsObject) = this mergeWith other

    def mergeField(field: JsField) = new JsObject(jsObject.fields + field)

    def + = mergeField _
  }

  implicit class FieldDeletableJsObject(jsObject: JsObject) {
    def deleteJsField(key: String) = new JsObject(jsObject.fields.filterKeys(_ != key))

    def - = this deleteJsField _
  }

  implicit def optionToJson[T](implicit writer: JsonWriter[T]): (Option[T]) =>
    JsValue = _.fold[JsValue](JsNull)(_.toJson)

  implicit def objectToJson[T](implicit writer: JsonWriter[T]): (T) => JsValue = writer.write _

  implicit class FieldSuppressibleJsonFormat[T](jsonFormat: JsonFormat[T]) {
    def suppressField(key: String): JsonFormat[T] = new JsonFormat[T] {
      override def write(obj: T): JsValue = jsonFormat.write(obj).asJsObject - key

      override def read(json: JsValue): T = jsonFormat.read(json.asJsObject + ("id" -> JsNull))
    }

    def - = this suppressField _
  }
}
