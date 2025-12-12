package ir.moke.jsysbox.disk;

import java.util.Arrays;

public enum MbrType implements FilesystemType {
    EMPTY(0x00),
    FAT12(0x01),
    XENIX_ROOT(0x02),
    XENIX_USR(0x03),
    FAT16_32M(0x04),
    EXTENDED(0x05),
    FAT16(0x06),
    HPFS_NTFS_EXFAT(0x07),
    AIX(0x08),
    AIX_BOOTABLE(0x09),
    OS2_BOOT_MANAG(0x0a),
    W95_FAT32(0x0b),
    W95_FAT32_LBA(0x0c),
    W95_FAT16_LBA(0x0e),
    W95_EXT_LBA(0x0f),
    OPUS(0x10),
    HIDDEN_FAT12(0x11),
    COMPAQ_DIAGNOST(0x12),
    HIDDEN_FAT16(0x14),
    HIDDEN_HPFSNTF(0x17),
    AST_SMARTSLEEP(0x18),
    HIDDEN_W95_FAT3(0x1b),
    HIDDEN_W95_FAT1(0x1e),
    NEC_DOS(0x24),
    HIDDEN_NTFS_WIN(0x27),
    PLAN_9(0x39),
    PARTITIONMAGIC(0x3c),
    VENIX_80286(0x40),
    PPC_PREP_BOOT(0x41),
    SFS(0x42),
    ONTRACK_DM(0x50),
    ONTRACK_DM6_AUX(0x51),
    CPM(0x52),
    ONTRACKDM6(0x54),
    EZ_DRIVE(0x55),
    GOLDEN_BOW(0x56),
    PRIAM_EDISK(0x5c),
    SPEEDSTOR(0x61),
    GNU_HURD_OR_SYS(0x63),
    NOVELL_NETWARE(0x64),
    DISKSECURE_MULT(0x70),
    PCIX(0x75),
    OLD_MINIX(0x80),
    MINIX(0x81),
    LINUX_SWAP(0x82),
    LINUX(0x83),
    OS2_HIDDEN_OR(0x84),
    LINUX_EXTENDED(0x85),
    NTFS_VOLUME_SET(0x86),
    LINUX_PLAINTEXT(0x88),
    LINUX_LVM(0x8e),
    AMOEBA(0x93),
    AMOEBA_BBT(0x94),
    BSDOS(0x9f),
    IBM_THINKPAD_HI(0xa0),
    FREEBSD(0xa5),
    OPENBSD(0xa6),
    NEXTSTEP(0xa7),
    DARWIN_UFS(0xa8),
    NETBSD(0xa9),
    DARWIN_BOOT(0xab),
    HFS(0xaf),
    BSDI_FS(0xb7),
    BSDI_SWAP(0xb8),
    BOOT_WIZARD_HID(0xbb),
    ACRONIS_FAT32_L(0xbc),
    SOLARIS_BOOT(0xbe),
    SOLARIS(0xbf),
    SYRINX(0xc7),
    NON_FS_DATA(0xda),
    DELL_UTILITY(0xde),
    BOOTIT(0xdf),
    DOS_ACCESS(0xe1),
    DOS_RO(0xe3),
    BEOS_FS(0xeb),
    GPT(0xee),
    EFI_FAT_12_16(0xef),
    LINUXPA_RISC_B(0xf0),
    DOS_SECONDARY(0xf2),
    EBBR_PROTECTIVE(0xf8),
    VMWARE_VMFS(0xfb),
    VMWARE_VMKCORE(0xfc),
    LINUX_RAID_AUTO(0xfd),
    LANSTEP(0xfe),
    BBT(0xff);

    private final int code;

    MbrType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static MbrType fromValue(int code) {
        return Arrays.stream(MbrType.values())
                .filter(item -> item.getCode() == code)
                .findFirst()
                .orElse(null);
    }
}
