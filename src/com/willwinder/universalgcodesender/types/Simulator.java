package com.willwinder.universalgcodesender.types;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: MerrellM
 * Date: 2/17/14
 * Time: 10:04 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Simulator {

    protected Collection<String> configStrings;

    public Simulator() {
        configStrings = new ArrayList<>();
    }

    public Simulator(Collection<String> configurationCommands) {
        configStrings = new ArrayList<>(configurationCommands);
    }

    public Collection<String> getConfigStrings() {
        return configStrings;
    }

    public void setConfigStrings(Collection<String> configStrings) {
        this.configStrings = configStrings;
    }

    public abstract long estimateRunLength(Collection<String> commands);
}
