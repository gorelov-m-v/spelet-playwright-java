package test.framework.device;

/**
 * Description of a testing device including its name and size in pixels.
 */
public record Device(String name, int width, int height) {
    @Override
    public String toString() {
        return name;
    }
}
