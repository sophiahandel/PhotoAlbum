public enum EDIT_MODE {
    PEN ("pen"),
    TEXT ("text");

    private String modeString;

    EDIT_MODE(String mode) {
        this.modeString = mode;
    }

    public String toString() {
       return modeString;
    }
}
