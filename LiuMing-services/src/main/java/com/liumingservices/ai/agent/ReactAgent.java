package com.liumingservices.ai.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReactAgent extends BaseAgent {
    public abstract void think();

    public abstract void act();

    @Override
    public boolean step() {
        think();

        return false;
    }

    @Override
    public void cleanup() {

    }
}
