package com.example.config;

import java.util.Collections;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.metrics.MetricsFeature;
import org.apache.cxf.metrics.codahale.CodahaleMetricsProvider;
import org.apache.cxf.tracing.opentracing.OpenTracingFeature;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.codahale.metrics.MetricRegistry;
import com.example.rs.PeopleRestService;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.uber.jaeger.samplers.ProbabilisticSampler;

@Configuration
@EnableAutoConfiguration
@Import(PrometheusConfig.class)
public class AppConfig {

    @Bean(destroyMethod = "destroy")
    public Server jaxRsServer(final Bus bus) {
        
        final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();

        factory.setServiceBean(peopleRestService());
        factory.setProvider(new JacksonJsonProvider());
        factory.setProvider(new OpenTracingFeature(tracer()));
        factory.setBus(bus);
        factory.setAddress("/");
        factory.setFeatures(Collections.singletonList(new MetricsFeature(new CodahaleMetricsProvider(bus))));
        factory.setProperties(Collections.singletonMap("org.apache.cxf.management.service.counter.name", "cxf-services."));
        
        return factory.create();
    }

    @Bean
    public PeopleRestService peopleRestService() {
        return new PeopleRestService(metricRegistry());
    }

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    public io.opentracing.Tracer tracer() {
//        return io.opentracing.contrib.metrics.Metrics.decorate(
//                new com.uber.jaeger.Configuration("jaxrs-server",
//                        new com.uber.jaeger.Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
//                        new com.uber.jaeger.Configuration.ReporterConfiguration()).getTracer(),
//                PrometheusMetricsReporter.newMetricsReporter().build());
        return new com.uber.jaeger.Configuration("jaxrs-server", new com.uber.jaeger.Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
                new com.uber.jaeger.Configuration.ReporterConfiguration())
                .getTracer();
    }
    
}
