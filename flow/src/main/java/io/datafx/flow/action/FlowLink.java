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
package io.datafx.flow.action;

import io.datafx.flow.FxmlLoadException;
import io.datafx.flow.FlowException;
import io.datafx.flow.FlowHandler;
import io.datafx.core.Assert;

/**
 * A {@link FlowAction} implementation that navigates to a different view in a flow.
 * @param <T> class of the controller of the target view
 */
public class FlowLink<T> implements FlowAction {

    private final Class<T> controllerClazz;

    /**
     * Default constructor of the class
     * @param controllerClazz controller class of the target view
     */
    public FlowLink(final Class<T> controllerClazz) {
        this.controllerClazz = controllerClazz;
    }
	
	@Override
	public void handle(final FlowHandler flowHandler, final String actionId)
			throws FlowException {
        Assert.requireNonNull(flowHandler, "flowHandler");
		try {
            flowHandler.switchView(controllerClazz, false);
        } catch (FxmlLoadException e) {
            throw new FlowException(e);
        }
	}

}
