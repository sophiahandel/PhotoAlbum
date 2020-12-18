public enum TAG {
    SCHOOL ("school"),
    VACATION ("vacation"),
    FAMILY ("family"),
    WORK ("work");

    private String name;

    TAG(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
