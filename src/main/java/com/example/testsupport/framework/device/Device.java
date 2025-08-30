package com.example.testsupport.framework.device;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description of a testing device including its name and size in pixels.
 */
@Data
@NoArgsConstructor
public class Device {
    private String name;
    private int width;
    private int height;

    @Override
    public String toString() {
        return name;
    }
}
