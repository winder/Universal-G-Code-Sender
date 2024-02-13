package com.willwinder.universalgcodesender.types;

public enum MacroVersion {
    /**
     * The first version of the macro system.
     * This version allows pseudo-multiline macros separated by a semicolon.
     * It also allows for the use of the {prompt|name} macros and machine and work coordinate substitution.
     * This version is compatible with the newer macro system, but will not be separated in multiple lines.
     */
    V1(1),

    // TODO: Add more documentation when the second version is implemented.
    /**
     * The second version of the macro system.
     * This version will be backwards compatible with the first version, but will allow for the use of
     * multi-line macros and more TBA.
     */
    V2(2);

    private final int version;

    MacroVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }
}
