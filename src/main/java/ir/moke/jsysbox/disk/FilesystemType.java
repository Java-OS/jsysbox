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
    DEV_TMPFS("devtmpfs"),
    SYSFS("sysfs"),
    PROC("proc"),
    EXT3("ext3"),
    EXT4("ext4"),
    BTRFS("btrfs"),
    XFS("xfs"),
    VFAT("vfat"),
    NTFS("ntfs"),
    SWAP("swap"),
    ISO9660("iso9660"),
    ;

    private final String type;

    FilesystemType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static List<FilesystemType> list() {
        return List.of(FilesystemType.values());
    }

    public static FilesystemType find(String type) throws JSysboxException {
        return list().stream().filter(item -> item.type.equals(type)).findFirst().orElseThrow(() -> new JSysboxException("Filesystem does not supported"));
    }

}
