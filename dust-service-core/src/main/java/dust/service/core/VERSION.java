package dust.service.core;

/**
 * Created by huangshengtao on 2017-2-27.
 */
public final class VERSION {
    public final static int MajorVersion    = 1;
    public final static int MinorVersion    = 0;
    public final static int RevisionVersion = 0;

    public static String getVersionNumber() {
        return VERSION.MajorVersion + "." + VERSION.MinorVersion + "." + VERSION.RevisionVersion;
    }
}
