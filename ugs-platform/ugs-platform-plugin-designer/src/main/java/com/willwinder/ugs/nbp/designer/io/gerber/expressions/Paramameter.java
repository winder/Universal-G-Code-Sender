package com.willwinder.ugs.nbp.designer.io.gerber.expressions;

public record Paramameter(int index) implements Expression {
    public double eval(double[] params) { return params[index]; }
}