package org.gaseumlabs.uhc.util

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

inline fun <reified C: Any>createComplexDataType(
	size: Int,
	crossinline serialize: (C, ByteBuffer) -> Unit,
	crossinline deserialize: (ByteBuffer) -> C
) = object : PersistentDataType<ByteArray, C> {
	override fun getPrimitiveType() = ByteArray::class.java
	override fun getComplexType() = C::class.java

	override fun toPrimitive(complex: C, context: PersistentDataAdapterContext): ByteArray {
		val buffer = ByteBuffer.wrap(ByteArray(size))
		serialize(complex, buffer)
		return buffer.array()
	}

	override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): C {
		return deserialize(ByteBuffer.wrap(primitive))
	}
}

inline fun <reified C: Any>createArrayDataType(
	size: Int,
	crossinline serialize: (C, ByteBuffer) -> Unit,
	crossinline deserialize: (ByteBuffer) -> C
) = object : PersistentDataType<ByteArray, ArrayList<C>> {
	override fun getPrimitiveType() = ByteArray::class.java
	override fun getComplexType() = ArrayList::class.java as Class<ArrayList<C>>

	override fun toPrimitive(complex: ArrayList<C>, context: PersistentDataAdapterContext): ByteArray {
		val buffer = ByteBuffer.wrap(ByteArray(size * complex.size + 4))
		buffer.putInt(complex.size)
		complex.forEach { serialize(it, buffer) }
		return buffer.array()
	}

	override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): ArrayList<C> {
		val buffer = ByteBuffer.wrap(primitive)
		val length = buffer.int
		val list = ArrayList<C>(length)
		for (i in 0 until length) list.add(deserialize(buffer))
		return list
	}
}

abstract class DataProperty<C, T>(private val field: KProperty1<C, T>) {
	fun getValue(complex: C) = field.get(complex)
	abstract fun getSize(value: T): Int
	abstract fun serialize(value: T, buffer: ByteBuffer)
	abstract fun deserialize(buffer: ByteBuffer): T
}

fun <C: Any>createInt(field: KProperty1<C, Int>) = object : DataProperty<C, Int>(field) {
	override fun getSize(int: Int) = 4
	override fun serialize(int: Int, buffer: ByteBuffer) { buffer.putInt(int) }
	override fun deserialize(buffer: ByteBuffer) = buffer.int
}

fun <C: Any>createUUID(field: KProperty1<C, UUID>) = object : DataProperty<C, UUID>(field) {
	override fun getSize(uuid: UUID) = 16
	override fun serialize(uuid: UUID, buffer: ByteBuffer) {
		buffer.putLong(uuid.mostSignificantBits)
		buffer.putLong(uuid.leastSignificantBits)
	}
	override fun deserialize(buffer: ByteBuffer) = UUID(buffer.long, buffer.long)
}

fun <C: Any>createCoords(field: KProperty1<C, Coords>) = object : DataProperty<C, Coords>(field) {
	override fun getSize(coords: Coords) = 8
	override fun serialize(coords: Coords, buffer: ByteBuffer) {
		buffer.putInt(coords.x)
		buffer.putInt(coords.z)
	}
	override fun deserialize(buffer: ByteBuffer) = Coords(buffer.int, buffer.int)
}

fun <C: Any>createBlockPos(field: KProperty1<C, BlockPos>) = object : DataProperty<C, BlockPos>(field) {
	override fun getSize(blockPos: BlockPos) = 12
	override fun serialize(blockPos: BlockPos, buffer: ByteBuffer) {
		buffer.putInt(blockPos.x)
		buffer.putInt(blockPos.y)
		buffer.putInt(blockPos.z)
	}
	override fun deserialize(buffer: ByteBuffer) = BlockPos(buffer.int, buffer.int, buffer.int)
}

fun <C: Any>createString(field: KProperty1<C, String>) = object : DataProperty<C, String>(field) {
	override fun getSize(string: String) = 2 + string.length * 2
	override fun serialize(string: String, buffer: ByteBuffer) {
		buffer.putShort(string.length.toShort())
		string.forEach { char -> buffer.putChar(char) }
	}
	override fun deserialize(buffer: ByteBuffer) = String(CharArray(buffer.short.toInt()) { buffer.char })
}

fun <C: Any>createBoolean(field: KProperty1<C, Boolean>) = object : DataProperty<C, Boolean>(field) {
	override fun getSize(boolean: Boolean) = 1
	override fun serialize(boolean: Boolean, buffer: ByteBuffer) { buffer.put(if (boolean) 1 else 0) }
	override fun deserialize(buffer: ByteBuffer) = buffer.get() == 1.toByte()
}

fun <C: Any>propertyCreators() = arrayOf<Pair<Class<*>, KFunction<DataProperty<C, *>>>>(
	Integer.TYPE to ::createInt,
	UUID::class.java to ::createUUID,
	Coords::class.java to ::createCoords,
	BlockPos::class.java to ::createBlockPos,
	String::class.java to ::createString,
	Boolean::class.java to ::createBoolean,
)

inline fun <reified C: Any>createSuperArrayDataType() = object : PersistentDataType<ByteArray, ArrayList<C>> {
	val constructor = C::class.primaryConstructor ?: throw Error("Class does not have a primary constructor")
	init { constructor.isAccessible = true }
	private val properties: Array<DataProperty<C, Any>>
	init {
		val creators = propertyCreators<C>()
		val namesOrder = constructor.valueParameters.map { it.name ?: throw Error("Class has unnamed argument") }
		val tempProperties = Array<DataProperty<C, Any>?>(namesOrder.size) { null }
		C::class.memberProperties.forEach { field ->
			val index = namesOrder.indexOf(field.name)
			field.isAccessible = true
			val type = field.javaField?.type ?: throw Error("Field has no type")
			tempProperties[index] = creators.find { it.first === type }?.second?.call(field) as DataProperty<C, Any>?
				?: throw Error("Unsupported field type ${type.name}")
		}
		properties = tempProperties.requireNoNulls()
	}

	override fun getPrimitiveType() = ByteArray::class.java
	override fun getComplexType() = ArrayList::class.java as Class<ArrayList<C>>

	override fun toPrimitive(complexList: ArrayList<C>, context: PersistentDataAdapterContext): ByteArray {
		val values = complexList.flatMap { complex -> properties.map { it to it.getValue(complex) } }

		val size = values.sumOf { (property, value) -> property.getSize(value) }
		val buffer = ByteBuffer.wrap(ByteArray(2 + size))

		/* number of elements */
		buffer.putShort(complexList.size.toShort())
		/* the elements serialized */
		values.forEach { (property, value) -> property.serialize(value, buffer) }

		return buffer.array()
	}

	override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): ArrayList<C> {
		val buffer = ByteBuffer.wrap(primitive)

		return Util.createArrayList(buffer.short.toInt()) {
			val values = properties.map { it.deserialize(buffer) }.toTypedArray()
			constructor.call(*values)
		}
	}
}
