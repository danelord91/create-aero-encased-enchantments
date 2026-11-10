package dev.danelord.createaero.mixin;

import com.simibubi.create.impl.registry.SimpleRegistryImpl;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Used only for narrowly guarded replacement of Encased's registered actor. */
@Mixin(value = SimpleRegistryImpl.class, remap = false)
public interface SimpleRegistryAccessor {
    @Accessor("registrations")
    Map<Object, Object> aero$registrations();
}
