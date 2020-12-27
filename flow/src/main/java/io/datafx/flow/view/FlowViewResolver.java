/**
 * Copyright (c) 2011, 2013, Jonathan Giles, Johan Vos, Hendrik Ebbers
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of DataFX, the website javafxdata.org, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.datafx.flow.view;

import io.datafx.flow.action.resource.ControllerResourceConsumer;
import io.datafx.flow.view.resource.ViewControllerAnnotatedType;
import io.datafx.flow.FlowView;
import io.datafx.core.Assert;
import io.datafx.core.DataFXUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class FlowViewResolver {

	@SuppressWarnings("unchecked")
    public static <T> void injectResources(final FlowView<T> view) {
        Assert.requireNonNull(view, "view");
        T obj = view.getController();
        List<ViewControllerAnnotatedType<?, ?>> allResourceTypes = getAnnotatedControllerResourceTypes();
        List<ControllerResourceConsumer<?, ?>> resourceConsumers = getControllerResourceConsumer();

        for (final Field field : DataFXUtils.getInheritedDeclaredFields(view.getControllerClazz())) {
            List<Annotation> fieldAnnotations = Arrays.asList(field.getAnnotations());
            if(!fieldAnnotations.isEmpty()) {
                boolean injected = false;
                for(ViewControllerAnnotatedType currentResourceType : allResourceTypes) {
                    if(field.getAnnotation(currentResourceType.getSupportedAnnotation()) != null) {
                        if(injected) {
                            //TODO: Custom Exception
                            throw new RuntimeException("TODO: double injection of field");
                        }
                        DataFXUtils.setPrivileged(field, obj, currentResourceType.getResource(field.getAnnotation(currentResourceType.getSupportedAnnotation()), view));
                        injected = true;
                    }
                }

                for(ControllerResourceConsumer consumer : resourceConsumers) {
                    if(field.getAnnotation(consumer.getSupportedAnnotation()) != null) {
                        consumer.consumeResource(field.getAnnotation(consumer.getSupportedAnnotation()), DataFXUtils.getPrivileged(field, obj), view);
                    }
                }
            }
        }

    }

    private static List<ViewControllerAnnotatedType<?, ?>> getAnnotatedControllerResourceTypes() {
        var serviceLoader = ServiceLoader.load(ViewControllerAnnotatedType.class);

        //Check if a Annotation is defined with two resourceTypes
        var allResourceTypesIterator = serviceLoader.iterator();
        List<Class<Annotation>> supportedAnnotations = new ArrayList<>();
	    List<ViewControllerAnnotatedType<?, ?>> allResourceTypes = new ArrayList<>();
        while(allResourceTypesIterator.hasNext()) {
            var currentResourceType = allResourceTypesIterator.next();
            if(supportedAnnotations.contains(currentResourceType.getSupportedAnnotation())) {
                //TODO: Custom Exception
                throw new RuntimeException("TODO: Annotation wird doppelt belegt");
            }
            supportedAnnotations.add(currentResourceType.getSupportedAnnotation());
            allResourceTypes.add(currentResourceType);
        }
        return allResourceTypes;
    }

    private static List<ControllerResourceConsumer<?, ?>> getControllerResourceConsumer() {
        var serviceLoader = ServiceLoader.load(ControllerResourceConsumer.class);
        var iterator = serviceLoader.iterator();
        List<ControllerResourceConsumer<?, ?>> ret = new ArrayList<>();
        while(iterator.hasNext()) {
            ret.add(iterator.next());
        }
        return ret;
    }

    public static <T> T createInstanceWithInjections(Class<T> cls, FlowView<?> view)
            throws InstantiationException, IllegalAccessException {
        T instance = cls.newInstance();
//        injectResources(instance, view);
        return instance;
    }
}
