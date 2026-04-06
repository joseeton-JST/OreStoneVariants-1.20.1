package io.github.joseetoon.osv.preset.data;

import com.mojang.serialization.Codec;
import io.github.joseetoon.genlib.serialization.HjsonOps;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

public interface DynamicSerializable<T> {
    Codec<T> codec();

    @SuppressWarnings("unchecked")
    default JsonObject toJson() {
        return this.codec().encodeStart(HjsonOps.INSTANCE, (T) this).result()
            .map(JsonValue::asObject)
            .orElseGet(JsonObject::new);
    }
}
