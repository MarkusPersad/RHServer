package org.markus.rhserver.utils;

import java.util.UUID;

public class UUIDUtils {
    public static String stitchingUUIDs(UUID uuid1, UUID uuid2){
    if (uuid1 == null) {
        throw new IllegalArgumentException("第一个UUID参数不能为空");
    }
    if (uuid2 == null) {
        throw new IllegalArgumentException("第二个UUID参数不能为空");
    }

    String str1 = uuid1.toString();
    String str2 = uuid2.toString();

    if (str1.compareTo(str2) <= 0) {
        return str1 + str2;
    } else {
        return str2 + str1;
    }
}

}
