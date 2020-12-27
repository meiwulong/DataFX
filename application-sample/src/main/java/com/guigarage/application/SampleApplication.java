package com.guigarage.application;

import io.datafx.flow.Flow;

public class SampleApplication extends AbstractFlowApplication {

    private ApplicationConfiguration applicationConfiguration;

    @Override
    public void init() throws Exception {
        applicationConfiguration = new ApplicationConfiguration();
    }

    protected Flow createApplicationFlow() {
        return null;
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }


}
