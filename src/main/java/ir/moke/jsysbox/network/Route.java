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

package ir.moke.jsysbox.network;

import java.util.Objects;

public class Route {
    private int id;
    private String destination;
    private String netmask;
    private String gateway;
    private String iface;
    private int flags;
    private int use;
    private int metrics;
    private int mtu;
    private int window;
    private int irtt;
    private int refcnt;
    private boolean locked = false;

    public Route(int id, String destination, String netmask, String gateway, String iface, int flags, int use, int metrics, int mtu, int window, int irtt, int refcnt) {
        this.id = id;
        this.destination = destination;
        this.netmask = netmask;
        this.gateway = gateway;
        this.iface = iface;
        this.flags = flags;
        this.use = use;
        this.metrics = metrics;
        this.mtu = mtu;
        this.window = window;
        this.irtt = irtt;
        this.refcnt = refcnt;
    }

    public int getId() {
        return id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getIface() {
        return iface;
    }

    public int getFlags() {
        return flags;
    }

    public int getUse() {
        return use;
    }

    public int getMetrics() {
        return metrics;
    }

    public int getMtu() {
        return mtu;
    }

    public int getWindow() {
        return window;
    }

    public int getIrtt() {
        return irtt;
    }

    public int getRefcnt() {
        return refcnt;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getFlagStr() {
        return RouteFlag.getFlagStr(flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return metrics == route.metrics &&
                Objects.equals(destination, route.destination) &&
                Objects.equals(netmask, route.netmask) &&
                Objects.equals(gateway, route.gateway) &&
                Objects.equals(iface, route.iface);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, netmask, gateway, iface, metrics);
    }
}
