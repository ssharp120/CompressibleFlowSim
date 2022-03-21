package Functions;

public class IsentropicRelations {
	public static double staticTemperatureFromTotalTemperature(double totalTemperature, double velocity, double specific_heat_p) {
		if (totalTemperature < 0 || velocity < 0 || specific_heat_p < 0) return 0;
		else return Math.max(0, totalTemperature - velocity * velocity / specific_heat_p / 2);
	}
	
	public static double totalTemperatureFromStaticTemperature(double staticTemperature, double velocity, double specific_heat_p) {
		if (staticTemperature < 0 || velocity < 0 || specific_heat_p < 0) return 0;
		else return Math.max(0, staticTemperature + velocity * velocity / specific_heat_p / 2);
	}
	
	public static double totalStaticTemperatureRatio(double staticTemperature, double velocity, double specific_heat_p) {
		if (velocity < 0 || specific_heat_p < 0) return 1;
		else return 1 + velocity * velocity / specific_heat_p / staticTemperature / 2;
	}
	
	public static double totalStaticPressureRatioFromTotalStaticTemperatureRatio(double totalStaticTemperatureRatio, double specific_heat_k) {
		if (totalStaticTemperatureRatio < 0 || specific_heat_k < 0) return 1;
		else return Math.pow(totalStaticTemperatureRatio, specific_heat_k / (specific_heat_k - 1));
	}
	
	public static double totalStaticDensityRatioFromTotalStaticPressureRatio(double totalStaticTemperatureRatio, double specific_heat_k) {
		if (totalStaticTemperatureRatio < 0 || specific_heat_k < 0) return 1;
		else return Math.pow(totalStaticTemperatureRatio, 1 / (specific_heat_k - 1));
	}
	
	public static double mach(double velocity, double static_temperature, double specific_heat_k, double gas_constant_R) {
		if (velocity < 0 || static_temperature < 0 || specific_heat_k < 0 || gas_constant_R < 0) return 0;
		else return velocity / Math.sqrt(specific_heat_k * gas_constant_R * static_temperature);
	}
}
