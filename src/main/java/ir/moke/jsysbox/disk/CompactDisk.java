package ir.moke.jsysbox.disk;

public record CompactDisk(String drive_name,
                          String drive_speed,
                          String drive,
                          String Can_close_tray,
                          String Can_open_tray,
                          String Can_lock_tray,
                          String Can_change_speed,
                          String Can_select_disk,
                          String Can_read_multisession,
                          String Can_read_MCN,
                          String Reports_media_changed,
                          String Can_play_audio,
                          String Can_write_CD_R,
                          String Can_write_CD_RW,
                          String Can_read_DVD,
                          String Can_write_DVD_R,
                          String Can_write_DVD_RAM,
                          String Can_read_MRW,
                          String Can_write_MRW,
                          String Can_write_RAM
) {
}
