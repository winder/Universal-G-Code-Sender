package com.willwinder.ugs.designer.io.gerber.expressions;

public record Paramameter(int index) implements Expression {
    public double eval(double[] params) { return params[index]; }
}