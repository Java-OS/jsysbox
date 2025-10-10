/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ir.moke.jsysbox.disk;

import ir.moke.jsysbox.JSysboxException;

import java.util.List;

public enum FilesystemType {
    DEV_TMPFS("devtmpfs", null),
    SYSFS("sysfs", null),
    PROC("proc", null),
    EXT3("ext3", "0x83"),
    EXT4("ext4", "0x83"),
    BTRFS("btrfs", "0x83"),
    XFS("xfs", "0x83"),
    NTFS("ntfs", "0x07"),
    SWAP("linux-swap", "0x82"),
    ISO9660("iso9660", null),
    EXT2("ext2", "0x83"),
    FAT16("fat16", "0x0e"),
    FAT32("fat32", "0x0c");

    private final String type;
    private final String code;

    FilesystemType(String type, String code) {
        this.type = type;
        this.code = code;
    }

    public static List<FilesystemType> list() {
        return List.of(FilesystemType.values());
    }

    public static FilesystemType find(String type) throws JSysboxException {
        return list().stream().filter(item -> item.type.equals(type)).findFirst().orElseThrow(() -> new JSysboxException("Filesystem does not supported"));
    }

    public String getType() {
        return type;
    }

    public String getCode() {
        return code;
    }
}
