package com.willwinder.universalgcodesender;

public class Prompt {
    public String prompt;
    public String defaultValue;

    public Prompt(String prompt, String defaultValue) {
        this.prompt = prompt;
        this.defaultValue = defaultValue;
    } 

    public String toPlaceholder() {
        return "{prompt|"+this.prompt+(this.defaultValue == null ? "" : "|"+this.defaultValue)+"}";
    }

    public String toValuePlaceholder() {
        return "{"+this.prompt+"}";
    }
}
