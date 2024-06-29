package ir.moke.jsysbox.disk;

public enum PartitionType {
    BTRFS("btrfs"),
    EXT2("ext2"),
    EXT3("ext3"),
    EXT4("ext4"),
    FAT16("fat16"),
    FAT32("fat32"),
    HFS("hfs"),
    hfs_PLUS("hfs+"),
    SWAP("linux-swap"),
    NTFS("ntfs"),
    REISERFS("reiserfs"),
    UDF("udf"),
    XF("xfs");

    private final String type;

    PartitionType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
