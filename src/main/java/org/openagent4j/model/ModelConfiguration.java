package org.openagent4j.model;

/**
 * Sampling and output limits for the model call.
 */
public record ModelConfiguration(Double temperature, Integer maxTokenOutput) {

    public static TemperatureStep temperature(double temperature) {
        return new TemperatureStep(temperature);
    }

    /**
     * Fluent entry matching {@code ModelConfiguration.temperature(0).maxTokenOutput(500)}.
     */
    public static final class TemperatureStep {
        private final double temperature;

        private TemperatureStep(double temperature) {
            this.temperature = temperature;
        }

        public ModelConfiguration maxTokenOutput(int maxTokenOutput) {
            return new ModelConfiguration(temperature, maxTokenOutput);
        }
    }
}
