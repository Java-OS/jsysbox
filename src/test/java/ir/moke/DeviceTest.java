package ir.moke;

import ir.moke.jsysbox.dev.Device;
import ir.moke.jsysbox.dev.JDevice;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DeviceTest {

    @Test
    public void check() {
        List<Device> devices = JDevice.scanDevices();
        for (Device device : devices) {
            System.out.println(device);
        }
    }
}
