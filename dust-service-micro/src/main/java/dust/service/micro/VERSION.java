package dust.service.micro;

/**
 * 版本信息
 */
public final class VERSION {
    public static final int MajorVersion = 1;
    public static final int MinorVersion = 0;
    public static final int RevisionVersion = 2;

    public VERSION() {
    }

    public static String getVersionNumber() {
        return VERSION.MajorVersion + "." + VERSION.MinorVersion + "." + VERSION.RevisionVersion;
    }
}
