package android.support.v4.content;

import android.content.Context;
import android.net.Uri;

import java.io.File;

public class FileProvider extends androidx.core.content.FileProvider
{
    public static Uri getUriForFile(Context context, String authority, File file)
    {
        return androidx.core.content.FileProvider.getUriForFile(context,authority,file);
    }
}
