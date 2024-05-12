package io.github.glandais.virtual;

import io.github.glandais.virtual.aero.AeroPowerProvider;
import io.github.glandais.virtual.cyclist.CyclistPowerProvider;
import io.github.glandais.virtual.grav.GravPowerProvider;
import io.github.glandais.virtual.rolling.RollingResistancePowerProvider;
import io.github.glandais.virtual.rolling.WheelBearingsPowerProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.inject.Singleton;
import java.util.List;

@RequiredArgsConstructor
@Service
@Singleton
public class PowerProviderList {

    final WheelBearingsPowerProvider wheelBearingsPowerProvider;

    final RollingResistancePowerProvider rollingResistancePowerProvider;

    final CyclistPowerProvider cyclistPowerProvider;

    final AeroPowerProvider aeroPowerProvider;

    final GravPowerProvider gravPowerProvider;

    public List<PowerProvider> getPowerProviders() {
        return List.of(
                wheelBearingsPowerProvider,
                rollingResistancePowerProvider,
                cyclistPowerProvider,
                aeroPowerProvider,
                gravPowerProvider
        );
    }

}
