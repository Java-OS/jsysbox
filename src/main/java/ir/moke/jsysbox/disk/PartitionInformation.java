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

public class PartitionInformation {
    public String blk;
    public String mountPoint;
    public String uuid;
    public String label;
    public String type;
    public long totalSize;
    public Long freeSize;
    public long startSector;
    public long endSector;
    public long sectorSize;

    @Override
    public String toString() {
        return "PartitionInformation{" +
                "blk='" + blk + '\'' +
                ", mountPoint='" + mountPoint + '\'' +
                ", uuid='" + uuid + '\'' +
                ", label='" + label + '\'' +
                ", type='" + type + '\'' +
                ", totalSize=" + totalSize +
                ", freeSize=" + freeSize +
                ", startSector=" + startSector +
                ", endSector=" + endSector +
                ", sectorSize=" + sectorSize +
                '}';
    }
}
