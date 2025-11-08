package ir.moke.jsysbox.disk;

public record Disk(String blk,
                   String vendor,
                   String model,
                   Long size,
                   Long sectors,
                   PartitionTable partitionTable,
                   Integer partitions,
                   int sectorSize) {
}
