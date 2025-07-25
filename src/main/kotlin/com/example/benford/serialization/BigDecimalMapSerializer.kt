package com.example.benford.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal

object BigDecimalMapSerializer : KSerializer<Map<Int, BigDecimal>> {
    private val delegate = MapSerializer(Int.serializer(), BigDecimalSerializer)
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: Map<Int, BigDecimal>) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Map<Int, BigDecimal> {
        return delegate.deserialize(decoder)
    }
}