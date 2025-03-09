package io.github.glandais.gpx.virtual.power;

import io.github.glandais.gpx.virtual.power.aero.AeroPowerProvider;
import io.github.glandais.gpx.virtual.power.cyclist.MuscularPowerProvider;
import io.github.glandais.gpx.virtual.power.grav.GravPowerProvider;
import io.github.glandais.gpx.virtual.power.rolling.RollingResistancePowerProvider;
import io.github.glandais.gpx.virtual.power.rolling.WheelBearingsPowerProvider;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Singleton
public class PowerProviderList {

    final WheelBearingsPowerProvider wheelBearingsPowerProvider;

    final RollingResistancePowerProvider rollingResistancePowerProvider;

    final MuscularPowerProvider muscularPowerProvider;

    final AeroPowerProvider aeroPowerProvider;

    final GravPowerProvider gravPowerProvider;

    public List<PowerProvider> getPowerProviders() {
        return List.of(
                wheelBearingsPowerProvider,
                rollingResistancePowerProvider,
                gravPowerProvider,
                aeroPowerProvider,
                muscularPowerProvider
        );
    }

}
