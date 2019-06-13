package org.kie.workbench.common.services.datamodeller.driver.package7;

import java.io.Serializable;

import org.kie.workbench.common.services.datamodeller.annotations.PrimitivesAnnotation;

public class TestFieldOrder implements Serializable {

    @PrimitivesAnnotation(
            charParam = '1', charArrayParam = { '1', '2' },
            stringParam = "1", stringArrayParam = { "1", "2" }
    )
    private String field1;


    private String field2;

    @PrimitivesAnnotation(
            stringParam = "3", stringArrayParam = { "3", "4" }
    )
    private String field3;

}
