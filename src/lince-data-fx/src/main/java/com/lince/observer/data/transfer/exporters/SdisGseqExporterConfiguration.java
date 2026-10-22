package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.export.GseqExporterFactory.GseqFormatType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers one {@link SdisGseqExporter} bean per SDIS-GSEQ format (LO-349).
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Configuration(proxyBeanMethods = false)
public class SdisGseqExporterConfiguration {

    @Bean
    public SdisGseqExporter sdisGseqEventExporter(LegacyBridge bridge) {
        return new SdisGseqExporter(bridge, GseqFormatType.EVENT);
    }

    @Bean
    public SdisGseqExporter sdisGseqStateExporter(LegacyBridge bridge) {
        return new SdisGseqExporter(bridge, GseqFormatType.STATE);
    }

    @Bean
    public SdisGseqExporter sdisGseqIntervalExporter(LegacyBridge bridge) {
        return new SdisGseqExporter(bridge, GseqFormatType.INTERVAL);
    }

    @Bean
    public SdisGseqExporter sdisGseqMultieventExporter(LegacyBridge bridge) {
        return new SdisGseqExporter(bridge, GseqFormatType.MULTIEVENT);
    }

    @Bean
    public SdisGseqExporter sdisGseqTimedExporter(LegacyBridge bridge) {
        return new SdisGseqExporter(bridge, GseqFormatType.TIMED);
    }
}
