public class User {
    private final String name;
    private final Handle handle;

    public User(String n, Handle hndl) {
        this.name = n;
        this.handle = hndl;
    }
    public String getName() {
        return name;
    }

    public Handle getHandle() {
        return handle;
    }
}
