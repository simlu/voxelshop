package com.vitco.app.core.container;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Temporary fix for loading old files.
 *
 * todo: Remove when new format is ready
 */
public class HackedObjectInputStream extends ObjectInputStream {

        /**
         * Migration table. Holds old to new classes representation.
         */
        private static final Map<String, Class<?>> MIGRATION_MAP = new HashMap<String, Class<?>>();

        static {
            MIGRATION_MAP.put("com.vitco.app.engine.data.container.VoxelLayer", com.vitco.app.core.data.container.VoxelLayer.class);
            MIGRATION_MAP.put("com.vitco.app.engine.data.container.Voxel", com.vitco.app.core.data.container.Voxel.class);
            MIGRATION_MAP.put("com.vitco.app.engine.data.container.ExtendedVector", com.vitco.app.core.data.container.ExtendedVector.class);
            MIGRATION_MAP.put("com.vitco.app.engine.data.container.ExtendedLine", com.vitco.app.core.data.container.ExtendedLine.class);
            MIGRATION_MAP.put("com.vitco.app.engine.data.container.DataContainer", com.vitco.app.core.data.container.DataContainer.class);
            MIGRATION_MAP.put("com.vitco.core.data.container.VoxelLayer", com.vitco.app.core.data.container.VoxelLayer.class);
            MIGRATION_MAP.put("com.vitco.core.data.container.Voxel", com.vitco.app.core.data.container.Voxel.class);
            MIGRATION_MAP.put("com.vitco.core.data.container.ExtendedVector", com.vitco.app.core.data.container.ExtendedVector.class);
            MIGRATION_MAP.put("com.vitco.core.data.container.ExtendedLine", com.vitco.app.core.data.container.ExtendedLine.class);
            MIGRATION_MAP.put("com.vitco.core.data.container.DataContainer", com.vitco.app.core.data.container.DataContainer.class);
        }

        /**
         * Constructor.
         * @param stream input stream
         * @throws IOException if io error
         */
        public HackedObjectInputStream(final InputStream stream) throws IOException {
            super(stream);
        }

        @Override
        protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
            ObjectStreamClass resultClassDescriptor = super.readClassDescriptor();

            if (MIGRATION_MAP.containsKey(resultClassDescriptor.getName())) {
                String replacement = MIGRATION_MAP.get(resultClassDescriptor.getName()).getName();

                try {
                    Field f = resultClassDescriptor.getClass().getDeclaredField("name");
                    f.setAccessible(true);
                    f.set(resultClassDescriptor, replacement);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return resultClassDescriptor;
        }

}
