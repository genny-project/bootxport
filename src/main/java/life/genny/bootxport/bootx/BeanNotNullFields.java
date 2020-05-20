package life.genny.bootxport.bootx;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BeanNotNullFields extends BeanUtilsBean {
    private final Log log = LogFactory.getLog(BeanNotNullFields.class);

    private void processDynaBean(final Object orig, final Object dest) throws InvocationTargetException, IllegalAccessException {
        log.info("INSTANCE OF DYNABEAN.");
        final DynaProperty[] origDescriptors = ((DynaBean) orig).getDynaClass().getDynaProperties();
        for (DynaProperty origDescriptor : origDescriptors) {
            final String name = origDescriptor.getName();
            // Need to check isReadable() for WrapDynaBean
            // (see Jira issue# BEANUTILS-61)
            if (getPropertyUtils().isReadable(orig, name)
                    && getPropertyUtils().isWriteable(dest, name)) {
                final Object newValue = ((DynaBean) orig).get(name);
                final Object oldValue = ((DynaBean) dest).get(name);
                if (newValue == null)
                    copyProperty(dest, name, oldValue);
                else
                    copyProperty(dest, name, newValue);
            }
        }
    }

    private void processMap(final Object orig, final Object dest) throws InvocationTargetException, IllegalAccessException {
        log.info("INSTANCE OF MAP.");
        @SuppressWarnings("unchecked") final
        // Map properties are always of type <String, Object>
                Map<String, Object> propMap = (Map<String, Object>) orig;
        for (final Map.Entry<String, Object> entry : propMap.entrySet()) {
            final String name = entry.getKey();
            if (getPropertyUtils().isWriteable(dest, name)) {
                copyProperty(dest, name, entry.getValue());
            }
        }
    }


    private void processJavaBean(final Object orig, final Object dest) throws InvocationTargetException, IllegalAccessException {
        log.info("Java property on standard JavaBean");
        final PropertyDescriptor[] origDescriptors = getPropertyUtils().getPropertyDescriptors(orig);
        for (PropertyDescriptor origDescriptor : origDescriptors) {
            final String name = origDescriptor.getName();
            if ("class".equals(name)) {
                continue; // No point in trying to set an object's class
            }
            if (getPropertyUtils().isReadable(orig, name)
                    && getPropertyUtils().isWriteable(dest, name)) {
                try {
                    final Object newValue = getPropertyUtils().getSimpleProperty(orig, name);
                    final Object oldValue = getPropertyUtils().getSimpleProperty(dest, name);
                    if (newValue == null)
                        copyProperty(dest, name, oldValue);
                    else
                        copyProperty(dest, name, newValue);
                } catch (final NoSuchMethodException e) {
                    // Should not happen
                    log.error(e.getMessage());
                }
            }
        }
    }

    @Override
    public void copyProperties(final Object dest, final Object orig)
            throws IllegalAccessException, InvocationTargetException {
        // Validate existence of the specified beans
        if (dest == null) {
            throw new IllegalArgumentException("No destination bean specified");
        } else if (orig == null) {
            throw new IllegalArgumentException("No origin bean specified");
        } else if (log.isDebugEnabled()) {
            log.debug("BeanUtils.copyProperties(" + dest + ", " + orig + ")");
        }

        // Copy the properties, converting as necessary
        if (orig instanceof DynaBean) {
            processDynaBean(orig, dest);
        } else if (orig instanceof Map) {
            processMap(orig, dest);
        } else {
            processJavaBean(orig, dest);
        }
    }
}
