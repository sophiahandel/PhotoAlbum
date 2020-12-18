public enum VIEW_MODE {
    PHOTO ("photo"),
    GRID ("grid");

    private String modeString;

    VIEW_MODE(String mode) {
        this.modeString = mode;
    }

    public String toString() {
        return modeString;
    }
}
