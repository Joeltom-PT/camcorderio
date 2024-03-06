package com.camcorderio.camcorderio.entity.user;

public enum ReturnReason {
    DAMAGED("Damaged"),
    WRONG_ITEM("Wrong Item"),
    FIND_ANOTHER("I find new one"),
    OTHER("Other"),
    hiusdhif("");

    private final String label;

    ReturnReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
